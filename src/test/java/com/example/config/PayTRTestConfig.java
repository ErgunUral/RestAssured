package com.example.config;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

/**
 * PayTR Test Konfigürasyon Sınıfı
 * PayTR test ortamı için gerekli tüm konfigürasyonları içerir
 */
public class PayTRTestConfig {
    
    // PayTR Test Environment URLs
    public static final String BASE_URL = "https://testweb.paytr.com";
    public static final String LOGIN_URL = BASE_URL;
    public static final String PAYMENT_URL = BASE_URL;
    public static final String VIRTUAL_POS_URL = BASE_URL;
    public static final String API_BASE_URL = BASE_URL + "/api";
    
    // PayTR Test Merchant Bilgileri
    public static final String TEST_MERCHANT_ID = "123456";
    public static final String TEST_MERCHANT_KEY = "test_merchant_key";
    public static final String TEST_MERCHANT_SALT = "test_merchant_salt";
    public static final String TEST_MERCHANT_EMAIL = "test@paytr.com";
    
    // PayTR Test Kullanıcı Bilgileri
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "testpass123";
    public static final String TEST_EMAIL = "testuser@paytr.com";
    public static final String TEST_PHONE = "+905551234567";
    
    // PayTR Test Kart Bilgileri
    public static final Map<String, Map<String, String>> TEST_CARDS = new HashMap<String, Map<String, String>>() {{
        put("VISA_SUCCESS", new HashMap<String, String>() {{
            put("number", "4111111111111111");
            put("holder", "Test User");
            put("expiry_month", "12");
            put("expiry_year", "2025");
            put("cvv", "123");
            put("type", "visa");
            put("expected_result", "success");
        }});
        
        put("MASTERCARD_SUCCESS", new HashMap<String, String>() {{
            put("number", "5555555555554444");
            put("holder", "Test User");
            put("expiry_month", "12");
            put("expiry_year", "2025");
            put("cvv", "123");
            put("type", "mastercard");
            put("expected_result", "success");
        }});
        
        put("AMEX_SUCCESS", new HashMap<String, String>() {{
            put("number", "378282246310005");
            put("holder", "Test User");
            put("expiry_month", "12");
            put("expiry_year", "2025");
            put("cvv", "1234");
            put("type", "amex");
            put("expected_result", "success");
        }});
        
        put("VISA_FAIL", new HashMap<String, String>() {{
            put("number", "4000000000000002");
            put("holder", "Test User");
            put("expiry_month", "12");
            put("expiry_year", "2025");
            put("cvv", "123");
            put("type", "visa");
            put("expected_result", "fail");
        }});
        
        put("INVALID_CARD", new HashMap<String, String>() {{
            put("number", "1234567890123456");
            put("holder", "Test User");
            put("expiry_month", "12");
            put("expiry_year", "2025");
            put("cvv", "123");
            put("type", "invalid");
            put("expected_result", "error");
        }});
        
        put("EXPIRED_CARD", new HashMap<String, String>() {{
            put("number", "4111111111111111");
            put("holder", "Test User");
            put("expiry_month", "01");
            put("expiry_year", "2020");
            put("cvv", "123");
            put("type", "visa");
            put("expected_result", "expired");
        }});
    }};
    
    // PayTR Test Tutarları ve Para Birimleri
    public static final Map<String, Object> TEST_AMOUNTS = new HashMap<String, Object>() {{
        put("MIN_AMOUNT", 1.00);
        put("MAX_AMOUNT", 10000.00);
        put("STANDARD_AMOUNT", 100.00);
        put("DECIMAL_AMOUNT", 99.99);
        put("LARGE_AMOUNT", 5000.00);
        put("CURRENCIES", Arrays.asList("TRY", "USD", "EUR", "GBP"));
        put("DEFAULT_CURRENCY", "TRY");
    }};
    
