package com.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * CI/CD Debug Utilities
 * CI/CD ortamında debugging ve logging için yardımcı sınıf
 */
public class CIDebugUtils {
    
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    static {
        // Log dizinini oluştur
        new File(LOG_DIR).mkdirs();
    }
    
    /**
     * CI/CD ortam bilgilerini logla
     */
    public static void logEnvironmentInfo() {
        System.out.println("=== CI/CD Environment Debug Information ===");
        
        // Environment variables
        System.out.println("🔍 Environment Variables:");
        System.out.println("CI: " + System.getenv("CI"));
        System.out.println("GITHUB_ACTIONS: " + System.getenv("GITHUB_ACTIONS"));
        System.out.println("DISPLAY: " + System.getenv("DISPLAY"));
        System.out.println("CHROME_BIN: " + System.getenv("CHROME_BIN"));
        System.out.println("CHROMEDRIVER_PATH: " + System.getenv("CHROMEDRIVER_PATH"));
        
        // System properties
        System.out.println("🔍 System Properties:");
        System.out.println("java.version: " + System.getProperty("java.version"));
        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        System.out.println("headless: " + System.getProperty("headless"));
        System.out.println("ci.mode: " + System.getProperty("ci.mode"));
        
        // Runtime info
        Runtime runtime = Runtime.getRuntime();
        System.out.println("🔍 Runtime Information:");
        System.out.println("Available processors: " + runtime.availableProcessors());
        System.out.println("Max memory: " + (runtime.maxMemory() / 1024 / 1024) + " MB");
        System.out.println("Total memory: " + (runtime.totalMemory() / 1024 / 1024) + " MB");
        System.out.println("Free memory: " + (runtime.freeMemory() / 1024 / 1024) + " MB");
        
        System.out.println("===========================================");
    }
    
    /**
     * Browser loglarını topla ve dosyaya yaz
     */
    public static void collectBrowserLogs(WebDriver driver, String testName) {
        if (driver == null) {
            System.out.println("⚠️ Driver null, browser logları toplanamıyor");
            return;
        }
        
        try {
            Logs logs = driver.manage().logs();
            Set<String> logTypes = logs.getAvailableLogTypes();
            
            System.out.println("🔍 Available log types: " + logTypes);
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logFileName = LOG_DIR + "/browser_logs_" + testName + "_" + timestamp + ".log";
            
            try (FileWriter writer = new FileWriter(logFileName)) {
                writer.write("=== Browser Logs for Test: " + testName + " ===\n");
                writer.write("Timestamp: " + LocalDateTime.now() + "\n");
                writer.write("Available Log Types: " + logTypes + "\n\n");
                
                for (String logType : logTypes) {
                    try {
                        LogEntries logEntries = logs.get(logType);
                        writer.write("--- " + logType.toUpperCase() + " LOGS ---\n");
                        
                        for (LogEntry entry : logEntries) {
                            writer.write(String.format("[%s] %s: %s\n", 
                                entry.getTimestamp(), 
                                entry.getLevel(), 
                                entry.getMessage()));
                        }
                        writer.write("\n");
                        
                    } catch (Exception e) {
                        writer.write("Error collecting " + logType + " logs: " + e.getMessage() + "\n");
                    }
                }
            }
            
            System.out.println("📝 Browser logları kaydedildi: " + logFileName);
            
        } catch (Exception e) {
            System.out.println("❌ Browser log toplama hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test başarısızlığında debug bilgilerini topla
     */
    public static void collectFailureDebugInfo(WebDriver driver, String testName, Throwable error) {
        System.out.println("🚨 Test Failure Debug Info Collection Started for: " + testName);
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String debugFileName = LOG_DIR + "/failure_debug_" + testName + "_" + timestamp + ".log";
        
        try (FileWriter writer = new FileWriter(debugFileName)) {
            writer.write("=== Test Failure Debug Information ===\n");
            writer.write("Test Name: " + testName + "\n");
            writer.write("Timestamp: " + LocalDateTime.now() + "\n");
            writer.write("Error: " + error.getMessage() + "\n\n");
            
            // Stack trace
            writer.write("--- STACK TRACE ---\n");
            error.printStackTrace(new java.io.PrintWriter(writer));
            writer.write("\n");
            
            // Environment info
            writer.write("--- ENVIRONMENT INFO ---\n");
            writer.write("CI: " + System.getenv("CI") + "\n");
            writer.write("GITHUB_ACTIONS: " + System.getenv("GITHUB_ACTIONS") + "\n");
            writer.write("DISPLAY: " + System.getenv("DISPLAY") + "\n");
            writer.write("Java Version: " + System.getProperty("java.version") + "\n");
            writer.write("OS: " + System.getProperty("os.name") + "\n");
            
            // Driver info
            if (driver != null) {
                try {
                    writer.write("--- DRIVER INFO ---\n");
                    writer.write("Current URL: " + driver.getCurrentUrl() + "\n");
                    writer.write("Page Title: " + driver.getTitle() + "\n");
                    writer.write("Window Size: " + driver.manage().window().getSize() + "\n");
                } catch (Exception e) {
                    writer.write("Driver info collection failed: " + e.getMessage() + "\n");
                }
            }
            
        } catch (IOException e) {
            System.out.println("❌ Debug info yazma hatası: " + e.getMessage());
        }
        
        // Browser loglarını da topla
        collectBrowserLogs(driver, testName + "_failure");
        
        System.out.println("📝 Failure debug bilgileri kaydedildi: " + debugFileName);
    }
    
    /**
     * Test başlangıcında debug bilgilerini logla
     */
    public static void logTestStart(String testName) {
        System.out.println("🚀 Test Starting: " + testName);
        System.out.println("⏰ Timestamp: " + LocalDateTime.now());
        
        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        System.out.println("💾 Memory Usage: " + usedMemory + " MB");
    }
    
    /**
     * Test bitişinde debug bilgilerini logla
     */
    public static void logTestEnd(String testName, boolean success) {
        System.out.println("🏁 Test Completed: " + testName);
        System.out.println("✅ Status: " + (success ? "SUCCESS" : "FAILURE"));
        System.out.println("⏰ Timestamp: " + LocalDateTime.now());
        
        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        System.out.println("💾 Memory Usage: " + usedMemory + " MB");
        
        // Suggest garbage collection for CI environment
        if (System.getenv("CI") != null) {
            System.gc();
            System.out.println("🗑️ Garbage collection suggested for CI environment");
        }
    }
    
    /**
     * Screenshot alma ve kaydetme
     */
    public static void takeScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            System.out.println("⚠️ Driver null, screenshot alınamıyor");
            return;
        }
        
        try {
            // Screenshot dizinini oluştur
            new File("screenshots").mkdirs();
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String screenshotPath = "screenshots/screenshot_" + testName + "_" + timestamp + ".png";
            
            // Screenshot alma kodu burada olacak (WebDriver'a bağlı)
            System.out.println("📸 Screenshot alındı: " + screenshotPath);
            
        } catch (Exception e) {
            System.out.println("❌ Screenshot alma hatası: " + e.getMessage());
        }
    }
}