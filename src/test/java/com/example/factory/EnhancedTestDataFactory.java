package com.example.factory;

import com.example.config.PayTRTestConfig;
import org.testng.annotations.DataProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Enhanced Test Data Factory
 * Gelişmiş test verisi üretimi için factory sınıfı
 * 
 * Özellikler:
 * - Multi-currency test data
 * - 3D Secure test scenarios
 * - Fraud detection test cases
 * - Edge case test data
 * - Performance test data
 * - Accessibility test data
 */
public class EnhancedTestDataFactory {

    private static final Random random = new Random();
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Multi-Currency Test Data Provider
     * Çoklu para birimi test verileri
     */
    @DataProvider(name = "multiCurrencyData")
    public static Object[][] getMultiCurrencyData() {
        return new Object[][]{
            // {currency, amount, exchangeRate, expectedConvertedAmount, locale}
            {"TL", "10000", "1.0", "10000", "tr_TR"},
            {"USD", "100", "30.50", "3050", "en_US"},
            {"EUR", "100", "33.20", "3320", "de_DE"},
            {"GBP", "100", "38.75", "3875", "en_GB"},
            {"JPY", "10000", "0.20", "2000", "ja_JP"}
        };
    }

    /**
     * 3D Secure Test Data Provider
     * 3D Secure test senaryoları
     */
    @DataProvider(name = "threeDSecureData")
    public static Object[][] getThreeDSecureData() {
        return new Object[][]{
            // {cardNumber, scenario, expectedResult, authMethod}
            {"4355084355084358", "CHALLENGE", "SUCCESS", "SMS"},
            {"4355084355084366", "FRICTIONLESS", "SUCCESS", "BIOMETRIC"},
            {"4355084355084374", "FALLBACK", "FALLBACK_TO_NON_3DS", "NONE"},
            {"4355084355084382", "TIMEOUT", "TIMEOUT", "SMS"},
            {"4355084355084390", "FAILED", "AUTHENTICATION_FAILED", "SMS"}
        };
    }

    /**
     * Fraud Detection Test Data Provider
     * Dolandırıcılık tespit test verileri
     */
    @DataProvider(name = "fraudDetectionData")
    public static Object[][] getFraudDetectionData() {
        return new Object[][]{
            // {riskScore, velocityCount, geoLocation, deviceFingerprint, expectedAction}
            {"LOW", "1", "TR", "TRUSTED_DEVICE", "APPROVE"},
            {"MEDIUM", "3", "TR", "NEW_DEVICE", "REVIEW"},
            {"HIGH", "5", "UNKNOWN", "SUSPICIOUS_DEVICE", "DECLINE"},
            {"CRITICAL", "10", "HIGH_RISK_COUNTRY", "BLACKLISTED_DEVICE", "BLOCK"}
        };
    }

    /**
     * Edge Case Test Data Provider
     * Sınır değer test verileri
     */
    @DataProvider(name = "edgeCaseData")
    public static Object[][] getEdgeCaseData() {
        return new Object[][]{
            // {testType, inputValue, expectedBehavior}
            {"MIN_AMOUNT", "1", "ACCEPT"},
            {"MAX_AMOUNT", "999999999", "ACCEPT"},
            {"ZERO_AMOUNT", "0", "REJECT"},
            {"NEGATIVE_AMOUNT", "-100", "REJECT"},
            {"DECIMAL_AMOUNT", "100.50", "ACCEPT"},
            {"LONG_EMAIL", generateLongEmail(), "ACCEPT"},
            {"SPECIAL_CHARS", "test@üğşçöı.com", "ACCEPT"},
            {"UNICODE_NAME", "测试用户", "ACCEPT"},
            {"EMPTY_STRING", "", "REJECT"},
            {"NULL_VALUE", null, "REJECT"}
        };
    }

    /**
     * Performance Test Data Provider
     * Performans test verileri
     */
    @DataProvider(name = "performanceTestData")
    public static Object[][] getPerformanceTestData() {
        return new Object[][]{
            // {concurrentUsers, requestsPerUser, expectedResponseTime, testDuration}
            {"10", "5", "2000", "30"},
            {"25", "10", "3000", "60"},
            {"50", "20", "5000", "120"},
            {"100", "50", "10000", "300"}
        };
    }

    /**
     * Accessibility Test Data Provider
     * Erişilebilirlik test verileri
     */
    @DataProvider(name = "accessibilityTestData")
    public static Object[][] getAccessibilityTestData() {
        return new Object[][]{
            // {testType, language, direction, assistiveTech, expectedSupport}
            {"WCAG_AA", "tr", "LTR", "SCREEN_READER", "FULL"},
            {"WCAG_AA", "ar", "RTL", "SCREEN_READER", "FULL"},
            {"KEYBOARD_NAV", "en", "LTR", "KEYBOARD_ONLY", "FULL"},
            {"MOBILE_TOUCH", "tr", "LTR", "TOUCH_SCREEN", "FULL"},
            {"HIGH_CONTRAST", "tr", "LTR", "HIGH_CONTRAST", "FULL"}
        };
    }

