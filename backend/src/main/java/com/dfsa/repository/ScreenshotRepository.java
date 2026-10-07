package com.dfsa.repository;

import com.dfsa.model.Screenshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenshotRepository extends JpaRepository<Screenshot, Long> {
    Optional<Screenshot> findBySha256(String sha256);
    List<Screenshot> findByTheCase_Id(Long caseId);
    List<Screenshot> findByStatusIn(List<Screenshot.Status> statuses);

    @Query("SELECT COUNT(s) FROM Screenshot s WHERE s.status IN :statuses")
    Long countByStatusIn(@Param("statuses") List<Screenshot.Status> statuses);

    @Query(value = "SELECT DATE(s.uploaded_at) as date, COUNT(s.id) as count " +
           "FROM screenshots s " +
           "WHERE s.uploaded_at >= CURDATE() - INTERVAL 7 DAY " +
           "GROUP BY DATE(s.uploaded_at) " +
           "ORDER BY DATE(s.uploaded_at)",
           nativeQuery = true)
    List<Object[]> getUploadsPerDayLast7Days();
}