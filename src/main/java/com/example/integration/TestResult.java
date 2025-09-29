package com.example.integration;

import java.util.List;

public class TestResult {
    private String scenarioId;
    private String scenarioName;
    private String status;
    private long startTime;
    private long endTime;
    private long duration;
    private String errorMessage;
    private List<String> screenshots;
    private String details;

    // Constructors
    public TestResult() {}

    public TestResult(String scenarioId, String scenarioName) {
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.startTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getScenarioId() { return scenarioId; }
    public void setScenarioId(String scenarioId) { this.scenarioId = scenarioId; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<String> getScreenshots() { return screenshots; }
    public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}