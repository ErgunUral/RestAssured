package com.example.tests;

import com.example.utils.WebDriverSetup;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * WebDriver Setup Test Class
 * WebDriver initialization ve browser setup'ını test eder
 */
public class WebDriverSetupTest {
    
    @Test(priority = 1, groups = {"webdriver", "setup"})
    public void testChromeDriverSetup() {
        System.out.println("🧪 Chrome WebDriver Setup Test başlatılıyor...");
        
        try {
            // Chrome driver setup
            WebDriverSetup.setupDriver("chrome");
            WebDriver driver = WebDriverSetup.getDriver();
            
            // Driver'ın null olmadığını kontrol et
            Assert.assertNotNull(driver, "Chrome WebDriver null olmamalı");
            
            // Basit bir sayfa yükle
            driver.get("https://www.google.com");
            
            // Sayfa title'ını kontrol et
            String title = driver.getTitle();
            Assert.assertNotNull(title, "Sayfa title'ı null olmamalı");
            Assert.assertTrue(title.contains("Google"), "Sayfa title'ı 'Google' içermeli");
            
            System.out.println("✅ Chrome WebDriver başarıyla kuruldu ve test edildi");
            System.out.println("📄 Sayfa Title: " + title);
            
        } catch (Exception e) {
            System.out.println("❌ Chrome WebDriver setup hatası: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 2, groups = {"webdriver", "setup"})
    public void testFirefoxDriverSetup() {
        System.out.println("🧪 Firefox WebDriver Setup Test başlatılıyor...");
        
        try {
            // Firefox driver setup
            WebDriverSetup.setupDriver("firefox");
            WebDriver driver = WebDriverSetup.getDriver();
            
            // Driver'ın null olmadığını kontrol et
            Assert.assertNotNull(driver, "Firefox WebDriver null olmamalı");
            
            // Basit bir sayfa yükle
            driver.get("https://www.google.com");
            
            // Sayfa title'ını kontrol et
            String title = driver.getTitle();
            Assert.assertNotNull(title, "Sayfa title'ı null olmamalı");
            Assert.assertTrue(title.contains("Google"), "Sayfa title'ı 'Google' içermeli");
            
            System.out.println("✅ Firefox WebDriver başarıyla kuruldu ve test edildi");
            System.out.println("📄 Sayfa Title: " + title);
            
        } catch (Exception e) {
            System.out.println("❌ Firefox WebDriver setup hatası: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 3, groups = {"webdriver", "setup"})
    public void testEdgeDriverSetup() {
        System.out.println("🧪 Edge WebDriver Setup Test başlatılıyor...");
        
        try {
            // Edge driver setup
            WebDriverSetup.setupDriver("edge");
            WebDriver driver = WebDriverSetup.getDriver();
            
            // Driver'ın null olmadığını kontrol et
            Assert.assertNotNull(driver, "Edge WebDriver null olmamalı");
            
            // Basit bir sayfa yükle
            driver.get("https://www.google.com");
            
            // Sayfa title'ını kontrol et
            String title = driver.getTitle();
            Assert.assertNotNull(title, "Sayfa title'ı null olmamalı");
            Assert.assertTrue(title.contains("Google"), "Sayfa title'ı 'Google' içermeli");
            
            System.out.println("✅ Edge WebDriver başarıyla kuruldu ve test edildi");
            System.out.println("📄 Sayfa Title: " + title);
            
        } catch (Exception e) {
            System.out.println("❌ Edge WebDriver setup hatası: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 4, groups = {"webdriver", "error"})
    public void testUnsupportedBrowserError() {
        System.out.println("🧪 Unsupported Browser Error Test başlatılıyor...");
        
        try {
            // Desteklenmeyen browser ile test
            WebDriverSetup.setupDriver("unsupported");
            Assert.fail("Desteklenmeyen browser için exception fırlatılmalı");
            
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Beklenen hata yakalandı: " + e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Browser not supported"), 
                "Hata mesajı 'Browser not supported' içermeli");
        } catch (Exception e) {
            System.out.println("❌ Beklenmeyen hata: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 5, groups = {"webdriver", "threading"})
    public void testThreadLocalDriver() {
        System.out.println("🧪 ThreadLocal Driver Test başlatılıyor...");
        
        try {
            // İlk driver setup
            WebDriverSetup.setupDriver("chrome");
            WebDriver driver1 = WebDriverSetup.getDriver();
            Assert.assertNotNull(driver1, "İlk driver null olmamalı");
            
            // Aynı thread'de ikinci driver setup
            WebDriverSetup.setupDriver("chrome");
            WebDriver driver2 = WebDriverSetup.getDriver();
            Assert.assertNotNull(driver2, "İkinci driver null olmamalı");
            
            // Driver'ların farklı olduğunu kontrol et (yeni setup eski driver'ı kapatır)
            Assert.assertNotSame(driver1, driver2, "Yeni setup sonrası driver farklı olmalı");
            
            System.out.println("✅ ThreadLocal Driver testi başarılı");
            
        } catch (Exception e) {
            System.out.println("❌ ThreadLocal Driver test hatası: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterMethod
    public void tearDown() {
        try {
            WebDriverSetup.quitDriver();
            System.out.println("🧹 WebDriver temizlendi");
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver temizleme hatası: " + e.getMessage());
        }
    }
}