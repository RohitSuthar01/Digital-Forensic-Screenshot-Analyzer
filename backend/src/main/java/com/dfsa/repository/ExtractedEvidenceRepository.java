package com.dfsa.repository;

import com.dfsa.model.ExtractedEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtractedEvidenceRepository extends JpaRepository<ExtractedEvidence, Long> {
    // ExtractedEvidence entity has 'screenshot' (ManyToOne), so use screenshot.id
    List<ExtractedEvidence> findByScreenshot_Id(Long screenshotId);
}