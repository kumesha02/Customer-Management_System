package com.cms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 🎓 LESSON: Complex Entity with Multiple Relationships
 * ============================================================================
 * 
 * The Customer entity is the centerpiece of our system. It demonstrates:
 * 1. @OneToMany relationships (customer → addresses, mobile numbers)
 * 2. @ManyToMany relationship (customer → family members)
 * 3. Cascade operations
 * 4. Orphan removal
 * 5. Bidirectional relationships
 * 6. Bean validation
 * 7. Audit fields (createdAt, updatedAt)
 * 
 * ============================================================================
 */
@Entity
@Table(name = "customer", indexes = {
        // Index on NIC for fast lookups (it's unique and frequently queried)
        @Index(name = "idx_nic", columnList = "nic_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 🎓 CONCEPT: Bean Validation
     * ============================================================================
     * 
     * @NotBlank vs @NotNull vs @NotEmpty:
     * 
     * @NotNull:
     *           - Field cannot be null
     *           - "" (empty string) is VALID
     *           - " " (whitespace) is VALID
     * 
     * @NotEmpty:
     *            - Field cannot be null or empty
     *            - "" is INVALID
     *            - " " is VALID (just whitespace)
     * 
     * @NotBlank:
     *            - Field cannot be null, empty, or just whitespace
     *            - Strictest validation for strings
     *            - Perfect for required text fields like names
     * 
     *            These run BEFORE data reaches the database, catching errors early!
     * 
     *            ============================================================================
     */
    @NotBlank(message = "Customer name is mandatory")
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Date of Birth field
     * 
     * @NotNull: Required field (mandatory)
     * 
     * @JsonFormat: Controls JSON serialization/deserialization
     *              - pattern: Date format (yyyy-MM-dd)
     *              - Without this, dates might serialize differently across systems
     * 
     *              LocalDate vs Date:
     *              - Date: Old Java class, mutable, includes time
     *              - LocalDate: Java 8+, immutable, date only (no time)
     *              - LocalDate is better for birthdays!
     */
    @NotNull(message = "Date of birth is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * NIC (National Identity Card) Number
     * 
     * unique = true: Database ensures no two customers have same NIC
     * This creates a UNIQUE constraint
     * 
     * 🎓 Why unique at database level AND application level?
     * - Database: Last line of defense (integrity guaranteed)
     * - Application: Faster validation, better error messages
     */
    @NotBlank(message = "NIC number is mandatory")
    @Column(name = "nic_number", unique = true, nullable = false, length = 20)
    private String nicNumber;

    /**
     * 🎓 CONCEPT: @OneToMany Relationship - Mobile Numbers
     * ============================================================================
     * 
     * One Customer can have Many Mobile Numbers
     * 
     * mappedBy = "customer":
     * - This is the "inverse" side of the relationship
     * - The MobileNumber entity has a "customer" field that owns the relationship
     * - The "owning" side has the foreign key column
     * - The "inverse" side uses mappedBy
     * 
     * cascade = CascadeType.ALL:
     * - Operations on Customer cascade to MobileNumbers
     * - Save customer → saves mobile numbers automatically
     * - Delete customer → deletes mobile numbers automatically
     * 
     * Types:
     * - PERSIST: Propagate save
     * - MERGE: Propagate update
     * - REMOVE: Propagate delete
     * - REFRESH: Reload from database
     * - DETACH: Detach from persistence context
     * - ALL: All of the above!
     * 
     * orphanRemoval = true:
     * - If you remove a mobile number from the list, it's deleted from database
     * - Example: customer.getMobileNumbers().remove(number) → DELETE from DB
     * - Without this, orphaned records would remain in database!
     * 
     * fetch = FetchType.LAZY:
     * - Mobile numbers are NOT loaded automatically with customer
     * - Loaded only when you call customer.getMobileNumbers()
     * - Reduces memory and improves performance
     * - For collections, LAZY is almost always the right choice
     * 
     * @JsonManagedReference:
     *                        - Part of Jackson's solution to circular reference
     *                        problem
     *                        - This is the "forward" part of the relationship
     *                        - MobileNumber will have @JsonBackReference
     *                        - Prevents infinite loop during JSON serialization
     * 
     *                        Why ArrayList not List?
     *                        - We initialize with new ArrayList<>() to avoid
     *                        NullPointerException
     *                        - Safer: can immediately add items without null checks
     * 
     *                        ============================================================================
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("customer-mobiles")
    private List<MobileNumber> mobileNumbers = new ArrayList<>();

    /**
     * 🎓 CONCEPT: @OneToMany Relationship - Addresses
     * 
     * Same pattern as mobile numbers. One customer, multiple addresses.
     * All the same cascade and orphan removal rules apply.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("customer-addresses")
    private List<Address> addresses = new ArrayList<>();

    /**
     * 🎓 CONCEPT: @ManyToMany Relationship - Family Members
     * ============================================================================
     * 
     * Many Customers can be family of Many other Customers
     * This is a SELF-REFERENCING relationship (Customer → Customer)
     * 
     * @JoinTable:
     *             - Creates a junction/join table to store the relationship
     *             - name = "customer_family": Junction table name
     * 
     *             joinColumns:
     *             - The foreign key for THIS entity (current customer)
     *             - name = "customer_id": Column in junction table
     *             - referencedColumnName = "id": Column in Customer table
     * 
     *             inverseJoinColumns:
     *             - The foreign key for the OTHER entity (family member)
     *             - name = "family_member_id": Column in junction table
     * 
     *             Junction table structure:
     *             customer_family (
     *             customer_id → customer.id,
     *             family_member_id → customer.id
     *             )
     * 
     *             Why Set instead of List?
     *             - Set ensures no duplicate family members
     *             - Order doesn't matter for family relationships
     *             - Better performance for contains() operations
     *             - HashSet is perfect for this use case
     * 
     *             fetch = FetchType.LAZY:
     *             - Family members loaded only when accessed
     *             - Prevents loading entire family tree automatically
     * 
     *             🎓 BIDIRECTIONAL CONSIDERATION:
     *             This is unidirectional. If we wanted bidirectional:
     *             - Add another Set<Customer> in same class with mappedBy
     *             - If A adds B as family, B automatically has A as family
     *             - More complex but sometimes useful
     * 
     *             ============================================================================
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "customer_family", joinColumns = @JoinColumn(name = "customer_id"), inverseJoinColumns = @JoinColumn(name = "family_member_id"))
    private Set<Customer> familyMembers = new HashSet<>();

    /**
     * 🎓 CONCEPT: Audit Fields / Temporal Data
     * ============================================================================
     * 
     * @CreationTimestamp:
     *                     - Hibernate automatically sets this when entity is first
     *                     saved
     *                     - You never need to set it manually
     *                     - Useful for tracking when records were created
     * 
     * @UpdateTimestamp:
     *                   - Automatically updated whenever entity is modified
     *                   - Useful for audit trails, caching, conflict detection
     * 
     *                   updatable = false (for createdAt):
     *                   - Once set, this field cannot be changed
     *                   - Prevents accidental modification of creation timestamp
     * 
     *                   LocalDateTime vs Timestamp:
     *                   - LocalDateTime: Java 8+, immutable, no timezone confusion
     *                   - Timestamp: Old SQL type, mutable, timezone issues
     *                   - LocalDateTime is preferred!
     * 
     *                   ============================================================================
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 🎓 CONCEPT: Helper Methods for Bidirectional Relationships
     * ============================================================================
     * 
     * When you have bidirectional relationships, you must keep both sides in sync.
     * These helper methods ensure consistency.
     * 
     * Why are these important?
     * - Without them: customer.getMobileNumbers().add(mobile)
     * → mobile.customer is null! (inconsistent state)
     * - With them: customer.addMobileNumber(mobile)
     * → Both sides are set correctly!
     * 
     * This is a common pattern in JPA applications.
     * 
     * ============================================================================
     */

    /**
     * Add mobile number and set the bidirectional relationship
     */
    public void addMobileNumber(MobileNumber mobileNumber) {
        mobileNumbers.add(mobileNumber);
        mobileNumber.setCustomer(this);
    }

    /**
     * Remove mobile number and break the bidirectional relationship
     */
    public void removeMobileNumber(MobileNumber mobileNumber) {
        mobileNumbers.remove(mobileNumber);
        mobileNumber.setCustomer(null);
    }

    /**
     * Add address and set the bidirectional relationship
     */
    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    /**
     * Remove address and break the bidirectional relationship
     */
    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setCustomer(null);
    }

    /**
     * Add family member (unidirectional, so only one side needs updating)
     */
    public void addFamilyMember(Customer familyMember) {
        familyMembers.add(familyMember);
    }

    /**
     * Remove family member
     */
    public void removeFamilyMember(Customer familyMember) {
        familyMembers.remove(familyMember);
    }
}
