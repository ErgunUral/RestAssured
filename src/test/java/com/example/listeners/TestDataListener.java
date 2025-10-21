package com.example.listeners;

import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Test Data Listener for PayTR Test Suite
 * Provides comprehensive test data tracking and reporting
 */
public class TestDataListener implements ITestListener {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ConcurrentHashMap<String, TestDataInfo> testDataMap = new ConcurrentHashMap<>();
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        
        // Capture test data information
        TestDataInfo dataInfo = captureTestDataInfo(result);
        testDataMap.put(testName, dataInfo);
        
        // Log test data information
        logTestDataInfo(testName, dataInfo);
        
        // Add to Allure
        addTestDataToAllure(testName, dataInfo);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        updateTestDataResult(testName, "PASSED", null);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        String errorMessage = getErrorMessage(result);
        updateTestDataResult(testName, "FAILED", errorMessage);
        
        // Capture additional failure data
        captureFailureData(testName, result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);
        String skipReason = getSkipReason(result);
        updateTestDataResult(testName, "SKIPPED", skipReason);
    }
    
    /**
     * Captures comprehensive test data information
     */
    private TestDataInfo captureTestDataInfo(ITestResult result) {
        TestDataInfo dataInfo = new TestDataInfo();
        
        // Basic test information
        dataInfo.setTestName(getTestName(result));
        dataInfo.setTestClass(result.getTestClass().getName());
        dataInfo.setTestMethod(result.getMethod().getMethodName());
        dataInfo.setStartTime(LocalDateTime.now());
        dataInfo.setThreadName(Thread.currentThread().getName());
        
        // Test groups
        String[] groups = result.getMethod().getGroups();
        dataInfo.setGroups(groups != null ? Arrays.asList(groups) : Arrays.asList());
        
        // Test description
        String description = result.getMethod().getDescription();
        dataInfo.setDescription(description != null ? description : "No description");
        
        // Test parameters
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            dataInfo.setParameters(Arrays.asList(parameters));
            dataInfo.setParameterCount(parameters.length);
        }
        
        // Test context and configuration
        captureTestContext(result, dataInfo);
        
        // Data provider information
        captureDataProviderInfo(result, dataInfo);
        
        return dataInfo;
    }
    
    /**
     * Captures test context and configuration
     */
    private void captureTestContext(ITestResult result, TestDataInfo dataInfo) {
        try {
            // Test suite information
            if (result.getTestContext() != null) {
                dataInfo.setSuiteName(result.getTestContext().getSuite().getName());
                dataInfo.setTestContextName(result.getTestContext().getName());
                
                // Suite parameters
                dataInfo.setSuiteParameters(result.getTestContext().getSuite().getParameters());
                
                // Test context attributes
                dataInfo.setContextAttributes(result.getTestContext().getAttributeNames());
            }
            
            // Test instance information
            Object testInstance = result.getInstance();
            if (testInstance != null) {
                dataInfo.setTestInstanceClass(testInstance.getClass().getName());
                
                // Try to capture test configuration from instance
                captureTestConfiguration(testInstance, dataInfo);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test context capture hatası: " + e.getMessage());
        }
    }
    
    /**
     * Captures data provider information
     */
    private void captureDataProviderInfo(ITestResult result, TestDataInfo dataInfo) {
        try {
            // Check if test uses data provider
            if (result.getMethod().getDataProvider() != null) {
                dataInfo.setDataProviderName(result.getMethod().getDataProvider());
                dataInfo.setUsesDataProvider(true);
                
                // Capture data provider class
                Class<?> dataProviderClass = result.getMethod().getDataProviderClass();
                if (dataProviderClass != null) {
                    dataInfo.setDataProviderClass(dataProviderClass.getName());
                }
                
                // Analyze parameters for data provider patterns
                analyzeDataProviderParameters(result, dataInfo);
            }
        } catch (Exception e) {
            System.err.println("❌ Data provider info capture hatası: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes data provider parameters
     */
    private void analyzeDataProviderParameters(ITestResult result, TestDataInfo dataInfo) {
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            StringBuilder paramAnalysis = new StringBuilder();
            
            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                if (param != null) {
                    paramAnalysis.append(String.format("Param[%d]: %s (%s)\n", 
                        i, param.toString(), param.getClass().getSimpleName()));
                    
                    // Special handling for common test data types
                    analyzeParameterType(param, dataInfo);
                } else {
                    paramAnalysis.append(String.format("Param[%d]: null\n", i));
                }
            }
            
            dataInfo.setParameterAnalysis(paramAnalysis.toString());
        }
    }
    
    /**
     * Analyzes specific parameter types
     */
    private void analyzeParameterType(Object param, TestDataInfo dataInfo) {
        String paramType = param.getClass().getSimpleName();
        
        // Track parameter types
        dataInfo.addParameterType(paramType);
        
        // Special analysis for common types
        if (param instanceof String) {
            String strParam = (String) param;
            if (strParam.contains("@")) {
                dataInfo.addDataPattern("EMAIL");
            }
            if (strParam.matches("\\d+")) {
                dataInfo.addDataPattern("NUMERIC_STRING");
            }
            if (strParam.length() > 100) {
                dataInfo.addDataPattern("LONG_STRING");
            }
        } else if (param instanceof Number) {
            dataInfo.addDataPattern("NUMERIC");
        } else if (param instanceof Boolean) {
            dataInfo.addDataPattern("BOOLEAN");
        }
    }
    
    /**
     * Captures test configuration from test instance
     */
    private void captureTestConfiguration(Object testInstance, TestDataInfo dataInfo) {
        try {
            // Try to get common configuration fields
            String[] configFields = {"baseUrl", "apiUrl", "timeout", "browser", "environment"};
            
            for (String fieldName : configFields) {
                try {
                    java.lang.reflect.Field field = testInstance.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(testInstance);
                    if (value != null) {
                        dataInfo.addConfigurationValue(fieldName, value.toString());
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Field doesn't exist or not accessible, continue
                }
            }
            
            // Try parent class as well
            Class<?> parentClass = testInstance.getClass().getSuperclass();
            if (parentClass != null) {
                for (String fieldName : configFields) {
                    try {
                        java.lang.reflect.Field field = parentClass.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(testInstance);
                        if (value != null) {
                            dataInfo.addConfigurationValue(fieldName, value.toString());
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Field doesn't exist or not accessible, continue
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test configuration capture hatası: " + e.getMessage());
        }
    }
    
    /**
     * Captures additional data when test fails
     */
    private void captureFailureData(String testName, ITestResult result) {
        TestDataInfo dataInfo = testDataMap.get(testName);
        if (dataInfo != null) {
            // Capture failure context
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                dataInfo.setFailureType(throwable.getClass().getSimpleName());
                dataInfo.setFailureMessage(throwable.getMessage());
                dataInfo.setStackTrace(getStackTrace(throwable));
            }
            
            // Capture system state at failure
            captureSystemStateAtFailure(dataInfo);
            
            // Add failure data to Allure
            addFailureDataToAllure(testName, dataInfo);
        }
    }
    
    /**
     * Captures system state at failure
     */
    private void captureSystemStateAtFailure(TestDataInfo dataInfo) {
        try {
            Runtime runtime = Runtime.getRuntime();
            dataInfo.addSystemInfo("Memory Used", 
                String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024) + " MB");
            dataInfo.addSystemInfo("Memory Free", 
                String.valueOf(runtime.freeMemory() / 1024 / 1024) + " MB");
            dataInfo.addSystemInfo("Memory Total", 
                String.valueOf(runtime.totalMemory() / 1024 / 1024) + " MB");
            dataInfo.addSystemInfo("Available Processors", 
                String.valueOf(runtime.availableProcessors()));
            
        } catch (Exception e) {
            System.err.println("❌ System state capture hatası: " + e.getMessage());
        }
    }
    
    /**
     * Updates test data result
     */
    private void updateTestDataResult(String testName, String status, String message) {
        TestDataInfo dataInfo = testDataMap.get(testName);
        if (dataInfo != null) {
            dataInfo.setEndTime(LocalDateTime.now());
            dataInfo.setStatus(status);
            dataInfo.setResultMessage(message);
            
            // Calculate duration
            if (dataInfo.getStartTime() != null) {
                long duration = java.time.Duration.between(dataInfo.getStartTime(), dataInfo.getEndTime()).toMillis();
                dataInfo.setDuration(duration);
            }
            
            // Log final result
            logTestDataResult(testName, dataInfo);
        }
    }
    
    /**
     * Logs test data information
     */
    private void logTestDataInfo(String testName, TestDataInfo dataInfo) {
        System.out.println("📊 Test Data Info: " + testName);
        System.out.println("   🧵 Thread: " + dataInfo.getThreadName());
        System.out.println("   🏷️ Groups: " + String.join(", ", dataInfo.getGroups()));
        
        if (dataInfo.isUsesDataProvider()) {
            System.out.println("   📋 Data Provider: " + dataInfo.getDataProviderName());
            System.out.println("   📊 Parameters: " + dataInfo.getParameterCount());
        }
        
        if (!dataInfo.getDataPatterns().isEmpty()) {
            System.out.println("   🔍 Data Patterns: " + String.join(", ", dataInfo.getDataPatterns()));
        }
    }
    
    /**
     * Logs test data result
     */
    private void logTestDataResult(String testName, TestDataInfo dataInfo) {
        System.out.println("📊 Test Data Result: " + testName);
        System.out.println("   ✅ Status: " + dataInfo.getStatus());
        System.out.println("   ⏱️ Duration: " + formatDuration(dataInfo.getDuration()));
        
        if (dataInfo.getStatus().equals("FAILED") && dataInfo.getFailureType() != null) {
            System.out.println("   🚨 Failure Type: " + dataInfo.getFailureType());
        }
    }
    
    /**
     * Adds test data to Allure report
     */
    private void addTestDataToAllure(String testName, TestDataInfo dataInfo) {
        StringBuilder dataReport = new StringBuilder();
        dataReport.append("Test Data Information:\n");
        dataReport.append("=".repeat(40)).append("\n");
        dataReport.append("Test: ").append(testName).append("\n");
        dataReport.append("Thread: ").append(dataInfo.getThreadName()).append("\n");
        dataReport.append("Groups: ").append(String.join(", ", dataInfo.getGroups())).append("\n");
        dataReport.append("Description: ").append(dataInfo.getDescription()).append("\n");
        
        if (dataInfo.isUsesDataProvider()) {
            dataReport.append("Data Provider: ").append(dataInfo.getDataProviderName()).append("\n");
            dataReport.append("Parameter Count: ").append(dataInfo.getParameterCount()).append("\n");
            
            if (!dataInfo.getParameterAnalysis().isEmpty()) {
                dataReport.append("\nParameter Analysis:\n");
                dataReport.append(dataInfo.getParameterAnalysis());
            }
        }
        
        if (!dataInfo.getConfigurationValues().isEmpty()) {
            dataReport.append("\nConfiguration:\n");
            dataInfo.getConfigurationValues().forEach((key, value) -> 
                dataReport.append(key).append(": ").append(value).append("\n")
            );
        }
        
        Allure.addAttachment("Test Data Info - " + testName, dataReport.toString());
    }
    
    /**
     * Adds failure data to Allure report
     */
    private void addFailureDataToAllure(String testName, TestDataInfo dataInfo) {
        StringBuilder failureReport = new StringBuilder();
        failureReport.append("Test Failure Data:\n");
        failureReport.append("=".repeat(40)).append("\n");
        failureReport.append("Test: ").append(testName).append("\n");
        failureReport.append("Failure Type: ").append(dataInfo.getFailureType()).append("\n");
        failureReport.append("Failure Message: ").append(dataInfo.getFailureMessage()).append("\n");
        
        if (!dataInfo.getSystemInfo().isEmpty()) {
            failureReport.append("\nSystem State at Failure:\n");
            dataInfo.getSystemInfo().forEach((key, value) -> 
                failureReport.append(key).append(": ").append(value).append("\n")
            );
        }
        
        if (dataInfo.getStackTrace() != null) {
            failureReport.append("\nStack Trace:\n");
            failureReport.append(dataInfo.getStackTrace());
        }
        
        Allure.addAttachment("Failure Data - " + testName, failureReport.toString());
    }
    
    // Utility methods
    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable != null ? throwable.getMessage() : "Unknown error";
    }
    
    private String getSkipReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable != null ? throwable.getMessage() : "Test skipped";
    }
    
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    private String formatDuration(long milliseconds) {
        if (milliseconds == 0) return "0 ms";
        
        long seconds = milliseconds / 1000;
        long ms = milliseconds % 1000;
        
        if (seconds > 0) {
            return String.format("%d sn %d ms", seconds, ms);
        } else {
            return String.format("%d ms", ms);
        }
    }
    
    /**
     * Gets test data statistics
     */
    public static void printTestDataStatistics() {
        System.out.println("=".repeat(80));
        System.out.println("📊 TEST DATA İSTATİSTİKLERİ");
        System.out.println("=".repeat(80));
        
        if (testDataMap.isEmpty()) {
            System.out.println("📋 Hiçbir test data bilgisi bulunamadı");
            return;
        }
        
        long totalTests = testDataMap.size();
        long dataProviderTests = testDataMap.values().stream()
            .mapToLong(info -> info.isUsesDataProvider() ? 1 : 0)
            .sum();
        
        System.out.println("📈 Toplam Test: " + totalTests);
        System.out.println("📋 Data Provider Kullanan: " + dataProviderTests);
        System.out.println("📊 Data Provider Oranı: " + 
            String.format("%.1f%%", (double) dataProviderTests / totalTests * 100));
        
        System.out.println("=".repeat(80));
    }
}