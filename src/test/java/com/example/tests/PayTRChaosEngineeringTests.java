package com.example.tests;

import com.example.config.BaseTest;
import com.example.config.PayTRTestConfig;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * PayTR Chaos Engineering Test Scenarios
 * Tests system resilience under adverse conditions
 * 
 * Test Categories:
 * - CE-001: Network Latency Simulation
 * - CE-002: Service Degradation Testing
 * - CE-003: Resource Exhaustion Testing
 * - CE-004: Random Failure Injection
 * - CE-005: Circuit Breaker Testing
 */
public class PayTRChaosEngineeringTests extends BaseTest {

    private static final Random random = new Random();

    /**
     * CE-001: Network Latency Simulation
     * Tests system behavior under high network latency conditions
     */
    @Test(groups = {"chaos", "network", "latency"}, 
          description = "Test system resilience under network latency")
    public void testNetworkLatencyResilience() {
        System.out.println("🌪️ Testing network latency resilience...");
        
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        paymentData.put("user_ip", "127.0.0.1");
        paymentData.put("merchant_oid", "CE001_LATENCY_" + System.currentTimeMillis());
        paymentData.put("email", "chaos.test@example.com");
        paymentData.put("payment_amount", "10000");
        paymentData.put("currency", "TL");
        paymentData.put("test_mode", "1");
        
        // Simulate network latency by adding artificial delays
        long startTime = System.currentTimeMillis();
        
        try {
            // Add random delay to simulate network issues
            Thread.sleep(random.nextInt(2000) + 1000); // 1-3 seconds delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Response response = given()
            .spec(requestSpec)
            .when()
            .body(paymentData)
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // System should handle the request despite latency
        Assert.assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 408);
        
        // Log performance metrics
        System.out.println("📊 Total request time with simulated latency: " + totalTime + "ms");
        
        // If successful, verify response structure
        if (response.getStatusCode() == 200) {
            Assert.assertNotNull(response.jsonPath().getString("status"));
        }
        
        System.out.println("✅ Network latency resilience test completed");
    }

