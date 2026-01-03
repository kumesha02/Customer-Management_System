package com.cms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 🎓 LESSON: Child Entity in @OneToMany Relationship
 * ============================================================================
 * 
 * MobileNumber is the "many" side in OneToMany relationship Customer →
 * MobileNumbers
 * This is the "owning" side that contains the foreign key.
 * 
 * ============================================================================
 */
@Entity
@Table(name = "mobile_number")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer") // Prevent circular reference in toString
public class MobileNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Phone number field with validation
     * 
     * @Pattern: Validates using regular expression
     *           - regexp: Pattern to match
     *           - This pattern accepts formats like:
     *           * +94771234567 (country code)
     *           * 0771234567 (local format)
     *           * 94771234567 (without +)
     * 
     *           Regular Expression Breakdown:
     *           ^ : Start of string
     *           [+]? : Optional + sign
     *           [0-9] : Any digit
     *           {10,15} : Between 10 and 15 digits
     *           $ : End of string
     * 
     *           This is flexible enough for international numbers!
     */
    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid mobile number format")
    @Column(nullable = false, length = 20)
    private String number;

    /**
     * 🎓 CONCEPT: The Owning Side of @OneToMany
     * ============================================================================
     * 
     * @ManyToOne from child's perspective (Many MobileNumbers → One Customer)
     * 
     *            This is the OWNING side because:
     *            1. It has the @JoinColumn (the foreign key)
     *            2. It controls the relationship in the database
     *            3. Changes here directly affect the foreign key column
     * 
     *            fetch = FetchType.LAZY:
     *            - Customer is not loaded when loading mobile number
     *            - Loaded only when you call mobileNumber.getCustomer()
     *            - Generally, use LAZY for @ManyToOne unless you ALWAYS need the
     *            parent
     * 
     * @JoinColumn:
     *              - name = "customer_id": Column name in THIS table
     *              (mobile_number)
     *              - nullable = false: Every mobile number MUST belong to a
     *              customer
     *              - Creates: mobile_number.customer_id → customer.id
     * 
     * @JsonBackReference:
     *                     - Pair with Customer's @JsonManagedReference
     *                     - Prevents infinite loop: Customer → Mobiles → Customer →
     *                     ...
     *                     - This field will be EXCLUDED from JSON serialization
     * 
     *                     JSON serialization:
     *                     {
     *                     "id": 1,
     *                     "number": "+94771234567"
     *                     // customer field NOT included (prevents loop)
     *                     }
     * 
     *                     ============================================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-mobiles")
    private Customer customer;

    /**
     * 🎓 BEST PRACTICE: Exclude Parent from toString()
     * 
     * @ToString(exclude = "customer") at class level prevents:
     *                   mobileNumber.toString() → customer.toString() →
     *                   mobileNumbers.toString() → ... (circular reference!)
     * 
     *                   This would cause StackOverflowError without the exclusion.
     */
}
