package com.example.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import static io.restassured.RestAssured.*;

/**
 * PayTR API Test Yardımcı Sınıfı
 * API testleri için ortak metodlar ve utilities
 */
public class APITestUtils {
    private Map<String, String> defaultHeaders;
    private String authToken;
    
    public APITestUtils() {
        this.defaultHeaders = new HashMap<>();
        setupDefaultHeaders();
    }
    
    /**
     * Varsayılan header'ları ayarlar
     */
    private void setupDefaultHeaders() {
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("User-Agent", "PayTR-Test-Suite/1.0");
    }
    
    /**
     * GET request gönderir
     */
    public Response sendGetRequest(String endpoint) {
        return sendGetRequest(endpoint, new HashMap<>());
    }
    
    /**
     * Parametreli GET request gönderir
     */
    public Response sendGetRequest(String endpoint, Map<String, Object> params) {
        RequestSpecification request = given();
        
        // Default header'ları ekle
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            request.header(header.getKey(), header.getValue());
        }
        
        // Auth token varsa ekle
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        
        // Parametreleri ekle
        if (!params.isEmpty()) {
            request.params(params);
        }
        
        return request.when().get(endpoint);
    }
    
    /**
     * POST request gönderir
     */
    public Response sendPostRequest(String endpoint, Object body) {
        RequestSpecification request = given();
        
        // Default header'ları ekle
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            request.header(header.getKey(), header.getValue());
        }
        
        // Auth token varsa ekle
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        
        // Body ekle
        if (body != null) {
            if (body instanceof String) {
                request.body(body);
            } else if (body instanceof JSONObject) {
                request.body(body.toString());
            } else {
                request.body(body);
            }
        }
        
        return request.when().post(endpoint);
    }
    
    /**
     * PUT request gönderir
     */
    public Response sendPutRequest(String endpoint, Object body) {
        RequestSpecification request = given();
        
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            request.header(header.getKey(), header.getValue());
        }
        
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        
        if (body != null) {
            request.body(body.toString());
        }
        
        return request.when().put(endpoint);
    }
    
    /**
     * DELETE request gönderir
     */
    public Response sendDeleteRequest(String endpoint) {
        RequestSpecification request = given();
        
        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            request.header(header.getKey(), header.getValue());
        }
        
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        
        return request.when().delete(endpoint);
    }
    
    /**
     * Response'ın JSON olup olmadığını kontrol eder
     */
    public boolean isValidJSON(Response response) {
        try {
            String contentType = response.getHeader("Content-Type");
            if (contentType != null && contentType.contains("application/json")) {
                String body = response.getBody().asString();
                if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                    new JSONObject(body); // JSON parse testi
                    return true;
                }
            }
        } catch (Exception e) {
            try {
                // Array olabilir
                new JSONArray(response.getBody().asString());
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Response time'ı kontrol eder
     */
    public boolean isResponseTimeAcceptable(Response response, long maxTimeMs) {
        return response.getTime() <= maxTimeMs;
    }
    
    /**
     * Status code'ın başarılı olup olmadığını kontrol eder
     */
    public boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Client error status code kontrolü
     */
    public boolean isClientErrorStatusCode(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Server error status code kontrolü
     */
    public boolean isServerErrorStatusCode(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * Rate limiting kontrolü
     */
    public boolean isRateLimited(Response response) {
        int statusCode = response.getStatusCode();
        return statusCode == 429 || statusCode == 503;
    }
    
    /**
     * Rate limit header'larını alır
     */
    public Map<String, String> getRateLimitHeaders(Response response) {
        Map<String, String> rateLimitInfo = new HashMap<>();
        
        String limit = response.getHeader("X-RateLimit-Limit");
        String remaining = response.getHeader("X-RateLimit-Remaining");
        String reset = response.getHeader("X-RateLimit-Reset");
        String retryAfter = response.getHeader("Retry-After");
        
        if (limit != null) rateLimitInfo.put("limit", limit);
        if (remaining != null) rateLimitInfo.put("remaining", remaining);
        if (reset != null) rateLimitInfo.put("reset", reset);
        if (retryAfter != null) rateLimitInfo.put("retryAfter", retryAfter);
        
        return rateLimitInfo;
    }
    
    /**
     * Authentication token'ı ayarlar
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Authentication token'ı temizler
     */
    public void clearAuthToken() {
        this.authToken = null;
    }
    
    /**
     * Login işlemi yapar ve token alır
     */
    public String performLogin(String loginEndpoint, String email, String password) {
        try {
            JSONObject loginData = new JSONObject();
            loginData.put("email", email);
            loginData.put("password", password);
            
            Response response = sendPostRequest(loginEndpoint, loginData);
            
            if (isSuccessStatusCode(response.getStatusCode())) {
                String responseBody = response.getBody().asString();
                
                // Token'ı response'dan çıkar
                if (isValidJSON(response)) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    // Farklı token field'larını dene
                    String[] tokenFields = {"token", "access_token", "auth_token", "jwt"};
                    for (String field : tokenFields) {
                        if (jsonResponse.has(field)) {
                            String token = jsonResponse.getString(field);
                            setAuthToken(token);
                            return token;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Login hatası: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Endpoint'in mevcut olup olmadığını kontrol eder
     */
    public boolean isEndpointAvailable(String endpoint) {
        try {
            Response response = sendGetRequest(endpoint);
            return response.getStatusCode() != 404;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * API health check yapar
     */
    public Map<String, Object> performHealthCheck(String baseUrl) {
        Map<String, Object> healthStatus = new HashMap<>();
        
        try {
            // Temel endpoint'leri kontrol et
            String[] healthEndpoints = {
                "/health",
                "/status",
                "/ping",
                "/api/health",
                "/api/status"
            };
            
            boolean healthEndpointFound = false;
            for (String endpoint : healthEndpoints) {
                Response response = sendGetRequest(endpoint);
                if (response.getStatusCode() != 404) {
                    healthStatus.put("healthEndpoint", endpoint);
                    healthStatus.put("statusCode", response.getStatusCode());
                    healthStatus.put("responseTime", response.getTime());
                    healthStatus.put("available", true);
                    healthEndpointFound = true;
                    break;
                }
            }
            
            if (!healthEndpointFound) {
                // Ana sayfa kontrolü
                Response response = sendGetRequest("/");
                healthStatus.put("statusCode", response.getStatusCode());
                healthStatus.put("responseTime", response.getTime());
                healthStatus.put("available", response.getStatusCode() < 500);
            }
            
        } catch (Exception e) {
            healthStatus.put("available", false);
            healthStatus.put("error", e.getMessage());
        }
        
        return healthStatus;
    }
    
    /**
     * API performance metriklerini toplar
     */
    public Map<String, Object> collectPerformanceMetrics(String endpoint, int requestCount) {
        Map<String, Object> metrics = new HashMap<>();
        List<Long> responseTimes = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        try {
            for (int i = 0; i < requestCount; i++) {
                long startTime = System.currentTimeMillis();
                Response response = sendGetRequest(endpoint);
                long endTime = System.currentTimeMillis();
                
                long responseTime = endTime - startTime;
                responseTimes.add(responseTime);
                
                if (isSuccessStatusCode(response.getStatusCode())) {
                    successCount++;
                } else {
                    errorCount++;
                }
                
                // Kısa bekleme
                Thread.sleep(100);
            }
            
            // Metrikleri hesapla
            long totalTime = responseTimes.stream().mapToLong(Long::longValue).sum();
            long avgResponseTime = totalTime / requestCount;
            long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            
            metrics.put("requestCount", requestCount);
            metrics.put("successCount", successCount);
            metrics.put("errorCount", errorCount);
            metrics.put("successRate", (double) successCount / requestCount * 100);
            metrics.put("avgResponseTime", avgResponseTime);
            metrics.put("minResponseTime", minResponseTime);
            metrics.put("maxResponseTime", maxResponseTime);
            metrics.put("totalTime", totalTime);
            
        } catch (Exception e) {
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * API test raporu oluşturur
     */
    public String generateAPITestReport(String testName, Response response, boolean passed) {
        StringBuilder report = new StringBuilder();
        report.append("\n🔌 API TEST RAPORU\n");
        report.append("========================================").append("\n");
        report.append("Test Adı: ").append(testName).append("\n");
        report.append("Sonuç: ").append(passed ? "✅ BAŞARILI" : "❌ BAŞARISIZ").append("\n");
        
        if (response != null) {
            report.append("Status Code: ").append(response.getStatusCode()).append("\n");
            report.append("Response Time: ").append(response.getTime()).append(" ms\n");
            
            String contentType = response.getHeader("Content-Type");
            if (contentType != null) {
                report.append("Content-Type: ").append(contentType).append("\n");
            }
            
            // Rate limit bilgileri
            Map<String, String> rateLimitInfo = getRateLimitHeaders(response);
            if (!rateLimitInfo.isEmpty()) {
                report.append("Rate Limit Info: ").append(rateLimitInfo).append("\n");
            }
            
            // Response body (kısaltılmış)
            String body = response.getBody().asString();
            if (body.length() > 200) {
                body = body.substring(0, 200) + "...";
            }
            report.append("Response Body: ").append(body).append("\n");
        }
        
        report.append("Zaman: ").append(java.time.LocalDateTime.now()).append("\n");
        report.append("========================================").append("\n");
        
        return report.toString();
    }
    
    /**
     * Custom header ekler
     */
    public void addHeader(String key, String value) {
        defaultHeaders.put(key, value);
    }
    
    /**
     * Header'ı kaldırır
     */
    public void removeHeader(String key) {
        defaultHeaders.remove(key);
    }
    
    /**
     * Tüm header'ları temizler
     */
    public void clearHeaders() {
        defaultHeaders.clear();
        setupDefaultHeaders();
    }
}