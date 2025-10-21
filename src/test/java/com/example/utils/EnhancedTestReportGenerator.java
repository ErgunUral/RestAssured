package com.example.utils;

import org.testng.ITestResult;
import org.testng.Reporter;
import io.qameta.allure.Allure;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced Test Report Generator for PayTR Test Suite
 * Generates comprehensive HTML and JSON reports with detailed analytics
 */
public class EnhancedTestReportGenerator {
    
    private static final String REPORTS_DIR = "target/enhanced-reports";
    private static final Map<String, TestReportData> testResults = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> categoryResults = new ConcurrentHashMap<>();
    private static final Map<String, Long> categoryDurations = new ConcurrentHashMap<>();
    
    /**
     * Test Report Data Container
     */
    public static class TestReportData {
        private String testName;
        private String className;
        private String methodName;
        private String category;
        private String status;
        private long duration;
        private String errorMessage;
        private String stackTrace;
        private List<String> steps;
        private Map<String, String> parameters;
        private String threadName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String screenshotPath;
        private Map<String, Object> customData;
        
        public TestReportData() {
            this.steps = new ArrayList<>();
            this.parameters = new HashMap<>();
            this.customData = new HashMap<>();
        }
        
        // Getters and setters
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getStackTrace() { return stackTrace; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
        
        public List<String> getSteps() { return steps; }
        public void addStep(String step) { this.steps.add(step); }
        
        public Map<String, String> getParameters() { return parameters; }
        public void addParameter(String key, String value) { this.parameters.put(key, value); }
        
        public String getThreadName() { return threadName; }
        public void setThreadName(String threadName) { this.threadName = threadName; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getScreenshotPath() { return screenshotPath; }
        public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
        
        public Map<String, Object> getCustomData() { return customData; }
        public void addCustomData(String key, Object value) { this.customData.put(key, value); }
        
        public String getFormattedDuration() {
            if (duration < 1000) {
                return duration + "ms";
            } else {
                return String.format("%.2fs", duration / 1000.0);
            }
        }
    }
    
    /**
     * Add test result to report data
     */
    public static void addTestResult(ITestResult result) {
        String testKey = getTestKey(result);
        TestReportData reportData = new TestReportData();
        
        reportData.setTestName(testKey);
        reportData.setClassName(result.getTestClass().getName());
        reportData.setMethodName(result.getMethod().getMethodName());
        reportData.setThreadName(Thread.currentThread().getName());
        reportData.setStartTime(LocalDateTime.now().minusNanos(result.getEndMillis() - result.getStartMillis()));
        reportData.setEndTime(LocalDateTime.now());
        reportData.setDuration(result.getEndMillis() - result.getStartMillis());
        
        // Determine category from test groups or class name
        String category = determineTestCategory(result);
        reportData.setCategory(category);
        
        // Set status
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                reportData.setStatus("PASSED");
                break;
            case ITestResult.FAILURE:
                reportData.setStatus("FAILED");
                if (result.getThrowable() != null) {
                    reportData.setErrorMessage(result.getThrowable().getMessage());
                    reportData.setStackTrace(getStackTrace(result.getThrowable()));
                }
                break;
            case ITestResult.SKIP:
                reportData.setStatus("SKIPPED");
                break;
        }
        
        // Add parameters if available
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                reportData.addParameter("param" + i, String.valueOf(parameters[i]));
            }
        }
        
        // Add screenshot path if available
        String screenshotPath = ThreadSafeScreenshotUtils.getThreadScreenshotPath();
        if (screenshotPath != null) {
            reportData.setScreenshotPath(screenshotPath);
        }
        
        testResults.put(testKey, reportData);
        
        // Update category statistics
        categoryResults.computeIfAbsent(category, k -> new ArrayList<>()).add(reportData.getStatus());
        categoryDurations.merge(category, reportData.getDuration(), Long::sum);
        
