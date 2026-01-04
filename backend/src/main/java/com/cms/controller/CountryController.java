package com.cms.controller;

import com.cms.entity.Country;
import com.cms.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎓 LESSON: Master Data Controller - Countries
 * ============================================================================
 * 
 * This controller provides endpoints for country master data.
 * 
 * ============================================================================
 */
@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "http://localhost:3000")
public class CountryController {

    private static final Logger logger = LoggerFactory.getLogger(CountryController.class);

    @Autowired
    private CountryRepository countryRepository;

    /**
     * Get all countries
     * 
     * @return List of all countries
     */
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        logger.info("Fetching all countries");

        try {
            List<Country> countries = countryRepository.findAll();
            return ResponseEntity.ok(countries);

        } catch (Exception e) {
            logger.error("Error fetching countries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get country by ID
     * 
     * @param id Country ID
     * @return Country details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
        logger.info("Fetching country with ID: {}", id);

        return countryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get country by code
     * 
     * @param code Country code (e.g., "LK", "US")
     * @return Country details
     * 
     *         Example: GET /api/countries/code/LK
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Country> getCountryByCode(@PathVariable String code) {
        logger.info("Fetching country with code: {}", code);

        return countryRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
