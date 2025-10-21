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
 * PayTR Fraud Detection Test Senaryoları
 * Gerçek zamanlı fraud detection ve risk scoring testlerini içerir
 */
public class PayTRFraudDetectionTests extends BaseTest {

    @Test(groups = {"fraud", "realtime", "critical"}, 
          priority = 1,
          description = "FD-001: Real-time Fraud Detection Testi")
    public void testRealTimeFraudDetection() {
        logTestInfo("FD-001: Real-time Fraud Detection Test");
        
        // Test Data - Suspicious transaction patterns
        String suspiciousCard = "4000000000000119"; // High-risk test card
        
        // Step 1: Normal transaction to establish baseline
        Response baselineResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 50.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"normal@example.com\",\n" +
                      "    \"phone\": \"+905551234567\",\n" +
                      "    \"ipAddress\": \"192.168.1.100\"\n" +
                      "  },\n" +
                      "  \"deviceInfo\": {\n" +
                      "    \"fingerprint\": \"normal_device_123\",\n" +
                      "    \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64)\"\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String baselineRiskScore = baselineResponse.jsonPath().getString("riskScore");
        String baselineDecision = baselineResponse.jsonPath().getString("decision");
        
        Assert.assertNotNull(baselineRiskScore, "Risk score should be provided");
        Assert.assertEquals(baselineDecision, "APPROVE", "Normal transaction should be approved");
        
        // Step 2: Suspicious transaction with high-risk indicators
        Response fraudResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 5000.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + suspiciousCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"suspicious@tempmail.com\",\n" +
                      "    \"phone\": \"+1234567890\",\n" +
                      "    \"ipAddress\": \"10.0.0.1\"\n" +
                      "  },\n" +
                      "  \"deviceInfo\": {\n" +
                      "    \"fingerprint\": \"suspicious_device_999\",\n" +
                      "    \"userAgent\": \"Bot/1.0\"\n" +
                      "  },\n" +
                      "  \"transactionContext\": {\n" +
                      "    \"timeOfDay\": \"03:00\",\n" +
                      "    \"dayOfWeek\": \"Sunday\",\n" +
                      "    \"location\": \"Unknown\",\n" +
                      "    \"velocityCheck\": true\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String fraudRiskScore = fraudResponse.jsonPath().getString("riskScore");
        String fraudDecision = fraudResponse.jsonPath().getString("decision");
        String riskFactors = fraudResponse.jsonPath().getString("riskFactors");
        String recommendedAction = fraudResponse.jsonPath().getString("recommendedAction");
        
        Assert.assertNotNull(fraudRiskScore, "Risk score should be provided");
        Assert.assertTrue(fraudDecision.equals("DECLINE") || fraudDecision.equals("REVIEW"), 
                         "Suspicious transaction should be declined or flagged for review");
        Assert.assertNotNull(riskFactors, "Risk factors should be identified");
        Assert.assertNotNull(recommendedAction, "Recommended action should be provided");
        
        // Verify risk score is higher for suspicious transaction
        double baselineScore = Double.parseDouble(baselineRiskScore);
        double fraudScore = Double.parseDouble(fraudRiskScore);
        Assert.assertTrue(fraudScore > baselineScore, "Fraud risk score should be higher than baseline");
        
        System.out.println("Baseline Risk Score: " + baselineRiskScore + " (Decision: " + baselineDecision + ")");
        System.out.println("Fraud Risk Score: " + fraudRiskScore + " (Decision: " + fraudDecision + ")");
        System.out.println("Risk Factors: " + riskFactors);
        System.out.println("Recommended Action: " + recommendedAction);
    }

