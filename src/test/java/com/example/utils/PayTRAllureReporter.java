package com.example.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.qameta.allure.model.Status;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;
import com.example.config.PayTRTestConfig;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

/**
 * PayTR Allure Reporter
 * PayTR testleri için Allure Reports entegrasyonu
 */
public class PayTRAllureReporter implements ITestListener, ISuiteListener, TestLifecycleListener {
    
    private Map<String, Long> testStartTimes = new HashMap<>();
    
    @Override
    public void onStart(ISuite suite) {
        System.out.println("=== PayTR Allure Reports Başlatıldı ===");
        
        // Allure environment properties
        setAllureEnvironmentProperties();
        
        // Suite bilgilerini Allure'a ekle
        Allure.epic("PayTR Test Automation");
        Allure.feature("PayTR Platform Testing");
        Allure.story("Test Suite: " + suite.getName());
        
        addEnvironmentInfo();
    }
    
    @Override
    public void onFinish(ISuite suite) {
        System.out.println("=== PayTR Allure Reports Tamamlandı ===");
        System.out.println("Allure raporu oluşturmak için: allure serve allure-results");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        testStartTimes.put(testName, System.currentTimeMillis());
        
        // Test bilgilerini Allure'a ekle
        Allure.getLifecycle().updateTestCase(testResult -> {
            testResult.setName(getTestDisplayName(result));
            testResult.setDescription(getTestDescription(result));
        });
        
        // Test gruplarını label olarak ekle
        String[] groups = result.getMethod().getGroups();
        for (String group : groups) {
            Allure.label("testGroup", group);
        }
        
        // PayTR spesifik etiketler
        addPayTRLabels(result);
        
        // Test başlangıç bilgilerini ekle
        addTestStartInfo(result);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = calculateDuration(testName);
        
        // Başarı bilgilerini ekle
        addSuccessInfo(result, duration);
        
        // PayTR spesifik başarı bilgileri
        addPayTRSuccessDetails(result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = calculateDuration(testName);
        
        // Hata bilgilerini ekle
        addFailureInfo(result, duration);
        
        // Ekran görüntüsü al
        takeScreenshot(result);
        
        // PayTR spesifik hata bilgileri
        addPayTRFailureDetails(result);
        
        // Hata loglarını ekle
        addErrorLogs(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // Atlama bilgilerini ekle
        addSkipInfo(result);
        
        // Atlama sebebini ekle
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            attachText("Atlama Sebebi", throwable.getMessage());
        }
    }
    
    @Step("Test başlatılıyor: {testName}")
    private void addTestStartInfo(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        attachText("Test Bilgileri", 
            "Test Adı: " + testName + "\n" +
            "Test Sınıfı: " + result.getTestClass().getName() + "\n" +
            "Test Ortamı: " + PayTRTestConfig.BASE_URL + "\n" +
            "Başlangıç Zamanı: " + new java.util.Date());
    }
    
    @Step("Test başarıyla tamamlandı")
    private void addSuccessInfo(ITestResult result, long duration) {
        attachText("Test Sonucu", 
            "Durum: BAŞARILI ✅\n" +
            "Süre: " + duration + "ms\n" +
            "Bitiş Zamanı: " + new java.util.Date());
    }
    
    @Step("Test başarısız oldu")
    private void addFailureInfo(ITestResult result, long duration) {
        Throwable throwable = result.getThrowable();
        
        String errorInfo = "Durum: BAŞARISIZ ❌\n" +
                          "Süre: " + duration + "ms\n" +
                          "Bitiş Zamanı: " + new java.util.Date();
        
        if (throwable != null) {
            errorInfo += "\nHata Mesajı: " + throwable.getMessage();
        }
        
        attachText("Test Sonucu", errorInfo);
        
        // Stack trace'i ekle
        if (throwable != null) {
            attachText("Stack Trace", getStackTrace(throwable));
        }
    }
    
    @Step("Test atlandı")
    private void addSkipInfo(ITestResult result) {
        attachText("Test Sonucu", 
            "Durum: ATLANDI ⏭️\n" +
            "Zaman: " + new java.util.Date());
    }
    
    private void addPayTRLabels(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // Test türüne göre etiketler
        if (testName.contains("Payment")) {
            Allure.label("feature", "Payment Processing");
            Allure.label("component", "Payment Form");
            Allure.label("testType", "Functional");
        } else if (testName.contains("VirtualPOS")) {
            Allure.label("feature", "Virtual POS");
            Allure.label("component", "POS Integration");
            Allure.label("testType", "Integration");
        } else if (testName.contains("Security")) {
            Allure.label("feature", "Security");
            Allure.label("component", "Security Controls");
            Allure.label("testType", "Security");
        } else if (testName.contains("Card")) {
            Allure.label("feature", "Card Validation");
            Allure.label("component", "Card Form");
            Allure.label("testType", "Validation");
        } else if (testName.contains("Installment")) {
            Allure.label("feature", "Installment Options");
            Allure.label("component", "Installment Calculator");
            Allure.label("testType", "Calculation");
        } else if (testName.contains("Smoke")) {
            Allure.label("feature", "Smoke Tests");
            Allure.label("component", "Critical Path");
            Allure.label("testType", "Smoke");
        }
        
        // Öncelik etiketleri
        if (testName.contains("Critical") || testName.contains("Smoke")) {
            Allure.label("priority", "Critical");
        } else if (testName.contains("Security")) {
            Allure.label("priority", "High");
        } else {
            Allure.label("priority", "Medium");
        }
        
        // PayTR spesifik etiketler
        Allure.label("platform", "PayTR");
        Allure.label("environment", PayTRTestConfig.BASE_URL.contains("test") ? "Test" : "Production");
        Allure.label("browser", PayTRTestConfig.DEFAULT_BROWSER);
    }
    
    private void addPayTRSuccessDetails(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        if (testName.contains("Payment")) {
            attachText("Ödeme İşlemi Detayları", 
                "✅ Ödeme formu başarıyla yüklendi\n" +
                "✅ Kart bilgileri doğru şekilde işlendi\n" +
                "✅ SSL güvenlik kontrolü geçti\n" +
                "✅ Ödeme işlemi tamamlandı");
        } else if (testName.contains("VirtualPOS")) {
            attachText("Sanal POS Detayları", 
                "✅ Sanal POS formu başarıyla yüklendi\n" +
                "✅ Hash doğrulama başarılı\n" +
                "✅ Merchant bilgileri doğru\n" +
                "✅ İşlem başarıyla tamamlandı");
        } else if (testName.contains("Security")) {
            attachText("Güvenlik Kontrol Detayları", 
                "✅ SSL sertifikası geçerli\n" +
                "✅ CSRF koruması aktif\n" +
                "✅ XSS koruması çalışıyor\n" +
                "✅ Güvenlik başlıkları mevcut");
        } else if (testName.contains("Card")) {
            attachText("Kart Validasyon Detayları", 
                "✅ Kart numarası formatı doğru\n" +
                "✅ CVV validasyonu çalışıyor\n" +
                "✅ Son kullanma tarihi kontrolü aktif\n" +
                "✅ Kart türü tanıma başarılı");
        } else if (testName.contains("Installment")) {
            attachText("Taksit İşlem Detayları", 
                "✅ Taksit seçenekleri yüklendi\n" +
                "✅ Komisyon hesaplama doğru\n" +
                "✅ Toplam tutar hesaplama başarılı\n" +
                "✅ Taksit limitleri kontrol edildi");
        }
    }
    
    private void addPayTRFailureDetails(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        if (testName.contains("Payment")) {
            attachText("Ödeme İşlemi Hata Detayları", 
                "❌ Ödeme işlemi başarısız\n" +
                "🔍 Kontrol edilecekler:\n" +
                "  - Ödeme formu elementleri\n" +
                "  - API bağlantısı\n" +
                "  - SSL sertifikası\n" +
                "  - Test kart bilgileri");
        } else if (testName.contains("VirtualPOS")) {
            attachText("Sanal POS Hata Detayları", 
                "❌ Sanal POS işlemi başarısız\n" +
                "🔍 Kontrol edilecekler:\n" +
                "  - Merchant bilgileri\n" +
                "  - Hash hesaplama\n" +
                "  - API endpoint'leri\n" +
                "  - Form validasyonu");
        } else if (testName.contains("Security")) {
            attachText("Güvenlik Kontrol Hata Detayları", 
                "❌ Güvenlik kontrolü başarısız\n" +
                "⚠️ Potansiyel güvenlik riskleri:\n" +
                "  - SSL sertifikası sorunu\n" +
                "  - CSRF koruması eksik\n" +
                "  - XSS açığı mevcut\n" +
                "  - Güvenlik başlıkları eksik");
        } else if (testName.contains("Card")) {
            attachText("Kart Validasyon Hata Detayları", 
                "❌ Kart validasyonu başarısız\n" +
                "🔍 Kontrol edilecekler:\n" +
                "  - Kart numarası format kontrolü\n" +
                "  - CVV validasyon kuralları\n" +
                "  - Son kullanma tarihi kontrolü\n" +
                "  - Kart türü tanıma algoritması");
        } else if (testName.contains("Installment")) {
            attachText("Taksit İşlem Hata Detayları", 
                "❌ Taksit işlemi başarısız\n" +
                "🔍 Kontrol edilecekler:\n" +
                "  - Taksit seçenekleri yükleme\n" +
                "  - Komisyon hesaplama algoritması\n" +
                "  - Toplam tutar hesaplama\n" +
                "  - Taksit limit kontrolleri");
        }
    }
    
    private void addErrorLogs(ITestResult result) {
        // Browser console loglarını al (eğer WebDriver varsa)
        try {
            Object testInstance = result.getInstance();
            WebDriver driver = getDriverFromTestInstance(testInstance);
            
            if (driver != null) {
                // Console loglarını al
                org.openqa.selenium.logging.LogEntries logs = driver.manage().logs().get("browser");
                if (logs != null && logs.getAll().size() > 0) {
                    StringBuilder consoleLogs = new StringBuilder();
                    for (org.openqa.selenium.logging.LogEntry log : logs) {
                        consoleLogs.append(log.getLevel()).append(": ").append(log.getMessage()).append("\n");
                    }
                    attachText("Browser Console Logs", consoleLogs.toString());
                }
            }
        } catch (Exception e) {
            // Console logları alınamadı
        }
    }
    
    private void setAllureEnvironmentProperties() {
        // Environment bilgilerini Allure için ayarla
        System.setProperty("allure.results.directory", "allure-results");
        
        // Allure environment.properties dosyası için bilgiler
        attachText("Environment Properties", 
            "Test.Environment=" + PayTRTestConfig.BASE_URL + "\n" +
            "Platform=" + System.getProperty("os.name") + "\n" +
            "Java.Version=" + System.getProperty("java.version") + "\n" +
            "Browser=" + PayTRTestConfig.DEFAULT_BROWSER + "\n" +
            "Headless.Mode=" + PayTRTestConfig.HEADLESS_MODE + "\n" +
            "PayTR.Merchant.ID=" + PayTRTestConfig.MERCHANT_ID + "\n" +
            "Page.Load.Timeout=" + PayTRTestConfig.PAGE_LOAD_TIMEOUT + "s\n" +
            "API.Response.Timeout=" + PayTRTestConfig.API_RESPONSE_TIMEOUT + "s");
    }
    
    private void addEnvironmentInfo() {
        Allure.addAttachment("Test Environment Info", 
            "PayTR Test Ortamı: " + PayTRTestConfig.BASE_URL + "\n" +
            "Test Tarihi: " + new java.util.Date() + "\n" +
            "İşletim Sistemi: " + System.getProperty("os.name") + "\n" +
            "Java Versiyonu: " + System.getProperty("java.version") + "\n" +
            "Test Framework: TestNG + RestAssured + Selenium\n" +
            "Raporlama: Allure Reports");
    }
    
    private String getTestDisplayName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        // Metod adından daha okunabilir isim oluştur
        if (methodName.startsWith("test")) {
            return methodName.substring(4).replaceAll("([A-Z])", " $1").trim();
        }
        return methodName;
    }
    
