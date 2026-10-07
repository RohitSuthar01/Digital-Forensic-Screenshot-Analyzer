package com.dfsa.mapper;

import com.dfsa.dto.CaseDTO;
import com.dfsa.model.Case;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

    public CaseDTO toDTO(Case theCase) {
        CaseDTO dto = new CaseDTO();
        dto.setId(theCase.getId());
        dto.setCaseNumber(theCase.getCaseNumber());
        dto.setTitle(theCase.getTitle());
        dto.setDescription(theCase.getDescription());
        dto.setPriority(theCase.getPriority());
        dto.setStatus(theCase.getStatus());
        dto.setInvestigatorId(theCase.getInvestigator().getId());
        dto.setInvestigatorName(theCase.getInvestigator().getFirstName() + " " + theCase.getInvestigator().getLastName());
        dto.setCreatedAt(theCase.getCreatedAt());
        dto.setUpdatedAt(theCase.getUpdatedAt());
        return dto;
    }

    public Case toEntity(CaseDTO dto) {
        Case theCase = new Case();
        theCase.setId(dto.getId());
        theCase.setCaseNumber(dto.getCaseNumber());
        theCase.setTitle(dto.getTitle());
        theCase.setDescription(dto.getDescription());
        theCase.setPriority(dto.getPriority());
        theCase.setStatus(dto.getStatus());
        // Note: investigator and timestamps are set in the service
        return theCase;
    }
}