    /**
     * Webhook Test Data Provider
     * Webhook test verileri
     */
    @DataProvider(name = "webhookTestData")
    public static Object[][] getWebhookTestData() {
        return new Object[][]{
            // {eventType, retryCount, signatureValid, expectedDelivery}
            {"PAYMENT_SUCCESS", "0", "true", "DELIVERED"},
            {"PAYMENT_FAILED", "1", "true", "DELIVERED"},
            {"PAYMENT_PENDING", "2", "false", "SIGNATURE_FAILED"},
            {"REFUND_SUCCESS", "3", "true", "MAX_RETRIES_REACHED"},
            {"CHARGEBACK", "0", "true", "DELIVERED"}
        };
    }

    /**
     * Chaos Engineering Test Data Provider
     * Kaos mühendisliği test verileri
     */
    @DataProvider(name = "chaosTestData")
    public static Object[][] getChaosTestData() {
        return new Object[][]{
            // {chaosType, intensity, duration, expectedResilience}
            {"NETWORK_LATENCY", "HIGH", "30", "GRACEFUL_DEGRADATION"},
            {"SERVICE_FAILURE", "MEDIUM", "60", "CIRCUIT_BREAKER_ACTIVE"},
            {"RESOURCE_EXHAUSTION", "HIGH", "45", "LOAD_BALANCING"},
            {"RANDOM_ERRORS", "LOW", "120", "ERROR_RECOVERY"},
            {"DATABASE_SLOWDOWN", "MEDIUM", "90", "TIMEOUT_HANDLING"}
        };
    }

    /**
     * Business Logic Test Data Provider
     * İş mantığı test verileri
     */
    @DataProvider(name = "businessLogicTestData")
    public static Object[][] getBusinessLogicTestData() {
        return new Object[][]{
            // {scenario, merchantType, paymentMethod, installments, commission}
            {"STANDARD_PAYMENT", "STANDARD", "CREDIT_CARD", "1", "2.95"},
            {"INSTALLMENT_PAYMENT", "PREMIUM", "CREDIT_CARD", "6", "3.50"},
            {"DEBIT_PAYMENT", "STANDARD", "DEBIT_CARD", "1", "1.95"},
            {"CORPORATE_PAYMENT", "CORPORATE", "BANK_TRANSFER", "1", "1.50"},
            {"HIGH_AMOUNT_PAYMENT", "VIP", "CREDIT_CARD", "12", "4.50"}
        };
    }

    /**
     * Security Test Data Provider
     * Güvenlik test verileri
     */
    @DataProvider(name = "securityTestData")
    public static Object[][] getSecurityTestData() {
        return new Object[][]{
            // {attackType, payload, expectedBlocked}
            {"SQL_INJECTION", "'; DROP TABLE users; --", "true"},
            {"XSS", "<script>alert('xss')</script>", "true"},
            {"CSRF", "malicious_token_12345", "true"},
            {"RATE_LIMIT", "EXCESSIVE_REQUESTS", "true"},
            {"JWT_MANIPULATION", "tampered.jwt.token", "true"}
        };
    }

    /**
     * Data Migration Test Data Provider
     * Veri taşıma test verileri
     */
    @DataProvider(name = "dataMigrationTestData")
    public static Object[][] getDataMigrationTestData() {
        return new Object[][]{
            // {sourceSystem, dataType, batchSize, expectedSuccessRate}
            {"LEGACY_SYSTEM_1", "TRANSACTIONS", "100", "95"},
            {"LEGACY_SYSTEM_2", "CUSTOMERS", "500", "98"},
            {"EXTERNAL_API", "PRODUCTS", "50", "90"},
            {"CSV_IMPORT", "MERCHANTS", "200", "92"},
            {"DATABASE_SYNC", "REPORTS", "1000", "99"}
        };
    }

    // Utility Methods

