package com.example.integration;

public class TestStep {
    private int order;
    private String description;
    private String action;
    private String expectedResult;

    // Constructors
    public TestStep() {}

    // Getters and Setters
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
}