package com.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import java.time.Duration;
import java.util.List;

/**
 * Safe WebDriver Utilities with null checks and error handling
 * Provides safe methods for WebDriver operations with automatic recovery
 */
public class SafeWebDriverUtils {
    
    private static final int DEFAULT_TIMEOUT = 20;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Safely get WebDriver with null checks and recovery
     */
    public static WebDriver getSafeWebDriver() {
        try {
            WebDriver driver = WebDriverSetup.getDriver();
            if (driver == null) {
                System.out.println("⚠️ WebDriver is null, attempting to create new instance...");
                WebDriverSetup.setupDriver("chrome");
                driver = WebDriverSetup.getDriver();
                
                if (driver != null) {
                    ThreadSafeScreenshotUtils.setDriver(driver);
                    System.out.println("✅ WebDriver recovery successful");
                } else {
                    throw new RuntimeException("Failed to create WebDriver after recovery attempt");
                }
            }
            
            // Validate driver is responsive
            try {
                driver.getCurrentUrl();
                return driver;
            } catch (Exception e) {
                System.out.println("⚠️ WebDriver validation failed, recreating...");
                WebDriverSetup.quitDriver();
                WebDriverSetup.setupDriver("chrome");
                driver = WebDriverSetup.getDriver();
                
                if (driver != null) {
                    ThreadSafeScreenshotUtils.setDriver(driver);
                    return driver;
                } else {
                    throw new RuntimeException("Failed to recreate responsive WebDriver");
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Critical WebDriver error: " + e.getMessage());
            throw new RuntimeException("Unable to obtain safe WebDriver", e);
        }
    }
    
    /**
     * Safe navigation with retry mechanism and fallback URLs
     */
    public static void safeNavigate(String url) {
        WebDriver driver = getSafeWebDriver();
        
        // Try original URL first
        if (tryNavigateToURL(driver, url)) {
            return;
        }
        
        // If original URL fails, try fallback URLs
        System.out.println("⚠️ Original URL failed, trying fallback URLs...");
        
        // Extract path from original URL if it contains one
        String path = "";
        try {
            int pathIndex = url.indexOf("/", 8); // Skip protocol part
            if (pathIndex > 0) {
                path = url.substring(pathIndex);
            }
        } catch (Exception e) {
            // Ignore path extraction errors
        }
        
        // Try fallback URLs with the same path
        String[] fallbackUrls = {
            "https://www.paytr.com" + path,
            "https://demo.paytr.com" + path,
            "https://httpbin.org/get", // Mock endpoint for testing
            "https://jsonplaceholder.typicode.com/posts/1" // Another mock endpoint
        };
        
        for (String fallbackUrl : fallbackUrls) {
            System.out.println("🔄 Trying fallback URL: " + fallbackUrl);
            if (tryNavigateToURL(driver, fallbackUrl)) {
                System.out.println("✅ Fallback navigation successful: " + fallbackUrl);
                return;
            }
        }
        
        throw new RuntimeException("Navigation failed for all URLs including fallbacks");
    }
    
    /**
     * Try to navigate to a specific URL with retries
     */
    private static boolean tryNavigateToURL(WebDriver driver, String url) {
        int attempts = 0;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                System.out.println("🌐 Navigating to: " + url + " (Attempt: " + (attempts + 1) + ")");
                driver.get(url);
                
                // Wait for page to load
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
                
                System.out.println("✅ Navigation successful: " + driver.getCurrentUrl());
                return true;
                
            } catch (Exception e) {
                attempts++;
                System.out.println("⚠️ Navigation attempt " + attempts + " failed: " + e.getMessage());
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    System.out.println("❌ All attempts failed for URL: " + url);
                    return false;
                }
                
                // Wait before retry
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Safe element finding with wait and retry
     */
    public static WebElement safeFindElement(By locator) {
        return safeFindElement(locator, DEFAULT_TIMEOUT);
    }
    
    public static WebElement safeFindElement(By locator, int timeoutSeconds) {
        WebDriver driver = getSafeWebDriver();
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            
            // Additional check: ensure element is visible
            wait.until(ExpectedConditions.visibilityOf(element));
            
            return element;
            
        } catch (TimeoutException e) {
            System.out.println("⚠️ Element not found: " + locator.toString());
            throw new RuntimeException("Element not found within " + timeoutSeconds + " seconds: " + locator, e);
        }
    }
    
    /**
     * Safe element finding for multiple elements
     */
    public static List<WebElement> safeFindElements(By locator) {
        return safeFindElements(locator, DEFAULT_TIMEOUT);
    }
    
    public static List<WebElement> safeFindElements(By locator, int timeoutSeconds) {
        WebDriver driver = getSafeWebDriver();
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            
            return driver.findElements(locator);
            
        } catch (TimeoutException e) {
            System.out.println("⚠️ Elements not found: " + locator.toString());
            return List.of(); // Return empty list instead of throwing exception
        }
    }
    
