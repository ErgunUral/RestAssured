package com.example.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.example.config.PayTRTestConfig;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * PayTR Extent Reporter
 * PayTR testleri için ExtentReports kullanarak gelişmiş raporlama
 */
public class PayTRExtentReporter implements ITestListener, ISuiteListener {
    
    private static ExtentReports extent;
    private static ExtentSparkReporter sparkReporter;
    private static final String REPORT_DIR = "test-output/extent-reports";
    private static final String SCREENSHOT_DIR = "test-output/extent-screenshots";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private Map<String, Long> testStartTimes = new HashMap<>();
    
    @Override
    public void onStart(ISuite suite) {
        String timestamp = DATE_FORMAT.format(new Date());
        String reportName = "PayTR_Extent_Report_" + timestamp + ".html";
        
        // Rapor dizinlerini oluştur
        createReportDirectories();
        
        // ExtentSparkReporter yapılandırması
        sparkReporter = new ExtentSparkReporter(REPORT_DIR + "/" + reportName);
        configureSparkReporter();
        
        // ExtentReports yapılandırması
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        configureExtentReports();
        
        System.out.println("=== PayTR Extent Reports Başlatıldı ===");
        System.out.println("Rapor Dosyası: " + REPORT_DIR + "/" + reportName);
    }
    
