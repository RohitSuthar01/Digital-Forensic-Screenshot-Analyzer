package com.dfsa.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Entity
@Table(name = "comparison_results")
public class ComparisonResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reference_screenshot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Screenshot referenceScreenshot;

    @OneToOne
    @JoinColumn(name = "target_screenshot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Screenshot targetScreenshot;

    private boolean visualDifferenceDetected;
    
    @Column(columnDefinition = "TEXT")
    private String changedRegionsDetails; // JSON or text

    private String differenceMapFileName;

    private String methodVersion;
    
    @Column(columnDefinition = "TEXT")
    private String limitations;

    @Temporal(TemporalType.TIMESTAMP)
    private Date comparedAt;

    public ComparisonResult() {
        this.comparedAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Screenshot getReferenceScreenshot() { return referenceScreenshot; }
    public void setReferenceScreenshot(Screenshot referenceScreenshot) { this.referenceScreenshot = referenceScreenshot; }
    
    public Screenshot getTargetScreenshot() { return targetScreenshot; }
    public void setTargetScreenshot(Screenshot targetScreenshot) { this.targetScreenshot = targetScreenshot; }

    public boolean isVisualDifferenceDetected() { return visualDifferenceDetected; }
    public void setVisualDifferenceDetected(boolean visualDifferenceDetected) { this.visualDifferenceDetected = visualDifferenceDetected; }

    public String getChangedRegionsDetails() { return changedRegionsDetails; }
    public void setChangedRegionsDetails(String changedRegionsDetails) { this.changedRegionsDetails = changedRegionsDetails; }

    public String getDifferenceMapFileName() { return differenceMapFileName; }
    public void setDifferenceMapFileName(String differenceMapFileName) { this.differenceMapFileName = differenceMapFileName; }

    public String getMethodVersion() { return methodVersion; }
    public void setMethodVersion(String methodVersion) { this.methodVersion = methodVersion; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public Date getComparedAt() { return comparedAt; }
    public void setComparedAt(Date comparedAt) { this.comparedAt = comparedAt; }
}
