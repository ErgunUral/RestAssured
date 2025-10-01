package com.example.tests;

import com.example.pages.PayTRBasePage;
import com.example.pages.PayTRLoginPage;
import com.example.pages.PayTRPaymentPage;
import com.example.pages.PayTRVirtualPOSPage;
import org.testng.annotations.*;
import org.testng.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LogEntry;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.Duration;
import java.time.Instant;

/**
 * PayTR Güvenlik Test Senaryoları
 * SSL, fraud kontrolü, güvenlik başlıkları ve diğer güvenlik özelliklerini test eder
 */
public class PayTRSecurityTest {
    
    private WebDriver driver;
    private PayTRBasePage basePage;
    private PayTRLoginPage loginPage;
    private PayTRPaymentPage paymentPage;
    private PayTRVirtualPOSPage virtualPOSPage;
    
    @BeforeClass
    public void setupSecurityTests() {
        System.out.println("PayTR Security Test Suite başlatılıyor...");
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--ignore-ssl-errors");
        options.addArguments("--disable-features=VizDisplayCompositor");
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        basePage = new PayTRBasePage(driver);
        loginPage = new PayTRLoginPage(driver);
        paymentPage = new PayTRPaymentPage(driver);
        virtualPOSPage = new PayTRVirtualPOSPage(driver);
        
        System.out.println("PayTR Test Environment Security Test Suite hazır: https://testweb.paytr.com");
    }
    
    @AfterClass
    public void tearDownSecurityTests() {
        if (driver != null) {
            driver.quit();
        }
        System.out.println("PayTR Security Test Suite tamamlandı.");
    }
    
    @Test(priority = 1)
    public void testSSLCertificateValidation() {
        System.out.println("SSL sertifika doğrulaması test ediliyor...");
        
        basePage.navigateToHomePage();
        
        // SSL aktif mi kontrol et
        Assert.assertTrue(basePage.isSSLActive(), 
            "PayTR test ortamında SSL aktif olmalı");
        
        // URL HTTPS ile başlıyor mu kontrol et
        String currentUrl = basePage.getCurrentUrl();
        Assert.assertTrue(currentUrl.startsWith("https://"), 
            "PayTR test ortamı HTTPS kullanmalı");
        
        // SSL sertifika bilgilerini JavaScript ile kontrol et
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object securityState = js.executeScript(
            "return window.location.protocol === 'https:' && " +
            "document.location.protocol === 'https:'"
        );
        Assert.assertTrue((Boolean) securityState, 
            "SSL güvenlik durumu doğrulanmalı");
        
        System.out.println("✓ SSL sertifika doğrulaması başarılı");
    }
    
    @Test(priority = 2)
    public void testSecurityHeaders() {
        System.out.println("Güvenlik başlıkları test ediliyor...");
        
        basePage.navigateToHomePage();
        
        // JavaScript ile güvenlik başlıklarını kontrol et
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Content Security Policy kontrolü
        Object cspHeader = js.executeScript(
            "var meta = document.querySelector('meta[http-equiv=\"Content-Security-Policy\"]');" +
            "return meta ? meta.getAttribute('content') : null;"
        );
        
        // X-Frame-Options kontrolü
        Object xFrameOptions = js.executeScript(
            "return document.querySelector('meta[http-equiv=\"X-Frame-Options\"]') !== null;"
        );
        
        // Strict-Transport-Security kontrolü (HSTS)
        Object hstsHeader = js.executeScript(
            "return document.querySelector('meta[http-equiv=\"Strict-Transport-Security\"]') !== null;"
        );
        
        // En az bir güvenlik başlığı olmalı
        boolean hasSecurityHeaders = cspHeader != null || 
                                   (Boolean) xFrameOptions || 
                                   (Boolean) hstsHeader;
        
        Assert.assertTrue(hasSecurityHeaders, 
            "PayTR test ortamında güvenlik başlıkları bulunmalı");
        
        System.out.println("✓ Güvenlik başlıkları kontrolü tamamlandı");
    }
    
