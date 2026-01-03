package com.cms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 🎓 LESSON: Data Transfer Objects (DTOs)
 * ============================================================================
 * 
 * WHAT IS A DTO?
 * ---------------
 * A DTO is a simple object that carries data between processes or layers.
 * It's specifically designed for data transfer, not business logic.
 * 
 * WHY USE DTOs? (Critical to understand!)
 * ----------------------------------------
 * 
 * ❌ BAD APPROACH: Returning entities directly from REST APIs
 * 
 * @GetMapping("/customers/{id}")
 * public Customer getCustomer(@PathVariable Long id) {
 * return customerRepository.findById(id).orElseThrow();
 * }
 * 
 * Problems with this:
 * 
 * 1. LAZY LOADING ISSUES:
 * - Entity has lazy-loaded collections (addresses, mobile numbers)
 * - Outside transaction, accessing these causes LazyInitializationException
 * - Jackson tries to serialize → CRASH! 💥
 * 
 * 2. CIRCULAR REFERENCES:
 * - Customer → Address → Customer → Address... (infinite loop!)
 * - JSON serialization fails or creates huge responses
 * 
 * 3. SECURITY:
 * - Exposes internal database structure
 * - Might expose sensitive fields you don't want in API
 * - Changes to entities break API contracts
 * 
 * 4. PERFORMANCE:
 * - Might serialize more data than needed
 * - No control over what's included/excluded
 * 
 * 5. API COUPLING:
 * - API structure tied to database structure
 * - Can't change one without affecting the other
 * 
 * ✅ GOOD APPROACH: Use DTOs
 * 
 * @GetMapping("/customers/{id}")
 * public CustomerDTO getCustomer(@PathVariable Long id) {
 * Customer customer = customerRepository.findById(id).orElseThrow();
 * return convertToDTO(customer); // Clean, controlled data transfer
 * }
 * 
 * Benefits:
 * 
 * 1. CONTROL: Decide exactly what data to expose
 * 2. SEPARATION: API independent from database structure
 * 3. VERSIONING: Different DTOs for different API versions
 * 4. PERFORMANCE: Only serialize what's needed
 * 5. VALIDATION: API-specific validation rules
 * 6. DOCUMENTATION: Clear API contracts
 * 
 * ============================================================================
 * DTO vs ENTITY: What's the difference?
 * ============================================================================
 * 
 * ENTITY:
 * - Represents database table
 * - Has @Entity, @Table annotations
 * - Managed by JPA/Hibernate
 * - Contains relationships (@OneToMany, etc.)
 * - Has database-specific fields (createdAt, etc.)
 * - Lives in persistence context
 * 
 * DTO:
 * - Plain old Java object (POJO)
 * - No JPA annotations
 * - Not managed by Hibernate
 * - Flat structure (or minimal nesting)
 * - Only fields needed for API
 * - Serializes cleanly to JSON
 * 
 * ============================================================================
 * DESIGN PATTERNS:
 * ============================================================================
 * 
 * 1. FLAT DTO: All data in one level
 * { id, name, dob, mobile1, mobile2, address1, address2 }
 * - Simple, but inflexible
 * 
 * 2. NESTED DTO: Structured data (we use this!)
 * { id, name, dob, mobileNumbers: [...], addresses: [...] }
 * - Clean, flexible, matches UI needs
 * 
 * 3. PROJECTION: Only specific fields
 * { id, name }
 * - Ultra-lightweight for lists
 * 
 * We'll use NESTED DTOs - best balance of flexibility and clarity!
 * 
 * ============================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    /**
     * ID field - null for new customers, set for existing
     */
    private Long id;

    /**
     * Same validation as entity
     * Validation happens at DTO level for API requests
     */
    @NotBlank(message = "Customer name is mandatory")
    private String name;

    @NotNull(message = "Date of birth is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "NIC number is mandatory")
    private String nicNumber;

    /**
     * 🎓 NESTED DTOs: Instead of List<MobileNumber> entity objects
     * 
     * We use List<MobileNumberDTO> - simple objects with just the data we need
     * This breaks the JPA dependency and prevents lazy loading issues
     */
    private List<MobileNumberDTO> mobileNumbers = new ArrayList<>();

    /**
     * Nested address DTOs
     */
    private List<AddressDTO> addresses = new ArrayList<>();

    /**
     * 🎓 FAMILY MEMBERS: Simplified representation
     * 
     * Instead of full Customer objects, we just include IDs
     * This prevents:
     * - Infinite nesting (family → family → family...)
     * - Huge JSON responses
     * - Circular reference issues
     * 
     * Frontend can fetch full details if needed via separate API call
     */
    private Set<Long> familyMemberIds = new HashSet<>();

    /**
     * 🎓 OPTIONAL: Include family member names for UI convenience
     * 
     * This is hybrid approach:
     * - IDs for updating relationships
     * - Names for display without additional API calls
     * 
     * Trade-off: Slightly larger response, but better UX
     */
    private List<FamilyMemberSummaryDTO> familyMembers = new ArrayList<>();

    /**
     * Audit fields - read-only in API
     * These are set by server, never by client
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 🎓 NESTED DTO CLASSES
     * ============================================================================
     * 
     * We can define simple DTOs as static inner classes when they're only
     * used within this context. Keeps related classes together.
     * 
     * Alternative: Separate files (MobileNumberDTO.java, AddressDTO.java)
     * 
     * Rule of thumb:
     * - Inner class: DTO used only with this parent
     * - Separate file: DTO used in multiple places
     * 
     * ============================================================================
     */

    /**
     * Simple DTO for mobile numbers
     * Just ID and number - that's all we need!
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobileNumberDTO {
        private Long id;

        @NotBlank(message = "Mobile number cannot be empty")
        private String number;
    }

    /**
     * DTO for addresses with city/country details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private Long id;
        private String addressLine1;
        private String addressLine2;

        /**
         * Just city ID for updates
         * Frontend will have city dropdown with IDs
         */
        private Long cityId;

        /**
         * City/country details for display
         * Populated when reading, ignored when writing
         */
        private String cityName;
        private String countryName;
    }

    /**
     * Minimal DTO for family member display
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberSummaryDTO {
        private Long id;
        private String name;
        private String nicNumber;
    }
}
