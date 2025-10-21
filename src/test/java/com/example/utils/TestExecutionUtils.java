package com.example.utils;

import org.testng.ITestResult;
import org.testng.Reporter;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Test Execution Utilities for PayTR Test Suite
 * Provides comprehensive test execution monitoring, reporting, and resource management
 */
public class TestExecutionUtils {
    
    private static final Map<String, TestExecutionMetrics> testMetrics = new ConcurrentHashMap<>();
    private static final AtomicInteger totalTestsExecuted = new AtomicInteger(0);
    private static final AtomicInteger totalTestsPassed = new AtomicInteger(0);
    private static final AtomicInteger totalTestsFailed = new AtomicInteger(0);
    private static final AtomicInteger totalTestsSkipped = new AtomicInteger(0);
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    /**
     * Test Execution Metrics Container
     */
    public static class TestExecutionMetrics {
        private final String testName;
        private final String className;
        private final String methodName;
        private final long startTime;
        private long endTime;
        private long duration;
        private String status;
        private String threadName;
        private long memoryUsed;
        private int activeThreads;
        private String errorMessage;
        private List<String> steps;
        
        public TestExecutionMetrics(String testName, String className, String methodName) {
            this.testName = testName;
            this.className = className;
            this.methodName = methodName;
            this.startTime = System.currentTimeMillis();
            this.threadName = Thread.currentThread().getName();
            this.steps = new ArrayList<>();
            this.memoryUsed = getMemoryUsage();
            this.activeThreads = threadBean.getThreadCount();
        }
        
