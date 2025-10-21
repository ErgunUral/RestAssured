package com.example.listeners;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test Data Information Container
 * Stores comprehensive test execution data and metadata
 */
public class TestDataInfo {
    
    // Basic test information
    private String testName;
    private String testClass;
    private String testMethod;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private String status;
    private String resultMessage;
    private String threadName;
    
    // Test categorization
    private List<String> groups = new ArrayList<>();
    private String suiteName;
    private String testContextName;
    
    // Test parameters and data
    private List<Object> parameters = new ArrayList<>();
    private int parameterCount;
    private String parameterAnalysis;
    private boolean usesDataProvider;
    private String dataProviderName;
    private String dataProviderClass;
    
    // Test instance and configuration
    private String testInstanceClass;
    private Map<String, String> suiteParameters = new ConcurrentHashMap<>();
    private Set<String> contextAttributes = new HashSet<>();
    private Map<String, String> configurationValues = new ConcurrentHashMap<>();
    
    // Data analysis
    private Set<String> parameterTypes = new HashSet<>();
    private Set<String> dataPatterns = new HashSet<>();
    
    // Failure information
    private String failureType;
    private String failureMessage;
    private String stackTrace;
    private Map<String, String> systemInfo = new ConcurrentHashMap<>();
    
    // Constructors
    public TestDataInfo() {
        this.startTime = LocalDateTime.now();
        this.status = "RUNNING";
    }
    
    public TestDataInfo(String testName) {
        this();
        this.testName = testName;
    }
    
