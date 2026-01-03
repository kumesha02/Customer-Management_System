package com.cms.repository;

import com.cms.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 🎓 Country Repository - Simplest Repository
 * ============================================================================
 * 
 * Just extends JpaRepository - that's all we need!
 * All standard CRUD operations provided automatically.
 * 
 * For master data like Country, we rarely need custom queries.
 * 
 * ============================================================================
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    /**
     * Find country by code (e.g., "LK", "US", "IN")
     * 
     * Useful for bulk operations where Excel might have country codes
     */
    Optional<Country> findByCode(String code);

    /**
     * Find country by name
     * 
     * Case-insensitive search for user convenience
     */
    Optional<Country> findByNameIgnoreCase(String name);
}
