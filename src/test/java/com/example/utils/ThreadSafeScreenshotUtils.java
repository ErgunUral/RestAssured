package com.example.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-Safe Screenshot Utilities for Parallel Test Execution
 * Provides comprehensive screenshot capture and management for PayTR test suite
 */
public class ThreadSafeScreenshotUtils {
    
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final String ALLURE_SCREENSHOTS_DIR = "target/allure-results";
    private static final AtomicInteger screenshotCounter = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, String> threadScreenshotPaths = new ConcurrentHashMap<>();
    
    // Thread-local WebDriver storage for parallel execution
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    /**
     * Set WebDriver for current thread
     */
    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }
    
    /**
     * Get WebDriver for current thread
     */
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }
    
    /**
     * Remove WebDriver for current thread
     */
    public static void removeDriver() {
        driverThreadLocal.remove();
    }
    
    /**
     * Take screenshot with automatic naming and thread safety
     */
    public static String takeScreenshot(String testName) {
        WebDriver driver = getDriver();
        if (driver == null) {
            Reporter.log("❌ No WebDriver found for current thread: " + Thread.currentThread().getName());
            return null;
        }
        
        try {
            // Create screenshot directory if it doesn't exist
            createScreenshotDirectories();
            
            // Generate unique screenshot name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String threadName = Thread.currentThread().getName().replaceAll("[^a-zA-Z0-9]", "_");
            int screenshotNum = screenshotCounter.incrementAndGet();
            
            String fileName = String.format("%s_%s_%s_%d.png", 
                testName.replaceAll("[^a-zA-Z0-9]", "_"), 
                threadName, 
                timestamp, 
                screenshotNum);
            
            // Take screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            
            // Save to file system
            Path screenshotPath = Paths.get(SCREENSHOT_DIR, fileName);
            Files.write(screenshotPath, screenshotBytes);
            
            // Store path for thread
            threadScreenshotPaths.put(Thread.currentThread().getName(), screenshotPath.toString());
            
            // Add to Allure report
            Allure.addAttachment("Screenshot - " + testName, "image/png", 
                new ByteArrayInputStream(screenshotBytes), ".png");
            
            Reporter.log(String.format("📸 Screenshot captured: %s [Thread: %s]", 
                fileName, Thread.currentThread().getName()));
            
            return screenshotPath.toString();
            
        } catch (Exception e) {
            Reporter.log(String.format("❌ Failed to take screenshot for test '%s': %s", 
                testName, e.getMessage()));
            return null;
        }
    }
    
    /**
     * Take screenshot on test failure
     */
    public static String takeFailureScreenshot(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        String fullTestName = className + "_" + testName + "_FAILURE";
        
        String screenshotPath = takeScreenshot(fullTestName);
        
        if (screenshotPath != null) {
            // Add failure context to Allure
            try {
                byte[] screenshotBytes = Files.readAllBytes(Paths.get(screenshotPath));
                Allure.addAttachment("Failure Screenshot", "image/png", 
                    new ByteArrayInputStream(screenshotBytes), ".png");
            } catch (IOException e) {
                Reporter.log("❌ Failed to attach failure screenshot: " + e.getMessage());
            }
            
            // Add failure details
            if (result.getThrowable() != null) {
                Allure.addAttachment("Failure Details", 
                    String.format("Test: %s\nError: %s\nThread: %s\nTimestamp: %s", 
                        fullTestName,
                        result.getThrowable().getMessage(),
                        Thread.currentThread().getName(),
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            }
        }
        
        return screenshotPath;
    }
    
    /**
     * Take screenshot with custom message
     */
    public static String takeScreenshotWithMessage(String testName, String message) {
        String screenshotPath = takeScreenshot(testName);
        
        if (screenshotPath != null) {
            Allure.addAttachment("Screenshot Context", message);
            Reporter.log(String.format("📸 Screenshot with message: %s - %s", testName, message));
        }
        
        return screenshotPath;
    }
    
    /**
     * Take screenshot for specific step
     */
    public static String takeStepScreenshot(String testName, String stepName) {
        String fullName = testName + "_" + stepName;
        String screenshotPath = takeScreenshot(fullName);
        
        if (screenshotPath != null) {
            Allure.step("Screenshot: " + stepName, () -> {
                try {
                    byte[] screenshotBytes = Files.readAllBytes(Paths.get(screenshotPath));
                    Allure.addAttachment("Step Screenshot", "image/png", 
                        new ByteArrayInputStream(screenshotBytes), ".png");
                } catch (IOException e) {
                    Reporter.log("❌ Failed to attach step screenshot: " + e.getMessage());
                }
            });
        }
        
        return screenshotPath;
    }
    
    /**
     * Take comparison screenshots (before/after)
     */
    public static void takeComparisonScreenshots(String testName, String action) {
        takeScreenshotWithMessage(testName + "_BEFORE", "Before " + action);
        
        // This method should be called again after the action with "AFTER"
        // Example usage: takeComparisonScreenshots(testName + "_AFTER", "After " + action);
    }
    
    /**
     * Get screenshot path for current thread
     */
    public static String getThreadScreenshotPath() {
        return threadScreenshotPaths.get(Thread.currentThread().getName());
    }
    
    /**
     * Create necessary directories
     */
    private static void createScreenshotDirectories() {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            Files.createDirectories(Paths.get(ALLURE_SCREENSHOTS_DIR));
        } catch (IOException e) {
            Reporter.log("❌ Failed to create screenshot directories: " + e.getMessage());
        }
    }
    
    /**
     * Clean up old screenshots (keep only recent ones)
     */
    public static void cleanupOldScreenshots(int daysToKeep) {
        try {
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                return;
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
            
            Files.walk(screenshotDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".png"))
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        Reporter.log("🗑️ Deleted old screenshot: " + path.getFileName());
                    } catch (IOException e) {
                        Reporter.log("❌ Failed to delete screenshot: " + path.getFileName());
                    }
                });
                
        } catch (IOException e) {
            Reporter.log("❌ Failed to cleanup old screenshots: " + e.getMessage());
        }
    }
    
    /**
     * Get screenshot statistics
     */
    public static String getScreenshotStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("\n📸 SCREENSHOT STATISTICS:\n");
        stats.append("-".repeat(50) + "\n");
        stats.append(String.format("Total Screenshots Taken: %d\n", screenshotCounter.get()));
        stats.append(String.format("Active Threads with Screenshots: %d\n", threadScreenshotPaths.size()));
        stats.append(String.format("Screenshot Directory: %s\n", SCREENSHOT_DIR));
        
        try {
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (Files.exists(screenshotDir)) {
                long totalSize = Files.walk(screenshotDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".png"))
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
                
                stats.append(String.format("Total Screenshot Size: %.2f MB\n", totalSize / (1024.0 * 1024.0)));
            }
        } catch (IOException e) {
            stats.append("Failed to calculate screenshot size\n");
        }
        
        stats.append("-".repeat(50) + "\n");
        return stats.toString();
    }
    
    /**
     * Allure attachment method for screenshots
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] attachScreenshot() {
        WebDriver driver = getDriver();
        if (driver != null && driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }
    
    /**
     * Allure attachment method for failure screenshots
     */
    @Attachment(value = "Failure Screenshot", type = "image/png")
    public static byte[] attachFailureScreenshot() {
        return attachScreenshot();
    }
    
    /**
     * Clear thread-specific data (call in test cleanup)
     */
    public static void clearThreadData() {
        threadScreenshotPaths.remove(Thread.currentThread().getName());
        removeDriver();
    }
    
    /**
     * Clear all screenshot data
     */
    public static void clearAllData() {
        threadScreenshotPaths.clear();
        screenshotCounter.set(0);
    }
    
    /**
     * Check if screenshot directory exists and is writable
     */
    public static boolean isScreenshotDirectoryReady() {
        try {
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }
            return Files.isWritable(screenshotDir);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get thread-safe screenshot file name
     */
    public static String generateThreadSafeFileName(String baseName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String threadName = Thread.currentThread().getName().replaceAll("[^a-zA-Z0-9]", "_");
        int screenshotNum = screenshotCounter.incrementAndGet();
        
        return String.format("%s_%s_%s_%d.png", 
            baseName.replaceAll("[^a-zA-Z0-9]", "_"), 
            threadName, 
            timestamp, 
            screenshotNum);
    }
}