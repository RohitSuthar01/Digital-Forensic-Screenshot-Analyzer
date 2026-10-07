package com.dfsa.service;

import com.dfsa.dto.CaseDTO;
import com.dfsa.mapper.CaseMapper;
import com.dfsa.model.Case;
import com.dfsa.model.User;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseMapper caseMapper;

    public CaseDTO createCase(CaseDTO caseDTO, Long investigatorId) {
        User investigator = userRepository.findById(investigatorId)
                .orElseThrow(() -> new RuntimeException("Investigator not found"));

        Case theCase = caseMapper.toEntity(caseDTO);
        theCase.setInvestigator(investigator);
        theCase.setCaseNumber(generateCaseNumber());
        theCase.setStatus(Case.Status.OPEN); // Default status
        theCase.setCreatedAt(new Date());
        theCase.setUpdatedAt(new Date());

        Case savedCase = caseRepository.save(theCase);
        return caseMapper.toDTO(savedCase);
    }

    public CaseDTO getCaseById(Long id, Long currentUserId) {
        Case theCase = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Authorization: check if the current user is the investigator or an admin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isInvestigator = theCase.getInvestigator().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole().equals(com.dfsa.model.Role.ADMIN);

        if (!isInvestigator && !isAdmin) {
            // Check if the case is shared with the current user
            // We'll implement case sharing later, for now we don't have the repository for case shares.
            // For simplicity, we'll throw an exception if not investigator and not admin.
            throw new RuntimeException("Access denied: You are not authorized to view this case");
        }

        return caseMapper.toDTO(theCase);
    }

    public Page<CaseDTO> getAllCases(int page, int size, String sortBy, String direction, Long currentUserId, boolean isAdmin) {
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Case> casePage;
        if (isAdmin) {
            // Admin can see all cases
            casePage = caseRepository.findAll(pageable);
        } else {
            // Regular user can only see their own cases
            casePage = caseRepository.findByInvestigatorId(currentUserId, pageable);
        }
        return casePage.map(caseMapper::toDTO);
    }

    public CaseDTO updateCase(Long id, CaseDTO caseDTO, Long currentUserId) {
        Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Authorization: only the investigator or admin can update
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isInvestigator = existingCase.getInvestigator().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole().equals(com.dfsa.model.Role.ADMIN);

        if (!isInvestigator && !isAdmin) {
            throw new RuntimeException("Access denied: You are not authorized to update this case");
        }

        // Update fields
        existingCase.setTitle(caseDTO.getTitle());
        existingCase.setDescription(caseDTO.getDescription());
        existingCase.setPriority(caseDTO.getPriority());
        existingCase.setStatus(caseDTO.getStatus());
        existingCase.setUpdatedAt(new Date());
        Case updatedCase = caseRepository.save(existingCase);
        return caseMapper.toDTO(updatedCase);
    }

    public void deleteCase(Long id, Long currentUserId) {
        Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Authorization: only the investigator or admin can delete
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isInvestigator = existingCase.getInvestigator().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole().equals(com.dfsa.model.Role.ADMIN);

        if (!isInvestigator && !isAdmin) {
            throw new RuntimeException("Access denied: You are not authorized to delete this case");
        }

        caseRepository.deleteById(id);
    }

    private String generateCaseNumber() {
        // Generate a case number like DFSA-2026-0001
        long count = caseRepository.count();
        String number = String.format("%04d", count + 1);
        return "DFSA-" + java.time.Year.now().getValue() + "-" + number;
    }
}