package com.cms.controller;

import com.cms.dto.BulkUploadResponse;
import com.cms.service.BulkUploadService;
import com.cms.util.ExcelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 🎓 LESSON: File Upload Controller
 * ============================================================================
 * 
 * Handling file uploads in Spring Boot is straightforward!
 * 
 * KEY CONCEPTS:
 * -------------
 * 
 * MultipartFile:
 * - Spring's interface for uploaded files
 * - Provides methods: getBytes(), getInputStream(), getOriginalFilename()
 * - Automatically handles multipart/form-data encoding
 * 
 * @RequestParam("file"):
 * - Binds uploaded file to method parameter
 * - Parameter name must match form field name
 * 
 * FRONTEND REQUEST (From React):
 * -------------------------------
 * const formData = new FormData();
 * formData.append('file', selectedFile);
 * 
 * axios.post('/api/bulk/upload', formData, {
 * headers: { 'Content-Type': 'multipart/form-data' }
 * });
 * 
 * ============================================================================
 */
@RestController
@RequestMapping("/api/bulk")
@CrossOrigin(origins = "http://localhost:3000")
public class BulkUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BulkUploadController.class);

    @Autowired
    private BulkUploadService bulkUploadService;

    /**
     * 🎓 UPLOAD ENDPOINT
     * ============================================================================
     * 
     * This endpoint handles Excel file upload and bulk customer creation.
     * 
     * EXAMPLE REQUEST:
     * ----------------
     * POST /api/bulk/upload
     * Content-Type: multipart/form-data
     * 
     * Form Data:
     * file: [Excel file]
     * 
     * EXAMPLE RESPONSE:
     * -----------------
     * {
     * "totalRecards": 1000,
     * "successCount": 995,
     * "failureCount": 5,
     * "processingTimeMs": 15000,
     * "errors": [
     * {
     * "rowNumber": 5,
     * "field": "nicNumber",
     * "message": "Duplicate NIC number"
     * },
     * ...
     * ]
     * }
     * 
     * ============================================================================
     */
    @PostMapping("/upload")
    public ResponseEntity<BulkUploadResponse> uploadCustomers(
            @RequestParam("file") MultipartFile file) {
        logger.info("Received bulk upload request: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        /**
         * 🎓 FILE VALIDATION:
         * 
         * Always validate uploaded files:
         * 1. Is it present?
         * 2. Is it the correct format?
         * 3. Is it within size limits?
         */

        // Check if file is present
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded");
            return ResponseEntity.badRequest().build();
        }

        // Check file format
        if (!ExcelHelper.hasExcelFormat(file)) {
            logger.warn("Invalid file format: {}", file.getContentType());
            BulkUploadResponse response = new BulkUploadResponse();
            response.addError(0, null, "Invalid file format. Please upload an Excel file (.xlsx)");
            return ResponseEntity.badRequest().body(response);
        }

        /**
         * 🎓 FILE SIZE CHECK:
         * 
         * We configured max file size in application.properties:
         * spring.servlet.multipart.max-file-size=100MB
         * 
         * Spring automatically rejects larger files with error.
         * But we can add additional checks here if needed.
         */

        try {
            // Process the upload
            BulkUploadResponse response = bulkUploadService.processBulkUpload(file);

            logger.info("Bulk upload completed: {} succeeded, {} failed in {}ms",
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    response.getProcessingTimeMs());

            /**
             * 🎓 RESPONSE STATUS:
             * 
             * We return 200 OK even if some records failed.
             * Why? Because the operation itself succeeded.
             * 
             * Client checks response.failureCount to see if there were errors.
             * 
             * Alternative: Return 207 MULTI-STATUS for partial success
             */
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing bulk upload", e);

            BulkUploadResponse response = new BulkUploadResponse();
            response.addError(0, null, "Failed to process file: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🎓 DOWNLOAD TEMPLATE ENDPOINT
     * ============================================================================
     * 
     * Provides users with a sample Excel template to fill in.
     * 
     * This is a FILE DOWNLOAD endpoint.
     * 
     * EXAMPLE REQUEST:
     * ----------------
     * GET /api/bulk/template
     * 
     * RESPONSE:
     * ---------
     * Content-Type:
     * application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
     * Content-Disposition: attachment; filename=customer_template.xlsx
     * 
     * [Excel file bytes]
     * 
     * ============================================================================
     */
    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        logger.info("Template download requested");

        try {
            /**
             * 🎓 GENERATING FILE:
             * 
             * ExcelHelper.createSampleTemplate() generates Excel bytes in memory.
             * We wrap it in ByteArrayResource for Spring to send as HTTP response.
             */
            byte[] excelBytes = ExcelHelper.createSampleTemplate();
            ByteArrayResource resource = new ByteArrayResource(excelBytes);

            /**
             * 🎓 HTTP HEADERS FOR FILE DOWNLOAD:
             * 
             * Content-Type: Tells browser it's an Excel file
             * Content-Disposition: Tells browser to download (not display)
             * - attachment: Download as file
             * - filename=...: Suggested filename
             * Content-Length: File size in bytes
             */
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=customer_template.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(excelBytes.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error generating template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 ADDITIONAL ENDPOINT IDEAS:
     * ============================================================================
     * 
     * 1. PROGRESS ENDPOINT:
     * For large uploads, process asynchronously and provide progress:
     * 
     * @PostMapping("/upload/async")
     * public ResponseEntity<String> uploadAsync(@RequestParam("file") MultipartFile
     * file) {
     * String jobId = UUID.randomUUID().toString();
     * // Process in background
     * return ResponseEntity.ok(jobId);
     * }
     * 
     * @GetMapping("/upload/status/{jobId}")
     * public ResponseEntity<UploadStatus> getUploadStatus(@PathVariable String
     * jobId) {
     * // Return progress percentage
     * }
     * 
     * 2. ERROR REPORT DOWNLOAD:
     * If bulk upload has errors, allow downloading error report:
     * 
     * @GetMapping("/errors/{uploadId}")
     * public ResponseEntity<ByteArrayResource> downloadErrors(@PathVariable String
     * uploadId) {
     * // Generate Excel with error details
     * }
     * 
     * 3. VALIDATE BEFORE UPLOAD:
     * Pre-validate file without saving:
     * 
     * @PostMapping("/validate")
     * public ResponseEntity<ValidationResult> validateFile(@RequestParam("file")
     * MultipartFile file) {
     * // Check for errors without saving
     * }
     * 
     * ============================================================================
     */
}