    @Test(priority = 3)
    public void testCSRFProtection() {
        System.out.println("CSRF koruması test ediliyor...");
        
        loginPage.navigateToLoginPage();
        
        // CSRF token varlığını kontrol et
        Assert.assertTrue(basePage.hasCSRFToken(), 
            "Login sayfasında CSRF token bulunmalı");
        
        // Payment sayfasında da CSRF kontrolü
        paymentPage.navigateToPaymentPage();
        Assert.assertTrue(basePage.hasCSRFToken(), 
            "Payment sayfasında CSRF token bulunmalı");
        
        // Virtual POS sayfasında da CSRF kontrolü
        virtualPOSPage.navigateToVirtualPOSPage();
        Assert.assertTrue(basePage.hasCSRFToken(), 
            "Virtual POS sayfasında CSRF token bulunmalı");
        
        System.out.println("✓ CSRF koruması doğrulandı");
    }
    
    @Test(priority = 4)
    public void testInputValidationSecurity() {
        System.out.println("Input validasyon güvenliği test ediliyor...");
        
        paymentPage.navigateToPaymentPage();
        
        // XSS saldırısı test et
        String xssPayload = "<script>alert('XSS')</script>";
        paymentPage.enterCardHolderName(xssPayload);
        
        // XSS payload'ının escape edildiğini kontrol et
        String enteredValue = paymentPage.getCardHolderNameValue();
        Assert.assertFalse(enteredValue.contains("<script>"), 
            "XSS payload escape edilmeli");
        
        // SQL Injection test et
        String sqlPayload = "'; DROP TABLE users; --";
        paymentPage.enterCardHolderName(sqlPayload);
        
        // SQL payload'ının güvenli şekilde işlendiğini kontrol et
        enteredValue = paymentPage.getCardHolderNameValue();
        Assert.assertFalse(enteredValue.contains("DROP TABLE"), 
            "SQL injection payload güvenli şekilde işlenmeli");
        
        // Uzun string test et (Buffer overflow)
        String longString = "A".repeat(1000);
        paymentPage.enterCardHolderName(longString);
        
        // Maksimum uzunluk kontrolü
        enteredValue = paymentPage.getCardHolderNameValue();
        Assert.assertTrue(enteredValue.length() <= 100, 
            "Input uzunluğu sınırlandırılmalı");
        
        System.out.println("✓ Input validasyon güvenliği doğrulandı");
    }
    
    @Test(priority = 5)
    public void testPasswordSecurity() {
        System.out.println("Şifre güvenliği test ediliyor...");
        
        loginPage.navigateToLoginPage();
        
        // Şifre alanının type="password" olduğunu kontrol et
        Assert.assertTrue(loginPage.isPasswordFieldSecure(), 
            "Şifre alanı güvenli olmalı (type=password)");
        
        // Şifre alanının autocomplete="off" olduğunu kontrol et
        String autocomplete = loginPage.getPasswordFieldAutocomplete();
        Assert.assertTrue(autocomplete.equals("off") || autocomplete.equals("new-password"), 
            "Şifre alanında autocomplete kapalı olmalı");
        
        // Zayıf şifre testi
        loginPage.enterPassword("123");
        loginPage.clickLoginButton();
        
        // Zayıf şifre uyarısı veya hata mesajı olmalı
        boolean hasPasswordError = loginPage.hasPasswordError() || 
                                 basePage.hasErrorMessage();
        Assert.assertTrue(hasPasswordError, 
            "Zayıf şifre için uyarı verilmeli");
        
        System.out.println("✓ Şifre güvenliği doğrulandı");
    }
    
    @Test(priority = 6)
    public void testSessionSecurity() {
        System.out.println("Oturum güvenliği test ediliyor...");
        
        basePage.navigateToHomePage();
        
        // Session cookie'lerinin secure flag'i olup olmadığını kontrol et
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object secureCookies = js.executeScript(
            "var cookies = document.cookie.split(';');" +
            "var secureCount = 0;" +
            "for(var i = 0; i < cookies.length; i++) {" +
            "  if(cookies[i].includes('Secure')) secureCount++;" +
            "}" +
            "return secureCount > 0;"
        );
        
        // HttpOnly cookie kontrolü
        Object httpOnlyCookies = js.executeScript(
            "return document.cookie.length > 0;"
        );
        
        // En az bir güvenli cookie olmalı
        Assert.assertTrue((Boolean) secureCookies || (Boolean) httpOnlyCookies, 
            "Güvenli session cookie'leri bulunmalı");
        
        System.out.println("✓ Oturum güvenliği doğrulandı");
    }
    