    @Test(groups = {"fraud", "velocity", "high"}, 
          priority = 2,
          description = "FD-002: Velocity Check ve Rate Limiting Testi")
    public void testVelocityCheckAndRateLimiting() {
        logTestInfo("FD-002: Velocity Check and Rate Limiting Test");
        
        String testCard = "4111111111111111";
        String customerEmail = "velocity.test@example.com";
        
        // Step 1: Perform multiple rapid transactions
        int transactionCount = 5;
        String lastTransactionId = null;
        
        for (int i = 1; i <= transactionCount; i++) {
            Response response = RestAssured
                .given()
                    .header("Content-Type", "application/json")
                    .body("{\n" +
                          "  \"amount\": " + (100.00 * i) + ",\n" +
                          "  \"currency\": \"TRY\",\n" +
                          "  \"cardNumber\": \"" + testCard + "\",\n" +
                          "  \"expiryMonth\": \"12\",\n" +
                          "  \"expiryYear\": \"25\",\n" +
                          "  \"cvv\": \"123\",\n" +
                          "  \"customerInfo\": {\n" +
                          "    \"email\": \"" + customerEmail + "\",\n" +
                          "    \"phone\": \"+905551234567\"\n" +
                          "  },\n" +
                          "  \"velocityCheck\": true\n" +
                          "}")
                .when()
                    .post("/api/payment/fraud-check")
                .then()
                    .statusCode(200)
                    .extract().response();
            
            String decision = response.jsonPath().getString("decision");
            String velocityScore = response.jsonPath().getString("velocityScore");
            String transactionId = response.jsonPath().getString("transactionId");
            
            System.out.println("Transaction " + i + " - Decision: " + decision + 
                             ", Velocity Score: " + velocityScore);
            
            if (i <= 3) {
                Assert.assertEquals(decision, "APPROVE", 
                    "First few transactions should be approved");
            } else {
                Assert.assertTrue(decision.equals("DECLINE") || decision.equals("REVIEW"), 
                    "Later transactions should be flagged due to velocity");
            }
            
            lastTransactionId = transactionId;
            
            // Small delay between transactions
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Step 2: Check velocity analysis
        Response velocityAnalysis = RestAssured
            .given()
                .header("Content-Type", "application/json")
            .when()
                .get("/api/fraud/velocity-analysis?email=" + customerEmail + "&timeWindow=300")
            .then()
                .statusCode(200)
                .extract().response();
        
        String totalTransactions = velocityAnalysis.jsonPath().getString("totalTransactions");
        String totalAmount = velocityAnalysis.jsonPath().getString("totalAmount");
        String riskLevel = velocityAnalysis.jsonPath().getString("riskLevel");
        
        Assert.assertEquals(totalTransactions, String.valueOf(transactionCount), 
            "Should track all transactions");
        Assert.assertNotNull(totalAmount, "Total amount should be calculated");
        Assert.assertTrue(riskLevel.equals("HIGH") || riskLevel.equals("MEDIUM"), 
            "Risk level should be elevated due to velocity");
        
        System.out.println("Velocity Analysis - Transactions: " + totalTransactions + 
                          ", Amount: " + totalAmount + ", Risk: " + riskLevel);
    }

    @Test(groups = {"fraud", "geolocation", "medium"}, 
          priority = 3,
          description = "FD-003: Geolocation Anomaly Detection Testi")
    public void testGeolocationAnomalyDetection() {
        logTestInfo("FD-003: Geolocation Anomaly Detection Test");
        
        String testCard = "4111111111111111";
        String customerEmail = "geo.test@example.com";
        
        // Step 1: Establish normal location pattern (Istanbul)
        Response normalResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + testCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"" + customerEmail + "\",\n" +
                      "    \"phone\": \"+905551234567\"\n" +
                      "  },\n" +
                      "  \"locationInfo\": {\n" +
                      "    \"ipAddress\": \"85.105.123.45\",\n" +
                      "    \"country\": \"TR\",\n" +
                      "    \"city\": \"Istanbul\",\n" +
                      "    \"latitude\": 41.0082,\n" +
                      "    \"longitude\": 28.9784,\n" +
                      "    \"timezone\": \"Europe/Istanbul\"\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String normalDecision = normalResponse.jsonPath().getString("decision");
        String normalGeoRisk = normalResponse.jsonPath().getString("geolocationRisk");
        
        Assert.assertEquals(normalDecision, "APPROVE", "Normal location should be approved");
        Assert.assertEquals(normalGeoRisk, "LOW", "Geolocation risk should be low");
        
        // Wait to establish pattern
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Step 2: Transaction from suspicious location (different continent)
        Response anomalyResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 500.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + testCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"" + customerEmail + "\",\n" +
                      "    \"phone\": \"+905551234567\"\n" +
                      "  },\n" +
                      "  \"locationInfo\": {\n" +
                      "    \"ipAddress\": \"198.51.100.1\",\n" +
                      "    \"country\": \"US\",\n" +
                      "    \"city\": \"New York\",\n" +
                      "    \"latitude\": 40.7128,\n" +
                      "    \"longitude\": -74.0060,\n" +
                      "    \"timezone\": \"America/New_York\"\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String anomalyDecision = anomalyResponse.jsonPath().getString("decision");
        String anomalyGeoRisk = anomalyResponse.jsonPath().getString("geolocationRisk");
        String distanceKm = anomalyResponse.jsonPath().getString("distanceFromLastTransaction");
        String travelTimeHours = anomalyResponse.jsonPath().getString("impossibleTravelTime");
        
        Assert.assertTrue(anomalyDecision.equals("DECLINE") || anomalyDecision.equals("REVIEW"), 
            "Geolocation anomaly should trigger review or decline");
        Assert.assertTrue(anomalyGeoRisk.equals("HIGH") || anomalyGeoRisk.equals("MEDIUM"), 
            "Geolocation risk should be elevated");
        Assert.assertNotNull(distanceKm, "Distance should be calculated");
        Assert.assertNotNull(travelTimeHours, "Travel time should be calculated");
        
        System.out.println("Normal Location - Decision: " + normalDecision + ", Geo Risk: " + normalGeoRisk);
        System.out.println("Anomaly Location - Decision: " + anomalyDecision + ", Geo Risk: " + anomalyGeoRisk);
        System.out.println("Distance: " + distanceKm + " km, Travel Time: " + travelTimeHours + " hours");
    }

    @Test(groups = {"fraud", "device", "medium"}, 
          priority = 4,
          description = "FD-004: Device Fingerprinting ve Anomaly Detection Testi")
    public void testDeviceFingerprintingAnomalyDetection() {
        logTestInfo("FD-004: Device Fingerprinting and Anomaly Detection Test");
        
        String testCard = "4111111111111111";
        String customerEmail = "device.test@example.com";
        
        // Step 1: Establish trusted device pattern
        Response trustedDeviceResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + testCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"" + customerEmail + "\"\n" +
                      "  },\n" +
                      "  \"deviceInfo\": {\n" +
                      "    \"fingerprint\": \"trusted_device_12345\",\n" +
                      "    \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\",\n" +
                      "    \"screenResolution\": \"1920x1080\",\n" +
                      "    \"colorDepth\": 24,\n" +
                      "    \"timezone\": \"Europe/Istanbul\",\n" +
                      "    \"language\": \"tr-TR\",\n" +
                      "    \"platform\": \"Win32\",\n" +
                      "    \"cookiesEnabled\": true,\n" +
                      "    \"javaEnabled\": false\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String trustedDecision = trustedDeviceResponse.jsonPath().getString("decision");
        String trustedDeviceRisk = trustedDeviceResponse.jsonPath().getString("deviceRisk");
        
        Assert.assertEquals(trustedDecision, "APPROVE", "Trusted device should be approved");
        Assert.assertEquals(trustedDeviceRisk, "LOW", "Device risk should be low");
        
        // Step 2: Transaction from suspicious/new device
        Response suspiciousDeviceResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 300.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + testCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"" + customerEmail + "\"\n" +
                      "  },\n" +
                      "  \"deviceInfo\": {\n" +
                      "    \"fingerprint\": \"suspicious_device_99999\",\n" +
                      "    \"userAgent\": \"Bot/1.0 (Suspicious)\",\n" +
                      "    \"screenResolution\": \"800x600\",\n" +
                      "    \"colorDepth\": 8,\n" +
                      "    \"timezone\": \"UTC\",\n" +
                      "    \"language\": \"en-US\",\n" +
                      "    \"platform\": \"Unknown\",\n" +
                      "    \"cookiesEnabled\": false,\n" +
                      "    \"javaEnabled\": true\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String suspiciousDecision = suspiciousDeviceResponse.jsonPath().getString("decision");
        String suspiciousDeviceRisk = suspiciousDeviceResponse.jsonPath().getString("deviceRisk");
        String deviceAnomalies = suspiciousDeviceResponse.jsonPath().getString("deviceAnomalies");
        
        Assert.assertTrue(suspiciousDecision.equals("DECLINE") || suspiciousDecision.equals("REVIEW"), 
            "Suspicious device should trigger review or decline");
        Assert.assertTrue(suspiciousDeviceRisk.equals("HIGH") || suspiciousDeviceRisk.equals("MEDIUM"), 
            "Device risk should be elevated");
        Assert.assertNotNull(deviceAnomalies, "Device anomalies should be identified");
        
        System.out.println("Trusted Device - Decision: " + trustedDecision + ", Risk: " + trustedDeviceRisk);
        System.out.println("Suspicious Device - Decision: " + suspiciousDecision + ", Risk: " + suspiciousDeviceRisk);
        System.out.println("Device Anomalies: " + deviceAnomalies);
    }

    @Test(groups = {"fraud", "behavioral", "medium"}, 
          priority = 5,
          description = "FD-005: Behavioral Analysis ve Pattern Recognition Testi")
    public void testBehavioralAnalysisPatternRecognition() {
        logTestInfo("FD-005: Behavioral Analysis and Pattern Recognition Test");
        
        String testCard = "4111111111111111";
        String customerEmail = "behavioral.test@example.com";
        
        // Step 1: Establish normal behavioral pattern
        String[] normalAmounts = {"25.50", "45.00", "67.25", "89.99"};
        String[] normalTimes = {"14:30", "16:45", "19:20", "21:15"};
        
        for (int i = 0; i < normalAmounts.length; i++) {
            Response response = RestAssured
                .given()
                    .header("Content-Type", "application/json")
                    .body("{\n" +
                          "  \"amount\": " + normalAmounts[i] + ",\n" +
                          "  \"currency\": \"TRY\",\n" +
                          "  \"cardNumber\": \"" + testCard + "\",\n" +
                          "  \"expiryMonth\": \"12\",\n" +
                          "  \"expiryYear\": \"25\",\n" +
                          "  \"cvv\": \"123\",\n" +
                          "  \"customerInfo\": {\n" +
                          "    \"email\": \"" + customerEmail + "\"\n" +
                          "  },\n" +
                          "  \"transactionContext\": {\n" +
                          "    \"timeOfDay\": \"" + normalTimes[i] + "\",\n" +
                          "    \"merchantCategory\": \"retail\",\n" +
                          "    \"paymentMethod\": \"card\"\n" +
                          "  }\n" +
                          "}")
                .when()
                    .post("/api/payment/fraud-check")
                .then()
                    .statusCode(200)
                    .extract().response();
            
            String decision = response.jsonPath().getString("decision");
            Assert.assertEquals(decision, "APPROVE", "Normal pattern should be approved");
            
            // Small delay between transactions
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Step 2: Anomalous behavioral pattern
        Response anomalousResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": 2500.00,\n" +
                      "  \"currency\": \"TRY\",\n" +
                      "  \"cardNumber\": \"" + testCard + "\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\",\n" +
                      "  \"customerInfo\": {\n" +
                      "    \"email\": \"" + customerEmail + "\"\n" +
                      "  },\n" +
                      "  \"transactionContext\": {\n" +
                      "    \"timeOfDay\": \"03:15\",\n" +
                      "    \"merchantCategory\": \"gambling\",\n" +
                      "    \"paymentMethod\": \"card\"\n" +
                      "  }\n" +
                      "}")
            .when()
                .post("/api/payment/fraud-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String anomalousDecision = anomalousResponse.jsonPath().getString("decision");
        String behavioralScore = anomalousResponse.jsonPath().getString("behavioralScore");
        String patternAnomalies = anomalousResponse.jsonPath().getString("patternAnomalies");
        
        Assert.assertTrue(anomalousDecision.equals("DECLINE") || anomalousDecision.equals("REVIEW"), 
            "Anomalous behavior should trigger review or decline");
        Assert.assertNotNull(behavioralScore, "Behavioral score should be provided");
        Assert.assertNotNull(patternAnomalies, "Pattern anomalies should be identified");
        
        // Step 3: Get behavioral analysis report
        Response behavioralReport = RestAssured
            .given()
                .header("Content-Type", "application/json")
            .when()
                .get("/api/fraud/behavioral-analysis?email=" + customerEmail)
            .then()
                .statusCode(200)
                .extract().response();
        
        String avgTransactionAmount = behavioralReport.jsonPath().getString("averageTransactionAmount");
        String preferredTimeRange = behavioralReport.jsonPath().getString("preferredTimeRange");
        String riskProfile = behavioralReport.jsonPath().getString("riskProfile");
        
        Assert.assertNotNull(avgTransactionAmount, "Average transaction amount should be calculated");
        Assert.assertNotNull(preferredTimeRange, "Preferred time range should be identified");
        Assert.assertNotNull(riskProfile, "Risk profile should be determined");
        
        System.out.println("Anomalous Decision: " + anomalousDecision + ", Behavioral Score: " + behavioralScore);
        System.out.println("Pattern Anomalies: " + patternAnomalies);
        System.out.println("Behavioral Profile - Avg Amount: " + avgTransactionAmount + 
                          ", Time Range: " + preferredTimeRange + ", Risk: " + riskProfile);
    }
}