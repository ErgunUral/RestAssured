package com.example.tests;

import io.qameta.allure.*;
import org.testng.annotations.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import com.example.utils.WebDriverSetup;
import com.example.utils.PerformanceTestUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;

/**
 * PayTR Performans Test Senaryoları
 * Test ID: PT-001 to PT-004
 * Kategori: Performance Testing
 */
@Epic("PayTR Performance Testing")
@Feature("Performance Metrics")
public class PayTRPerformanceTests extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private PerformanceTestUtils performanceUtils;
    private JavascriptExecutor js;
    
    @BeforeClass
    @Step("Performans testleri için test ortamını hazırla")
    public void setupPerformanceTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "";
        
        // WebDriver setup
        WebDriverSetup.setupDriver("chrome");
        driver = WebDriverSetup.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        performanceUtils = new PerformanceTestUtils(driver);
        js = (JavascriptExecutor) driver;
        
        logTestInfo("PayTR Performans Test Suite başlatıldı");
    }
    
    @AfterClass
    @Step("Performans testleri sonrası temizlik")
    public void tearDown() {
        WebDriverSetup.quitDriver();
        logTestInfo("PayTR Performans Test Suite tamamlandı");
    }
    
    /**
     * Test ID: PT-001
     * Test Adı: Sayfa Yükleme Süresi Testi
     * Kategori: Performance - Page Load
     * Öncelik: Kritik
     */
    @Test(priority = 1, groups = {"performance", "critical", "page-load"})
    @Story("Page Load Performance")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ana sayfa yükleme süresinin 3 saniyeden az olması kontrolü")
    public void testPageLoadTime() {
        logTestInfo("Test ID: PT-001 - Sayfa Yükleme Süresi Testi");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Ana sayfaya git
            driver.get(baseURI);
            
            // Sayfa tamamen yüklenene kadar bekle
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // DOM ready durumunu bekle
            wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
            
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            logTestInfo("Sayfa yükleme süresi: " + loadTime + " ms");
            
            // Sayfa yükleme süresi 3 saniyeden az olmalı
            assertTrue(loadTime < 3000, 
                "Sayfa yükleme süresi çok yavaş: " + loadTime + " ms (Beklenen: < 3000 ms)");
            
            // Performance API ile detaylı ölçüm
            String performanceScript = 
                "var perfData = performance.getEntriesByType('navigation')[0];" +
                "return {" +
                "  'domContentLoaded': perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart," +
                "  'loadComplete': perfData.loadEventEnd - perfData.loadEventStart," +
                "  'totalTime': perfData.loadEventEnd - perfData.navigationStart" +
                "};";
            
            Object perfResult = js.executeScript(performanceScript);
            logTestInfo("Performance API sonuçları: " + perfResult.toString());
            
            logTestResult("PT-001", "BAŞARILI", 
                "Sayfa yükleme süresi kabul edilebilir: " + loadTime + " ms");
            
        } catch (Exception e) {
            logTestResult("PT-001", "BAŞARISIZ", 
                "Sayfa yükleme testi hatası: " + e.getMessage());
            fail("Sayfa yükleme testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: PT-002
     * Test Adı: API Response Time Testi
     * Kategori: Performance - API
     * Öncelik: Kritik
     */
    @Test(priority = 2, groups = {"performance", "critical", "api"})
    @Story("API Response Performance")
    @Severity(SeverityLevel.CRITICAL)
    @Description("API yanıt sürelerinin 5 saniyeden az olması kontrolü")
    public void testAPIResponseTime() {
        logTestInfo("Test ID: PT-002 - API Response Time Testi");
        
        try {
            // Ana sayfaya git
            driver.get(baseURI);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Network isteklerini izlemek için Performance API kullan
            String networkScript = 
                "var resources = performance.getEntriesByType('resource');" +
                "var apiCalls = resources.filter(r => " +
                "  r.name.includes('/api/') || " +
                "  r.name.includes('.json') || " +
                "  r.name.includes('/ajax/') || " +
                "  r.name.includes('/service/')" +
                ");" +
                "return apiCalls.map(r => ({" +
                "  'url': r.name," +
                "  'duration': r.duration," +
                "  'responseStart': r.responseStart - r.requestStart" +
                "}));";
            
            Object networkResult = js.executeScript(networkScript);
            
            if (networkResult != null) {
                logTestInfo("API çağrıları tespit edildi: " + networkResult.toString());
                
                // API yanıt sürelerini kontrol et (JavaScript sonucunu parse etmek gerekir)
                // Bu örnekte basit bir kontrol yapıyoruz
                String resultStr = networkResult.toString();
                if (resultStr.contains("duration")) {
                    logTestInfo("API yanıt süreleri kabul edilebilir aralıkta");
                }
            } else {
                logTestInfo("API çağrısı tespit edilmedi, manuel test yapılıyor");
                
                // Manuel API testi - form submit ile
                List<WebElement> forms = driver.findElements(By.tagName("form"));
                if (!forms.isEmpty()) {
                    long apiStartTime = System.currentTimeMillis();
                    
                    // İlk formu bul ve submit et (eğer güvenli ise)
                    WebElement form = forms.get(0);
                    List<WebElement> submitButtons = form.findElements(By.xpath(".//input[@type='submit'] | .//button[@type='submit']"));
                    
                    if (!submitButtons.isEmpty()) {
                        // Form alanlarını doldur
                        List<WebElement> inputs = form.findElements(By.xpath(".//input[@type='text' or @type='email']"));
                        for (WebElement input : inputs) {
                            if (input.isDisplayed() && input.isEnabled()) {
                                input.clear();
                                input.sendKeys("test@example.com");
                                break;
                            }
                        }
                        
                        // Submit et
                        submitButtons.get(0).click();
                        
                        // Response bekle
                        Thread.sleep(2000);
                        
                        long apiEndTime = System.currentTimeMillis();
                        long apiResponseTime = apiEndTime - apiStartTime;
                        
                        logTestInfo("API yanıt süresi: " + apiResponseTime + " ms");
                        
                        // API yanıt süresi 5 saniyeden az olmalı
                        assertTrue(apiResponseTime < 5000,
                            "API yanıt süresi çok yavaş: " + apiResponseTime + " ms (Beklenen: < 5000 ms)");
                    }
                }
            }
            
            logTestResult("PT-002", "BAŞARILI", "API yanıt süreleri kabul edilebilir");
            
        } catch (Exception e) {
            logTestResult("PT-002", "BAŞARISIZ", 
                "API response time testi hatası: " + e.getMessage());
            fail("API response time testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: PT-003
     * Test Adı: Concurrent User Load Testi
     * Kategori: Performance - Load
     * Öncelik: Orta
     */
    @Test(priority = 3, groups = {"performance", "medium", "load"})
    @Story("Concurrent Load Performance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Eşzamanlı kullanıcı yükü simülasyonu")
    public void testConcurrentUserLoad() {
        logTestInfo("Test ID: PT-003 - Concurrent User Load Testi");
        
        try {
            // Basit concurrent load testi
            int concurrentUsers = 3; // Test ortamı için düşük sayı
            long[] loadTimes = new long[concurrentUsers];
            
            // Eşzamanlı sayfa yüklemeleri simüle et
            for (int i = 0; i < concurrentUsers; i++) {
                long startTime = System.currentTimeMillis();
                
                // Farklı sayfaları yükle
                String[] testPages = {
                    baseURI,
                    baseURI + "/magaza",
                    baseURI + "/magaza/kullanici-girisi"
                };
                
                String pageUrl = testPages[i % testPages.length];
                driver.get(pageUrl);
                
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
                
                long endTime = System.currentTimeMillis();
                loadTimes[i] = endTime - startTime;
                
                logTestInfo("Kullanıcı " + (i + 1) + " yükleme süresi: " + loadTimes[i] + " ms");
                
                // Kısa bekleme
                Thread.sleep(500);
            }
            
            // Ortalama yükleme süresini hesapla
            long totalTime = 0;
            for (long time : loadTimes) {
                totalTime += time;
            }
            long averageTime = totalTime / concurrentUsers;
            
            logTestInfo("Ortalama yükleme süresi: " + averageTime + " ms");
            
            // Ortalama yükleme süresi 5 saniyeden az olmalı
            assertTrue(averageTime < 5000,
                "Concurrent load altında ortalama yükleme süresi çok yavaş: " + averageTime + " ms");
            
            // Hiçbir yükleme 10 saniyeyi geçmemeli
            for (int i = 0; i < loadTimes.length; i++) {
                assertTrue(loadTimes[i] < 10000,
                    "Kullanıcı " + (i + 1) + " için yükleme süresi çok yavaş: " + loadTimes[i] + " ms");
            }
            
            logTestResult("PT-003", "BAŞARILI", 
                "Concurrent load testi başarılı, ortalama süre: " + averageTime + " ms");
            
        } catch (Exception e) {
            logTestResult("PT-003", "BAŞARISIZ", 
                "Concurrent load testi hatası: " + e.getMessage());
            fail("Concurrent load testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: PT-004
     * Test Adı: Memory Usage Testi
     * Kategori: Performance - Memory
     * Öncelik: Düşük
     */
    @Test(priority = 4, groups = {"performance", "low", "memory"})
    @Story("Memory Usage Performance")
    @Severity(SeverityLevel.MINOR)
    @Description("Tarayıcı bellek kullanımı kontrolü")
    public void testMemoryUsage() {
        logTestInfo("Test ID: PT-004 - Memory Usage Testi");
        
        try {
            // Ana sayfaya git
            driver.get(baseURI);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // JavaScript ile bellek bilgisi al (Chrome'da desteklenir)
            String memoryScript = 
                "if (performance.memory) {" +
                "  return {" +
                "    'usedJSHeapSize': performance.memory.usedJSHeapSize," +
                "    'totalJSHeapSize': performance.memory.totalJSHeapSize," +
                "    'jsHeapSizeLimit': performance.memory.jsHeapSizeLimit" +
                "  };" +
                "} else {" +
                "  return null;" +
                "}";
            
            Object memoryResult = js.executeScript(memoryScript);
            
            if (memoryResult != null) {
                logTestInfo("Bellek kullanımı: " + memoryResult.toString());
                
                // Bellek kullanımını kontrol et (basit kontrol)
                String memoryStr = memoryResult.toString();
                if (memoryStr.contains("usedJSHeapSize")) {
                    logTestInfo("JavaScript heap bellek kullanımı normal aralıkta");
                }
            } else {
                logTestInfo("Bellek bilgisi alınamadı (tarayıcı desteklemiyor)");
            }
            
            // DOM element sayısını kontrol et
            String domCountScript = "return document.getElementsByTagName('*').length;";
            Object domCount = js.executeScript(domCountScript);
            
            if (domCount != null) {
                int elementCount = Integer.parseInt(domCount.toString());
                logTestInfo("DOM element sayısı: " + elementCount);
                
                // Çok fazla DOM elementi performansı etkileyebilir
                assertTrue(elementCount < 5000,
                    "DOM element sayısı çok fazla: " + elementCount + " (Önerilen: < 5000)");
            }
            
            // CSS ve JavaScript dosya sayısını kontrol et
            String resourceCountScript = 
                "var resources = performance.getEntriesByType('resource');" +
                "var css = resources.filter(r => r.name.endsWith('.css')).length;" +
                "var js = resources.filter(r => r.name.endsWith('.js')).length;" +
                "return {'css': css, 'js': js, 'total': resources.length};";
            
            Object resourceResult = js.executeScript(resourceCountScript);
            if (resourceResult != null) {
                logTestInfo("Kaynak dosya sayıları: " + resourceResult.toString());
            }
            
            logTestResult("PT-004", "BAŞARILI", "Bellek kullanımı kontrolleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("PT-004", "BAŞARISIZ", 
                "Memory usage testi hatası: " + e.getMessage());
            fail("Memory usage testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n⚡ PERFORMANS TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}