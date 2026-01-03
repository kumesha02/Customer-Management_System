package com.cms.repository;

import com.cms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 🎓 LESSON: Spring Data JPA Repositories
 * ============================================================================
 * 
 * This is where Spring Data JPA shows its magic! 🪄
 * 
 * CONCEPT: What is a Repository?
 * ----------------------------------
 * A Repository is the Data Access Layer - it handles all database operations.
 * Think of it as a collection of database queries in Java interface form.
 * 
 * TRADITIONAL APPROACH (The Hard Way):
 * --------------------------------------
 * Without Spring Data JPA, you'd write:
 * 
 * public class CustomerDAO {
 * public Customer save(Customer customer) {
 * // 20 lines of JDBC code...
 * Connection conn = dataSource.getConnection();
 * PreparedStatement ps = conn.prepareStatement(
 * "INSERT INTO customer (name, dob, nic) VALUES (?, ?, ?)");
 * ps.setString(1, customer.getName());
 * ps.setDate(2, Date.valueOf(customer.getDateOfBirth()));
 * ps.setString(3, customer.getNicNumber());
 * // ... more boilerplate code
 * }
 * 
 * public List<Customer> findAll() {
 * // 30 lines of ResultSet mapping...
 * }
 * 
 * // And so on for every operation...
 * }
 * 
 * SPRING DATA JPA APPROACH (The Easy Way):
 * -------------------------------------------
 * Just extend JpaRepository - that's it! Spring provides implementations
 * automatically.
 * 
 * ============================================================================
 * 🎓 CONCEPT: JpaRepository Interface
 * ============================================================================
 * 
 * JpaRepository<Customer, Long> means:
 * - Customer: The entity this repository manages
 * - Long: The type of the entity's primary key
 * 
 * By extending JpaRepository, you automatically get these methods FOR FREE:
 * 
 * === CRUD Operations ===
 * - save(Customer): Insert or update
 * - saveAll(List<Customer>): Batch insert/update
 * - findById(Long): Get by primary key
 * - existsById(Long): Check if exists
 * - findAll(): Get all records
 * - findAllById(List<Long>): Get multiple by IDs
 * - count(): Count all records
 * - delete(Customer): Delete entity
 * - deleteById(Long): Delete by ID
 * - deleteAll(): Delete everything (dangerous!)
 * 
 * === Pagination & Sorting ===
 * - findAll(Pageable): Get paginated results
 * - findAll(Sort): Get sorted results
 * 
 * === Batch Operations ===
 * - flush(): Force synchronization with database
 * - saveAndFlush(Customer): Save and immediately flush
 * 
 * You DON'T write SQL for any of these! Spring generates it automatically! 🎉
 * 
 * ============================================================================
 * 🎓 CONCEPT: Query Methods (Method Name Convention)
 * ============================================================================
 * 
 * Spring Data JPA can generate queries from method names!
 * 
 * Pattern: findBy + FieldName + Operator
 * 
 * Examples:
 * - findByName(String name) → WHERE name = ?
 * - findByNameAndNicNumber(String name, String nic) → WHERE name = ? AND
 * nic_number = ?
 * - findByNameContaining(String name) → WHERE name LIKE %?%
 * - findByDateOfBirthBefore(LocalDate date) → WHERE date_of_birth < ?
 * - findByAgeGreaterThan(int age) → WHERE age > ?
 * 
 * Full list of supported keywords:
 * - findBy, getBy, readBy, queryBy, searchBy, streamBy
 * - And, Or, Between, LessThan, GreaterThan, After, Before
 * - IsNull, IsNotNull, Like, NotLike, StartingWith, EndingWith, Containing
 * - OrderBy, Top, First, Distinct
 * 
 * MORE EXAMPLES:
 * - findTop10ByOrderByCreatedAtDesc() → Top 10, newest first
 * - findDistinctByName(String name) → Distinct results
 * - countByNicNumber(String nic) → Count matching records
 * 
 * ============================================================================
 * 
 * @Repository:
 * 
 *              - Marks this as a Spring-managed repository (Spring creates and
 *              manages it)
 *              - Enables exception translation (SQLException →
 *              DataAccessException)
 *              - Makes it available for @Autowired injection
 * 
 *              Technically optional with Spring Data JPA, but it's good
 *              practice!
 * 
 *              ============================================================================
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 🎓 EXAMPLE: Query Method by Method Name
     * --------------------------------------------------------
     * 
     * Method name: findByNicNumber
     * Spring interprets this as:
     * SELECT c FROM Customer c WHERE c.nicNumber = :nicNumber
     * 
     * Returns: Optional<Customer>
     * - Optional is Java 8+ wrapper for potentially null values
     * - Instead of returning null (which causes NullPointerException),
     * Optional forces you to handle the "not found" case explicitly
     * 
     * Usage:
     * Optional<Customer> customer = repository.findByNicNumber("123456789");
     * if (customer.isPresent()) {
     * Customer c = customer.get();
     * // use customer
     * } else {
     * // handle not found
     * }
     * 
     * Or more elegantly:
     * customer.ifPresent(c -> System.out.println(c.getName()));
     * 
     * Or:
     * Customer c = customer.orElseThrow(
     * () -> new CustomerNotFoundException("Customer not found")
     * );
     */
    Optional<Customer> findByNicNumber(String nicNumber);

    /**
     * 🎓 EXAMPLE: Custom JPQL Query with @Query
     * ============================================================================
     * 
     * Sometimes method names get too long or complex. Use @Query for clarity!
     * 
     * JPQL (Java Persistence Query Language):
     * - Object-oriented query language (uses entity names, not table names)
     * - 'Customer c': c is an alias for Customer entity
     * - 'LEFT JOIN FETCH': Fetches related entities in same query
     * - This prevents N+1 query problem!
     * 
     * ❌ N+1 QUERY PROBLEM:
     * Without JOIN FETCH, Hibernate does:
     * 1 query to get customers
     * + N queries to get mobile numbers for each customer
     * + N queries to get addresses for each customer
     * Total: 1 + 2N queries! 😱
     * 
     * ✅ WITH JOIN FETCH:
     * 1 query gets everything! 🎉
     * 
     * @Param:
     *         - Binds method parameter to query parameter
     *         - :id in query maps to @Param("id") parameter
     * 
     *         ============================================================================
     */
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN FETCH c.mobileNumbers " +
            "LEFT JOIN FETCH c.addresses " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    /**
     * 🎓 EXAMPLE: Custom Query with Multiple Joins
     * 
     * This query loads EVERYTHING in one shot:
     * - Customer
     * - Mobile numbers
     * - Addresses
     * - Family members
     * 
     * Perfect for detailed customer view where all data is needed!
     * 
     * Performance: 1 query vs potentially 100+ queries without JOIN FETCH
     */
    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.mobileNumbers " +
            "LEFT JOIN FETCH c.addresses a " +
            "LEFT JOIN FETCH a.city city " +
            "LEFT JOIN FETCH city.country " +
            "LEFT JOIN FETCH c.familyMembers " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithAllDetails(@Param("id") Long id);

    /**
     * 🎓 EXAMPLE: Query Method with Partial Match
     * 
     * findByNameContainingIgnoreCase generates:
     * SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(:name)
     * 
     * Usage:
     * repository.findByNameContainingIgnoreCase("john")
     * 
     * Matches: "John", "JOHNSON", "johnny", "John Doe", etc.
     * 
     * Great for search functionality!
     */
    Optional<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * 🎓 EXAMPLE: Existence Check
     * 
     * existsByNicNumber generates:
     * SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
     * FROM Customer c WHERE c.nicNumber = :nicNumber
     * 
     * Returns boolean, not the entity.
     * More efficient than findByNicNumber when you only need to check existence!
     * 
     * Usage:
     * if (repository.existsByNicNumber(nic)) {
     * throw new DuplicateNicException();
     * }
     */
    boolean existsByNicNumber(String nicNumber);

    /**
     * 🎓 WHAT ABOUT COMPLEX QUERIES?
     * ============================================================================
     * 
     * For very complex queries, you have options:
     * 
     * 1. @Query with JPQL (shown above)
     * 2. @Query with native SQL:
     * 
     * @Query(value = "SELECT * FROM customer WHERE ...", nativeQuery = true)
     * 
     *              3. Specifications (for dynamic queries):
     *              Extends JpaSpecificationExecutor<Customer>
     *              Allows building queries programmatically
     * 
     *              4. QueryDSL (type-safe queries):
     *              External library, very powerful for complex dynamic queries
     * 
     *              5. Custom repository implementation:
     *              For ultimate control, implement custom methods
     * 
     *              For 90% of cases, method names and @Query are sufficient!
     *              ============================================================================
     */
}