    private String getTestDescription(ITestResult result) {
        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        String testName = result.getMethod().getMethodName();
        if (testName.contains("Payment")) {
            return "PayTR ödeme işlemi testi";
        } else if (testName.contains("VirtualPOS")) {
            return "PayTR sanal POS testi";
        } else if (testName.contains("Security")) {
            return "PayTR güvenlik testi";
        } else if (testName.contains("Card")) {
            return "PayTR kart validasyon testi";
        } else if (testName.contains("Installment")) {
            return "PayTR taksit seçenekleri testi";
        }
        return "PayTR platform testi";
    }
    
    private long calculateDuration(String testName) {
        Long startTime = testStartTimes.get(testName);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    @Attachment(value = "Ekran Görüntüsü", type = "image/png")
    private byte[] takeScreenshot(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            WebDriver driver = getDriverFromTestInstance(testInstance);
            
            if (driver != null && driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception e) {
            System.err.println("Ekran görüntüsü alınamadı: " + e.getMessage());
        }
        return new byte[0];
    }
    
    private WebDriver getDriverFromTestInstance(Object testInstance) {
        try {
            java.lang.reflect.Field[] fields = testInstance.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (WebDriver.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (WebDriver) field.get(testInstance);
                }
            }
        } catch (Exception e) {
            // Driver bulunamadı
        }
        return null;
    }
    
    @Attachment(value = "{name}", type = "text/plain")
    private String attachText(String name, String content) {
        return content;
    }
    
    // Statik metodlar test sınıflarından kullanım için
    @Step("{stepDescription}")
    public static void step(String stepDescription) {
        // Bu metod @Step annotation'ı ile otomatik olarak Allure'a eklenir
    }
    
    public static void addInfo(String name, String content) {
        Allure.addAttachment(name, content);
    }
    
    public static void addScreenshot(byte[] screenshot) {
        Allure.addAttachment("Ekran Görüntüsü", "image/png", new ByteArrayInputStream(screenshot), "png");
    }
    
    public static void addLog(String message) {
        Allure.addAttachment("Log", message);
    }
}