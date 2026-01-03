package com.cms.service;

import com.cms.dto.BulkUploadResponse;
import com.cms.dto.CustomerDTO;
import com.cms.entity.*;
import com.cms.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 🎓 LESSON: Service Layer - The Heart of Business Logic
 * ============================================================================
 * 
 * WHAT IS A SERVICE?
 * -------------------
 * The Service layer sits between Controllers (API) and Repositories (Database).
 * It contains business logic and orchestrates data flow.
 * 
 * ARCHITECTURE (Layered Architecture):
 * -------------------------------------
 * 
 * Controller Layer (Presentation)
 * ↓ REST API calls
 * Service Layer (Business Logic) ← WE ARE HERE!
 * ↓ Database operations
 * Repository Layer (Data Access)
 * ↓ SQL queries
 * Database
 * 
 * WHY SEPARATE LAYERS?
 * ---------------------
 * 1. SEPARATION OF CONCERNS:
 * - Controllers: Handle HTTP, validation, response formatting
 * - Services: Business rules, transactions, orchestration
 * - Repositories: Database access only
 * 
 * 2. REUSABILITY:
 * - Same service can be used by different controllers
 * - Same service can be used by scheduled jobs, async processors, etc.
 * 
 * 3. TESTABILITY:
 * - Test business logic without HTTP layer
 * - Mock repositories easily
 * 
 * 4. TRANSACTION MANAGEMENT:
 * - Service methods define transaction boundaries (more on this below!)
 * 
 * ============================================================================
 * 
 * @Service ANNOTATION:
 *          ============================================================================
 * 
 * @Service is a Spring stereotype annotation that:
 *          - Marks this as a Spring-managed bean
 *          - Enables component scanning (Spring finds and creates instance)
 *          - Enables transaction management
 *          - Makes it available for dependency injection
 * 
 *          It's semantically similar to @Component, but @Service clearly
 *          indicates
 *          this is a service layer component (better for reading and tooling).
 * 
 *          ============================================================================
 *          DEPENDENCY INJECTION WITH @Autowired:
 *          ============================================================================
 * 
 *          Instead of:
 *          private CustomerRepository customerRepository = new
 *          CustomerRepositoryImpl();
 * 
 *          We use:
 * @Autowired
 *            private CustomerRepository customerRepository;
 * 
 *            Spring automatically:
 *            1. Finds a bean of type CustomerRepository
 *            2. Injects it into this field
 *            3. Manages its lifecycle
 * 
 *            Benefits:
 *            - Loose coupling (easy to swap implementations)
 *            - Easy testing (inject mocks)
 *            - Spring manages bean lifecycle
 * 
 *            ============================================================================
 */
@Service
public class CustomerService {

    /**
     * 🎓 DEPENDENCY INJECTION: Field Injection
     * 
     * THREE WAYS TO INJECT DEPENDENCIES:
     * 
     * 1. Field Injection (we use this for simplicity):
     * 
     * @Autowired
     *            private CustomerRepository repository;
     * 
     *            2. Constructor Injection (recommended for production):
     *            private final CustomerRepository repository;
     *            public CustomerService(CustomerRepository repository) {
     *            this.repository = repository;
     *            }
     *            Benefits: Immutable, required dependencies clear, easier testing
     * 
     *            3. Setter Injection (rarely used):
     * @Autowired
     *            public void setRepository(CustomerRepository repository) {...}
     * 
     *            For learning, field injection is cleaner. For production, prefer
     *            constructor.
     */

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    /**
     * 🎓 ModelMapper: Automated DTO ↔ Entity conversion
     * 
     * ModelMapper uses reflection to copy fields between objects.
     * Saves us from writing tedious mapping code!
     * 
     * Without ModelMapper:
     * CustomerDTO dto = new CustomerDTO();
     * dto.setId(entity.getId());
     * dto.setName(entity.getName());
     * dto.setDateOfBirth(entity.getDateOfBirth());
     * ... (20+ lines of boilerplate!)
     * 
     * With ModelMapper:
     * CustomerDTO dto = modelMapper.map(entity, CustomerDTO.class);
     * Done! 🎉
     */
    @Autowired
    private ModelMapper modelMapper;

    /**
     * 🎓 CONCEPT: @Transactional - Database Transactions
     * ============================================================================
     * 
     * What is a transaction?
     * -----------------------
     * A transaction is a unit of work that either:
     * - ALL succeeds (committed to database), OR
     * - ALL fails (rolled back, database unchanged)
     * 
     * ACID Properties:
     * - Atomicity: All or nothing
     * - Consistency: Database rules always valid
     * - Isolation: Transactions don't interfere with each other
     * - Durability: Once committed, data is permanent
     * 
     * @Transactional annotation:
     *                ---------------------------
     *                - Begins transaction when method starts
     *                - Commits when method completes successfully
     *                - Rolls back if exception is thrown
     * 
     *                Example without @Transactional:
     *                1. Save customer → SUCCESS
     *                2. Save mobile number → FAIL (exception)
     *                3. Result: Customer saved, mobile number not saved
     *                (INCONSISTENT!)
     * 
     *                With @Transactional:
     *                1. Save customer
     *                2. Save mobile number → FAIL
     *                3. Rollback: Customer NOT saved (database unchanged,
     *                CONSISTENT!)
     * 
     *                readOnly = true:
     *                - Optimization for read operations
     *                - Hibernate skips dirty checking
     *                - Some databases optimize read-only transactions
     *                - Prevents accidental writes
     * 
     *                ============================================================================
     */