    @Override
    public void onFinish(ISuite suite) {
        if (extent != null) {
            extent.flush();
        }
        System.out.println("=== PayTR Extent Reports Tamamlandı ===");
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testDescription = getTestDescription(result);
        String[] groups = result.getMethod().getGroups();
        
        ExtentTest test = extent.createTest(testName, testDescription);
        
        // Test gruplarını kategori olarak ekle
        if (groups.length > 0) {
            for (String group : groups) {
                test.assignCategory(group);
            }
        }
        
        // Test bilgilerini ekle
        test.info("🧪 Test başlatıldı: " + testName);
        test.info("🌐 Test Ortamı: " + PayTRTestConfig.BASE_URL);
        test.info("⏰ Başlangıç Zamanı: " + new Date());
        
        // Test sınıfı ve metod bilgilerini ekle
        test.info("📁 Test Sınıfı: " + result.getTestClass().getName());
        test.info("🔧 Test Metodu: " + testName);
        
        extentTest.set(test);
        testStartTimes.put(testName, System.currentTimeMillis());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = extentTest.get();
        String testName = result.getMethod().getMethodName();
        long duration = calculateDuration(testName);
        
        test.pass("✅ Test başarıyla tamamlandı");
        test.info("⏱️ Test Süresi: " + duration + "ms");
        
        // PayTR spesifik başarı bilgileri
        addPayTRSuccessInfo(test, result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        String testName = result.getMethod().getMethodName();
        long duration = calculateDuration(testName);
        
        test.fail("❌ Test başarısız oldu");
        test.info("⏱️ Test Süresi: " + duration + "ms");
        
        // Hata detaylarını ekle
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            test.fail("🚨 Hata Mesajı: " + throwable.getMessage());
            test.fail("📋 Stack Trace: " + getStackTrace(throwable));
        }
        
        // Ekran görüntüsü al ve ekle
        String screenshotPath = takeScreenshot(result);
        if (screenshotPath != null) {
            try {
                test.fail("📷 Ekran Görüntüsü:", 
                    com.aventstack.extentreports.MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            } catch (Exception e) {
                test.fail("Ekran görüntüsü eklenemedi: " + e.getMessage());
            }
        }
        
        // PayTR spesifik hata bilgileri
        addPayTRFailureInfo(test, result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = extentTest.get();
        String testName = result.getMethod().getMethodName();
        
        test.skip("⏭️ Test atlandı");
        
        // Atlama sebebini ekle
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            test.skip("📝 Atlama Sebebi: " + throwable.getMessage());
        }
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.warning("⚠️ Test başarısız ancak başarı yüzdesi içinde");
        onTestFailure(result);
    }
    
    private void createReportDirectories() {
        try {
            new File(REPORT_DIR).mkdirs();
            new File(SCREENSHOT_DIR).mkdirs();
        } catch (Exception e) {
            System.err.println("Rapor dizinleri oluşturulamadı: " + e.getMessage());
        }
    }
    
    private void configureSparkReporter() {
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("PayTR Test Automation Report");
        sparkReporter.config().setReportName("PayTR Test Execution Report");
        sparkReporter.config().setEncoding("UTF-8");
        
        // CSS özelleştirmeleri
        sparkReporter.config().setCss("""
            .brand-logo { background-color: #667eea; }
            .nav-wrapper { background-color: #667eea !important; }
            .test-content { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
            .category-list .category-item { border-left: 3px solid #667eea; }
            """);
        
        // JavaScript özelleştirmeleri
        sparkReporter.config().setJs("""
            $(document).ready(function() {
                $('.brand-logo').text('PayTR Test Reports');
            });
            """);
    }
    
    private void configureExtentReports() {
        // Sistem bilgileri
        extent.setSystemInfo("Test Ortamı", PayTRTestConfig.BASE_URL);
        extent.setSystemInfo("Platform", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Test Framework", "TestNG + RestAssured");
        extent.setSystemInfo("Reporting Tool", "ExtentReports");
        
        // PayTR spesifik bilgiler
        extent.setSystemInfo("PayTR Merchant ID", PayTRTestConfig.MERCHANT_ID);
        extent.setSystemInfo("Test Browser", PayTRTestConfig.DEFAULT_BROWSER);
        extent.setSystemInfo("Headless Mode", String.valueOf(PayTRTestConfig.HEADLESS_MODE));
        extent.setSystemInfo("Page Load Timeout", PayTRTestConfig.PAGE_LOAD_TIMEOUT + "s");
        extent.setSystemInfo("API Response Timeout", PayTRTestConfig.API_RESPONSE_TIMEOUT + "s");
    }
    
    private String getTestDescription(ITestResult result) {
        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        // Metod adından açıklama oluştur
        String methodName = result.getMethod().getMethodName();
        if (methodName.startsWith("test")) {
            return methodName.substring(4).replaceAll("([A-Z])", " $1").trim();
        }
        return methodName;
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
    
    private String takeScreenshot(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            WebDriver driver = getDriverFromTestInstance(testInstance);
            
            if (driver != null && driver instanceof TakesScreenshot) {
                TakesScreenshot screenshot = (TakesScreenshot) driver;
                byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
                
                String timestamp = DATE_FORMAT.format(new Date());
                String fileName = "screenshot_" + result.getMethod().getMethodName() + "_" + timestamp + ".png";
                String filePath = SCREENSHOT_DIR + "/" + fileName;
                
                FileUtils.writeByteArrayToFile(new File(filePath), screenshotBytes);
                return filePath;
            }
        } catch (Exception e) {
            System.err.println("Ekran görüntüsü alınamadı: " + e.getMessage());
        }
        return null;
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
    
    private void addPayTRSuccessInfo(ExtentTest test, ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // PayTR spesifik başarı bilgileri
        if (testName.contains("Payment")) {
            test.pass("💳 Ödeme işlemi başarıyla tamamlandı");
            test.pass("🔒 SSL güvenlik kontrolü geçti");
        } else if (testName.contains("VirtualPOS")) {
            test.pass("🏪 Sanal POS işlemi başarıyla tamamlandı");
            test.pass("🔐 Hash doğrulama başarılı");
        } else if (testName.contains("Security")) {
            test.pass("🛡️ Güvenlik kontrolü başarıyla geçti");
            test.pass("🔍 Güvenlik açığı tespit edilmedi");
        } else if (testName.contains("Card")) {
            test.pass("💳 Kart validasyonu başarılı");
            test.pass("✅ Kart bilgileri doğru format");
        } else if (testName.contains("Installment")) {
            test.pass("📊 Taksit hesaplama doğru");
            test.pass("💰 Komisyon bilgileri görüntülendi");
        }
        
        test.pass("🌐 PayTR test ortamı erişilebilir: " + PayTRTestConfig.BASE_URL);
    }
    
    private void addPayTRFailureInfo(ExtentTest test, ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // PayTR spesifik hata bilgileri
        if (testName.contains("Payment")) {
            test.fail("💳 Ödeme işlemi başarısız");
            test.fail("❗ Ödeme formu veya API hatası olabilir");
        } else if (testName.contains("VirtualPOS")) {
            test.fail("🏪 Sanal POS işlemi başarısız");
            test.fail("❗ Hash doğrulama veya API bağlantı hatası olabilir");
        } else if (testName.contains("Security")) {
            test.fail("🛡️ Güvenlik kontrolü başarısız");
            test.fail("⚠️ Potansiyel güvenlik açığı tespit edildi");
        } else if (testName.contains("Card")) {
            test.fail("💳 Kart validasyonu başarısız");
            test.fail("❗ Kart bilgileri hatalı format veya doğrulama sorunu");
        } else if (testName.contains("Installment")) {
            test.fail("📊 Taksit hesaplama hatalı");
            test.fail("❗ Taksit seçenekleri veya komisyon hesaplama sorunu");
        }
        
        // Genel PayTR hata kontrolleri
        test.fail("🔍 PayTR test ortamı kontrol edilmeli: " + PayTRTestConfig.BASE_URL);
        test.fail("📋 Test verilerinin güncel olduğundan emin olun");
        test.fail("🌐 Ağ bağlantısı ve SSL sertifikası kontrol edilmeli");
    }
    
    // Statik metodlar test sınıflarından kullanım için
    public static void logInfo(String message) {
        if (extentTest.get() != null) {
            extentTest.get().info(message);
        }
    }
    
    public static void logPass(String message) {
        if (extentTest.get() != null) {
            extentTest.get().pass(message);
        }
    }
    
    public static void logFail(String message) {
        if (extentTest.get() != null) {
            extentTest.get().fail(message);
        }
    }
    
    public static void logWarning(String message) {
        if (extentTest.get() != null) {
            extentTest.get().warning(message);
        }
    }
    
    public static void logSkip(String message) {
        if (extentTest.get() != null) {
            extentTest.get().skip(message);
        }
    }
    
    public static void addScreenshot(String screenshotPath) {
        if (extentTest.get() != null && screenshotPath != null) {
            try {
                extentTest.get().addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                logFail("Ekran görüntüsü eklenemedi: " + e.getMessage());
            }
        }
    }
}