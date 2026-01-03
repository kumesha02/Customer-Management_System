-- ============================================================================
-- 🎓 MASTER DATA (DML - Data Manipulation Language)
-- ============================================================================
-- DML statements INSERT, UPDATE, DELETE data
-- This file populates master data tables (countries and cities)
--
-- Spring Boot executes this after schema.sql if configured:
--   spring.sql.init.mode=always
-- ============================================================================

-- ============================================================================
-- COUNTRIES
-- ============================================================================
/**
 * 🎓 INSERT STATEMENT
 * ====================
 * 
 * Syntax: INSERT INTO table (columns) VALUES (values)
 * 
 * Best practices:
 * - Always specify column names (don't rely on order)
 * - Use multiple VALUES for batch insert (faster!)
 * - Include comments for clarity
 */

INSERT INTO country (name, code) VALUES
    ('Sri Lanka', 'LK'),
    ('India', 'IN'),
    ('United States', 'US'),
    ('United Kingdom', 'GB'),
    ('Australia', 'AU'),
    ('Canada', 'CA'),
    ('Singapore', 'SG'),
    ('Malaysia', 'MY'),
    ('Thailand', 'TH'),
    ('Japan', 'JP')
ON DUPLICATE KEY UPDATE name = VALUES(name);

/**
 * 🎓 ON DUPLICATE KEY UPDATE
 * ============================
 * 
 * What it does:
 * - If INSERT would cause duplicate key error (code is UNIQUE)
 * - Instead of failing, UPDATE the existing row
 * 
 * Example:
 * - First run: INSERTs all countries
 * - Second run: Would fail on duplicate 'LK', 'IN', etc.
 * - With ON DUPLICATE KEY UPDATE: Updates name if code exists
 * 
 * Why useful:
 * - Makes script idempotent (safe to run multiple times)
 * - Same result whether run once or 100 times
 * - Common in deployment scripts
 * 
 * Alternative approach:
 *   INSERT IGNORE INTO country...
 *   → Silently skips duplicates (doesn't update)
 */

