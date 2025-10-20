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
import java.time.Duration;

public class WebDriverSetup {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    public static synchronized void setupDriver(String browserName) {
        System.out.println("🔧 WebDriverSetup.setupDriver çağrıldı: " + browserName);
        
        if (driver.get() != null) {
            System.out.println("⚠️ Driver zaten mevcut, kapatılıyor...");
            quitDriver();
        }
        
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                switch (browserName.toLowerCase()) {
                    case "chrome":
                        System.out.println("🚀 Chrome driver kuruluyor... (Deneme: " + (attempts + 1) + ")");
                        
                        // CI/CD environment detection
                        boolean isCIEnvironment = System.getenv("CI") != null || 
                                                System.getenv("GITHUB_ACTIONS") != null ||
                                                System.getProperty("ci.mode") != null;
                        
                        if (isCIEnvironment) {
                            System.out.println("🔧 CI/CD ortamı tespit edildi, özel konfigürasyon uygulanıyor...");
                        }
                        
                        WebDriverManager.chromedriver().setup();
                        ChromeOptions chromeOptions = new ChromeOptions();
                        
                        // Basic headless configuration
                        chromeOptions.addArguments("--headless");
                        chromeOptions.addArguments("--window-size=1920,1080");
                        chromeOptions.addArguments("--disable-gpu");
                        chromeOptions.addArguments("--no-sandbox");
                        chromeOptions.addArguments("--disable-dev-shm-usage");
                        
                        // CI/CD specific optimizations
                        if (isCIEnvironment) {
                            chromeOptions.addArguments("--disable-background-timer-throttling");
                            chromeOptions.addArguments("--disable-backgrounding-occluded-windows");
                            chromeOptions.addArguments("--disable-renderer-backgrounding");
                            chromeOptions.addArguments("--disable-features=TranslateUI");
                            chromeOptions.addArguments("--disable-ipc-flooding-protection");
                            chromeOptions.addArguments("--disable-background-networking");
                            chromeOptions.addArguments("--disable-sync");
                            chromeOptions.addArguments("--disable-default-apps");
                            chromeOptions.addArguments("--disable-extensions-file-access-check");
                            chromeOptions.addArguments("--disable-extensions-http-throttling");
                            chromeOptions.addArguments("--aggressive-cache-discard");
                            chromeOptions.addArguments("--memory-pressure-off");
                        }
                        
                        // Standard security and stability options
                        chromeOptions.addArguments("--disable-extensions");
                        chromeOptions.addArguments("--disable-web-security");
                        chromeOptions.addArguments("--allow-running-insecure-content");
                        chromeOptions.addArguments("--ignore-certificate-errors");
                        chromeOptions.addArguments("--ignore-ssl-errors");
                        chromeOptions.addArguments("--remote-debugging-port=0"); // Use random port
                        
                        WebDriver chromeDriver = new ChromeDriver(chromeOptions);
                        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                        chromeDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
                        chromeDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
                        
                        driver.set(chromeDriver);
                        System.out.println("✅ Chrome driver başarıyla kuruldu");
                        return;
                        
                    case "firefox":
                        WebDriverManager.firefoxdriver().setup();
                        FirefoxOptions firefoxOptions = new FirefoxOptions();
                        firefoxOptions.addArguments("--headless");
                        firefoxOptions.addArguments("--width=1920");
                        firefoxOptions.addArguments("--height=1080");
                        
                        WebDriver firefoxDriver = new FirefoxDriver(firefoxOptions);
                        firefoxDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                        firefoxDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
                        
                        driver.set(firefoxDriver);
                        break;
                        
                    case "edge":
                        WebDriverManager.edgedriver().setup();
                        EdgeOptions edgeOptions = new EdgeOptions();
                        edgeOptions.addArguments("--headless");
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
                return; // Success, exit retry loop
                
            } catch (Exception e) {
                attempts++;
                System.out.println("❌ WebDriver kurulum hatası (Deneme " + attempts + "): " + e.getMessage());
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new RuntimeException("WebDriver kurulumu " + MAX_RETRY_ATTEMPTS + " denemeden sonra başarısız: " + e.getMessage(), e);
                }
                
                // Wait before retry
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("WebDriver kurulum retry interrupted", ie);
                }
            }
        }
    }
    
    public static synchronized WebDriver getDriver() {
        WebDriver currentDriver = driver.get();
        System.out.println("🔍 WebDriverSetup.getDriver çağrıldı, driver: " + (currentDriver != null ? "MEVCUT" : "NULL"));
        
        if (currentDriver == null) {
            System.out.println("⚠️ Driver null, yeni driver oluşturuluyor...");
            setupDriver("chrome");
            currentDriver = driver.get();
            System.out.println("🔄 Yeni driver oluşturuldu: " + (currentDriver != null ? "BAŞARILI" : "BAŞARISIZ"));
        }
        
        // Verify driver is still responsive
        try {
            currentDriver.getCurrentUrl();
        } catch (Exception e) {
            System.out.println("⚠️ Driver unresponsive, recreating...");
            quitDriver();
            setupDriver("chrome");
            currentDriver = driver.get();
        }
        
        return currentDriver;
    }
    
    public static synchronized void quitDriver() {
        try {
            if (driver.get() != null) {
                System.out.println("🔄 WebDriver kapatılıyor...");
                driver.get().quit();
                driver.remove();
                System.out.println("✅ WebDriver başarıyla kapatıldı");
            }
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver kapatma hatası: " + e.getMessage());
            driver.remove(); // Thread'den kaldır
        }
    }
    
    public static void closeDriver() {
        try {
            if (driver.get() != null) {
                driver.get().close();
            }
        } catch (Exception e) {
            System.out.println("⚠️ WebDriver close hatası: " + e.getMessage());
        }
    }
}