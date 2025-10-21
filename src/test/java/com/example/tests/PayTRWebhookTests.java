package com.example.tests;

import com.example.utils.PayTRTestDataProvider;
import com.example.utils.TestUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.concurrent.TimeUnit;

/**
 * PayTR Webhook Test Senaryoları
 * Webhook delivery, retry mechanism ve security testlerini içerir
 */
public class PayTRWebhookTests extends BaseTest {

    private static final String WEBHOOK_ENDPOINT = "https://webhook.site/test-endpoint";
    private static final String WEBHOOK_SECRET = "test_webhook_secret_key_123";

    @Test(groups = {"webhook", "delivery", "critical"}, 
          priority = 1,
          description = "WH-001: Webhook Delivery ve Retry Mechanism Testi")
    public void testWebhookDeliveryAndRetryMechanism() {
        logTestInfo("WH-001: Webhook Delivery and Retry Mechanism Test");
        
        // Step 1: Configure webhook endpoint
        Response configResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookUrl\": \"" + WEBHOOK_ENDPOINT + "\",\n" +
                      "  \"events\": [\"payment.success\", \"payment.failed\", \"payment.refund\"],\n" +
                      "  \"secret\": \"" + WEBHOOK_SECRET + "\",\n" +
                      "  \"retryPolicy\": {\n" +
                      "    \"maxRetries\": 3,\n" +
                      "    \"retryIntervals\": [30, 60, 120]\n" +
                      "  },\n" +
                      "  \"timeout\": 30\n" +
                      "}")
            .when()
                .post("/api/webhook/configure")
            .then()
                .statusCode(200)
                .extract().response();
        
        String webhookId = configResponse.jsonPath().getString("webhookId");
        String status = configResponse.jsonPath().getString("status");
        
        Assert.assertNotNull(webhookId, "Webhook ID should be provided");
        Assert.assertEquals(status, "ACTIVE", "Webhook should be active");
        