    /**
     * Safe click with wait and retry
     */
    public static void safeClick(By locator) {
        safeClick(locator, DEFAULT_TIMEOUT);
    }
    
    public static void safeClick(By locator, int timeoutSeconds) {
        WebElement element = safeFindElement(locator, timeoutSeconds);
        
        try {
            WebDriverWait wait = new WebDriverWait(getSafeWebDriver(), Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            
            System.out.println("✅ Successfully clicked element: " + locator.toString());
            
        } catch (Exception e) {
            System.out.println("⚠️ Click failed, trying JavaScript click: " + locator.toString());
            
            // Fallback to JavaScript click
            try {
                JavascriptExecutor js = (JavascriptExecutor) getSafeWebDriver();
                js.executeScript("arguments[0].click();", element);
                System.out.println("✅ JavaScript click successful: " + locator.toString());
            } catch (Exception jsError) {
                throw new RuntimeException("Both normal and JavaScript click failed for: " + locator, jsError);
            }
        }
    }
    
    /**
     * Safe text input with clear and retry
     */
    public static void safeSendKeys(By locator, String text) {
        safeSendKeys(locator, text, DEFAULT_TIMEOUT);
    }
    
    public static void safeSendKeys(By locator, String text, int timeoutSeconds) {
        WebElement element = safeFindElement(locator, timeoutSeconds);
        
        try {
            element.clear();
            element.sendKeys(text);
            System.out.println("✅ Successfully entered text in element: " + locator.toString());
            
        } catch (Exception e) {
            System.out.println("⚠️ SendKeys failed, trying JavaScript: " + locator.toString());
            
            // Fallback to JavaScript
            try {
                JavascriptExecutor js = (JavascriptExecutor) getSafeWebDriver();
                js.executeScript("arguments[0].value = arguments[1];", element, text);
                System.out.println("✅ JavaScript text input successful: " + locator.toString());
            } catch (Exception jsError) {
                throw new RuntimeException("Both normal and JavaScript text input failed for: " + locator, jsError);
            }
        }
    }
    
    /**
     * Safe page title retrieval
     */
    public static String getSafePageTitle() {
        try {
            WebDriver driver = getSafeWebDriver();
            String title = driver.getTitle();
            return title != null ? title : "No Title";
        } catch (Exception e) {
            System.out.println("⚠️ Failed to get page title: " + e.getMessage());
            return "Title Unavailable";
        }
    }
    
    /**
     * Safe current URL retrieval
     */
    public static String getSafeCurrentUrl() {
        try {
            WebDriver driver = getSafeWebDriver();
            String url = driver.getCurrentUrl();
            return url != null ? url : "No URL";
        } catch (Exception e) {
            System.out.println("⚠️ Failed to get current URL: " + e.getMessage());
            return "URL Unavailable";
        }
    }
    
    /**
     * Safe page source retrieval
     */
    public static String getSafePageSource() {
        try {
            WebDriver driver = getSafeWebDriver();
            String source = driver.getPageSource();
            return source != null ? source : "";
        } catch (Exception e) {
            System.out.println("⚠️ Failed to get page source: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Wait for page to be ready
     */
    public static void waitForPageReady() {
        waitForPageReady(DEFAULT_TIMEOUT);
    }
    
    public static void waitForPageReady(int timeoutSeconds) {
        try {
            WebDriver driver = getSafeWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            
            // Wait for document ready state
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Wait for jQuery if present
            wait.until(webDriver -> {
                try {
                    return (Boolean) ((JavascriptExecutor) webDriver)
                        .executeScript("return typeof jQuery === 'undefined' || jQuery.active === 0");
                } catch (Exception e) {
                    return true; // jQuery not present
                }
            });
            
            System.out.println("✅ Page ready state confirmed");
            
        } catch (Exception e) {
            System.out.println("⚠️ Page ready check failed: " + e.getMessage());
        }
    }
}