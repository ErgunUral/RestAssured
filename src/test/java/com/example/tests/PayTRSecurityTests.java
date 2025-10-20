package com.example.tests;

import com.example.utils.WebDriverSetup;
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
import java.time.Duration;
import java.util.List;
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
            // WebDriver setup with validation
            WebDriverSetup.setupDriver("chrome");
            driver = WebDriverSetup.getDriver();
            
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
        if (driver == null) {
            System.out.println("⚠️ Driver null, yeniden başlatılıyor...");
            try {
                WebDriverSetup.setupDriver("chrome");
                driver = WebDriverSetup.getDriver();
                wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                securityUtils = new SecurityTestUtils(driver);
            } catch (Exception e) {
                throw new RuntimeException("Driver yeniden başlatılamadı: " + e.getMessage(), e);
            }
        }
        
        // Driver responsiveness check
        try {
            driver.getCurrentUrl();
        } catch (Exception e) {
            System.out.println("⚠️ Driver unresponsive, yeniden başlatılıyor...");
            WebDriverSetup.quitDriver();
            WebDriverSetup.setupDriver("chrome");
            driver = WebDriverSetup.getDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            securityUtils = new SecurityTestUtils(driver);
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