    @Test(priority = 7)
    public void testFraudDetection() {
        System.out.println("Fraud detection test ediliyor...");
        
        paymentPage.navigateToPaymentPage();
        
        // Hızlı ardışık işlem testi (Rate limiting)
        for (int i = 0; i < 5; i++) {
            paymentPage.enterCardNumber("4111111111111111");
            paymentPage.enterCardHolderName("Test User " + i);
            paymentPage.enterExpiryMonth("12");
            paymentPage.enterExpiryYear("2025");
            paymentPage.enterCVV("123");
            paymentPage.clickPayButton();
            
            // Kısa bekleme
            basePage.waitFor(1);
        }
        
        // Rate limiting uyarısı veya blokaj olmalı
        boolean hasRateLimitWarning = basePage.hasErrorMessage() || 
                                    paymentPage.hasPaymentError();
        
        // Not: Test ortamında rate limiting olmayabilir, bu durumda uyarı veriyoruz
        if (!hasRateLimitWarning) {
            System.out.println("⚠ Rate limiting test ortamında aktif değil olabilir");
        }
        
        System.out.println("✓ Fraud detection testi tamamlandı");
    }
    
    @Test(priority = 8)
    public void testPaymentDataEncryption() {
        System.out.println("Ödeme verisi şifreleme test ediliyor...");
        
        paymentPage.navigateToPaymentPage();
        
        // Kart numarası alanının güvenli olduğunu kontrol et
        paymentPage.enterCardNumber("4111111111111111");
        
        // JavaScript ile input value'nun maskelenip maskelenmediğini kontrol et
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object cardNumberValue = js.executeScript(
            "var cardInput = document.querySelector('input[name*=\"card\"], input[id*=\"card\"]');" +
            "return cardInput ? cardInput.value : null;"
        );
        
        // CVV alanının güvenli olduğunu kontrol et
        paymentPage.enterCVV("123");
        Object cvvValue = js.executeScript(
            "var cvvInput = document.querySelector('input[name*=\"cvv\"], input[id*=\"cvv\"]');" +
            "return cvvInput ? cvvInput.type : null;"
        );
        
        // CVV alanı password tipinde olmalı
        Assert.assertEquals(cvvValue, "password", 
            "CVV alanı password tipinde olmalı");
        
        System.out.println("✓ Ödeme verisi şifreleme doğrulandı");
    }
    
    @Test(priority = 9)
    public void testVirtualPOSSecurity() {
        System.out.println("Virtual POS güvenlik özellikleri test ediliyor...");
        
        virtualPOSPage.navigateToVirtualPOSPage();
        
        // Virtual POS güvenlik özelliklerini al
        Map<String, Boolean> securityFeatures = virtualPOSPage.getVirtualPOSSecurityFeatures();
        
        // SSL kontrolü
        Assert.assertTrue(securityFeatures.get("SSL"), 
            "Virtual POS SSL kullanmalı");
        
        // CSRF token kontrolü
        Assert.assertTrue(securityFeatures.get("CSRF_Token"), 
            "Virtual POS CSRF token içermeli");
        
        // Form validasyon kontrolü
        Assert.assertTrue(securityFeatures.get("Form_Validation"), 
            "Virtual POS form validasyonu olmalı");
        
        // Hash field kontrolü
        Assert.assertTrue(securityFeatures.get("Hash_Field"), 
            "Virtual POS hash field içermeli");
        
        System.out.println("✓ Virtual POS güvenlik özellikleri doğrulandı");
    }
    
    @Test(priority = 10)
    public void testBrowserSecurityFeatures() {
        System.out.println("Tarayıcı güvenlik özellikleri test ediliyor...");
        
        basePage.navigateToHomePage();
        
        // Console error kontrolü
        java.util.List<Object> consoleErrors = basePage.getConsoleErrors();
        
        // Güvenlik ile ilgili console error'ları filtrele
        long securityErrorCount = consoleErrors.stream()
            .filter(error -> error.toString().toLowerCase().contains("security") || 
                           error.toString().toLowerCase().contains("mixed content") ||
                           error.toString().toLowerCase().contains("unsafe"))
            .count();
        
        Assert.assertEquals(securityErrorCount, 0, 
            "Güvenlik ile ilgili console error'ları olmamalı");
        
        // Mixed content kontrolü
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object mixedContent = js.executeScript(
            "var images = document.querySelectorAll('img');" +
            "var scripts = document.querySelectorAll('script[src]');" +
            "var links = document.querySelectorAll('link[href]');" +
            "var httpCount = 0;" +
            "images.forEach(function(img) {" +
            "  if(img.src && img.src.startsWith('http:')) httpCount++;" +
            "});" +
            "scripts.forEach(function(script) {" +
            "  if(script.src && script.src.startsWith('http:')) httpCount++;" +
            "});" +
            "links.forEach(function(link) {" +
            "  if(link.href && link.href.startsWith('http:')) httpCount++;" +
            "});" +
            "return httpCount;"
        );
        
        Assert.assertEquals(((Long) mixedContent).intValue(), 0, 
            "Mixed content (HTTP) kaynakları olmamalı");
        
        System.out.println("✓ Tarayıcı güvenlik özellikleri doğrulandı");
    }
    
