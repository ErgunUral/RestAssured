package com.example.listeners;

import io.qameta.allure.Allure;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Test Execution Listener for PayTR Test Suite
 * Provides comprehensive test execution monitoring and reporting
 */
public class TestExecutionListener implements ITestListener, ISuiteListener, IInvokedMethodListener {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ConcurrentHashMap<String, Long> testStartTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TestResult> testResults = new ConcurrentHashMap<>();
    private static final AtomicInteger totalTests = new AtomicInteger(0);
    private static final AtomicInteger passedTests = new AtomicInteger(0);
    private static final AtomicInteger failedTests = new AtomicInteger(0);
    private static final AtomicInteger skippedTests = new AtomicInteger(0);
    
    private long suiteStartTime;
    private String suiteName;
    
    // Suite Level Events
    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = System.currentTimeMillis();
        suiteName = suite.getName();
        
        System.out.println("=".repeat(80));
        System.out.println("🚀 PAYTR TEST SUITE BAŞLADI: " + suiteName);
        System.out.println("📅 Başlangıç Zamanı: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("🔧 Paralel Execution: " + getParallelMode(suite.getXmlSuite()));
        System.out.println("🧵 Thread Count: " + suite.getXmlSuite().getThreadCount());
        System.out.println("=".repeat(80));
        
        // Allure Environment Information
        Allure.addAttachment("Suite Information", 
            "Suite Name: " + suiteName + "\n" +
            "Start Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + "\n" +
            "Parallel Mode: " + getParallelMode(suite.getXmlSuite()) + "\n" +
            "Thread Count: " + suite.getXmlSuite().getThreadCount()
        );
    }
    
    @Override
    public void onFinish(ISuite suite) {
        long suiteEndTime = System.currentTimeMillis();
        long suiteDuration = suiteEndTime - suiteStartTime;
        
        System.out.println("=".repeat(80));
        System.out.println("🏁 PAYTR TEST SUITE TAMAMLANDI: " + suiteName);
        System.out.println("📅 Bitiş Zamanı: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("⏱️ Toplam Süre: " + formatDuration(suiteDuration));
        System.out.println("📊 Test Sonuçları:");
        System.out.println("   ✅ Başarılı: " + passedTests.get());
        System.out.println("   ❌ Başarısız: " + failedTests.get());
        System.out.println("   ⏭️ Atlanan: " + skippedTests.get());
        System.out.println("   📈 Toplam: " + totalTests.get());
        System.out.println("   🎯 Başarı Oranı: " + calculateSuccessRate() + "%");
        System.out.println("=".repeat(80));
        
        // Generate comprehensive test report
        generateTestReport(suiteDuration);
    }
    
    // Test Level Events
    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        testStartTimes.put(testName, System.currentTimeMillis());
        totalTests.incrementAndGet();
        
        System.out.println("🧪 Test Başladı: " + testName);
        System.out.println("   📝 Açıklama: " + getTestDescription(result));
        System.out.println("   🏷️ Gruplar: " + String.join(", ", result.getMethod().getGroups()));
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        
        // Allure Test Information
        Allure.addAttachment("Test Start Info", 
            "Test: " + testName + "\n" +
            "Description: " + getTestDescription(result) + "\n" +
            "Groups: " + String.join(", ", result.getMethod().getGroups()) + "\n" +
            "Thread: " + Thread.currentThread().getName() + "\n" +
            "Start Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateTestDuration(testName);
        passedTests.incrementAndGet();
        
        TestResult testResult = new TestResult(testName, "PASSED", duration, null);
        testResults.put(testName, testResult);
        
        System.out.println("✅ Test Başarılı: " + testName + " (" + formatDuration(duration) + ")");
        
        // Allure Success Information
        Allure.addAttachment("Test Success", 
            "Test: " + testName + "\n" +
            "Status: PASSED\n" +
            "Duration: " + formatDuration(duration) + "\n" +
            "End Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateTestDuration(testName);
        failedTests.incrementAndGet();
        
        String errorMessage = getErrorMessage(result);
        TestResult testResult = new TestResult(testName, "FAILED", duration, errorMessage);
        testResults.put(testName, testResult);
        
        System.out.println("❌ Test Başarısız: " + testName + " (" + formatDuration(duration) + ")");
        System.out.println("   🚨 Hata: " + errorMessage);
        
        // Allure Failure Information
        Allure.addAttachment("Test Failure", 
            "Test: " + testName + "\n" +
            "Status: FAILED\n" +
            "Duration: " + formatDuration(duration) + "\n" +
            "Error: " + errorMessage + "\n" +
            "End Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateTestDuration(testName);
        skippedTests.incrementAndGet();
        
        String skipReason = getSkipReason(result);
        TestResult testResult = new TestResult(testName, "SKIPPED", duration, skipReason);
        testResults.put(testName, testResult);
        
        System.out.println("⏭️ Test Atlandı: " + testName);
        System.out.println("   📝 Sebep: " + skipReason);
        
        // Allure Skip Information
        Allure.addAttachment("Test Skipped", 
            "Test: " + testName + "\n" +
            "Status: SKIPPED\n" +
            "Reason: " + skipReason + "\n" +
            "End Time: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }
    
    // Method Level Events
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            String methodName = method.getTestMethod().getMethodName();
            System.out.println("🔄 Method Çalıştırılıyor: " + methodName);
        }
    }
    
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            String methodName = method.getTestMethod().getMethodName();
            String status = getResultStatus(testResult.getStatus());
            System.out.println("✅ Method Tamamlandı: " + methodName + " - " + status);
        }
    }
    
    // Utility Methods
    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    private String getTestDescription(ITestResult result) {
        String description = result.getMethod().getDescription();
        return description != null ? description : "No description available";
    }
    
    private long calculateTestDuration(String testName) {
        Long startTime = testStartTimes.get(testName);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            return throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
        }
        return "Unknown error";
    }
    
