package com.cms.repository;

import com.cms.entity.MobileNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 🎓 MobileNumber Repository
 * ============================================================================
 * 
 * Simple repository for mobile numbers.
 * Typically accessed through Customer entity, but separate repository
 * allows independent queries if needed.
 * 
 * ============================================================================
 */
@Repository
public interface MobileNumberRepository extends JpaRepository<MobileNumber, Long> {

    /**
     * Find all mobile numbers for a customer
     * 
     * Usually accessed via customer.getMobileNumbers(), but this
     * allows direct database query if needed.
     */
    List<MobileNumber> findByCustomerId(Long customerId);
}
