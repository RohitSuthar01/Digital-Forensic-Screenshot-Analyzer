package com.dfsa.repository;

import com.dfsa.model.CaseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNote, Long> {
    // CaseNote entity uses 'theCase' (ManyToOne), so Spring derives: theCase.id
    List<CaseNote> findByTheCase_Id(Long caseId);
}