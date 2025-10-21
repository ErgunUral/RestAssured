package com.example.utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Fraud Detection Utilities
 * Dolandırıcılık tespit işlemleri için yardımcı sınıf
 * 
 * Özellikler:
 * - Risk skoru hesaplama
 * - Velocity kontrolü
 * - Coğrafi anomali tespiti
 * - Cihaz parmak izi analizi
 * - Davranışsal analiz
 */
public class FraudDetectionUtils {

    // Risk score thresholds
    public static final int LOW_RISK_THRESHOLD = 30;
    public static final int MEDIUM_RISK_THRESHOLD = 60;
    public static final int HIGH_RISK_THRESHOLD = 80;
    
    // Velocity limits
    public static final int MAX_TRANSACTIONS_PER_MINUTE = 5;
    public static final int MAX_TRANSACTIONS_PER_HOUR = 50;
    public static final int MAX_TRANSACTIONS_PER_DAY = 200;
    
    // High-risk countries (ISO country codes)
    private static final Set<String> HIGH_RISK_COUNTRIES = new HashSet<>(Arrays.asList(
        "AF", "IQ", "LY", "SO", "SY", "YE", "KP", "IR", "SD"
    ));
    
    // Suspicious IP patterns
    private static final List<Pattern> SUSPICIOUS_IP_PATTERNS = Arrays.asList(
        Pattern.compile("^10\\..*"),           // Private network
        Pattern.compile("^192\\.168\\..*"),    // Private network
        Pattern.compile("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*"), // Private network
        Pattern.compile("^169\\.254\\..*"),    // Link-local
        Pattern.compile("^127\\..*")           // Loopback
    );
    
    // Device fingerprint risk indicators
    private static final Map<String, Integer> DEVICE_RISK_SCORES = new HashMap<>();
    
    static {
        DEVICE_RISK_SCORES.put("TRUSTED_DEVICE", 0);
        DEVICE_RISK_SCORES.put("NEW_DEVICE", 20);
        DEVICE_RISK_SCORES.put("SUSPICIOUS_DEVICE", 50);
        DEVICE_RISK_SCORES.put("BLACKLISTED_DEVICE", 100);
        DEVICE_RISK_SCORES.put("UNKNOWN_DEVICE", 30);
    }

    /**
     * Risk Assessment Result
     */
    public static class RiskAssessment {
        private final int riskScore;
        private final String riskLevel;
        private final String recommendation;
        private final Map<String, Object> details;

        public RiskAssessment(int riskScore, String riskLevel, String recommendation, Map<String, Object> details) {
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.recommendation = recommendation;
            this.details = details;
        }

