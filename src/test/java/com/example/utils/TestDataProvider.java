package com.example.utils;

import org.testng.annotations.DataProvider;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PayTR Test Verisi Sağlayıcısı
 * TestNG DataProvider metodları ve test verisi yönetimi
 */
public class TestDataProvider {
    
    // Test kullanıcı bilgileri
    private static final Map<String, String> TEST_USERS = new HashMap<>();
    private static final Map<String, String> TEST_CARDS = new HashMap<>();
    private static final Map<String, String> TEST_MERCHANTS = new HashMap<>();
    
    static {
        // Test kullanıcıları
        TEST_USERS.put("valid_user", "test@paytr.com");
        TEST_USERS.put("valid_password", "Test123!");
        TEST_USERS.put("invalid_user", "invalid@test.com");
        TEST_USERS.put("invalid_password", "wrong123");
        TEST_USERS.put("empty_user", "");
        TEST_USERS.put("empty_password", "");
        
        // Test kartları
        TEST_CARDS.put("visa_valid", "4111111111111111");
        TEST_CARDS.put("mastercard_valid", "5555555555554444");
        TEST_CARDS.put("amex_valid", "378282246310005");
        TEST_CARDS.put("invalid_card", "1234567890123456");
        TEST_CARDS.put("expired_card", "4000000000000002");
        TEST_CARDS.put("declined_card", "4000000000000069");
        
        // Test merchant bilgileri
        TEST_MERCHANTS.put("merchant_id", "12345");
        TEST_MERCHANTS.put("merchant_key", "test_merchant_key");
        TEST_MERCHANTS.put("merchant_salt", "test_salt");
        TEST_MERCHANTS.put("invalid_merchant_id", "99999");
    }
    
    /**
     * Login test verileri
     */
    @DataProvider(name = "loginTestData")
    public static Object[][] getLoginTestData() {
        return new Object[][] {
            // {email, password, expectedResult, testDescription}
            {TEST_USERS.get("valid_user"), TEST_USERS.get("valid_password"), "success", "Geçerli kullanıcı girişi"},
            {TEST_USERS.get("invalid_user"), TEST_USERS.get("valid_password"), "failure", "Geçersiz email"},
            {TEST_USERS.get("valid_user"), TEST_USERS.get("invalid_password"), "failure", "Geçersiz şifre"},
            {TEST_USERS.get("empty_user"), TEST_USERS.get("valid_password"), "failure", "Boş email"},
            {TEST_USERS.get("valid_user"), TEST_USERS.get("empty_password"), "failure", "Boş şifre"},
            {TEST_USERS.get("empty_user"), TEST_USERS.get("empty_password"), "failure", "Boş email ve şifre"},
            {"test@", "123", "failure", "Geçersiz email formatı"},
            {"test@test", "123", "failure", "Eksik email domain"},
            {generateLongString(100) + "@test.com", "123", "failure", "Çok uzun email"},
            {"test@test.com", generateLongString(100), "failure", "Çok uzun şifre"}
        };
    }
    
    /**
     * Kart doğrulama test verileri
     */
    @DataProvider(name = "cardValidationTestData")
    public static Object[][] getCardValidationTestData() {
        return new Object[][] {
            // {cardNumber, expiryMonth, expiryYear, cvv, expectedResult, testDescription}
            {TEST_CARDS.get("visa_valid"), "12", "2025", "123", "success", "Geçerli Visa kartı"},
            {TEST_CARDS.get("mastercard_valid"), "06", "2026", "456", "success", "Geçerli Mastercard"},
            {TEST_CARDS.get("amex_valid"), "03", "2027", "1234", "success", "Geçerli American Express"},
            {TEST_CARDS.get("invalid_card"), "12", "2025", "123", "failure", "Geçersiz kart numarası"},
            {TEST_CARDS.get("expired_card"), "01", "2020", "123", "failure", "Süresi geçmiş kart"},
            {TEST_CARDS.get("declined_card"), "12", "2025", "123", "failure", "Reddedilen kart"},
            {"", "12", "2025", "123", "failure", "Boş kart numarası"},
            {TEST_CARDS.get("visa_valid"), "", "2025", "123", "failure", "Boş ay"},
            {TEST_CARDS.get("visa_valid"), "12", "", "123", "failure", "Boş yıl"},
            {TEST_CARDS.get("visa_valid"), "12", "2025", "", "failure", "Boş CVV"},
            {"123", "12", "2025", "123", "failure", "Çok kısa kart numarası"},
            {generateLongString(20), "12", "2025", "123", "failure", "Çok uzun kart numarası"},
            {TEST_CARDS.get("visa_valid"), "13", "2025", "123", "failure", "Geçersiz ay (13)"},
            {TEST_CARDS.get("visa_valid"), "00", "2025", "123", "failure", "Geçersiz ay (00)"},
            {TEST_CARDS.get("visa_valid"), "12", "2019", "123", "failure", "Geçmiş yıl"},
            {TEST_CARDS.get("visa_valid"), "12", "2025", "12", "failure", "Kısa CVV"},
            {TEST_CARDS.get("visa_valid"), "12", "2025", "12345", "failure", "Uzun CVV"}
        };
    }
    
