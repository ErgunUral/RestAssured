package com.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * PayTR Performans Test Yardımcı Sınıfı
 * Performans testleri için ortak metodlar ve utilities
 */
public class PerformanceTestUtils {
    private WebDriver driver;
    private JavascriptExecutor js;
    private Map<String, Long> performanceMetrics;
    
    public PerformanceTestUtils(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.performanceMetrics = new HashMap<>();
    }
    
    /**
     * Sayfa yükleme süresini ölçer
     */
    public long measurePageLoadTime(String url) {
        long startTime = System.currentTimeMillis();
        
        try {
            driver.get(url);
            
            // DOM ready durumunu bekle
            waitForPageLoad();
            
            long endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            
            performanceMetrics.put("pageLoadTime_" + url, loadTime);
            return loadTime;
            
        } catch (Exception e) {
            System.out.println("Sayfa yükleme ölçümü hatası: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * DOM ready durumunu bekler
     */
    public void waitForPageLoad() {
        try {
            // Document ready state kontrolü
            js.executeScript("return document.readyState").equals("complete");
            
            // jQuery varsa bekle
            Boolean jqueryReady = (Boolean) js.executeScript(
                "return typeof jQuery !== 'undefined' ? jQuery.active == 0 : true;");
            
            if (!jqueryReady) {
                Thread.sleep(1000); // jQuery işlemlerini bekle
            }
            
        } catch (Exception e) {
            // Hata durumunda devam et
        }
    }
    
    /**
     * Performance API ile detaylı timing bilgilerini alır
     */
    public Map<String, Object> getPerformanceTimings() {
        try {
            String script = 
                "var perfData = performance.getEntriesByType('navigation')[0];" +
                "if (perfData) {" +
                "  return {" +
                "    'navigationStart': perfData.navigationStart," +
                "    'domainLookupStart': perfData.domainLookupStart," +
                "    'domainLookupEnd': perfData.domainLookupEnd," +
                "    'connectStart': perfData.connectStart," +
                "    'connectEnd': perfData.connectEnd," +
                "    'requestStart': perfData.requestStart," +
                "    'responseStart': perfData.responseStart," +
                "    'responseEnd': perfData.responseEnd," +
                "    'domContentLoadedEventStart': perfData.domContentLoadedEventStart," +
                "    'domContentLoadedEventEnd': perfData.domContentLoadedEventEnd," +
                "    'loadEventStart': perfData.loadEventStart," +
                "    'loadEventEnd': perfData.loadEventEnd," +
                "    'domComplete': perfData.domComplete" +
                "  };" +
                "} else {" +
                "  return null;" +
                "}";
            
            Object result = js.executeScript(script);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            
        } catch (Exception e) {
            System.out.println("Performance timing alınamadı: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    /**
     * Kaynak dosyalarının yükleme sürelerini analiz eder
     */
    public List<Map<String, Object>> getResourceTimings() {
        try {
            String script = 
                "var resources = performance.getEntriesByType('resource');" +
                "return resources.map(function(r) {" +
                "  return {" +
                "    'name': r.name," +
                "    'duration': r.duration," +
                "    'size': r.transferSize || 0," +
                "    'type': r.initiatorType," +
                "    'startTime': r.startTime," +
                "    'responseEnd': r.responseEnd" +
                "  };" +
                "});";
            
            Object result = js.executeScript(script);
            if (result instanceof List) {
                return (List<Map<String, Object>>) result;
            }
            
        } catch (Exception e) {
            System.out.println("Resource timing alınamadı: " + e.getMessage());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Bellek kullanımı bilgilerini alır (Chrome)
     */
    public Map<String, Object> getMemoryUsage() {
        try {
            String script = 
                "if (performance.memory) {" +
                "  return {" +
                "    'usedJSHeapSize': performance.memory.usedJSHeapSize," +
                "    'totalJSHeapSize': performance.memory.totalJSHeapSize," +
                "    'jsHeapSizeLimit': performance.memory.jsHeapSizeLimit," +
                "    'usedPercent': (performance.memory.usedJSHeapSize / performance.memory.jsHeapSizeLimit) * 100" +
                "  };" +
                "} else {" +
                "  return null;" +
                "}";
            
            Object result = js.executeScript(script);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            
        } catch (Exception e) {
            System.out.println("Bellek bilgisi alınamadı: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    /**
     * DOM element sayısını sayar
     */
    public int getDOMElementCount() {
        try {
            Object result = js.executeScript("return document.getElementsByTagName('*').length;");
            if (result != null) {
                return Integer.parseInt(result.toString());
            }
        } catch (Exception e) {
            System.out.println("DOM element sayısı alınamadı: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * CSS ve JavaScript dosya sayılarını sayar
     */
    public Map<String, Integer> getResourceCounts() {
        try {
            String script = 
                "var resources = performance.getEntriesByType('resource');" +
                "var counts = {" +
                "  'css': 0," +
                "  'javascript': 0," +
                "  'image': 0," +
                "  'font': 0," +
                "  'other': 0," +
                "  'total': resources.length" +
                "};" +
                "resources.forEach(function(r) {" +
                "  if (r.name.endsWith('.css')) counts.css++;" +
                "  else if (r.name.endsWith('.js')) counts.javascript++;" +
                "  else if (r.name.match(/\\.(jpg|jpeg|png|gif|svg|webp)$/i)) counts.image++;" +
                "  else if (r.name.match(/\\.(woff|woff2|ttf|eot)$/i)) counts.font++;" +
                "  else counts.other++;" +
                "});" +
                "return counts;";
            
            Object result = js.executeScript(script);
            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                Map<String, Integer> counts = new HashMap<>();
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    counts.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
                }
                return counts;
            }
            
        } catch (Exception e) {
            System.out.println("Kaynak sayıları alınamadı: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    /**
     * Sayfa boyutunu hesaplar
     */
    public Map<String, Object> getPageSize() {
        try {
            String script = 
                "var resources = performance.getEntriesByType('resource');" +
                "var totalSize = 0;" +
                "var sizes = {" +
                "  'html': 0," +
                "  'css': 0," +
                "  'javascript': 0," +
                "  'images': 0," +
                "  'fonts': 0," +
                "  'other': 0" +
                "};" +
                "resources.forEach(function(r) {" +
                "  var size = r.transferSize || 0;" +
                "  totalSize += size;" +
                "  if (r.name.endsWith('.html')) sizes.html += size;" +
                "  else if (r.name.endsWith('.css')) sizes.css += size;" +
                "  else if (r.name.endsWith('.js')) sizes.javascript += size;" +
                "  else if (r.name.match(/\\.(jpg|jpeg|png|gif|svg|webp)$/i)) sizes.images += size;" +
                "  else if (r.name.match(/\\.(woff|woff2|ttf|eot)$/i)) sizes.fonts += size;" +
                "  else sizes.other += size;" +
                "});" +
                "sizes.total = totalSize;" +
                "return sizes;";
            
            Object result = js.executeScript(script);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            
        } catch (Exception e) {
            System.out.println("Sayfa boyutu hesaplanamadı: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    /**
     * Performans skorunu hesaplar (0-100 arası)
     */
    public int calculatePerformanceScore() {
        try {
            Map<String, Object> timings = getPerformanceTimings();
            if (timings.isEmpty()) {
                return 50; // Varsayılan skor
            }
            
            // Basit performans skoru hesaplama
            int score = 100;
            
            // DOM Content Loaded süresi kontrolü
            if (timings.containsKey("domContentLoadedEventEnd") && timings.containsKey("navigationStart")) {
                long domTime = (Long) timings.get("domContentLoadedEventEnd") - (Long) timings.get("navigationStart");
                if (domTime > 3000) score -= 20; // 3 saniyeden fazla
                else if (domTime > 2000) score -= 10; // 2 saniyeden fazla
            }
            
            // Load event süresi kontrolü
            if (timings.containsKey("loadEventEnd") && timings.containsKey("navigationStart")) {
                long loadTime = (Long) timings.get("loadEventEnd") - (Long) timings.get("navigationStart");
                if (loadTime > 5000) score -= 30; // 5 saniyeden fazla
                else if (loadTime > 3000) score -= 15; // 3 saniyeden fazla
            }
            
            // DOM element sayısı kontrolü
            int domCount = getDOMElementCount();
            if (domCount > 3000) score -= 15;
            else if (domCount > 2000) score -= 10;
            
            // Kaynak dosya sayısı kontrolü
            Map<String, Integer> resourceCounts = getResourceCounts();
            int totalResources = resourceCounts.getOrDefault("total", 0);
            if (totalResources > 100) score -= 10;
            else if (totalResources > 50) score -= 5;
            
            return Math.max(0, Math.min(100, score));
            
        } catch (Exception e) {
            System.out.println("Performans skoru hesaplanamadı: " + e.getMessage());
            return 50;
        }
    }
    
    /**
     * Performans raporunu oluşturur
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n⚡ PERFORMANS RAPORU\n");
        report.append("========================================").append("\n");
        
        // Timing bilgileri
        Map<String, Object> timings = getPerformanceTimings();
        if (!timings.isEmpty()) {
            report.append("📊 Timing Bilgileri:\n");
            for (Map.Entry<String, Object> entry : timings.entrySet()) {
                report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        // Bellek kullanımı
        Map<String, Object> memory = getMemoryUsage();
        if (!memory.isEmpty()) {
            report.append("\n💾 Bellek Kullanımı:\n");
            for (Map.Entry<String, Object> entry : memory.entrySet()) {
                report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        // DOM bilgileri
        int domCount = getDOMElementCount();
        report.append("\n🏗️ DOM Element Sayısı: ").append(domCount).append("\n");
        
        // Kaynak sayıları
        Map<String, Integer> resourceCounts = getResourceCounts();
        if (!resourceCounts.isEmpty()) {
            report.append("\n📁 Kaynak Dosya Sayıları:\n");
            for (Map.Entry<String, Integer> entry : resourceCounts.entrySet()) {
                report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        // Performans skoru
        int score = calculatePerformanceScore();
        report.append("\n🎯 Performans Skoru: ").append(score).append("/100\n");
        
        // Öneriler
        report.append("\n💡 Öneriler:\n");
        if (score < 70) {
            report.append("  - Sayfa yükleme süresini optimize edin\n");
            report.append("  - Kaynak dosya sayısını azaltın\n");
            report.append("  - DOM element sayısını kontrol edin\n");
        } else if (score < 85) {
            report.append("  - Performans kabul edilebilir, küçük optimizasyonlar yapılabilir\n");
        } else {
            report.append("  - Performans mükemmel! 🎉\n");
        }
        
        report.append("\n⏰ Rapor Zamanı: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("========================================").append("\n");
        
        return report.toString();
    }
    
    /**
     * Performans metriklerini temizler
     */
    public void clearMetrics() {
        performanceMetrics.clear();
    }
    
    /**
     * Kaydedilen metrikleri döndürür
     */
    public Map<String, Long> getMetrics() {
        return new HashMap<>(performanceMetrics);
    }
}