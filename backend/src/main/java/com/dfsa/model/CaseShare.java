package com.dfsa.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "case_shares", indexes = {
        @Index(columnList = "case_id"),
        @Index(columnList = "shared_with_user_id")
})
public class CaseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private com.dfsa.model.Case theCase;

    @ManyToOne
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private com.dfsa.model.User sharedWithUser;

    @ManyToOne
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private com.dfsa.model.User sharedByUser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sharedAt;

    // Constructors
    public CaseShare() {
        this.sharedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public com.dfsa.model.Case getTheCase() { return theCase; }
    public void setTheCase(com.dfsa.model.Case theCase) { this.theCase = theCase; }

    public com.dfsa.model.User getSharedWithUser() { return sharedWithUser; }
    public void setSharedWithUser(com.dfsa.model.User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public com.dfsa.model.User getSharedByUser() { return sharedByUser; }
    public void setSharedByUser(com.dfsa.model.User sharedByUser) { this.sharedByUser = sharedByUser; }

    public Date getSharedAt() { return sharedAt; }
    public void setSharedAt(Date sharedAt) { this.sharedAt = sharedAt; }
}