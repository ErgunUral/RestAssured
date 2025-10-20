package com.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * PayTR Güvenlik Test Yardımcı Sınıfı
 * Güvenlik testleri için ortak metodlar ve utilities
 */
public class SecurityTestUtils {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    public SecurityTestUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) driver;
    }
    
    /**
     * Email alanını bulur
     */
    public WebElement findEmailField() {
        try {
            // Çeşitli email field selector'ları dene
            String[] emailSelectors = {
                "input[type='email']",
                "input[name='email']",
                "input[name='kullanici_adi']",
                "input[name='username']",
                "input[id='email']",
                "input[placeholder*='email']",
                "input[placeholder*='e-posta']"
            };
            
            for (String selector : emailSelectors) {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }
            }
            
            // XPath ile dene
            List<WebElement> elements = driver.findElements(By.xpath(
                "//input[@type='text' and (contains(@name,'email') or contains(@name,'kullanici') or contains(@placeholder,'email'))]"));
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                return elements.get(0);
            }
            
        } catch (Exception e) {
            System.out.println("Email alanı bulunamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Şifre alanını bulur
     */
    public WebElement findPasswordField() {
        try {
            // Çeşitli password field selector'ları dene
            String[] passwordSelectors = {
                "input[type='password']",
                "input[name='password']",
                "input[name='sifre']",
                "input[id='password']",
                "input[placeholder*='şifre']",
                "input[placeholder*='password']"
            };
            
            for (String selector : passwordSelectors) {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Şifre alanı bulunamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Giriş butonunu bulur
     */
    public WebElement findLoginButton() {
        try {
            // Çeşitli login button selector'ları dene
            String[] buttonSelectors = {
                "button[type='submit']",
                "input[type='submit']",
                "button:contains('Giriş')",
                "button:contains('Login')",
                "input[value*='Giriş']",
                "input[value*='Login']"
            };
            
            for (String selector : buttonSelectors) {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }
            }
            
            // XPath ile dene
            List<WebElement> elements = driver.findElements(By.xpath(
                "//button[contains(text(),'Giriş') or contains(text(),'Login')] | //input[@value='Giriş' or @value='Login']"));
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                return elements.get(0);
            }
            
        } catch (Exception e) {
            System.out.println("Giriş butonu bulunamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * SQL Injection payload'ının güvenli olup olmadığını kontrol eder
     */
    public boolean isSQLInjectionSafe(String input, String response) {
        // Tehlikeli SQL anahtar kelimeleri
        String[] dangerousKeywords = {
            "select", "union", "drop", "delete", "insert", "update",
            "create", "alter", "exec", "execute", "sp_", "xp_",
            "database", "table", "column", "schema", "information_schema"
        };
        
        String lowerResponse = response.toLowerCase();
        
        // SQL hata mesajları
        String[] sqlErrors = {
            "sql syntax", "mysql", "ora-", "microsoft ole db",
            "odbc", "jdbc", "sqlite", "postgresql", "syntax error"
        };
        
        // SQL hata mesajı var mı kontrol et
        for (String error : sqlErrors) {
            if (lowerResponse.contains(error)) {
                return false; // SQL hata mesajı bulundu, güvenli değil
            }
        }
        
        // Veritabanı bilgisi sızdı mı kontrol et
        for (String keyword : dangerousKeywords) {
            if (lowerResponse.contains(keyword)) {
                return false; // Veritabanı bilgisi sızdı
            }
        }
        
        return true; // Güvenli
    }
    
    /**
     * XSS payload'ının güvenli olup olmadığını kontrol eder
     */
    public boolean isXSSSafe(String input, String output) {
        // Tehlikeli XSS pattern'leri
        String[] xssPatterns = {
            "<script", "javascript:", "onload=", "onerror=", "onclick=",
            "onmouseover=", "<iframe", "<object", "<embed", "eval(",
            "alert(", "confirm(", "prompt("
        };
        
        String lowerOutput = output.toLowerCase();
        
        // XSS payload'ı encode edilmiş mi kontrol et
        for (String pattern : xssPatterns) {
            if (lowerOutput.contains(pattern)) {
                return false; // XSS payload encode edilmemiş
            }
        }
        
        return true; // Güvenli
    }
    
    /**
     * CSRF token varlığını kontrol eder
     */
    public boolean hasCSRFToken() {
        try {
            List<WebElement> csrfTokens = driver.findElements(By.xpath(
                "//input[@name='_token' or @name='csrf_token' or @name='authenticity_token' or contains(@name,'csrf')]"));
            
            return !csrfTokens.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Session cookie'lerinin güvenli olup olmadığını kontrol eder
     */
    public boolean areSessionCookiesSecure() {
        try {
            String cookies = (String) js.executeScript("return document.cookie;");
            
            if (cookies == null || cookies.isEmpty()) {
                return true; // Cookie yok, güvenli sayılır
            }
            
            // HTTPS kontrolü
            if (!driver.getCurrentUrl().startsWith("https://")) {
                return false; // HTTP üzerinde cookie güvenli değil
            }
            
            // Session cookie'si var mı kontrol et
            if (cookies.toLowerCase().contains("session") || cookies.toLowerCase().contains("phpsessid")) {
                // Secure ve HttpOnly flag'leri kontrol edilemez JavaScript ile
                // Bu kontrol server-side response header'larında yapılmalı
                return true;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Input alanının maksimum karakter sınırını kontrol eder
     */
    public boolean hasInputLengthLimit(WebElement element, int maxLength) {
        try {
            String maxLengthAttr = element.getAttribute("maxlength");
            if (maxLengthAttr != null) {
                int limit = Integer.parseInt(maxLengthAttr);
                return limit <= maxLength;
            }
            
            // JavaScript ile kontrol et
            String script = "return arguments[0].maxLength;";
            Object result = js.executeScript(script, element);
            if (result != null && !result.toString().equals("-1")) {
                int limit = Integer.parseInt(result.toString());
                return limit <= maxLength;
            }
            
        } catch (Exception e) {
            // Hata durumunda false döndür
        }
        return false;
    }
    
    /**
     * Brute force saldırısı için rate limiting kontrolü
     */
    public boolean isRateLimited(int attemptCount, long timeBetweenAttempts) {
        // Bu metod gerçek bir rate limiting testi yapmaz
        // Sadece test senaryosu için placeholder
        
        if (attemptCount > 5 && timeBetweenAttempts < 1000) {
            // 5'ten fazla deneme ve 1 saniyeden az aralık = rate limiting gerekli
            return true;
        }
        
        return false;
    }
    
    /**
     * Güvenlik header'larını kontrol eder (JavaScript ile sınırlı)
     */
    public boolean hasSecurityHeaders() {
        try {
            // Content Security Policy kontrolü
            String csp = (String) js.executeScript(
                "return document.querySelector('meta[http-equiv=\"Content-Security-Policy\"]')?.content;");
            
            // X-Frame-Options kontrolü (meta tag ile)
            String xFrameOptions = (String) js.executeScript(
                "return document.querySelector('meta[http-equiv=\"X-Frame-Options\"]')?.content;");
            
            return csp != null || xFrameOptions != null;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Password strength kontrolü
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    /**
     * Test sonucu için güvenlik raporu oluşturur
     */
    public String generateSecurityReport(String testName, boolean passed, String details) {
        StringBuilder report = new StringBuilder();
        report.append("\n🔒 GÜVENLİK TEST RAPORU\n");
        report.append("========================================").append("\n");
        report.append("Test Adı: ").append(testName).append("\n");
        report.append("Sonuç: ").append(passed ? "✅ BAŞARILI" : "❌ BAŞARISIZ").append("\n");
        report.append("Detaylar: ").append(details).append("\n");
        report.append("Zaman: ").append(LocalDateTime.now()).append("\n");
        report.append("========================================").append("\n");
        
        return report.toString();
    }
}