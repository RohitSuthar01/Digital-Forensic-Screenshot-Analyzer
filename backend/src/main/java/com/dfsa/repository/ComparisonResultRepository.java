package com.dfsa.repository;

import com.dfsa.model.ComparisonResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComparisonResultRepository extends JpaRepository<ComparisonResult, Long> {
    Optional<ComparisonResult> findByTargetScreenshotId(Long targetScreenshotId);
}