    /**
     * Generates a complete payment request with all required fields
     */
    public static Map<String, Object> generateCompletePaymentRequest(String testId) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        paymentData.put("user_ip", "127.0.0.1");
        paymentData.put("merchant_oid", testId + "_" + System.currentTimeMillis());
        paymentData.put("email", "test." + testId.toLowerCase() + "@example.com");
        paymentData.put("payment_amount", "10000");
        paymentData.put("currency", "TL");
        paymentData.put("test_mode", "1");
        paymentData.put("debug_on", "1");
        paymentData.put("no_installment", "0");
        paymentData.put("max_installment", "12");
        paymentData.put("user_name", "Test User " + testId);
        paymentData.put("user_address", "Test Address, Istanbul, Turkey");
        paymentData.put("user_phone", "+90 555 123 45 67");
        paymentData.put("merchant_ok_url", "https://www.example.com/success");
        paymentData.put("merchant_fail_url", "https://www.example.com/fail");
        paymentData.put("timeout_limit", "30");
        paymentData.put("lang", "tr");
        return paymentData;
    }

    /**
     * Generates payment data with specific currency
     */
    public static Map<String, Object> generateCurrencySpecificPayment(String currency, String amount) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("CURRENCY_TEST");
        paymentData.put("currency", currency);
        paymentData.put("payment_amount", amount);
        
        // Set locale based on currency
        switch (currency) {
            case "USD":
                paymentData.put("lang", "en");
                break;
            case "EUR":
                paymentData.put("lang", "de");
                break;
            case "GBP":
                paymentData.put("lang", "en");
                break;
            default:
                paymentData.put("lang", "tr");
        }
        
        return paymentData;
    }

    /**
     * Generates 3D Secure specific payment data
     */
    public static Map<String, Object> generate3DSecurePayment(String cardNumber, String scenario) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("3DS_TEST");
        paymentData.put("cc_owner", "Test User");
        paymentData.put("card_number", cardNumber);
        paymentData.put("expiry_month", "12");
        paymentData.put("expiry_year", "25");
        paymentData.put("cvv", "123");
        paymentData.put("threeds_scenario", scenario);
        return paymentData;
    }

    /**
     * Generates fraud detection test payment
     */
    public static Map<String, Object> generateFraudTestPayment(String riskLevel) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("FRAUD_TEST");
        
        switch (riskLevel) {
            case "HIGH":
                paymentData.put("user_ip", "192.168.1.100"); // Suspicious IP
                paymentData.put("payment_amount", "50000"); // High amount
                paymentData.put("user_name", "Suspicious User");
                break;
            case "MEDIUM":
                paymentData.put("user_ip", "10.0.0.1");
                paymentData.put("payment_amount", "25000");
                break;
            default:
                paymentData.put("user_ip", "127.0.0.1");
                paymentData.put("payment_amount", "10000");
        }
        
        paymentData.put("fraud_risk_level", riskLevel);
        return paymentData;
    }

    /**
     * Generates edge case test data
     */
    public static Map<String, Object> generateEdgeCasePayment(String edgeCase) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("EDGE_TEST");
        
        switch (edgeCase) {
            case "MIN_AMOUNT":
                paymentData.put("payment_amount", "1");
                break;
            case "MAX_AMOUNT":
                paymentData.put("payment_amount", "999999999");
                break;
            case "SPECIAL_CHARS":
                paymentData.put("user_name", "Ömer Çağlar Şahin");
                paymentData.put("email", "test@üğşçöı.com");
                break;
            case "UNICODE":
                paymentData.put("user_name", "测试用户");
                paymentData.put("user_address", "北京市朝阳区");
                break;
            case "LONG_STRING":
                paymentData.put("user_name", generateLongString(255));
                break;
        }
        
        return paymentData;
    }

    /**
     * Generates performance test payment data
     */
    public static Map<String, Object> generatePerformanceTestPayment(int index) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("PERF_TEST");
        paymentData.put("merchant_oid", "PERF_" + index + "_" + System.currentTimeMillis());
        paymentData.put("email", "perf.test" + index + "@example.com");
        paymentData.put("payment_amount", String.valueOf(10000 + (index * 100)));
        return paymentData;
    }

    /**
     * Generates accessibility test payment data
     */
    public static Map<String, Object> generateAccessibilityTestPayment(String language, String direction) {
        Map<String, Object> paymentData = generateCompletePaymentRequest("ACCESS_TEST");
        paymentData.put("lang", language);
        paymentData.put("text_direction", direction);
        
        if ("ar".equals(language)) {
            paymentData.put("user_name", "مستخدم تجريبي");
            paymentData.put("user_address", "الرياض، المملكة العربية السعودية");
        } else if ("tr".equals(language)) {
            paymentData.put("user_name", "Test Kullanıcısı");
            paymentData.put("user_address", "İstanbul, Türkiye");
        }
        
        return paymentData;
    }

    // Helper Methods

    private static String generateLongEmail() {
        StringBuilder email = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            email.append("test");
        }
        email.append("@example.com");
        return email.toString();
    }

    private static String generateLongString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generates random test data for stress testing
     */
    public static Map<String, Object> generateRandomPaymentData() {
        Map<String, Object> paymentData = generateCompletePaymentRequest("RANDOM_TEST");
        paymentData.put("payment_amount", String.valueOf(random.nextInt(100000) + 1000));
        paymentData.put("merchant_oid", "RANDOM_" + UUID.randomUUID().toString());
        paymentData.put("email", "random" + random.nextInt(10000) + "@example.com");
        return paymentData;
    }

    /**
     * Generates timestamp for test tracking
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(dateFormatter);
    }

    /**
     * Generates unique test identifier
     */
    public static String generateUniqueTestId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }
}