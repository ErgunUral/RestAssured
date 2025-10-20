package com.example.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;

public class WebDriverSetup {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    public static synchronized void setupDriver(String browserName) {
        System.out.println("🔧 WebDriverSetup.setupDriver çağrıldı: " + browserName);
        
        if (driver.get() != null) {
            System.out.println("⚠️ Driver zaten mevcut, kapatılıyor...");
            quitDriver();
        }
        
        switch (browserName.toLowerCase()) {
            case "chrome":
                System.out.println("🚀 Chrome driver kuruluyor...");
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                WebDriver chromeDriver = new ChromeDriver(chromeOptions);
                driver.set(chromeDriver);
                System.out.println("✅ Chrome driver başarıyla kuruldu");
                break;
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--width=1920");
                firefoxOptions.addArguments("--height=1080");
                driver.set(new FirefoxDriver(firefoxOptions));
                break;
                
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--no-sandbox");
                edgeOptions.addArguments("--disable-dev-shm-usage");
                edgeOptions.addArguments("--window-size=1920,1080");
                driver.set(new EdgeDriver(edgeOptions));
                break;
                
            case "safari":
                driver.set(new SafariDriver());
                break;
                
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserName);
        }
    }
    
    public static synchronized WebDriver getDriver() {
        WebDriver currentDriver = driver.get();
        System.out.println("🔍 WebDriverSetup.getDriver çağrıldı, driver: " + (currentDriver != null ? "MEVCUT" : "NULL"));
        
        if (currentDriver == null) {
            System.out.println("⚠️ Driver null, yeni driver oluşturuluyor...");
            // Eğer driver null ise, yeni bir driver oluştur
            setupDriver("chrome");
            currentDriver = driver.get();
            System.out.println("🔄 Yeni driver oluşturuldu: " + (currentDriver != null ? "BAŞARILI" : "BAŞARISIZ"));
        }
        return currentDriver;
    }
    
    public static synchronized void quitDriver() {
        try {
            if (driver.get() != null) {
                driver.get().quit();
                driver.remove();
            }
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver kapatma hatası: " + e.getMessage());
            driver.remove(); // Thread'den kaldır
        }
    }
    
    public static void closeDriver() {
        if (driver.get() != null) {
            driver.get().close();
        }
    }
}