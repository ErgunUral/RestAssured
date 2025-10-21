package com.example.listeners;

import io.qameta.allure.Allure;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced Parallel Test Execution Listener for PayTR Test Suite
 * Provides comprehensive monitoring and reporting for parallel test execution
 */
public class ParallelTestExecutionListener implements ITestListener, ISuiteListener, IInvokedMethodListener {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Thread-safe counters and maps
    private static final ConcurrentHashMap<String, Long> testStartTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> testThreads = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TestExecutionInfo> testExecutionInfo = new ConcurrentHashMap<>();
    private static final AtomicInteger activeTests = new AtomicInteger(0);
    private static final AtomicInteger maxConcurrentTests = new AtomicInteger(0);
    private static final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // Suite level tracking
    private long suiteStartTime;
    private String suiteName;
    private int configuredThreadCount;
    private XmlSuite.ParallelMode parallelMode;
    
    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = System.currentTimeMillis();
        suiteName = suite.getName();
        configuredThreadCount = suite.getXmlSuite().getThreadCount();
        parallelMode = suite.getXmlSuite().getParallel();
        
        System.out.println("=".repeat(80));
        System.out.println("🚀 PARALLEL TEST SUITE BAŞLADI: " + suiteName);
        System.out.println("📅 Başlangıç: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("🧵 Paralel Mod: " + (parallelMode != null ? parallelMode : "NONE"));
        System.out.println("🔢 Thread Sayısı: " + configuredThreadCount);
        System.out.println("🖥️ Mevcut CPU: " + Runtime.getRuntime().availableProcessors());
        System.out.println("💾 Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        System.out.println("=".repeat(80));
        
        // Add to Allure
        Allure.addAttachment("Parallel Suite Configuration", 
            "Suite: " + suiteName + "\n" +
            "Parallel Mode: " + (parallelMode != null ? parallelMode : "NONE") + "\n" +
            "Thread Count: " + configuredThreadCount + "\n" +
            "Available CPUs: " + Runtime.getRuntime().availableProcessors() + "\n" +
            "Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB\n" +
            "Start Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }
    
    @Override
    public void onFinish(ISuite suite) {
        long suiteEndTime = System.currentTimeMillis();
        long suiteDuration = suiteEndTime - suiteStartTime;
        
        // Generate comprehensive parallel execution report
        generateParallelExecutionReport(suiteDuration);
        
        System.out.println("=".repeat(80));
        System.out.println("🏁 PARALLEL TEST SUITE TAMAMLANDI: " + suiteName);
        System.out.println("📅 Bitiş: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("⏱️ Toplam Süre: " + formatDuration(suiteDuration));
        System.out.println("🧵 Max Eşzamanlı Test: " + maxConcurrentTests.get());
        System.out.println("📊 Paralel Verimlilik: " + calculateParallelEfficiency() + "%");
        System.out.println("=".repeat(80));
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        
        // Track test execution
        testStartTimes.put(testName, startTime);
        testThreads.put(testName, threadName);
        
        // Update concurrent test tracking
        int currentActive = activeTests.incrementAndGet();
        updateMaxConcurrentTests(currentActive);
        
        // Create execution info
        TestExecutionInfo execInfo = new TestExecutionInfo(testName, threadName, startTime);
        testExecutionInfo.put(testName, execInfo);
        
        System.out.println(String.format("🧪 [%s] Test Başladı: %s (Aktif: %d)", 
            threadName, testName, currentActive));
        
        // Monitor memory usage
        monitorResourceUsage(testName, "START");
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        handleTestCompletion(result, "PASSED");
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        handleTestCompletion(result, "FAILED");
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        handleTestCompletion(result, "SKIPPED");
    }
    
    private void handleTestCompletion(ITestResult result, String status) {
        String testName = getTestName(result);
        String threadName = Thread.currentThread().getName();
        long endTime = System.currentTimeMillis();
        
        // Calculate duration
        Long startTime = testStartTimes.get(testName);
        long duration = startTime != null ? endTime - startTime : 0;
        totalExecutionTime.addAndGet(duration);
        
        // Update active test count
        int currentActive = activeTests.decrementAndGet();
        
        // Update execution info
        TestExecutionInfo execInfo = testExecutionInfo.get(testName);
        if (execInfo != null) {
            execInfo.setEndTime(endTime);
            execInfo.setStatus(status);
            execInfo.setDuration(duration);
        }
        
        System.out.println(String.format("✅ [%s] Test %s: %s (%s) (Aktif: %d)", 
            threadName, status, testName, formatDuration(duration), currentActive));
        
        // Monitor memory usage
        monitorResourceUsage(testName, "END");
        
        // Add detailed execution info to Allure
        addExecutionInfoToAllure(testName, execInfo, status);
    }
    
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            String methodName = method.getTestMethod().getMethodName();
            String threadName = Thread.currentThread().getName();
            System.out.println(String.format("🔄 [%s] Method Başlıyor: %s", threadName, methodName));
        }
    }
    
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            String methodName = method.getTestMethod().getMethodName();
            String threadName = Thread.currentThread().getName();
            String status = getResultStatus(testResult.getStatus());
            System.out.println(String.format("✅ [%s] Method Tamamlandı: %s - %s", threadName, methodName, status));
        }
    }
    
