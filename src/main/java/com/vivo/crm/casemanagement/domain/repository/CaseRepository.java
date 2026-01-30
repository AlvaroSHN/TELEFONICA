package com.vivo.crm.casemanagement.domain.repository;

import com.vivo.crm.casemanagement.domain.model.Case;
import com.vivo.crm.casemanagement.domain.model.CasePriority;
import com.vivo.crm.casemanagement.domain.model.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, String> {

    Optional<Case> findByProtocol(String protocol);

    Optional<Case> findBySalesforceCaseId(String salesforceCaseId);

    List<Case> findByStatus(CaseStatus status);

    List<Case> findByPriority(CasePriority priority);

    List<Case> findByCustomerId(String customerId);

    @Query("SELECT c FROM Case c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:ticketType IS NULL OR c.ticketType = :ticketType)")
    List<Case> findByFilters(
            @Param("status") CaseStatus status,
            @Param("priority") CasePriority priority,
            @Param("ticketType") String ticketType
    );
}
