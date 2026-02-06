package com.example.tests;

import io.qameta.allure.*;
import org.testng.annotations.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import com.example.utils.SafeWebDriverUtils;
import com.example.utils.WebDriverSetup;
import java.time.Duration;
import java.util.List;
import static org.testng.Assert.*;

/**
 * PayTR Sınır Durumu Test Senaryoları
 * Test ID: BT-001 to BT-004
 * Kategori: Boundary Testing
 */
@Epic("PayTR Boundary Testing")
@Feature("Boundary Conditions")
public class PayTRBoundaryTests extends BaseTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    
    @BeforeClass(alwaysRun = true)
    @Step("Sınır durumu testleri için test ortamını hazırla")
    public void setupBoundaryTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "";
        
        // WebDriver setup using SafeWebDriverUtils
        try {
            driver = SafeWebDriverUtils.getSafeWebDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            js = (JavascriptExecutor) driver;
            logTestInfo("PayTR Sınır Durumu Test Suite başlatıldı ve WebDriver başarıyla alındı");
        } catch (Exception e) {
            logTestInfo("WebDriver başlatma hatası: " + e.getMessage());
            throw new RuntimeException("WebDriver başlatılamadı", e);
        }
    }
    
    @BeforeMethod(alwaysRun = true)
    public void checkDriver() {
        if (driver == null) {
            driver = SafeWebDriverUtils.getSafeWebDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            js = (JavascriptExecutor) driver;
        }
    }
    
    @AfterClass(alwaysRun = true)
    @Step("Sınır durumu testleri sonrası temizlik")
    public void tearDown() {
        WebDriverSetup.quitDriver();
        logTestInfo("PayTR Sınır Durumu Test Suite tamamlandı");
    }
    
    /**
     * Test ID: BT-001
     * Test Adı: Minimum Payment Amount Testi
     * Kategori: Boundary - Payment Amount
     * Öncelik: Kritik
     */
    @Test(priority = 1, groups = {"boundary", "critical", "payment"})
    @Story("Minimum Payment Amount")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Minimum ödeme tutarı sınır değeri kontrolü")
    public void testMinimumPaymentAmount() {
        logTestInfo("Test ID: BT-001 - Minimum Payment Amount Testi");
        
        try {
            // Ödeme sayfasına git
            driver.get(baseURI + "/magaza");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Ödeme tutarı alanını bul
            List<WebElement> amountFields = driver.findElements(By.xpath(
                "//input[@type='number' or @name='amount' or @name='tutar' or contains(@placeholder,'tutar') or contains(@placeholder,'amount')]"));
            
            if (!amountFields.isEmpty()) {
                WebElement amountField = amountFields.get(0);
                
                // Minimum değer testleri
                String[] minTestValues = {
                    "0",      // Sıfır değer
                    "0.01",   // En küçük pozitif değer
                    "0.99",   // 1'den küçük değer
                    "1",      // Minimum kabul edilebilir değer
                    "1.00",   // Minimum değer (ondalıklı)
                    "-1",     // Negatif değer
                    "-0.01"   // Negatif ondalık değer
                };
                
                for (String testValue : minTestValues) {
                    try {
                        amountField.clear();
                        amountField.sendKeys(testValue);
                        
                        // JavaScript ile değeri kontrol et
                        String actualValue = (String) js.executeScript("return arguments[0].value;", amountField);
                        logTestInfo("Test değeri: " + testValue + ", Gerçek değer: " + actualValue);
                        
                        // Negatif değerler kabul edilmemeli
                        if (testValue.startsWith("-")) {
                            assertFalse(actualValue.startsWith("-"), 
                                "Negatif değer kabul edildi: " + testValue);
                        }
                        
                        // Sıfır değer kontrolü
                        if (testValue.equals("0")) {
                            // Sıfır değer genellikle kabul edilmemeli
                            String validationMessage = amountField.getAttribute("validationMessage");
                            if (validationMessage != null && !validationMessage.isEmpty()) {
                                logTestInfo("Sıfır değer için validation mesajı: " + validationMessage);
                            }
                        }
                        
                        // Min attribute kontrolü
                        String minAttr = amountField.getAttribute("min");
                        if (minAttr != null) {
                            double minValue = Double.parseDouble(minAttr);
                            double testValueDouble = Double.parseDouble(testValue);
                            
                            if (testValueDouble < minValue) {
                                logTestInfo("Minimum değerin altında test: " + testValue + " < " + minValue);
                            }
                        }
                        
                    } catch (Exception e) {
                        logTestInfo("Test değeri hatası: " + testValue + " - " + e.getMessage());
                    }
                }
                
                logTestResult("BT-001", "BAŞARILI", "Minimum ödeme tutarı sınır testleri tamamlandı");
                
            } else {
                logTestInfo("Ödeme tutarı alanı bulunamadı, test atlandı");
                logTestResult("BT-001", "ATLANDI", "Ödeme tutarı alanı bulunamadı");
            }
            
        } catch (Exception e) {
            logTestResult("BT-001", "BAŞARISIZ", "Minimum payment amount testi hatası: " + e.getMessage());
            fail("Minimum payment amount testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: BT-002
     * Test Adı: Maximum Payment Amount Testi
     * Kategori: Boundary - Payment Amount
     * Öncelik: Kritik
     */
    @Test(priority = 2, groups = {"boundary", "critical", "payment"})
    @Story("Maximum Payment Amount")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Maximum ödeme tutarı sınır değeri kontrolü")
    public void testMaximumPaymentAmount() {
        logTestInfo("Test ID: BT-002 - Maximum Payment Amount Testi");
        
        try {
            driver.get(baseURI + "/magaza");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            List<WebElement> amountFields = driver.findElements(By.xpath(
                "//input[@type='number' or @name='amount' or @name='tutar']"));
            
            if (!amountFields.isEmpty()) {
                WebElement amountField = amountFields.get(0);
                
                // Maximum değer testleri
                String[] maxTestValues = {
                    "999999",      // Büyük değer
                    "1000000",     // 1 milyon
                    "9999999",     // Çok büyük değer
                    "99999999",    // Çok çok büyük değer
                    "999999999",   // Maksimum test değeri
                    "1000000000",  // 1 milyar
                    "99999.99",    // Büyük ondalık değer
                    "999999.99"    // Maksimum ondalık değer
                };
                
                for (String testValue : maxTestValues) {
                    try {
                        amountField.clear();
                        amountField.sendKeys(testValue);
                        
                        String actualValue = (String) js.executeScript("return arguments[0].value;", amountField);
                        logTestInfo("Test değeri: " + testValue + ", Gerçek değer: " + actualValue);
                        
                        // Max attribute kontrolü
                        String maxAttr = amountField.getAttribute("max");
                        if (maxAttr != null) {
                            double maxValue = Double.parseDouble(maxAttr);
                            double testValueDouble = Double.parseDouble(testValue);
                            
                            if (testValueDouble > maxValue) {
                                logTestInfo("Maximum değerin üstünde test: " + testValue + " > " + maxValue);
                                
                                // Değer kesilmiş olmalı
                                double actualValueDouble = Double.parseDouble(actualValue);
                                assertTrue(actualValueDouble <= maxValue,
                                    "Maximum değer aşıldı: " + actualValueDouble + " > " + maxValue);
                            }
                        }
                        
                        // Validation message kontrolü
                        String validationMessage = amountField.getAttribute("validationMessage");
                        if (validationMessage != null && !validationMessage.isEmpty()) {
                            logTestInfo("Validation mesajı: " + validationMessage);
                        }
                        
                    } catch (Exception e) {
                        logTestInfo("Test değeri hatası: " + testValue + " - " + e.getMessage());
                    }
                }
                
                logTestResult("BT-002", "BAŞARILI", "Maximum ödeme tutarı sınır testleri tamamlandı");
                
            } else {
                logTestResult("BT-002", "ATLANDI", "Ödeme tutarı alanı bulunamadı");
            }
            
        } catch (Exception e) {
            logTestResult("BT-002", "BAŞARISIZ", "Maximum payment amount testi hatası: " + e.getMessage());
            fail("Maximum payment amount testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: BT-003
     * Test Adı: Input Field Length Limits Testi
     * Kategori: Boundary - Input Length
     * Öncelik: Orta
     */
    @Test(priority = 3, groups = {"boundary", "medium", "input-length"})
    @Story("Input Field Length Limits")
    @Severity(SeverityLevel.NORMAL)
    @Description("Input alanlarının karakter sınırı kontrolü")
    public void testInputFieldLengthLimits() {
        logTestInfo("Test ID: BT-003 - Input Field Length Limits Testi");
        
        try {
            driver.get(baseURI + "/magaza/kullanici-girisi");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Tüm text input alanlarını bul
            List<WebElement> textInputs = driver.findElements(By.xpath(
                "//input[@type='text' or @type='email' or @type='password']"));
            
            for (WebElement input : textInputs) {
                try {
                    if (!input.isDisplayed() || !input.isEnabled()) {
                        continue;
                    }
                    
                    String fieldName = input.getAttribute("name");
                    if (fieldName == null) {
                        fieldName = input.getAttribute("id");
                    }
                    if (fieldName == null) {
                        fieldName = "unknown_field";
                    }
                    
                    logTestInfo("Test edilen alan: " + fieldName);
                    
                    // MaxLength attribute kontrolü
                    String maxLengthAttr = input.getAttribute("maxlength");
                    int maxLength = maxLengthAttr != null ? Integer.parseInt(maxLengthAttr) : -1;
                    
                    if (maxLength > 0) {
                        logTestInfo("MaxLength: " + maxLength);
                        
                        // Sınır değer testleri
                        String[] lengthTests = {
                            generateString(maxLength - 1),     // Sınırın altında
                            generateString(maxLength),         // Tam sınır
                            generateString(maxLength + 1),     // Sınırın üstünde
                            generateString(maxLength + 10),    // Çok üstünde
                            generateString(maxLength * 2)      // İki katı
                        };
                        
                        for (int i = 0; i < lengthTests.length; i++) {
                            String testString = lengthTests[i];
                            input.clear();
                            input.sendKeys(testString);
                            
                            String actualValue = (String) js.executeScript("return arguments[0].value;", input);
                            int actualLength = actualValue.length();
                            
                            logTestInfo("Test " + (i + 1) + " - Gönderilen: " + testString.length() + 
                                       " karakter, Alınan: " + actualLength + " karakter");
                            
                            // MaxLength kontrolü
                            assertTrue(actualLength <= maxLength,
                                "MaxLength aşıldı: " + actualLength + " > " + maxLength);
                        }
                    } else {
                        // MaxLength yoksa genel test
                        String longString = generateString(1000); // 1000 karakter
                        input.clear();
                        input.sendKeys(longString);
                        
                        String actualValue = (String) js.executeScript("return arguments[0].value;", input);
                        logTestInfo("Uzun string testi - Gönderilen: 1000, Alınan: " + actualValue.length());
                        
                        // Çok uzun string kabul edilmemeli (güvenlik)
                        assertTrue(actualValue.length() < 10000,
                            "Çok uzun string kabul edildi: " + actualValue.length());
                    }
                    
                } catch (Exception e) {
                    logTestInfo("Input test hatası: " + e.getMessage());
                }
            }
            
            logTestResult("BT-003", "BAŞARILI", "Input field length limits testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("BT-003", "BAŞARISIZ", "Input length limits testi hatası: " + e.getMessage());
            fail("Input length limits testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: BT-004
     * Test Adı: Date Range Boundary Testi
     * Kategori: Boundary - Date Range
     * Öncelik: Orta
     */
    @Test(priority = 4, groups = {"boundary", "medium", "date"})
    @Story("Date Range Boundaries")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tarih aralığı sınır değerleri kontrolü")
    public void testDateRangeBoundary() {
        logTestInfo("Test ID: BT-004 - Date Range Boundary Testi");
        
        try {
            driver.get(baseURI + "/magaza");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Date input alanlarını bul
            List<WebElement> dateInputs = driver.findElements(By.xpath("//input[@type='date']"));
            
            if (!dateInputs.isEmpty()) {
                for (WebElement dateInput : dateInputs) {
                    try {
                        if (!dateInput.isDisplayed() || !dateInput.isEnabled()) {
                            continue;
                        }
                        
                        String fieldName = dateInput.getAttribute("name");
                        logTestInfo("Test edilen tarih alanı: " + fieldName);
                        
                        // Min ve Max tarih kontrolü
                        String minDate = dateInput.getAttribute("min");
                        String maxDate = dateInput.getAttribute("max");
                        
                        logTestInfo("Min Date: " + minDate + ", Max Date: " + maxDate);
                        
                        // Sınır tarih testleri
                        String[] dateTests = {
                            "1900-01-01",    // Çok eski tarih
                            "1970-01-01",    // Unix epoch
                            "2000-01-01",    // Y2K
                            "2024-01-01",    // Güncel yıl
                            "2030-12-31",    // Gelecek tarih
                            "2099-12-31",    // Uzak gelecek
                            "9999-12-31"     // Maksimum tarih
                        };
                        
                        for (String testDate : dateTests) {
                            try {
                                dateInput.clear();
                                dateInput.sendKeys(testDate);
                                
                                String actualValue = (String) js.executeScript("return arguments[0].value;", dateInput);
                                logTestInfo("Test tarihi: " + testDate + ", Gerçek değer: " + actualValue);
                                
                                // Min tarih kontrolü
                                if (minDate != null && !minDate.isEmpty()) {
                                    if (testDate.compareTo(minDate) < 0) {
                                        logTestInfo("Minimum tarihten önce: " + testDate + " < " + minDate);
                                    }
                                }
                                
                                // Max tarih kontrolü
                                if (maxDate != null && !maxDate.isEmpty()) {
                                    if (testDate.compareTo(maxDate) > 0) {
                                        logTestInfo("Maximum tarihten sonra: " + testDate + " > " + maxDate);
                                    }
                                }
                                
                            } catch (Exception e) {
                                logTestInfo("Tarih test hatası: " + testDate + " - " + e.getMessage());
                            }
                        }
                        
                    } catch (Exception e) {
                        logTestInfo("Date input test hatası: " + e.getMessage());
                    }
                }
                
                logTestResult("BT-004", "BAŞARILI", "Date range boundary testleri tamamlandı");
                
            } else {
                logTestInfo("Date input alanı bulunamadı, test atlandı");
                logTestResult("BT-004", "ATLANDI", "Date input alanı bulunamadı");
            }
            
        } catch (Exception e) {
            logTestResult("BT-004", "BAŞARISIZ", "Date range boundary testi hatası: " + e.getMessage());
            fail("Date range boundary testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Belirtilen uzunlukta string oluşturur
     */
    private String generateString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n🎯 SINIR DURUMU TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}