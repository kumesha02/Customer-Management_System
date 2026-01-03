-- ============================================================================
-- 🎓 DATABASE SCHEMA (DDL - Data Definition Language)
-- ============================================================================
-- DDL statements CREATE the database structure (tables, constraints, indexes)
-- 
-- This file is executed automatically by Spring Boot on startup if configured:
--   spring.sql.init.mode=always
--
-- ============================================================================

-- ============================================================================
-- 1. COUNTRY TABLE (Master Data)
-- ============================================================================
CREATE TABLE IF NOT EXISTS country (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key, auto-incremented',
    name VARCHAR(100) NOT NULL COMMENT 'Country name (e.g., Sri Lanka, India)',
    code VARCHAR(3) UNIQUE COMMENT 'ISO country code (e.g., LK, IN, US)',
    
    INDEX idx_country_name (name),
    INDEX idx_country_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Master table for countries';

/**
 * 🎓 CONCEPT: Table Creation Options
 * ====================================
 * 
 * CREATE TABLE IF NOT EXISTS:
 * - Safe to run multiple times
 * - Won't fail if table already exists
 * 
 * AUTO_INCREMENT:
 * - Database automatically generates sequential IDs
 * - Starts at 1, increments by 1
 * 
 * PRIMARY KEY:
 * - Uniquely identifies each row
 * - Automatically creates index
 * - Cannot be NULL
 * 
 * UNIQUE:
 * - Ensures no duplicate values
 * - NULL values allowed (unless NOT NULL specified)
 * - Creates index automatically
 * 
 * NOT NULL:
 * - Field must have a value
 * - INSERT without value fails
 * 
 * ENGINE=InnoDB:
 * - InnoDB supports transactions (ACID)
 * - Supports foreign keys
 * - Row-level locking (better concurrency)
 * - Recommended for almost all use cases
 * 
 * CHARSET & COLLATION:
 * - utf8mb4: Full Unicode support (including emojis!)
 * - utf8mb4_unicode_ci: Case-insensitive Unicode collation
 * - CRITICAL for international applications
 * 
 * COMMENT:
 * - Documentation in database
 * - Helps DBAs understand schema
 */

-- ============================================================================
-- 2. CITY TABLE (Master Data)
-- ============================================================================
CREATE TABLE IF NOT EXISTS city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'City name (e.g., Colombo, Mumbai)',
    country_id BIGINT NOT NULL COMMENT 'Foreign key to country table',
    
    INDEX idx_city_name (name),
    INDEX idx_city_country (country_id),
    
    -- FOREIGN KEY CONSTRAINT
    CONSTRAINT fk_city_country FOREIGN KEY (country_id) 
        REFERENCES country(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Master table for cities, linked to countries';

/**
 * 🎓 CONCEPT: Foreign Key Constraints
 * =====================================
 * 
 * FOREIGN KEY:
 * - Links two tables together
 * - Ensures referential integrity
 * - city.country_id MUST exist in country.id
 * 
 * ON DELETE RESTRICT:
 * - Prevents deleting a country if cities reference it
 * - Example: Can't delete "Sri Lanka" if "Colombo" exists
 * - Alternative: CASCADE (delete cities too), SET NULL, NO ACTION
 * 
 * ON UPDATE CASCADE:
 * - If country.id changes, city.country_id updates automatically
 * - Rarely needed (primary keys don't usually change)
 * - Alternative: RESTRICT, SET NULL, NO ACTION
 * 
 * Benefits of Foreign Keys:
 * - Data integrity (orphaned records impossible)
 * - Clear relationship definition
 * - Prevents invalid data
 * 
 * When NOT to use:
 * - Very high-volume writes (FK checks add overhead)
 * - Flexible schemas (NoSQL-style)
 * - When application handles integrity
 */

-- ============================================================================
-- 3. CUSTOMER TABLE (Main Entity)
-- ============================================================================
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT 'Customer full name',
    date_of_birth DATE NOT NULL COMMENT 'Date of birth',
    nic_number VARCHAR(20) NOT NULL UNIQUE COMMENT 'National Identity Card number (unique)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    
    INDEX idx_customer_name (name),
    INDEX idx_customer_nic (nic_number),
    INDEX idx_customer_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Main customer table';

/**
 * 🎓 CONCEPT: Timestamp Columns (Audit Fields)
 * ==============================================
 * 
 * DEFAULT CURRENT_TIMESTAMP:
 * - Automatically set to current time when row is inserted
 * - You don't need to provide value
 * 
 * ON UPDATE CURRENT_TIMESTAMP:
 * - Automatically updates to current time when row is modified
 * - Tracks last modification
 * 
 * These are "audit fields" - track who did what when.
 * Essential for:
 * - Debugging
 * - Compliance
 * - Business analytics
 * - Conflict resolution
 */

-- ============================================================================
-- 4. MOBILE_NUMBER TABLE (One-to-Many with Customer)
-- ============================================================================
CREATE TABLE IF NOT EXISTS mobile_number (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL COMMENT 'Foreign key to customer',
    number VARCHAR(20) NOT NULL COMMENT 'Phone number',
    
    INDEX idx_mobile_customer (customer_id),
    INDEX idx_mobile_number (number),
    
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer mobile numbers (one customer can have multiple)';

/**
 * 🎓 CONCEPT: ON DELETE CASCADE
 * ==============================
 * 
 * When customer is deleted, all their mobile numbers are automatically deleted.
 * 
 * Example:
 * - Customer ID 1 has mobile numbers: M1, M2, M3
 * - DELETE FROM customer WHERE id = 1
 * - Result: M1, M2, M3 also deleted automatically!
 * 
 * This matches our JPA entity configuration:
 *   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
 * 
 * Database and application layer agree = good design!
 */

-- ============================================================================
-- 5. ADDRESS TABLE (One-to-Many with Customer)
-- ============================================================================
CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL COMMENT 'Foreign key to customer',
    address_line1 VARCHAR(255) COMMENT 'Address line 1',
    address_line2 VARCHAR(255) COMMENT 'Address line 2',
    city_id BIGINT COMMENT 'Foreign key to city (optional)',
    
    INDEX idx_address_customer (customer_id),
    INDEX idx_address_city (city_id),
    
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_address_city FOREIGN KEY (city_id)
        REFERENCES city(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Customer addresses (one customer can have multiple)';

/**
 * 🎓 CONCEPT: Multiple Foreign Keys
 * ===================================
 * 
 * Address table has TWO foreign keys:
 * 1. customer_id → customer (parent)
 * 2. city_id → city (reference data)
 * 
 * Different ON DELETE behaviors:
 * - customer deleted → CASCADE (delete addresses)
 * - city deleted → RESTRICT (prevent if addresses exist)
 * 
 * This reflects business rules:
 * - Customer owns addresses (cascade delete)
 * - City is shared data (protect from deletion)
 */

-- ============================================================================
-- 6. CUSTOMER_FAMILY TABLE (Many-to-Many Junction Table)
-- ============================================================================
CREATE TABLE IF NOT EXISTS customer_family (
    customer_id BIGINT NOT NULL COMMENT 'Customer ID',
    family_member_id BIGINT NOT NULL COMMENT 'Family member customer ID',
    
    -- COMPOSITE PRIMARY KEY (both columns together must be unique)
    PRIMARY KEY (customer_id, family_member_id),
    
    INDEX idx_family_customer (customer_id),
    INDEX idx_family_member (family_member_id),
    
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_family_member FOREIGN KEY (family_member_id)
        REFERENCES customer(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    -- Prevent self-reference (customer can't be their own family member!)
    CONSTRAINT chk_no_self_reference CHECK (customer_id != family_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Many-to-many relationship: customers can have multiple family members';

/**
 * 🎓 CONCEPT: Junction Table (Many-to-Many)
 * ===========================================
 * 
 * PROBLEM: Customer can have many family members, family members can have many customers
 * Can't store in one table!
 * 
 * SOLUTION: Junction/Join/Link table
 * 
 * Example data:
 * customer_id | family_member_id
 * ------------|------------------
 *      1      |        2          (Customer 1's family is Customer 2)
 *      1      |        3          (Customer 1's family is Customer 3)
 *      2      |        1          (Customer 2's family is Customer 1)
 * 
 * COMPOSITE PRIMARY KEY:
 * - Both columns together must be unique
 * - Prevents duplicate relationships
 * - (1, 2) can exist only once
 * 
 * SELF-REFERENCING:
 * - Both FKs point to same table (customer)
 * - This is a "self-referential many-to-many"
 * - Rare but valid pattern
 * 
 * CHECK CONSTRAINT:
 * - Business rule enforcement in database
 * - customer_id != family_member_id prevents self-reference
 * - Database rejects: INSERT INTO customer_family VALUES (1, 1)
 */

-- ============================================================================
-- 🎓 INDEXES: Performance Optimization
-- ============================================================================
/**
 * WHY INDEXES?
 * ------------
 * Without index:
 *   SELECT * FROM customer WHERE nic_number = '123456789V'
 *   → Full table scan (checks every row) - SLOW for large tables!
 * 
 * With index:
 *   → Index lookup (like book index) - FAST!
 *   → 1,000,000 rows: 0.001s instead of 10s
 * 
 * WHEN TO INDEX:
 * - Primary keys (automatic)
 * - Foreign keys (for joins)
 * - Fields used in WHERE clauses
 * - Fields used in ORDER BY
 * - Fields used in JOIN conditions
 * 
 * WHEN NOT TO INDEX:
 * - Tables with few rows (< 1000)
 * - Columns rarely queried
 * - Columns with mostly duplicate values
 * - Tables with heavy writes (indexes slow down INSERT/UPDATE)
 * 
 * TRADE-OFFS:
 * - Faster SELECTs
 * - Slower INSERTs/UPDATEs/DELETEs
 * - More disk space
 * 
 * Our indexes are well-chosen:
 * - Foreign keys (for joins)
 * - Unique fields (nic_number)
 * - Common search fields (name)
 */

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- Run these to verify schema was created correctly:

-- Show all tables
-- SHOW TABLES;

-- Describe customer table
-- DESCRIBE customer;

-- Show foreign keys
-- SELECT 
--     TABLE_NAME,
--     COLUMN_NAME,
--     CONSTRAINT_NAME,
--     REFERENCED_TABLE_NAME,
--     REFERENCED_COLUMN_NAME
-- FROM
--     INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE
--     REFERENCED_TABLE_SCHEMA = 'customer_management'
--     AND REFERENCED_TABLE_NAME IS NOT NULL;

-- ============================================================================
-- DDL COMPLETE! 🎉
-- ============================================================================
-- Next: Run data.sql to populate master data (countries and cities)
-- ============================================================================