    // Basic getters and setters
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public String getTestClass() {
        return testClass;
    }
    
    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }
    
    public String getTestMethod() {
        return testMethod;
    }
    
    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }
    
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    
    // Groups and categorization
    public List<String> getGroups() {
        return groups;
    }
    
    public void setGroups(List<String> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
    }
    
    public void addGroup(String group) {
        if (group != null && !this.groups.contains(group)) {
            this.groups.add(group);
        }
    }
    
    public String getSuiteName() {
        return suiteName;
    }
    
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }
    
    public String getTestContextName() {
        return testContextName;
    }
    
    public void setTestContextName(String testContextName) {
        this.testContextName = testContextName;
    }
    
    // Parameters and data provider
    public List<Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.parameterCount = this.parameters.size();
    }
    
    public int getParameterCount() {
        return parameterCount;
    }
    
    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }
    
    public String getParameterAnalysis() {
        return parameterAnalysis;
    }
    
    public void setParameterAnalysis(String parameterAnalysis) {
        this.parameterAnalysis = parameterAnalysis;
    }
    
    public boolean isUsesDataProvider() {
        return usesDataProvider;
    }
    
    public void setUsesDataProvider(boolean usesDataProvider) {
        this.usesDataProvider = usesDataProvider;
    }
    
    public String getDataProviderName() {
        return dataProviderName;
    }
    
    public void setDataProviderName(String dataProviderName) {
        this.dataProviderName = dataProviderName;
        this.usesDataProvider = (dataProviderName != null && !dataProviderName.isEmpty());
    }
    
    public String getDataProviderClass() {
        return dataProviderClass;
    }
    
    public void setDataProviderClass(String dataProviderClass) {
        this.dataProviderClass = dataProviderClass;
    }
    
    // Test instance and configuration
    public String getTestInstanceClass() {
        return testInstanceClass;
    }
    
    public void setTestInstanceClass(String testInstanceClass) {
        this.testInstanceClass = testInstanceClass;
    }
    
    public Map<String, String> getSuiteParameters() {
        return suiteParameters;
    }
    
    public void setSuiteParameters(Map<String, String> suiteParameters) {
        this.suiteParameters = suiteParameters != null ? suiteParameters : new ConcurrentHashMap<>();
    }
    
    public Set<String> getContextAttributes() {
        return contextAttributes;
    }
    
    public void setContextAttributes(Set<String> contextAttributes) {
        this.contextAttributes = contextAttributes != null ? contextAttributes : new HashSet<>();
    }
    
    public Map<String, String> getConfigurationValues() {
        return configurationValues;
    }
    
    public void setConfigurationValues(Map<String, String> configurationValues) {
        this.configurationValues = configurationValues != null ? configurationValues : new ConcurrentHashMap<>();
    }
    
    public void addConfigurationValue(String key, String value) {
        if (key != null && value != null) {
            this.configurationValues.put(key, value);
        }
    }
    
    // Data analysis
    public Set<String> getParameterTypes() {
        return parameterTypes;
    }
    
    public void setParameterTypes(Set<String> parameterTypes) {
        this.parameterTypes = parameterTypes != null ? parameterTypes : new HashSet<>();
    }
    
    public void addParameterType(String parameterType) {
        if (parameterType != null) {
            this.parameterTypes.add(parameterType);
        }
    }
    
    public Set<String> getDataPatterns() {
        return dataPatterns;
    }
    
    public void setDataPatterns(Set<String> dataPatterns) {
        this.dataPatterns = dataPatterns != null ? dataPatterns : new HashSet<>();
    }
    
    public void addDataPattern(String dataPattern) {
        if (dataPattern != null) {
            this.dataPatterns.add(dataPattern);
        }
    }
    
    // Failure information
    public String getFailureType() {
        return failureType;
    }
    
    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }
    
    public String getFailureMessage() {
        return failureMessage;
    }
    
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    public Map<String, String> getSystemInfo() {
        return systemInfo;
    }
    
    public void setSystemInfo(Map<String, String> systemInfo) {
        this.systemInfo = systemInfo != null ? systemInfo : new ConcurrentHashMap<>();
    }
    
    public void addSystemInfo(String key, String value) {
        if (key != null && value != null) {
            this.systemInfo.put(key, value);
        }
    }
    
    // Utility methods
    public boolean isCompleted() {
        return endTime != null;
    }
    
    public boolean isSuccessful() {
        return "PASSED".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    public boolean isSkipped() {
        return "SKIPPED".equals(status);
    }
    
    public boolean hasParameters() {
        return parameters != null && !parameters.isEmpty();
    }
    
    public boolean hasFailureInfo() {
        return failureType != null || failureMessage != null;
    }
    
    public String getFormattedDuration() {
        if (duration == 0) return "0 ms";
        
        long seconds = duration / 1000;
        long ms = duration % 1000;
        
        if (seconds > 0) {
            return String.format("%d sn %d ms", seconds, ms);
        } else {
            return String.format("%d ms", ms);
        }
    }
    
    public String getShortTestName() {
        if (testName == null) return "Unknown";
        
        int lastDot = testName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < testName.length() - 1) {
            return testName.substring(lastDot + 1);
        }
        return testName;
    }
    
    public String getTestCategory() {
        if (groups.isEmpty()) return "Uncategorized";
        
        // Return the first group as primary category
        return groups.get(0);
    }
    
    public boolean belongsToGroup(String group) {
        return groups.contains(group);
    }
    
    public boolean hasDataPattern(String pattern) {
        return dataPatterns.contains(pattern);
    }
    
    public boolean hasParameterType(String type) {
        return parameterTypes.contains(type);
    }
    
    // Summary methods
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Test: ").append(getShortTestName()).append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Duration: ").append(getFormattedDuration()).append("\n");
        summary.append("Category: ").append(getTestCategory()).append("\n");
        
        if (usesDataProvider) {
            summary.append("Data Provider: ").append(dataProviderName).append("\n");
            summary.append("Parameters: ").append(parameterCount).append("\n");
        }
        
        if (hasFailureInfo()) {
            summary.append("Failure: ").append(failureType).append("\n");
        }
        
        return summary.toString();
    }
    
    public String getDetailedSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=".repeat(50)).append("\n");
        summary.append("TEST DATA SUMMARY\n");
        summary.append("=".repeat(50)).append("\n");
        summary.append("Test Name: ").append(testName).append("\n");
        summary.append("Test Class: ").append(testClass).append("\n");
        summary.append("Test Method: ").append(testMethod).append("\n");
        summary.append("Description: ").append(description).append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Duration: ").append(getFormattedDuration()).append("\n");
        summary.append("Thread: ").append(threadName).append("\n");
        summary.append("Groups: ").append(String.join(", ", groups)).append("\n");
        
        if (usesDataProvider) {
            summary.append("\nDATA PROVIDER INFO:\n");
            summary.append("Provider Name: ").append(dataProviderName).append("\n");
            summary.append("Provider Class: ").append(dataProviderClass).append("\n");
            summary.append("Parameter Count: ").append(parameterCount).append("\n");
            summary.append("Parameter Types: ").append(String.join(", ", parameterTypes)).append("\n");
            summary.append("Data Patterns: ").append(String.join(", ", dataPatterns)).append("\n");
        }
        
        if (!configurationValues.isEmpty()) {
            summary.append("\nCONFIGURATION:\n");
            configurationValues.forEach((key, value) -> 
                summary.append(key).append(": ").append(value).append("\n")
            );
        }
        
        if (hasFailureInfo()) {
            summary.append("\nFAILURE INFO:\n");
            summary.append("Failure Type: ").append(failureType).append("\n");
            summary.append("Failure Message: ").append(failureMessage).append("\n");
            
            if (!systemInfo.isEmpty()) {
                summary.append("\nSYSTEM INFO AT FAILURE:\n");
                systemInfo.forEach((key, value) -> 
                    summary.append(key).append(": ").append(value).append("\n")
                );
            }
        }
        
        summary.append("=".repeat(50)).append("\n");
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return String.format("TestDataInfo{testName='%s', status='%s', duration=%d ms, groups=%s}", 
            testName, status, duration, groups);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestDataInfo that = (TestDataInfo) o;
        return Objects.equals(testName, that.testName) &&
               Objects.equals(testClass, that.testClass) &&
               Objects.equals(testMethod, that.testMethod);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(testName, testClass, testMethod);
    }
}