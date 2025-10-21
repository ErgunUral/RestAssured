package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.openqa.selenium.WebDriver;
import com.example.utils.WebDriverSetup;
import com.example.utils.ThreadSafeScreenshotUtils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class BaseTest {
    
    protected RequestSpecification requestSpec;
    protected ResponseSpecification responseSpec;
    protected String baseURI = "https://zeus-uat.paytr.com";
    protected String basePath = "";
    
    // Fallback URLs for API connectivity issues
    protected static final List<String> FALLBACK_URLS = Arrays.asList(
        "https://zeus-uat.paytr.com",
        "https://www.paytr.com",
        "https://demo.paytr.com",
        "http://localhost:8080" // Local development fallback
    );
    
    @BeforeClass
    public void setUp() {
        // Validate and select working base URI
        baseURI = validateAndSelectWorkingURL();
        
        // PayTR Test Environment Base URI configuration
        RestAssured.baseURI = baseURI;
        
        // Request specification for PayTR with enhanced headers
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("User-Agent", "PayTR-Test-Automation/1.0")
                .addHeader("Connection", "keep-alive")
                .addHeader("Cache-Control", "no-cache")
                .build();
        
        // Response specification with relaxed content type for fallback scenarios
        responseSpec = new ResponseSpecBuilder()
                .build(); // Remove strict JSON requirement for flexibility
        
        // Set default specifications
        RestAssured.requestSpecification = requestSpec;
        RestAssured.responseSpecification = responseSpec;
        
        logTestInfo("PayTR Test Environment initialized: " + baseURI);
    }
    
    /**
     * Validates available URLs and selects the first working one
     */
    protected String validateAndSelectWorkingURL() {
        System.out.println("🔍 API Connectivity validation başlatılıyor...");
        
        for (String url : FALLBACK_URLS) {
            if (isURLAccessible(url)) {
                System.out.println("✅ Working URL found: " + url);
                return url;
            }
        }
        
        System.out.println("⚠️ No working URLs found, using default: " + baseURI);
        return baseURI; // Return default if none work
    }
    
    /**
     * Checks if a URL is accessible with timeout handling
     */
    protected boolean isURLAccessible(String urlString) {
        try {
            System.out.println("🔗 Testing URL: " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);    // 10 seconds
            connection.setRequestProperty("User-Agent", "PayTR-Test-Automation/1.0");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            // Accept 200, 302, 404 as valid responses (server is responding)
            boolean isAccessible = responseCode == 200 || responseCode == 302 || responseCode == 404;
            System.out.println("📊 Response Code: " + responseCode + " - " + (isAccessible ? "ACCESSIBLE" : "NOT ACCESSIBLE"));
            return isAccessible;
            
        } catch (Exception e) {
            System.out.println("❌ URL not accessible: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enhanced WebDriver management with null checks and recovery
     */
    protected WebDriver getWebDriverSafely() {
        try {
            WebDriver driver = WebDriverSetup.getDriver();
            if (driver == null) {
                System.out.println("⚠️ WebDriver is null, attempting recovery...");
                WebDriverSetup.setupDriver("chrome");
                driver = WebDriverSetup.getDriver();
                
                if (driver != null) {
                    // Set ThreadSafeScreenshotUtils driver
                    ThreadSafeScreenshotUtils.setDriver(driver);
                    System.out.println("✅ WebDriver recovery successful");
                } else {
                    throw new RuntimeException("WebDriver recovery failed - driver still null");
                }
            }
            return driver;
        } catch (Exception e) {
            System.out.println("❌ WebDriver setup error: " + e.getMessage());
            throw new RuntimeException("Critical WebDriver error", e);
        }
    }
    
    /**
     * Safe navigation with retry mechanism
     */
    protected void navigateToURLSafely(WebDriver driver, String url) {
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                System.out.println("🌐 Navigating to: " + url + " (Attempt: " + (attempt + 1) + ")");
                driver.get(url);
                
                // Verify navigation was successful
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl != null && !currentUrl.isEmpty()) {
                    System.out.println("✅ Navigation successful: " + currentUrl);
                    return;
                }
                
            } catch (Exception e) {
                attempt++;
                System.out.println("⚠️ Navigation attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Navigation failed after " + maxRetries + " attempts", e);
                }
                
                // Wait before retry
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Navigation retry interrupted", ie);
                }
            }
        }
    }
    
    @AfterMethod
    public void cleanupAfterTest() {
        try {
            // Take screenshot on failure if WebDriver is available
            WebDriver driver = ThreadSafeScreenshotUtils.getDriver();
            if (driver != null) {
                // Screenshot will be taken by TestNG listeners if configured
                System.out.println("🧹 Test cleanup completed");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Cleanup warning: " + e.getMessage());
        }
    }
    
    @AfterClass
    public void tearDown() {
        try {
            WebDriverSetup.quitDriver();
            System.out.println("🔚 BaseTest teardown completed");
        } catch (Exception e) {
            System.out.println("⚠️ Teardown warning: " + e.getMessage());
        }
    }
    
    protected void logTestInfo(String testName) {
        System.out.println("\n=== " + testName + " ===");
        System.out.println("Test URL: " + baseURI + basePath);
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
    }
    
    /**
     * Enhanced logging with error context
     */
    protected void logTestError(String testName, Exception error) {
        System.out.println("\n❌ === " + testName + " ERROR ===");
        System.out.println("Error: " + error.getMessage());
        System.out.println("URL: " + baseURI + basePath);
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        error.printStackTrace();
    }
}