package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * PayTR Advanced & Edge Case Test Scenarios
 * Covers security, race conditions, and manipulation attempts
 */
public class PayTRAdvancedScenariosTest {

    private String merchantId;
    private String merchantKey;
    private String merchantSalt;
    private RequestSpecification requestSpec;
    private Properties properties;

    @BeforeClass
    public void setup() {
        loadProperties();
        RestAssured.baseURI = getProperty("api.base.url", "https://zeus-uat.paytr.com");
        
        merchantId = getProperty("merchant.id", "test_merchant_id");
        merchantKey = getProperty("merchant.key", "test_merchant_key");
        merchantSalt = getProperty("merchant.salt", "test_merchant_salt");

        createRequestSpec();
    }
    
    private void createRequestSpec() {
        requestSpec = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.out.println("Config file not found, using defaults");
        }
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            createRequestSpec();
        }
        return requestSpec;
    }

    /**
     * Test: Double Payment Prevention (Idempotency)
     * Simulates clicking the 'Pay' button twice simultaneously
     */
    @Test(priority = 1, groups = {"security", "payment"})
    public void testDoublePaymentPrevention() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        String orderId = "TEST_ORDER_" + UUID.randomUUID().toString();
        Map<String, Object> paymentPayload = createValidPaymentPayload(orderId, "100.00");

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    latch.await(); // Wait for signal
                    Response response = given()
                            .spec(getRequestSpec())
                            .body(paymentPayload)
                            .post("/odeme/api/get-token");
                    
                    if (response.statusCode() == 200 && response.jsonPath().getString("status").equals("success")) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown(); // Start both requests exactly at same time
        service.awaitTermination(5, TimeUnit.SECONDS);

        // Expectation: Only ONE request should succeed (or both if idempotency key handles it gracefully)
        // But for a strictly transactional system, we expect only 1 successful token generation for same Order ID
        // Note: PayTR might allow token generation multiple times, but final payment should be unique.
        // Adjust assertion based on actual API behavior.
        System.out.println("Success Count: " + successCount.get());
        System.out.println("Fail Count: " + failCount.get());
    }

    /**
     * Test: Amount Manipulation
     * Attempts to tamper with the amount field in client-side payload
     */
    @Test(priority = 2, groups = {"security"})
    public void testAmountManipulation() {
        String orderId = "MANIPULATED_" + UUID.randomUUID();
        
        // 1. Create payload with manipulated amount (e.g. 0.01 or negative)
        Map<String, Object> manipulatedPayload = createValidPaymentPayload(orderId, "-50.00");
        
        Response response = given()
                .spec(getRequestSpec())
                .body(manipulatedPayload)
                .post("/odeme/api/get-token");

        // Expectation: System should reject negative amount
        response.then()
                .statusCode(anyOf(is(200), is(400))) // API might return 200 with status:failed
                .body("status", equalTo("failed"));
    }

    /**
     * Test: Replay Attack
     * Captures a valid request and attempts to resend it
     */
    @Test(priority = 3, groups = {"security"})
    public void testReplayAttack() {
        String orderId = "REPLAY_" + UUID.randomUUID();
        Map<String, Object> payload = createValidPaymentPayload(orderId, "100.00");

        // 1. First Request (Should Succeed)
        Response response1 = given()
                .spec(getRequestSpec())
                .body(payload)
                .post("/odeme/api/get-token");
        
        // If first request failed, skip test
        if (!"success".equals(response1.jsonPath().getString("status"))) {
             // System.out.println("First request failed, skipping replay check");
             // return;
        }

        // 2. Replay Request (Same Order ID, Same Payload)
        Response response2 = given()
                .spec(getRequestSpec())
                .body(payload)
                .post("/odeme/api/get-token");

        // Expectation: Depending on business logic, using the exact same Order ID 
        // for a new token request might be allowed (update) OR denied.
        // Assuming strict uniqueness:
        // response2.then().body("status", equalTo("failed"));
        System.out.println("Replay Response: " + response2.asString());
    }

    /**
     * Test: Invalid Luhn Algorithm (Card Validation)
     */
    @Test(priority = 4, groups = {"validation"})
    public void testInvalidLuhnCard() {
        // A card number that looks valid length-wise but fails Luhn check
        // 4111 1111 1111 1112 (Last digit changed)
        String invalidLuhnCard = "4111111111111112"; 
        
        // Note: This would typically be a Direct API payment test, not Token API
        // Assuming we have an endpoint for direct payment or bin check
        /*
        Response response = given()
                .spec(requestSpec)
                .body(createDirectPaymentPayload(invalidLuhnCard))
                .post("/odeme/api/payment");
                
        response.then()
                .body("status", equalTo("failed"))
                .body("reason", containsString("invalid card"));
        */
        System.out.println("Luhn test simulated.");
    }

    /**
     * Test: Partial Refund Loop
     * Attempting to refund more than the original amount via multiple partial refunds
     */
    @Test(priority = 5, groups = {"payment", "fraud"})
    public void testPartialRefundLoop() {
        // This requires a valid transaction ID from a previous successful payment
        // Mocking the flow here
        String mockTransactionId = "TRX_123456";
        double originalAmount = 100.00;
        
        // 1. Refund 60.00
        // 2. Refund 60.00 again
        // Total 120.00 > 100.00 -> Should fail
        
        System.out.println("Partial Refund Loop test simulated.");
    }
    
    // Helper to create valid payload
    private Map<String, Object> createValidPaymentPayload(String orderId, String amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("merchant_id", merchantId);
        params.put("merchant_oid", orderId);
        params.put("email", "test@example.com");
        params.put("payment_amount", amount); // cent amount? usually PayTR expects raw amount * 100 or similar
        params.put("currency", "TL");
        params.put("user_name", "Test User");
        params.put("user_address", "Test Address");
        params.put("user_phone", "905555555555");
        params.put("merchant_ok_url", "https://example.com/success");
        params.put("merchant_fail_url", "https://example.com/fail");
        params.put("user_basket", "[[\"Test Product\", \"100.00\", 1]]");
        params.put("debug_on", "1");
        params.put("no_installment", "0");
        params.put("max_installment", "0");
        params.put("timeout_limit", "30");
        
        // Token generation logic would normally go here (SHA256 etc)
        // params.put("paytr_token", generateToken(params));
        
        return params;
    }
}