    /**
     * Ödeme tutarı test verileri
     */
    @DataProvider(name = "paymentAmountTestData")
    public static Object[][] getPaymentAmountTestData() {
        return new Object[][] {
            // {amount, currency, expectedResult, testDescription}
            {"1.00", "TRY", "success", "Minimum tutar"},
            {"10.50", "TRY", "success", "Normal tutar"},
            {"999.99", "TRY", "success", "Yüksek tutar"},
            {"0.01", "TRY", "boundary", "Çok düşük tutar"},
            {"0", "TRY", "failure", "Sıfır tutar"},
            {"-1", "TRY", "failure", "Negatif tutar"},
            {"", "TRY", "failure", "Boş tutar"},
            {"abc", "TRY", "failure", "Geçersiz tutar formatı"},
            {"10.123", "TRY", "boundary", "Çok fazla ondalık"},
            {"999999", "TRY", "boundary", "Çok yüksek tutar"},
            {"10.50", "USD", "success", "USD para birimi"},
            {"10.50", "EUR", "success", "EUR para birimi"},
            {"10.50", "XXX", "failure", "Geçersiz para birimi"},
            {"10.50", "", "failure", "Boş para birimi"}
        };
    }
    
    /**
     * API test verileri
     */
    @DataProvider(name = "apiTestData")
    public static Object[][] getAPITestData() {
        return new Object[][] {
            // {endpoint, method, expectedStatus, testDescription}
            {"/api/health", "GET", 200, "Health check endpoint"},
            {"/api/status", "GET", 200, "Status endpoint"},
            {"/api/version", "GET", 200, "Version endpoint"},
            {"/api/payment", "POST", 201, "Payment creation"},
            {"/api/payment/123", "GET", 200, "Payment retrieval"},
            {"/api/payment/123", "PUT", 200, "Payment update"},
            {"/api/payment/123", "DELETE", 204, "Payment deletion"},
            {"/api/nonexistent", "GET", 404, "Non-existent endpoint"},
            {"/api/payment", "GET", 405, "Method not allowed"},
            {"", "GET", 400, "Empty endpoint"}
        };
    }
    
    /**
     * Güvenlik test verileri
     */
    @DataProvider(name = "securityTestData")
    public static Object[][] getSecurityTestData() {
        return new Object[][] {
            // {inputType, maliciousInput, expectedBehavior, testDescription}
            {"sql_injection", "'; DROP TABLE users; --", "blocked", "SQL Injection saldırısı"},
            {"xss_script", "<script>alert('XSS')</script>", "escaped", "XSS script injection"},
            {"xss_img", "<img src=x onerror=alert('XSS')>", "escaped", "XSS image injection"},
            {"path_traversal", "../../../etc/passwd", "blocked", "Path traversal saldırısı"},
            {"command_injection", "; cat /etc/passwd", "blocked", "Command injection"},
            {"ldap_injection", "*)(&(objectClass=*))", "blocked", "LDAP injection"},
            {"xml_injection", "<?xml version=\"1.0\"?><!DOCTYPE test [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>", "blocked", "XML injection"},
            {"buffer_overflow", generateLongString(10000), "handled", "Buffer overflow denemesi"},
            {"null_byte", "test\0.txt", "blocked", "Null byte injection"},
            {"unicode_bypass", "\\u003cscript\\u003e", "blocked", "Unicode bypass denemesi"}
        };
    }
    
    /**
     * Performans test verileri
     */
    @DataProvider(name = "performanceTestData")
    public static Object[][] getPerformanceTestData() {
        return new Object[][] {
            // {testType, targetMetric, expectedValue, testDescription}
            {"page_load", "load_time", 3000, "Sayfa yükleme süresi (ms)"},
            {"api_response", "response_time", 1000, "API yanıt süresi (ms)"},
            {"memory_usage", "memory_mb", 100, "Bellek kullanımı (MB)"},
            {"cpu_usage", "cpu_percent", 80, "CPU kullanımı (%)"},
            {"concurrent_users", "user_count", 100, "Eşzamanlı kullanıcı sayısı"},
            {"throughput", "requests_per_second", 50, "Saniye başına istek"},
            {"database_query", "query_time", 500, "Veritabanı sorgu süresi (ms)"},
            {"file_upload", "upload_time", 5000, "Dosya yükleme süresi (ms)"},
            {"network_latency", "latency_ms", 100, "Ağ gecikmesi (ms)"},
            {"cache_hit_ratio", "hit_ratio_percent", 90, "Cache hit oranı (%)"}
        };
    }
    
