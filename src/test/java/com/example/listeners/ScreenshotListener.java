package com.example.listeners;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Enhanced Screenshot Listener for PayTR Test Suite
 * Automatically captures screenshots on test failures and provides enhanced reporting
 */
public class ScreenshotListener implements ITestListener {
    
    private static final String SCREENSHOT_DIR = "test-output/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    static {
        // Create screenshots directory if it doesn't exist
        try {
            Path screenshotPath = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotPath)) {
                Files.createDirectories(screenshotPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create screenshot directory: " + e.getMessage());
        }
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("📸 Screenshot Listener aktif - Test: " + getTestName(result));
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("📸 Test başarısız - Screenshot alınıyor: " + getTestName(result));
        
        try {
            // Capture screenshot
            byte[] screenshot = captureScreenshot(result);
            if (screenshot != null) {
                // Save screenshot to file
                String fileName = saveScreenshotToFile(screenshot, result);
                
                // Attach to Allure report
                attachScreenshotToAllure(screenshot, result);
                
                // Add screenshot info to test result
                addScreenshotInfoToTestResult(result, fileName);
                
                System.out.println("✅ Screenshot başarıyla alındı: " + fileName);
            } else {
                System.out.println("⚠️ Screenshot alınamadı - WebDriver bulunamadı");
            }
        } catch (Exception e) {
            System.err.println("❌ Screenshot alma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("📸 Test atlandı - Screenshot alınıyor: " + getTestName(result));
        
        try {
            // Also capture screenshot for skipped tests to understand the state
            byte[] screenshot = captureScreenshot(result);
            if (screenshot != null) {
                String fileName = saveScreenshotToFile(screenshot, result);
                attachScreenshotToAllure(screenshot, result);
                addScreenshotInfoToTestResult(result, fileName);
                
                System.out.println("✅ Skipped test screenshot alındı: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("❌ Skipped test screenshot alma hatası: " + e.getMessage());
        }
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        // Optionally capture screenshots for successful tests in debug mode
        String debugMode = System.getProperty("screenshot.debug", "false");
        if ("true".equalsIgnoreCase(debugMode)) {
            System.out.println("📸 Debug mode - Başarılı test screenshot alınıyor: " + getTestName(result));
            
            try {
                byte[] screenshot = captureScreenshot(result);
                if (screenshot != null) {
                    String fileName = saveScreenshotToFile(screenshot, result);
                    attachScreenshotToAllure(screenshot, result);
                    System.out.println("✅ Debug screenshot alındı: " + fileName);
                }
            } catch (Exception e) {
                System.err.println("❌ Debug screenshot alma hatası: " + e.getMessage());
            }
        }
    }
    
    /**
     * Captures screenshot from WebDriver
     */
    private byte[] captureScreenshot(ITestResult result) {
        try {
            // Try to get WebDriver from test class
            WebDriver driver = getWebDriverFromTest(result);
            
            if (driver != null && driver instanceof TakesScreenshot) {
                TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
                return takesScreenshot.getScreenshotAs(OutputType.BYTES);
            } else {
                System.out.println("⚠️ WebDriver TakesScreenshot desteklemiyor veya null");
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Screenshot capture hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Attempts to get WebDriver instance from test class
     */
    private WebDriver getWebDriverFromTest(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            
            // Try common field names for WebDriver
            String[] driverFieldNames = {"driver", "webDriver", "webdriver", "browser"};
            
            for (String fieldName : driverFieldNames) {
                try {
                    java.lang.reflect.Field field = testInstance.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(testInstance);
                    
                    if (fieldValue instanceof WebDriver) {
                        return (WebDriver) fieldValue;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Continue to next field name
                }
            }
            
            // Try parent class fields
            Class<?> parentClass = testInstance.getClass().getSuperclass();
            if (parentClass != null) {
                for (String fieldName : driverFieldNames) {
                    try {
                        java.lang.reflect.Field field = parentClass.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object fieldValue = field.get(testInstance);
                        
                        if (fieldValue instanceof WebDriver) {
                            return (WebDriver) fieldValue;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Continue to next field name
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ WebDriver alma hatası: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Saves screenshot to file system
     */
    private String saveScreenshotToFile(byte[] screenshot, ITestResult result) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String testName = getTestName(result).replaceAll("[^a-zA-Z0-9.-]", "_");
        String status = getTestStatus(result);
        String fileName = String.format("%s_%s_%s.png", testName, status, timestamp);
        
        Path filePath = Paths.get(SCREENSHOT_DIR, fileName);
        Files.write(filePath, screenshot);
        
        return fileName;
    }
    
    /**
     * Attaches screenshot to Allure report
     */
    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] attachScreenshotToAllure(byte[] screenshot, ITestResult result) {
        // Also add as Allure attachment with context
        Allure.addAttachment(
            "Screenshot - " + getTestName(result),
            "image/png",
            new ByteArrayInputStream(screenshot),
            ".png"
        );
        
        return screenshot;
    }
    
    /**
     * Adds screenshot information to test result
     */
    private void addScreenshotInfoToTestResult(ITestResult result, String fileName) {
        // Add screenshot info to test result attributes
        result.setAttribute("screenshot.fileName", fileName);
        result.setAttribute("screenshot.path", SCREENSHOT_DIR + "/" + fileName);
        result.setAttribute("screenshot.timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        // Add to Allure as text attachment
        String screenshotInfo = String.format(
            "Screenshot Information:\n" +
            "File Name: %s\n" +
            "Path: %s\n" +
            "Test: %s\n" +
            "Status: %s\n" +
            "Timestamp: %s",
            fileName,
            SCREENSHOT_DIR + "/" + fileName,
            getTestName(result),
            getTestStatus(result),
            LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
        
        Allure.addAttachment("Screenshot Info", screenshotInfo);
    }
    
    /**
     * Gets formatted test name
     */
    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    /**
     * Gets test status as string
     */
    private String getTestStatus(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASSED";
            case ITestResult.FAILURE:
                return "FAILED";
            case ITestResult.SKIP:
                return "SKIPPED";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Utility method to capture screenshot manually from any test
     */
    public static void captureScreenshotManually(WebDriver driver, String testName, String description) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                
                // Save to file
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String fileName = String.format("manual_%s_%s.png", 
                    testName.replaceAll("[^a-zA-Z0-9.-]", "_"), timestamp);
                
                Path filePath = Paths.get(SCREENSHOT_DIR, fileName);
                Files.write(filePath, screenshot);
                
                // Attach to Allure
                Allure.addAttachment(
                    "Manual Screenshot - " + description,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
                );
                
                System.out.println("✅ Manual screenshot alındı: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("❌ Manual screenshot alma hatası: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup old screenshots (optional utility method)
     */
    public static void cleanupOldScreenshots(int daysToKeep) {
        try {
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (Files.exists(screenshotDir)) {
                long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
                
                Files.list(screenshotDir)
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
                            System.out.println("🗑️ Eski screenshot silindi: " + path.getFileName());
                        } catch (IOException e) {
                            System.err.println("❌ Screenshot silme hatası: " + e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("❌ Screenshot cleanup hatası: " + e.getMessage());
        }
    }
}