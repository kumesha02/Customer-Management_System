package com.cms.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

/**
 * 🎓 LESSON: Apache POI - Excel File Processing
 * ============================================================================
 * 
 * Apache POI is the Java library for Microsoft Office documents.
 * 
 * KEY CONCEPTS:
 * --------------
 * 
 * 1. WORKBOOK: An Excel file (.xls or .xlsx)
 * - HSSFWorkbook: Old .xls format (Excel 97-2003)
 * - XSSFWorkbook: New .xlsx format (Excel 2007+)
 * - SXSSFWorkbook: Streaming version for large files
 * 
 * 2. SHEET: A tab/worksheet in Excel
 * - Workbook contains multiple sheets
 * 
 * 3. ROW: A horizontal row in the sheet
 * - Indexed from 0 (row 0 = first row)
 * 
 * 4. CELL: Individual cell in a row
 * - Indexed from 0 (cell 0 = column A)
 * - Has a type: NUMERIC, STRING, BOOLEAN, FORMULA, etc.
 * 
 * ============================================================================
 * MEMORY CONSIDERATIONS FOR LARGE FILES:
 * ============================================================================
 * 
 * ❌ BAD: Loading entire file into memory
 * XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
 * - For 1M rows: Loads entire file (potentially GBs of RAM!)
 * - OutOfMemoryError with large files
 * 
 * ✅ GOOD: Streaming API (we'll implement this in service)
 * - Reads file row-by-row
 * - Processes in chunks
 * - Constant memory usage regardless of file size
 * 
 * ============================================================================
 */
public class ExcelHelper {

    /**
     * Check if uploaded file is valid Excel format
     * 
     * @param file Uploaded multipart file
     * @return true if .xlsx or .xls
     */
    public static boolean hasExcelFormat(MultipartFile file) {
        /**
         * 🎓 CONTENT TYPE vs FILE EXTENSION:
         * 
         * Content-Type header tells us the MIME type:
         * - .xlsx → application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
         * - .xls → application/vnd.ms-excel
         * 
         * We check both content type AND filename for safety!
         */
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        return (contentType != null
                && (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.ms-excel")))
                || (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")));
    }

    /**
     * 🎓 READING CELL VALUES: Type-Safe Extraction
     * ============================================================================
     * 
     * Excel cells can contain different types of data:
     * - Numbers (including dates!)
     * - Text
     * - Formulas
     * - Booleans
     * - Empty cells
     * 
     * We must handle each type correctly to avoid errors!
     * 
     * ============================================================================
     */

    /**
     * Get string value from cell (handles different cell types)
     */
    public static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        /**
         * 🎓 CELL TYPES:
         * 
         * We use getCellType() to determine what's in the cell,
         * then extract accordingly.
         * 
         * Common mistake: Assuming all cells are strings!
         * If a cell contains 123 (numeric), cell.getStringCellValue() throws exception!
         */
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                /**
                 * 🎓 NUMERIC HANDLING:
                 * 
                 * Excel stores numbers as doubles (including dates!).
                 * We convert to string for fields like NIC numbers.
                 * 
                 * Example: Cell contains 123
                 * - cell.getNumericCellValue() → 123.0 (double)
                 * - Convert to long to remove decimal → 123
                 * - Convert to string → "123"
                 */
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Handle date cells
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // Handle numeric cells (remove decimal if whole number)
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                /**
                 * 🎓 FORMULA CELLS:
                 * 
                 * Excel cell might contain =SUM(A1:A10) instead of value.
                 * We want the CALCULATED value, not the formula text.
                 * 
                 * We evaluate the formula to get its result.
                 */
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }

            case BLANK:
                return null;

            default:
                return null;
        }
    }

    /**
     * Get LocalDate from cell (for date of birth)
     */
    public static LocalDate getDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        /**
         * 🎓 DATE HANDLING IN EXCEL:
         * ============================================================================
         * 
         * Excel stores dates as numbers!
         * - January 1, 1900 = 1
         * - January 2, 1900 = 2
         * - Today = (days since 1900)
         * 
         * POI provides DateUtil to check if a numeric cell is formatted as a date.
         * 
         * ============================================================================
         */
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                // Excel date → Java Date → LocalDate
                Date date = cell.getDateCellValue();
                return date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                // Try parsing string as date (format: yyyy-MM-dd)
                return LocalDate.parse(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            // Invalid date format
            return null;
        }

        return null;
    }

    /**
     * 🎓 CREATING EXCEL FILES: Error Reports
     * ============================================================================
     * 
     * When bulk upload has errors, we generate an Excel file showing which
     * rows failed and why. Users can fix and re-upload.
     * 
     * ============================================================================
     */

    /**
     * Create error report Excel file
     * 
     * @param errors List of error details (row number, field, message)
     * @return Excel file as byte array
     */
    public static byte[] createErrorReportExcel(java.util.List<String> errors) throws IOException {
        /**
         * 🎓 SXSSFWorkbook: Streaming Write
         * 
         * For creating large Excel files, use SXSSFWorkbook:
         * - Keeps only last N rows in memory
         * - Writes others to disk
         * - Prevents OutOfMemoryError
         * 
         * Constructor parameter (100): Keep 100 rows in memory
         */
        try (Workbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Upload Errors");

            // Create header row
            Row headerRow = sheet.createRow(0);

            /**
             * 🎓 CELL STYLES: Making Excel Pretty
             * 
             * CellStyle defines formatting:
             * - Font (bold, size, color)
             * - Background color
             * - Borders
             * - Alignment
             */
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);

            // Header columns
            Cell cell0 = headerRow.createCell(0);
            cell0.setCellValue("Row Number");
            cell0.setCellStyle(headerStyle);

            Cell cell1 = headerRow.createCell(1);
            cell1.setCellValue("Error Message");
            cell1.setCellStyle(headerStyle);

            // Data rows
            int rowNum = 1;
            for (String error : errors) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(error);
            }

            // Auto-size columns for readability
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create sample Excel template for bulk upload
     * 
     * This gives users a template to fill in
     */
    public static byte[] createSampleTemplate() throws IOException {
        try (Workbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customer Template");

            // Create styled header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Name*", "Date of Birth* (yyyy-MM-dd)", "NIC Number*",
                    "Mobile1", "Mobile2", "Mobile3",
                    "Address1_Line1", "Address1_Line2", "Address1_City",
                    "Address2_Line1", "Address2_Line2", "Address2_City"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256); // Set column width
            }

            // Sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("John Doe");
            sampleRow.createCell(1).setCellValue("1990-01-15");
            sampleRow.createCell(2).setCellValue("123456789V");
            sampleRow.createCell(3).setCellValue("+94771234567");
            sampleRow.createCell(4).setCellValue("+94712345678");
            sampleRow.createCell(6).setCellValue("123 Main St");
            sampleRow.createCell(7).setCellValue("Apt 4B");
            sampleRow.createCell(8).setCellValue("Colombo");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
