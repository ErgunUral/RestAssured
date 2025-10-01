package com.example.utils;

import com.example.config.PayTRTestConfig;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;

/**
 * PayTR Test Reporter
 * PayTR testleri için özelleştirilmiş test raporlama sınıfı
 */
public class PayTRTestReporter implements ITestListener, ISuiteListener {
    
    private static final String REPORT_DIR = "test-output/paytr-reports";
    private static final String SCREENSHOT_DIR = "test-output/screenshots";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    private Map<String, TestResult> testResults = new HashMap<>();
    private Map<String, Long> testStartTimes = new HashMap<>();
    private Instant suiteStartTime;
    private String reportFileName;
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int skippedTests = 0;
    
    // Test sonucu bilgilerini tutan sınıf
    private static class TestResult {
        String testName;
        String status;
        long duration;
        String errorMessage;
        String screenshotPath;
        String testGroup;
        String description;
        
        TestResult(String testName, String status, long duration) {
            this.testName = testName;
            this.status = status;
            this.duration = duration;
        }
    }
    
    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = Instant.now();
        String timestamp = DATE_FORMAT.format(new Date());
        reportFileName = "PayTR_Test_Report_" + timestamp + ".html";
        
        // Rapor dizinlerini oluştur
        createReportDirectories();
        
