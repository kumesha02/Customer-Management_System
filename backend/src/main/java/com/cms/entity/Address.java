package com.cms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

/**
 * 🎓 LESSON: Entity with Multiple Relationships
 * ============================================================================
 * 
 * Address entity demonstrates:
 * 1. @ManyToOne to Customer (parent)
 * 2. @ManyToOne to City (reference data)
 * 3. Mixed EAGER and LAZY loading strategies
 * 4. Optional fields (address can be incomplete)
 * 
 * ============================================================================
 */
@Entity
@Table(name = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Address fields - These are optional (customers might provide partial
     * addresses)
     * 
     * Notice: No @NotBlank or @NotNull annotations
     * nullable is not explicitly set, defaults to true
     * 
     * This is a business decision - addresses are optional per requirements
     */
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    /**
     * 🎓 CONCEPT: Relationship to Master Data
     * ============================================================================
     * 
     * City is master/reference data loaded via dropdown in UI
     * 
     * fetch = FetchType.EAGER:
     * - City is loaded immediately with Address
     * - Why EAGER here?
     * * City is small data (just id, name, country)
     * * Almost always needed when displaying addresses
     * * Master data is cached, so performance impact is minimal
     * 
     * optional = true (default):
     * - Address can exist without a city
     * - Allows partial address entry
     * - Makes the system more flexible
     * 
     * @JsonIgnoreProperties:
     *                        - Prevents Hibernate proxy fields from appearing in
     *                        JSON
     *                        - These are internal Hibernate fields used for lazy
     *                        loading
     * 
     *                        ============================================================================
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private City city;

    /**
     * 🎓 CONCEPT: Relationship to Parent Entity
     * ============================================================================
     * 
     * Same pattern as MobileNumber:
     * - fetch = LAZY: Customer loaded only if needed
     * - @JsonBackReference: Prevents circular reference in JSON
     * - @ToString(exclude = "customer"): Prevents circular reference in toString
     * 
     * Why these patterns again?
     * - Consistency across codebase
     * - Proven best practices
     * - Prevents common pitfalls (StackOverflow, infinite JSON loops)
     * 
     * ============================================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-addresses")
    private Customer customer;

    /**
     * 🎓 DESIGN QUESTION: Why not store country directly?
     * 
     * We could have:
     * 
     * @ManyToOne
     *            private Country country;
     * 
     *            But we use City → Country instead because:
     *            1. Normalization: Avoid data redundancy
     *            2. Consistency: City and Country are always in sync
     *            3. Flexibility: If city changes country (rare but possible), only
     *            City table changes
     * 
     *            To get country: address.getCity().getCountry()
     * 
     *            This is called "navigating the object graph"
     */
}