    /**
     * Sınır durumu test verileri
     */
    @DataProvider(name = "boundaryTestData")
    public static Object[][] getBoundaryTestData() {
        return new Object[][] {
            // {fieldType, testValue, expectedResult, testDescription}
            {"email_length", generateEmail(1), "failure", "1 karakter email"},
            {"email_length", generateEmail(50), "success", "50 karakter email"},
            {"email_length", generateEmail(100), "boundary", "100 karakter email"},
            {"email_length", generateEmail(255), "boundary", "255 karakter email"},
            {"email_length", generateEmail(256), "failure", "256 karakter email"},
            {"password_length", generateString(1), "failure", "1 karakter şifre"},
            {"password_length", generateString(8), "success", "8 karakter şifre"},
            {"password_length", generateString(50), "success", "50 karakter şifre"},
            {"password_length", generateString(100), "boundary", "100 karakter şifre"},
            {"password_length", generateString(256), "failure", "256 karakter şifre"},
            {"numeric_input", "0", "boundary", "Sıfır değer"},
            {"numeric_input", "1", "success", "Minimum pozitif değer"},
            {"numeric_input", "999999", "success", "Büyük pozitif değer"},
            {"numeric_input", "-1", "failure", "Negatif değer"},
            {"date_input", "1900-01-01", "boundary", "Çok eski tarih"},
            {"date_input", "2099-12-31", "boundary", "Çok gelecek tarih"},
            {"file_size", "1", "success", "1 byte dosya"},
            {"file_size", "1048576", "success", "1 MB dosya"},
            {"file_size", "10485760", "boundary", "10 MB dosya"},
            {"file_size", "104857600", "failure", "100 MB dosya"}
        };
    }
    
    /**
     * Kullanılabilirlik test verileri
     */
    @DataProvider(name = "usabilityTestData")
    public static Object[][] getUsabilityTestData() {
        return new Object[][] {
            // {deviceType, screenWidth, screenHeight, testDescription}
            {"mobile_small", 320, 568, "iPhone 5/SE"},
            {"mobile_medium", 375, 667, "iPhone 6/7/8"},
            {"mobile_large", 414, 896, "iPhone XR/11"},
            {"tablet_portrait", 768, 1024, "iPad Portrait"},
            {"tablet_landscape", 1024, 768, "iPad Landscape"},
            {"desktop_small", 1366, 768, "Laptop"},
            {"desktop_medium", 1920, 1080, "Desktop HD"},
            {"desktop_large", 2560, 1440, "Desktop QHD"},
            {"ultrawide", 3440, 1440, "Ultrawide Monitor"},
            {"mobile_landscape", 667, 375, "Mobile Landscape"}
        };
    }
    
    /**
     * Entegrasyon test verileri
     */
    @DataProvider(name = "integrationTestData")
    public static Object[][] getIntegrationTestData() {
        return new Object[][] {
            // {serviceType, endpoint, expectedResponse, testDescription}
            {"payment_gateway", "/api/payment/process", "success", "Ödeme gateway entegrasyonu"},
            {"sms_service", "/api/notification/sms", "sent", "SMS servis entegrasyonu"},
            {"email_service", "/api/notification/email", "sent", "Email servis entegrasyonu"},
            {"bank_api", "/api/external/bank-list", "success", "Banka API entegrasyonu"},
            {"currency_api", "/api/external/currency-rates", "success", "Döviz kuru API"},
            {"verification_api", "/api/external/verification", "success", "Doğrulama API"},
            {"logging_service", "/api/internal/logs", "logged", "Log servis entegrasyonu"},
            {"cache_service", "/api/internal/cache", "cached", "Cache servis entegrasyonu"},
            {"database_service", "/api/internal/health", "healthy", "Veritabanı entegrasyonu"},
            {"cdn_service", "/static/assets/test.js", "loaded", "CDN entegrasyonu"}
        };
    }
    
    /**
     * Test merchant bilgileri
     */
    public static Map<String, String> getTestMerchant() {
        Map<String, String> merchant = new HashMap<>();
        merchant.put("merchant_id", TEST_MERCHANTS.get("merchant_id"));
        merchant.put("merchant_key", TEST_MERCHANTS.get("merchant_key"));
        merchant.put("merchant_salt", TEST_MERCHANTS.get("merchant_salt"));
        merchant.put("success_url", "https://test.paytr.com/success");
        merchant.put("fail_url", "https://test.paytr.com/fail");
        return merchant;
    }
    
