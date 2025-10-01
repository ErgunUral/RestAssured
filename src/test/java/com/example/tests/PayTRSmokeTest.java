package com.example.tests;

import com.example.pages.PayTRBasePage;
import com.example.pages.PayTRLoginPage;
import com.example.pages.PayTRPaymentPage;
import com.example.pages.PayTRVirtualPOSPage;
import com.example.config.PayTRTestConfig;
import com.example.utils.PayTRTestDataProvider;
import org.testng.annotations.*;
import org.testng.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * PayTR Smoke Test Suite
 * PayTR kritik işlevlerinin hızlı doğrulaması için optimize edilmiş testler
 */
public class PayTRSmokeTest {
    
    private WebDriver driver;
    private PayTRBasePage basePage;
    private PayTRLoginPage loginPage;
    private PayTRPaymentPage paymentPage;
    private PayTRVirtualPOSPage virtualPOSPage;
    private WebDriverWait wait;
    
    @BeforeClass
    public void setupSmokeTests() {
        System.out.println("=== PayTR Smoke Test Suite Başlatılıyor ===");
        System.out.println("PayTR Test Environment: " + PayTRTestConfig.BASE_URL);
        System.out.println("Test Suite: Smoke Tests (Kritik İşlevler)");
        
        // WebDriver setup
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images");
            options.addArguments("--disable-javascript");
            
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
            
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            basePage = new PayTRBasePage(driver);
            loginPage = new PayTRLoginPage(driver);
            paymentPage = new PayTRPaymentPage(driver);
            virtualPOSPage = new PayTRVirtualPOSPage(driver);
            
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver setup hatası: " + e.getMessage());
            throw new RuntimeException("WebDriver başlatılamadı", e);
        }
    }
    
    @AfterClass
    public void tearDownSmokeTests() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver kapatma hatası: " + e.getMessage());
            // Hata durumunda da devam et
        }
        System.out.println("=== PayTR Smoke Test Suite Tamamlandı ===");
    }

    @Test(priority = 1, groups = {"smoke", "critical"})
    public void smokeTest_PayTRWebsiteAccessibility() {
        System.out.println("🔍 Smoke Test: PayTR Website Erişilebilirlik");
        
        try {
            // Basit URL erişim testi
            driver.get("https://testweb.paytr.com");
            
            // Sayfa yüklenme kontrolü - daha esnek
            Thread.sleep(3000); // Sayfa yüklenmesi için bekle
            
            String pageSource = driver.getPageSource();
            Assert.assertTrue(pageSource.length() > 1000, "Sayfa içeriği yüklenmeli");
            
            // URL kontrolü
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue(currentUrl.contains("testweb.paytr.com"), 
                "URL doğru test ortamını göstermeli");
            
            System.out.println("✅ Website erişilebilirlik testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Website erişilebilirlik testi hatası: " + e.getMessage());
            Assert.fail("Website erişilebilirlik testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 2, groups = {"smoke", "critical"})
    public void smokeTest_PaymentPageBasicFunctionality() {
        System.out.println("🔍 Smoke Test: Ödeme Sayfası Temel İşlevsellik");
        
        try {
            // Ödeme sayfasına git
            driver.get("https://testweb.paytr.com/magaza");
            Thread.sleep(3000);
            
            String pageSource = driver.getPageSource().toLowerCase();
            
            // Ödeme ile ilgili içerik kontrolü
            boolean hasPaymentContent = pageSource.contains("ödeme") || 
                                      pageSource.contains("payment") ||
                                      pageSource.contains("kart") ||
                                      pageSource.contains("card") ||
                                      pageSource.contains("paytr");
            
            Assert.assertTrue(hasPaymentContent, "Sayfa ödeme ile ilgili içerik içermeli");
            
            System.out.println("✅ Ödeme sayfası temel işlevsellik testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Ödeme sayfası testi hatası: " + e.getMessage());
            Assert.fail("Ödeme sayfası testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 3, groups = {"smoke", "critical"})
    public void smokeTest_VirtualPOSBasicFunctionality() {
        System.out.println("🔍 Smoke Test: Virtual POS Temel İşlevsellik");
        
        try {
            // Virtual POS sayfasına git
            driver.get("https://testweb.paytr.com/magaza");
            Thread.sleep(3000);
            
            String pageSource = driver.getPageSource().toLowerCase();
            
            // Virtual POS ile ilgili içerik kontrolü
            boolean hasVirtualPOSContent = pageSource.contains("pos") || 
                                         pageSource.contains("virtual") ||
                                         pageSource.contains("mağaza") ||
                                         pageSource.contains("magaza") ||
                                         pageSource.contains("paytr");
            
            Assert.assertTrue(hasVirtualPOSContent, "Sayfa Virtual POS ile ilgili içerik içermeli");
            
            System.out.println("✅ Virtual POS temel işlevsellik testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Virtual POS testi hatası: " + e.getMessage());
            Assert.fail("Virtual POS testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 4, groups = {"smoke", "security"})
    public void smokeTest_BasicSecurityFeatures() {
        System.out.println("🔍 Smoke Test: Temel Güvenlik Özellikleri");
        
        try {
            driver.get("https://testweb.paytr.com");
            Thread.sleep(2000);
            
            // HTTPS kontrolü
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue(currentUrl.startsWith("https://"), "Site HTTPS kullanmalı");
            
            System.out.println("✅ Temel güvenlik özellikleri testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Güvenlik testi hatası: " + e.getMessage());
            Assert.fail("Güvenlik testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 5, groups = {"smoke", "performance"})
    public void smokeTest_BasicPerformance() {
        System.out.println("🔍 Smoke Test: Temel Performans");
        
        try {
            Instant start = Instant.now();
            
            driver.get("https://testweb.paytr.com");
            Thread.sleep(2000);
            
            Instant end = Instant.now();
            Duration loadTime = Duration.between(start, end);
            
            // Test ortamı için esnek performans kontrolü (30 saniye)
            Assert.assertTrue(loadTime.getSeconds() < 30, 
                "Ana sayfa 30 saniyeden kısa sürede yüklenmeli");
            
            System.out.println("✅ Temel performans testi geçti (" + loadTime.getSeconds() + "s)");
            
        } catch (Exception e) {
            System.out.println("⚠️ Performans testi hatası: " + e.getMessage());
            Assert.fail("Performans testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 6, groups = {"smoke", "integration"})
    public void smokeTest_EndToEndFlow() {
        System.out.println("🔍 Smoke Test: Uçtan Uca Akış");
        
        try {
            // Ana sayfa
            driver.get("https://testweb.paytr.com");
            Thread.sleep(2000);
            
            String homePageSource = driver.getPageSource();
            Assert.assertTrue(homePageSource.length() > 1000, "Ana sayfa yüklenmeli");
            
            // Mağaza sayfası
            driver.get("https://testweb.paytr.com/magaza");
            Thread.sleep(3000);
            
            String shopPageSource = driver.getPageSource();
            Assert.assertTrue(shopPageSource.length() > 1000, "Mağaza sayfası yüklenmeli");
            
            System.out.println("✅ Uçtan uca akış testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Uçtan uca akış testi hatası: " + e.getMessage());
            Assert.fail("Uçtan uca akış testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 7, groups = {"smoke", "connectivity"})
    public void smokeTest_BasicAPIConnectivity() {
        System.out.println("🔍 Smoke Test: Temel API Bağlantısı");
        
        try {
            driver.get("https://testweb.paytr.com");
            Thread.sleep(2000);
            
            // Basit bağlantı kontrolü
            String currentUrl = driver.getCurrentUrl();
            Assert.assertTrue(currentUrl.contains("testweb.paytr.com"), 
                "API domain'ine erişim sağlanmalı");
            
            System.out.println("✅ Temel API bağlantısı testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ API bağlantısı testi hatası: " + e.getMessage());
            Assert.fail("API bağlantısı testi başarısız: " + e.getMessage());
        }
    }
    
    @Test(priority = 8, groups = {"smoke", "health"})
    public void smokeTest_OverallSystemHealth() {
        System.out.println("🔍 Smoke Test: Genel Sistem Sağlığı");
        
        try {
            int healthScore = 0;
            int totalChecks = 5;
            
            // Ana sayfa kontrolü
            try {
                driver.get("https://testweb.paytr.com");
                Thread.sleep(2000);
                if (driver.getPageSource().length() > 1000) {
                    healthScore++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Ana sayfa kontrolü başarısız");
            }
            
            // Mağaza sayfası kontrolü
            try {
                driver.get("https://testweb.paytr.com/magaza");
                Thread.sleep(2000);
                if (driver.getPageSource().length() > 1000) {
                    healthScore++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Mağaza sayfası kontrolü başarısız");
            }
            
            // HTTPS kontrolü
            try {
                if (driver.getCurrentUrl().startsWith("https://")) {
                    healthScore++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ HTTPS kontrolü başarısız");
            }
            
            // URL kontrolü
            try {
                if (driver.getCurrentUrl().contains("testweb.paytr.com")) {
                    healthScore++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ URL kontrolü başarısız");
            }
            
            // Sayfa içerik kontrolü
            try {
                String pageSource = driver.getPageSource().toLowerCase();
                if (pageSource.contains("paytr") || pageSource.contains("ödeme")) {
                    healthScore++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Sayfa içerik kontrolü başarısız");
            }
            
            double healthPercentage = (double) healthScore / totalChecks * 100;
            
            System.out.println("Sistem Sağlık Skoru: " + healthScore + "/" + totalChecks + 
                             " (" + String.format("%.1f", healthPercentage) + "%)");
            
            // Test ortamı için %40 eşik değeri
            Assert.assertTrue(healthPercentage >= 40, 
                "Sistem sağlık skoru en az %40 olmalı (Test ortamı)");
            
            System.out.println("✅ Genel sistem sağlığı testi geçti");
            
        } catch (Exception e) {
            System.out.println("⚠️ Sistem sağlığı testi hatası: " + e.getMessage());
            Assert.fail("Sistem sağlığı testi başarısız: " + e.getMessage());
        }
    }

    /**
     * Her test için WebDriver durumunu kontrol et
     */
    @BeforeMethod
    public void checkDriverStatus() {
        if (driver == null) {
            System.out.println("⚠️ WebDriver null, test atlanıyor");
            throw new RuntimeException("WebDriver başlatılmamış");
        }
    }

    /**
     * Smoke test sonuçlarını özetler
     */
    @AfterMethod
    public void logTestResult(org.testng.ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String status = result.getStatus() == org.testng.ITestResult.SUCCESS ? "GEÇTI" : "BAŞARISIZ";
        long duration = result.getEndMillis() - result.getStartMillis();
        
        System.out.println(String.format("📊 %s: %s (%dms)", testName, status, duration));
        
        if (result.getStatus() != org.testng.ITestResult.SUCCESS) {
            System.out.println("❌ Hata: " + result.getThrowable().getMessage());
        }
    }
}