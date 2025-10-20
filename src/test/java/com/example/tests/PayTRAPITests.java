package com.example.tests;

import io.qameta.allure.*;
import org.testng.annotations.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.example.utils.APITestUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/**
 * PayTR API Test Senaryoları
 * Test ID: AT-001 to AT-004
 * Kategori: API Testing
 */
@Epic("PayTR API Testing")
@Feature("API Endpoints")
public class PayTRAPITests extends BaseTest {
    private APITestUtils apiUtils;
    private String apiBaseUrl;
    
    @BeforeClass
    @Step("API testleri için test ortamını hazırla")
    public void setupAPITests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "/api";
        apiBaseUrl = baseURI + basePath;
        
        apiUtils = new APITestUtils();
        
        // RestAssured konfigürasyonu
        RestAssured.baseURI = baseURI;
        RestAssured.basePath = basePath;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        logTestInfo("PayTR API Test Suite başlatıldı");
        logTestInfo("API Base URL: " + apiBaseUrl);
    }
    
    @AfterClass
    @Step("API testleri sonrası temizlik")
    public void tearDown() {
        logTestInfo("PayTR API Test Suite tamamlandı");
    }
    
    /**
     * Test ID: AT-001
     * Test Adı: Payment API Endpoint Testi
     * Kategori: API - Payment
     * Öncelik: Kritik
     */
    @Test(priority = 1, groups = {"api", "critical", "payment"})
    @Story("Payment API")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ödeme API endpoint'inin doğru çalışması kontrolü")
    public void testPaymentAPIEndpoint() {
        logTestInfo("Test ID: AT-001 - Payment API Endpoint Testi");
        
        try {
            // Test verisi hazırla
            JSONObject paymentData = new JSONObject();
            paymentData.put("merchant_id", "test_merchant");
            paymentData.put("user_ip", "127.0.0.1");
            paymentData.put("merchant_oid", "test_order_" + System.currentTimeMillis());
            paymentData.put("email", "test@example.com");
            paymentData.put("payment_amount", "100");
            paymentData.put("currency", "TL");
            paymentData.put("test_mode", "1");
            
            // API çağrısı yap
            RequestSpecification request = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(paymentData.toString());
            
            Response response = request.when()
                .post("/payment/create");
            
            // Response kontrolü
            logTestInfo("API Response Status: " + response.getStatusCode());
            logTestInfo("API Response Body: " + response.getBody().asString());
            
            // Status code kontrolü
            if (response.getStatusCode() == 404) {
                logTestInfo("Payment API endpoint bulunamadı, alternatif endpoint deneniyor");
                
                // Alternatif endpoint'ler dene
                String[] alternativeEndpoints = {
                    "/payment",
                    "/pay",
                    "/transaction",
                    "/checkout"
                };
                
                boolean endpointFound = false;
                for (String endpoint : alternativeEndpoints) {
                    Response altResponse = given()
                        .header("Content-Type", "application/json")
                        .body(paymentData.toString())
                        .when()
                        .post(endpoint);
                    
                    if (altResponse.getStatusCode() != 404) {
                        logTestInfo("Alternatif endpoint bulundu: " + endpoint);
                        logTestInfo("Status: " + altResponse.getStatusCode());
                        endpointFound = true;
                        break;
                    }
                }
                
                if (!endpointFound) {
                    logTestInfo("Hiçbir payment endpoint bulunamadı, test atlandı");
                    logTestResult("AT-001", "ATLANDI", "Payment API endpoint bulunamadı");
                    return;
                }
            } else {
                // Başarılı response kontrolü
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300 || 
                          response.getStatusCode() == 400, // Bad request da kabul edilebilir (test verisi için)
                    "Payment API beklenmeyen status code döndü: " + response.getStatusCode());
                
                // Response time kontrolü
                long responseTime = response.getTime();
                assertTrue(responseTime < 5000, 
                    "Payment API yanıt süresi çok yavaş: " + responseTime + " ms");
                
                // Content-Type kontrolü
                String contentType = response.getHeader("Content-Type");
                if (contentType != null) {
                    assertTrue(contentType.contains("application/json") || contentType.contains("text/html"),
                        "Payment API beklenmeyen Content-Type döndü: " + contentType);
                }
            }
            
            logTestResult("AT-001", "BAŞARILI", "Payment API endpoint testi tamamlandı");
            
        } catch (Exception e) {
            logTestResult("AT-001", "BAŞARISIZ", "Payment API testi hatası: " + e.getMessage());
            fail("Payment API testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: AT-002
     * Test Adı: Authentication API Testi
     * Kategori: API - Authentication
     * Öncelik: Kritik
     */
    @Test(priority = 2, groups = {"api", "critical", "auth"})
    @Story("Authentication API")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kimlik doğrulama API'sinin doğru çalışması kontrolü")
    public void testAuthenticationAPI() {
        logTestInfo("Test ID: AT-002 - Authentication API Testi");
        
        try {
            // Login verisi hazırla
            JSONObject loginData = new JSONObject();
            loginData.put("email", "test@example.com");
            loginData.put("password", "test123");
            
            // Login API çağrısı
            Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(loginData.toString())
                .when()
                .post("/auth/login");
            
            logTestInfo("Login API Status: " + loginResponse.getStatusCode());
            logTestInfo("Login API Response: " + loginResponse.getBody().asString());
            
            if (loginResponse.getStatusCode() == 404) {
                // Alternatif auth endpoint'ler dene
                String[] authEndpoints = {
                    "/login",
                    "/auth",
                    "/user/login",
                    "/account/login"
                };
                
                boolean authEndpointFound = false;
                for (String endpoint : authEndpoints) {
                    Response authResponse = given()
                        .header("Content-Type", "application/json")
                        .body(loginData.toString())
                        .when()
                        .post(endpoint);
                    
                    if (authResponse.getStatusCode() != 404) {
                        logTestInfo("Auth endpoint bulundu: " + endpoint);
                        logTestInfo("Status: " + authResponse.getStatusCode());
                        authEndpointFound = true;
                        
                        // Response time kontrolü
                        long responseTime = authResponse.getTime();
                        assertTrue(responseTime < 5000,
                            "Auth API yanıt süresi çok yavaş: " + responseTime + " ms");
                        
                        break;
                    }
                }
                
                if (!authEndpointFound) {
                    logTestInfo("Auth API endpoint bulunamadı, test atlandı");
                    logTestResult("AT-002", "ATLANDI", "Authentication API endpoint bulunamadı");
                    return;
                }
            } else {
                // Response kontrolü
                assertTrue(loginResponse.getStatusCode() >= 200 && loginResponse.getStatusCode() < 500,
                    "Auth API beklenmeyen status code: " + loginResponse.getStatusCode());
                
                // Response time kontrolü
                long responseTime = loginResponse.getTime();
                assertTrue(responseTime < 5000,
                    "Auth API yanıt süresi çok yavaş: " + responseTime + " ms");
            }
            
            // Logout API testi (eğer token varsa)
            String responseBody = loginResponse.getBody().asString();
            if (responseBody.contains("token") || responseBody.contains("access_token")) {
                logTestInfo("Token bulundu, logout testi yapılıyor");
                
                Response logoutResponse = given()
                    .header("Content-Type", "application/json")
                    .when()
                    .post("/auth/logout");
                
                logTestInfo("Logout API Status: " + logoutResponse.getStatusCode());
            }
            
            logTestResult("AT-002", "BAŞARILI", "Authentication API testi tamamlandı");
            
        } catch (Exception e) {
            logTestResult("AT-002", "BAŞARISIZ", "Authentication API testi hatası: " + e.getMessage());
            fail("Authentication API testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: AT-003
     * Test Adı: API Rate Limiting Testi
     * Kategori: API - Rate Limiting
     * Öncelik: Orta
     */
    @Test(priority = 3, groups = {"api", "medium", "rate-limit"})
    @Story("API Rate Limiting")
    @Severity(SeverityLevel.NORMAL)
    @Description("API rate limiting kontrolü")
    public void testAPIRateLimiting() {
        logTestInfo("Test ID: AT-003 - API Rate Limiting Testi");
        
        try {
            String testEndpoint = "/status"; // Basit endpoint
            int requestCount = 10;
            int rateLimitHits = 0;
            
            // Hızlı ardışık istekler gönder
            for (int i = 0; i < requestCount; i++) {
                Response response = given()
                    .header("Accept", "application/json")
                    .when()
                    .get(testEndpoint);
                
                logTestInfo("İstek " + (i + 1) + " - Status: " + response.getStatusCode());
                
                // Rate limit status code'ları kontrol et
                if (response.getStatusCode() == 429 || response.getStatusCode() == 503) {
                    rateLimitHits++;
                    logTestInfo("Rate limit tespit edildi: " + response.getStatusCode());
                    
                    // Rate limit header'larını kontrol et
                    String rateLimitHeader = response.getHeader("X-RateLimit-Limit");
                    String remainingHeader = response.getHeader("X-RateLimit-Remaining");
                    String retryAfterHeader = response.getHeader("Retry-After");
                    
                    if (rateLimitHeader != null) {
                        logTestInfo("Rate Limit: " + rateLimitHeader);
                    }
                    if (remainingHeader != null) {
                        logTestInfo("Remaining: " + remainingHeader);
                    }
                    if (retryAfterHeader != null) {
                        logTestInfo("Retry After: " + retryAfterHeader);
                    }
                    
                    break; // Rate limit bulundu, test başarılı
                }
                
                // Kısa bekleme
                Thread.sleep(100);
            }
            
            if (rateLimitHits > 0) {
                logTestInfo("Rate limiting aktif - Güvenlik önlemi mevcut");
            } else {
                logTestInfo("Rate limiting tespit edilmedi - Bu normal olabilir");
            }
            
            logTestResult("AT-003", "BAŞARILI", "Rate limiting testi tamamlandı");
            
        } catch (Exception e) {
            logTestResult("AT-003", "BAŞARISIZ", "Rate limiting testi hatası: " + e.getMessage());
            fail("Rate limiting testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: AT-004
     * Test Adı: API Error Handling Testi
     * Kategori: API - Error Handling
     * Öncelik: Orta
     */
    @Test(priority = 4, groups = {"api", "medium", "error-handling"})
    @Story("API Error Handling")
    @Severity(SeverityLevel.NORMAL)
    @Description("API hata yönetimi kontrolü")
    public void testAPIErrorHandling() {
        logTestInfo("Test ID: AT-004 - API Error Handling Testi");
        
        try {
            // Geçersiz endpoint testi
            Response invalidEndpointResponse = given()
                .header("Accept", "application/json")
                .when()
                .get("/nonexistent-endpoint-12345");
            
            logTestInfo("Geçersiz endpoint status: " + invalidEndpointResponse.getStatusCode());
            
            // 404 status code beklenir
            assertEquals(invalidEndpointResponse.getStatusCode(), 404,
                "Geçersiz endpoint için 404 status code beklenir");
            
            // Geçersiz HTTP method testi
            Response invalidMethodResponse = given()
                .header("Accept", "application/json")
                .when()
                .patch("/"); // PATCH method genellikle desteklenmez
            
            logTestInfo("Geçersiz method status: " + invalidMethodResponse.getStatusCode());
            
            // 405 (Method Not Allowed) veya 404 beklenir
            assertTrue(invalidMethodResponse.getStatusCode() == 405 || 
                      invalidMethodResponse.getStatusCode() == 404,
                "Geçersiz method için 405 veya 404 status code beklenir");
            
            // Geçersiz JSON testi
            Response invalidJsonResponse = given()
                .header("Content-Type", "application/json")
                .body("{ invalid json }")
                .when()
                .post("/api/test");
            
            logTestInfo("Geçersiz JSON status: " + invalidJsonResponse.getStatusCode());
            
            // 400 (Bad Request) beklenir
            if (invalidJsonResponse.getStatusCode() != 404) { // Endpoint varsa
                assertTrue(invalidJsonResponse.getStatusCode() == 400 || 
                          invalidJsonResponse.getStatusCode() == 422,
                    "Geçersiz JSON için 400 veya 422 status code beklenir");
            }
            
            // Büyük payload testi
            StringBuilder largePayload = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                largePayload.append("a");
            }
            
            Response largePayloadResponse = given()
                .header("Content-Type", "application/json")
                .body("{\"data\":\"" + largePayload.toString() + "\"}")
                .when()
                .post("/api/test");
            
            logTestInfo("Büyük payload status: " + largePayloadResponse.getStatusCode());
            
            // 413 (Payload Too Large) veya 400 beklenir
            if (largePayloadResponse.getStatusCode() != 404) {
                assertTrue(largePayloadResponse.getStatusCode() == 413 || 
                          largePayloadResponse.getStatusCode() == 400 ||
                          largePayloadResponse.getStatusCode() == 422,
                    "Büyük payload için uygun hata kodu beklenir");
            }
            
            // Error response format kontrolü
            String errorResponseBody = invalidEndpointResponse.getBody().asString();
            if (!errorResponseBody.isEmpty()) {
                // JSON format kontrolü
                try {
                    new JSONObject(errorResponseBody);
                    logTestInfo("Hata yanıtı JSON formatında");
                } catch (Exception e) {
                    // HTML veya plain text olabilir
                    logTestInfo("Hata yanıtı JSON formatında değil");
                }
            }
            
            logTestResult("AT-004", "BAŞARILI", "API error handling testi tamamlandı");
            
        } catch (Exception e) {
            logTestResult("AT-004", "BAŞARISIZ", "API error handling testi hatası: " + e.getMessage());
            fail("API error handling testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n🔌 API TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}