        // Step 2: Trigger a payment to generate webhook
        Response paymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"webhookId\": \"" + webhookId + "\"\n" +
                      "}")
            .when()
                .post("/api/payment/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String transactionId = paymentResponse.jsonPath().getString("transactionId");
        String paymentStatus = paymentResponse.jsonPath().getString("status");
        
        Assert.assertEquals(paymentStatus, "SUCCESS", "Payment should be successful");
        
        // Step 3: Wait for webhook delivery
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Step 4: Check webhook delivery status
        Response webhookStatusResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/delivery-status/" + transactionId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String deliveryStatus = webhookStatusResponse.jsonPath().getString("deliveryStatus");
        String deliveryAttempts = webhookStatusResponse.jsonPath().getString("deliveryAttempts");
        String lastAttemptTime = webhookStatusResponse.jsonPath().getString("lastAttemptTime");
        String responseCode = webhookStatusResponse.jsonPath().getString("responseCode");
        
        Assert.assertNotNull(deliveryStatus, "Delivery status should be provided");
        Assert.assertNotNull(deliveryAttempts, "Delivery attempts should be tracked");
        Assert.assertNotNull(lastAttemptTime, "Last attempt time should be recorded");
        
        System.out.println("Webhook ID: " + webhookId);
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Delivery Status: " + deliveryStatus);
        System.out.println("Delivery Attempts: " + deliveryAttempts);
        System.out.println("Response Code: " + responseCode);
        
        // Step 5: Test retry mechanism with failed endpoint
        Response failedWebhookResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookUrl\": \"https://invalid-endpoint.example.com/webhook\",\n" +
                      "  \"events\": [\"payment.success\"],\n" +
                      "  \"secret\": \"" + WEBHOOK_SECRET + "\",\n" +
                      "  \"retryPolicy\": {\n" +
                      "    \"maxRetries\": 2,\n" +
                      "    \"retryIntervals\": [5, 10]\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/webhook/configure")
            .then()
                .statusCode(200)
                .extract().response();
        
        String failedWebhookId = failedWebhookResponse.jsonPath().getString("webhookId");
        
        // Trigger payment for failed webhook
        Response failedPaymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 50.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"webhookId\": \"" + failedWebhookId + "\"\n" +
                      "}")
            .when()
                .post("/api/payment/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String failedTransactionId = failedPaymentResponse.jsonPath().getString("transactionId");
        
        // Wait for retry attempts
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check retry status
        Response retryStatusResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/delivery-status/" + failedTransactionId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String retryDeliveryStatus = retryStatusResponse.jsonPath().getString("deliveryStatus");
        String retryAttempts = retryStatusResponse.jsonPath().getString("deliveryAttempts");
        
        Assert.assertEquals(retryDeliveryStatus, "FAILED", "Delivery should fail after retries");
        Assert.assertEquals(retryAttempts, "3", "Should attempt 3 times (initial + 2 retries)");
        
        System.out.println("Failed Webhook - Status: " + retryDeliveryStatus + ", Attempts: " + retryAttempts);
    }

    @Test(groups = {"webhook", "security", "critical"}, 
          priority = 2,
          description = "WH-002: Webhook Security ve Signature Verification Testi")
    public void testWebhookSecurityAndSignatureVerification() {
        logTestInfo("WH-002: Webhook Security and Signature Verification Test");
        
        // Step 1: Configure webhook with security settings
        Response secureWebhookResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookUrl\": \"" + WEBHOOK_ENDPOINT + "\",\n" +
                      "  \"events\": [\"payment.success\", \"payment.failed\"],\n" +
                      "  \"secret\": \"" + WEBHOOK_SECRET + "\",\n" +
                      "  \"signatureAlgorithm\": \"HMAC-SHA256\",\n" +
                      "  \"includeHeaders\": {\n" +
                      "    \"X-PayTR-Signature\": true,\n" +
                      "    \"X-PayTR-Timestamp\": true,\n" +
                      "    \"X-PayTR-Event\": true\n" +
                      "  },\n" +
                      "  \"ipWhitelist\": [\"192.168.1.0/24\", \"10.0.0.0/8\"]\n" +
                      "}")
            .when()
                .post("/api/webhook/configure")
            .then()
                .statusCode(200)
                .extract().response();
        
        String secureWebhookId = secureWebhookResponse.jsonPath().getString("webhookId");
        String securityStatus = secureWebhookResponse.jsonPath().getString("securityStatus");
        
        Assert.assertEquals(securityStatus, "ENABLED", "Security should be enabled");
        
        // Step 2: Test webhook signature generation
        Response signatureTestResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookId\": \"" + secureWebhookId + "\",\n" +
                      "  \"payload\": {\n" +
                      "    \"event\": \"payment.success\",\n" +
                      "    \"transactionId\": \"test_transaction_123\",\n" +
                      "    \"amount\": 100.00,\n" +
                      "    \"currency\": \"TRY\",\n" +
                      "    \"timestamp\": \"2024-01-15T10:30:00Z\"\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/webhook/generate-signature")
            .then()
                .statusCode(200)
                .extract().response();
        
        String generatedSignature = signatureTestResponse.jsonPath().getString("signature");
        String algorithm = signatureTestResponse.jsonPath().getString("algorithm");
        String timestamp = signatureTestResponse.jsonPath().getString("timestamp");
        
        Assert.assertNotNull(generatedSignature, "Signature should be generated");
        Assert.assertEquals(algorithm, "HMAC-SHA256", "Algorithm should be HMAC-SHA256");
        Assert.assertNotNull(timestamp, "Timestamp should be included");
        
        // Step 3: Test signature verification
        Response verificationResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookId\": \"" + secureWebhookId + "\",\n" +
                      "  \"payload\": {\n" +
                      "    \"event\": \"payment.success\",\n" +
                      "    \"transactionId\": \"test_transaction_123\",\n" +
                      "    \"amount\": 100.00,\n" +
                      "    \"currency\": \"TRY\",\n" +
                      "    \"timestamp\": \"" + timestamp + "\"\n" +
                      "  },\n" +
                      "  \"signature\": \"" + generatedSignature + "\"\n" +
                      "}")
            .when()
                .post("/api/webhook/verify-signature")
            .then()
                .statusCode(200)
                .extract().response();
        
        String verificationResult = verificationResponse.jsonPath().getString("verified");
        String verificationMessage = verificationResponse.jsonPath().getString("message");
        
        Assert.assertEquals(verificationResult, "true", "Signature should be verified successfully");
        Assert.assertNotNull(verificationMessage, "Verification message should be provided");
        
        // Step 4: Test invalid signature
        Response invalidSignatureResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookId\": \"" + secureWebhookId + "\",\n" +
                      "  \"payload\": {\n" +
                      "    \"event\": \"payment.success\",\n" +
                      "    \"transactionId\": \"test_transaction_123\",\n" +
                      "    \"amount\": 100.00,\n" +
                      "    \"currency\": \"TRY\",\n" +
                      "    \"timestamp\": \"" + timestamp + "\"\n" +
                      "  },\n" +
                      "  \"signature\": \"invalid_signature_12345\"\n" +
                      "}")
            .when()
                .post("/api/webhook/verify-signature")
            .then()
                .statusCode(200)
                .extract().response();
        
        String invalidVerificationResult = invalidSignatureResponse.jsonPath().getString("verified");
        String invalidVerificationMessage = invalidSignatureResponse.jsonPath().getString("message");
        
        Assert.assertEquals(invalidVerificationResult, "false", "Invalid signature should fail verification");
        Assert.assertNotNull(invalidVerificationMessage, "Error message should be provided");
        
        System.out.println("Secure Webhook ID: " + secureWebhookId);
        System.out.println("Generated Signature: " + generatedSignature);
        System.out.println("Verification Result: " + verificationResult);
        System.out.println("Invalid Verification: " + invalidVerificationResult);
    }

    @Test(groups = {"webhook", "events", "medium"}, 
          priority = 3,
          description = "WH-003: Webhook Event Types ve Filtering Testi")
    public void testWebhookEventTypesAndFiltering() {
        logTestInfo("WH-003: Webhook Event Types and Filtering Test");
        
        // Step 1: Configure webhook for specific events
        Response eventWebhookResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookUrl\": \"" + WEBHOOK_ENDPOINT + "\",\n" +
                      "  \"events\": [\"payment.success\", \"payment.refund\"],\n" +
                      "  \"eventFilters\": {\n" +
                      "    \"minAmount\": 50.00,\n" +
                      "    \"currencies\": [\"TRY\", \"USD\"],\n" +
                      "    \"merchantCategories\": [\"retail\", \"ecommerce\"]\n" +
                      "  },\n" +
                      "  \"secret\": \"" + WEBHOOK_SECRET + "\"\n" +
                      "}")
            .when()
                .post("/api/webhook/configure")
            .then()
                .statusCode(200)
                .extract().response();
        
        String eventWebhookId = eventWebhookResponse.jsonPath().getString("webhookId");
        
        // Step 2: Test payment.success event (should trigger webhook)
        Response successPaymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"webhookId\": \"" + eventWebhookId + "\",\n" +
                      "  \"merchantCategory\": \"retail\"\n" +
                      "}")
            .when()
                .post("/api/payment/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String successTransactionId = successPaymentResponse.jsonPath().getString("transactionId");
        
        // Step 3: Test payment below minimum amount (should not trigger webhook)
        Response lowAmountPaymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 25.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"webhookId\": \"" + eventWebhookId + "\",\n" +
                      "  \"merchantCategory\": \"retail\"\n" +
                      "}")
            .when()
                .post("/api/payment/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String lowAmountTransactionId = lowAmountPaymentResponse.jsonPath().getString("transactionId");
        
        // Step 4: Test payment.failed event (should not trigger webhook - not in events list)
        Response failedPaymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4000000000000002\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"webhookId\": \"" + eventWebhookId + "\",\n" +
                      "  \"merchantCategory\": \"retail\"\n" +
                      "}")
            .when()
                .post("/api/payment/process")
            .then()
                .statusCode(400)
                .extract().response();
        
        // Wait for webhook processing
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Step 5: Check webhook delivery for success payment
        Response successWebhookStatus = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/delivery-status/" + successTransactionId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String successDeliveryStatus = successWebhookStatus.jsonPath().getString("deliveryStatus");
        String successEventType = successWebhookStatus.jsonPath().getString("eventType");
        
        Assert.assertNotNull(successDeliveryStatus, "Webhook should be delivered for success payment");
        Assert.assertEquals(successEventType, "payment.success", "Event type should be payment.success");
        
        // Step 6: Check webhook delivery for low amount payment (should not be delivered)
        Response lowAmountWebhookStatus = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/delivery-status/" + lowAmountTransactionId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String lowAmountDeliveryStatus = lowAmountWebhookStatus.jsonPath().getString("deliveryStatus");
        String filterReason = lowAmountWebhookStatus.jsonPath().getString("filterReason");
        
        Assert.assertEquals(lowAmountDeliveryStatus, "FILTERED", "Webhook should be filtered for low amount");
        Assert.assertNotNull(filterReason, "Filter reason should be provided");
        
        System.out.println("Success Payment Webhook - Status: " + successDeliveryStatus + ", Event: " + successEventType);
        System.out.println("Low Amount Payment Webhook - Status: " + lowAmountDeliveryStatus + ", Filter Reason: " + filterReason);
    }

    @Test(groups = {"webhook", "monitoring", "medium"}, 
          priority = 4,
          description = "WH-004: Webhook Monitoring ve Analytics Testi")
    public void testWebhookMonitoringAndAnalytics() {
        logTestInfo("WH-004: Webhook Monitoring and Analytics Test");
        
        // Step 1: Get webhook analytics
        Response analyticsResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/analytics?timeRange=24h")
            .then()
                .statusCode(200)
                .extract().response();
        
        String totalWebhooks = analyticsResponse.jsonPath().getString("totalWebhooks");
        String successfulDeliveries = analyticsResponse.jsonPath().getString("successfulDeliveries");
        String failedDeliveries = analyticsResponse.jsonPath().getString("failedDeliveries");
        String averageResponseTime = analyticsResponse.jsonPath().getString("averageResponseTime");
        String successRate = analyticsResponse.jsonPath().getString("successRate");
        
        Assert.assertNotNull(totalWebhooks, "Total webhooks should be provided");
        Assert.assertNotNull(successfulDeliveries, "Successful deliveries should be tracked");
        Assert.assertNotNull(failedDeliveries, "Failed deliveries should be tracked");
        Assert.assertNotNull(averageResponseTime, "Average response time should be calculated");
        Assert.assertNotNull(successRate, "Success rate should be calculated");
        
        // Step 2: Get webhook health status
        Response healthResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
            .when()
                .get("/api/webhook/health")
            .then()
                .statusCode(200)
                .extract().response();
        
        String overallHealth = healthResponse.jsonPath().getString("overallHealth");
        String activeWebhooks = healthResponse.jsonPath().getString("activeWebhooks");
        String unhealthyEndpoints = healthResponse.jsonPath().getString("unhealthyEndpoints");
        
        Assert.assertNotNull(overallHealth, "Overall health should be provided");
        Assert.assertNotNull(activeWebhooks, "Active webhooks count should be provided");
        
        // Step 3: Test webhook endpoint health check
        Response endpointHealthResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                      "  \"webhookUrl\": \"" + WEBHOOK_ENDPOINT + "\"\n" +
                      "}")
            .when()
                .post("/api/webhook/health-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String endpointStatus = endpointHealthResponse.jsonPath().getString("status");
        String responseTime = endpointHealthResponse.jsonPath().getString("responseTime");
        String lastChecked = endpointHealthResponse.jsonPath().getString("lastChecked");
        
        Assert.assertNotNull(endpointStatus, "Endpoint status should be provided");
        Assert.assertNotNull(responseTime, "Response time should be measured");
        Assert.assertNotNull(lastChecked, "Last checked time should be recorded");
        
        System.out.println("Webhook Analytics:");
        System.out.println("  Total: " + totalWebhooks + ", Success: " + successfulDeliveries + 
                          ", Failed: " + failedDeliveries);
        System.out.println("  Success Rate: " + successRate + "%, Avg Response Time: " + averageResponseTime + "ms");
        System.out.println("Health Status:");
        System.out.println("  Overall: " + overallHealth + ", Active: " + activeWebhooks + 
                          ", Unhealthy: " + unhealthyEndpoints);
        System.out.println("Endpoint Health: " + endpointStatus + " (" + responseTime + "ms)");
    }

    /**
     * Helper method to get authentication token for webhook operations
     */
    private String getAuthToken() {
        // This would typically retrieve a valid auth token
        // For testing purposes, returning a mock token
        return "test_auth_token_12345";
    }
}