    /**
     * Updates maximum concurrent tests counter
     */
    private void updateMaxConcurrentTests(int currentActive) {
        maxConcurrentTests.updateAndGet(current -> Math.max(current, currentActive));
    }
    
    /**
     * Monitors resource usage during test execution
     */
    private void monitorResourceUsage(String testName, String phase) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        String resourceInfo = String.format(
            "Memory Usage [%s]: Used=%d MB, Free=%d MB, Total=%d MB",
            phase,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            totalMemory / 1024 / 1024
        );
        
        // Log high memory usage
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
        if (memoryUsagePercent > 80) {
            System.out.println("⚠️ [" + Thread.currentThread().getName() + "] Yüksek Memory Kullanımı: " + 
                String.format("%.1f%%", memoryUsagePercent));
        }
        
        // Add to test execution info
        TestExecutionInfo execInfo = testExecutionInfo.get(testName);
        if (execInfo != null) {
            execInfo.addResourceInfo(phase, resourceInfo);
        }
    }
    
    /**
     * Calculates parallel execution efficiency
     */
    private double calculateParallelEfficiency() {
        if (configuredThreadCount <= 1 || testExecutionInfo.isEmpty()) {
            return 0.0;
        }
        
        // Calculate theoretical vs actual execution time
        long totalTestTime = testExecutionInfo.values().stream()
            .mapToLong(TestExecutionInfo::getDuration)
            .sum();
        
        long actualSuiteTime = System.currentTimeMillis() - suiteStartTime;
        
        if (actualSuiteTime == 0) return 0.0;
        
        double theoreticalTime = (double) totalTestTime / configuredThreadCount;
        double efficiency = (theoreticalTime / actualSuiteTime) * 100;
        
        return Math.min(100.0, Math.max(0.0, efficiency));
    }
    
    /**
     * Generates comprehensive parallel execution report
     */
    private void generateParallelExecutionReport(long suiteDuration) {
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("📊 PARALLEL EXECUTION RAPORU\n");
        report.append("=".repeat(80)).append("\n");
        report.append("Suite: ").append(suiteName).append("\n");
        report.append("Paralel Mod: ").append(parallelMode != null ? parallelMode : "NONE").append("\n");
        report.append("Konfigüre Thread: ").append(configuredThreadCount).append("\n");
        report.append("Max Eşzamanlı Test: ").append(maxConcurrentTests.get()).append("\n");
        report.append("Toplam Suite Süresi: ").append(formatDuration(suiteDuration)).append("\n");
        report.append("Toplam Test Süresi: ").append(formatDuration(totalExecutionTime.get())).append("\n");
        report.append("Paralel Verimlilik: ").append(String.format("%.2f%%", calculateParallelEfficiency())).append("\n");
        report.append("=".repeat(80)).append("\n");
        
        // Thread usage analysis
        report.append("🧵 THREAD KULLANIM ANALİZİ:\n");
        report.append("-".repeat(80)).append("\n");
        
        ConcurrentHashMap<String, AtomicInteger> threadUsage = new ConcurrentHashMap<>();
        testThreads.values().forEach(thread -> 
            threadUsage.computeIfAbsent(thread, k -> new AtomicInteger(0)).incrementAndGet()
        );
        
        threadUsage.forEach((thread, count) -> 
            report.append(String.format("%-30s: %d test\n", thread, count.get()))
        );
        
        // Performance insights
        report.append("\n📈 PERFORMANS İÇGÖRÜLERİ:\n");
        report.append("-".repeat(80)).append("\n");
        
        if (maxConcurrentTests.get() < configuredThreadCount) {
            report.append("⚠️ Thread kapasitesi tam kullanılmadı\n");
        }
        
        double efficiency = calculateParallelEfficiency();
        if (efficiency < 50) {
            report.append("⚠️ Düşük paralel verimlilik - Thread sayısını azaltmayı düşünün\n");
        } else if (efficiency > 90) {
            report.append("✅ Mükemmel paralel verimlilik\n");
        }
        
        // Add to Allure
        Allure.addAttachment("Parallel Execution Report", report.toString());
        
        System.out.println(report.toString());
    }
    
    /**
     * Adds detailed execution info to Allure
     */
    private void addExecutionInfoToAllure(String testName, TestExecutionInfo execInfo, String status) {
        if (execInfo != null) {
            StringBuilder info = new StringBuilder();
            info.append("Test Execution Details:\n");
            info.append("Test: ").append(testName).append("\n");
            info.append("Thread: ").append(execInfo.getThreadName()).append("\n");
            info.append("Status: ").append(status).append("\n");
            info.append("Duration: ").append(formatDuration(execInfo.getDuration())).append("\n");
            info.append("Start Time: ").append(LocalDateTime.ofEpochSecond(
                execInfo.getStartTime() / 1000, 0, java.time.ZoneOffset.UTC).format(TIMESTAMP_FORMAT)).append("\n");
            
            if (!execInfo.getResourceInfo().isEmpty()) {
                info.append("\nResource Usage:\n");
                execInfo.getResourceInfo().forEach((phase, resourceInfo) -> 
                    info.append(phase).append(": ").append(resourceInfo).append("\n")
                );
            }
            
            Allure.addAttachment("Parallel Execution Info - " + testName, info.toString());
        }
    }
    
    // Utility methods
    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    private String getResultStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASSED";
            case ITestResult.FAILURE: return "FAILED";
            case ITestResult.SKIP: return "SKIPPED";
            default: return "UNKNOWN";
        }
    }
    
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d dk %d sn", minutes, seconds);
        } else {
            return String.format("%d sn", seconds);
        }
    }
    
    /**
     * Inner class to track test execution information
     */
    private static class TestExecutionInfo {
        private final String testName;
        private final String threadName;
        private final long startTime;
        private long endTime;
        private long duration;
        private String status;
        private final ConcurrentHashMap<String, String> resourceInfo;
        
        public TestExecutionInfo(String testName, String threadName, long startTime) {
            this.testName = testName;
            this.threadName = threadName;
            this.startTime = startTime;
            this.resourceInfo = new ConcurrentHashMap<>();
        }
        
        // Getters and setters
        public String getTestName() { return testName; }
        public String getThreadName() { return threadName; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public ConcurrentHashMap<String, String> getResourceInfo() { return resourceInfo; }
        
        public void addResourceInfo(String phase, String info) {
            resourceInfo.put(phase, info);
        }
    }
}