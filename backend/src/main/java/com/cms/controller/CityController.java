package com.cms.controller;

import com.cms.entity.City;
import com.cms.repository.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎓 LESSON: Master Data Controller - Cities
 * ============================================================================
 * 
 * This controller provides endpoints for city master data.
 * Used by frontend dropdowns and cascading selects.
 * 
 * Master data characteristics:
 * - Read-only (no create/update/delete in this app)
 * - Cached on frontend
 * - Rarely changes
 * - Small dataset (can load all at once)
 * 
 * ============================================================================
 */
@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "http://localhost:3000")
public class CityController {

    private static final Logger logger = LoggerFactory.getLogger(CityController.class);

    @Autowired
    private CityRepository cityRepository;

    /**
     * Get all cities with country information
     * 
     * @return List of all cities
     */
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        logger.info("Fetching all cities");

        try {
            /**
             * Use findAllWithCountry to eagerly load country data
             * Prevents N+1 query problem when frontend accesses city.country
             */
            List<City> cities = cityRepository.findAllWithCountry();
            return ResponseEntity.ok(cities);

        } catch (Exception e) {
            logger.error("Error fetching cities", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cities by country ID
     * 
     * @param countryId Country ID
     * @return List of cities in that country
     * 
     *         Example: GET /api/cities/by-country/1
     */
    @GetMapping("/by-country/{countryId}")
    public ResponseEntity<List<City>> getCitiesByCountry(@PathVariable Long countryId) {
        logger.info("Fetching cities for country ID: {}", countryId);

        try {
            List<City> cities = cityRepository.findByCountryId(countryId);
            return ResponseEntity.ok(cities);

        } catch (Exception e) {
            logger.error("Error fetching cities by country", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get city by ID
     * 
     * @param id City ID
     * @return City details
     */
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        logger.info("Fetching city with ID: {}", id);

        return cityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
