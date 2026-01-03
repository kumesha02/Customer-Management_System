package com.cms.repository;

import com.cms.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 🎓 City Repository - Simple Master Data Repository
 * ============================================================================
 * 
 * This is a minimal repository - perfect for master data tables!
 * We only need basic operations plus country filtering.
 * 
 * ============================================================================
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Find all cities in a specific country
     * 
     * Method name pattern: findBy + relationship + field
     * country.id → navigates the relationship
     * 
     * Generated query:
     * SELECT c FROM City c WHERE c.country.id = :countryId
     * 
     * Or in SQL:
     * SELECT * FROM city WHERE country_id = ?
     */
    List<City> findByCountryId(Long countryId);

    /**
     * 🎓 OPTIMIZATION: Load cities with countries in one query
     * 
     * For displaying city dropdowns with country context,
     * this avoids N+1 queries.
     * 
     * JOIN FETCH loads related Country immediately
     */
    @Query("SELECT c FROM City c JOIN FETCH c.country")
    List<City> findAllWithCountry();
}
