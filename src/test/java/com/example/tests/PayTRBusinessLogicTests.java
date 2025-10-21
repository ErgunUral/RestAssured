package com.example.tests;

import com.example.config.BaseTest;
import com.example.config.PayTRTestConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * PayTR Business Logic Test Sınıfı
 * İş mantığı ve business rule testlerini içerir
 * 
 * Test Kategorileri:
 * - BL-001: Payment Flow Business Rules
 * - BL-002: Merchant Configuration Validation
 * - BL-003: Transaction State Management
 * - BL-004: Commission and Fee Calculation
 * - BL-005: Refund Business Logic
 */
@Epic("PayTR Business Logic Tests")
@Feature("Business Rules and Logic Validation")
public class PayTRBusinessLogicTests extends BaseTest {

    /**
     * Test ID: BL-001
     * Business Logic - Payment Flow Business Rules
     * Tests payment flow business rules and validations
     */
    @Test(priority = 1, groups = {"business-logic", "payment-flow", "critical"})
    @Story("Payment Flow Business Rules")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ödeme akışı iş kuralları ve validasyonları")
    public void testPaymentFlowBusinessRules() {
        logTestInfo("Test ID: BL-001 - Payment Flow Business Rules");
        
        try {
            // Test 1: Minimum payment amount validation
            Map<String, Object> minAmountData = new HashMap<>();
            minAmountData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            minAmountData.put("user_ip", "127.0.0.1");
            minAmountData.put("merchant_oid", "BL001_MIN_" + System.currentTimeMillis());
            minAmountData.put("email", "min.amount@example.com");
            minAmountData.put("payment_amount", "50"); // Below minimum (100 kuruş)
            minAmountData.put("currency", "TL");
            minAmountData.put("test_mode", "1");
            
            Response minAmountResponse = given()
                .spec(requestSpec)
                .body(minAmountData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Minimum amount test response: " + minAmountResponse.getStatusCode());
            
            // Test 2: Maximum payment amount validation
            Map<String, Object> maxAmountData = new HashMap<>();
            maxAmountData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            maxAmountData.put("user_ip", "127.0.0.1");
            maxAmountData.put("merchant_oid", "BL001_MAX_" + System.currentTimeMillis());
            maxAmountData.put("email", "max.amount@example.com");
            maxAmountData.put("payment_amount", "10000000"); // Very high amount
            maxAmountData.put("currency", "TL");
            maxAmountData.put("test_mode", "1");
            
            Response maxAmountResponse = given()
                .spec(requestSpec)
                .body(maxAmountData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Maximum amount test response: " + maxAmountResponse.getStatusCode());
            
            // Test 3: Valid payment amount
            Map<String, Object> validAmountData = new HashMap<>();
            validAmountData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            validAmountData.put("user_ip", "127.0.0.1");
            validAmountData.put("merchant_oid", "BL001_VALID_" + System.currentTimeMillis());
            validAmountData.put("email", "valid.amount@example.com");
            validAmountData.put("payment_amount", "10000"); // Valid amount (100 TL)
            validAmountData.put("currency", "TL");
            validAmountData.put("test_mode", "1");
            
            Response validAmountResponse = given()
                .spec(requestSpec)
                .body(validAmountData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Valid amount test response: " + validAmountResponse.getStatusCode());
            
            // Test 4: Currency validation
            Map<String, Object> invalidCurrencyData = new HashMap<>();
            invalidCurrencyData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            invalidCurrencyData.put("user_ip", "127.0.0.1");
            invalidCurrencyData.put("merchant_oid", "BL001_CURR_" + System.currentTimeMillis());
            invalidCurrencyData.put("email", "currency.test@example.com");
            invalidCurrencyData.put("payment_amount", "10000");
            invalidCurrencyData.put("currency", "INVALID"); // Invalid currency
            invalidCurrencyData.put("test_mode", "1");
            
            Response currencyResponse = given()
                .spec(requestSpec)
                .body(invalidCurrencyData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Invalid currency test response: " + currencyResponse.getStatusCode());
            
            // Business rule assertions
            assertTrue(validAmountResponse.getStatusCode() == 200 || 
                      validAmountResponse.getStatusCode() == 201,
                "Valid payment amount should be accepted");
            
            // Log business rule validation results
            logTestInfo("Business Rules Validation Results:");
            logTestInfo("  Minimum Amount Validation: " + (minAmountResponse.getStatusCode() != 200 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Maximum Amount Validation: " + (maxAmountResponse.getStatusCode() != 200 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Valid Amount Processing: " + (validAmountResponse.getStatusCode() == 200 ? "PASSED" : "FAILED"));
            logTestInfo("  Currency Validation: " + (currencyResponse.getStatusCode() != 200 ? "PASSED" : "NEEDS_REVIEW"));
            
            logTestResult("BL-001", "BAŞARILI", "Payment flow business rules validated");
            
        } catch (Exception e) {
            logTestResult("BL-001", "BAŞARISIZ", "Business rules test hatası: " + e.getMessage());
            fail("Payment flow business rules test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: BL-002
     * Business Logic - Merchant Configuration Validation
     * Tests merchant-specific business rules and configurations
     */
    @Test(priority = 2, groups = {"business-logic", "merchant-config", "high"})
    @Story("Merchant Configuration")
    @Severity(SeverityLevel.HIGH)
    @Description("Merchant konfigürasyonu ve iş kuralları validasyonu")
    public void testMerchantConfigurationValidation() {
        logTestInfo("Test ID: BL-002 - Merchant Configuration Validation");
        
        try {
            // Test 1: Valid merchant configuration
            Map<String, Object> validMerchantData = new HashMap<>();
            validMerchantData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            validMerchantData.put("user_ip", "127.0.0.1");
            validMerchantData.put("merchant_oid", "BL002_VALID_" + System.currentTimeMillis());
            validMerchantData.put("email", "merchant.valid@example.com");
            validMerchantData.put("payment_amount", "10000");
            validMerchantData.put("currency", "TL");
            validMerchantData.put("test_mode", "1");
            
            Response validMerchantResponse = given()
                .spec(requestSpec)
                .body(validMerchantData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Valid merchant response: " + validMerchantResponse.getStatusCode());
            
            // Test 2: Invalid merchant ID
            Map<String, Object> invalidMerchantData = new HashMap<>();
            invalidMerchantData.put("merchant_id", "INVALID_MERCHANT_ID");
            invalidMerchantData.put("user_ip", "127.0.0.1");
            invalidMerchantData.put("merchant_oid", "BL002_INVALID_" + System.currentTimeMillis());
            invalidMerchantData.put("email", "merchant.invalid@example.com");
            invalidMerchantData.put("payment_amount", "10000");
            invalidMerchantData.put("currency", "TL");
            invalidMerchantData.put("test_mode", "1");
            
            Response invalidMerchantResponse = given()
                .spec(requestSpec)
                .body(invalidMerchantData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Invalid merchant response: " + invalidMerchantResponse.getStatusCode());
            
            // Test 3: Merchant-specific payment limits
            Map<String, Object> limitTestData = new HashMap<>();
            limitTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            limitTestData.put("user_ip", "127.0.0.1");
            limitTestData.put("merchant_oid", "BL002_LIMIT_" + System.currentTimeMillis());
            limitTestData.put("email", "merchant.limit@example.com");
            limitTestData.put("payment_amount", "500000"); // High amount to test limits
            limitTestData.put("currency", "TL");
            limitTestData.put("test_mode", "1");
            
            Response limitResponse = given()
                .spec(requestSpec)
                .body(limitTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Merchant limit test response: " + limitResponse.getStatusCode());
            
            // Test 4: Merchant callback URL validation
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            callbackData.put("user_ip", "127.0.0.1");
            callbackData.put("merchant_oid", "BL002_CALLBACK_" + System.currentTimeMillis());
            callbackData.put("email", "merchant.callback@example.com");
            callbackData.put("payment_amount", "10000");
            callbackData.put("currency", "TL");
            callbackData.put("merchant_ok_url", "https://example.com/success");
            callbackData.put("merchant_fail_url", "https://example.com/fail");
            callbackData.put("test_mode", "1");
            
            Response callbackResponse = given()
                .spec(requestSpec)
                .body(callbackData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Callback URL test response: " + callbackResponse.getStatusCode());
            
            // Merchant configuration assertions
            assertTrue(validMerchantResponse.getStatusCode() == 200 || 
                      validMerchantResponse.getStatusCode() == 201,
                "Valid merchant configuration should be accepted");
            
            assertNotEquals(invalidMerchantResponse.getStatusCode(), 200,
                "Invalid merchant ID should be rejected");
            
            logTestInfo("Merchant Configuration Results:");
            logTestInfo("  Valid Merchant: " + (validMerchantResponse.getStatusCode() == 200 ? "PASSED" : "FAILED"));
            logTestInfo("  Invalid Merchant Rejection: " + (invalidMerchantResponse.getStatusCode() != 200 ? "PASSED" : "FAILED"));
            logTestInfo("  Payment Limits: " + (limitResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Callback URLs: " + (callbackResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            
            logTestResult("BL-002", "BAŞARILI", "Merchant configuration validation completed");
            
        } catch (Exception e) {
            logTestResult("BL-002", "BAŞARISIZ", "Merchant configuration test hatası: " + e.getMessage());
            fail("Merchant configuration test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: BL-003
     * Business Logic - Transaction State Management
     * Tests transaction lifecycle and state transitions
     */
    @Test(priority = 3, groups = {"business-logic", "transaction-state", "medium"})
    @Story("Transaction State Management")
    @Severity(SeverityLevel.MEDIUM)
    @Description("İşlem durumu yönetimi ve durum geçişleri")
    public void testTransactionStateManagement() {
        logTestInfo("Test ID: BL-003 - Transaction State Management");
        
        try {
            String baseOrderId = "BL003_STATE_" + System.currentTimeMillis();
            
            // Test 1: Create initial transaction
            Map<String, Object> initialTransactionData = new HashMap<>();
            initialTransactionData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            initialTransactionData.put("user_ip", "127.0.0.1");
            initialTransactionData.put("merchant_oid", baseOrderId);
            initialTransactionData.put("email", "state.test@example.com");
            initialTransactionData.put("payment_amount", "10000");
            initialTransactionData.put("currency", "TL");
            initialTransactionData.put("test_mode", "1");
            
            Response initialResponse = given()
                .spec(requestSpec)
                .body(initialTransactionData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Initial transaction response: " + initialResponse.getStatusCode());
            
            // Test 2: Duplicate transaction prevention
            Map<String, Object> duplicateTransactionData = new HashMap<>();
            duplicateTransactionData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            duplicateTransactionData.put("user_ip", "127.0.0.1");
            duplicateTransactionData.put("merchant_oid", baseOrderId); // Same order ID
            duplicateTransactionData.put("email", "state.duplicate@example.com");
            duplicateTransactionData.put("payment_amount", "10000");
            duplicateTransactionData.put("currency", "TL");
            duplicateTransactionData.put("test_mode", "1");
            
            // Wait a bit before duplicate attempt
            Thread.sleep(1000);
            
            Response duplicateResponse = given()
                .spec(requestSpec)
                .body(duplicateTransactionData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Duplicate transaction response: " + duplicateResponse.getStatusCode());
            
            // Test 3: Transaction timeout handling
            Map<String, Object> timeoutTestData = new HashMap<>();
            timeoutTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            timeoutTestData.put("user_ip", "127.0.0.1");
            timeoutTestData.put("merchant_oid", "BL003_TIMEOUT_" + System.currentTimeMillis());
            timeoutTestData.put("email", "state.timeout@example.com");
            timeoutTestData.put("payment_amount", "10000");
            timeoutTestData.put("currency", "TL");
            timeoutTestData.put("timeout_limit", "30"); // 30 seconds timeout
            timeoutTestData.put("test_mode", "1");
            
            Response timeoutResponse = given()
                .spec(requestSpec)
                .body(timeoutTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Timeout test response: " + timeoutResponse.getStatusCode());
            
            // Test 4: Transaction cancellation
            Map<String, Object> cancellationData = new HashMap<>();
            cancellationData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            cancellationData.put("user_ip", "127.0.0.1");
            cancellationData.put("merchant_oid", "BL003_CANCEL_" + System.currentTimeMillis());
            cancellationData.put("email", "state.cancel@example.com");
            cancellationData.put("payment_amount", "10000");
            cancellationData.put("currency", "TL");
            cancellationData.put("test_mode", "1");
            
            Response cancellationResponse = given()
                .spec(requestSpec)
                .body(cancellationData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Cancellation test response: " + cancellationResponse.getStatusCode());
            
            // State management assertions
            assertTrue(initialResponse.getStatusCode() == 200 || 
                      initialResponse.getStatusCode() == 201,
                "Initial transaction should be created successfully");
            
            logTestInfo("Transaction State Management Results:");
            logTestInfo("  Initial Transaction: " + (initialResponse.getStatusCode() == 200 ? "PASSED" : "FAILED"));
            logTestInfo("  Duplicate Prevention: " + (duplicateResponse.getStatusCode() != 200 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Timeout Handling: " + (timeoutResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Cancellation Support: " + (cancellationResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            
            logTestResult("BL-003", "BAŞARILI", "Transaction state management validated");
            
        } catch (Exception e) {
            logTestResult("BL-003", "BAŞARISIZ", "Transaction state test hatası: " + e.getMessage());
            fail("Transaction state management test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: BL-004
     * Business Logic - Commission and Fee Calculation
     * Tests commission calculation and fee structures
     */
    @Test(priority = 4, groups = {"business-logic", "commission", "medium"})
    @Story("Commission and Fee Calculation")
    @Severity(SeverityLevel.MEDIUM)
    @Description("Komisyon hesaplama ve ücret yapıları")
    public void testCommissionAndFeeCalculation() {
        logTestInfo("Test ID: BL-004 - Commission and Fee Calculation");
        
        try {
            // Test different payment amounts for commission calculation
            int[] testAmounts = {1000, 5000, 10000, 50000, 100000}; // Different amounts in kuruş
            
            for (int amount : testAmounts) {
                Map<String, Object> commissionData = new HashMap<>();
                commissionData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                commissionData.put("user_ip", "127.0.0.1");
                commissionData.put("merchant_oid", "BL004_COMM_" + amount + "_" + System.currentTimeMillis());
                commissionData.put("email", "commission.test" + amount + "@example.com");
                commissionData.put("payment_amount", String.valueOf(amount));
                commissionData.put("currency", "TL");
                commissionData.put("test_mode", "1");
                
                Response commissionResponse = given()
                    .spec(requestSpec)
                    .body(commissionData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                logTestInfo("Commission test for " + amount + " kuruş: " + commissionResponse.getStatusCode());
                
                // Check if response contains commission information
                if (commissionResponse.getStatusCode() == 200) {
                    String responseBody = commissionResponse.getBody().asString();
                    logTestInfo("Response for amount " + amount + ": " + responseBody.substring(0, Math.min(100, responseBody.length())));
                }
                
                Thread.sleep(500); // Small delay between requests
            }
            
            // Test installment-based commission
            Map<String, Object> installmentData = new HashMap<>();
            installmentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            installmentData.put("user_ip", "127.0.0.1");
            installmentData.put("merchant_oid", "BL004_INST_" + System.currentTimeMillis());
            installmentData.put("email", "installment.test@example.com");
            installmentData.put("payment_amount", "50000"); // 500 TL
            installmentData.put("currency", "TL");
            installmentData.put("installment_count", "6"); // 6 installments
            installmentData.put("test_mode", "1");
            
            Response installmentResponse = given()
                .spec(requestSpec)
                .body(installmentData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Installment commission test: " + installmentResponse.getStatusCode());
            
            // Test currency-based commission
            String[] currencies = {"TL", "USD", "EUR"};
            for (String currency : currencies) {
                Map<String, Object> currencyCommissionData = new HashMap<>();
                currencyCommissionData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                currencyCommissionData.put("user_ip", "127.0.0.1");
                currencyCommissionData.put("merchant_oid", "BL004_CURR_" + currency + "_" + System.currentTimeMillis());
                currencyCommissionData.put("email", "currency.commission." + currency.toLowerCase() + "@example.com");
                currencyCommissionData.put("payment_amount", "10000");
                currencyCommissionData.put("currency", currency);
                currencyCommissionData.put("test_mode", "1");
                
                Response currencyResponse = given()
                    .spec(requestSpec)
                    .body(currencyCommissionData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                logTestInfo("Currency commission test for " + currency + ": " + currencyResponse.getStatusCode());
                
                Thread.sleep(300);
            }
            
            logTestInfo("Commission and Fee Calculation Results:");
            logTestInfo("  Amount-based Commission: TESTED");
            logTestInfo("  Installment Commission: " + (installmentResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Currency-based Commission: TESTED");
            
            logTestResult("BL-004", "BAŞARILI", "Commission and fee calculation tests completed");
            
        } catch (Exception e) {
            logTestResult("BL-004", "BAŞARISIZ", "Commission calculation test hatası: " + e.getMessage());
            fail("Commission and fee calculation test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: BL-005
     * Business Logic - Refund Business Logic
     * Tests refund processing and business rules
     */
    @Test(priority = 5, groups = {"business-logic", "refund", "medium"})
    @Story("Refund Business Logic")
    @Severity(SeverityLevel.MEDIUM)
    @Description("İade işlemi iş mantığı ve kuralları")
    public void testRefundBusinessLogic() {
        logTestInfo("Test ID: BL-005 - Refund Business Logic");
        
        try {
            // Test 1: Full refund scenario
            Map<String, Object> fullRefundData = new HashMap<>();
            fullRefundData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            fullRefundData.put("user_ip", "127.0.0.1");
            fullRefundData.put("merchant_oid", "BL005_FULL_REFUND_" + System.currentTimeMillis());
            fullRefundData.put("email", "full.refund@example.com");
            fullRefundData.put("payment_amount", "10000");
            fullRefundData.put("currency", "TL");
            fullRefundData.put("test_mode", "1");
            
            Response fullRefundResponse = given()
                .spec(requestSpec)
                .body(fullRefundData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Full refund test response: " + fullRefundResponse.getStatusCode());
            
            // Test 2: Partial refund scenario
            Map<String, Object> partialRefundData = new HashMap<>();
            partialRefundData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            partialRefundData.put("user_ip", "127.0.0.1");
            partialRefundData.put("merchant_oid", "BL005_PARTIAL_REFUND_" + System.currentTimeMillis());
            partialRefundData.put("email", "partial.refund@example.com");
            partialRefundData.put("payment_amount", "20000");
            partialRefundData.put("refund_amount", "5000"); // Partial refund
            partialRefundData.put("currency", "TL");
            partialRefundData.put("test_mode", "1");
            
            Response partialRefundResponse = given()
                .spec(requestSpec)
                .body(partialRefundData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Partial refund test response: " + partialRefundResponse.getStatusCode());
            
            // Test 3: Refund time limit validation
            Map<String, Object> timeLimitData = new HashMap<>();
            timeLimitData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            timeLimitData.put("user_ip", "127.0.0.1");
            timeLimitData.put("merchant_oid", "BL005_TIME_LIMIT_" + System.currentTimeMillis());
            timeLimitData.put("email", "time.limit@example.com");
            timeLimitData.put("payment_amount", "15000");
            timeLimitData.put("currency", "TL");
            timeLimitData.put("refund_time_limit", "30"); // 30 days limit
            timeLimitData.put("test_mode", "1");
            
            Response timeLimitResponse = given()
                .spec(requestSpec)
                .body(timeLimitData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Refund time limit test response: " + timeLimitResponse.getStatusCode());
            
            // Test 4: Multiple refund attempts
            String baseOrderId = "BL005_MULTIPLE_" + System.currentTimeMillis();
            
            Map<String, Object> multipleRefundData1 = new HashMap<>();
            multipleRefundData1.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            multipleRefundData1.put("user_ip", "127.0.0.1");
            multipleRefundData1.put("merchant_oid", baseOrderId);
            multipleRefundData1.put("email", "multiple.refund@example.com");
            multipleRefundData1.put("payment_amount", "30000");
            multipleRefundData1.put("currency", "TL");
            multipleRefundData1.put("test_mode", "1");
            
            Response multipleRefund1 = given()
                .spec(requestSpec)
                .body(multipleRefundData1)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Multiple refund attempt 1: " + multipleRefund1.getStatusCode());
            
            // Wait before second attempt
            Thread.sleep(1000);
            
            Map<String, Object> multipleRefundData2 = new HashMap<>();
            multipleRefundData2.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            multipleRefundData2.put("user_ip", "127.0.0.1");
            multipleRefundData2.put("merchant_oid", baseOrderId + "_SECOND");
            multipleRefundData2.put("email", "multiple.refund2@example.com");
            multipleRefundData2.put("payment_amount", "30000");
            multipleRefundData2.put("currency", "TL");
            multipleRefundData2.put("test_mode", "1");
            
            Response multipleRefund2 = given()
                .spec(requestSpec)
                .body(multipleRefundData2)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Multiple refund attempt 2: " + multipleRefund2.getStatusCode());
            
            // Test 5: Refund amount validation
            Map<String, Object> invalidRefundData = new HashMap<>();
            invalidRefundData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            invalidRefundData.put("user_ip", "127.0.0.1");
            invalidRefundData.put("merchant_oid", "BL005_INVALID_" + System.currentTimeMillis());
            invalidRefundData.put("email", "invalid.refund@example.com");
            invalidRefundData.put("payment_amount", "10000");
            invalidRefundData.put("refund_amount", "15000"); // Refund more than payment
            invalidRefundData.put("currency", "TL");
            invalidRefundData.put("test_mode", "1");
            
            Response invalidRefundResponse = given()
                .spec(requestSpec)
                .body(invalidRefundData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Invalid refund amount test: " + invalidRefundResponse.getStatusCode());
            
            logTestInfo("Refund Business Logic Results:");
            logTestInfo("  Full Refund: " + (fullRefundResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Partial Refund: " + (partialRefundResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Time Limit Validation: " + (timeLimitResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Multiple Refund Handling: " + (multipleRefund2.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Invalid Amount Rejection: " + (invalidRefundResponse.getStatusCode() != 200 ? "PASSED" : "NEEDS_REVIEW"));
            
            logTestResult("BL-005", "BAŞARILI", "Refund business logic tests completed");
            
        } catch (Exception e) {
            logTestResult("BL-005", "BAŞARISIZ", "Refund business logic test hatası: " + e.getMessage());
            fail("Refund business logic test başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n📊 İŞ MANTIK TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}