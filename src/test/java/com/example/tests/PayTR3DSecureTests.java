package com.example.tests;

import com.example.utils.PayTRTestDataProvider;
import com.example.utils.SecurityTestUtils;
import com.example.utils.TestUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.concurrent.TimeUnit;

/**
 * PayTR 3D Secure Test Senaryoları
 * 3D Secure 2.0 authentication ve fraud detection testlerini içerir
 */
public class PayTR3DSecureTests extends BaseTest {

    @Test(groups = {"3dsecure", "critical", "authentication"}, 
          priority = 1,
          description = "3DS-001: 3D Secure 2.0 Challenge Flow Testi")
    public void test3DSecureChallenge() {
        logTestInfo("3DS-001: 3D Secure 2.0 Challenge Flow Test");
        
        // Test Data - Challenge gerektiren kart
        String challengeCard = "4000000000001091"; // 3DS Challenge test card
        
        // Step 1: Initiate payment with 3DS challenge card
        Response initResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + challengeCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true,\n" +
                      "  \"browserInfo\": {\n" +
                      "    \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64)\",\n" +
                      "    \"acceptHeader\": \"text/html,application/xhtml+xml\",\n" +
                      "    \"language\": \"en-US\",\n" +
                      "    \"colorDepth\": 24,\n" +
                      "    \"screenHeight\": 1080,\n" +
                      "    \"screenWidth\": 1920,\n" +
                      "    \"timeZoneOffset\": -180,\n" +
                      "    \"javaEnabled\": false\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/initiate")
            .then()
                .statusCode(200)
                .extract().response();
        
        String transactionId = initResponse.jsonPath().getString("transactionId");
        String challengeUrl = initResponse.jsonPath().getString("challengeUrl");
        String threeDSVersion = initResponse.jsonPath().getString("threeDSVersion");
        
        Assert.assertNotNull(transactionId, "Transaction ID should be provided");
        Assert.assertNotNull(challengeUrl, "Challenge URL should be provided");
        Assert.assertEquals(threeDSVersion, "2.0", "Should use 3DS version 2.0");
        
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Challenge URL: " + challengeUrl);
        
