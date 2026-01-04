import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import CustomerList from './components/CustomerList';
import CustomerForm from './components/CustomerForm';
import BulkUpload from './components/BulkUpload';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

/**
 * 🎓 LESSON: React Router - Navigation in Single Page Applications  
 * ============================================================================
 * 
 * WHAT IS REACT ROUTER?
 * ----------------------
 * React Router enables navigation between different views (pages) in a React app
 * WITHOUT full page reloads. This is called client-side routing.
 * 
 * TRADITIONAL WEB (Server-side routing):
 * - Click link → Browser requests new HTML from server → Full page reload
 * - Slow, losing state, flash of white screen
 * 
 * REACT ROUTER (Client-side routing):
 * - Click link → React Router changes component → No page reload
 * - Fast, maintains state, smooth transitions
 * 
 * ============================================================================
 * CORE COMPONENTS:
 * ============================================================================
 * 
 * <Router>:
 * - Wraps entire app
 * - Manages browser history
 * - Enables routing functionality
 * - BrowserRouter uses HTML5 history API (clean URLs: /customers, not /#/customers)
 * 
 * <Routes>:
 * - Container for all <Route> components
 * - Picks the first matching route
 * 
 * <Route>:
 * - Maps URL path to component
 * - path="/customers" → Show <CustomerList />
 * - path="/customers/new" → Show <CustomerForm />
 * 
 * <Link>:
 * - Navigation without page reload
 * - Replaces <a href> for internal links
 * - to="/customers" → Navigate to /customers route
 * 
 * ============================================================================
 */
function App() {
  return (
    /**
     * 🎓 BrowserRouter: Enable Routing
     * ==================================
     * 
     * Wraps entire app to enable routing.
     * Must be at root level!
     */
    <Router>
      <div className="App">
        {/* ====================================================================
            🎓 NAVIGATION BAR
            ====================================================================
            
            Bootstrap navbar for navigation.
            Links use <Link> from react-router (not <a> tags!) for SPA navigation.
        */}
        <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
          <div className="container-fluid">
            <Link className="navbar-brand" to="/">
              <i className="bi bi-people-fill me-2"></i>
              Customer Management System
            </Link>

            {/* Hamburger button for mobile */}
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarNav"
            >
              <span className="navbar-toggler-icon"></span>
            </button>

            <div className="collapse navbar-collapse" id="navbarNav">
              <ul className="navbar-nav ms-auto">
                <li className="nav-item">
                  <Link className="nav-link" to="/">
                    <i className="bi bi-list-ul me-1"></i>
                    Customer List
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/customers/new">
                    <i className="bi bi-person-plus me-1"></i>
                    Add Customer
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/bulk-upload">
                    <i className="bi bi-file-earmark-excel me-1"></i>
                    Bulk Upload
                  </Link>
                </li>
              </ul>
            </div>
          </div>
        </nav>

        {/* ====================================================================
            🎓 MAIN CONTENT AREA
            ====================================================================
        */}
        <div className="container-fluid mt-4">
          {/**
           * 🎓 ROUTES: Map URLs to Components
           * ===================================
           * 
           * Routes matches URL to component:
           * 
           * - / → CustomerList
           * - /customers/new → CustomerForm (create mode)
           * - /customers/:id/edit → CustomerForm (edit mode, :id is parameter)
           * - /bulk-upload → BulkUpload
           * 
           * :id is a route parameter:
           *   /customers/5/edit → id = 5
           *   Access in component: useParams() → { id: "5" }
           */}
          <Routes>
            {/* Home page - Customer List */}
            <Route path="/" element={<CustomerList />} />

            {/* Create new customer */}
            <Route path="/customers/new" element={<CustomerForm mode="create" />} />

            {/* Edit existing customer */}
            <Route path="/customers/:id/edit" element={<CustomerForm mode="edit" />} />

            {/* Bulk upload page */}
            <Route path="/bulk-upload" element={<BulkUpload />} />
          </Routes>
        </div>

        {/* ====================================================================
            FOOTER
            ====================================================================
        */}
        <footer className="footer mt-5 py-3 bg-light">
          <div className="container text-center">
            <span className="text-muted">
              Customer Management System © 2026 | Built with React & Spring Boot
            </span>
          </div>
        </footer>
      </div>
    </Router>
  );
}

export default App;

/**
 * 🎓 KEY REACT CONCEPTS IN THIS COMPONENT:
 * ==========================================
 * 
 * 1. JSX (JavaScript XML):
 *    - HTML-like syntax in JavaScript
 *    - className (not class) for CSS classes
 *    - {} for JavaScript expressions
 * 
 * 2. Component Composition:
 *    - App component includes other components
 *    - CustomerList, CustomerForm, BulkUpload are child components
 * 
 * 3. Routing:
 *    - URL determines which component renders
 *    - No page reloads when navigating
 * 
 * 4. Bootstrap Integration:
 *    - Import CSS: import 'bootstrap/dist/css/bootstrap.min.css'
 *    - Use Bootstrap classes for styling
 * 
 * 5. Icons (Bootstrap Icons):
 *    - Add to index.html: <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
 *    - Use: <i className="bi bi-people-fill"></i>
 * 
 * ============================================================================
 */