        // Getters and setters
        public String getTestName() { return testName; }
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public long getDuration() { return duration; }
        public String getStatus() { return status; }
        public String getThreadName() { return threadName; }
        public long getMemoryUsed() { return memoryUsed; }
        public int getActiveThreads() { return activeThreads; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getSteps() { return steps; }
        
        public void setEndTime(long endTime) {
            this.endTime = endTime;
            this.duration = endTime - startTime;
        }
        
        public void setStatus(String status) { this.status = status; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public void addStep(String step) { this.steps.add(step); }
        
        public String getFormattedDuration() {
            if (duration < 1000) {
                return duration + "ms";
            } else {
                return String.format("%.2fs", duration / 1000.0);
            }
        }
    }
    
    /**
     * Start tracking test execution
     */
    public static void startTestExecution(ITestResult result) {
        String testKey = getTestKey(result);
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        
        TestExecutionMetrics metrics = new TestExecutionMetrics(testKey, className, methodName);
        testMetrics.put(testKey, metrics);
        
        totalTestsExecuted.incrementAndGet();
        
        // Add Allure step
        Allure.step("Test Execution Started", () -> {
            Allure.addAttachment("Test Details", 
                String.format("Class: %s\nMethod: %s\nThread: %s\nStart Time: %s", 
                    className, methodName, Thread.currentThread().getName(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        });
        
        Reporter.log(String.format("🚀 Test started: %s [Thread: %s]", testKey, Thread.currentThread().getName()));
    }
    
    /**
     * End tracking test execution
     */
    public static void endTestExecution(ITestResult result) {
        String testKey = getTestKey(result);
        TestExecutionMetrics metrics = testMetrics.get(testKey);
        
        if (metrics != null) {
            metrics.setEndTime(System.currentTimeMillis());
            
            switch (result.getStatus()) {
                case ITestResult.SUCCESS:
                    metrics.setStatus("PASSED");
                    totalTestsPassed.incrementAndGet();
                    break;
                case ITestResult.FAILURE:
                    metrics.setStatus("FAILED");
                    totalTestsFailed.incrementAndGet();
                    if (result.getThrowable() != null) {
                        metrics.setErrorMessage(result.getThrowable().getMessage());
                    }
                    break;
                case ITestResult.SKIP:
                    metrics.setStatus("SKIPPED");
                    totalTestsSkipped.incrementAndGet();
                    break;
            }
            
            // Add Allure step
            Allure.step("Test Execution Completed", () -> {
                Allure.addAttachment("Execution Summary", 
                    String.format("Status: %s\nDuration: %s\nMemory Used: %d MB\nActive Threads: %d", 
                        metrics.getStatus(), metrics.getFormattedDuration(),
                        metrics.getMemoryUsed() / (1024 * 1024), metrics.getActiveThreads()));
            });
            
            Reporter.log(String.format("✅ Test completed: %s - %s (%s)", 
                testKey, metrics.getStatus(), metrics.getFormattedDuration()));
        }
    }
    
    /**
     * Add execution step
     */
    public static void addExecutionStep(ITestResult result, String stepDescription) {
        String testKey = getTestKey(result);
        TestExecutionMetrics metrics = testMetrics.get(testKey);
        
        if (metrics != null) {
            metrics.addStep(stepDescription);
            
            Allure.step(stepDescription, () -> {
                Reporter.log(String.format("📝 Step: %s", stepDescription));
            });
        }
    }
    
    /**
     * Get current memory usage
     */
    private static long getMemoryUsage() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }
    
    /**
     * Get test key for tracking
     */
    private static String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    /**
     * Generate execution summary report
     */
    public static String generateExecutionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("\n" + "=".repeat(80) + "\n");
        summary.append("📊 PAYTR TEST EXECUTION SUMMARY\n");
        summary.append("=".repeat(80) + "\n");
        
        summary.append(String.format("Total Tests Executed: %d\n", totalTestsExecuted.get()));
        summary.append(String.format("✅ Passed: %d\n", totalTestsPassed.get()));
        summary.append(String.format("❌ Failed: %d\n", totalTestsFailed.get()));
        summary.append(String.format("⏭️ Skipped: %d\n", totalTestsSkipped.get()));
        
        double successRate = totalTestsExecuted.get() > 0 ? 
            (double) totalTestsPassed.get() / totalTestsExecuted.get() * 100 : 0;
        summary.append(String.format("📈 Success Rate: %.2f%%\n", successRate));
        
        summary.append("\n📋 DETAILED TEST RESULTS:\n");
        summary.append("-".repeat(80) + "\n");
        
        testMetrics.values().stream()
            .sorted((a, b) -> Long.compare(b.getDuration(), a.getDuration()))
            .forEach(metrics -> {
                summary.append(String.format("%-50s | %-8s | %10s | %s\n",
                    metrics.getTestName(),
                    metrics.getStatus(),
                    metrics.getFormattedDuration(),
                    metrics.getThreadName()));
            });
        
        summary.append("-".repeat(80) + "\n");
        
        // Performance insights
        summary.append("\n⚡ PERFORMANCE INSIGHTS:\n");
        OptionalDouble avgDuration = testMetrics.values().stream()
            .mapToLong(TestExecutionMetrics::getDuration)
            .average();
        
        if (avgDuration.isPresent()) {
            summary.append(String.format("Average Test Duration: %.2fs\n", avgDuration.getAsDouble() / 1000.0));
        }
        
        testMetrics.values().stream()
            .max((a, b) -> Long.compare(a.getDuration(), b.getDuration()))
            .ifPresent(slowest -> 
                summary.append(String.format("Slowest Test: %s (%s)\n", 
                    slowest.getTestName(), slowest.getFormattedDuration())));
        
        testMetrics.values().stream()
            .min((a, b) -> Long.compare(a.getDuration(), b.getDuration()))
            .ifPresent(fastest -> 
                summary.append(String.format("Fastest Test: %s (%s)\n", 
                    fastest.getTestName(), fastest.getFormattedDuration())));
        
        summary.append("=".repeat(80) + "\n");
        
        return summary.toString();
    }
    
    /**
     * Save execution report to file
     */
    public static void saveExecutionReport() {
        try {
            String reportContent = generateExecutionSummary();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("paytr_test_execution_report_%s.txt", timestamp);
            String reportPath = "target/test-reports/" + fileName;
            
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get("target/test-reports"));
            
            Files.write(Paths.get(reportPath), reportContent.getBytes());
            
            // Add to Allure report
            Allure.addAttachment("Execution Summary Report", "text/plain", 
                new ByteArrayInputStream(reportContent.getBytes()), ".txt");
            
            Reporter.log("📄 Execution report saved: " + reportPath);
            
        } catch (IOException e) {
            Reporter.log("❌ Failed to save execution report: " + e.getMessage());
        }
    }
    
    /**
     * Get test metrics for specific test
     */
    public static TestExecutionMetrics getTestMetrics(String testKey) {
        return testMetrics.get(testKey);
    }
    
    /**
     * Get all test metrics
     */
    public static Map<String, TestExecutionMetrics> getAllTestMetrics() {
        return new HashMap<>(testMetrics);
    }
    
    /**
     * Clear all metrics (useful for test cleanup)
     */
    public static void clearMetrics() {
        testMetrics.clear();
        totalTestsExecuted.set(0);
        totalTestsPassed.set(0);
        totalTestsFailed.set(0);
        totalTestsSkipped.set(0);
    }
    
    /**
     * Get execution statistics
     */
    public static Map<String, Object> getExecutionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", totalTestsExecuted.get());
        stats.put("passedTests", totalTestsPassed.get());
        stats.put("failedTests", totalTestsFailed.get());
        stats.put("skippedTests", totalTestsSkipped.get());
        stats.put("successRate", totalTestsExecuted.get() > 0 ? 
            (double) totalTestsPassed.get() / totalTestsExecuted.get() * 100 : 0);
        stats.put("totalDuration", testMetrics.values().stream()
            .mapToLong(TestExecutionMetrics::getDuration).sum());
        stats.put("averageDuration", testMetrics.values().stream()
            .mapToLong(TestExecutionMetrics::getDuration).average().orElse(0));
        
        return stats;
    }
    
    /**
     * Check if parallel execution is enabled
     */
    public static boolean isParallelExecution() {
        return threadBean.getThreadCount() > 1;
    }
    
    /**
     * Get current system resource usage
     */
    public static Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> resources = new HashMap<>();
        resources.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
        resources.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
        resources.put("nonHeapMemoryUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        resources.put("activeThreads", threadBean.getThreadCount());
        resources.put("peakThreads", threadBean.getPeakThreadCount());
        resources.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        return resources;
    }
}