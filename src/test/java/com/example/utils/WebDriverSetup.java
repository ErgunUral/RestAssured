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
        // Eğer driver zaten varsa, önce temizle
        if (driver.get() != null) {
            quitDriver();
        }
        
        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-web-security");
                chromeOptions.addArguments("--allow-running-insecure-content");
                // JavaScript'i aktif bırak (PayTR için gerekli)
                // chromeOptions.addArguments("--disable-javascript"); // Kaldırıldı
                driver.set(new ChromeDriver(chromeOptions));
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
        if (currentDriver == null) {
            // Eğer driver null ise, yeni bir driver oluştur
            setupDriver("chrome");
            currentDriver = driver.get();
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