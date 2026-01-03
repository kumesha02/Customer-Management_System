package com.cms.controller;

import com.cms.dto.CustomerDTO;
import com.cms.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;

/**
 * 🎓 LESSON: REST Controllers - Your API Layer
 * ============================================================================
 * 
 * WHAT IS A REST API?
 * -------------------
 * REST (Representational State Transfer) is an architectural style for APIs.
 * 
 * KEY PRINCIPLES:
 * 1. Resource-based: Everything is a resource (Customer, Order, etc.)
 * 2. HTTP Methods: Use standard methods (GET, POST, PUT, DELETE)
 * 3. Stateless: Each request contains all needed information
 * 4. JSON: Data exchanged as JSON
 * 
 * EXAMPLE REST API:
 * -----------------
 * Resource: Customer
 * 
 * GET /api/customers → Get all customers
 * GET /api/customers/1 → Get customer with ID 1
 * POST /api/customers → Create new customer
 * PUT /api/customers/1 → Update customer with ID 1
 * DELETE /api/customers/1 → Delete customer with ID 1
 * 
 * This is called "CRUD" (Create, Read, Update, Delete)
 * 
 * ============================================================================
 * ANNOTATIONS EXPLAINED:
 * ============================================================================
 * 
 * @RestController:
 *                  - Combination of @Controller + @ResponseBody
 *                  - Marks this as a REST API controller
 *                  - Return values automatically converted to JSON
 *                  - Spring scans and registers endpoints
 * 
 *                  @RequestMapping("/api/customers"):
 *                  - Base path for all endpoints in this controller
 *                  - All methods inherit this prefix
 *                  - Example: @GetMapping("/{id}") → /api/customers/{id}
 * 
 * @CrossOrigin:
 *               - Alternative to global CORS config
 *               - Allows requests from specified origins
 *               - We already configured CORS globally, so this is optional
 * 
 *               ============================================================================
 *               HTTP STATUS CODES:
 *               ============================================================================
 * 
 *               Success:
 *               - 200 OK: Request successful (GET, PUT)
 *               - 201 CREATED: Resource created (POST)
 *               - 204 NO CONTENT: Successful, but no data to return (DELETE)
 * 
 *               Client Errors:
 *               - 400 BAD REQUEST: Invalid data sent
 *               - 401 UNAUTHORIZED: Authentication required
 *               - 403 FORBIDDEN: Authenticated but not authorized
 *               - 404 NOT FOUND: Resource doesn't exist
 *               - 409 CONFLICT: Duplicate resource
 * 
 *               Server Errors:
 *               - 500 INTERNAL SERVER ERROR: Something went wrong
 * 
 *               ============================================================================
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000")
@Validated // Enable method parameter validation
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    /**
     * 🎓 CREATE OPERATION - POST
     * ============================================================================
     * 
     * @PostMapping:
     *               - Maps HTTP POST requests to this method
     *               - Full path: POST /api/customers
     * 
     * @RequestBody:
     *               - Binds HTTP request body to method parameter
     *               - Spring automatically converts JSON → CustomerDTO using
     *               Jackson
     * 
     * @Valid:
     *         - Triggers validation annotations (@NotBlank, @NotNull in DTO)
     *         - If validation fails, Spring returns 400 BAD REQUEST automatically
     *         - Error messages from annotations are included in response
     * 
     *         ResponseEntity:
     *         - Wrapper for HTTP response
     *         - Contains status code, headers, and body
     *         - More control than just returning the object
     * 
     *         EXAMPLE REQUEST:
     *         ----------------
     *         POST /api/customers
     *         Content-Type: application/json
     * 
     *         {
     *         "name": "John Doe",
     *         "dateOfBirth": "1990-01-15",
     *         "nicNumber": "123456789V",
     *         "mobileNumbers": [
     *         {"number": "+94771234567"}
     *         ],
     *         "addresses": [
     *         {
     *         "addressLine1": "123 Main St",
     *         "cityId": 1
     *         }
     *         ]
     *         }
     * 
     *         EXAMPLE RESPONSE:
     *         -----------------
     *         HTTP 201 CREATED
     * 
     *         {
     *         "id": 1,
     *         "name": "John Doe",
     *         "dateOfBirth": "1990-01-15",
     *         "nicNumber": "123456789V",
     *         "mobileNumbers": [...],
     *         "addresses": [...],
     *         "createdAt": "2025-01-03T09:30:00",
     *         "updatedAt": "2025-01-03T09:30:00"
     *         }
     * 
     *         ============================================================================
     */
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        logger.info("Creating new customer: {}", customerDTO.getName());

        try {
            CustomerDTO created = customerService.createCustomer(customerDTO);

            /**
             * 🎓 ResponseEntity.status().body():
             * 
             * ResponseEntity.status(HttpStatus.CREATED)
             * → Sets status code to 201
             * .body(created)
             * → Sets response body
             * 
             * Alternative shorthand:
             * return ResponseEntity.ok(created); // 200 OK
             * return new ResponseEntity<>(created, HttpStatus.CREATED); // 201 CREATED
             */
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            // Duplicate NIC or validation error
            logger.warn("Failed to create customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 READ OPERATION (Single) - GET /{id}
     * ============================================================================
     * 
     * @GetMapping("/{id}"):
     * - Maps GET requests to /api/customers/{id}
     * - {id} is a path variable
     * 
     * @PathVariable:
     *                - Extracts value from URL path
     *                - Example: GET /api/customers/5 → id = 5
     * 
     *                ============================================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);

        try {
            CustomerDTO customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);

        } catch (EntityNotFoundException e) {
            logger.warn("Customer not found: {}", id);
            return ResponseEntity.notFound().build(); // 404 NOT FOUND
        } catch (Exception e) {
            logger.error("Error fetching customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 READ OPERATION (List) - GET with Pagination
     * ============================================================================
     * 
     * @RequestParam:
     *                - Extracts query parameters from URL
     *                - Example: GET /api/customers?page=0&size=20&sort=name
     * 
     *                defaultValue:
     *                - Used if parameter not provided
     *                - Makes parameters optional
     * 
     *                EXAMPLE REQUEST:
     *                ----------------
     *                GET /api/customers?page=0&size=10&sort=name,asc
     * 
     *                QUERY PARAMETERS:
     *                - page: Page number (0-indexed)
     *                - size: Items per page
     *                - sort: Sort field and direction
     * 
     *                EXAMPLE RESPONSE:
     *                -----------------
     *                {
     *                "content": [...], // Array of customers
     *                "totalElements": 100, // Total customers in database
     *                "totalPages": 10, // Total pages
     *                "size": 10, // Page size
     *                "number": 0, // Current page number
     *                "first": true, // Is first page?
     *                "last": false, // Is last page?
     *                "numberOfElements": 10 // Items in current page
     *                }
     * 
     *                ============================================================================
     */
    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        logger.info("Fetching customers - page: {}, size: {}, sort: {} {}",
                page, size, sortBy, sortDir);

        try {
            /**
             * 🎓 BUILDING PAGEABLE:
             * 
             * Sort.Direction.fromString(sortDir):
             * - Converts "asc" → Sort.Direction.ASC
             * - Converts "desc" → Sort.Direction.DESC
             * 
             * Sort.by(direction, sortBy):
             * - Creates sort specification
             * - Can sort by multiple fields: Sort.by("name").and(Sort.by("id"))
             * 
             * PageRequest.of(page, size, sort):
             * - Creates Pageable object
             * - Spring Data JPA uses this for pagination
             */
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
            return ResponseEntity.ok(customers);

        } catch (Exception e) {
            logger.error("Error fetching customers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 UPDATE OPERATION - PUT
     * ============================================================================
     * 
     * PUT vs PATCH:
     * - PUT: Replace entire resource (all fields)
     * - PATCH: Update specific fields only
     * 
     * We use PUT (full replacement).
     * 
     * ============================================================================
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        logger.info("Updating customer with ID: {}", id);

        try {
            CustomerDTO updated = customerService.updateCustomer(id, customerDTO);
            return ResponseEntity.ok(updated);

        } catch (EntityNotFoundException e) {
            logger.warn("Customer not found for update: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error updating customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 DELETE OPERATION - DELETE
     * ============================================================================
     * 
     * DELETE typically returns:
     * - 204 NO CONTENT: Successful, no body
     * - 404 NOT FOUND: Resource doesn't exist
     * 
     * ============================================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);

        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build(); // 204 NO CONTENT

        } catch (EntityNotFoundException e) {
            logger.warn("Customer not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 SEARCH ENDPOINT - Custom Operation
     * ============================================================================
     * 
     * Not part of standard CRUD, but common in real applications.
     * 
     * EXAMPLE: GET /api/customers/search?name=john
     * 
     * ============================================================================
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(
            @RequestParam String name) {
        logger.info("Searching customers by name: {}", name);

        try {
            List<CustomerDTO> customers = customerService.searchByName(name);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            logger.error("Error searching customers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎓 BEST PRACTICES:
     * ============================================================================
     * 
     * 1. LOGGING:
     * - Log important operations
     * - Include relevant data (IDs, names)
     * - Different levels: INFO, WARN, ERROR
     * 
     * 2. ERROR HANDLING:
     * - Catch specific exceptions
     * - Return appropriate HTTP status codes
     * - Don't expose internal errors to clients
     * 
     * 3. VALIDATION:
     * - Use @Valid for automatic validation
     * - Return 400 for validation errors
     * - Include error messages
     * 
     * 4. DOCUMENTATION:
     * - Consider adding Swagger/OpenAPI
     * - Document expected request/response formats
     * - Include example requests
     * 
     * 5. VERSIONING:
     * - Consider API versioning: /api/v1/customers
     * - Allows changes without breaking existing clients
     * 
     * ============================================================================
     */
}