    /**
     * CE-002: Service Degradation Testing
     * Tests system behavior when services are partially degraded
     */
    @Test(groups = {"chaos", "degradation", "resilience"}, 
          description = "Test system behavior under service degradation")
    public void testServiceDegradationHandling() {
        System.out.println("🌪️ Testing service degradation handling...");
        
        // Test multiple requests to simulate load during degradation
        int successCount = 0;
        int totalRequests = 10;
        
        for (int i = 0; i < totalRequests; i++) {
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            paymentData.put("user_ip", "127.0.0.1");
            paymentData.put("merchant_oid", "CE002_DEGRAD_" + i + "_" + System.currentTimeMillis());
            paymentData.put("email", "degradation.test" + i + "@example.com");
            paymentData.put("payment_amount", "10000");
            paymentData.put("currency", "TL");
            paymentData.put("test_mode", "1");
            
            try {
                // Simulate random service delays
                if (random.nextBoolean()) {
                    Thread.sleep(random.nextInt(1000) + 500); // 0.5-1.5 seconds
                }
                
                Response response = given()
                    .spec(requestSpec)
                    .body(paymentData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                    
                if (response.getStatusCode() == 200) {
                    successCount++;
                }
                
                // Small delay between requests
                Thread.sleep(100);
                
            } catch (Exception e) {
                System.err.println("Request " + i + " failed: " + e.getMessage());
            }
        }
        
        // Calculate success rate
        double successRate = (double) successCount / totalRequests * 100;
        
        // System should maintain at least 70% success rate during degradation
        Assert.assertTrue(successRate >= 70.0, 
            "Expected at least 70% success rate during degradation, but got: " + successRate + "%");
        
        System.out.println("📊 Service degradation success rate: " + successRate + "%");
        System.out.println("✅ Service degradation handling test completed");
    }

    /**
     * CE-003: Resource Exhaustion Testing
     * Tests system behavior under resource constraints
     */
    @Test(groups = {"chaos", "resource", "exhaustion"}, 
          description = "Test system behavior under resource exhaustion")
    public void testResourceExhaustionResilience() {
        System.out.println("🌪️ Testing resource exhaustion resilience...");
        
        // Create large payload to simulate memory pressure
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("Large data chunk ").append(i).append(" ");
        }
        
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        paymentData.put("user_ip", "127.0.0.1");
        paymentData.put("merchant_oid", "CE003_RESOURCE_" + System.currentTimeMillis());
        paymentData.put("email", "resource.test@example.com");
        paymentData.put("payment_amount", "10000");
        paymentData.put("currency", "TL");
        paymentData.put("test_mode", "1");
        paymentData.put("user_name", largeData.toString()); // Large data field
        
        Response response = given()
            .spec(requestSpec)
            .body(paymentData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        // System should handle large payloads gracefully
        Assert.assertTrue(response.getStatusCode() == 200 || 
                         response.getStatusCode() == 413 || // Payload too large
                         response.getStatusCode() == 400);   // Bad request
        
        // If rejected, should have proper error message
        if (response.getStatusCode() != 200) {
            Assert.assertNotNull(response.getBody().asString());
        }
        
        System.out.println("📊 Resource exhaustion response code: " + response.getStatusCode());
        System.out.println("✅ Resource exhaustion resilience test completed");
    }

    /**
     * CE-004: Random Failure Injection
     * Tests system behavior with random failures
     */
    @Test(groups = {"chaos", "random", "failure"}, 
          description = "Test system behavior with random failure injection")
    public void testRandomFailureInjection() {
        System.out.println("🌪️ Testing random failure injection...");
        
        int totalAttempts = 15;
        int successCount = 0;
        int expectedFailures = 0;
        
        for (int i = 0; i < totalAttempts; i++) {
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            paymentData.put("user_ip", "127.0.0.1");
            paymentData.put("merchant_oid", "CE004_RANDOM_" + i + "_" + System.currentTimeMillis());
            paymentData.put("email", "random.test" + i + "@example.com");
            paymentData.put("payment_amount", "10000");
            paymentData.put("currency", "TL");
            paymentData.put("test_mode", "1");
            
            // Randomly inject failures
            boolean injectFailure = random.nextInt(100) < 30; // 30% failure rate
            
            if (injectFailure) {
                expectedFailures++;
                // Inject various types of failures
                int failureType = random.nextInt(3);
                switch (failureType) {
                    case 0:
                        // Invalid merchant ID
                        paymentData.put("merchant_id", "INVALID_ID");
                        break;
                    case 1:
                        // Invalid email format
                        paymentData.put("email", "invalid-email");
                        break;
                    case 2:
                        // Invalid amount
                        paymentData.put("payment_amount", "-1000");
                        break;
                }
            }
            
            try {
                Response response = given()
                    .spec(requestSpec)
                    .body(paymentData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                    
                if (response.getStatusCode() == 200 && !injectFailure) {
                    successCount++;
                }
                
                // Verify that injected failures are properly handled
                if (injectFailure) {
                    Assert.assertTrue(response.getStatusCode() != 200, 
                        "Injected failure should not result in success");
                }
                
            } catch (Exception e) {
                if (!injectFailure) {
                    System.err.println("Unexpected failure in attempt " + i + ": " + e.getMessage());
                }
            }
            
            // Random delay between attempts
            try {
                Thread.sleep(random.nextInt(200) + 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        int validAttempts = totalAttempts - expectedFailures;
        double successRate = validAttempts > 0 ? (double) successCount / validAttempts * 100 : 0;
        
        // Valid requests should have high success rate
        Assert.assertTrue(successRate >= 80.0, 
            "Expected at least 80% success rate for valid requests, but got: " + successRate + "%");
        
        System.out.println("📊 Random failure injection results:");
        System.out.println("   Total attempts: " + totalAttempts);
        System.out.println("   Injected failures: " + expectedFailures);
        System.out.println("   Valid attempts: " + validAttempts);
        System.out.println("   Success rate: " + successRate + "%");
        System.out.println("✅ Random failure injection test completed");
    }

    /**
     * CE-005: Circuit Breaker Testing
     * Tests circuit breaker pattern implementation
     */
    @Test(groups = {"chaos", "circuit-breaker", "resilience"}, 
          description = "Test circuit breaker pattern behavior")
    public void testCircuitBreakerPattern() {
        System.out.println("🌪️ Testing circuit breaker pattern...");
        
        // First, trigger multiple failures to potentially open circuit breaker
        int failureAttempts = 5;
        for (int i = 0; i < failureAttempts; i++) {
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("merchant_id", "INVALID_MERCHANT_ID");
            invalidData.put("user_ip", "127.0.0.1");
            invalidData.put("merchant_oid", "CE005_FAIL_" + i + "_" + System.currentTimeMillis());
            invalidData.put("email", "circuit.test@example.com");
            invalidData.put("payment_amount", "10000");
            invalidData.put("currency", "TL");
            invalidData.put("test_mode", "1");
            
            try {
                Response response = given()
                    .spec(requestSpec)
                    .body(invalidData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                    
                // Should fail due to invalid merchant ID
                Assert.assertNotEquals(response.getStatusCode(), 200);
                
                Thread.sleep(100); // Small delay between failures
                
            } catch (Exception e) {
                // Expected for circuit breaker testing
            }
        }
        
        // Wait for potential circuit breaker cool-down
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Now test with valid data to see if circuit breaker allows requests
        Map<String, Object> validData = new HashMap<>();
        validData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
        validData.put("user_ip", "127.0.0.1");
        validData.put("merchant_oid", "CE005_VALID_" + System.currentTimeMillis());
        validData.put("email", "circuit.recovery@example.com");
        validData.put("payment_amount", "10000");
        validData.put("currency", "TL");
        validData.put("test_mode", "1");
        
        Response recoveryResponse = given()
            .spec(requestSpec)
            .body(validData)
            .when()
            .post("/odeme/api/get-token")
            .then()
            .extract().response();
            
        // System should recover and process valid requests
        // (Circuit breaker should allow valid requests through)
        Assert.assertTrue(recoveryResponse.getStatusCode() == 200 || 
                         recoveryResponse.getStatusCode() == 503, // Service unavailable if circuit open
            "Circuit breaker should either allow valid requests or return 503");
        
        System.out.println("📊 Circuit breaker recovery response: " + recoveryResponse.getStatusCode());
        System.out.println("✅ Circuit breaker pattern test completed");
    }
}