    // PayTR Taksit Seçenekleri
    public static final Map<String, Object> INSTALLMENT_OPTIONS = new HashMap<String, Object>() {{
        put("AVAILABLE_INSTALLMENTS", Arrays.asList(1, 2, 3, 6, 9, 12));
        put("MIN_INSTALLMENT_AMOUNT", 100.00);
        put("MAX_INSTALLMENTS", 12);
        put("COMMISSION_RATES", new HashMap<Integer, Double>() {{
            put(1, 0.0);
            put(2, 2.5);
            put(3, 3.5);
            put(6, 6.0);
            put(9, 9.0);
            put(12, 12.0);
        }});
    }};
    
    // PayTR Test Timeout Değerleri
    public static final Map<String, Integer> TIMEOUTS = new HashMap<String, Integer>() {{
        put("PAGE_LOAD", 30);
        put("ELEMENT_WAIT", 10);
        put("PAYMENT_PROCESS", 60);
        put("API_RESPONSE", 30);
        put("IMPLICIT_WAIT", 10);
        put("SCRIPT_TIMEOUT", 30);
    }};
    
    // PayTR Test Browser Konfigürasyonları
    public static final Map<String, Object> BROWSER_CONFIG = new HashMap<String, Object>() {{
        put("DEFAULT_BROWSER", "chrome");
        put("HEADLESS", false);
        put("WINDOW_SIZE", "1920x1080");
        put("CHROME_OPTIONS", Arrays.asList(
            "--disable-web-security",
            "--allow-running-insecure-content",
            "--ignore-certificate-errors",
            "--disable-features=VizDisplayCompositor"
        ));
        put("FIREFOX_OPTIONS", Arrays.asList(
            "--width=1920",
            "--height=1080"
        ));
    }};
    
