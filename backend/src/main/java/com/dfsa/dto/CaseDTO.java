package com.dfsa.dto;

import com.dfsa.model.Case;

import java.util.Date;

public class CaseDTO {

    private Long id;
    private String caseNumber;
    private String title;
    private String description;
    private Case.Priority priority;
    private Case.Status status;
    private Long investigatorId;
    private String investigatorName; // For display
    private Date createdAt;
    private Date updatedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Case.Priority getPriority() {
        return priority;
    }

    public void setPriority(Case.Priority priority) {
        this.priority = priority;
    }

    public Case.Status getStatus() {
        return status;
    }

    public void setStatus(Case.Status status) {
        this.status = status;
    }

    public Long getInvestigatorId() {
        return investigatorId;
    }

    public void setInvestigatorId(Long investigatorId) {
        this.investigatorId = investigatorId;
    }

    public String getInvestigatorName() {
        return investigatorName;
    }

    public void setInvestigatorName(String investigatorName) {
        this.investigatorName = investigatorName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}