        System.out.println("=== PayTR Test Suite Başlatıldı ===");
        System.out.println("Suite: " + suite.getName());
        System.out.println("Başlangıç Zamanı: " + new Date());
        System.out.println("Test Ortamı: " + PayTRTestConfig.BASE_URL);
        System.out.println("Rapor Dosyası: " + reportFileName);
    }
    
    @Override
    public void onFinish(ISuite suite) {
        Instant suiteEndTime = Instant.now();
        Duration suiteDuration = Duration.between(suiteStartTime, suiteEndTime);
        
        // HTML raporu oluştur
        generateHTMLReport(suite, suiteDuration);
        
        // JSON raporu oluştur
        generateJSONReport(suite, suiteDuration);
        
        // Konsol özeti yazdır
        printConsoleSummary(suite, suiteDuration);
        
        System.out.println("=== PayTR Test Suite Tamamlandı ===");
        System.out.println("Toplam Süre: " + formatDuration(suiteDuration));
        System.out.println("Rapor Konumu: " + REPORT_DIR + "/" + reportFileName);
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        testStartTimes.put(testName, System.currentTimeMillis());
        totalTests++;
        
        System.out.println("🧪 Test Başlatıldı: " + testName);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateDuration(testName);
        passedTests++;
        
        TestResult testResult = new TestResult(testName, "PASSED", duration);
        testResult.testGroup = getTestGroup(result);
        testResult.description = getTestDescription(result);
        testResults.put(testName, testResult);
        
        System.out.println("✅ Test Geçti: " + testName + " (" + duration + "ms)");
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateDuration(testName);
        failedTests++;
        
        TestResult testResult = new TestResult(testName, "FAILED", duration);
        testResult.testGroup = getTestGroup(result);
        testResult.description = getTestDescription(result);
        testResult.errorMessage = getErrorMessage(result);
        testResult.screenshotPath = takeScreenshot(result);
        testResults.put(testName, testResult);
        
        System.out.println("❌ Test Başarısız: " + testName + " (" + duration + "ms)");
        System.out.println("   Hata: " + testResult.errorMessage);
        if (testResult.screenshotPath != null) {
            System.out.println("   Ekran Görüntüsü: " + testResult.screenshotPath);
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);
        long duration = calculateDuration(testName);
        skippedTests++;
        
        TestResult testResult = new TestResult(testName, "SKIPPED", duration);
        testResult.testGroup = getTestGroup(result);
        testResult.description = getTestDescription(result);
        testResult.errorMessage = getErrorMessage(result);
        testResults.put(testName, testResult);
        
        System.out.println("⏭️ Test Atlandı: " + testName);
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
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
    
    private String getTestName(ITestResult result) {
        return result.getMethod().getMethodName();
    }
    
    private String getTestGroup(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        return groups.length > 0 ? String.join(", ", groups) : "default";
    }
    
    private String getTestDescription(ITestResult result) {
        String description = result.getMethod().getDescription();
        return description != null ? description : "";
    }
    
    private String getErrorMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            String message = throwable.getMessage();
            return message != null ? message : throwable.getClass().getSimpleName();
        }
        return "";
    }
    
    private long calculateDuration(String testName) {
        Long startTime = testStartTimes.get(testName);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    private String takeScreenshot(ITestResult result) {
        try {
            Object testInstance = result.getInstance();
            WebDriver driver = getDriverFromTestInstance(testInstance);
            
            if (driver != null && driver instanceof TakesScreenshot) {
                TakesScreenshot screenshot = (TakesScreenshot) driver;
                byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
                
                String timestamp = DATE_FORMAT.format(new Date());
                String fileName = "screenshot_" + getTestName(result) + "_" + timestamp + ".png";
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
            // Reflection ile driver field'ını bul
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
    
    private void generateHTMLReport(ISuite suite, Duration suiteDuration) {
        try {
            String htmlContent = buildHTMLReport(suite, suiteDuration);
            String filePath = REPORT_DIR + "/" + reportFileName;
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(htmlContent);
            }
            
            System.out.println("HTML raporu oluşturuldu: " + filePath);
        } catch (IOException e) {
            System.err.println("HTML raporu oluşturulamadı: " + e.getMessage());
        }
    }
    
    private String buildHTMLReport(ISuite suite, Duration suiteDuration) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='tr'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>PayTR Test Raporu</title>\n");
        html.append("    <style>\n");
        html.append(getHTMLStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("    <div class='header'>\n");
        html.append("        <h1>🏦 PayTR Test Raporu</h1>\n");
        html.append("        <div class='info'>\n");
        html.append("            <p><strong>Test Ortamı:</strong> ").append(PayTRTestConfig.BASE_URL).append("</p>\n");
        html.append("            <p><strong>Tarih:</strong> ").append(new Date()).append("</p>\n");
        html.append("            <p><strong>Toplam Süre:</strong> ").append(formatDuration(suiteDuration)).append("</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Summary
        html.append("    <div class='summary'>\n");
        html.append("        <h2>📊 Test Özeti</h2>\n");
        html.append("        <div class='stats'>\n");
        html.append("            <div class='stat total'>\n");
        html.append("                <span class='number'>").append(totalTests).append("</span>\n");
        html.append("                <span class='label'>Toplam Test</span>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat passed'>\n");
        html.append("                <span class='number'>").append(passedTests).append("</span>\n");
        html.append("                <span class='label'>Geçen</span>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat failed'>\n");
        html.append("                <span class='number'>").append(failedTests).append("</span>\n");
        html.append("                <span class='label'>Başarısız</span>\n");
        html.append("            </div>\n");
        html.append("            <div class='stat skipped'>\n");
        html.append("                <span class='number'>").append(skippedTests).append("</span>\n");
        html.append("                <span class='label'>Atlanan</span>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Success rate
        double successRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        html.append("        <div class='success-rate'>\n");
        html.append("            <h3>Başarı Oranı: %").append(String.format("%.1f", successRate)).append("</h3>\n");
        html.append("            <div class='progress-bar'>\n");
        html.append("                <div class='progress' style='width: ").append(successRate).append("%'></div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Test Results
        html.append("    <div class='results'>\n");
        html.append("        <h2>🧪 Test Sonuçları</h2>\n");
        html.append("        <table>\n");
        html.append("            <thead>\n");
        html.append("                <tr>\n");
        html.append("                    <th>Test Adı</th>\n");
        html.append("                    <th>Durum</th>\n");
        html.append("                    <th>Süre</th>\n");
        html.append("                    <th>Grup</th>\n");
        html.append("                    <th>Açıklama</th>\n");
        html.append("                    <th>Hata</th>\n");
        html.append("                </tr>\n");
        html.append("            </thead>\n");
        html.append("            <tbody>\n");
        
        for (TestResult result : testResults.values()) {
            html.append("                <tr class='").append(result.status.toLowerCase()).append("'>\n");
            html.append("                    <td>").append(result.testName).append("</td>\n");
            html.append("                    <td><span class='status ").append(result.status.toLowerCase()).append("'>")
                .append(getStatusIcon(result.status)).append(" ").append(result.status).append("</span></td>\n");
            html.append("                    <td>").append(result.duration).append("ms</td>\n");
            html.append("                    <td>").append(result.testGroup != null ? result.testGroup : "").append("</td>\n");
            html.append("                    <td>").append(result.description != null ? result.description : "").append("</td>\n");
            html.append("                    <td>");
            if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
                html.append("<div class='error-message'>").append(result.errorMessage).append("</div>");
            }
            if (result.screenshotPath != null) {
                html.append("<a href='").append(result.screenshotPath).append("' target='_blank'>📷 Ekran Görüntüsü</a>");
            }
            html.append("</td>\n");
            html.append("                </tr>\n");
        }
        
        html.append("            </tbody>\n");
        html.append("        </table>\n");
        html.append("    </div>\n");
        
        // Footer
        html.append("    <div class='footer'>\n");
        html.append("        <p>PayTR Test Automation Framework - ").append(new Date()).append("</p>\n");
        html.append("    </div>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    private String getHTMLStyles() {
        return """
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
            .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 20px; }
            .header h1 { margin: 0; font-size: 2.5em; }
            .header .info { margin-top: 15px; }
            .header .info p { margin: 5px 0; }
            .summary { background: white; padding: 25px; border-radius: 10px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
            .summary h2 { margin-top: 0; color: #333; }
            .stats { display: flex; gap: 20px; margin: 20px 0; }
            .stat { text-align: center; padding: 20px; border-radius: 8px; flex: 1; }
            .stat.total { background-color: #e3f2fd; color: #1976d2; }
            .stat.passed { background-color: #e8f5e8; color: #2e7d32; }
            .stat.failed { background-color: #ffebee; color: #c62828; }
            .stat.skipped { background-color: #fff3e0; color: #ef6c00; }
            .stat .number { display: block; font-size: 2em; font-weight: bold; }
            .stat .label { font-size: 0.9em; }
            .success-rate { margin-top: 20px; }
            .progress-bar { width: 100%; height: 20px; background-color: #e0e0e0; border-radius: 10px; overflow: hidden; }
            .progress { height: 100%; background: linear-gradient(90deg, #4caf50, #8bc34a); transition: width 0.3s ease; }
            .results { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
            .results h2 { margin-top: 0; color: #333; }
            table { width: 100%; border-collapse: collapse; margin-top: 15px; }
            th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
            th { background-color: #f8f9fa; font-weight: 600; }
            tr.passed { background-color: #f1f8e9; }
            tr.failed { background-color: #ffebee; }
            tr.skipped { background-color: #fff8e1; }
            .status { padding: 4px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 500; }
            .status.passed { background-color: #c8e6c9; color: #2e7d32; }
            .status.failed { background-color: #ffcdd2; color: #c62828; }
            .status.skipped { background-color: #ffe0b2; color: #ef6c00; }
            .error-message { font-size: 0.85em; color: #d32f2f; background-color: #ffebee; padding: 5px; border-radius: 3px; margin-bottom: 5px; }
            .footer { text-align: center; margin-top: 30px; color: #666; font-size: 0.9em; }
            a { color: #1976d2; text-decoration: none; }
            a:hover { text-decoration: underline; }
            """;
    }
    
    private String getStatusIcon(String status) {
        switch (status) {
            case "PASSED": return "✅";
            case "FAILED": return "❌";
            case "SKIPPED": return "⏭️";
            default: return "❓";
        }
    }
    
    private void generateJSONReport(ISuite suite, Duration suiteDuration) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"suiteName\": \"").append(suite.getName()).append("\",\n");
            json.append("  \"environment\": \"").append(PayTRTestConfig.BASE_URL).append("\",\n");
            json.append("  \"startTime\": \"").append(suiteStartTime).append("\",\n");
            json.append("  \"duration\": ").append(suiteDuration.toMillis()).append(",\n");
            json.append("  \"summary\": {\n");
            json.append("    \"total\": ").append(totalTests).append(",\n");
            json.append("    \"passed\": ").append(passedTests).append(",\n");
            json.append("    \"failed\": ").append(failedTests).append(",\n");
            json.append("    \"skipped\": ").append(skippedTests).append(",\n");
            json.append("    \"successRate\": ").append(totalTests > 0 ? (double) passedTests / totalTests * 100 : 0).append("\n");
            json.append("  },\n");
            json.append("  \"tests\": [\n");
            
            List<TestResult> results = new ArrayList<>(testResults.values());
            for (int i = 0; i < results.size(); i++) {
                TestResult result = results.get(i);
                json.append("    {\n");
                json.append("      \"name\": \"").append(result.testName).append("\",\n");
                json.append("      \"status\": \"").append(result.status).append("\",\n");
                json.append("      \"duration\": ").append(result.duration).append(",\n");
                json.append("      \"group\": \"").append(result.testGroup != null ? result.testGroup : "").append("\",\n");
                json.append("      \"description\": \"").append(result.description != null ? result.description : "").append("\",\n");
                json.append("      \"errorMessage\": \"").append(result.errorMessage != null ? result.errorMessage.replace("\"", "\\\"") : "").append("\",\n");
                json.append("      \"screenshotPath\": \"").append(result.screenshotPath != null ? result.screenshotPath : "").append("\"\n");
                json.append("    }");
                if (i < results.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ]\n");
            json.append("}");
            
            String jsonFileName = reportFileName.replace(".html", ".json");
            String filePath = REPORT_DIR + "/" + jsonFileName;
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(json.toString());
            }
            
            System.out.println("JSON raporu oluşturuldu: " + filePath);
        } catch (IOException e) {
            System.err.println("JSON raporu oluşturulamadı: " + e.getMessage());
        }
    }
    
    private void printConsoleSummary(ISuite suite, Duration suiteDuration) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏦 PAYTR TEST RAPORU ÖZETİ");
        System.out.println("=".repeat(60));
        System.out.println("Test Ortamı: " + PayTRTestConfig.BASE_URL);
        System.out.println("Toplam Süre: " + formatDuration(suiteDuration));
        System.out.println();
        System.out.println("📊 TEST İSTATİSTİKLERİ:");
        System.out.println("  Toplam Test: " + totalTests);
        System.out.println("  ✅ Geçen: " + passedTests);
        System.out.println("  ❌ Başarısız: " + failedTests);
        System.out.println("  ⏭️ Atlanan: " + skippedTests);
        
        double successRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        System.out.println("  🎯 Başarı Oranı: %" + String.format("%.1f", successRate));
        
        if (failedTests > 0) {
            System.out.println("\n❌ BAŞARISIZ TESTLER:");
            testResults.values().stream()
                .filter(result -> "FAILED".equals(result.status))
                .forEach(result -> System.out.println("  - " + result.testName + ": " + result.errorMessage));
        }
        
        System.out.println("\n📁 RAPOR DOSYALARI:");
        System.out.println("  HTML: " + REPORT_DIR + "/" + reportFileName);
        System.out.println("  JSON: " + REPORT_DIR + "/" + reportFileName.replace(".html", ".json"));
        if (failedTests > 0) {
            System.out.println("  Ekran Görüntüleri: " + SCREENSHOT_DIR);
        }
        System.out.println("=".repeat(60));
    }
    
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d dakika %d saniye", minutes, seconds);
        } else {
            return String.format("%d saniye", seconds);
        }
    }
}