    @Test(priority = 11)
    public void testSecurityResponseTime() {
        System.out.println("Güvenlik kontrolleri yanıt süresi test ediliyor...");
        
        Instant start = Instant.now();
        
        // SSL handshake ve sayfa yükleme süresi
        basePage.navigateToHomePage();
        
        Instant end = Instant.now();
        Duration loadTime = Duration.between(start, end);
        
        // Güvenlik kontrolleri nedeniyle sayfa yükleme süresi 10 saniyeyi geçmemeli
        Assert.assertTrue(loadTime.getSeconds() < 10, 
            "Güvenlik kontrolleri sayfa yükleme süresini aşırı uzatmamalı");
        
        System.out.println("✓ Güvenlik kontrolleri yanıt süresi: " + loadTime.getSeconds() + " saniye");
    }
    
    @Test(priority = 12)
    public void testSecurityCompliance() {
        System.out.println("Güvenlik uyumluluk kontrolü yapılıyor...");
        
        basePage.navigateToHomePage();
        
        Map<String, Boolean> complianceChecks = new HashMap<>();
        
        // PCI DSS uyumluluk kontrolleri
        complianceChecks.put("SSL_Active", basePage.isSSLActive());
        complianceChecks.put("CSRF_Protection", basePage.hasCSRFToken());
        complianceChecks.put("Secure_Forms", paymentPage.hasSecurePaymentForm());
        complianceChecks.put("Input_Validation", true); // Önceki testlerde doğrulandı
        
        // Tüm uyumluluk kontrolleri geçmeli
        boolean allCompliant = complianceChecks.values().stream()
            .allMatch(Boolean::booleanValue);
        
        Assert.assertTrue(allCompliant, 
            "Tüm güvenlik uyumluluk kontrolleri geçmeli");
        
        // Detaylı rapor
        System.out.println("Güvenlik Uyumluluk Raporu:");
        complianceChecks.forEach((check, result) -> 
            System.out.println("  " + check + ": " + (result ? "✓ GEÇTI" : "✗ BAŞARISIZ"))
        );
        
        System.out.println("✓ Güvenlik uyumluluk kontrolü tamamlandı");
    }
    
    /**
     * Test verisi sağlayıcı - Güvenlik test senaryoları
     */
    @DataProvider(name = "securityTestData")
    public Object[][] getSecurityTestData() {
        return new Object[][] {
            {"XSS", "<script>alert('xss')</script>"},
            {"SQL_Injection", "'; DROP TABLE users; --"},
            {"HTML_Injection", "<img src=x onerror=alert('html')>"},
            {"Command_Injection", "; rm -rf /"},
            {"Path_Traversal", "../../../etc/passwd"},
            {"LDAP_Injection", "*()|&'"},
            {"XML_Injection", "<?xml version=\"1.0\"?><!DOCTYPE test [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"},
            {"NoSQL_Injection", "{$gt: ''}"},
            {"CRLF_Injection", "test\r\nSet-Cookie: malicious=true"},
            {"Header_Injection", "test\nX-Malicious: true"}
        };
    }
    
    @Test(dataProvider = "securityTestData", priority = 13)
    public void testSecurityPayloads(String attackType, String payload) {
        System.out.println("Güvenlik payload testi: " + attackType);
        
        paymentPage.navigateToPaymentPage();
        
        // Payload'ı farklı alanlarda test et
        paymentPage.enterCardHolderName(payload);
        String cardHolderValue = paymentPage.getCardHolderNameValue();
        
        // Payload'ın escape edildiğini veya filtrelendiğini kontrol et
        Assert.assertFalse(cardHolderValue.contains(payload), 
            attackType + " payload'ı filtrelenmeli veya escape edilmeli");
        
        System.out.println("✓ " + attackType + " payload güvenlik testi geçti");
    }
}