package com.dfsa.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "case_notes", indexes = {
        @Index(columnList = "case_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "created_at")
})
public class CaseNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private Case theCase;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // Constructors
    public CaseNote() {
        this.createdAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Case getTheCase() { return theCase; }
    public void setTheCase(Case theCase) { this.theCase = theCase; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}