    // PayTR Test Veritabanı Konfigürasyonu
    public static final Map<String, String> DATABASE_CONFIG = new HashMap<String, String>() {{
        put("DB_URL", "jdbc:mysql://localhost:3306/paytr_test");
        put("DB_USERNAME", "paytr_test_user");
        put("DB_PASSWORD", "paytr_test_pass");
        put("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
    }};
    
    // PayTR Test API Endpoints
    public static final Map<String, String> API_ENDPOINTS = new HashMap<String, String>() {{
        put("LOGIN", "/api/auth/login");
        put("PAYMENT", "/api/payment/process");
        put("VIRTUAL_POS", "/api/pos/virtual");
        put("INSTALLMENT", "/api/payment/installment");
        put("CARD_VALIDATION", "/api/card/validate");
        put("TRANSACTION_STATUS", "/api/transaction/status");
        put("REFUND", "/api/payment/refund");
        put("CANCEL", "/api/payment/cancel");
    }};
    
    // PayTR Test Güvenlik Konfigürasyonu
    public static final Map<String, Object> SECURITY_CONFIG = new HashMap<String, Object>() {{
        put("SSL_REQUIRED", true);
        put("CSRF_PROTECTION", true);
        put("XSS_PROTECTION", true);
        put("SQL_INJECTION_PROTECTION", true);
        put("RATE_LIMITING", true);
        put("SESSION_TIMEOUT", 30); // dakika
        put("MAX_LOGIN_ATTEMPTS", 5);
        put("PASSWORD_MIN_LENGTH", 8);
    }};
    
    // PayTR Test Raporlama Konfigürasyonu
    public static final Map<String, Object> REPORTING_CONFIG = new HashMap<String, Object>() {{
        put("REPORT_FORMAT", "HTML");
        put("SCREENSHOT_ON_FAILURE", true);
        put("VIDEO_RECORDING", false);
        put("LOG_LEVEL", "INFO");
        put("REPORT_DIRECTORY", "test-output/paytr-reports");
        put("SCREENSHOT_DIRECTORY", "test-output/screenshots");
    }};
    
    // PayTR Test Ortam Bilgileri
    public static final Map<String, String> ENVIRONMENT_INFO = new HashMap<String, String>() {{
        put("ENVIRONMENT", "TEST");
        put("VERSION", "1.0.0");
        put("LAST_UPDATED", "2024-01-15");
        put("CONTACT", "test@paytr.com");
        put("DOCUMENTATION", "https://docs.paytr.com/test");
    }};
    
    // PayTR Test Veri Sağlayıcıları
    public static final Map<String, List<Object[]>> TEST_DATA_PROVIDERS = new HashMap<String, List<Object[]>>() {{
        put("VALID_CARDS", Arrays.asList(
            new Object[]{"4111111111111111", "Test User", "12", "2025", "123", "visa"},
            new Object[]{"5555555555554444", "Test User", "12", "2025", "123", "mastercard"},
            new Object[]{"378282246310005", "Test User", "12", "2025", "1234", "amex"}
        ));
        
        put("INVALID_CARDS", Arrays.asList(
            new Object[]{"1234567890123456", "Invalid Card", "12", "2025", "123"},
            new Object[]{"4111111111111112", "Wrong Checksum", "12", "2025", "123"},
            new Object[]{"", "Empty Card", "12", "2025", "123"}
        ));
        
        put("PAYMENT_AMOUNTS", Arrays.asList(
            new Object[]{1.00, "TRY"},
            new Object[]{100.00, "TRY"},
            new Object[]{999.99, "TRY"},
            new Object[]{50.00, "USD"},
            new Object[]{75.00, "EUR"}
        ));
        
        put("INSTALLMENT_DATA", Arrays.asList(
            new Object[]{100.00, 1, 0.0},
            new Object[]{200.00, 2, 2.5},
            new Object[]{300.00, 3, 3.5},
            new Object[]{600.00, 6, 6.0},
            new Object[]{1200.00, 12, 12.0}
        ));
    }};
    
    // PayTR Test Locator Stratejileri
    public static final Map<String, String> LOCATOR_STRATEGIES = new HashMap<String, String>() {{
        put("CARD_NUMBER", "//input[contains(@name, 'card') or contains(@id, 'card')]");
        put("CARD_HOLDER", "//input[contains(@name, 'holder') or contains(@name, 'name')]");
        put("EXPIRY_MONTH", "//select[contains(@name, 'month') or contains(@name, 'ay')]");
        put("EXPIRY_YEAR", "//select[contains(@name, 'year') or contains(@name, 'yil')]");
        put("CVV", "//input[contains(@name, 'cvv') or contains(@name, 'cvc')]");
        put("AMOUNT", "//input[contains(@name, 'amount') or contains(@name, 'tutar')]");
        put("CURRENCY", "//select[contains(@name, 'currency') or contains(@name, 'para')]");
        put("INSTALLMENT", "//select[contains(@name, 'installment') or contains(@name, 'taksit')]");
        put("PAY_BUTTON", "//button[contains(text(), 'Öde') or contains(text(), 'Pay')]");
        put("LOGIN_BUTTON", "//button[contains(text(), 'Giriş') or contains(text(), 'Login')]");
    }};
    
    // PayTR Test Mesajları
    public static final Map<String, String> TEST_MESSAGES = new HashMap<String, String>() {{
        put("SUCCESS_PAYMENT", "Ödeme başarıyla tamamlandı");
        put("FAILED_PAYMENT", "Ödeme başarısız");
        put("INVALID_CARD", "Geçersiz kart numarası");
        put("EXPIRED_CARD", "Kartın son kullanma tarihi geçmiş");
        put("INSUFFICIENT_FUNDS", "Yetersiz bakiye");
        put("CARD_BLOCKED", "Kart bloke");
        put("INVALID_CVV", "Geçersiz CVV");
        put("INVALID_AMOUNT", "Geçersiz tutar");
        put("LOGIN_SUCCESS", "Giriş başarılı");
        put("LOGIN_FAILED", "Giriş başarısız");
        put("INVALID_CREDENTIALS", "Geçersiz kullanıcı bilgileri");
    }};
    
    // PayTR Test Utility Metodları
    public static String getTestCard(String cardType, String field) {
        Map<String, String> card = TEST_CARDS.get(cardType);
        return card != null ? card.get(field) : null;
    }
    
    public static Double getTestAmount(String amountType) {
        Object amount = TEST_AMOUNTS.get(amountType);
        return amount instanceof Double ? (Double) amount : null;
    }
    
    public static List<String> getSupportedCurrencies() {
        return (List<String>) TEST_AMOUNTS.get("CURRENCIES");
    }
    
    public static List<Integer> getAvailableInstallments() {
        return (List<Integer>) INSTALLMENT_OPTIONS.get("AVAILABLE_INSTALLMENTS");
    }
    
    public static Double getInstallmentCommission(Integer installmentCount) {
        Map<Integer, Double> commissions = (Map<Integer, Double>) INSTALLMENT_OPTIONS.get("COMMISSION_RATES");
        return commissions.get(installmentCount);
    }
    
    public static Integer getTimeout(String timeoutType) {
        return TIMEOUTS.get(timeoutType);
    }
    
    public static String getApiEndpoint(String endpointName) {
        return API_BASE_URL + API_ENDPOINTS.get(endpointName);
    }
    
    public static String getLocatorStrategy(String elementName) {
        return LOCATOR_STRATEGIES.get(elementName);
    }
    
    public static String getTestMessage(String messageKey) {
        return TEST_MESSAGES.get(messageKey);
    }
    
    public static boolean isSecurityFeatureEnabled(String feature) {
        Object enabled = SECURITY_CONFIG.get(feature);
        return enabled instanceof Boolean ? (Boolean) enabled : false;
    }
    
    public static String getBrowserOption(String optionName) {
        Object option = BROWSER_CONFIG.get(optionName);
        return option != null ? option.toString() : null;
    }
    
    public static String getEnvironmentInfo(String infoKey) {
        return ENVIRONMENT_INFO.get(infoKey);
    }
    
    // PayTR Test Veri Doğrulama Metodları
    public static boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{13,19}");
    }
    
