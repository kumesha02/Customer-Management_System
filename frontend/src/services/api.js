/**
 * 🎓 LESSON: Axios Service Layer - API Communication
 * ============================================================================
 * 
 * WHAT IS AXIOS?
 * ---------------
 * Axios is a promise-based HTTP client for JavaScript.
 * It's the most popular way to make API calls in React applications.
 * 
 * WHY USE AXIOS (vs fetch)?
 * ---------------------------
 * 1. AUTOMATIC JSON CONVERSION:
 *    - Axios: response.data (automatically parsed)
 *    - Fetch: await response.json() (manual parsing)
 * 
 * 2. BETTER ERROR HANDLING:
 *    - Axios: Rejects promise on 4xx/5xx status codes
 *    - Fetch: Only rejects on network errors
 * 
 * 3. REQUEST/RESPONSE INTERCEPTORS:
 *    - Add auth tokens globally
 *    - Handle errors in one place
 *    - Transform data automatically
 * 
 * 4. CANCEL REQUESTS:
 *    - Built-in support for canceling requests
 *    - Useful for cleanup in React components
 * 
 * 5. TIMEOUT SUPPORT:
 *    - Easy to configure request timeouts
 * 
 * ============================================================================
 * SERVICE LAYER PATTERN:
 * ============================================================================
 * 
 * Instead of axios.get() directly in components:
 *   ❌ Component is tightly coupled to API details
 *   ❌ Hard to change API endpoints
 *   ❌ Can't reuse API logic
 * 
 * We create a service layer:
 *   ✅ Single source of truth for API calls
 *   ✅ Easy to update endpoints
 *   ✅ Reusable across components
 *   ✅ Easy to mock for testing
 * 
 * ============================================================================
 */

import axios from 'axios';

/**
 * 🎓 AXIOS INSTANCE: Configure Once, Use Everywhere
 * ===================================================
 * 
 * axios.create() creates a custominstance with pre-configured settings.
 * 
 * Benefits:
 * - Base URL applied to all requests
 * - Default headers for all requests
 * - Timeouts configured once
 * - Interceptors added once
 */
