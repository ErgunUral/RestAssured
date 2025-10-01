package com.example.utils;

import com.example.config.PayTRTestConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * PayTR Report Manager
 * PayTR test raporlarını yönetmek ve birleştirmek için yardımcı sınıf
 */
public class PayTRReportManager {
    
    private static final String REPORTS_BASE_DIR = "test-output";
    private static final String CONSOLIDATED_REPORTS_DIR = "test-output/consolidated-reports";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Tüm test raporlarını birleştirir ve konsolide rapor oluşturur
     */
    public static void consolidateReports() {
        try {
            createConsolidatedReportsDirectory();
            
            String timestamp = DATE_FORMAT.format(new Date());
            String consolidatedReportName = "PayTR_Consolidated_Report_" + timestamp + ".html";
            
            // Mevcut raporları topla
            ReportSummary summary = collectReportSummaries();
            
            // Konsolide HTML raporu oluştur
            generateConsolidatedHTMLReport(consolidatedReportName, summary);
            
            // Konsolide JSON raporu oluştur
            generateConsolidatedJSONReport(consolidatedReportName.replace(".html", ".json"), summary);
            
            // Rapor indeksi oluştur
            generateReportIndex();
            
            System.out.println("=== PayTR Konsolide Rapor Oluşturuldu ===");
            System.out.println("Konum: " + CONSOLIDATED_REPORTS_DIR + "/" + consolidatedReportName);
            
        } catch (Exception e) {
            System.err.println("Konsolide rapor oluşturulamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Eski raporları temizler (7 günden eski)
     */
    public static void cleanupOldReports() {
        try {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            
            // Test output dizinindeki eski dosyaları temizle
            cleanupDirectory(REPORTS_BASE_DIR, sevenDaysAgo);
            
            System.out.println("Eski raporlar temizlendi (7 günden eski)");
            
        } catch (Exception e) {
            System.err.println("Eski raporlar temizlenemedi: " + e.getMessage());
        }
    }
    
    /**
     * Test raporlarının özetini çıkarır
     */
    public static void generateReportSummary() {
        try {
            ReportSummary summary = collectReportSummaries();
            printReportSummary(summary);
            
        } catch (Exception e) {
            System.err.println("Rapor özeti oluşturulamadı: " + e.getMessage());
        }
    }
    
    /**
     * Rapor dizinlerini oluşturur
     */
    private static void createConsolidatedReportsDirectory() {
        try {
            new File(CONSOLIDATED_REPORTS_DIR).mkdirs();
        } catch (Exception e) {
            System.err.println("Konsolide rapor dizini oluşturulamadı: " + e.getMessage());
        }
    }
    
    /**
     * Mevcut raporların özetini toplar
     */
    private static ReportSummary collectReportSummaries() {
        ReportSummary summary = new ReportSummary();
        
        // HTML raporlarını tara
        collectHTMLReports(summary);
        
        // JSON raporlarını tara
        collectJSONReports(summary);
        
        // ExtentReports raporlarını tara
        collectExtentReports(summary);
        
        // Allure raporlarını tara
        collectAllureReports(summary);
        
        // TestNG raporlarını tara
        collectTestNGReports(summary);
        
        return summary;
    }
    
    private static void collectHTMLReports(ReportSummary summary) {
        try {
            Path reportsDir = Paths.get(REPORTS_BASE_DIR + "/paytr-reports");
            if (Files.exists(reportsDir)) {
                try (Stream<Path> files = Files.list(reportsDir)) {
                    files.filter(path -> path.toString().endsWith(".html"))
                         .forEach(path -> {
                             ReportInfo info = new ReportInfo();
                             info.type = "Custom HTML";
                             info.path = path.toString();
                             info.name = path.getFileName().toString();
                             info.size = getFileSize(path.toFile());
                             info.lastModified = new Date(path.toFile().lastModified());
                             summary.reports.add(info);
                         });
                }
            }
        } catch (Exception e) {
            System.err.println("HTML raporları toplanamadı: " + e.getMessage());
        }
    }
    
    private static void collectJSONReports(ReportSummary summary) {
        try {
            Path reportsDir = Paths.get(REPORTS_BASE_DIR + "/paytr-reports");
            if (Files.exists(reportsDir)) {
                try (Stream<Path> files = Files.list(reportsDir)) {
                    files.filter(path -> path.toString().endsWith(".json"))
                         .forEach(path -> {
                             ReportInfo info = new ReportInfo();
                             info.type = "JSON";
                             info.path = path.toString();
                             info.name = path.getFileName().toString();
                             info.size = getFileSize(path.toFile());
                             info.lastModified = new Date(path.toFile().lastModified());
                             summary.reports.add(info);
                         });
                }
            }
        } catch (Exception e) {
            System.err.println("JSON raporları toplanamadı: " + e.getMessage());
        }
    }
    
    private static void collectExtentReports(ReportSummary summary) {
        try {
            Path reportsDir = Paths.get(REPORTS_BASE_DIR + "/extent-reports");
            if (Files.exists(reportsDir)) {
                try (Stream<Path> files = Files.list(reportsDir)) {
                    files.filter(path -> path.toString().endsWith(".html"))
                         .forEach(path -> {
                             ReportInfo info = new ReportInfo();
                             info.type = "ExtentReports";
                             info.path = path.toString();
                             info.name = path.getFileName().toString();
                             info.size = getFileSize(path.toFile());
                             info.lastModified = new Date(path.toFile().lastModified());
                             summary.reports.add(info);
                         });
                }
            }
        } catch (Exception e) {
            System.err.println("ExtentReports raporları toplanamadı: " + e.getMessage());
        }
    }
    
    private static void collectAllureReports(ReportSummary summary) {
        try {
            Path allureResults = Paths.get("allure-results");
            if (Files.exists(allureResults)) {
                ReportInfo info = new ReportInfo();
                info.type = "Allure";
                info.path = allureResults.toString();
                info.name = "Allure Results";
                info.size = getDirectorySize(allureResults.toFile());
                info.lastModified = new Date();
                summary.reports.add(info);
            }
        } catch (Exception e) {
            System.err.println("Allure raporları toplanamadı: " + e.getMessage());
        }
    }
    
    private static void collectTestNGReports(ReportSummary summary) {
        try {
            Path testOutputDir = Paths.get(REPORTS_BASE_DIR);
            if (Files.exists(testOutputDir)) {
                // emailable-report.html
                Path emailableReport = testOutputDir.resolve("emailable-report.html");
                if (Files.exists(emailableReport)) {
                    ReportInfo info = new ReportInfo();
                    info.type = "TestNG Emailable";
                    info.path = emailableReport.toString();
                    info.name = "emailable-report.html";
                    info.size = getFileSize(emailableReport.toFile());
                    info.lastModified = new Date(emailableReport.toFile().lastModified());
                    summary.reports.add(info);
                }
                
                // index.html
                Path indexReport = testOutputDir.resolve("index.html");
                if (Files.exists(indexReport)) {
                    ReportInfo info = new ReportInfo();
                    info.type = "TestNG Index";
                    info.path = indexReport.toString();
                    info.name = "index.html";
                    info.size = getFileSize(indexReport.toFile());
                    info.lastModified = new Date(indexReport.toFile().lastModified());
                    summary.reports.add(info);
                }
            }
        } catch (Exception e) {
            System.err.println("TestNG raporları toplanamadı: " + e.getMessage());
        }
    }
    
    private static void generateConsolidatedHTMLReport(String fileName, ReportSummary summary) {
        try {
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang='tr'>\n");
            html.append("<head>\n");
            html.append("    <meta charset='UTF-8'>\n");
            html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
            html.append("    <title>PayTR Konsolide Test Raporu</title>\n");
            html.append("    <style>\n");
            html.append(getConsolidatedReportStyles());
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            
            // Header
            html.append("    <div class='header'>\n");
            html.append("        <h1>🏦 PayTR Konsolide Test Raporu</h1>\n");
            html.append("        <div class='info'>\n");
            html.append("            <p><strong>Test Ortamı:</strong> ").append(PayTRTestConfig.BASE_URL).append("</p>\n");
            html.append("            <p><strong>Rapor Tarihi:</strong> ").append(new Date()).append("</p>\n");
            html.append("            <p><strong>Toplam Rapor Sayısı:</strong> ").append(summary.reports.size()).append("</p>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            // Summary
            html.append("    <div class='summary'>\n");
            html.append("        <h2>📊 Rapor Özeti</h2>\n");
            html.append("        <div class='report-types'>\n");
            
            Map<String, Integer> reportTypeCounts = new HashMap<>();
            for (ReportInfo report : summary.reports) {
                reportTypeCounts.put(report.type, reportTypeCounts.getOrDefault(report.type, 0) + 1);
            }
            
            for (Map.Entry<String, Integer> entry : reportTypeCounts.entrySet()) {
                html.append("            <div class='report-type'>\n");
                html.append("                <span class='type-name'>").append(entry.getKey()).append("</span>\n");
                html.append("                <span class='type-count'>").append(entry.getValue()).append("</span>\n");
                html.append("            </div>\n");
            }
            
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            // Reports List
            html.append("    <div class='reports-list'>\n");
            html.append("        <h2>📁 Mevcut Raporlar</h2>\n");
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>Rapor Adı</th>\n");
            html.append("                    <th>Tür</th>\n");
            html.append("                    <th>Boyut</th>\n");
            html.append("                    <th>Son Değişiklik</th>\n");
            html.append("                    <th>Konum</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");
            
            for (ReportInfo report : summary.reports) {
                html.append("                <tr>\n");
                html.append("                    <td><a href='").append(report.path).append("' target='_blank'>").append(report.name).append("</a></td>\n");
                html.append("                    <td><span class='report-type-badge ").append(report.type.toLowerCase().replace(" ", "-")).append("'>").append(report.type).append("</span></td>\n");
                html.append("                    <td>").append(formatFileSize(report.size)).append("</td>\n");
                html.append("                    <td>").append(report.lastModified).append("</td>\n");
                html.append("                    <td><code>").append(report.path).append("</code></td>\n");
                html.append("                </tr>\n");
            }
            
            html.append("            </tbody>\n");
            html.append("        </table>\n");
            html.append("    </div>\n");
            
            // Quick Actions
            html.append("    <div class='quick-actions'>\n");
            html.append("        <h2>⚡ Hızlı Erişim</h2>\n");
            html.append("        <div class='actions'>\n");
            html.append("            <a href='../paytr-reports/' class='action-btn'>📊 Custom Reports</a>\n");
            html.append("            <a href='../extent-reports/' class='action-btn'>📈 ExtentReports</a>\n");
            html.append("            <a href='../emailable-report.html' class='action-btn'>📧 TestNG Email Report</a>\n");
            html.append("            <a href='../index.html' class='action-btn'>🏠 TestNG Index</a>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
            
            // Footer
            html.append("    <div class='footer'>\n");
            html.append("        <p>PayTR Test Automation Framework - Konsolide Rapor - ").append(new Date()).append("</p>\n");
            html.append("    </div>\n");
            
            html.append("</body>\n");
            html.append("</html>");
            
            String filePath = CONSOLIDATED_REPORTS_DIR + "/" + fileName;
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(html.toString());
            }
            
            System.out.println("Konsolide HTML raporu oluşturuldu: " + filePath);
            
        } catch (IOException e) {
            System.err.println("Konsolide HTML raporu oluşturulamadı: " + e.getMessage());
        }
    }
    
    private static void generateConsolidatedJSONReport(String fileName, ReportSummary summary) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"reportTitle\": \"PayTR Konsolide Test Raporu\",\n");
            json.append("  \"generatedAt\": \"").append(new Date()).append("\",\n");
            json.append("  \"environment\": \"").append(PayTRTestConfig.BASE_URL).append("\",\n");
            json.append("  \"totalReports\": ").append(summary.reports.size()).append(",\n");
            json.append("  \"reports\": [\n");
            
            for (int i = 0; i < summary.reports.size(); i++) {
                ReportInfo report = summary.reports.get(i);
                json.append("    {\n");
                json.append("      \"name\": \"").append(report.name).append("\",\n");
                json.append("      \"type\": \"").append(report.type).append("\",\n");
                json.append("      \"path\": \"").append(report.path).append("\",\n");
                json.append("      \"size\": ").append(report.size).append(",\n");
                json.append("      \"lastModified\": \"").append(report.lastModified).append("\"\n");
                json.append("    }");
                if (i < summary.reports.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ]\n");
            json.append("}");
            
            String filePath = CONSOLIDATED_REPORTS_DIR + "/" + fileName;
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(json.toString());
            }
            
            System.out.println("Konsolide JSON raporu oluşturuldu: " + filePath);
            
        } catch (IOException e) {
            System.err.println("Konsolide JSON raporu oluşturulamadı: " + e.getMessage());
        }
    }
    
    private static void generateReportIndex() {
        try {
            String indexContent = """
                <!DOCTYPE html>
                <html lang='tr'>
                <head>
                    <meta charset='UTF-8'>
                    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
                    <title>PayTR Test Reports Index</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
                        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        h1 { color: #333; text-align: center; margin-bottom: 30px; }
                        .report-links { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
                        .report-link { display: block; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 8px; text-align: center; transition: transform 0.2s; }
                        .report-link:hover { transform: translateY(-2px); }
                        .report-link h3 { margin: 0 0 10px 0; }
                        .report-link p { margin: 0; opacity: 0.9; }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <h1>🏦 PayTR Test Reports</h1>
                        <div class='report-links'>
                            <a href='consolidated-reports/' class='report-link'>
                                <h3>📊 Konsolide Raporlar</h3>
                                <p>Tüm test raporlarının birleştirilmiş görünümü</p>
                            </a>
                            <a href='paytr-reports/' class='report-link'>
                                <h3>📋 Custom HTML Raporlar</h3>
                                <p>PayTR özel HTML test raporları</p>
                            </a>
                            <a href='extent-reports/' class='report-link'>
                                <h3>📈 ExtentReports</h3>
                                <p>Gelişmiş ExtentReports raporları</p>
                            </a>
                            <a href='emailable-report.html' class='report-link'>
                                <h3>📧 TestNG Email Report</h3>
                                <p>TestNG e-posta raporu</p>
                            </a>
                            <a href='index.html' class='report-link'>
                                <h3>🏠 TestNG Index</h3>
                                <p>TestNG ana indeks sayfası</p>
                            </a>
                        </div>
                    </div>
                </body>
                </html>
                """;
            
            String filePath = REPORTS_BASE_DIR + "/reports-index.html";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(indexContent);
            }
            
            System.out.println("Rapor indeksi oluşturuldu: " + filePath);
            
        } catch (IOException e) {
            System.err.println("Rapor indeksi oluşturulamadı: " + e.getMessage());
        }
    }
    
