package com.cms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 🎓 LESSON: Entity Relationships - @ManyToOne
 * ============================================================================
 * 
 * City entity demonstrates a Many-to-One relationship:
 * - Many cities belong to one country
 * - This is the most common type of relationship in databases
 * 
 * In SQL terms, this is a FOREIGN KEY relationship.
 * 
 * ============================================================================
 */
@Entity
@Table(name = "city")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 🎓 CONCEPT: @ManyToOne Relationship
     * ============================================================================
     * 
     * @ManyToOne tells JPA that:
     *            - Multiple City records can reference one Country record
     *            - This field will be a foreign key in the database
     * 
     *            Key attributes:
     * 
     *            fetch = FetchType.EAGER:
     *            - When you load a City, the Country is loaded immediately
     *            - Alternative: FetchType.LAZY loads Country only when accessed
     * 
     *            EAGER: SELECT * FROM city c JOIN country co ON c.country_id =
     *            co.id
     *            LAZY: SELECT * FROM city c (Country loaded later if needed)
     * 
     *            We use EAGER here because Country data is small and always needed.
     *            For large collections, use LAZY to avoid performance issues.
     * 
     *            optional = false:
     *            - Every City MUST have a Country (NOT NULL constraint)
     *            - If true, city.country could be null
     * 
     * @JoinColumn:
     *              - Specifies the foreign key column details
     *              - name = "country_id": Column name in city table
     *              - referencedColumnName = "id": Column in country table being
     *              referenced
     * 
     *              This creates: city.country_id → country.id
     * 
     * @JsonIgnoreProperties:
     *                        - Prevents infinite recursion in JSON serialization
     *                        - Without this, if Country had cities list, it would
     *                        create:
     *                        City → Country → Cities → Countries → Cities → ...
     *                        (infinite loop!)
     *                        - "hibernateLazyInitializer": Ignores Hibernate proxy
     *                        fields
     * 
     *                        ============================================================================
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "country_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Country country;

    /**
     * 🎓 BEST PRACTICE: toString() Exclusion
     * 
     * If we don't exclude 'country' from toString(), we get:
     * city.toString() → country.toString() → ...
     * 
     * Lombok allows us to exclude fields from generated methods:
     * 
     * @ToString(exclude = "country")
     * @EqualsAndHashCode(exclude = "country")
     * 
     *                            But since we're using @Data, we'd need to
     *                            use @ToString.Exclude on the field:
     */
}
