package com.example.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Webhook Utilities
 * Webhook testleri için yardımcı sınıf
 * 
 * Özellikler:
 * - Webhook signature generation and verification
 * - Webhook delivery simulation
 * - Retry mechanism testing
 * - Event filtering
 * - Webhook monitoring and analytics
 */
public class WebhookUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Map<String, List<WebhookDeliveryAttempt>> deliveryHistory = new ConcurrentHashMap<>();
    private static final Map<String, WebhookEndpoint> webhookEndpoints = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    /**
     * Webhook Event
     */
    public static class WebhookEvent {
        private final String id;
        private final String type;
        private final String timestamp;
        private final Map<String, Object> data;
        private final Map<String, String> metadata;

        public WebhookEvent(String id, String type, Map<String, Object> data) {
            this.id = id;
            this.type = type;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
            this.metadata = new HashMap<>();
        }

        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return data; }
        public Map<String, String> getMetadata() { return metadata; }

        public void addMetadata(String key, String value) {
            metadata.put(key, value);
        }

        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"id\":\"").append(id).append("\",");
            json.append("\"type\":\"").append(type).append("\",");
            json.append("\"timestamp\":\"").append(timestamp).append("\",");
            json.append("\"data\":").append(mapToJson(data)).append(",");
            json.append("\"metadata\":").append(mapToJson(metadata));
            json.append("}");
            return json.toString();
        }

        private String mapToJson(Map<String, ?> map) {
            if (map.isEmpty()) return "{}";
            
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":");
                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(value).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else {
                    json.append("\"").append(value != null ? value.toString() : "null").append("\"");
                }
                first = false;
            }
            json.append("}");
            return json.toString();
        }
    }

    /**
     * Webhook Endpoint Configuration
     */
    public static class WebhookEndpoint {
        private final String url;
        private final String secret;
        private final Set<String> eventTypes;
        private final Map<String, String> headers;
        private final int maxRetries;
        private final int retryDelaySeconds;
        private boolean active;

        public WebhookEndpoint(String url, String secret, Set<String> eventTypes) {
            this.url = url;
            this.secret = secret;
            this.eventTypes = new HashSet<>(eventTypes);
            this.headers = new HashMap<>();
            this.maxRetries = 3;
            this.retryDelaySeconds = 60;
            this.active = true;
        }

        // Getters
        public String getUrl() { return url; }
        public String getSecret() { return secret; }
        public Set<String> getEventTypes() { return eventTypes; }
        public Map<String, String> getHeaders() { return headers; }
        public int getMaxRetries() { return maxRetries; }
        public int getRetryDelaySeconds() { return retryDelaySeconds; }
        public boolean isActive() { return active; }

        public void setActive(boolean active) { this.active = active; }
        public void addHeader(String key, String value) { headers.put(key, value); }
        public boolean supportsEventType(String eventType) { return eventTypes.contains(eventType); }
    }

    /**
     * Webhook Delivery Attempt
     */
    public static class WebhookDeliveryAttempt {
        private final String webhookId;
        private final String eventId;
        private final String url;
        private final int attemptNumber;
        private final LocalDateTime timestamp;
        private final boolean successful;
        private final int responseCode;
        private final String responseBody;
        private final long responseTimeMs;
        private final String errorMessage;

        public WebhookDeliveryAttempt(String webhookId, String eventId, String url, int attemptNumber,
                                    boolean successful, int responseCode, String responseBody,
                                    long responseTimeMs, String errorMessage) {
            this.webhookId = webhookId;
            this.eventId = eventId;
            this.url = url;
            this.attemptNumber = attemptNumber;
            this.timestamp = LocalDateTime.now();
            this.successful = successful;
            this.responseCode = responseCode;
            this.responseBody = responseBody;
            this.responseTimeMs = responseTimeMs;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getWebhookId() { return webhookId; }
        public String getEventId() { return eventId; }
        public String getUrl() { return url; }
        public int getAttemptNumber() { return attemptNumber; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isSuccessful() { return successful; }
        public int getResponseCode() { return responseCode; }
        public String getResponseBody() { return responseBody; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Webhook Signature Generator
     */
    public static class SignatureGenerator {
        
        public static String generateSignature(String payload, String secret) {
            try {
                Mac mac = Mac.getInstance(HMAC_SHA256);
                SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
                mac.init(secretKeySpec);
                
                byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                return "sha256=" + bytesToHex(hash);
                
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Failed to generate webhook signature", e);
            }
        }
        
        public static boolean verifySignature(String payload, String secret, String signature) {
            String expectedSignature = generateSignature(payload, secret);
            return expectedSignature.equals(signature);
        }
        
        private static String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
    }

    /**
     * Webhook Delivery Simulator
     */
    public static class DeliverySimulator {
        
        public static WebhookDeliveryAttempt simulateDelivery(WebhookEndpoint endpoint, WebhookEvent event, 
                                                            int attemptNumber) {
            long startTime = System.currentTimeMillis();
            
            // Simulate network delay
            try {
                Thread.sleep(random.nextInt(500) + 100); // 100-600ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Simulate delivery success/failure
            boolean successful = random.nextDouble() > 0.2; // 80% success rate
            int responseCode = successful ? 200 : (random.nextBoolean() ? 500 : 404);
            String responseBody = successful ? "OK" : "Error processing webhook";
            long responseTime = System.currentTimeMillis() - startTime;
            String errorMessage = successful ? null : "Simulated delivery failure";
            
            WebhookDeliveryAttempt attempt = new WebhookDeliveryAttempt(
                endpoint.getUrl(), event.getId(), endpoint.getUrl(), attemptNumber,
                successful, responseCode, responseBody, responseTime, errorMessage
            );
            
            // Record delivery attempt
            String key = endpoint.getUrl() + ":" + event.getId();
            deliveryHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(attempt);
            
            return attempt;
        }
        
        public static List<WebhookDeliveryAttempt> simulateDeliveryWithRetries(WebhookEndpoint endpoint, 
                                                                              WebhookEvent event) {
            List<WebhookDeliveryAttempt> attempts = new ArrayList<>();
            
            for (int attempt = 1; attempt <= endpoint.getMaxRetries() + 1; attempt++) {
                WebhookDeliveryAttempt deliveryAttempt = simulateDelivery(endpoint, event, attempt);
                attempts.add(deliveryAttempt);
                
                if (deliveryAttempt.isSuccessful()) {
                    break; // Success, no need to retry
                }
                
                if (attempt <= endpoint.getMaxRetries()) {
                    // Simulate retry delay
                    try {
                        Thread.sleep(endpoint.getRetryDelaySeconds() * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            return attempts;
        }
    }

    /**
     * Event Filter
     */
    public static class EventFilter {
        
        public static boolean shouldDeliverEvent(WebhookEndpoint endpoint, WebhookEvent event) {
            // Check if endpoint is active
            if (!endpoint.isActive()) {
                return false;
            }
            
            // Check if endpoint supports this event type
            if (!endpoint.supportsEventType(event.getType())) {
                return false;
            }
            
            // Additional filtering logic can be added here
            return true;
        }
        
        public static List<WebhookEndpoint> filterEndpointsForEvent(Collection<WebhookEndpoint> endpoints, 
                                                                   WebhookEvent event) {
            List<WebhookEndpoint> filteredEndpoints = new ArrayList<>();
            
            for (WebhookEndpoint endpoint : endpoints) {
                if (shouldDeliverEvent(endpoint, event)) {
                    filteredEndpoints.add(endpoint);
                }
            }
            
            return filteredEndpoints;
        }
    }

    /**
     * Webhook Analytics
     */
    public static class WebhookAnalytics {
        
        public static Map<String, Object> generateDeliveryStats(String webhookUrl) {
            Map<String, Object> stats = new HashMap<>();
            
            List<WebhookDeliveryAttempt> attempts = new ArrayList<>();
            for (Map.Entry<String, List<WebhookDeliveryAttempt>> entry : deliveryHistory.entrySet()) {
                if (entry.getKey().startsWith(webhookUrl + ":")) {
                    attempts.addAll(entry.getValue());
                }
            }
            
            int totalAttempts = attempts.size();
            int successfulAttempts = 0;
            long totalResponseTime = 0;
            Map<Integer, Integer> responseCodeCounts = new HashMap<>();
            
            for (WebhookDeliveryAttempt attempt : attempts) {
                if (attempt.isSuccessful()) {
                    successfulAttempts++;
                }
                totalResponseTime += attempt.getResponseTimeMs();
                
                int responseCode = attempt.getResponseCode();
                responseCodeCounts.put(responseCode, responseCodeCounts.getOrDefault(responseCode, 0) + 1);
            }
            
            double successRate = totalAttempts > 0 ? (double) successfulAttempts / totalAttempts * 100 : 0;
            double averageResponseTime = totalAttempts > 0 ? (double) totalResponseTime / totalAttempts : 0;
            
            stats.put("webhook_url", webhookUrl);
            stats.put("total_attempts", totalAttempts);
            stats.put("successful_attempts", successfulAttempts);
            stats.put("failed_attempts", totalAttempts - successfulAttempts);
            stats.put("success_rate_percent", successRate);
            stats.put("average_response_time_ms", averageResponseTime);
            stats.put("response_code_distribution", responseCodeCounts);
            stats.put("health_status", determineHealthStatus(successRate));
            
            return stats;
        }
        
        public static Map<String, Object> generateOverallStats() {
            Map<String, Object> overallStats = new HashMap<>();
            
            Set<String> uniqueWebhooks = new HashSet<>();
            int totalEvents = 0;
            int totalAttempts = 0;
            int successfulAttempts = 0;
            long totalResponseTime = 0;
            
            for (Map.Entry<String, List<WebhookDeliveryAttempt>> entry : deliveryHistory.entrySet()) {
                String webhookUrl = entry.getKey().split(":")[0];
                uniqueWebhooks.add(webhookUrl);
                
                for (WebhookDeliveryAttempt attempt : entry.getValue()) {
                    if (attempt.getAttemptNumber() == 1) {
                        totalEvents++;
                    }
                    totalAttempts++;
                    if (attempt.isSuccessful()) {
                        successfulAttempts++;
                    }
                    totalResponseTime += attempt.getResponseTimeMs();
                }
            }
            
            double successRate = totalAttempts > 0 ? (double) successfulAttempts / totalAttempts * 100 : 0;
            double averageResponseTime = totalAttempts > 0 ? (double) totalResponseTime / totalAttempts : 0;
            double retryRate = totalEvents > 0 ? (double) (totalAttempts - totalEvents) / totalEvents * 100 : 0;
            
            overallStats.put("total_webhooks", uniqueWebhooks.size());
            overallStats.put("total_events", totalEvents);
            overallStats.put("total_attempts", totalAttempts);
            overallStats.put("successful_attempts", successfulAttempts);
            overallStats.put("success_rate_percent", successRate);
            overallStats.put("retry_rate_percent", retryRate);
            overallStats.put("average_response_time_ms", averageResponseTime);
            overallStats.put("overall_health", determineHealthStatus(successRate));
            
            return overallStats;
        }
        
        private static String determineHealthStatus(double successRate) {
            if (successRate >= 95) {
                return "EXCELLENT";
            } else if (successRate >= 90) {
                return "GOOD";
            } else if (successRate >= 80) {
                return "FAIR";
            } else if (successRate >= 70) {
                return "POOR";
            } else {
                return "CRITICAL";
            }
        }
    }

    /**
     * Test Data Generators
     */
    public static WebhookEvent generatePaymentEvent(String eventType, Map<String, Object> paymentData) {
        String eventId = "evt_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("payment_id", paymentData.getOrDefault("payment_id", "pay_" + random.nextInt(100000)));
        eventData.put("amount", paymentData.getOrDefault("amount", "100.00"));
        eventData.put("currency", paymentData.getOrDefault("currency", "TRY"));
        eventData.put("status", paymentData.getOrDefault("status", "completed"));
        eventData.put("merchant_id", paymentData.getOrDefault("merchant_id", "merchant_123"));
        
        WebhookEvent event = new WebhookEvent(eventId, eventType, eventData);
        event.addMetadata("source", "paytr_api");
        event.addMetadata("version", "1.0");
        
        return event;
    }
    
    public static WebhookEndpoint generateTestWebhookEndpoint(String baseUrl, String eventType) {
        String url = baseUrl + "/webhook/" + random.nextInt(1000);
        String secret = "webhook_secret_" + random.nextInt(10000);
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add(eventType);
        
        WebhookEndpoint endpoint = new WebhookEndpoint(url, secret, eventTypes);
        endpoint.addHeader("Content-Type", "application/json");
        endpoint.addHeader("User-Agent", "PayTR-Webhook/1.0");
        
        webhookEndpoints.put(url, endpoint);
        
        return endpoint;
    }
    
    public static List<WebhookEvent> generateWebhookTestEvents() {
        List<WebhookEvent> events = new ArrayList<>();
        
        // Payment completed event
        Map<String, Object> paymentData1 = new HashMap<>();
        paymentData1.put("payment_id", "pay_12345");
        paymentData1.put("amount", "250.00");
        paymentData1.put("currency", "TRY");
        paymentData1.put("status", "completed");
        events.add(generatePaymentEvent("payment.completed", paymentData1));
        
        // Payment failed event
        Map<String, Object> paymentData2 = new HashMap<>();
        paymentData2.put("payment_id", "pay_12346");
        paymentData2.put("amount", "150.00");
        paymentData2.put("currency", "USD");
        paymentData2.put("status", "failed");
        paymentData2.put("error_code", "insufficient_funds");
        events.add(generatePaymentEvent("payment.failed", paymentData2));
        
        // Refund processed event
        Map<String, Object> refundData = new HashMap<>();
        refundData.put("refund_id", "ref_12345");
        refundData.put("payment_id", "pay_12345");
        refundData.put("amount", "100.00");
        refundData.put("currency", "TRY");
        refundData.put("status", "processed");
        events.add(generatePaymentEvent("refund.processed", refundData));
        
        return events;
    }
    
    public static List<Map<String, Object>> generateWebhookTestScenarios() {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        
        // Successful delivery scenario
        Map<String, Object> successScenario = new HashMap<>();
        successScenario.put("name", "successful_delivery");
        successScenario.put("event_type", "payment.completed");
        successScenario.put("expected_attempts", 1);
        successScenario.put("expected_success", true);
        scenarios.add(successScenario);
        
        // Retry mechanism scenario
        Map<String, Object> retryScenario = new HashMap<>();
        retryScenario.put("name", "retry_mechanism");
        retryScenario.put("event_type", "payment.failed");
        retryScenario.put("simulate_failures", 2);
        retryScenario.put("expected_attempts", 3);
        retryScenario.put("expected_success", true);
        scenarios.add(retryScenario);
        
        // Signature verification scenario
        Map<String, Object> signatureScenario = new HashMap<>();
        signatureScenario.put("name", "signature_verification");
        signatureScenario.put("event_type", "refund.processed");
        signatureScenario.put("test_invalid_signature", true);
        signatureScenario.put("expected_success", false);
        scenarios.add(signatureScenario);
        
        // Event filtering scenario
        Map<String, Object> filteringScenario = new HashMap<>();
        filteringScenario.put("name", "event_filtering");
        filteringScenario.put("event_type", "payment.pending");
        filteringScenario.put("endpoint_supports_event", false);
        filteringScenario.put("expected_delivery", false);
        scenarios.add(filteringScenario);
        
        return scenarios;
    }

    /**
     * Webhook Test Validators
     */
    public static boolean validateWebhookDelivery(WebhookDeliveryAttempt attempt, Map<String, Object> expectedResult) {
        boolean expectedSuccess = (Boolean) expectedResult.getOrDefault("expected_success", true);
        Integer expectedResponseCode = (Integer) expectedResult.get("expected_response_code");
        Long maxResponseTime = (Long) expectedResult.get("max_response_time_ms");
        
        // Validate success status
        if (attempt.isSuccessful() != expectedSuccess) {
            return false;
        }
        
        // Validate response code if specified
        if (expectedResponseCode != null && attempt.getResponseCode() != expectedResponseCode) {
            return false;
        }
        
        // Validate response time if specified
        if (maxResponseTime != null && attempt.getResponseTimeMs() > maxResponseTime) {
            return false;
        }
        
        return true;
    }
    
    public static Map<String, Object> validateWebhookTestResults(List<WebhookDeliveryAttempt> attempts, 
                                                               Map<String, Object> scenario) {
        Map<String, Object> validationResult = new HashMap<>();
        
        String scenarioName = (String) scenario.get("name");
        Integer expectedAttempts = (Integer) scenario.get("expected_attempts");
        Boolean expectedSuccess = (Boolean) scenario.get("expected_success");
        Boolean expectedDelivery = (Boolean) scenario.getOrDefault("expected_delivery", true);
        
        validationResult.put("scenario_name", scenarioName);
        validationResult.put("actual_attempts", attempts.size());
        validationResult.put("expected_attempts", expectedAttempts);
        
        if (!expectedDelivery) {
            // No delivery expected
            validationResult.put("validation_passed", attempts.isEmpty());
            validationResult.put("validation_message", attempts.isEmpty() ? 
                "Correctly filtered event" : "Event should not have been delivered");
        } else {
            // Delivery expected
            boolean hasSuccessfulAttempt = attempts.stream().anyMatch(WebhookDeliveryAttempt::isSuccessful);
            boolean attemptsMatch = expectedAttempts == null || attempts.size() == expectedAttempts;
            boolean successMatch = expectedSuccess == null || hasSuccessfulAttempt == expectedSuccess;
            
            boolean validationPassed = attemptsMatch && successMatch;
            
            validationResult.put("validation_passed", validationPassed);
            validationResult.put("has_successful_attempt", hasSuccessfulAttempt);
            validationResult.put("attempts_match", attemptsMatch);
            validationResult.put("success_match", successMatch);
            
            if (!validationPassed) {
                List<String> issues = new ArrayList<>();
                if (!attemptsMatch) {
                    issues.add("Attempt count mismatch: expected " + expectedAttempts + ", got " + attempts.size());
                }
                if (!successMatch) {
                    issues.add("Success status mismatch: expected " + expectedSuccess + ", got " + hasSuccessfulAttempt);
                }
                validationResult.put("validation_message", String.join("; ", issues));
            } else {
                validationResult.put("validation_message", "All validations passed");
            }
        }
        
        return validationResult;
    }

    /**
     * Cleanup utility methods
     */
    public static void clearDeliveryHistory() {
        deliveryHistory.clear();
    }
    
    public static void clearWebhookEndpoints() {
        webhookEndpoints.clear();
    }
    
    public static void resetWebhookTestEnvironment() {
        clearDeliveryHistory();
        clearWebhookEndpoints();
    }
}