    /**
     * Test ödeme bilgileri
     */
    public static Map<String, Object> getTestPayment() {
        Map<String, Object> payment = new HashMap<>();
        payment.put("amount", "10.50");
        payment.put("currency", "TRY");
        payment.put("order_id", "TEST_" + System.currentTimeMillis());
        payment.put("description", "Test ödeme işlemi");
        payment.put("customer_name", "Test Kullanıcı");
        payment.put("customer_email", "test@example.com");
        payment.put("customer_phone", "+905551234567");
        return payment;
    }
    
    /**
     * Test kart bilgileri
     */
    public static Map<String, String> getTestCard(String cardType) {
        Map<String, String> card = new HashMap<>();
        
        switch (cardType.toLowerCase()) {
            case "visa":
                card.put("number", TEST_CARDS.get("visa_valid"));
                card.put("type", "Visa");
                break;
            case "mastercard":
                card.put("number", TEST_CARDS.get("mastercard_valid"));
                card.put("type", "Mastercard");
                break;
            case "amex":
                card.put("number", TEST_CARDS.get("amex_valid"));
                card.put("type", "American Express");
                break;
            case "invalid":
                card.put("number", TEST_CARDS.get("invalid_card"));
                card.put("type", "Invalid");
                break;
            default:
                card.put("number", TEST_CARDS.get("visa_valid"));
                card.put("type", "Visa");
        }
        
        card.put("expiry_month", "12");
        card.put("expiry_year", "2025");
        card.put("cvv", "123");
        card.put("holder_name", "TEST KULLANICI");
        
        return card;
    }
    
    /**
     * Random test verisi oluşturma
     */
    public static Map<String, Object> generateRandomTestData() {
        Map<String, Object> data = new HashMap<>();
        Random random = new Random();
        
        data.put("random_string", generateRandomString(10));
        data.put("random_number", random.nextInt(1000));
        data.put("random_email", generateRandomEmail());
        data.put("random_phone", generateRandomPhone());
        data.put("random_amount", String.format("%.2f", random.nextDouble() * 1000));
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return data;
    }
    
    /**
     * Test environment bilgileri
     */
    public static Map<String, String> getTestEnvironment() {
        Map<String, String> env = new HashMap<>();
        env.put("base_url", "https://zeus-uat.paytr.com");
        env.put("api_base_url", "https://zeus-uat.paytr.com/api");
        env.put("environment", "UAT");
        env.put("version", "1.0.0");
        env.put("timeout", "30000");
        env.put("retry_count", "3");
        return env;
    }
    
    // Helper Methods
    
    /**
     * Belirtilen uzunlukta string oluşturur
     */
    private static String generateString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }
    
    /**
     * Belirtilen uzunlukta uzun string oluşturur
     */
    private static String generateLongString(int length) {
        StringBuilder sb = new StringBuilder();
        String pattern = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(pattern.charAt(i % pattern.length()));
        }
        return sb.toString();
    }
    
    /**
     * Belirtilen uzunlukta email oluşturur
     */
    private static String generateEmail(int totalLength) {
        if (totalLength < 5) return "a@b.c"; // Minimum email format
        
        int domainLength = 6; // "@b.com".length()
        int localLength = totalLength - domainLength;
        
        if (localLength < 1) localLength = 1;
        
        return generateString(localLength) + "@b.com";
    }
    
    /**
     * Random string oluşturur
     */
    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Random email oluşturur
     */
    private static String generateRandomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * Random telefon numarası oluşturur
     */
    private static String generateRandomPhone() {
        Random random = new Random();
        return "+9055" + String.format("%08d", random.nextInt(100000000));
    }
    
    /**
     * Test verisi temizleme
     */
    public static void cleanupTestData() {
        // Test sonrası temizlik işlemleri
        System.out.println("Test verisi temizleniyor...");
    }
    
    /**
     * Test verisi doğrulama
     */
    public static boolean validateTestData(Object data) {
        if (data == null) return false;
        if (data instanceof String && ((String) data).trim().isEmpty()) return false;
        if (data instanceof Map && ((Map<?, ?>) data).isEmpty()) return false;
        return true;
    }
    
    /**
     * Test verisi raporlama
     */
    public static void reportTestDataUsage(String testName, Object data) {
        System.out.println("📊 Test Verisi Kullanımı:");
        System.out.println("🧪 Test: " + testName);
        System.out.println("📝 Veri: " + data.toString());
        System.out.println("⏰ Zaman: " + LocalDateTime.now());
        System.out.println("========================================");
    }
}