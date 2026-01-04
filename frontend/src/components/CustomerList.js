/**
 * 🎓 LESSON: React Component - Customer List with Pagination
 * ============================================================================
 * 
 * This component demonstrates:
 * - useState and useEffect hooks
 * - API integration with async/await
 * - Pagination and sorting
 * - Conditional rendering
 * - Event handling
 * - Bootstrap table styling
 * 
 * ============================================================================
 */

import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { customerService } from '../services/api';

/**
 * 🎓 FUNCTIONAL COMPONENT with Hooks
 * ====================================
 * 
 * Modern React uses functional components with hooks (not class components).
 * Hooks let you use state and lifecycle features in functional components.
 */
function CustomerList() {
    /**
     * 🎓 useState HOOK: Managing Component State
     * ============================================
     * 
     * const [value, setValue] = useState(initialValue);
     * 
     * - value: Current state value
     * - setValue: Function to update state
     * - initialValue: Starting value
     * 
     * When state changes, component re-renders!
     */

    // Customer data from API
    const [customers, setCustomers] = useState([]);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalElements, setTotalElements] = useState(0);

    // Sorting state
    const [sortBy, setSortBy] = useState('id');
    const [sortDir, setSortDir] = useState('asc');

    // Loading and error states
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Search state
    const [searchTerm, setSearchTerm] = useState('');

    /**
     * 🎓 useNavigate HOOK: Programmatic Navigation
     * ==============================================
     * 
     * Navigate to different routes programmatically (not via <Link>).
     * 
     * Example: After deleting customer, redirect to home
     */
    const navigate = useNavigate();

    /**
     * 🎓 useEffect HOOK: Side Effects and Lifecycle
     * ===============================================
     * 
     * useEffect(() => { ... }, [dependencies])
     * 
     * Runs after component renders.
     * 
     * Dependencies array:
     * - [] (empty): Run once on mount (like componentDidMount)
     * - [value]: Run when 'value' changes
     * - No array: Run after every render (usually bad!)
     * 
     * Common uses:
     * - Fetching data from API
     * - Setting up subscriptions
     * - Updating document title
     * - Setting up timers
     */
    useEffect(() => {
        fetchCustomers();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [currentPage, pageSize, sortBy, sortDir]); // Re-fetch when these change

    /**
     * Fetch customers from API
     * 
     * async/await makes asynchronous code look synchronous!
     */
    const fetchCustomers = async () => {
        try {
            setLoading(true);
            setError(null);

            /**
             * 🎓 ASYNC/AWAIT: Modern JavaScript Promises
             * ============================================
             * 
             * Old way (callback hell):
             *   customerService.getAllCustomers()
             *     .then(data => setCustomers(data.content))
             *     .catch(err => setError(err));
             * 
             * New way (clean!):
             *   const data = await customerService.getAllCustomers();
             *   setCustomers(data.content);
             */
            const data = await customerService.getAllCustomers(
                currentPage,
                pageSize,
                sortBy,
                sortDir
            );

            // Update state with API response
            setCustomers(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);

        } catch (err) {
            console.error('Error fetching customers:', err);
            setError('Failed to load customers. Please try again.');
        } finally {
            // Always runs, whether success or error
            setLoading(false);
        }
    };

    /**
     * Handle customer deletion
     */
    const handleDelete = async (id, name) => {
        /**
         * 🎓 window.confirm: Native Browser Dialog
         * ==========================================
         * 
         * Shows confirmation dialog.
         * Returns true if user clicks OK, false if Cancel.
         */
        if (!window.confirm(`Are you sure you want to delete ${name}?`)) {
            return; // User canceled
        }

        try {
            await customerService.deleteCustomer(id);

            // Show success message
            alert('Customer deleted successfully!');

            // Refresh the list
            fetchCustomers();

        } catch (err) {
            console.error('Error deleting customer:', err);
            alert('Failed to delete customer. Please try again.');
        }
    };

    /**
     * Handle search
     */
    const handleSearch = async (e) => {
        e.preventDefault();

        if (!searchTerm.trim()) {
            // Empty search - fetch all
            fetchCustomers();
            return;
        }

        try {
            setLoading(true);
            const results = await customerService.searchCustomers(searchTerm);
            setCustomers(results);
            setTotalPages(1); // Search results not paginated
            setTotalElements(results.length);
        } catch (err) {
            console.error('Error searching:', err);
            setError('Search failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    /**
     * Clear search and reload all customers
     */
    const clearSearch = () => {
        setSearchTerm('');
        fetchCustomers();
    };

    /**
     * Handle sort column change
     */
    const handleSort = (column) => {
        if (sortBy === column) {
            // Same column - toggle direction
            setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
        } else {
            // New column - default to ascending
            setSortBy(column);
            setSortDir('asc');
        }
    };

    /**
     * Get sort icon for column header
     */
    const getSortIcon = (column) => {
        if (sortBy !== column) {
            return <i className="bi bi-arrow-down-up ms-1 text-muted"></i>;
        }
        return sortDir === 'asc'
            ? <i className="bi bi-arrow-up ms-1"></i>
            : <i className="bi bi-arrow-down ms-1"></i>;
    };

    /**
     * 🎓 CONDITIONAL RENDERING: Show Different UI Based on State
     * ============================================================
     * 
     * Three common patterns:
     * 
     * 1. Early return:
     *    if (loading) return <div>Loading...</div>;
     * 
     * 2. Ternary operator:
     *    {loading ? <Spinner /> : <Content />}
     * 
     * 3. Logical AND:
     *    {error && <Alert>{error}</Alert>}
     */

    // Loading state
    if (loading && customers.length === 0) {
        return (
            <div className="text-center mt-5">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
                <p className="mt-3">Loading customers...</p>
            </div>
        );
    }

    // Error state
    if (error && customers.length === 0) {
        return (
            <div className="alert alert-danger m-3" role="alert">
                <i className="bi bi-exclamation-triangle me-2"></i>
                {error}
                <button className="btn btn-sm btn-outline-danger ms-3" onClick={fetchCustomers}>
                    Retry
                </button>
            </div>
        );
    }

    /**
     * 🎓 MAIN RENDER: JSX Return
     * ============================
     * 
     * Component must return JSX (single root element).
     * Use fragments <></> if you don't want extra div.
     */
    return (
        <div className="container-fluid">
            {/* Header Section */}
            <div className="row mb-4">
                <div className="col">
                    <h2>
                        <i className="bi bi-people-fill me-2"></i>
                        Customer Management
                    </h2>
                    <p className="text-muted">
                        Total Customers: {totalElements}
                    </p>
                </div>
                <div className="col-auto">
                    <Link to="/customers/new" className="btn btn-primary">
                        <i className="bi bi-person-plus me-2"></i>
                        Add New Customer
                    </Link>
                </div>
            </div>

            {/* Search Bar */}
            <div className="row mb-3">
                <div className="col-md-6">
                    <form onSubmit={handleSearch} className="input-group">
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search by name..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                        <button className="btn btn-outline-primary" type="submit">
                            <i className="bi bi-search"></i> Search
                        </button>
                        {searchTerm && (
                            <button
                                className="btn btn-outline-secondary"
                                type="button"
                                onClick={clearSearch}
                            >
                                <i className="bi bi-x-circle"></i> Clear
                            </button>
                        )}
                    </form>
                </div>
                <div className="col-md-6 text-end">
                    <select
                        className="form-select d-inline-block w-auto"
                        value={pageSize}
                        onChange={(e) => {
                            setPageSize(Number(e.target.value));
                            setCurrentPage(0); // Reset to first page
                        }}
                    >
                        <option value="5">5 per page</option>
                        <option value="10">10 per page</option>
                        <option value="20">20 per page</option>
                        <option value="50">50 per page</option>
                    </select>
                </div>
            </div>

            {/* Customer Table */}
            <div className="card">
                <div className="card-body p-0">
                    <div className="table-responsive">
                        <table className="table table-hover table-striped mb-0">
                            <thead className="table-primary">
                                <tr>
                                    <th onClick={() => handleSort('id')} style={{ cursor: 'pointer' }}>
                                        ID {getSortIcon('id')}
                                    </th>
                                    <th onClick={() => handleSort('name')} style={{ cursor: 'pointer' }}>
                                        Name {getSortIcon('name')}
                                    </th>
                                    <th onClick={() => handleSort('nicNumber')} style={{ cursor: 'pointer' }}>
                                        NIC Number {getSortIcon('nicNumber')}
                                    </th>
                                    <th onClick={() => handleSort('dateOfBirth')} style={{ cursor: 'pointer' }}>
                                        Date of Birth {getSortIcon('dateOfBirth')}
                                    </th>
                                    <th>Mobile Numbers</th>
                                    <th>Addresses</th>
                                    <th className="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {/**
                 * 🎓 ARRAY MAPPING: Render List of Items
                 * ========================================
                 * 
                 * customers.map(customer => <tr>...</tr>)
                 * 
                 * Transforms array into JSX elements.
                 * Each element needs unique 'key' prop for React's reconciliation.
                 */}
                                {customers.length === 0 ? (
                                    <tr>
                                        <td colSpan="7" className="text-center py-4">
                                            <i className="bi bi-inbox fs-1 text-muted"></i>
                                            <p className="mt-2 text-muted">No customers found</p>
                                        </td>
                                    </tr>
                                ) : (
                                    customers.map((customer) => (
                                        <tr key={customer.id}>
                                            <td>{customer.id}</td>
                                            <td>
                                                <strong>{customer.name}</strong>
                                            </td>
                                            <td>
                                                <code>{customer.nicNumber}</code>
                                            </td>
                                            <td>{customer.dateOfBirth}</td>
                                            <td>
                                                {customer.mobileNumbers && customer.mobileNumbers.length > 0 ? (
                                                    <ul className="list-unstyled mb-0">
                                                        {customer.mobileNumbers.map((mobile, idx) => (
                                                            <li key={idx}>
                                                                <i className="bi bi-telephone me-1"></i>
                                                                {mobile.number}
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <span className="text-muted">-</span>
                                                )}
                                            </td>
                                            <td>
                                                {customer.addresses && customer.addresses.length > 0 ? (
                                                    <ul className="list-unstyled mb-0">
                                                        {customer.addresses.map((addr, idx) => (
                                                            <li key={idx} className="small">
                                                                <i className="bi bi-geo-alt me-1"></i>
                                                                {addr.cityName || 'N/A'}
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <span className="text-muted">-</span>
                                                )}
                                            </td>
                                            <td className="text-center">
                                                <div className="btn-group btn-group-sm" role="group">
                                                    <Link
                                                        to={`/customers/${customer.id}/edit`}
                                                        className="btn btn-outline-primary"
                                                        title="Edit"
                                                    >
                                                        <i className="bi bi-pencil"></i>
                                                    </Link>
                                                    <button
                                                        className="btn btn-outline-danger"
                                                        onClick={() => handleDelete(customer.id, customer.name)}
                                                        title="Delete"
                                                    >
                                                        <i className="bi bi-trash"></i>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <nav className="mt-3" aria-label="Customer pagination">
                    <ul className="pagination justify-content-center">
                        {/* Previous button */}
                        <li className={`page-item ${currentPage === 0 ? 'disabled' : ''}`}>
                            <button
                                className="page-link"
                                onClick={() => setCurrentPage(currentPage - 1)}
                                disabled={currentPage === 0}
                            >
                                <i className="bi bi-chevron-left"></i> Previous
                            </button>
                        </li>

                        {/* Page numbers */}
                        {[...Array(totalPages)].map((_, index) => (
                            <li
                                key={index}
                                className={`page-item ${currentPage === index ? 'active' : ''}`}
                            >
                                <button
                                    className="page-link"
                                    onClick={() => setCurrentPage(index)}
                                >
                                    {index + 1}
                                </button>
                            </li>
                        ))}

                        {/* Next button */}
                        <li className={`page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`}>
                            <button
                                className="page-link"
                                onClick={() => setCurrentPage(currentPage + 1)}
                                disabled={currentPage === totalPages - 1}
                            >
                                Next <i className="bi bi-chevron-right"></i>
                            </button>
                        </li>
                    </ul>
                </nav>
            )}
        </div>
    );
}

export default CustomerList;

/**
 * 🎓 KEY CONCEPTS DEMONSTRATED:
 * ==============================
 * 
 * 1. HOOKS:
 *    - useState: Component state management
 *    - useEffect: Side effects and lifecycle
 *    - useNavigate: Programmatic navigation
 * 
 * 2. ASYNC OPERATIONS:
 *    - async/await for API calls
 *    - try/catch for error handling
 *    - Loading and error states
 * 
 * 3. EVENT HANDLING:
 *    - onClick, onChange, onSubmit
 *    - Preventing default behavior (e.preventDefault())
 *    - Passing parameters to handlers
 * 
 * 4. CONDITIONAL RENDERING:
 *    - Early returns for loading/error
 *    - Ternary operators for inline conditions
 *    - Logical AND for optional rendering
 * 
 * 5. LISTS AND KEYS:
 *    - Array.map() for rendering lists
 *    - Unique key prop for each item
 * 
 * 6. PAGINATION:
 *    - Page state management
 *    - Dynamic page number generation
 *    - Disabled states for buttons
 * 
 * 7. SORTING:
 *    - Sort state (column and direction)
 *    - Toggle sort direction
 *    - Visual indicators (icons)
 * 
 * 8. SEARCH:
 *    - Controlled input (value + onChange)
 *    - Form submission
 *    - Clear functionality
 * 
 * ============================================================================
 */