    public static boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }
    
    public static boolean isValidAmount(Double amount) {
        Double minAmount = getTestAmount("MIN_AMOUNT");
        Double maxAmount = getTestAmount("MAX_AMOUNT");
        return amount != null && amount >= minAmount && amount <= maxAmount;
    }
    
    public static boolean isValidCurrency(String currency) {
        return getSupportedCurrencies().contains(currency);
    }
    
    public static boolean isValidInstallment(Integer installment) {
        return getAvailableInstallments().contains(installment);
    }
    
    // PayTR Test Hash Hesaplama (Örnek)
    public static String calculatePaymentHash(String merchantId, String orderId, String amount, String currency) {
        // Bu gerçek bir hash hesaplama değil, sadece test amaçlı
        String data = merchantId + orderId + amount + currency + TEST_MERCHANT_SALT;
        return "test_hash_" + data.hashCode();
    }
    
    // PayTR Test Order ID Üretimi
    public static String generateOrderId() {
        return "TEST_ORDER_" + System.currentTimeMillis();
    }
    
    // PayTR Test Email Üretimi
    public static String generateTestEmail() {
        return "test_" + System.currentTimeMillis() + "@paytr.com";
    }
    
    // PayTR Test Telefon Üretimi
    public static String generateTestPhone() {
        return "+9055" + (10000000 + (int)(Math.random() * 90000000));
    }
    
    // Backward compatibility constants
    public static final String MERCHANT_ID = TEST_MERCHANT_ID;
    public static final int PAGE_LOAD_TIMEOUT = getTimeout("PAGE_LOAD");
    public static final int API_RESPONSE_TIMEOUT = getTimeout("API_RESPONSE");
    public static final boolean HEADLESS_MODE = (Boolean) BROWSER_CONFIG.get("HEADLESS");
    public static final String DEFAULT_BROWSER = (String) BROWSER_CONFIG.get("DEFAULT_BROWSER");
}