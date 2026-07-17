package com.contractmanagement.repository;

import com.contractmanagement.entity.Contract;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContractSpecification {

    public static Specification<Contract> withFilters(
            ContractStatus status,
            String customerReference,
            ContractType contractType,
            BigDecimal minimumNetValue,
            BigDecimal maximumNetValue,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // By default exclude deleted contracts
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(customerReference)) {
                predicates.add(criteriaBuilder.equal(root.get("customerReference"), customerReference));
            }

            if (contractType != null) {
                predicates.add(criteriaBuilder.equal(root.get("contractType"), contractType));
            }

            if (minimumNetValue != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("netValue"), minimumNetValue));
            }

            if (maximumNetValue != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("netValue"), maximumNetValue));
            }

            if (startDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }

            if (startDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }

            if (endDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }

            if (endDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
