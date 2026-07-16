package com.contractmanagement.repository;

import com.contractmanagement.entity.ContractApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractApprovalRepository extends JpaRepository<ContractApproval, Long> {
}