-- ============================================================================
-- CITIES - SRI LANKA
-- ============================================================================
INSERT INTO city (name, country_id) VALUES
    -- Sri Lanka cities (country_id = 1)
    ('Colombo', (SELECT id FROM country WHERE code = 'LK')),
    ('Kandy', (SELECT id FROM country WHERE code = 'LK')),
    ('Galle', (SELECT id FROM country WHERE code = 'LK')),
    ('Jaffna', (SELECT id FROM country WHERE code = 'LK')),
    ('Negombo', (SELECT id FROM country WHERE code = 'LK')),
    ('Trincomalee', (SELECT id FROM country WHERE code = 'LK')),
    ('Batticaloa', (SELECT id FROM country WHERE code = 'LK')),
    ('Matara', (SELECT id FROM country WHERE code = 'LK')),
    ('Anuradhapura', (SELECT id FROM country WHERE code = 'LK')),
    ('Kurunegala', (SELECT id FROM country WHERE code = 'LK'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

/**
 * 🎓 SUBQUERY IN INSERT
 * =======================
 * 
 * (SELECT id FROM country WHERE code = 'LK')
 * 
 * This is a subquery that:
 * 1. Runs first
 * 2. Returns the ID of Sri Lanka
 * 3. Uses that ID for country_id
 * 
 * Why not hardcode ID?
 * - Auto-increment IDs might vary
 * - Different environments might have different IDs
 * - Using code ('LK') is more portable
 * 
 * Alternative approach:
 *   SET @sri_lanka_id = (SELECT id FROM country WHERE code = 'LK');
 *   INSERT INTO city (name, country_id) VALUES ('Colombo', @sri_lanka_id);
 */

-- ============================================================================
-- CITIES - INDIA
-- ============================================================================
INSERT INTO city (name, country_id) VALUES
    ('Mumbai', (SELECT id FROM country WHERE code = 'IN')),
    ('Delhi', (SELECT id FROM country WHERE code = 'IN')),
    ('Bangalore', (SELECT id FROM country WHERE code = 'IN')),
    ('Hyderabad', (SELECT id FROM country WHERE code = 'IN')),
    ('Chennai', (SELECT id FROM country WHERE code = 'IN')),
    ('Kolkata', (SELECT id FROM country WHERE code = 'IN')),
    ('Pune', (SELECT id FROM country WHERE code = 'IN')),
    ('Ahmedabad', (SELECT id FROM country WHERE code = 'IN')),
    ('Jaipur', (SELECT id FROM country WHERE code = 'IN')),
    ('Surat', (SELECT id FROM country WHERE code = 'IN'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================================
-- CITIES - UNITED STATES
-- ============================================================================
INSERT INTO city (name, country_id) VALUES
    ('New York', (SELECT id FROM country WHERE code = 'US')),
    ('Los Angeles', (SELECT id FROM country WHERE code = 'US')),
    ('Chicago', (SELECT id FROM country WHERE code = 'US')),
    ('Houston', (SELECT id FROM country WHERE code = 'US')),
    ('Phoenix', (SELECT id FROM country WHERE code = 'US')),
    ('Philadelphia', (SELECT id FROM country WHERE code = 'US')),
    ('San Antonio', (SELECT id FROM country WHERE code = 'US')),
    ('San Diego', (SELECT id FROM country WHERE code = 'US')),
    ('Dallas', (SELECT id FROM country WHERE code = 'US')),
    ('San Francisco', (SELECT id FROM country WHERE code = 'US'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================================
-- CITIES - UNITED KINGDOM
-- ============================================================================
INSERT INTO city (name, country_id) VALUES
    ('London', (SELECT id FROM country WHERE code = 'GB')),
    ('Birmingham', (SELECT id FROM country WHERE code = 'GB')),
    ('Manchester', (SELECT id FROM country WHERE code = 'GB')),
    ('Glasgow', (SELECT id FROM country WHERE code = 'GB')),
    ('Liverpool', (SELECT id FROM country WHERE code = 'GB')),
    ('Edinburgh', (SELECT id FROM country WHERE code = 'GB')),
    ('Bristol', (SELECT id FROM country WHERE code = 'GB')),
    ('Cardiff', (SELECT id FROM country WHERE code = 'GB')),
    ('Belfast', (SELECT id FROM country WHERE code = 'GB')),
    ('Leeds', (SELECT id FROM country WHERE code = 'GB'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================================
-- CITIES - OTHER COUNTRIES (Sample)
-- ============================================================================
INSERT INTO city (name, country_id) VALUES
    -- Australia
    ('Sydney', (SELECT id FROM country WHERE code = 'AU')),
    ('Melbourne', (SELECT id FROM country WHERE code = 'AU')),
    ('Brisbane', (SELECT id FROM country WHERE code = 'AU')),
    
    -- Canada
    ('Toronto', (SELECT id FROM country WHERE code = 'CA')),
    ('Vancouver', (SELECT id FROM country WHERE code = 'CA')),
    ('Montreal', (SELECT id FROM country WHERE code = 'CA')),
    
    -- Singapore
    ('Singapore', (SELECT id FROM country WHERE code = 'SG')),
    
    -- Malaysia
    ('Kuala Lumpur', (SELECT id FROM country WHERE code = 'MY')),
    ('George Town', (SELECT id FROM country WHERE code = 'MY')),
    ('Johor Bahru', (SELECT id FROM country WHERE code = 'MY')),
    
    -- Thailand
    ('Bangkok', (SELECT id FROM country WHERE code = 'TH')),
    ('Chiang Mai', (SELECT id FROM country WHERE code = 'TH')),
    ('Phuket', (SELECT id FROM country WHERE code = 'TH')),
    
    -- Japan
    ('Tokyo', (SELECT id FROM country WHERE code = 'JP')),
    ('Osaka', (SELECT id FROM country WHERE code = 'JP')),
    ('Kyoto', (SELECT id FROM country WHERE code = 'JP'))
ON DUPLICATE KEY UPDATE name = VALUES(name);

/**
 * 🎓 MASTER DATA BEST PRACTICES
 * ===============================
 * 
 * 1. IMMUTABLE DATA:
 *    - Countries and cities rarely change
 *    - Safe to hardcode in scripts
 *    - Could also load from CSV/JSON for easier management
 * 
 * 2. REFERENTIAL INTEGRITY:
 *    - Cities reference countries via FK
 *    - Can't insert city without valid country
 *    - Order matters: countries first, then cities!
 * 
 * 3. IDEMPOTENT SCRIPTS:
 *    - Safe to run multiple times
 *    - ON DUPLICATE KEY UPDATE ensures consistency
 *    - Important for CI/CD pipelines
 * 
 * 4. TESTING DATA:
 *    - In test environments, might want different/less data
 *    - Use spring profiles: data-test.sql, data-prod.sql
 * 
 * 5. INTERNATIONALIZATION:
 *    - Consider local language names
 *    - UTF8MB4 supports all characters
 *    - Might need translations table: city_translations
 */

-- ============================================================================
-- SAMPLE CUSTOMER DATA (Optional - for testing)
-- ============================================================================
/**
 * Uncomment below to insert sample customers for testing
 */

/*
INSERT INTO customer (name, date_of_birth, nic_number) VALUES
    ('John Doe', '1990-01-15', '123456789V'),
    ('Jane Smith', '1985-05-20', '987654321V'),
    ('Bob Johnson', '1995-11-30', '456789123V')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Sample mobile numbers
INSERT INTO mobile_number (customer_id, number) VALUES
    ((SELECT id FROM customer WHERE nic_number = '123456789V'), '+94771234567'),
    ((SELECT id FROM customer WHERE nic_number = '123456789V'), '+94712345678'),
    ((SELECT id FROM customer WHERE nic_number = '987654321V'), '+94769876543')
ON DUPLICATE KEY UPDATE number = VALUES(number);

-- Sample addresses
INSERT INTO address (customer_id, address_line1, address_line2, city_id) VALUES
    (
        (SELECT id FROM customer WHERE nic_number = '123456789V'),
        '123 Main Street',
        'Apartment 4B',
        (SELECT id FROM city WHERE name = 'Colombo' LIMIT 1)
    ),
    (
        (SELECT id FROM customer WHERE nic_number = '987654321V'),
        '456 Park Avenue',
        NULL,
        (SELECT id FROM city WHERE name = 'Kandy' LIMIT 1)
    )
ON DUPLICATE KEY UPDATE address_line1 = VALUES(address_line1);
*/

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify data was inserted correctly:

-- Count countries
-- SELECT COUNT(*) as country_count FROM country;

-- Count cities per country
-- SELECT c.name, COUNT(ci.id) as city_count
-- FROM country c
-- LEFT JOIN city ci ON c.id = ci.country_id
-- GROUP BY c.id, c.name
-- ORDER BY city_count DESC;

-- Show all cities with countries
-- SELECT ci.name as city, c.name as country
-- FROM city ci
-- JOIN country c ON ci.country_id = c.id
-- ORDER BY c.name, ci.name;

-- ============================================================================
-- DML COMPLETE! 🎉
-- ============================================================================
-- Database is now ready with master data!
-- Start the application and test the APIs!
-- ============================================================================
