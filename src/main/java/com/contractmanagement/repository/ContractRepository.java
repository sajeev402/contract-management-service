package com.contractmanagement.repository;

import com.contractmanagement.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    
    @Modifying
    @Query("UPDATE Contract c SET c.status = 'EXPIRED', c.updatedAt = :now WHERE c.status = 'ACTIVE' AND c.endDate < :today AND c.deleted = false")
    int expireContracts(@Param("today") LocalDate today, @Param("now") OffsetDateTime now);
}