    /**
     * Create a new customer
     * 
     * @Transactional ensures:
     *                - Customer, addresses, and mobile numbers all saved together
     *                - If any save fails, everything rolls back
     */
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        /**
         * 🎓 STEP-BY-STEP: Create Operation
         * =========================================
         */

        // STEP 1: Check for duplicate NIC
        if (customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
            throw new IllegalArgumentException(
                    "Customer with NIC " + customerDTO.getNicNumber() + " already exists");
        }

        // STEP 2: Convert DTO → Entity
        Customer customer = convertToEntity(customerDTO);

        // STEP 3: Save (Hibernate generates INSERT query)
        Customer savedCustomer = customerRepository.save(customer);

        // STEP 4: Convert Entity → DTO and return
        return convertToDTO(savedCustomer);
    }

    /**
     * Update existing customer
     * 
     * @Transactional ensures atomic update of customer and related entities
     */
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        /**
         * 🎓 UPDATE STRATEGY:
         * 
         * 1. Load existing customer (with all relationships)
         * 2. Update fields
         * 3. Handle collections (addresses, mobile numbers)
         * - Remove old ones not in new list (orphanRemoval)
         * - Add new ones
         * 4. Save (Hibernate generates UPDATE queries automatically!)
         */

        // STEP 1: Load existing customer or throw exception
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        // STEP 2: Check NIC uniqueness (if changed)
        if (!existingCustomer.getNicNumber().equals(customerDTO.getNicNumber())) {
            if (customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
                throw new IllegalArgumentException(
                        "Customer with NIC " + customerDTO.getNicNumber() + " already exists");
            }
        }

        // STEP 3: Update simple fields
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setDateOfBirth(customerDTO.getDateOfBirth());
        existingCustomer.setNicNumber(customerDTO.getNicNumber());

        // STEP 4: Update mobile numbers
        updateMobileNumbers(existingCustomer, customerDTO.getMobileNumbers());

        // STEP 5: Update addresses
        updateAddresses(existingCustomer, customerDTO.getAddresses());

        // STEP 6: Update family members
        updateFamilyMembers(existingCustomer, customerDTO.getFamilyMemberIds());

        // STEP 7: Save (Hibernate detects changes and generates UPDATE queries)
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return convertToDTO(updatedCustomer);
    }

    /**
     * Get customer by ID
     * 
     * readOnly = true: Optimization for read operations
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        /**
         * 🎓 FETCHING WITH RELATIONSHIPS:
         * 
         * We use findByIdWithAllDetails to:
         * - Load customer, addresses, mobiles, family in ONE query
         * - Avoid N+1 query problem
         * - Get all data needed for DTO
         */
        Customer customer = customerRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        return convertToDTO(customer);
    }

    /**
     * Get all customers with pagination
     * 
     * @param pageable Contains page number, size, and sort info
     * @return Page of CustomerDTOs
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        /**
         * 🎓 PAGINATION:
         * 
         * Instead of loading 1,000,000 customers:
         * List<Customer> all = customerRepository.findAll(); // 💥 Out of memory!
         * 
         * We use pagination:
         * Pageable pageable = PageRequest.of(0, 20); // Page 0, size 20
         * Page<Customer> page = repository.findAll(pageable);
         * 
         * Spring generates: SELECT * FROM customer LIMIT 20 OFFSET 0
         * 
         * Page object contains:
         * - List of results for current page
         * - Total elements
         * - Total pages
         * - Current page number
         * - Has next/previous page
         * 
         * Perfect for table views with pagination!
         */
        Page<Customer> customerPage = customerRepository.findAll(pageable);

        /**
         * 🎓 STREAM API & map():
         * 
         * customerPage.map() transforms each Customer → CustomerDTO
         * 
         * Instead of:
         * List<CustomerDTO> dtos = new ArrayList<>();
         * for (Customer c : customers) {
         * dtos.add(convertToDTO(c));
         * }
         * 
         * We use:
         * customerPage.map(this::convertToDTO)
         * 
         * this::convertToDTO is a method reference (Java 8 feature)
         * Equivalent to: customer -> convertToDTO(customer)
         */
        return customerPage.map(this::convertToDTO);
    }

    /**
     * Delete customer
     * 
     * @Transactional ensures:
     *                - Customer and related records deleted together
     *                - Rollback if deletion fails
     */
    @Transactional
    public void deleteCustomer(Long id) {
        /**
         * 🎓 CASCADE DELETE:
         * 
         * Because Customer entity has:
         * 
         * @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
         * 
         *                    Deleting customer automatically deletes:
         *                    - All mobile numbers
         *                    - All addresses
         * 
         *                    Hibernate generates:
         *                    DELETE FROM mobile_number WHERE customer_id = ?
         *                    DELETE FROM address WHERE customer_id = ?
         *                    DELETE FROM customer_family WHERE customer_id = ?
         *                    DELETE FROM customer WHERE id = ?
         * 
         *                    All in ONE transaction!
         */
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found with id: " + id);
        }

        customerRepository.deleteById(id);
    }

    /**
     * Search customers by name
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchByName(String name) {
        // This would normally return all matches - should add pagination!
        List<Customer> customers = customerRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .limit(100) // Limit results to prevent huge responses
                .collect(Collectors.toList());

        return customers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * =========================================================================
     * 🎓 HELPER METHODS: The "Boring But Necessary" Code
     * =========================================================================
     */

    /**
     * Convert Entity → DTO
     * 
     * This is complex because of nested objects
     */
    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setNicNumber(customer.getNicNumber());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        // Convert mobile numbers
        if (customer.getMobileNumbers() != null) {
            dto.setMobileNumbers(
                    customer.getMobileNumbers().stream()
                            .map(m -> new CustomerDTO.MobileNumberDTO(m.getId(), m.getNumber()))
                            .collect(Collectors.toList()));
        }

        // Convert addresses
        if (customer.getAddresses() != null) {
            dto.setAddresses(
                    customer.getAddresses().stream()
                            .map(this::convertAddressToDTO)
                            .collect(Collectors.toList()));
        }

        // Convert family members
        if (customer.getFamilyMembers() != null) {
            dto.setFamilyMemberIds(
                    customer.getFamilyMembers().stream()
                            .map(Customer::getId)
                            .collect(Collectors.toSet()));

            dto.setFamilyMembers(
                    customer.getFamilyMembers().stream()
                            .map(f -> new CustomerDTO.FamilyMemberSummaryDTO(
                                    f.getId(), f.getName(), f.getNicNumber()))
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Convert DTO → Entity
     */
    private Customer convertToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        // Convert mobile numbers
        if (dto.getMobileNumbers() != null) {
            dto.getMobileNumbers().forEach(mDto -> {
                MobileNumber mobile = new MobileNumber();
                mobile.setNumber(mDto.getNumber());
                customer.addMobileNumber(mobile); // Use helper method!
            });
        }

        // Convert addresses
        if (dto.getAddresses() != null) {
            dto.getAddresses().forEach(aDto -> {
                Address address = convertAddressToEntity(aDto);
                customer.addAddress(address);
            });
        }

        // Family members (loaded from database)
        if (dto.getFamilyMemberIds() != null && !dto.getFamilyMemberIds().isEmpty()) {
            List<Customer> familyMembers = customerRepository.findAllById(dto.getFamilyMemberIds());
            familyMembers.forEach(customer::addFamilyMember);
        }

        return customer;
    }

    private CustomerDTO.AddressDTO convertAddressToDTO(Address address) {
        CustomerDTO.AddressDTO dto = new CustomerDTO.AddressDTO();
        dto.setId(address.getId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());

        if (address.getCity() != null) {
            dto.setCityId(address.getCity().getId());
            dto.setCityName(address.getCity().getName());

            if (address.getCity().getCountry() != null) {
                dto.setCountryName(address.getCity().getCountry().getName());
            }
        }

        return dto;
    }

    private Address convertAddressToEntity(CustomerDTO.AddressDTO dto) {
        Address address = new Address();
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());

        if (dto.getCityId() != null) {
            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
            address.setCity(city);
        }

        return address;
    }

    /**
     * Update mobile numbers (handle additions/removals)
     */
    private void updateMobileNumbers(Customer customer, List<CustomerDTO.MobileNumberDTO> dtos) {
        // Remove old ones
        customer.getMobileNumbers().clear();

        // Add new ones
        if (dtos != null) {
            dtos.forEach(dto -> {
                MobileNumber mobile = new MobileNumber();
                mobile.setId(dto.getId());
                mobile.setNumber(dto.getNumber());
                customer.addMobileNumber(mobile);
            });
        }
    }

    private void updateAddresses(Customer customer, List<CustomerDTO.AddressDTO> dtos) {
        customer.getAddresses().clear();

        if (dtos != null) {
            dtos.forEach(dto -> {
                Address address = convertAddressToEntity(dto);
                address.setId(dto.getId());
                customer.addAddress(address);
            });
        }
    }

    private void updateFamilyMembers(Customer customer, Set<Long> familyMemberIds) {
        customer.getFamilyMembers().clear();

        if (familyMemberIds != null && !familyMemberIds.isEmpty()) {
            List<Customer> familyMembers = customerRepository.findAllById(familyMemberIds);
            familyMembers.forEach(customer::addFamilyMember);
        }
    }
}