    private static void cleanupDirectory(String dirPath, long cutoffTime) throws IOException {
        Path directory = Paths.get(dirPath);
        if (Files.exists(directory)) {
            try (Stream<Path> files = Files.walk(directory)) {
                files.filter(Files::isRegularFile)
                     .filter(path -> path.toFile().lastModified() < cutoffTime)
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                             System.out.println("Silindi: " + path);
                         } catch (IOException e) {
                             System.err.println("Silinemedi: " + path + " - " + e.getMessage());
                         }
                     });
            }
        }
    }
    
    private static void printReportSummary(ReportSummary summary) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏦 PAYTR TEST RAPORLARI ÖZETİ");
        System.out.println("=".repeat(60));
        System.out.println("Toplam Rapor Sayısı: " + summary.reports.size());
        
        Map<String, Integer> typeCounts = new HashMap<>();
        for (ReportInfo report : summary.reports) {
            typeCounts.put(report.type, typeCounts.getOrDefault(report.type, 0) + 1);
        }
        
        System.out.println("\nRapor Türleri:");
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println("\nSon Raporlar:");
        summary.reports.stream()
                .sorted((a, b) -> b.lastModified.compareTo(a.lastModified))
                .limit(5)
                .forEach(report -> System.out.println("  " + report.name + " (" + report.type + ") - " + report.lastModified));
        
        System.out.println("=".repeat(60));
    }
    
    private static String getConsolidatedReportStyles() {
        return """
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
            .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 20px; }
            .header h1 { margin: 0; font-size: 2.5em; }
            .header .info { margin-top: 15px; }
            .header .info p { margin: 5px 0; }
            .summary, .reports-list, .quick-actions { background: white; padding: 25px; border-radius: 10px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
            .summary h2, .reports-list h2, .quick-actions h2 { margin-top: 0; color: #333; }
            .report-types { display: flex; flex-wrap: wrap; gap: 15px; }
            .report-type { background-color: #f8f9fa; padding: 15px; border-radius: 8px; text-align: center; flex: 1; min-width: 120px; }
            .type-name { display: block; font-weight: 600; color: #333; }
            .type-count { display: block; font-size: 1.5em; color: #667eea; font-weight: bold; }
            table { width: 100%; border-collapse: collapse; margin-top: 15px; }
            th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
            th { background-color: #f8f9fa; font-weight: 600; }
            .report-type-badge { padding: 4px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 500; }
            .report-type-badge.custom-html { background-color: #e3f2fd; color: #1976d2; }
            .report-type-badge.extentreports { background-color: #e8f5e8; color: #2e7d32; }
            .report-type-badge.allure { background-color: #fff3e0; color: #ef6c00; }
            .report-type-badge.testng-emailable { background-color: #f3e5f5; color: #7b1fa2; }
            .report-type-badge.testng-index { background-color: #ffebee; color: #c62828; }
            .report-type-badge.json { background-color: #e0f2f1; color: #00695c; }
            .actions { display: flex; flex-wrap: wrap; gap: 15px; }
            .action-btn { display: inline-block; padding: 12px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 6px; font-weight: 500; transition: transform 0.2s; }
            .action-btn:hover { transform: translateY(-2px); }
            .footer { text-align: center; margin-top: 30px; color: #666; font-size: 0.9em; }
            a { color: #1976d2; text-decoration: none; }
            a:hover { text-decoration: underline; }
            code { background-color: #f5f5f5; padding: 2px 4px; border-radius: 3px; font-family: monospace; font-size: 0.9em; }
            """;
    }
    
    private static long getFileSize(File file) {
        return file.exists() ? file.length() : 0;
    }
    
    private static long getDirectorySize(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        try {
            return FileUtils.sizeOfDirectory(directory);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // İç sınıflar
    private static class ReportSummary {
        List<ReportInfo> reports = new ArrayList<>();
    }
    
    private static class ReportInfo {
        String name;
        String type;
        String path;
        long size;
        Date lastModified;
    }
}