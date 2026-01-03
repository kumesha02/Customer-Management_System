package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎓 Bulk Upload Response DTO
 * ============================================================================
 * 
 * This DTO encapsulates the results of a bulk upload operation.
 * 
 * WHY A SPECIAL DTO FOR BULK OPERATIONS?
 * ----------------------------------------
 * Bulk operations need to report:
 * - Overall success/failure
 * - How many succeeded
 * - How many failed
 * - Detailed errors for each failure
 * - Processing time
 * 
 * This can't be represented by standard success/error responses.
 * 
 * ============================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {

    /**
     * Total records in the uploaded file
     */
    private int totalRecords;

    /**
     * Number of successfully created/updated customers
     */
    private int successCount;

    /**
     * Number of failed records
     */
    private int failureCount;

    /**
     * List of errors with row numbers and messages
     * 
     * Example:
     * [
     * { row: 5, field: "nicNumber", error: "Duplicate NIC" },
     * { row: 12, field: "dateOfBirth", error: "Invalid date format" }
     * ]
     */
    private List<ErrorDetail> errors = new ArrayList<>();

    /**
     * Processing time in milliseconds
     * Useful for performance monitoring
     */
    private long processingTimeMs;

    /**
     * 🎓 Nested class for error details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        /**
         * Row number in Excel file (1-indexed)
         */
        private int rowNumber;

        /**
         * Field that caused the error (can be null for row-level errors)
         */
        private String field;

        /**
         * Human-readable error message
         */
        private String message;
    }

    /**
     * 🎓 HELPER METHOD: Add error
     * 
     * Convenience method for service layer
     */
    public void addError(int rowNumber, String field, String message) {
        this.errors.add(new ErrorDetail(rowNumber, field, message));
        this.failureCount++;
    }

    /**
     * Helper method: Increment success count
     */
    public void incrementSuccess() {
        this.successCount++;
    }
}
