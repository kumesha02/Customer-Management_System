/**
 * 🎓 LESSON: File Upload Component with Progress Tracking
 * ============================================================================
 * 
 * This component demonstrates:
 * - File input handling
 * - File validation
 * - Upload progress tracking
 * - Download functionality
 * - Error reporting
 * - Bootstrap styling
 * 
 * ============================================================================
 */

import React, { useState } from 'react';
import { bulkUploadService } from '../services/api';

function BulkUpload() {
    /**
     * Component state
     */
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [uploadResult, setUploadResult] = useState(null);
    const [error, setError] = useState(null);

    /**
     * 🎓 FILE INPUT HANDLING
     * ========================
     * 
     * File inputs work differently than text inputs:
     * - event.target.files is a FileList (array-like)
     * - files[0] gets the first selected file
     * - File object has: name, size, type, lastModified
     */
    const handleFileSelect = (event) => {
        const file = event.target.files[0];

        // Reset previous state
        setError(null);
        setUploadResult(null);
        setUploadProgress(0);

        if (!file) {
            setSelectedFile(null);
            return;
        }

        /**
         * 🎓 FILE VALIDATION
         * ===================
         * 
         * Always validate files client-side:
         * - File type (MIME type)
         * - File size
         * - File extension
         * 
         * Server-side validation is still required!
         */

        // Check file type
        const validTypes = [
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
            'application/vnd.ms-excel' // .xls
        ];

        if (!validTypes.includes(file.type)) {
            setError('Please select a valid Excel file (.xlsx or .xls)');
            setSelectedFile(null);
            return;
        }

        // Check file size (max 100MB)
        const maxSize = 100 * 1024 * 1024; // 100MB in bytes
        if (file.size > maxSize) {
            setError('File size must be less than 100MB');
            setSelectedFile(null);
            return;
        }

        setSelectedFile(file);
    };

    /**
     * Handle file upload
     */
    const handleUpload = async () => {
        if (!selectedFile) {
            setError('Please select a file first');
            return;
        }

        try {
            setUploading(true);
            setError(null);
            setUploadProgress(0);

            /**
             * 🎓 UPLOAD WITH PROGRESS TRACKING
             * ==================================
             * 
             * We configured onUploadProgress in api.js
             * Here we could track it if needed for UI updates
             */
            const result = await bulkUploadService.uploadCustomers(selectedFile);

            setUploadResult(result);

            // Show success message
            if (result.failureCount === 0) {
                alert(`Success! ${result.successCount} customers uploaded.`);
            } else {
                alert(
                    `Upload completed with some errors:\n` +
                    `Success: ${result.successCount}\n` +
                    `Failed: ${result.failureCount}\n` +
                    `Check the error details below.`
                );
            }

        } catch (err) {
            console.error('Upload error:', err);
            setError(
                err.response?.data?.message ||
                'Upload failed. Please check your file and try again.'
            );
        } finally {
            setUploading(false);
            setUploadProgress(0);
        }
    };

    /**
     * Handle template download
     */
    const handleDownloadTemplate = async () => {
        try {
            /**
             * 🎓 FILE DOWNLOAD IN BROWSER
             * =============================
             * 
             * Steps to download file:
             * 1. Get blob data from API
             * 2. Create object URL from blob
             * 3. Create temporary <a> element
             * 4. Trigger click
             * 5. Clean up object URL
             */
            const blob = await bulkUploadService.downloadTemplate();

            // Create object URL
            const url = window.URL.createObjectURL(blob);

            // Create temporary link
            const link = document.createElement('a');
            link.href = url;
            link.download = 'customer_template.xlsx';

            // Trigger download
            document.body.appendChild(link);
            link.click();

            // Cleanup
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

        } catch (err) {
            console.error('Download error:', err);
            alert('Failed to download template');
        }
    };

    /**
     * Format file size for display
     */
    const formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    /**
     * Format processing time
     */
    const formatTime = (ms) => {
        if (ms < 1000) return `${ms}ms`;
        if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
        return `${(ms / 60000).toFixed(1)}min`;
    };

    return (
        <div className="container">
            <div className="row justify-content-center">
                <div className="col-lg-10">
                    {/* Header */}
                    <div className="mb-4">
                        <h2>
                            <i className="bi bi-file-earmark-excel me-2"></i>
                            Bulk Customer Upload
                        </h2>
                        <p className="text-muted">
                            Upload an Excel file to create multiple customers at once.
                            Maximum 1,000,000 records supported.
                        </p>
                    </div>

                    {/* Instructions Card */}
                    <div className="card mb-4">
                        <div className="card-header bg-info text-white">
                            <h5 className="mb-0">
                                <i className="bi bi-info-circle me-2"></i>
                                Instructions
                            </h5>
                        </div>
                        <div className="card-body">
                            <ol className="mb-0">
                                <li>Download the Excel template using the button below</li>
                                <li>Fill in customer data following the template format</li>
                                <li>Save the file and upload it using the upload section</li>
                                <li>Review the upload results and fix any errors if needed</li>
                            </ol>

                            <div className="mt-3">
                                <button
                                    className="btn btn-info"
                                    onClick={handleDownloadTemplate}
                                >
                                    <i className="bi bi-download me-2"></i>
                                    Download Excel Template
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Upload Card */}
                    <div className="card mb-4">
                        <div className="card-header bg-primary text-white">
                            <h5 className="mb-0">
                                <i className="bi bi-cloud-upload me-2"></i>
                                Upload File
                            </h5>
                        </div>
                        <div className="card-body">
                            {/* File Input */}
                            <div className="mb-3">
                                <label className="form-label">Select Excel File</label>
                                <input
                                    type="file"
                                    className="form-control"
                                    accept=".xlsx,.xls"
                                    onChange={handleFileSelect}
                                    disabled={uploading}
                                />
                                <div className="form-text">
                                    Accepted formats: .xlsx, .xls (Max size: 100MB)
                                </div>
                            </div>

                            {/* Selected File Info */}
                            {selectedFile && (
                                <div className="alert alert-info">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <div>
                                            <i className="bi bi-file-earmark-excel fs-4 me-2"></i>
                                            <strong>{selectedFile.name}</strong>
                                            <br />
                                            <small className="text-muted">
                                                Size: {formatFileSize(selectedFile.size)}
                                            </small>
                                        </div>
                                        <button
                                            className="btn btn-primary"
                                            onClick={handleUpload}
                                            disabled={uploading}
                                        >
                                            {uploading ? (
                                                <>
                                                    <span className="spinner-border spinner-border-sm me-2"></span>
                                                    Uploading...
                                                </>
                                            ) : (
                                                <>
                                                    <i className="bi bi-upload me-2"></i>
                                                    Upload
                                                </>
                                            )}
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* Upload Progress */}
                            {uploading && (
                                <div className="mt-3">
                                    <div className="progress">
                                        <div
                                            className="progress-bar progress-bar-striped progress-bar-animated"
                                            role="progressbar"
                                            style={{ width: `${uploadProgress}%` }}
                                        >
                                            {uploadProgress}%
                                        </div>
                                    </div>
                                    <p className="text-center mt-2 text-muted">
                                        Processing... This may take a while for large files.
                                    </p>
                                </div>
                            )}

                            {/* Error Message */}
                            {error && (
                                <div className="alert alert-danger mt-3">
                                    <i className="bi bi-exclamation-triangle me-2"></i>
                                    {error}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Upload Results */}
                    {uploadResult && (
                        <div className="card">
                            <div className={`card-header ${uploadResult.failureCount === 0 ? 'bg-success' : 'bg-warning'} text-white`}>
                                <h5 className="mb-0">
                                    <i className="bi bi-clipboard-check me-2"></i>
                                    Upload Results
                                </h5>
                            </div>
                            <div className="card-body">
                                {/* Summary Stats */}
                                <div className="row text-center mb-4">
                                    <div className="col-md-3">
                                        <div className="card bg-light">
                                            <div className="card-body">
                                                <h3 className="text-primary">{uploadResult.totalRecords}</h3>
                                                <p className="mb-0 text-muted">Total Records</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-3">
                                        <div className="card bg-light">
                                            <div className="card-body">
                                                <h3 className="text-success">{uploadResult.successCount}</h3>
                                                <p className="mb-0 text-muted">Successful</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-3">
                                        <div className="card bg-light">
                                            <div className="card-body">
                                                <h3 className="text-danger">{uploadResult.failureCount}</h3>
                                                <p className="mb-0 text-muted">Failed</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-3">
                                        <div className="card bg-light">
                                            <div className="card-body">
                                                <h3 className="text-info">
                                                    {formatTime(uploadResult.processingTimeMs)}
                                                </h3>
                                                <p className="mb-0 text-muted">Processing Time</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Error Details */}
                                {uploadResult.errors && uploadResult.errors.length > 0 && (
                                    <div>
                                        <h6 className="text-danger">
                                            <i className="bi bi-exclamation-circle me-2"></i>
                                            Error Details ({uploadResult.errors.length} errors)
                                        </h6>
                                        <div className="table-responsive">
                                            <table className="table table-sm table-bordered">
                                                <thead className="table-light">
                                                    <tr>
                                                        <th>Row</th>
                                                        <th>Field</th>
                                                        <th>Error Message</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {uploadResult.errors.slice(0, 50).map((error, index) => (
                                                        <tr key={index}>
                                                            <td>{error.rowNumber}</td>
                                                            <td>
                                                                <code>{error.field || 'N/A'}</code>
                                                            </td>
                                                            <td>{error.message}</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                            {uploadResult.errors.length > 50 && (
                                                <p className="text-muted text-center">
                                                    Showing first 50 errors. Total: {uploadResult.errors.length}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {/* Success Message */}
                                {uploadResult.failureCount === 0 && (
                                    <div className="alert alert-success">
                                        <i className="bi bi-check-circle me-2"></i>
                                        All records uploaded successfully! No errors found.
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default BulkUpload;

/**
 * 🎓 KEY CONCEPTS DEMONSTRATED:
 * ==============================
 * 
 * 1. FILE HANDLING:
 *    - File input element
 *    - FileList and File objects
 *    - File validation (type, size)
 * 
 * 2. FILE UPLOAD:
 *    - FormData for multipart uploads
 *    - Progress tracking
 *    - Error handling
 * 
 * 3. FILE DOWNLOAD:
 *    - Blob handling
 *    - Object URLs
 *    - Programmatic download
 * 
 * 4. CONDITIONAL RENDERING:
 *    - Show/hide based on state
 *    - Different UI for different states
 * 
 * 5. DATA FORMATTING:
 *    - File size formatting
 *    - Time formatting
 *    - Number formatting
 * 
 * 6. USER FEEDBACK:
 *    - Loading states
 *    - Progress indicators
 *    - Success/error messages
 *    - Result summaries
 * 
 * 7. BOOTSTRAP COMPONENTS:
 *    - Cards for sections
 *    - Progress bars
 *    - Alerts for messages
 *    - Tables for data
 * 
 * ============================================================================
 */
