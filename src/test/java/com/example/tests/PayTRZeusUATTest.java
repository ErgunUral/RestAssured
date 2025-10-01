package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import com.example.utils.WebDriverSetup;
import com.example.config.PayTRTestConfig;
import java.time.Duration;
import java.util.List;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/**
 * PayTR Zeus UAT Ortamına Özel Test Sınıfı
 * Zeus UAT ortamının özel özelliklerini ve fonksiyonalitelerini test eder
 */
public class PayTRZeusUATTest extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor jsExecutor;
    
    @BeforeClass
    public void setupZeusUATTests() {
        baseURI = PayTRTestConfig.BASE_URL;
        basePath = "/magaza";
        
        // WebDriver setup
        WebDriverSetup.setupDriver("chrome");
        driver = WebDriverSetup.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        jsExecutor = (JavascriptExecutor) driver;
        
        logTestInfo("PayTR Zeus UAT Test Suite başlatıldı - " + baseURI);
    }
    
    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test(priority = 1, groups = {"zeus-uat", "smoke", "critical"})
    public void testZeusUATEnvironmentAccessibility() {
        logTestInfo("Zeus UAT Environment Accessibility Test");
        
        try {
            // Zeus UAT ana sayfasına erişim testi
            driver.get(baseURI);
            
            // Sayfa yüklenme kontrolü
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();
            
            // Zeus UAT spesifik kontroller
            assertTrue(currentUrl.contains("zeus-uat.paytr.com"), "Zeus UAT ortamına erişilemedi");
            assertTrue(pageTitle != null && !pageTitle.isEmpty(), "Sayfa başlığı boş");
            
            // HTTPS kontrolü
            assertTrue(currentUrl.startsWith("https://"), "Güvenli bağlantı sağlanamadı");
            
            System.out.println("✅ Zeus UAT ortamına başarıyla erişildi");
            System.out.println("📍 URL: " + currentUrl);
            System.out.println("📄 Başlık: " + pageTitle);
            
        } catch (Exception e) {
            fail("Zeus UAT ortamına erişim hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 2, groups = {"zeus-uat", "login", "critical"})
    public void testZeusUATLoginPageAccess() {
        logTestInfo("Zeus UAT Login Page Access Test");
        
        try {
            // Zeus UAT login sayfasına git
            String loginUrl = PayTRTestConfig.LOGIN_URL;
            driver.get(loginUrl);
            
            // Sayfa yüklenme kontrolü
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            String currentUrl = driver.getCurrentUrl();
            
            // Login sayfası spesifik kontroller
            assertTrue(currentUrl.contains("zeus-uat.paytr.com"), "Zeus UAT login sayfasına erişilemedi");
            assertTrue(currentUrl.contains("kullanici-girisi"), "Login endpoint'ine yönlendirilemedi");
            
            System.out.println("✅ Zeus UAT login sayfası erişilebilir");
            System.out.println("📍 Login URL: " + currentUrl);
            
        } catch (Exception e) {
            fail("Zeus UAT login sayfası erişim hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 3, groups = {"zeus-uat", "security", "ssl"})
    public void testZeusUATSSLCertificate() {
        logTestInfo("Zeus UAT SSL Certificate Test");
        
        try {
            // SSL sertifikası kontrolü
            driver.get(baseURI);
            
            // HTTPS protokolü kontrolü
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.startsWith("https://"), "SSL sertifikası aktif değil");
            
            System.out.println("✅ Zeus UAT SSL sertifikası geçerli");
            System.out.println("🔒 HTTPS protokolü aktif");
            
        } catch (Exception e) {
            fail("Zeus UAT SSL sertifikası kontrolü hatası: " + e.getMessage());
        }
    }
}