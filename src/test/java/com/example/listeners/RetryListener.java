package com.example.listeners;

import io.qameta.allure.Allure;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced Retry Listener for PayTR Test Suite
 * Provides intelligent test retry mechanism with detailed logging and reporting
 */
public class RetryListener implements IAnnotationTransformer {
    
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        // Set retry analyzer for all test methods
        annotation.setRetryAnalyzer(PayTRRetryAnalyzer.class);
    }
    
    /**
     * Custom Retry Analyzer for PayTR Tests
     */
    public static class PayTRRetryAnalyzer implements IRetryAnalyzer {
        
        private static final int MAX_RETRY_COUNT = 2;
        private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Thread-safe counters for retry tracking
        private static final ConcurrentHashMap<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<String, RetryInfo> retryInfoMap = new ConcurrentHashMap<>();
        
        @Override
        public boolean retry(ITestResult result) {
            String testName = getTestName(result);
            AtomicInteger retryCounter = retryCounters.computeIfAbsent(testName, k -> new AtomicInteger(0));
            
            int currentRetryCount = retryCounter.get();
            
            // Check if we should retry
            if (currentRetryCount < MAX_RETRY_COUNT && shouldRetry(result)) {
                retryCounter.incrementAndGet();
                
                // Log retry attempt
                logRetryAttempt(result, currentRetryCount + 1);
                
                // Update retry info
                updateRetryInfo(testName, currentRetryCount + 1, result);
                
                // Add delay before retry (optional)
                addRetryDelay(currentRetryCount + 1);
                
                return true;
            } else {
                // Log final failure
                logFinalFailure(result, currentRetryCount);
                
                // Generate retry report
                generateRetryReport(testName, currentRetryCount);
                
                return false;
            }
        }
        
        /**
         * Determines if a test should be retried based on failure type
         */
        private boolean shouldRetry(ITestResult result) {
            Throwable throwable = result.getThrowable();
            
            if (throwable == null) {
                return false;
            }
            
            String errorMessage = throwable.getMessage();
            String errorClass = throwable.getClass().getSimpleName();
            
            // Don't retry assertion failures (these are likely real test failures)
            if (errorClass.contains("AssertionError") || errorClass.contains("AssertionFailedError")) {
                System.out.println("🚫 Assertion hatası - Retry yapılmayacak: " + errorMessage);
                return false;
            }
            
            // Don't retry data-related failures
            if (errorMessage != null && (
                errorMessage.contains("Invalid test data") ||
                errorMessage.contains("Configuration error") ||
                errorMessage.contains("Setup failed")
            )) {
                System.out.println("🚫 Konfigürasyon/Data hatası - Retry yapılmayacak: " + errorMessage);
                return false;
            }
            
            // Retry for infrastructure/network related issues
            if (errorMessage != null && (
                errorMessage.contains("timeout") ||
                errorMessage.contains("connection") ||
                errorMessage.contains("network") ||
                errorMessage.contains("WebDriverException") ||
                errorMessage.contains("NoSuchElementException") ||
                errorMessage.contains("StaleElementReferenceException") ||
                errorMessage.contains("ElementNotInteractableException")
            )) {
                System.out.println("🔄 Infrastructure/Network hatası - Retry yapılacak: " + errorMessage);
                return true;
            }
            
            // Retry for selenium-related issues
            if (errorClass.contains("WebDriverException") ||
                errorClass.contains("TimeoutException") ||
                errorClass.contains("NoSuchElementException") ||
                errorClass.contains("StaleElementReferenceException")) {
                System.out.println("🔄 Selenium hatası - Retry yapılacak: " + errorClass);
                return true;
            }
            
            // Default: retry for unknown errors
            System.out.println("🔄 Bilinmeyen hata - Retry yapılacak: " + errorClass + " - " + errorMessage);
            return true;
        }
        
        /**
         * Logs retry attempt with detailed information
         */
        private void logRetryAttempt(ITestResult result, int retryCount) {
            String testName = getTestName(result);
            String errorMessage = getErrorMessage(result);
            
            System.out.println("=".repeat(60));
            System.out.println("🔄 TEST RETRY - Deneme #" + retryCount);
            System.out.println("📝 Test: " + testName);
            System.out.println("⏰ Zaman: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
            System.out.println("🚨 Hata: " + errorMessage);
            System.out.println("🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("=".repeat(60));
            
            // Add to Allure report
            Allure.addAttachment("Retry Attempt #" + retryCount, 
                "Test: " + testName + "\n" +
                "Retry Count: " + retryCount + "\n" +
                "Error: " + errorMessage + "\n" +
                "Thread: " + Thread.currentThread().getName() + "\n" +
                "Timestamp: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
            );
        }
        
        /**
         * Logs final failure after all retries exhausted
         */
        private void logFinalFailure(ITestResult result, int totalRetries) {
            String testName = getTestName(result);
            String errorMessage = getErrorMessage(result);
            
            System.out.println("=".repeat(60));
            System.out.println("❌ TEST FINAL FAILURE");
            System.out.println("📝 Test: " + testName);
            System.out.println("🔢 Toplam Retry: " + totalRetries);
            System.out.println("⏰ Zaman: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
            System.out.println("🚨 Son Hata: " + errorMessage);
            System.out.println("=".repeat(60));
            
            // Add to Allure report
            Allure.addAttachment("Final Failure", 
                "Test: " + testName + "\n" +
                "Total Retries: " + totalRetries + "\n" +
                "Final Error: " + errorMessage + "\n" +
                "Timestamp: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
            );
        }
        
        /**
         * Updates retry information for reporting
         */
        private void updateRetryInfo(String testName, int retryCount, ITestResult result) {
            RetryInfo retryInfo = retryInfoMap.computeIfAbsent(testName, k -> new RetryInfo(testName));
            retryInfo.addRetryAttempt(retryCount, getErrorMessage(result));
        }
        
        /**
         * Adds delay before retry to allow system recovery
         */
        private void addRetryDelay(int retryCount) {
            try {
                // Progressive delay: 1s, 2s, 3s...
                int delaySeconds = retryCount;
                System.out.println("⏳ Retry öncesi bekleme: " + delaySeconds + " saniye");
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Retry delay interrupted: " + e.getMessage());
            }
        }
        
        /**
         * Generates comprehensive retry report
         */
        private void generateRetryReport(String testName, int totalRetries) {
            RetryInfo retryInfo = retryInfoMap.get(testName);
            if (retryInfo != null) {
                StringBuilder report = new StringBuilder();
                report.append("📊 RETRY RAPORU\n");
                report.append("=".repeat(40)).append("\n");
                report.append("Test: ").append(testName).append("\n");
                report.append("Toplam Retry: ").append(totalRetries).append("\n");
                report.append("Max Retry Limit: ").append(MAX_RETRY_COUNT).append("\n");
                report.append("=".repeat(40)).append("\n");
                
                for (int i = 1; i <= totalRetries; i++) {
                    String error = retryInfo.getRetryError(i);
                    report.append("Retry #").append(i).append(": ").append(error).append("\n");
                }
                
                Allure.addAttachment("Retry Report - " + testName, report.toString());
                System.out.println(report.toString());
            }
        }
        
        /**
         * Gets formatted test name
         */
        private String getTestName(ITestResult result) {
            return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        }
        
        /**
         * Gets error message from test result
         */
        private String getErrorMessage(ITestResult result) {
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                return throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
            }
            return "Unknown error";
        }
        
        /**
         * Gets retry statistics for reporting
         */
        public static void printRetryStatistics() {
            System.out.println("=".repeat(80));
            System.out.println("📊 RETRY İSTATİSTİKLERİ");
            System.out.println("=".repeat(80));
            
            if (retryCounters.isEmpty()) {
                System.out.println("✅ Hiçbir test retry gerektirmedi");
            } else {
                retryCounters.forEach((testName, counter) -> {
                    System.out.println(String.format("🔄 %-60s Retry: %d", testName, counter.get()));
                });
                
                int totalRetries = retryCounters.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
                System.out.println("-".repeat(80));
                System.out.println("📈 Toplam Retry Sayısı: " + totalRetries);
                System.out.println("📊 Retry Yapılan Test Sayısı: " + retryCounters.size());
            }
            System.out.println("=".repeat(80));
        }
        
        /**
         * Clears retry statistics (useful for test cleanup)
         */
        public static void clearRetryStatistics() {
            retryCounters.clear();
            retryInfoMap.clear();
        }
    }
    
    /**
     * Inner class to track retry information
     */
    private static class RetryInfo {
        private final String testName;
        private final ConcurrentHashMap<Integer, String> retryErrors;
        
        public RetryInfo(String testName) {
            this.testName = testName;
            this.retryErrors = new ConcurrentHashMap<>();
        }
        
        public void addRetryAttempt(int retryCount, String error) {
            retryErrors.put(retryCount, error);
        }
        
        public String getRetryError(int retryCount) {
            return retryErrors.getOrDefault(retryCount, "Unknown error");
        }
    }
}