        // Step 2: Simulate challenge completion
        Response challengeResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"transactionId\": \"" + transactionId + "\",\n" +
                      "  \"challengeResult\": \"Y\",\n" +
                      "  \"authenticationValue\": \"MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=\",\n" +
                      "  \"eci\": \"05\"\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/challenge/complete")
            .then()
                .statusCode(200)
                .extract().response();
        
        String authResult = challengeResponse.jsonPath().getString("authenticationResult");
        String paymentStatus = challengeResponse.jsonPath().getString("paymentStatus");
        
        Assert.assertEquals(authResult, "Y", "Authentication should be successful");
        Assert.assertEquals(paymentStatus, "SUCCESS", "Payment should be successful after 3DS");
        
        System.out.println("Authentication Result: " + authResult);
        System.out.println("Payment Status: " + paymentStatus);
    }

    @Test(groups = {"3dsecure", "frictionless", "high"}, 
          priority = 2,
          description = "3DS-002: 3D Secure Frictionless Flow Testi")
    public void test3DSecureFrictionless() {
        logTestInfo("3DS-002: 3D Secure Frictionless Flow Test");
        
        // Test Data - Low risk transaction for frictionless flow
        String frictionlessCard = "4000000000001000"; // 3DS Frictionless test card
        
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 25.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + frictionlessCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true,\n" +
                      "  \"riskData\": {\n" +
                      "    \"customerEmail\": \"trusted@example.com\",\n" +
                      "    \"customerPhone\": \"+905551234567\",\n" +
                      "    \"billingAddress\": {\n" +
                      "      \"country\": \"TR\",\n" +
                      "      \"city\": \"Istanbul\"\n" +
                      "    },\n" +
                      "    \"previousTransactions\": 15,\n" +
                      "    \"accountAge\": 365\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String authResult = response.jsonPath().getString("authenticationResult");
        String flow = response.jsonPath().getString("flow");
        String paymentStatus = response.jsonPath().getString("paymentStatus");
        String riskScore = response.jsonPath().getString("riskScore");
        
        Assert.assertEquals(flow, "frictionless", "Should use frictionless flow");
        Assert.assertEquals(authResult, "Y", "Authentication should be successful");
        Assert.assertEquals(paymentStatus, "SUCCESS", "Payment should be successful");
        Assert.assertNotNull(riskScore, "Risk score should be provided");
        
        // Verify low risk score for frictionless flow
        double risk = Double.parseDouble(riskScore);
        Assert.assertTrue(risk < 30.0, "Risk score should be low for frictionless flow");
        
        System.out.println("Flow Type: " + flow);
        System.out.println("Risk Score: " + riskScore);
        System.out.println("Authentication Result: " + authResult);
    }

    @Test(groups = {"3dsecure", "fallback", "medium"}, 
          priority = 3,
          description = "3DS-003: 3D Secure Fallback Mechanism Testi")
    public void test3DSecureFallback() {
        logTestInfo("3DS-003: 3D Secure Fallback Mechanism Test");
        
        // Test Data - Card that doesn't support 3DS 2.0
        String fallbackCard = "4000000000000002"; // 3DS 1.0 fallback card
        
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 150.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + fallbackCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String threeDSVersion = response.jsonPath().getString("threeDSVersion");
        String fallbackReason = response.jsonPath().getString("fallbackReason");
        String authResult = response.jsonPath().getString("authenticationResult");
        
        Assert.assertEquals(threeDSVersion, "1.0", "Should fallback to 3DS 1.0");
        Assert.assertNotNull(fallbackReason, "Fallback reason should be provided");
        Assert.assertNotNull(authResult, "Authentication result should be provided");
        
        System.out.println("3DS Version: " + threeDSVersion);
        System.out.println("Fallback Reason: " + fallbackReason);
        System.out.println("Authentication Result: " + authResult);
    }

    @Test(groups = {"3dsecure", "timeout", "medium"}, 
          priority = 4,
          description = "3DS-004: 3D Secure Timeout Handling Testi")
    public void test3DSecureTimeout() {
        logTestInfo("3DS-004: 3D Secure Timeout Handling Test");
        
        String timeoutCard = "4000000000001109"; // 3DS Timeout test card
        
        Response initResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 200.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + timeoutCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true,\n" +
                      "  \"challengeTimeout\": 30\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/initiate")
            .then()
                .statusCode(200)
                .extract().response();
        
        String transactionId = initResponse.jsonPath().getString("transactionId");
        
        // Wait for timeout to occur
        try {
            TimeUnit.SECONDS.sleep(35);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check transaction status after timeout
        Response statusResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
            .when()
                .get("/api/payment/3ds/status/" + transactionId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String status = statusResponse.jsonPath().getString("status");
        String timeoutReason = statusResponse.jsonPath().getString("timeoutReason");
        
        Assert.assertEquals(status, "TIMEOUT", "Transaction should be marked as timeout");
        Assert.assertNotNull(timeoutReason, "Timeout reason should be provided");
        
        System.out.println("Transaction Status: " + status);
        System.out.println("Timeout Reason: " + timeoutReason);
    }

    @Test(groups = {"3dsecure", "exemption", "medium"}, 
          priority = 5,
          description = "3DS-005: SCA Exemption Handling Testi")
    public void testSCAExemptionHandling() {
        logTestInfo("3DS-005: SCA Exemption Handling Test");
        
        // Test Low Value Exemption (under 30 EUR)
        Response lowValueResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 25.00,\n" +
                      "  \"currency\": \"EUR\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true,\n" +
                      "  \"exemption\": \"low_value\"\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String exemptionApplied = lowValueResponse.jsonPath().getString("exemptionApplied");
        String exemptionType = lowValueResponse.jsonPath().getString("exemptionType");
        String paymentStatus = lowValueResponse.jsonPath().getString("paymentStatus");
        
        Assert.assertEquals(exemptionApplied, "true", "Low value exemption should be applied");
        Assert.assertEquals(exemptionType, "low_value", "Exemption type should be low_value");
        Assert.assertEquals(paymentStatus, "SUCCESS", "Payment should be successful with exemption");
        
        // Test Trusted Merchant Exemption
        Response trustedMerchantResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"EUR\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true,\n" +
                      "  \"exemption\": \"trusted_merchant\",\n" +
                      "  \"merchantWhitelisted\": true\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String trustedExemption = trustedMerchantResponse.jsonPath().getString("exemptionApplied");
        String trustedType = trustedMerchantResponse.jsonPath().getString("exemptionType");
        
        Assert.assertEquals(trustedExemption, "true", "Trusted merchant exemption should be applied");
        Assert.assertEquals(trustedType, "trusted_merchant", "Exemption type should be trusted_merchant");
        
        System.out.println("Low Value Exemption: " + exemptionApplied);
        System.out.println("Trusted Merchant Exemption: " + trustedExemption);
    }

    @Test(groups = {"3dsecure", "liability", "high"}, 
          priority = 6,
          description = "3DS-006: Liability Shift Verification Testi")
    public void testLiabilityShiftVerification() {
        logTestInfo("3DS-006: Liability Shift Verification Test");
        
        // Test successful 3DS with liability shift
        Response successResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 500.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4000000000001000\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String liabilityShift = successResponse.jsonPath().getString("liabilityShift");
        String authResult = successResponse.jsonPath().getString("authenticationResult");
        String eci = successResponse.jsonPath().getString("eci");
        
        Assert.assertEquals(authResult, "Y", "Authentication should be successful");
        Assert.assertEquals(liabilityShift, "YES", "Liability should shift to issuer");
        Assert.assertTrue(eci.equals("05") || eci.equals("02"), "ECI should indicate successful 3DS");
        
        // Test failed 3DS without liability shift
        Response failResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 500.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4000000000001018\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"threeDSecure\": true\n" +
                      "}")
            .when()
                .post("/api/payment/3ds/process")
            .then()
                .statusCode(200)
                .extract().response();
        
        String failLiabilityShift = failResponse.jsonPath().getString("liabilityShift");
        String failAuthResult = failResponse.jsonPath().getString("authenticationResult");
        
        Assert.assertEquals(failAuthResult, "N", "Authentication should fail");
        Assert.assertEquals(failLiabilityShift, "NO", "Liability should remain with merchant");
        
        System.out.println("Successful 3DS - Liability Shift: " + liabilityShift + ", ECI: " + eci);
        System.out.println("Failed 3DS - Liability Shift: " + failLiabilityShift);
    }
}