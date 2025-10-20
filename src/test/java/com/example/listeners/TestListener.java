package com.example.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ITestContext;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import io.qameta.allure.Attachment;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * PayTR Test Listener
 * Test execution lifecycle'ını dinler ve raporlama yapar
 */
public class TestListener implements ITestListener, ISuiteListener {
    
    private static final Logger logger = Logger.getLogger(TestListener.class.getName());
    private long startTime;
    private long endTime;
    
    @Override
    public void onStart(ITestContext context) {
        startTime = System.currentTimeMillis();
        logger.info("=== PayTR Test Suite Başladı ===");
        logger.info("Test Suite: " + context.getName());
        logger.info("Başlangıç Zamanı: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        logger.info("Toplam Test Sayısı: " + context.getAllTestMethods().length);
        
        // Test environment bilgilerini logla
        String browser = context.getCurrentXmlTest().getParameter("browser");
        String environment = context.getCurrentXmlTest().getParameter("environment");
        
        if (browser != null) {
            logger.info("Browser: " + browser);
        }
        if (environment != null) {
            logger.info("Environment: " + environment);
        }
    }
    
    @Override
    public void onFinish(ITestContext context) {
        endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        logger.info("=== PayTR Test Suite Tamamlandı ===");
        logger.info("Bitiş Zamanı: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        logger.info("Toplam Süre: " + (duration / 1000) + " saniye");
        logger.info("Başarılı Testler: " + context.getPassedTests().size());
        logger.info("Başarısız Testler: " + context.getFailedTests().size());
        logger.info("Atlanan Testler: " + context.getSkippedTests().size());
        
        // Test sonuçlarını detaylı logla
        logTestResults(context);
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test Başladı: " + result.getMethod().getMethodName());
        logger.info("Test Sınıfı: " + result.getTestClass().getName());
        
        // Test gruplarını logla
        String[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            logger.info("Test Grupları: " + String.join(", ", groups));
        }
        
        // Test prioritysini logla
        int priority = result.getMethod().getPriority();
        logger.info("Test Priority: " + priority);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("✅ Test BAŞARILI: " + result.getMethod().getMethodName() + 
                   " (" + duration + "ms)");
        
        // Başarılı test için screenshot al (opsiyonel)
        takeScreenshotOnSuccess(result);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.severe("❌ Test BAŞARISIZ: " + result.getMethod().getMethodName() + 
                     " (" + duration + "ms)");
        logger.severe("Hata: " + result.getThrowable().getMessage());
        
        // Başarısız test için screenshot al
        takeScreenshotOnFailure(result);
        
        // Stack trace'i logla
        logStackTrace(result.getThrowable());
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warning("⏭️ Test ATLANDI: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            logger.warning("Atlama Nedeni: " + result.getThrowable().getMessage());
        }
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warning("⚠️ Test KISMEN BAŞARILI: " + result.getMethod().getMethodName());
    }
    
    public void onSuiteStart(ISuite suite) {
        logger.info("=== Test Suite Başlatılıyor: " + suite.getName() + " ===");
        
        // Suite parametrelerini logla
        suite.getXmlSuite().getParameters().forEach((key, value) -> 
            logger.info("Suite Parameter - " + key + ": " + value));
    }
    
    public void onSuiteFinish(ISuite suite) {
        logger.info("=== Test Suite Tamamlandı: " + suite.getName() + " ===");
        
        // Suite sonuçlarını logla
        suite.getResults().forEach((testName, suiteResult) -> {
            logger.info("Test: " + testName);
            logger.info("  Başarılı: " + suiteResult.getTestContext().getPassedTests().size());
            logger.info("  Başarısız: " + suiteResult.getTestContext().getFailedTests().size());
            logger.info("  Atlanan: " + suiteResult.getTestContext().getSkippedTests().size());
        });
    }
    
    /**
     * Başarısız test için screenshot al
     */
    @Attachment(value = "Screenshot on Failure", type = "image/png")
    private byte[] takeScreenshotOnFailure(ITestResult result) {
        try {
            WebDriver driver = getDriverFromResult(result);
            if (driver != null && driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                logger.info("Screenshot alındı: " + result.getMethod().getMethodName());
                return screenshot;
            }
        } catch (Exception e) {
            logger.warning("Screenshot alınamadı: " + e.getMessage());
        }
        return new byte[0];
    }
    
    /**
     * Başarılı test için screenshot al (opsiyonel)
     */
    @Attachment(value = "Screenshot on Success", type = "image/png")
    private byte[] takeScreenshotOnSuccess(ITestResult result) {
        try {
            // Sadece UI testleri için screenshot al
            if (isUITest(result)) {
                WebDriver driver = getDriverFromResult(result);
                if (driver != null && driver instanceof TakesScreenshot) {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    logger.info("Başarı screenshot'ı alındı: " + result.getMethod().getMethodName());
                    return screenshot;
                }
            }
        } catch (Exception e) {
            logger.warning("Başarı screenshot'ı alınamadı: " + e.getMessage());
        }
        return new byte[0];
    }
    
    /**
     * Test sonucundan WebDriver instance'ını al
     */
    private WebDriver getDriverFromResult(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            
            // Reflection ile driver field'ını bul
            Class<?> testClass = testInstance.getClass();
            
            // driver field'ını ara
            try {
                java.lang.reflect.Field driverField = testClass.getDeclaredField("driver");
                driverField.setAccessible(true);
                return (WebDriver) driverField.get(testInstance);
            } catch (NoSuchFieldException e) {
                // Superclass'larda ara
                Class<?> superClass = testClass.getSuperclass();
                while (superClass != null) {
                    try {
                        java.lang.reflect.Field driverField = superClass.getDeclaredField("driver");
                        driverField.setAccessible(true);
                        return (WebDriver) driverField.get(testInstance);
                    } catch (NoSuchFieldException ex) {
                        superClass = superClass.getSuperclass();
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("WebDriver instance bulunamadı: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Test'in UI testi olup olmadığını kontrol et
     */
    private boolean isUITest(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        for (String group : groups) {
            if (group.contains("ui") || group.contains("selenium") || group.contains("web")) {
                return true;
            }
        }
        
        // Test sınıfı adına göre kontrol et
        String className = result.getTestClass().getName();
        return className.contains("UI") || className.contains("Web") || className.contains("Selenium");
    }
    
    /**
     * Test sonuçlarını detaylı logla
     */
    private void logTestResults(ITestContext context) {
        logger.info("=== DETAYLI TEST SONUÇLARI ===");
        
        // Başarılı testler
        if (!context.getPassedTests().getAllResults().isEmpty()) {
            logger.info("BAŞARILI TESTLER:");
            context.getPassedTests().getAllResults().forEach(result -> {
                long duration = result.getEndMillis() - result.getStartMillis();
                logger.info("  ✅ " + result.getMethod().getMethodName() + " (" + duration + "ms)");
            });
        }
        
        // Başarısız testler
        if (!context.getFailedTests().getAllResults().isEmpty()) {
            logger.severe("BAŞARISIZ TESTLER:");
            context.getFailedTests().getAllResults().forEach(result -> {
                long duration = result.getEndMillis() - result.getStartMillis();
                logger.severe("  ❌ " + result.getMethod().getMethodName() + " (" + duration + "ms)");
                logger.severe("     Hata: " + result.getThrowable().getMessage());
            });
        }
        
        // Atlanan testler
        if (!context.getSkippedTests().getAllResults().isEmpty()) {
            logger.warning("ATLANAN TESTLER:");
            context.getSkippedTests().getAllResults().forEach(result -> {
                logger.warning("  ⏭️ " + result.getMethod().getMethodName());
                if (result.getThrowable() != null) {
                    logger.warning("     Neden: " + result.getThrowable().getMessage());
                }
            });
        }
    }
    
    /**
     * Stack trace'i logla
     */
    private void logStackTrace(Throwable throwable) {
        if (throwable != null) {
            logger.log(Level.SEVERE, "Stack Trace:", throwable);
        }
    }
    
    /**
     * Test execution log'unu dosyaya yaz
     */
    @Attachment(value = "Test Execution Log", type = "text/plain")
    public String attachTestLog() {
        StringBuilder logContent = new StringBuilder();
        logContent.append("PayTR Test Execution Log\n");
        logContent.append("========================\n");
        logContent.append("Execution Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        logContent.append("Total Duration: ").append((endTime - startTime) / 1000).append(" seconds\n");
        
        return logContent.toString();
    }
}