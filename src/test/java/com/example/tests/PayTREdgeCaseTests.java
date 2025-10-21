package com.example.tests;

import com.example.config.BaseTest;
import com.example.config.PayTRTestConfig;
import com.example.utils.PayTRTestDataProvider;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * PayTR Edge Case Test Scenarios
 * Tests boundary conditions, extreme values, and unusual scenarios
 * 
 * Test Categories:
 * - EC-001: Boundary Value Testing
 * - EC-002: Extreme Value Testing
 * - EC-003: Null/Empty Value Testing
 * - EC-004: Special Character Testing
 * - EC-005: Concurrent Transaction Testing
 */
public class PayTREdgeCaseTests extends BaseTest {

    /**
     * EC-001: Boundary Value Testing
     * Tests payment amounts at boundary values (minimum, maximum, just below/above limits)
     */
    @Test(groups = {"edge-case", "boundary", "payment"}, 
          description = "Test payment processing with boundary values")
    public void testBoundaryValuePayments() {
        System.out.println("🔍 Testing boundary value payments...");
        
        // Test minimum payment amount (0.01 TL)
        Map<String, Object> minPaymentData = new HashMap<>();
        minPaymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        minPaymentData.put("user_ip", "127.0.0.1");
        minPaymentData.put("merchant_oid", "EC001_MIN_" + System.currentTimeMillis());
        minPaymentData.put("email", "test@example.com");
        minPaymentData.put("payment_amount", "1"); // 0.01 TL (in kuruş)
        minPaymentData.put("currency", "TL");
        minPaymentData.put("test_mode", "1");
        
        Response minResponse = given()
            .spec(requestSpec)
            .body(minPaymentData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .spec(responseSpec)
            .extract().response();
            
        Assert.assertEquals(minResponse.getStatusCode(), 200);
        Assert.assertEquals(minResponse.jsonPath().getString("status"), "success");
        
        // Test maximum payment amount (999,999.99 TL)
        Map<String, Object> maxPaymentData = new HashMap<>();
        maxPaymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        maxPaymentData.put("user_ip", "127.0.0.1");
        maxPaymentData.put("merchant_oid", "EC001_MAX_" + System.currentTimeMillis());
        maxPaymentData.put("email", "test@example.com");
        maxPaymentData.put("payment_amount", "99999999"); // 999,999.99 TL (in kuruş)
        maxPaymentData.put("currency", "TL");
        maxPaymentData.put("test_mode", "1");
        
        Response maxResponse = given()
            .spec(requestSpec)
            .body(maxPaymentData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .spec(responseSpec)
            .extract().response();
            
        Assert.assertEquals(maxResponse.getStatusCode(), 200);
        
        // Test just above maximum (should fail)
        Map<String, Object> overMaxData = new HashMap<>();
        overMaxData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        overMaxData.put("user_ip", "127.0.0.1");
        overMaxData.put("merchant_oid", "EC001_OVER_" + System.currentTimeMillis());
        overMaxData.put("email", "test@example.com");
        overMaxData.put("payment_amount", "100000000"); // Over limit
        overMaxData.put("currency", "TL");
        overMaxData.put("test_mode", "1");
        
        Response overMaxResponse = given()
            .spec(requestSpec)
            .body(overMaxData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertTrue(overMaxResponse.getStatusCode() == 400 || 
                         overMaxResponse.jsonPath().getString("status").equals("failed"));
        
        System.out.println("✅ Boundary value testing completed successfully");
    }

    /**
     * EC-002: Extreme Value Testing
     * Tests system behavior with extreme input values
     */
    @Test(groups = {"edge-case", "extreme", "stress"}, 
          description = "Test system behavior with extreme values")
    public void testExtremeValueHandling() {
        System.out.println("🔍 Testing extreme value handling...");
        
        // Test extremely long merchant order ID
        String extremelyLongOid = "A".repeat(1000); // 1000 characters
        
        Map<String, Object> extremeData = new HashMap<>();
        extremeData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        extremeData.put("user_ip", "127.0.0.1");
        extremeData.put("merchant_oid", extremelyLongOid);
        extremeData.put("email", "test@example.com");
        extremeData.put("payment_amount", "10000");
        extremeData.put("currency", "TL");
        extremeData.put("test_mode", "1");
        
        Response extremeResponse = given()
            .spec(requestSpec)
            .body(extremeData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        // Should handle gracefully (either accept or reject with proper error)
        Assert.assertTrue(extremeResponse.getStatusCode() == 200 || 
                         extremeResponse.getStatusCode() == 400);
        
        // Test extremely long email
        String extremeEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
        
        Map<String, Object> extremeEmailData = new HashMap<>();
        extremeEmailData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        extremeEmailData.put("user_ip", "127.0.0.1");
        extremeEmailData.put("merchant_oid", "EC002_EMAIL_" + System.currentTimeMillis());
        extremeEmailData.put("email", extremeEmail);
        extremeEmailData.put("payment_amount", "10000");
        extremeEmailData.put("currency", "TL");
        extremeEmailData.put("test_mode", "1");
        
        Response extremeEmailResponse = given()
            .spec(requestSpec)
            .body(extremeEmailData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertTrue(extremeEmailResponse.getStatusCode() == 200 || 
                         extremeEmailResponse.getStatusCode() == 400);
        
        System.out.println("✅ Extreme value testing completed successfully");
    }

    /**
     * EC-003: Null and Empty Value Testing
     * Tests system behavior with null and empty values
     */
    @Test(groups = {"edge-case", "null-empty", "validation"}, 
          description = "Test system behavior with null and empty values")
    public void testNullAndEmptyValues() {
        System.out.println("🔍 Testing null and empty value handling...");
        
        // Test with null email
        Map<String, Object> nullEmailData = new HashMap<>();
        nullEmailData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        nullEmailData.put("user_ip", "127.0.0.1");
        nullEmailData.put("merchant_oid", "EC003_NULL_" + System.currentTimeMillis());
        nullEmailData.put("email", null);
        nullEmailData.put("payment_amount", "10000");
        nullEmailData.put("currency", "TL");
        nullEmailData.put("test_mode", "1");
        
        Response nullEmailResponse = given()
            .spec(requestSpec)
            .body(nullEmailData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertEquals(nullEmailResponse.getStatusCode(), 400);
        
        // Test with empty email
        Map<String, Object> emptyEmailData = new HashMap<>();
        emptyEmailData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        emptyEmailData.put("user_ip", "127.0.0.1");
        emptyEmailData.put("merchant_oid", "EC003_EMPTY_" + System.currentTimeMillis());
        emptyEmailData.put("email", "");
        emptyEmailData.put("payment_amount", "10000");
        emptyEmailData.put("currency", "TL");
        emptyEmailData.put("test_mode", "1");
        
        Response emptyEmailResponse = given()
            .spec(requestSpec)
            .body(emptyEmailData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertEquals(emptyEmailResponse.getStatusCode(), 400);
        
        // Test with zero payment amount
        Map<String, Object> zeroAmountData = new HashMap<>();
        zeroAmountData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        zeroAmountData.put("user_ip", "127.0.0.1");
        zeroAmountData.put("merchant_oid", "EC003_ZERO_" + System.currentTimeMillis());
        zeroAmountData.put("email", "test@example.com");
        zeroAmountData.put("payment_amount", "0");
        zeroAmountData.put("currency", "TL");
        zeroAmountData.put("test_mode", "1");
        
        Response zeroAmountResponse = given()
            .spec(requestSpec)
            .body(zeroAmountData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertEquals(zeroAmountResponse.getStatusCode(), 400);
        
        System.out.println("✅ Null and empty value testing completed successfully");
    }

    /**
     * EC-004: Special Character Testing
     * Tests system behavior with special characters and encoding
     */
    @Test(groups = {"edge-case", "special-chars", "encoding"}, 
          description = "Test system behavior with special characters")
    public void testSpecialCharacterHandling() {
        System.out.println("🔍 Testing special character handling...");
        
        // Test with special characters in merchant order ID
        String specialCharOid = "EC004_<script>alert('xss')</script>_" + System.currentTimeMillis();
        
        Map<String, Object> specialCharData = new HashMap<>();
        specialCharData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        specialCharData.put("user_ip", "127.0.0.1");
        specialCharData.put("merchant_oid", specialCharOid);
        specialCharData.put("email", "test@example.com");
        specialCharData.put("payment_amount", "10000");
        specialCharData.put("currency", "TL");
        specialCharData.put("test_mode", "1");
        
        Response specialCharResponse = given()
            .spec(requestSpec)
            .body(specialCharData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        // Should handle special characters safely
        Assert.assertTrue(specialCharResponse.getStatusCode() == 200 || 
                         specialCharResponse.getStatusCode() == 400);
        
        // Test with Unicode characters
        String unicodeOid = "EC004_测试_العربية_" + System.currentTimeMillis();
        
        Map<String, Object> unicodeData = new HashMap<>();
        unicodeData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        unicodeData.put("user_ip", "127.0.0.1");
        unicodeData.put("merchant_oid", unicodeOid);
        unicodeData.put("email", "test@example.com");
        unicodeData.put("payment_amount", "10000");
        unicodeData.put("currency", "TL");
        unicodeData.put("test_mode", "1");
        
        Response unicodeResponse = given()
            .spec(requestSpec)
            .body(unicodeData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        Assert.assertTrue(unicodeResponse.getStatusCode() == 200 || 
                         unicodeResponse.getStatusCode() == 400);
        
        System.out.println("✅ Special character testing completed successfully");
    }

    /**
     * EC-005: Concurrent Transaction Testing
     * Tests system behavior with concurrent transactions
     */
    @Test(groups = {"edge-case", "concurrent", "performance"}, 
          description = "Test system behavior with concurrent transactions")
    public void testConcurrentTransactions() {
        System.out.println("🔍 Testing concurrent transaction handling...");
        
        // Create multiple concurrent requests
        Thread[] threads = new Thread[5];
        boolean[] results = new boolean[5];
        
        for (int i = 0; i < 5; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    Map<String, Object> concurrentData = new HashMap<>();
                    concurrentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                    concurrentData.put("user_ip", "127.0.0.1");
                    concurrentData.put("merchant_oid", "EC005_CONCURRENT_" + threadIndex + "_" + System.currentTimeMillis());
                    concurrentData.put("email", "test" + threadIndex + "@example.com");
                    concurrentData.put("payment_amount", "10000");
                    concurrentData.put("currency", "TL");
                    concurrentData.put("test_mode", "1");
                    
                    Response response = given()
                        .spec(requestSpec)
                        .body(concurrentData)
                        .when()
                        .post("/odeme/api/get-token")
                        .then()
                        .extract().response();
                        
                    results[threadIndex] = response.getStatusCode() == 200;
                    
                } catch (Exception e) {
                    results[threadIndex] = false;
                    System.err.println("Thread " + threadIndex + " failed: " + e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join(10000); // 10 second timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Check results
        int successCount = 0;
        for (boolean result : results) {
            if (result) successCount++;
        }
        
        // At least 80% should succeed in concurrent scenario
        Assert.assertTrue(successCount >= 4, 
            "Expected at least 4 out of 5 concurrent requests to succeed, but got: " + successCount);
        
        System.out.println("✅ Concurrent transaction testing completed successfully");
        System.out.println("📊 Concurrent success rate: " + successCount + "/5 (" + (successCount * 20) + "%)");
    }
}