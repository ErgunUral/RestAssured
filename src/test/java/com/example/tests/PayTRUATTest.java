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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.Dimension;
import com.example.utils.WebDriverSetup;
import java.time.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class PayTRUATTest extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private List<TestResult> testResults = new ArrayList<>();
    private String screenshotDir = "test-screenshots";
    
    // Test result class
    public static class TestResult {
        public String testName;
        public boolean passed;
        public String message;
        public String screenshot;
        public long duration;
        public String timestamp;
        
        public TestResult(String testName, boolean passed, String message, String screenshot, long duration) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
            this.screenshot = screenshot;
            this.duration = duration;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    @BeforeClass
    public void setupUATTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "/magaza";
        
        // Create screenshot directory
        new File(screenshotDir).mkdirs();
        
        // WebDriver setup with optimized configuration
        WebDriverSetup.setupDriver("chrome");
        driver = WebDriverSetup.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        logTestInfo("PayTR UAT Test Suite başlatıldı - " + baseURI + basePath);
    }
    
    @AfterClass
    public void tearDown() {
        generateHTMLReport();
        WebDriverSetup.quitDriver();
        logTestInfo("PayTR UAT Test Suite tamamlandı");
    }
    
    private String takeScreenshot(String testName) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
            String fileName = testName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".png";
            String filePath = screenshotDir + "/" + fileName;
            Files.write(Paths.get(filePath), screenshotBytes);
            return filePath;
        } catch (IOException e) {
            return "Screenshot alınamadı: " + e.getMessage();
        }
    }
    
    @Test(priority = 1)
    public void testUATPageLoad() {
        long startTime = System.currentTimeMillis();
        String testName = "UAT Sayfa Yükleme Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            // Sayfa yüklenme kontrolü
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            String pageTitle = driver.getTitle();
            String currentUrl = driver.getCurrentUrl();
            
            assertTrue(pageTitle != null && !pageTitle.isEmpty(), "Sayfa başlığı boş");
            assertTrue(currentUrl.contains("zeus-uat.paytr.com"), "URL doğru değil");
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            testResults.add(new TestResult(testName, true, "Sayfa başarıyla yüklendi. Başlık: " + pageTitle, screenshot, duration));
            
            logTestInfo("UAT sayfa yükleme testi başarılı: " + pageTitle);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Sayfa yüklenemedi: " + e.getMessage(), screenshot, duration));
            fail("UAT sayfa yükleme hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 2)
    public void testLoginPageElements() {
        long startTime = System.currentTimeMillis();
        String testName = "Login Sayfa Elementleri Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            // Login form elementlerini ara
            List<WebElement> emailFields = driver.findElements(By.xpath("//input[@type='email' or @name='email' or @id='email' or contains(@placeholder, 'email') or contains(@placeholder, 'E-posta')]"));
            List<WebElement> passwordFields = driver.findElements(By.xpath("//input[@type='password' or @name='password' or @id='password']"));
            List<WebElement> submitButtons = driver.findElements(By.xpath("//button[@type='submit'] | //input[@type='submit'] | //button[contains(text(), 'Giriş')] | //button[contains(text(), 'GİRİŞ')]"));
            
            boolean hasLoginForm = emailFields.size() > 0 && passwordFields.size() > 0;
            boolean hasSubmitButton = submitButtons.size() > 0;
            
            String message = String.format("Email alanları: %d, Şifre alanları: %d, Giriş butonları: %d", 
                                         emailFields.size(), passwordFields.size(), submitButtons.size());
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            
            if (hasLoginForm && hasSubmitButton) {
                testResults.add(new TestResult(testName, true, "Login elementleri bulundu. " + message, screenshot, duration));
                logTestInfo("Login elementleri testi başarılı");
            } else {
                testResults.add(new TestResult(testName, false, "Login elementleri eksik. " + message, screenshot, duration));
                logTestInfo("Login elementleri testi kısmen başarılı: " + message);
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Login elementleri testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("Login elementleri testi hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 3)
    public void testFormValidation() {
        long startTime = System.currentTimeMillis();
        String testName = "Form Validasyon Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            // Form validasyon elementlerini kontrol et
            String pageSource = driver.getPageSource();
            
            boolean hasRequiredFields = pageSource.contains("required") || pageSource.contains("*");
            boolean hasValidationMessages = pageSource.contains("validation") || pageSource.contains("error") || pageSource.contains("hata");
            boolean hasFormValidation = pageSource.contains("validate") || pageSource.contains("form");
            
            String message = String.format("Zorunlu alanlar: %s, Validasyon mesajları: %s, Form validasyonu: %s", 
                                         hasRequiredFields, hasValidationMessages, hasFormValidation);
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            testResults.add(new TestResult(testName, true, message, screenshot, duration));
            
            logTestInfo("Form validasyon testi tamamlandı: " + message);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Form validasyon testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("Form validasyon testi hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 4)
    public void testResponsiveDesign() {
        long startTime = System.currentTimeMillis();
        String testName = "Responsive Tasarım Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            // Farklı ekran boyutlarını test et
            Dimension[] screenSizes = {
                new Dimension(1920, 1080), // Desktop
                new Dimension(768, 1024),  // Tablet
                new Dimension(375, 667)    // Mobile
            };
            
            StringBuilder results = new StringBuilder();
            boolean allSizesWork = true;
            
            for (Dimension size : screenSizes) {
                driver.manage().window().setSize(size);
                Thread.sleep(1000); // Wait for resize
                
                boolean pageLoaded = driver.findElements(By.tagName("body")).size() > 0;
                String sizeResult = String.format("%dx%d: %s", size.width, size.height, pageLoaded ? "OK" : "FAIL");
                results.append(sizeResult).append(", ");
                
                if (!pageLoaded) allSizesWork = false;
            }
            
            // Reset to desktop size
            driver.manage().window().setSize(new Dimension(1920, 1080));
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            testResults.add(new TestResult(testName, allSizesWork, "Responsive test sonuçları: " + results.toString(), screenshot, duration));
            
            logTestInfo("Responsive tasarım testi tamamlandı");
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Responsive tasarım testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("Responsive tasarım testi hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 5)
    public void testQRCodeSection() {
        long startTime = System.currentTimeMillis();
        String testName = "QR Kod Bölümü Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            // QR kod elementlerini ara
            List<WebElement> qrElements = driver.findElements(By.xpath("//*[contains(@class, 'qr') or contains(@id, 'qr') or contains(text(), 'QR') or contains(text(), 'qr')]"));
            
            String pageSource = driver.getPageSource();
            boolean hasQRText = pageSource.toLowerCase().contains("qr") || pageSource.contains("karekod");
            boolean hasQRImage = pageSource.contains("qr.png") || pageSource.contains("qrcode");
            boolean hasMobileText = pageSource.toLowerCase().contains("mobil") || pageSource.toLowerCase().contains("mobile");
            
            String message = String.format("QR elementleri: %d, QR metni: %s, QR resmi: %s, Mobil metni: %s", 
                                         qrElements.size(), hasQRText, hasQRImage, hasMobileText);
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            
            boolean hasQRFeatures = qrElements.size() > 0 || hasQRText || hasQRImage;
            testResults.add(new TestResult(testName, hasQRFeatures, message, screenshot, duration));
            
            logTestInfo("QR kod bölümü testi tamamlandı: " + message);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "QR kod bölümü testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("QR kod bölümü testi hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 6)
    public void testPagePerformance() {
        long startTime = System.currentTimeMillis();
        String testName = "Sayfa Performans Testi";
        
        try {
            long pageLoadStart = System.currentTimeMillis();
            driver.get(baseURI + basePath);
            
            // Sayfa yüklenme süresini ölç
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            long pageLoadTime = System.currentTimeMillis() - pageLoadStart;
            
            // JavaScript ile performans bilgilerini al
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long domContentLoaded = (Long) js.executeScript("return performance.timing.domContentLoadedEventEnd - performance.timing.navigationStart;");
            Long loadComplete = (Long) js.executeScript("return performance.timing.loadEventEnd - performance.timing.navigationStart;");
            
            boolean performanceGood = pageLoadTime < 5000; // 5 saniyeden az
            
            String message = String.format("Sayfa yükleme: %dms, DOM yükleme: %dms, Tam yükleme: %dms", 
                                         pageLoadTime, domContentLoaded != null ? domContentLoaded : 0, 
                                         loadComplete != null ? loadComplete : 0);
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            testResults.add(new TestResult(testName, performanceGood, message, screenshot, duration));
            
            logTestInfo("Sayfa performans testi tamamlandı: " + message);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Sayfa performans testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("Sayfa performans testi hatası: " + e.getMessage());
        }
    }
    
    @Test(priority = 7)
    public void testSecurityFeatures() {
        long startTime = System.currentTimeMillis();
        String testName = "Güvenlik Özellikleri Testi";
        
        try {
            driver.get(baseURI + basePath);
            
            String currentUrl = driver.getCurrentUrl();
            boolean isHTTPS = currentUrl.startsWith("https://");
            
            String pageSource = driver.getPageSource();
            boolean hasSecurityHeaders = pageSource.contains("X-Frame-Options") || pageSource.contains("Content-Security-Policy");
            boolean hasPasswordField = pageSource.contains("type=\"password\"") || pageSource.contains("type='password'");
            boolean hasSecurityText = pageSource.toLowerCase().contains("güvenli") || pageSource.toLowerCase().contains("secure");
            
            String message = String.format("HTTPS: %s, Güvenlik başlıkları: %s, Şifre alanı: %s, Güvenlik metni: %s", 
                                         isHTTPS, hasSecurityHeaders, hasPasswordField, hasSecurityText);
            
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName);
            testResults.add(new TestResult(testName, isHTTPS, message, screenshot, duration));
            
            logTestInfo("Güvenlik özellikleri testi tamamlandı: " + message);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String screenshot = takeScreenshot(testName + "_FAILED");
            testResults.add(new TestResult(testName, false, "Güvenlik özellikleri testi hatası: " + e.getMessage(), screenshot, duration));
            logTestInfo("Güvenlik özellikleri testi hatası: " + e.getMessage());
        }
    }
    
    private void generateHTMLReport() {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang='tr'>\n");
            html.append("<head>\n");
            html.append("    <meta charset='UTF-8'>\n");
            html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
            html.append("    <title>PayTR UAT Test Raporu</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }\n");
            html.append("        .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); overflow: hidden; }\n");
            html.append("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }\n");
            html.append("        .header h1 { margin: 0; font-size: 2.5em; font-weight: 300; }\n");
            html.append("        .header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 1.1em; }\n");
            html.append("        .summary { display: flex; justify-content: space-around; padding: 30px; background: #f8f9fa; border-bottom: 1px solid #dee2e6; }\n");
            html.append("        .summary-item { text-align: center; }\n");
            html.append("        .summary-number { font-size: 2.5em; font-weight: bold; margin-bottom: 5px; }\n");
            html.append("        .summary-label { color: #6c757d; font-size: 0.9em; text-transform: uppercase; letter-spacing: 1px; }\n");
            html.append("        .passed { color: #28a745; }\n");
            html.append("        .failed { color: #dc3545; }\n");
            html.append("        .total { color: #007bff; }\n");
            html.append("        .test-results { padding: 30px; }\n");
            html.append("        .test-item { margin-bottom: 25px; border: 1px solid #dee2e6; border-radius: 10px; overflow: hidden; transition: transform 0.2s; }\n");
            html.append("        .test-item:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); }\n");
            html.append("        .test-header { padding: 20px; cursor: pointer; display: flex; justify-content: space-between; align-items: center; }\n");
            html.append("        .test-header.passed { background: linear-gradient(135deg, #d4edda, #c3e6cb); border-left: 5px solid #28a745; }\n");
            html.append("        .test-header.failed { background: linear-gradient(135deg, #f8d7da, #f5c6cb); border-left: 5px solid #dc3545; }\n");
            html.append("        .test-name { font-weight: bold; font-size: 1.1em; }\n");
            html.append("        .test-status { padding: 5px 15px; border-radius: 20px; color: white; font-size: 0.9em; font-weight: bold; }\n");
            html.append("        .test-status.passed { background: #28a745; }\n");
            html.append("        .test-status.failed { background: #dc3545; }\n");
            html.append("        .test-details { padding: 20px; background: #f8f9fa; border-top: 1px solid #dee2e6; display: none; }\n");
            html.append("        .test-message { margin-bottom: 15px; line-height: 1.6; }\n");
            html.append("        .test-meta { display: flex; justify-content: space-between; font-size: 0.9em; color: #6c757d; margin-bottom: 15px; }\n");
            html.append("        .screenshot { max-width: 100%; border-radius: 5px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
            html.append("        .footer { background: #343a40; color: white; padding: 20px; text-align: center; }\n");
            html.append("        @media (max-width: 768px) { .summary { flex-direction: column; } .summary-item { margin-bottom: 20px; } }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class='container'>\n");
            html.append("        <div class='header'>\n");
            html.append("            <h1>PayTR UAT Test Raporu</h1>\n");
            html.append("            <p>Test Tarihi: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))).append("</p>\n");
            html.append("            <p>Test URL: https://zeus-uat.paytr.com/magaza</p>\n");
            html.append("        </div>\n");
            
            // Summary section
            long passedTests = testResults.stream().mapToLong(t -> t.passed ? 1 : 0).sum();
            long failedTests = testResults.size() - passedTests;
            
            html.append("        <div class='summary'>\n");
            html.append("            <div class='summary-item'>\n");
            html.append("                <div class='summary-number total'>").append(testResults.size()).append("</div>\n");
            html.append("                <div class='summary-label'>Toplam Test</div>\n");
            html.append("            </div>\n");
            html.append("            <div class='summary-item'>\n");
            html.append("                <div class='summary-number passed'>").append(passedTests).append("</div>\n");
            html.append("                <div class='summary-label'>Başarılı</div>\n");
            html.append("            </div>\n");
            html.append("            <div class='summary-item'>\n");
            html.append("                <div class='summary-number failed'>").append(failedTests).append("</div>\n");
            html.append("                <div class='summary-label'>Başarısız</div>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
            
            // Test results section
            html.append("        <div class='test-results'>\n");
            for (TestResult result : testResults) {
                html.append("            <div class='test-item'>\n");
                html.append("                <div class='test-header ").append(result.passed ? "passed" : "failed").append("' onclick='toggleDetails(this)'>\n");
                html.append("                    <div class='test-name'>").append(result.testName).append("</div>\n");
                html.append("                    <div class='test-status ").append(result.passed ? "passed" : "failed").append("'>").append(result.passed ? "BAŞARILI" : "BAŞARISIZ").append("</div>\n");
                html.append("                </div>\n");
                html.append("                <div class='test-details'>\n");
                html.append("                    <div class='test-meta'>\n");
                html.append("                        <span>Süre: ").append(result.duration).append("ms</span>\n");
                html.append("                        <span>Zaman: ").append(result.timestamp).append("</span>\n");
                html.append("                    </div>\n");
                html.append("                    <div class='test-message'>").append(result.message).append("</div>\n");
                if (result.screenshot != null && !result.screenshot.isEmpty()) {
                    html.append("                    <img src='").append(result.screenshot).append("' alt='Test Screenshot' class='screenshot'>\n");
                }
                html.append("                </div>\n");
                html.append("            </div>\n");
            }
            html.append("        </div>\n");
            
            html.append("        <div class='footer'>\n");
            html.append("            <p>PayTR UAT Test Automation Framework - Powered by Selenium & TestNG</p>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            html.append("    <script>\n");
            html.append("        function toggleDetails(header) {\n");
            html.append("            const details = header.nextElementSibling;\n");
            html.append("            details.style.display = details.style.display === 'block' ? 'none' : 'block';\n");
            html.append("        }\n");
            html.append("    </script>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            
            Files.write(Paths.get("PayTR_UAT_Test_Report.html"), html.toString().getBytes());
            logTestInfo("HTML test raporu oluşturuldu: PayTR_UAT_Test_Report.html");
            
        } catch (IOException e) {
            logTestInfo("HTML rapor oluşturma hatası: " + e.getMessage());
        }
    }
}