        Reporter.log(String.format("📊 Test result added to report: %s - %s", testKey, reportData.getStatus()));
    }
    
    /**
     * Generate comprehensive HTML report
     */
    public static void generateHTMLReport() {
        try {
            createReportsDirectory();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("paytr_enhanced_report_%s.html", timestamp);
            String filePath = REPORTS_DIR + "/" + fileName;
            
            StringBuilder html = new StringBuilder();
            html.append(generateHTMLHeader());
            html.append(generateExecutiveSummary());
            html.append(generateCategoryAnalysis());
            html.append(generateDetailedResults());
            html.append(generatePerformanceAnalysis());
            html.append(generateHTMLFooter());
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(html.toString());
            }
            
            // Add to Allure report
            Allure.addAttachment("Enhanced HTML Report", "text/html", 
                html.toString(), ".html");
            
            Reporter.log("📄 Enhanced HTML report generated: " + filePath);
            
        } catch (IOException e) {
            Reporter.log("❌ Failed to generate HTML report: " + e.getMessage());
        }
    }
    
    /**
     * Generate JSON report for API consumption
     */
    public static void generateJSONReport() {
        try {
            createReportsDirectory();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("paytr_enhanced_report_%s.json", timestamp);
            String filePath = REPORTS_DIR + "/" + fileName;
            
            Map<String, Object> jsonReport = new HashMap<>();
            jsonReport.put("reportMetadata", generateReportMetadata());
            jsonReport.put("executiveSummary", generateExecutiveSummaryData());
            jsonReport.put("categoryAnalysis", generateCategoryAnalysisData());
            jsonReport.put("testResults", testResults);
            jsonReport.put("performanceMetrics", generatePerformanceMetricsData());
            
            String jsonContent = convertToJSON(jsonReport);
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(jsonContent);
            }
            
            // Add to Allure report
            Allure.addAttachment("Enhanced JSON Report", "application/json", 
                jsonContent, ".json");
            
            Reporter.log("📄 Enhanced JSON report generated: " + filePath);
            
        } catch (IOException e) {
            Reporter.log("❌ Failed to generate JSON report: " + e.getMessage());
        }
    }
    
    /**
     * Generate HTML header
     */
    private static String generateHTMLHeader() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>PayTR Enhanced Test Report</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 8px 8px 0 0; }
                    .header h1 { margin: 0; font-size: 2.5em; }
                    .header .subtitle { margin: 10px 0 0 0; opacity: 0.9; }
                    .section { padding: 30px; border-bottom: 1px solid #eee; }
                    .section:last-child { border-bottom: none; }
                    .section h2 { color: #333; border-bottom: 2px solid #667eea; padding-bottom: 10px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
                    .stat-card { background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; border-left: 4px solid #667eea; }
                    .stat-number { font-size: 2em; font-weight: bold; color: #667eea; }
                    .stat-label { color: #666; margin-top: 5px; }
                    .test-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                    .test-table th, .test-table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                    .test-table th { background-color: #f8f9fa; font-weight: 600; }
                    .status-passed { color: #28a745; font-weight: bold; }
                    .status-failed { color: #dc3545; font-weight: bold; }
                    .status-skipped { color: #ffc107; font-weight: bold; }
                    .category-section { margin: 20px 0; padding: 20px; background: #f8f9fa; border-radius: 8px; }
                    .progress-bar { width: 100%; height: 20px; background: #e9ecef; border-radius: 10px; overflow: hidden; }
                    .progress-fill { height: 100%; background: linear-gradient(90deg, #28a745, #20c997); transition: width 0.3s ease; }
                    .chart-container { margin: 20px 0; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚀 PayTR Enhanced Test Report</h1>
                        <div class="subtitle">Comprehensive Test Execution Analysis - """ + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss")) + 
                        """
                        </div>
                    </div>
            """;
    }
    
    /**
     * Generate executive summary section
     */
    private static String generateExecutiveSummary() {
        Map<String, Object> summary = generateExecutiveSummaryData();
        
        return String.format("""
            <div class="section">
                <h2>📊 Executive Summary</h2>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Total Tests</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" style="color: #28a745;">%d</div>
                        <div class="stat-label">Passed</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" style="color: #dc3545;">%d</div>
                        <div class="stat-label">Failed</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" style="color: #ffc107;">%d</div>
                        <div class="stat-label">Skipped</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%.1f%%</div>
                        <div class="stat-label">Success Rate</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%.2fs</div>
                        <div class="stat-label">Total Duration</div>
                    </div>
                </div>
                <div style="margin-top: 20px;">
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: %.1f%%;"></div>
                    </div>
                    <p style="text-align: center; margin-top: 10px; color: #666;">Overall Test Success Rate</p>
                </div>
            </div>
            """, 
            summary.get("totalTests"), summary.get("passedTests"), summary.get("failedTests"), 
            summary.get("skippedTests"), summary.get("successRate"), 
            (Double) summary.get("totalDuration") / 1000.0, summary.get("successRate"));
    }
    
    /**
     * Generate category analysis section
     */
    private static String generateCategoryAnalysis() {
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <h2>📋 Test Category Analysis</h2>
            """);
        
        for (Map.Entry<String, List<String>> entry : categoryResults.entrySet()) {
            String category = entry.getKey();
            List<String> results = entry.getValue();
            
            long passed = results.stream().mapToLong(r -> "PASSED".equals(r) ? 1 : 0).sum();
            long failed = results.stream().mapToLong(r -> "FAILED".equals(r) ? 1 : 0).sum();
            long skipped = results.stream().mapToLong(r -> "SKIPPED".equals(r) ? 1 : 0).sum();
            long total = results.size();
            double successRate = total > 0 ? (double) passed / total * 100 : 0;
            long duration = categoryDurations.getOrDefault(category, 0L);
            
            html.append(String.format("""
                <div class="category-section">
                    <h3>%s</h3>
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-number">%d</div>
                            <div class="stat-label">Total</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number" style="color: #28a745;">%d</div>
                            <div class="stat-label">Passed</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number" style="color: #dc3545;">%d</div>
                            <div class="stat-label">Failed</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%.1f%%</div>
                            <div class="stat-label">Success Rate</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">%.2fs</div>
                            <div class="stat-label">Duration</div>
                        </div>
                    </div>
                </div>
                """, category, total, passed, failed, successRate, duration / 1000.0));
        }
        
        html.append("</div>");
        return html.toString();
    }
    
    /**
     * Generate detailed results section
     */
    private static String generateDetailedResults() {
        StringBuilder html = new StringBuilder();
        html.append("""
            <div class="section">
                <h2>📝 Detailed Test Results</h2>
                <table class="test-table">
                    <thead>
                        <tr>
                            <th>Test Name</th>
                            <th>Category</th>
                            <th>Status</th>
                            <th>Duration</th>
                            <th>Thread</th>
                            <th>Error Message</th>
                        </tr>
                    </thead>
                    <tbody>
            """);
        
        testResults.values().stream()
            .sorted((a, b) -> a.getCategory().compareTo(b.getCategory()))
            .forEach(test -> {
                String statusClass = "status-" + test.getStatus().toLowerCase();
                String errorMessage = test.getErrorMessage() != null ? 
                    test.getErrorMessage().substring(0, Math.min(test.getErrorMessage().length(), 100)) + "..." : "";
                
                html.append(String.format("""
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td class="%s">%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """, test.getTestName(), test.getCategory(), statusClass, 
                    test.getStatus(), test.getFormattedDuration(), 
                    test.getThreadName(), errorMessage));
            });
        
        html.append("""
                    </tbody>
                </table>
            </div>
            """);
        
        return html.toString();
    }
    
    /**
     * Generate performance analysis section
     */
    private static String generatePerformanceAnalysis() {
        Map<String, Object> metrics = generatePerformanceMetricsData();
        
        return String.format("""
            <div class="section">
                <h2>⚡ Performance Analysis</h2>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-number">%.2fs</div>
                        <div class="stat-label">Average Duration</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%.2fs</div>
                        <div class="stat-label">Fastest Test</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%.2fs</div>
                        <div class="stat-label">Slowest Test</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number">%d</div>
                        <div class="stat-label">Parallel Threads</div>
                    </div>
                </div>
            </div>
            """, 
            (Double) metrics.get("averageDuration") / 1000.0,
            (Double) metrics.get("fastestTest") / 1000.0,
            (Double) metrics.get("slowestTest") / 1000.0,
            metrics.get("parallelThreads"));
    }
    
    /**
     * Generate HTML footer
     */
    private static String generateHTMLFooter() {
        return """
                </div>
                <script>
                    // Add any interactive JavaScript here
                    console.log('PayTR Enhanced Test Report loaded');
                </script>
            </body>
            </html>
            """;
    }
    
    /**
     * Helper methods for data generation
     */
    private static String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    private static String determineTestCategory(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            return groups[0];
        }
        
        String className = result.getTestClass().getName();
        if (className.contains("Payment")) return "Core Payment";
        if (className.contains("Security")) return "Security";
        if (className.contains("Performance")) return "Performance";
        if (className.contains("Currency")) return "Multi-Currency";
        if (className.contains("3DSecure")) return "3D Secure";
        if (className.contains("Fraud")) return "Fraud Detection";
        if (className.contains("Webhook")) return "Webhook";
        if (className.contains("Accessibility")) return "Accessibility";
        if (className.contains("Edge")) return "Edge Case";
        if (className.contains("Chaos")) return "Chaos Engineering";
        if (className.contains("Business")) return "Business Logic";
        if (className.contains("Migration")) return "Data Migration";
        
        return "Other";
    }
    
    private static String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    private static void createReportsDirectory() throws IOException {
        Files.createDirectories(Paths.get(REPORTS_DIR));
    }
    
    private static Map<String, Object> generateReportMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("reportVersion", "1.0");
        metadata.put("testSuite", "PayTR Enhanced Test Suite");
        metadata.put("environment", System.getProperty("environment", "test"));
        metadata.put("browser", System.getProperty("browser", "chrome"));
        return metadata;
    }
    
    private static Map<String, Object> generateExecutiveSummaryData() {
        long totalTests = testResults.size();
        long passedTests = testResults.values().stream().mapToLong(t -> "PASSED".equals(t.getStatus()) ? 1 : 0).sum();
        long failedTests = testResults.values().stream().mapToLong(t -> "FAILED".equals(t.getStatus()) ? 1 : 0).sum();
        long skippedTests = testResults.values().stream().mapToLong(t -> "SKIPPED".equals(t.getStatus()) ? 1 : 0).sum();
        double successRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        double totalDuration = testResults.values().stream().mapToLong(TestReportData::getDuration).sum();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTests", totalTests);
        summary.put("passedTests", passedTests);
        summary.put("failedTests", failedTests);
        summary.put("skippedTests", skippedTests);
        summary.put("successRate", successRate);
        summary.put("totalDuration", totalDuration);
        
        return summary;
    }
    
    private static Map<String, Object> generateCategoryAnalysisData() {
        Map<String, Object> analysis = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : categoryResults.entrySet()) {
            String category = entry.getKey();
            List<String> results = entry.getValue();
            
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("total", results.size());
            categoryData.put("passed", results.stream().mapToLong(r -> "PASSED".equals(r) ? 1 : 0).sum());
            categoryData.put("failed", results.stream().mapToLong(r -> "FAILED".equals(r) ? 1 : 0).sum());
            categoryData.put("skipped", results.stream().mapToLong(r -> "SKIPPED".equals(r) ? 1 : 0).sum());
            categoryData.put("duration", categoryDurations.getOrDefault(category, 0L));
            
            analysis.put(category, categoryData);
        }
        
        return analysis;
    }
    
    private static Map<String, Object> generatePerformanceMetricsData() {
        Map<String, Object> metrics = new HashMap<>();
        
        OptionalDouble avgDuration = testResults.values().stream()
            .mapToLong(TestReportData::getDuration)
            .average();
        
        OptionalLong fastestTest = testResults.values().stream()
            .mapToLong(TestReportData::getDuration)
            .min();
        
        OptionalLong slowestTest = testResults.values().stream()
            .mapToLong(TestReportData::getDuration)
            .max();
        
        Set<String> uniqueThreads = testResults.values().stream()
            .map(TestReportData::getThreadName)
            .collect(Collectors.toSet());
        
        metrics.put("averageDuration", avgDuration.orElse(0));
        metrics.put("fastestTest", fastestTest.orElse(0));
        metrics.put("slowestTest", slowestTest.orElse(0));
        metrics.put("parallelThreads", uniqueThreads.size());
        
        return metrics;
    }
    
    private static String convertToJSON(Map<String, Object> data) {
        // Simple JSON conversion - in production, use a proper JSON library
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        data.entrySet().forEach(entry -> {
            json.append("  \"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(",\n");
        });
        
        if (json.length() > 2) {
            json.setLength(json.length() - 2); // Remove last comma
        }
        json.append("\n}");
        
        return json.toString();
    }
    
    /**
     * Clear all report data
     */
    public static void clearReportData() {
        testResults.clear();
        categoryResults.clear();
        categoryDurations.clear();
    }
    
    /**
     * Get current report statistics
     */
    public static Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTestsRecorded", testResults.size());
        stats.put("categoriesTracked", categoryResults.size());
        stats.put("reportsDirectory", REPORTS_DIR);
        return stats;
    }
}