    private String getSkipReason(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            return throwable.getMessage() != null ? throwable.getMessage() : "Test skipped";
        }
        return "Test skipped - no reason provided";
    }
    
    private String getResultStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASSED";
            case ITestResult.FAILURE: return "FAILED";
            case ITestResult.SKIP: return "SKIPPED";
            default: return "UNKNOWN";
        }
    }
    
    private String getParallelMode(XmlSuite xmlSuite) {
        XmlSuite.ParallelMode parallelMode = xmlSuite.getParallel();
        return parallelMode != null ? parallelMode.toString() : "NONE";
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
    
    private double calculateSuccessRate() {
        int total = totalTests.get();
        if (total == 0) return 0.0;
        return Math.round((double) passedTests.get() / total * 100 * 100.0) / 100.0;
    }
    
    private void generateTestReport(long suiteDuration) {
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("📊 PAYTR TEST SUITE RAPORU\n");
        report.append("=".repeat(80)).append("\n");
        report.append("Suite: ").append(suiteName).append("\n");
        report.append("Toplam Süre: ").append(formatDuration(suiteDuration)).append("\n");
        report.append("Toplam Test: ").append(totalTests.get()).append("\n");
        report.append("Başarılı: ").append(passedTests.get()).append("\n");
        report.append("Başarısız: ").append(failedTests.get()).append("\n");
        report.append("Atlanan: ").append(skippedTests.get()).append("\n");
        report.append("Başarı Oranı: ").append(calculateSuccessRate()).append("%\n");
        report.append("=".repeat(80)).append("\n");
        
        // Add detailed test results
        report.append("📋 DETAYLI TEST SONUÇLARI:\n");
        report.append("-".repeat(80)).append("\n");
        
        testResults.values().forEach(result -> {
            report.append(String.format("%-60s %s (%s)\n", 
                result.testName, 
                result.status, 
                formatDuration(result.duration)
            ));
            if (result.errorMessage != null && !result.status.equals("PASSED")) {
                report.append("   Error: ").append(result.errorMessage).append("\n");
            }
        });
        
        // Allure Final Report
        Allure.addAttachment("Final Test Report", report.toString());
        
        System.out.println(report.toString());
    }
    
    // Inner class for test results
    private static class TestResult {
        final String testName;
        final String status;
        final long duration;
        final String errorMessage;
        
        TestResult(String testName, String status, long duration, String errorMessage) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
    }
}