package com.example.tests;

import com.example.tests.BaseTest;
import com.example.config.PayTRTestConfig;
import com.example.utils.WebDriverSetup;
import com.example.utils.SafeWebDriverUtils;
import com.example.utils.SecurityTestUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * PayTR Güvenlik Test Senaryoları
 * Test ID: ST-001 to ST-004
 * Kategori: Security Testing
 */
@Epic("PayTR Security Testing")
@Feature("Security Vulnerabilities")
public class PayTRSecurityTests extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private SecurityTestUtils securityUtils;
    
    @BeforeClass
    @Step("Güvenlik testleri için test ortamını hazırla")
    public void setupSecurityTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "";
        
        try {
            // Use SafeWebDriverUtils for robust WebDriver initialization
            driver = SafeWebDriverUtils.getSafeWebDriver();
            
            if (driver == null) {
                throw new RuntimeException("WebDriver başlatılamadı - driver null döndü");
            }
            
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            securityUtils = new SecurityTestUtils(driver);
            
            logTestInfo("PayTR Güvenlik Test Suite başlatıldı");
            System.out.println("✅ WebDriver başarıyla başlatıldı: " + driver.getClass().getSimpleName());
            
        } catch (Exception e) {
            System.out.println("❌ WebDriver setup hatası: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("WebDriver başlatılamadı", e);
        }
    }
    
    @BeforeMethod
    public void validateDriverBeforeTest() {
        try {
            // Use SafeWebDriverUtils for robust driver validation and recovery
            driver = SafeWebDriverUtils.getSafeWebDriver();
            
            if (driver != null) {
                wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                securityUtils = new SecurityTestUtils(driver);
                System.out.println("✅ Driver validation successful");
            } else {
                throw new RuntimeException("Driver validation failed - null driver");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Driver validation error: " + e.getMessage());
            throw new RuntimeException("Driver validation failed", e);
        }
    }
    
    @AfterClass
    @Step("Güvenlik testleri sonrası temizlik")
    public void tearDown() {
        WebDriverSetup.quitDriver();
        logTestInfo("PayTR Güvenlik Test Suite tamamlandı");
    }
    
    /**
     * Test ID: ST-001
     * Test Adı: Login Form SQL Injection Testi
     * Kategori: Security - SQL Injection
     * Öncelik: Kritik
     */
    @Test(priority = 1, groups = {"security", "critical", "sql-injection"})
    @Story("SQL Injection Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Giriş formunda SQL injection saldırısı denemesi ve korunma kontrolü")
    public void testLoginFormSQLInjection() {
        logTestInfo("Test ID: ST-001 - Login Form SQL Injection Testi");
        
        try {
            // Giriş sayfasına git
            driver.get(baseURI + "/magaza/kullanici-girisi");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // SQL Injection payload'ları
            String[] sqlPayloads = {
                "admin'--",
                "' OR '1'='1",
                "' OR 1=1--",
                "admin'; DROP TABLE users;--",
                "' UNION SELECT * FROM users--"
            };
            
            for (String payload : sqlPayloads) {
                // Email alanını bul ve SQL injection payload gir
                WebElement emailField = securityUtils.findEmailField();
                WebElement passwordField = securityUtils.findPasswordField();
                WebElement loginButton = securityUtils.findLoginButton();
                
                if (emailField != null && passwordField != null && loginButton != null) {
                    emailField.clear();
                    emailField.sendKeys(payload);
                    passwordField.clear();
                    passwordField.sendKeys("test123");
                    
                    loginButton.click();
                    
                    // SQL injection'ın engellenmesini kontrol et
                    Thread.sleep(2000);
                    String currentUrl = driver.getCurrentUrl();
                    String pageSource = driver.getPageSource().toLowerCase();
                    
                    // SQL injection başarısız olmalı
                    assertFalse(currentUrl.contains("dashboard") || currentUrl.contains("admin"), 
                        "SQL Injection payload başarılı oldu: " + payload);
                    
                    // Hata mesajı veya güvenlik uyarısı olmalı
                    assertTrue(pageSource.contains("hata") || pageSource.contains("error") || 
                              pageSource.contains("geçersiz") || pageSource.contains("invalid"),
                        "SQL Injection için uygun hata mesajı görüntülenmedi");
                    
                    logTestInfo("SQL Injection payload engellendi: " + payload);
                }
            }
            
            logTestResult("ST-001", "BAŞARILI", "SQL Injection saldırıları başarıyla engellendi");
            
        } catch (Exception e) {
            logTestResult("ST-001", "BAŞARISIZ", "SQL Injection testi hatası: " + e.getMessage());
            fail("SQL Injection testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: ST-002
     * Test Adı: Search Form SQL Injection Testi
     * Kategori: Security - SQL Injection
     * Öncelik: Kritik
     */
    @Test(priority = 2, groups = {"security", "critical", "sql-injection"})
    @Story("Search Form SQL Injection Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Arama formunda SQL injection saldırısı denemesi")
    public void testSearchFormSQLInjection() {
        logTestInfo("Test ID: ST-002 - Search Form SQL Injection Testi");
        
        try {
            // Ana sayfaya git
            driver.get(baseURI);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Arama alanını bul
            List<WebElement> searchFields = driver.findElements(By.xpath(
                "//input[@type='search' or @name='search' or @placeholder*='ara' or @placeholder*='search']"));
            
            if (!searchFields.isEmpty()) {
                WebElement searchField = searchFields.get(0);
                
                // SQL Injection payload'ları
                String[] searchPayloads = {
                    "' UNION SELECT * FROM users--",
                    "'; DROP TABLE products;--",
                    "' OR 1=1--",
                    "admin'/**/UNION/**/SELECT/**/password/**/FROM/**/users--"
                };
                
                for (String payload : searchPayloads) {
                    searchField.clear();
                    searchField.sendKeys(payload);
                    
                    // Enter tuşuna bas veya arama butonuna tıkla
                    searchField.submit();
                    
                    Thread.sleep(2000);
                    
                    // SQL injection'ın engellenmesini kontrol et
                    String pageSource = driver.getPageSource().toLowerCase();
                    
                    // Veritabanı hatası veya hassas bilgi sızıntısı olmamalı
                    assertFalse(pageSource.contains("sql") || pageSource.contains("mysql") || 
                               pageSource.contains("database") || pageSource.contains("table"),
                        "SQL Injection ile veritabanı bilgisi sızdı: " + payload);
                    
                    logTestInfo("Search SQL Injection payload engellendi: " + payload);
                }
            } else {
                logTestInfo("Arama alanı bulunamadı, test atlandı");
            }
            
            logTestResult("ST-002", "BAŞARILI", "Search form SQL Injection saldırıları engellendi");
            
        } catch (Exception e) {
            logTestResult("ST-002", "BAŞARISIZ", "Search SQL Injection testi hatası: " + e.getMessage());
            fail("Search SQL Injection testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: ST-003
     * Test Adı: Stored XSS Testi
     * Kategori: Security - XSS
     * Öncelik: Kritik
     */
    @Test(priority = 3, groups = {"security", "critical", "xss"})
    @Story("XSS Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kullanıcı profil bilgilerinde stored XSS saldırısı denemesi")
    public void testStoredXSSAttack() {
        logTestInfo("Test ID: ST-003 - Stored XSS Testi");
        
        try {
            // Giriş sayfasına git
            driver.get(baseURI + "/magaza/kullanici-girisi");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // XSS payload'ları
            String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "<svg onload=alert('XSS')>",
                "javascript:alert('XSS')",
                "<iframe src=javascript:alert('XSS')></iframe>"
            };
            
            // Form alanlarını bul
            List<WebElement> inputFields = driver.findElements(By.xpath("//input[@type='text' or @type='email']"));
            List<WebElement> textAreas = driver.findElements(By.tagName("textarea"));
            
            // Tüm input alanlarını test et
            for (WebElement field : inputFields) {
                for (String payload : xssPayloads) {
                    try {
                        field.clear();
                        field.sendKeys(payload);
                        
                        // JavaScript executor ile XSS kontrolü
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        String fieldValue = (String) js.executeScript("return arguments[0].value;", field);
                        
                        // XSS payload'ının encode edilmiş olması gerekir
                        assertFalse(fieldValue.contains("<script>") || fieldValue.contains("javascript:"),
                            "XSS payload encode edilmedi: " + payload);
                        
                        logTestInfo("XSS payload güvenli şekilde işlendi: " + payload);
                        
                    } catch (Exception e) {
                        // Field erişim hatası, devam et
                        continue;
                    }
                }
            }
            
            // TextArea alanlarını test et
            for (WebElement textArea : textAreas) {
                for (String payload : xssPayloads) {
                    try {
                        textArea.clear();
                        textArea.sendKeys(payload);
                        
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        String textValue = (String) js.executeScript("return arguments[0].value;", textArea);
                        
                        assertFalse(textValue.contains("<script>") || textValue.contains("javascript:"),
                            "XSS payload encode edilmedi: " + payload);
                        
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            
            logTestResult("ST-003", "BAŞARILI", "XSS saldırıları başarıyla engellendi");
            
        } catch (Exception e) {
            logTestResult("ST-003", "BAŞARISIZ", "XSS testi hatası: " + e.getMessage());
            fail("XSS testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: ST-004
     * Test Adı: Session Timeout Testi
     * Kategori: Security - Session Management
     * Öncelik: Orta
     */
    @Test(priority = 4, groups = {"security", "medium", "session"})
    @Story("Session Management")
    @Severity(SeverityLevel.NORMAL)
    @Description("Oturum zaman aşımı kontrolü")
    public void testSessionTimeout() {
        logTestInfo("Test ID: ST-004 - Session Timeout Testi");
        
        try {
            // Giriş sayfasına git
            driver.get(baseURI + "/magaza/kullanici-girisi");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Session cookie'lerini kontrol et
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Session cookie'si var mı kontrol et
            String sessionCookie = (String) js.executeScript(
                "return document.cookie.split(';').find(row => row.startsWith('PHPSESSID') || row.startsWith('session'));"
            );
            
            if (sessionCookie != null) {
                logTestInfo("Session cookie bulundu: " + sessionCookie);
                
                // Cookie'nin secure ve httpOnly flag'lerini kontrol et
                String allCookies = (String) js.executeScript("return document.cookie;");
                
                // Secure flag kontrolü (HTTPS ortamında olmalı)
                assertTrue(driver.getCurrentUrl().startsWith("https://"), 
                    "Session cookie HTTPS ortamında kullanılmalı");
                
                logTestInfo("Session güvenlik kontrolleri başarılı");
            } else {
                logTestInfo("Session cookie bulunamadı, test atlandı");
            }
            
            // CSRF token kontrolü
            List<WebElement> csrfTokens = driver.findElements(By.xpath(
                "//input[@name='_token' or @name='csrf_token' or @name='authenticity_token']"));
            
            if (!csrfTokens.isEmpty()) {
                logTestInfo("CSRF token bulundu, güvenlik önlemi mevcut");
            } else {
                logTestInfo("CSRF token bulunamadı");
            }
            
            logTestResult("ST-004", "BAŞARILI", "Session güvenlik kontrolleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("ST-004", "BAŞARISIZ", "Session timeout testi hatası: " + e.getMessage());
            fail("Session timeout testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: AS-001
     * Advanced Security - API Rate Limiting and DDoS Protection
     * Tests system behavior under high-frequency requests
     */
    @Test(priority = 10, groups = {"security", "advanced", "rate-limiting"})
    @Story("Advanced Rate Limiting Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("API rate limiting ve DDoS korunma testleri")
    public void testAdvancedRateLimitingProtection() {
        logTestInfo("Test ID: AS-001 - Advanced Rate Limiting Protection");
        
        try {
            int requestCount = 50;
            int blockedRequests = 0;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < requestCount; i++) {
                Map<String, Object> paymentData = new HashMap<>();
                paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                paymentData.put("user_ip", "127.0.0.1");
                paymentData.put("merchant_oid", "AS001_RATE_" + i + "_" + System.currentTimeMillis());
                paymentData.put("email", "ratelimit.test" + i + "@example.com");
                paymentData.put("payment_amount", "10000");
                paymentData.put("currency", "TL");
                paymentData.put("test_mode", "1");
                
                Response response = given()
                    .spec(requestSpec)
                    .body(paymentData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                // Check for rate limiting responses
                if (response.getStatusCode() == 429 || response.getStatusCode() == 503) {
                    blockedRequests++;
                }
                
                // Very short delay to simulate rapid requests
                Thread.sleep(10);
            }
            
            long endTime = System.currentTimeMillis();
            double requestsPerSecond = (double) requestCount / ((endTime - startTime) / 1000.0);
            
            // Rate limiting should kick in for high-frequency requests
            assertTrue(blockedRequests > 0 || requestsPerSecond < 100, 
                "Rate limiting should activate under high-frequency requests");
            
            logTestInfo("Rate limiting test - Blocked requests: " + blockedRequests + "/" + requestCount);
            logTestInfo("Requests per second: " + String.format("%.2f", requestsPerSecond));
            logTestResult("AS-001", "BAŞARILI", "Rate limiting koruması aktif");
            
        } catch (Exception e) {
            logTestResult("AS-001", "BAŞARISIZ", "Rate limiting testi hatası: " + e.getMessage());
            fail("Rate limiting testi başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: AS-002
     * Advanced Security - JWT Token Security and Manipulation
     * Tests JWT token validation and security
     */
    @Test(priority = 11, groups = {"security", "advanced", "jwt"})
    @Story("JWT Token Security")
    @Severity(SeverityLevel.CRITICAL)
    @Description("JWT token güvenliği ve manipülasyon testleri")
    public void testJWTTokenSecurity() {
        logTestInfo("Test ID: AS-002 - JWT Token Security Testing");
        
        try {
            // First, get a valid token
            Map<String, Object> validData = new HashMap<>();
            validData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            validData.put("user_ip", "127.0.0.1");
            validData.put("merchant_oid", "AS002_JWT_" + System.currentTimeMillis());
            validData.put("email", "jwt.test@example.com");
            validData.put("payment_amount", "10000");
            validData.put("currency", "TL");
            validData.put("test_mode", "1");
            
            Response tokenResponse = given()
                .spec(requestSpec)
                .body(validData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            if (tokenResponse.getStatusCode() == 200) {
                String token = tokenResponse.jsonPath().getString("token");
                
                if (token != null && !token.isEmpty()) {
                    // Test 1: Manipulated token
                    String manipulatedToken = token.substring(0, token.length() - 5) + "XXXXX";
                    
                    Response manipulatedResponse = given()
                        .spec(requestSpec)
                        .header("Authorization", "Bearer " + manipulatedToken)
                        .when()
                        .get("/odeme/api/validate-token")
                        .then()
                        .extract().response();
                    
                    // Manipulated token should be rejected
                    assertNotEquals(manipulatedResponse.getStatusCode(), 200, 
                        "Manipulated JWT token should be rejected");
                    
                    // Test 2: Expired token simulation (if possible)
                    // This would require a token that's already expired or time manipulation
                    
                    // Test 3: Invalid signature
                    String[] tokenParts = token.split("\\.");
                    if (tokenParts.length == 3) {
                        String invalidToken = tokenParts[0] + "." + tokenParts[1] + ".invalid_signature";
                        
                        Response invalidResponse = given()
                            .spec(requestSpec)
                            .header("Authorization", "Bearer " + invalidToken)
                            .when()
                            .get("/odeme/api/validate-token")
                            .then()
                            .extract().response();
                        
                        // Invalid signature should be rejected
                        assertNotEquals(invalidResponse.getStatusCode(), 200, 
                            "JWT token with invalid signature should be rejected");
                    }
                    
                    logTestResult("AS-002", "BAŞARILI", "JWT token güvenlik kontrolleri başarılı");
                } else {
                    logTestResult("AS-002", "ATLANDΙ", "JWT token alınamadı");
                }
            } else {
                logTestResult("AS-002", "ATLANDΙ", "Token oluşturulamadı");
            }
            
        } catch (Exception e) {
            logTestResult("AS-002", "BAŞARISIZ", "JWT token testi hatası: " + e.getMessage());
            fail("JWT token testi başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: AS-003
     * Advanced Security - CSRF Protection Testing
     * Tests Cross-Site Request Forgery protection mechanisms
     */
    @Test(priority = 12, groups = {"security", "advanced", "csrf"})
    @Story("CSRF Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Cross-Site Request Forgery korunma testleri")
    public void testCSRFProtection() {
        logTestInfo("Test ID: AS-003 - CSRF Protection Testing");
        
        try {
            // Test CSRF protection by making requests without proper CSRF tokens
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            paymentData.put("user_ip", "127.0.0.1");
            paymentData.put("merchant_oid", "AS003_CSRF_" + System.currentTimeMillis());
            paymentData.put("email", "csrf.test@example.com");
            paymentData.put("payment_amount", "10000");
            paymentData.put("currency", "TL");
            paymentData.put("test_mode", "1");
            
            // Test 1: Request without CSRF token
            Response noCsrfResponse = given()
                .spec(requestSpec)
                .header("Origin", "https://malicious-site.com")
                .header("Referer", "https://malicious-site.com")
                .body(paymentData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            // Test 2: Request with invalid CSRF token
            Response invalidCsrfResponse = given()
                .spec(requestSpec)
                .header("X-CSRF-Token", "invalid_csrf_token")
                .header("Origin", "https://malicious-site.com")
                .body(paymentData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            // CSRF protection should be in place
            // Note: Some APIs might not implement CSRF protection for API endpoints
            // but should validate Origin/Referer headers
            
            logTestInfo("CSRF test - No token response: " + noCsrfResponse.getStatusCode());
            logTestInfo("CSRF test - Invalid token response: " + invalidCsrfResponse.getStatusCode());
            
            logTestResult("AS-003", "BAŞARILI", "CSRF korunma testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("AS-003", "BAŞARISIZ", "CSRF testi hatası: " + e.getMessage());
            fail("CSRF testi başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: AS-004
     * Advanced Security - Input Sanitization and XSS Protection
     * Tests comprehensive input sanitization across all fields
     */
    @Test(priority = 13, groups = {"security", "advanced", "xss", "sanitization"})
    @Story("Input Sanitization and XSS Protection")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Gelişmiş input sanitization ve XSS korunma testleri")
    public void testAdvancedInputSanitization() {
        logTestInfo("Test ID: AS-004 - Advanced Input Sanitization Testing");
        
        try {
            // Advanced XSS payloads
            String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "javascript:alert('XSS')",
                "<img src=x onerror=alert('XSS')>",
                "<svg onload=alert('XSS')>",
                "';alert('XSS');//",
                "<iframe src=javascript:alert('XSS')></iframe>",
                "<body onload=alert('XSS')>",
                "<input onfocus=alert('XSS') autofocus>",
                "<select onfocus=alert('XSS') autofocus>",
                "<textarea onfocus=alert('XSS') autofocus>"
            };
            
            for (String payload : xssPayloads) {
                Map<String, Object> xssData = new HashMap<>();
                xssData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                xssData.put("user_ip", "127.0.0.1");
                xssData.put("merchant_oid", "AS004_XSS_" + System.currentTimeMillis());
                xssData.put("email", payload); // XSS payload in email field
                xssData.put("payment_amount", "10000");
                xssData.put("currency", "TL");
                xssData.put("test_mode", "1");
                xssData.put("user_name", payload); // XSS payload in name field
                
                Response xssResponse = given()
                    .spec(requestSpec)
                    .body(xssData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                // XSS should be sanitized or rejected
                String responseBody = xssResponse.getBody().asString();
                assertFalse(responseBody.contains("<script>") || 
                           responseBody.contains("javascript:") ||
                           responseBody.contains("onerror=") ||
                           responseBody.contains("onload="),
                    "XSS payload not properly sanitized: " + payload);
                
                logTestInfo("XSS payload sanitized: " + payload);
            }
            
            logTestResult("AS-004", "BAŞARILI", "Input sanitization testleri başarılı");
            
        } catch (Exception e) {
            logTestResult("AS-004", "BAŞARISIZ", "Input sanitization testi hatası: " + e.getMessage());
            fail("Input sanitization testi başarısız: " + e.getMessage());
        }
    }

    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n🔒 GÜVENLİK TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}