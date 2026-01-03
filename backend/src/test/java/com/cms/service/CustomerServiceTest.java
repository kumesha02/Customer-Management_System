package com.cms.service;

import com.cms.dto.CustomerDTO;
import com.cms.entity.City;
import com.cms.entity.Country;
import com.cms.entity.Customer;
import com.cms.repository.CityRepository;
import com.cms.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🎓 LESSON: Unit Testing with JUnit 5 & Mockito
 * ============================================================================
 * 
 * WHAT IS UNIT TESTING?
 * ----------------------
 * Testing individual units of code (methods, classes) in isolation.
 * 
 * WHY TEST?
 * ---------
 * 1. CATCH BUGS EARLY: Find problems before production
 * 2. DOCUMENTATION: Tests show how code should be used
 * 3. REFACTORING SAFETY: Change code with confidence
 * 4. DESIGN FEEDBACK: Hard-to-test code = bad design
 * 5. REGRESSION PREVENTION: Ensure fixes stay fixed
 * 
 * JUNIT 5 (Jupiter):
 * -------------------
 * - Modern testing framework for Java
 * - Annotations: @Test, @BeforeEach, @AfterEach
 * - Assertions: assertEquals, assertThrows, etc.
 * - Lifecycle management
 * 
 * MOCKITO:
 * ---------
 * - Mocking framework
 * - Creates "fake" objects for testing
 * - Controls behavior: when(...).thenReturn(...)
 * - Verifies interactions: verify(mock).someMethod()
 * 
 * ============================================================================
 * ANNOTATIONS EXPLAINED:
 * ============================================================================
 * 
 * @ExtendWith(MockitoExtension.class):
 * - Integrates Mockito with JUnit 5
 * - Enables @Mock and @InjectMocks annotations
 * - Handles mock lifecycle
 * 
 * @Mock:
 *        - Creates a mock (fake) object
 *        - Doesn't execute real code
 *        - You control its behavior in tests
 * 
 * @InjectMocks:
 *               - Creates real instance and injects mocks into it
 *               - Example: Creates CustomerService with mock CustomerRepository
 * 
 * @BeforeEach:
 *              - Runs before each test method
 *              - Used for setup/initialization
 *              - Each test gets fresh objects (isolation!)
 * 
 * @Test:
 *        - Marks method as a test
 *        - JUnit runs these methods
 *        - Test fails if exception thrown or assertion fails
 * 
 *        ============================================================================
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    /**
     * 🎓 MOCKING DEPENDENCIES
     * ========================
     * 
     * CustomerService depends on:
     * - CustomerRepository
     * - CityRepository
     * - ModelMapper
     * 
     * We create MOCKS (fakes) for these.
     * Why? We're testing CustomerService, not the repositories!
     * 
     * Unit testing philosophy: Test ONE thing at a time.
     */

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ModelMapper modelMapper;

    /**
     * 🎓 @InjectMocks: The Class Under Test
     * =======================================
     * 
     * Mockito will:
     * 1. Create a REAL CustomerService instance
     * 2. Inject the @Mock dependencies into it
     * 
     * Result: CustomerService with fake dependencies = isolated test!
     */
    @InjectMocks
    private CustomerService customerService;

    // Test data
    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;
    private City testCity;
    private Country testCountry;

    /**
     * 🎓 SETUP METHOD
     * ================
     * 
     * @BeforeEach runs before EVERY test method.
     * 
     *             Why?
     *             - Each test needs fresh data
     *             - Tests should be independent
     *             - Changes in one test shouldn't affect others
     */
    @BeforeEach
    void setUp() {
        /**
         * Create test data
         * 
         * These are REAL objects used in tests.
         * Only the dependencies (repositories) are mocked.
         */

        // Create test country
        testCountry = new Country();
        testCountry.setId(1L);
        testCountry.setName("Sri Lanka");
        testCountry.setCode("LK");

        // Create test city
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Colombo");
        testCity.setCountry(testCountry);

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testCustomer.setNicNumber("123456789V");
        testCustomer.setMobileNumbers(new ArrayList<>());
        testCustomer.setAddresses(new ArrayList<>());

        // Create test DTO
        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setName("John Doe");
        testCustomerDTO.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testCustomerDTO.setNicNumber("123456789V");
        testCustomerDTO.setMobileNumbers(new ArrayList<>());
        testCustomerDTO.setAddresses(new ArrayList<>());
    }

    /**
     * 🎓 TEST METHOD: Create Customer - Happy Path
     * ==============================================
     * 
     * "Happy path" = everything works as expected.
     * 
     * TEST STRUCTURE (AAA Pattern):
     * 1. ARRANGE: Set up test data and mocks
     * 2. ACT: Execute the method being tested
     * 3. ASSERT: Verify the results
     */
    @Test
    void createCustomer_Success() {
        // ARRANGE
        /**
         * 🎓 STUBBING: Defining Mock Behavior
         * =====================================
         * 
         * when(mock.method()).thenReturn(value)
         * 
         * Tells Mockito: "When this method is called, return this value"
         * 
         * Example:
         * when(customerRepository.existsByNicNumber("123456789V")).thenReturn(false)
         * 
         * Means: Repository will say "NIC doesn't exist" (no duplicate)
         */
        when(customerRepository.existsByNicNumber(testCustomerDTO.getNicNumber()))
                .thenReturn(false); // No duplicate

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(testCustomer);

        // ACT
        /**
         * Call the actual method we're testing
         */
        CustomerDTO result = customerService.createCustomer(testCustomerDTO);

        // ASSERT
        /**
         * 🎓 ASSERTIONS: Verifying Results
         * ==================================
         * 
         * assertNotNull: Value should not be null
         * assertEquals: Values should be equal
         * assertTrue: Condition should be true
         * assertThrows: Method should throw exception
         */
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals(testCustomer.getName(), result.getName());

        /**
         * 🎓 VERIFICATION: Checking Interactions
         * ========================================
         * 
         * verify(mock).method(args)
         * 
         * Checks that the mock method was called with specific arguments.
         * 
         * Example:
         * verify(customerRepository).save(any(Customer.class))
         * 
         * Confirms: save() was called with some Customer object
         * 
         * Why verify?
         * - Ensures method interacts correctly with dependencies
         * - Catches bugs where method doesn't call required operations
         */
        verify(customerRepository).existsByNicNumber(testCustomerDTO.getNicNumber());
        verify(customerRepository).save(any(Customer.class));
    }

    /**
     * 🎓 TEST METHOD: Create Customer - Duplicate NIC
     * =================================================
     * 
     * This tests the ERROR PATH (unhappy path).
     * What happens when NIC already exists?
     */
    @Test
    void createCustomer_DuplicateNIC_ThrowsException() {
        // ARRANGE
        /**
         * Stub repository to say "NIC exists" (duplicate!)
         */
        when(customerRepository.existsByNicNumber(testCustomerDTO.getNicNumber()))
                .thenReturn(true); // Duplicate exists!

        // ACT & ASSERT
        /**
         * 🎓 assertThrows: Testing Exceptions
         * =====================================
         * 
         * assertThrows(ExceptionClass.class, () -> method())
         * 
         * Verifies that the method throws the expected exception.
         * Test PASSES if exception is thrown, FAILS if not.
         * 
         * Lambda syntax: () -> method() creates an executable
         */
        assertThrows(IllegalArgumentException.class, () -> {
            customerService.createCustomer(testCustomerDTO);
        });

        /**
         * Verify that save was NEVER called
         * 
         * verify(mock, never()).method()
         * 
         * Ensures method wasn't called (because exception thrown before save)
         */
        verify(customerRepository, never()).save(any(Customer.class));
    }

    /**
     * 🎓 TEST METHOD: Get Customer By ID - Success
     */
    @Test
    void getCustomerById_Success() {
        // ARRANGE
        Long customerId = 1L;

        /**
         * Repository returns Optional with customer
         */
        when(customerRepository.findByIdWithAllDetails(customerId))
                .thenReturn(Optional.of(testCustomer));

        // ACT
        CustomerDTO result = customerService.getCustomerById(customerId);

        // ASSERT
        assertNotNull(result);
        assertEquals(testCustomer.getName(), result.getName());
        assertEquals(testCustomer.getNicNumber(), result.getNicNumber());

        verify(customerRepository).findByIdWithAllDetails(customerId);
    }

    /**
     * 🎓 TEST METHOD: Get Customer By ID - Not Found
     */
    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // ARRANGE
        Long customerId = 999L;

        /**
         * Repository returns empty Optional (customer not found)
         */
        when(customerRepository.findByIdWithAllDetails(customerId))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(EntityNotFoundException.class, () -> {
            customerService.getCustomerById(customerId);
        });
    }

    /**
     * 🎓 TEST METHOD: Delete Customer - Success
     */
    @Test
    void deleteCustomer_Success() {
        // ARRANGE
        Long customerId = 1L;

        when(customerRepository.existsById(customerId))
                .thenReturn(true);

        /**
         * 🎓 VOID METHODS: doNothing()
         * ==============================
         * 
         * For void methods (methods that don't return anything):
         * doNothing().when(mock).method()
         * 
         * Tells Mockito: "Method does nothing (succeeds silently)"
         * 
         * Actually optional for void methods (default behavior),
         * but explicit is clearer!
         */
        doNothing().when(customerRepository).deleteById(customerId);

        // ACT
        customerService.deleteCustomer(customerId);

        // ASSERT
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    /**
     * 🎓 TESTING BEST PRACTICES
     * ===========================
     * 
     * 1. TEST ONE THING:
     * - Each test should verify one behavior
     * - Clear, focused tests are easier to maintain
     * 
     * 2. INDEPENDENT TESTS:
     * - Tests should not depend on each other
     * - Should pass in any order
     * - @BeforeEach ensures fresh state
     * 
     * 3. NAMING:
     * - Descriptive names: methodName_scenario_expectedResult
     * - Example: createCustomer_DuplicateNIC_ThrowsException
     * - Name explains what's being tested
     * 
     * 4. AAA PATTERN:
     * - Arrange: Set up
     * - Act: Execute
     * - Assert: Verify
     * 
     * 5. TEST BOTH PATHS:
     * - Happy path (success)
     * - Unhappy path (failures, edge cases)
     * 
     * 6. DON'T TEST FRAMEWORK CODE:
     * - Don't test Spring/JPA (they're already tested!)
     * - Test YOUR business logic
     * 
     * 7. MOCK EXTERNAL DEPENDENCIES:
     * - Databases, web services, file systems
     * - Tests should be fast and reliable
     * 
     * 8. COVERAGE:
     * - Aim for 80%+ code coverage
     * - 100% is ideal but not always practical
     * - Focus on critical business logic
     * 
     * ============================================================================
     * RUNNING TESTS
     * ============================================================================
     * 
     * Command line:
     * mvn test # Run all tests
     * mvn test -Dtest=CustomerServiceTest # Run specific test class
     * 
     * IDE:
     * - Right-click test class/method → Run Test
     * - All modern IDEs support JUnit
     * 
     * Coverage report:
     * mvn test jacoco:report
     * # Open: target/site/jacoco/index.html
     * 
     * ============================================================================
     */
}