        // Getters
        public int getRiskScore() { return riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public String getRecommendation() { return recommendation; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * Transaction Context for fraud analysis
     */
    public static class TransactionContext {
        private String userId;
        private String ipAddress;
        private String countryCode;
        private String deviceFingerprint;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime timestamp;
        private Map<String, Object> additionalData;

        public TransactionContext(String userId, String ipAddress, String countryCode, 
                                String deviceFingerprint, BigDecimal amount, String currency) {
            this.userId = userId;
            this.ipAddress = ipAddress;
            this.countryCode = countryCode;
            this.deviceFingerprint = deviceFingerprint;
            this.amount = amount;
            this.currency = currency;
            this.timestamp = LocalDateTime.now();
            this.additionalData = new HashMap<>();
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public String getIpAddress() { return ipAddress; }
        public String getCountryCode() { return countryCode; }
        public String getDeviceFingerprint() { return deviceFingerprint; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getAdditionalData() { return additionalData; }
        
        public void setAdditionalData(String key, Object value) {
            this.additionalData.put(key, value);
        }
    }

    /**
     * Calculates comprehensive risk score for a transaction
     */
    public static RiskAssessment calculateRiskScore(TransactionContext context) {
        Map<String, Object> details = new HashMap<>();
        int totalRiskScore = 0;

        // Amount-based risk
        int amountRisk = calculateAmountRisk(context.getAmount(), context.getCurrency());
        totalRiskScore += amountRisk;
        details.put("amount_risk", amountRisk);

        // Geographic risk
        int geoRisk = calculateGeographicRisk(context.getCountryCode(), context.getIpAddress());
        totalRiskScore += geoRisk;
        details.put("geographic_risk", geoRisk);

        // Device risk
        int deviceRisk = calculateDeviceRisk(context.getDeviceFingerprint());
        totalRiskScore += deviceRisk;
        details.put("device_risk", deviceRisk);

        // Velocity risk
        int velocityRisk = calculateVelocityRisk(context.getUserId());
        totalRiskScore += velocityRisk;
        details.put("velocity_risk", velocityRisk);

        // Time-based risk
        int timeRisk = calculateTimeBasedRisk(context.getTimestamp());
        totalRiskScore += timeRisk;
        details.put("time_risk", timeRisk);

        // Determine risk level and recommendation
        String riskLevel = determineRiskLevel(totalRiskScore);
        String recommendation = generateRecommendation(totalRiskScore, riskLevel);

        details.put("total_score", totalRiskScore);
        details.put("timestamp", context.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return new RiskAssessment(totalRiskScore, riskLevel, recommendation, details);
    }

    /**
     * Calculates risk based on transaction amount
     */
    public static int calculateAmountRisk(BigDecimal amount, String currency) {
        // Convert to TL for standardized comparison
        BigDecimal tlAmount = CurrencyUtils.convertCurrency(amount, currency, "TL");
        
        if (tlAmount.compareTo(new BigDecimal("100000")) > 0) { // > 1000 TL
            return 30;
        } else if (tlAmount.compareTo(new BigDecimal("50000")) > 0) { // > 500 TL
            return 20;
        } else if (tlAmount.compareTo(new BigDecimal("10000")) > 0) { // > 100 TL
            return 10;
        } else if (tlAmount.compareTo(new BigDecimal("100")) < 0) { // < 1 TL
            return 15; // Very small amounts can be suspicious
        }
        
        return 0;
    }

    /**
     * Calculates geographic risk based on country and IP
     */
    public static int calculateGeographicRisk(String countryCode, String ipAddress) {
        int risk = 0;
        
        // High-risk country check
        if (HIGH_RISK_COUNTRIES.contains(countryCode)) {
            risk += 40;
        }
        
        // Suspicious IP patterns
        for (Pattern pattern : SUSPICIOUS_IP_PATTERNS) {
            if (pattern.matcher(ipAddress).matches()) {
                risk += 20;
                break;
            }
        }
        
        // TOR/VPN detection (simplified)
        if (isSuspiciousIP(ipAddress)) {
            risk += 25;
        }
        
        return Math.min(risk, 50); // Cap at 50
    }

    /**
     * Calculates device-based risk
     */
    public static int calculateDeviceRisk(String deviceFingerprint) {
        return DEVICE_RISK_SCORES.getOrDefault(deviceFingerprint, 30);
    }

    /**
     * Calculates velocity-based risk (simulated)
     */
    public static int calculateVelocityRisk(String userId) {
        // Simulate transaction count for user
        Random random = new Random(userId.hashCode());
        int transactionsLastHour = random.nextInt(10);
        int transactionsLastDay = random.nextInt(50);
        
        int risk = 0;
        
        if (transactionsLastHour > MAX_TRANSACTIONS_PER_MINUTE) {
            risk += 50;
        } else if (transactionsLastHour > MAX_TRANSACTIONS_PER_HOUR / 2) {
            risk += 25;
        }
        
        if (transactionsLastDay > MAX_TRANSACTIONS_PER_DAY) {
            risk += 30;
        } else if (transactionsLastDay > MAX_TRANSACTIONS_PER_DAY / 2) {
            risk += 15;
        }
        
        return Math.min(risk, 60); // Cap at 60
    }

    /**
     * Calculates time-based risk
     */
    public static int calculateTimeBasedRisk(LocalDateTime timestamp) {
        int hour = timestamp.getHour();
        
        // Higher risk during unusual hours (2 AM - 6 AM)
        if (hour >= 2 && hour <= 6) {
            return 15;
        }
        
        // Weekend transactions might be slightly riskier
        if (timestamp.getDayOfWeek().getValue() >= 6) {
            return 5;
        }
        
        return 0;
    }

    /**
     * Determines risk level based on total score
     */
    public static String determineRiskLevel(int riskScore) {
        if (riskScore >= HIGH_RISK_THRESHOLD) {
            return "HIGH";
        } else if (riskScore >= MEDIUM_RISK_THRESHOLD) {
            return "MEDIUM";
        } else if (riskScore >= LOW_RISK_THRESHOLD) {
            return "LOW";
        } else {
            return "VERY_LOW";
        }
    }

    /**
     * Generates recommendation based on risk assessment
     */
    public static String generateRecommendation(int riskScore, String riskLevel) {
        switch (riskLevel) {
            case "HIGH":
                return "DECLINE";
            case "MEDIUM":
                return "REVIEW";
            case "LOW":
                return "APPROVE_WITH_MONITORING";
            default:
                return "APPROVE";
        }
    }

    /**
     * Checks if IP address is suspicious
     */
    public static boolean isSuspiciousIP(String ipAddress) {
        // Simplified suspicious IP detection
        return ipAddress.startsWith("192.168.1.100") || 
               ipAddress.startsWith("10.0.0.") ||
               ipAddress.equals("127.0.0.1");
    }

    /**
     * Validates device fingerprint
     */
    public static boolean isValidDeviceFingerprint(String deviceFingerprint) {
        return deviceFingerprint != null && 
               deviceFingerprint.length() >= 10 && 
               !deviceFingerprint.equals("UNKNOWN");
    }

    /**
     * Generates test fraud scenarios
     */
    public static List<TransactionContext> generateFraudTestScenarios() {
        List<TransactionContext> scenarios = new ArrayList<>();
        
        // Low risk scenario
        scenarios.add(new TransactionContext(
            "user_001", "127.0.0.1", "TR", "TRUSTED_DEVICE", 
            new BigDecimal("100"), "TL"
        ));
        
        // Medium risk scenario
        scenarios.add(new TransactionContext(
            "user_002", "192.168.1.50", "US", "NEW_DEVICE", 
            new BigDecimal("500"), "USD"
        ));
        
        // High risk scenario
        scenarios.add(new TransactionContext(
            "user_003", "192.168.1.100", "AF", "SUSPICIOUS_DEVICE", 
            new BigDecimal("2000"), "EUR"
        ));
        
        // Critical risk scenario
        scenarios.add(new TransactionContext(
            "user_004", "10.0.0.1", "IQ", "BLACKLISTED_DEVICE", 
            new BigDecimal("5000"), "USD"
        ));
        
        return scenarios;
    }

    /**
     * Simulates behavioral analysis
     */
    public static Map<String, Object> analyzeBehavioralPatterns(String userId, TransactionContext context) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Simulate user behavior patterns
        Random random = new Random(userId.hashCode());
        
        // Typical transaction amount for user
        BigDecimal typicalAmount = new BigDecimal(random.nextInt(1000) + 100);
        BigDecimal currentAmount = context.getAmount();
        
        // Amount deviation
        double amountDeviation = Math.abs(currentAmount.subtract(typicalAmount).doubleValue()) / typicalAmount.doubleValue();
        analysis.put("amount_deviation", amountDeviation);
        analysis.put("amount_anomaly", amountDeviation > 2.0);
        
        // Typical transaction time
        int typicalHour = random.nextInt(12) + 9; // 9 AM - 9 PM
        int currentHour = context.getTimestamp().getHour();
        analysis.put("time_deviation", Math.abs(currentHour - typicalHour));
        analysis.put("time_anomaly", Math.abs(currentHour - typicalHour) > 6);
        
        // Location consistency
        boolean locationConsistent = context.getCountryCode().equals("TR") || 
                                   context.getCountryCode().equals("US");
        analysis.put("location_consistent", locationConsistent);
        
        // Device consistency
        boolean deviceConsistent = context.getDeviceFingerprint().contains("TRUSTED") ||
                                 context.getDeviceFingerprint().contains("NEW");
        analysis.put("device_consistent", deviceConsistent);
        
        // Overall behavioral risk
        int behavioralRisk = 0;
        if ((Boolean) analysis.get("amount_anomaly")) behavioralRisk += 20;
        if ((Boolean) analysis.get("time_anomaly")) behavioralRisk += 15;
        if (!locationConsistent) behavioralRisk += 25;
        if (!deviceConsistent) behavioralRisk += 30;
        
        analysis.put("behavioral_risk_score", behavioralRisk);
        analysis.put("behavioral_risk_level", behavioralRisk > 40 ? "HIGH" : behavioralRisk > 20 ? "MEDIUM" : "LOW");
        
        return analysis;
    }

    /**
     * Checks velocity limits
     */
    public static Map<String, Object> checkVelocityLimits(String userId, int timeWindowMinutes) {
        Map<String, Object> velocityCheck = new HashMap<>();
        
        // Simulate transaction counts
        Random random = new Random(userId.hashCode() + timeWindowMinutes);
        int transactionCount = random.nextInt(timeWindowMinutes / 5 + 1);
        
        // Determine limits based on time window
        int limit;
        if (timeWindowMinutes <= 1) {
            limit = MAX_TRANSACTIONS_PER_MINUTE;
        } else if (timeWindowMinutes <= 60) {
            limit = MAX_TRANSACTIONS_PER_HOUR;
        } else {
            limit = MAX_TRANSACTIONS_PER_DAY;
        }
        
        velocityCheck.put("transaction_count", transactionCount);
        velocityCheck.put("limit", limit);
        velocityCheck.put("exceeded", transactionCount > limit);
        velocityCheck.put("utilization_percent", (double) transactionCount / limit * 100);
        
        return velocityCheck;
    }

    /**
     * Generates fraud detection test data
     */
    public static Map<String, Object> generateFraudTestData(String riskLevel) {
        Map<String, Object> testData = new HashMap<>();
        
        switch (riskLevel.toUpperCase()) {
            case "LOW":
                testData.put("user_id", "low_risk_user_001");
                testData.put("ip_address", "127.0.0.1");
                testData.put("country_code", "TR");
                testData.put("device_fingerprint", "TRUSTED_DEVICE");
                testData.put("amount", "100");
                testData.put("currency", "TL");
                break;
                
            case "MEDIUM":
                testData.put("user_id", "medium_risk_user_002");
                testData.put("ip_address", "192.168.1.50");
                testData.put("country_code", "US");
                testData.put("device_fingerprint", "NEW_DEVICE");
                testData.put("amount", "500");
                testData.put("currency", "USD");
                break;
                
            case "HIGH":
                testData.put("user_id", "high_risk_user_003");
                testData.put("ip_address", "192.168.1.100");
                testData.put("country_code", "AF");
                testData.put("device_fingerprint", "SUSPICIOUS_DEVICE");
                testData.put("amount", "2000");
                testData.put("currency", "EUR");
                break;
                
            case "CRITICAL":
                testData.put("user_id", "critical_risk_user_004");
                testData.put("ip_address", "10.0.0.1");
                testData.put("country_code", "IQ");
                testData.put("device_fingerprint", "BLACKLISTED_DEVICE");
                testData.put("amount", "5000");
                testData.put("currency", "USD");
                break;
                
            default:
                testData.put("user_id", "default_user");
                testData.put("ip_address", "127.0.0.1");
                testData.put("country_code", "TR");
                testData.put("device_fingerprint", "TRUSTED_DEVICE");
                testData.put("amount", "100");
                testData.put("currency", "TL");
        }
        
        testData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        testData.put("expected_risk_level", riskLevel.toUpperCase());
        
        return testData;
    }

    /**
     * Validates fraud detection response
     */
    public static boolean validateFraudDetectionResponse(Map<String, Object> response, String expectedRiskLevel) {
        if (response == null || !response.containsKey("risk_level")) {
            return false;
        }
        
        String actualRiskLevel = (String) response.get("risk_level");
        return expectedRiskLevel.equalsIgnoreCase(actualRiskLevel);
    }

    /**
     * Generates geolocation anomaly test cases
     */
    public static List<Map<String, Object>> generateGeolocationAnomalyTests() {
        List<Map<String, Object>> tests = new ArrayList<>();
        
        // Normal location
        Map<String, Object> normal = new HashMap<>();
        normal.put("country_code", "TR");
        normal.put("ip_address", "127.0.0.1");
        normal.put("expected_anomaly", false);
        tests.add(normal);
        
        // High-risk country
        Map<String, Object> highRisk = new HashMap<>();
        highRisk.put("country_code", "AF");
        highRisk.put("ip_address", "192.168.1.100");
        highRisk.put("expected_anomaly", true);
        tests.add(highRisk);
        
        // Suspicious IP
        Map<String, Object> suspiciousIP = new HashMap<>();
        suspiciousIP.put("country_code", "US");
        suspiciousIP.put("ip_address", "10.0.0.1");
        suspiciousIP.put("expected_anomaly", true);
        tests.add(suspiciousIP);
        
        return tests;
    }
}