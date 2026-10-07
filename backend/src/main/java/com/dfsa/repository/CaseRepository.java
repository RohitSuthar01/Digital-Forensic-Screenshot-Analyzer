package com.dfsa.repository;

import com.dfsa.model.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    Optional<Case> findByCaseNumber(String caseNumber);
    Page<Case> findByInvestigatorId(Long investigatorId, Pageable pageable);
    List<Case> findByInvestigatorId(Long investigatorId);
    long countByPriority(Case.Priority priority);
}