const API = axios.create({
    /**
     * Base URL for all API calls
     * 
     * Instead of:
     *   axios.get('http://localhost:8080/api/customers')
     * 
     * We can now do:
     *   API.get('/customers')
     * 
     * In production, change this to your actual domain:
     *   baseURL: 'https://api.example.com/api'
     * 
     * Or better, use environment variables:
     *   baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api'
     */
    baseURL: 'http://localhost:8080/api',

    /**
     * Request timeout (30 seconds)
     * 
     * If request takes longer than 30s, it will be canceled.
     * Important for preventing hung requests.
     */
    timeout: 30000,

    /**
     * Default headers for all requests
     * 
     * Content-Type tells server we're sending JSON.
     * Accept tells server we want JSON back.
     */
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

/**
 * 🎓 REQUEST INTERCEPTOR: Modify Requests Before Sending
 * ========================================================
 * 
 * Interceptors run before every request.
 * 
 * Common use cases:
 * - Add authentication tokens
 * - Log requests (debugging)
 * - Add timestamps
 * - Transform request data
 */
API.interceptors.request.use(
    (config) => {
        /**
         * Example: Add auth token to requests
         * 
         * const token = localStorage.getItem('token');
         * if (token) {
         *     config.headers.Authorization = `Bearer ${token}`;
         * }
         */

        // Log request for debugging (remove in production!)
        console.log(`📤 ${config.method.toUpperCase()} ${config.url}`);

        return config;
    },
    (error) => {
        // Handle request error (e.g., network issues before sending)
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

/**
 * 🎓 RESPONSE INTERCEPTOR: Process Responses Before Components
 * ==============================================================
 * 
 * Interceptors run after every response.
 * 
 * Common use cases:
 * - Extract data automatically
 * - Handle errors globally
 * - Refresh expired tokens
 * - Log responses
 */
API.interceptors.response.use(
    (response) => {
        // Log successful response
        console.log(`✅ ${response.config.method.toUpperCase()} ${response.config.url} - ${response.status}`);

        /**
         * Return response.data directly
         * 
         * Instead of:
         *   const response = await API.get('/customers');
         *   const data = response.data; // Extract data
         * 
         * With interceptor:
         *   const data = await API.get('/customers'); // Data already extracted!
         */
        return response.data;
    },
    (error) => {
        /**
         * 🎓 GLOBAL ERROR HANDLING
         * =========================
         * 
         * Handle different error scenarios in one place!
         */

        if (error.response) {
            /**
             * Server responded with error status (4xx, 5xx)
             * 
             * error.response.status: HTTP status code (404, 500, etc.)
             * error.response.data: Error message from server
             */
            console.error(`❌ ${error.response.status}: ${error.response.data.message || error.message}`);

            // Handle specific status codes
            switch (error.response.status) {
                case 401:
                    // Unauthorized - redirect to login
                    console.error('Unauthorized! Redirecting to login...');
                    // window.location.href = '/login';
                    break;
                case 404:
                    console.error('Resource not found');
                    break;
                case 500:
                    console.error('Server error');
                    break;
                default:
                    console.error('API Error:', error.response.data);
            }
        } else if (error.request) {
            /**
             * Request was sent but no response received
             * 
             * Possible causes:
             * - Server is down
             * - Network connectivity issues
             * - CORS errors
             * - Request timeout
             */
            console.error('No response from server. Is the backend running?');
        } else {
            /**
             * Error setting up the request
             * 
             * Possibly programming error in the request configuration
             */
            console.error('Request setup error:', error.message);
        }

        // Always reject so components can handle errors
        return Promise.reject(error);
    }
);

/**
 * ============================================================================
 * 🎓 CUSTOMER SERVICE: All Customer-Related API Calls
 * ============================================================================
 */
const customerService = {
    /**
     * Get all customers with pagination
     * 
     * @param {number} page - Page number (0-indexed)
     * @param {number} size - Items per page
     * @param {string} sortBy - Field to sort by
     * @param {string} sortDir - Sort direction ('asc' or 'desc')
     * @returns {Promise} Promise resolving to page of customers
     * 
     * Example usage in component:
     *   const data = await customerService.getAllCustomers(0, 20, 'name', 'asc');
     *   setCustomers(data.content);
     *   setTotalPages(data.totalPages);
     */
    getAllCustomers: async (page = 0, size = 20, sortBy = 'id', sortDir = 'asc') => {
        /**
         * 🎓 QUERY PARAMETERS WITH AXIOS
         * ================================
         * 
         * Two ways to pass query params:
         * 
         * 1. Manually in URL:
         *    API.get(`/customers?page=${page}&size=${size}`)
         * 
         * 2. Using params object (cleaner!):
         *    API.get('/customers', { params: {...} })
         * 
         * Axios automatically converts params object to query string.
         */
        return API.get('/customers', {
            params: { page, size, sortBy, sortDir }
        });
    },

    /**
     * Get customer by ID
     * 
     * @param {number} id - Customer ID
     * @returns {Promise} Promise resolving to customer object
     */
    getCustomerById: async (id) => {
        return API.get(`/customers/${id}`);
    },

    /**
     * Create new customer
     * 
     * @param {Object} customerData - Customer data
     * @returns {Promise} Promise resolving to created customer
     * 
     * Example:
     *   const newCustomer = {
     *     name: "John Doe",
     *     dateOfBirth: "1990-01-15",
     *     nicNumber: "123456789V",
     *     mobileNumbers: [{number: "+94771234567"}],
     *     addresses: [{addressLine1: "123 Main St", cityId: 1}]
     *   };
     *   const created = await customerService.createCustomer(newCustomer);
     */
    createCustomer: async (customerData) => {
        /**
         * axios.post(url, data, config)
         * 
         * - url: endpoint
         * - data: request body (automatically JSON.stringify'd)
         * - config: optional configuration
         */
        return API.post('/customers', customerData);
    },

    /**
     * Update existing customer
     * 
     * @param {number} id - Customer ID
     * @param {Object} customerData - Updated customer data
     * @returns {Promise} Promise resolving to updated customer
     */
    updateCustomer: async (id, customerData) => {
        return API.put(`/customers/${id}`, customerData);
    },

    /**
     * Delete customer
     * 
     * @param {number} id - Customer ID
     * @returns {Promise} Promise resolving when deletion complete
     */
    deleteCustomer: async (id) => {
        return API.delete(`/customers/${id}`);
    },

    /**
     * Search customers by name
     * 
     * @param {string} name - Name to search for
     * @returns {Promise} Promise resolving to array of matching customers
     */
    searchCustomers: async (name) => {
        return API.get('/customers/search', {
            params: { name }
        });
    }
};

/**
 * ============================================================================
 * 🎓 MASTER DATA SERVICE: Cities and Countries
 * ============================================================================
 */
const masterDataService = {
    /**
     * Get all countries
     * 
     * Used for populating country dropdown in forms.
     */
    getAllCountries: async () => {
        // Note: You'll need to add this endpoint to backend!
        // For now, we can use a placeholder or fetch from city list
        return API.get('/countries');
    },

    /**
     * Get all cities
     * 
     * Used for populating city dropdown in forms.
     */
    getAllCities: async () => {
        return API.get('/cities');
    },

    /**
     * Get cities by country
     * 
     * @param {number} countryId - Country ID
     * @returns {Promise} Promise resolving to array of cities
     * 
     * For cascading dropdowns: select country → list relevant cities
     */
    getCitiesByCountry: async (countryId) => {
        return API.get(`/cities/by-country/${countryId}`);
    }
};

/**
 * ============================================================================
 * 🎓 BULK UPLOAD SERVICE: Excel Operations
 * ============================================================================
 */
const bulkUploadService = {
    /**
     * Upload Excel file for bulk customer creation
     * 
     * @param {File} file - Excel file from input
     * @returns {Promise} Promise resolving to upload results
     * 
     * Example usage in component:
     *   const handleFileUpload = async (event) => {
     *     const file = event.target.files[0];
     *     const result = await bulkUploadService.uploadCustomers(file);
     *     console.log(`Success: ${result.successCount}, Failed: ${result.failureCount}`);
     *   };
     */
    uploadCustomers: async (file) => {
        /**
         * 🎓 FILE UPLOAD WITH FORMDATA
         * ==============================
         * 
         * For file uploads, we must use FormData (not JSON).
         * 
         * FormData is multipart/form-data encoding:
         * - Can contain files and text data
         * - Browser handles encoding automatically
         */
        const formData = new FormData();
        formData.append('file', file); // 'file' matches @RequestParam("file") in backend

        /**
         * Override Content-Type for this request
         * 
         * We configured 'application/json' globally, but file uploads need
         * 'multipart/form-data'. Axios sets this automatically when it sees
         * FormData, but we make it explicit.
         */
        return API.post('/bulk/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            /**
             * 🎓 UPLOAD PROGRESS TRACKING
             * ============================
             * 
             * For large files, show upload progress to user.
             */
            onUploadProgress: (progressEvent) => {
                const percentCompleted = Math.round(
                    (progressEvent.loaded * 100) / progressEvent.total
                );
                console.log(`Upload progress: ${percentCompleted}%`);

                // You can dispatch this to React state to show progress bar!
            }
        });
    },

    /**
     * Download Excel template
     * 
     * @returns {Promise} Promise resolving to blob data
     * 
     * Example usage:
     *   const handleDownloadTemplate = async () => {
     *     const blob = await bulkUploadService.downloadTemplate();
     *     const url = window.URL.createObjectURL(blob);
     *     const a = document.createElement('a');
     *     a.href = url;
     *     a.download = 'customer_template.xlsx';
     *     a.click();
     *   };
     */
    downloadTemplate: async () => {
        /**
         * 🎓 FILE DOWNLOAD WITH AXIOS
         * ============================
         * 
         * For downloading files, set responseType to 'blob'.
         * Blob = Binary Large Object (file data)
         */
        return API.get('/bulk/template', {
            responseType: 'blob' // Important for file downloads!
        });
    }
};

/**
 * Export services
 * 
 * Components can import and use:
 *   import { customerService } from './services/api';
 *   const customers = await customerService.getAllCustomers();
 */
export {
    customerService,
    masterDataService,
    bulkUploadService
};

/**
 * 🎓 USAGE EXAMPLES IN REACT COMPONENTS:
 * ========================================
 * 
 * 1. FETCHING DATA (useEffect):
 * 
 * import { useEffect, useState } from 'react';
 * import { customerService } from './services/api';
 * 
 * function CustomerList() {
 *   const [customers, setCustomers] = useState([]);
 *   const [loading, setLoading] = useState(true);
 *   const [error, setError] = useState(null);
 * 
 *   useEffect(() => {
 *     const fetchCustomers = async () => {
 *       try {
 *         setLoading(true);
 *         const data = await customerService.getAllCustomers();
 *         setCustomers(data.content);
 *       } catch (err) {
 *         setError(err.message);
 *       } finally {
 *         setLoading(false);
 *       }
 *     };
 * 
 *     fetchCustomers();
 *   }, []); // Empty dependency array = run once on mount
 * 
 *   if (loading) return <div>Loading...</div>;
 *   if (error) return <div>Error: {error}</div>;
 * 
 *   return (
 *     <ul>
 *       {customers.map(customer => (
 *         <li key={customer.id}>{customer.name}</li>
 *       ))}
 *     </ul>
 *   );
 * }
 * 
 * 
 * 2. CREATING DATA (Form submit):
 * 
 * function CustomerForm() {
 *   const handleSubmit = async (event) => {
 *     event.preventDefault();
 *     
 *     const customerData = {
 *       name: event.target.name.value,
 *       dateOfBirth: event.target.dob.value,
 *       nicNumber: event.target.nic.value
 *     };
 * 
 *     try {
 *       const created = await customerService.createCustomer(customerData);
 *       alert(`Customer created with ID: ${created.id}`);
 *     } catch (error) {
 *       alert('Failed to create customer');
 *     }
 *   };
 * 
 *   return <form onSubmit={handleSubmit}>{ form fields }</form>;
 * }
 * 
 * ============================================================================
 */
