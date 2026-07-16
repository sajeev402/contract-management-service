package com.contractmanagement.repository;

import com.contractmanagement.entity.ContractLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractLineItemRepository extends JpaRepository<ContractLineItem, Long> {
}
