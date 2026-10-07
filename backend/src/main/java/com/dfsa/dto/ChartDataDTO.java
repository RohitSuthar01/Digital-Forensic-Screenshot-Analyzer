package com.dfsa.dto;

public class ChartDataDTO {

    private String label;
    private long value;

    // Constructors
    public ChartDataDTO() {}

    public ChartDataDTO(String label, long value) {
        this.label = label;
        this.value = value;
    }

    // Getters and Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}