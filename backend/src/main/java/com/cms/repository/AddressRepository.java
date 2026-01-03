package com.cms.repository;

import com.cms.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 🎓 Address Repository
 * ============================================================================
 * 
 * Repository for customer addresses.
 * Similar to MobileNumberRepository - simple and focused.
 * 
 * ============================================================================
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a customer
     */
    List<Address> findByCustomerId(Long customerId);

    /**
     * Find all addresses in a specific city
     * 
     * Useful for analytics: "How many customers in Colombo?"
     */
    List<Address> findByCityId(Long cityId);
}
