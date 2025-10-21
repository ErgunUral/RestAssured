package com.example.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Chaos Engineering Utilities
 * Kaos mühendisliği testleri için yardımcı sınıf
 * 
 * Özellikler:
 * - Network latency simulation
 * - Service degradation testing
 * - Resource exhaustion simulation
 * - Random failure injection
 * - Circuit breaker testing
 * - System resilience validation
 */
public class ChaosEngineeringUtils {

    private static final Random random = new Random();
    private static final Map<String, Boolean> circuitBreakerStates = new ConcurrentHashMap<>();
    private static final Map<String, Integer> failureCounters = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> lastFailureTime = new ConcurrentHashMap<>();
    
    // Circuit breaker configuration
    private static final int FAILURE_THRESHOLD = 5;
    private static final int RECOVERY_TIMEOUT_SECONDS = 30;
    
    /**
     * Chaos Test Result
     */
    public static class ChaosTestResult {
        private final String testType;
        private final boolean successful;
        private final long executionTime;
        private final String errorMessage;
        private final Map<String, Object> metrics;

        public ChaosTestResult(String testType, boolean successful, long executionTime, 
                             String errorMessage, Map<String, Object> metrics) {
            this.testType = testType;
            this.successful = successful;
            this.executionTime = executionTime;
            this.errorMessage = errorMessage;
            this.metrics = metrics;
        }

        // Getters
        public String getTestType() { return testType; }
        public boolean isSuccessful() { return successful; }
        public long getExecutionTime() { return executionTime; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetrics() { return metrics; }
    }

    /**
     * Network Latency Simulation
     */
    public static class NetworkLatencySimulator {
        
        public static void simulateLatency(int minLatencyMs, int maxLatencyMs) {
            int latency = random.nextInt(maxLatencyMs - minLatencyMs + 1) + minLatencyMs;
            try {
                Thread.sleep(latency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public static void simulateHighLatency() {
            simulateLatency(2000, 5000); // 2-5 seconds
        }
        
        public static void simulateMediumLatency() {
            simulateLatency(500, 2000); // 0.5-2 seconds
        }
        
        public static void simulateLowLatency() {
            simulateLatency(50, 500); // 50-500 ms
        }
        
        public static ChaosTestResult testNetworkLatencyResilience(Runnable operation, int maxAcceptableLatency) {
            long startTime = System.currentTimeMillis();
            Map<String, Object> metrics = new HashMap<>();
            
            try {
                // Simulate network latency
                simulateHighLatency();
                
                // Execute operation
                operation.run();
                
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("max_acceptable_latency_ms", maxAcceptableLatency);
                metrics.put("latency_acceptable", executionTime <= maxAcceptableLatency);
                
                boolean successful = executionTime <= maxAcceptableLatency;
                String errorMessage = successful ? null : "Execution time exceeded acceptable latency";
                
                return new ChaosTestResult("NETWORK_LATENCY", successful, executionTime, errorMessage, metrics);
                
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("exception", e.getClass().getSimpleName());
                
                return new ChaosTestResult("NETWORK_LATENCY", false, executionTime, e.getMessage(), metrics);
            }
        }
    }

    /**
     * Service Degradation Simulator
     */
    public static class ServiceDegradationSimulator {
        
        public static void simulateServiceDegradation(double degradationPercent) {
            if (random.nextDouble() * 100 < degradationPercent) {
                // Simulate service slowdown
                try {
                    Thread.sleep(random.nextInt(3000) + 1000); // 1-4 seconds delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        public static ChaosTestResult testServiceDegradationHandling(Runnable operation, double degradationPercent) {
            long startTime = System.currentTimeMillis();
            Map<String, Object> metrics = new HashMap<>();
            
            try {
                // Simulate service degradation
                simulateServiceDegradation(degradationPercent);
                
                // Execute operation
                operation.run();
                
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("degradation_percent", degradationPercent);
                metrics.put("degradation_applied", executionTime > 1000);
                
                return new ChaosTestResult("SERVICE_DEGRADATION", true, executionTime, null, metrics);
                
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("degradation_percent", degradationPercent);
                metrics.put("exception", e.getClass().getSimpleName());
                
                return new ChaosTestResult("SERVICE_DEGRADATION", false, executionTime, e.getMessage(), metrics);
            }
        }
    }

    /**
     * Resource Exhaustion Simulator
     */
    public static class ResourceExhaustionSimulator {
        
        public static void simulateMemoryPressure() {
            // Simulate memory pressure by creating temporary objects
            List<byte[]> memoryConsumer = new ArrayList<>();
            try {
                for (int i = 0; i < 100; i++) {
                    memoryConsumer.add(new byte[1024 * 1024]); // 1MB chunks
                    Thread.sleep(10);
                }
            } catch (OutOfMemoryError | InterruptedException e) {
                // Expected behavior
            } finally {
                memoryConsumer.clear();
                System.gc();
            }
        }
        
        public static void simulateCPUPressure() {
            // Simulate CPU pressure with intensive computation
            long endTime = System.currentTimeMillis() + 2000; // 2 seconds
            while (System.currentTimeMillis() < endTime) {
                Math.sqrt(random.nextDouble());
            }
        }
        
        public static ChaosTestResult testResourceExhaustionResilience(Runnable operation, String resourceType) {
            long startTime = System.currentTimeMillis();
            Map<String, Object> metrics = new HashMap<>();
            
            try {
                // Simulate resource exhaustion
                switch (resourceType.toUpperCase()) {
                    case "MEMORY":
                        simulateMemoryPressure();
                        break;
                    case "CPU":
                        simulateCPUPressure();
                        break;
                    default:
                        simulateMemoryPressure();
                }
                
                // Execute operation
                operation.run();
                
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("resource_type", resourceType);
                metrics.put("resource_pressure_applied", true);
                
                return new ChaosTestResult("RESOURCE_EXHAUSTION", true, executionTime, null, metrics);
                
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                metrics.put("execution_time_ms", executionTime);
                metrics.put("resource_type", resourceType);
                metrics.put("exception", e.getClass().getSimpleName());
                
                return new ChaosTestResult("RESOURCE_EXHAUSTION", false, executionTime, e.getMessage(), metrics);
            }
        }
    }

    /**
     * Random Failure Injector
     */
    public static class RandomFailureInjector {
        
        public static void injectRandomFailure(double failureRate) throws Exception {
            if (random.nextDouble() * 100 < failureRate) {
                String[] failureTypes = {
                    "CONNECTION_TIMEOUT", "SERVICE_UNAVAILABLE", "INTERNAL_ERROR", 
                    "RATE_LIMIT_EXCEEDED", "AUTHENTICATION_FAILED"
                };
                String failureType = failureTypes[random.nextInt(failureTypes.length)];
                throw new RuntimeException("Injected failure: " + failureType);
            }
        }
        
        public static ChaosTestResult testRandomFailureHandling(Runnable operation, double failureRate, int attempts) {
            long startTime = System.currentTimeMillis();
            Map<String, Object> metrics = new HashMap<>();
            
            int successfulAttempts = 0;
            int failedAttempts = 0;
            List<String> failures = new ArrayList<>();
            
            for (int i = 0; i < attempts; i++) {
                try {
                    // Inject random failure
                    injectRandomFailure(failureRate);
                    
                    // Execute operation
                    operation.run();
                    successfulAttempts++;
                    
                } catch (Exception e) {
                    failedAttempts++;
                    failures.add(e.getMessage());
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            double successRate = (double) successfulAttempts / attempts * 100;
            
            metrics.put("execution_time_ms", executionTime);
            metrics.put("total_attempts", attempts);
            metrics.put("successful_attempts", successfulAttempts);
            metrics.put("failed_attempts", failedAttempts);
            metrics.put("success_rate_percent", successRate);
            metrics.put("failure_rate_percent", failureRate);
            metrics.put("failures", failures);
            
            boolean successful = successRate >= (100 - failureRate - 10); // Allow 10% tolerance
            String errorMessage = successful ? null : "Success rate too low: " + successRate + "%";
            
            return new ChaosTestResult("RANDOM_FAILURE", successful, executionTime, errorMessage, metrics);
        }
    }

    /**
     * Circuit Breaker Pattern Tester
     */
    public static class CircuitBreakerTester {
        
        public static boolean isCircuitBreakerOpen(String serviceName) {
            return circuitBreakerStates.getOrDefault(serviceName, false);
        }
        
        public static void recordFailure(String serviceName) {
            int currentCount = failureCounters.getOrDefault(serviceName, 0) + 1;
            failureCounters.put(serviceName, currentCount);
            lastFailureTime.put(serviceName, LocalDateTime.now());
            
            if (currentCount >= FAILURE_THRESHOLD) {
                circuitBreakerStates.put(serviceName, true); // Open circuit
            }
        }
        
        public static void recordSuccess(String serviceName) {
            failureCounters.put(serviceName, 0);
            circuitBreakerStates.put(serviceName, false); // Close circuit
        }
        
        public static boolean canAttemptRecovery(String serviceName) {
            LocalDateTime lastFailure = lastFailureTime.get(serviceName);
            if (lastFailure == null) return true;
            
            return LocalDateTime.now().isAfter(lastFailure.plusSeconds(RECOVERY_TIMEOUT_SECONDS));
        }
        
        public static ChaosTestResult testCircuitBreakerPattern(String serviceName, Runnable operation, 
                                                              double initialFailureRate) {
            long startTime = System.currentTimeMillis();
            Map<String, Object> metrics = new HashMap<>();
            
            // Reset circuit breaker state
            circuitBreakerStates.put(serviceName, false);
            failureCounters.put(serviceName, 0);
            
            int totalAttempts = 0;
            int successfulAttempts = 0;
            int circuitBreakerTrips = 0;
            boolean circuitBreakerWorking = false;
            
            // Phase 1: Trigger failures to open circuit breaker
            for (int i = 0; i < FAILURE_THRESHOLD + 2; i++) {
                totalAttempts++;
                
                if (isCircuitBreakerOpen(serviceName) && !canAttemptRecovery(serviceName)) {
                    circuitBreakerTrips++;
                    circuitBreakerWorking = true;
                    continue; // Circuit breaker prevents execution
                }
                
                try {
                    // Inject failure to trigger circuit breaker
                    if (random.nextDouble() * 100 < initialFailureRate) {
                        recordFailure(serviceName);
                        throw new RuntimeException("Simulated service failure");
                    }
                    
                    operation.run();
                    recordSuccess(serviceName);
                    successfulAttempts++;
                    
                } catch (Exception e) {
                    recordFailure(serviceName);
                }
            }
            
            // Phase 2: Test recovery after timeout
            try {
                Thread.sleep(RECOVERY_TIMEOUT_SECONDS * 1000 + 1000); // Wait for recovery window
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Attempt recovery
            if (canAttemptRecovery(serviceName)) {
                totalAttempts++;
                try {
                    operation.run();
                    recordSuccess(serviceName);
                    successfulAttempts++;
                } catch (Exception e) {
                    recordFailure(serviceName);
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            metrics.put("execution_time_ms", executionTime);
            metrics.put("total_attempts", totalAttempts);
            metrics.put("successful_attempts", successfulAttempts);
            metrics.put("circuit_breaker_trips", circuitBreakerTrips);
            metrics.put("circuit_breaker_working", circuitBreakerWorking);
            metrics.put("failure_threshold", FAILURE_THRESHOLD);
            metrics.put("recovery_timeout_seconds", RECOVERY_TIMEOUT_SECONDS);
            metrics.put("final_circuit_state", isCircuitBreakerOpen(serviceName) ? "OPEN" : "CLOSED");
            
            boolean successful = circuitBreakerWorking;
            String errorMessage = successful ? null : "Circuit breaker did not function properly";
            
            return new ChaosTestResult("CIRCUIT_BREAKER", successful, executionTime, errorMessage, metrics);
        }
    }

    /**
     * Chaos Test Scenario Generator
     */
    public static List<Map<String, Object>> generateChaosTestScenarios() {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        
        // Network latency scenarios
        Map<String, Object> networkLatency = new HashMap<>();
        networkLatency.put("type", "NETWORK_LATENCY");
        networkLatency.put("intensity", "HIGH");
        networkLatency.put("duration_seconds", 30);
        networkLatency.put("max_acceptable_latency_ms", 10000);
        scenarios.add(networkLatency);
        
        // Service degradation scenarios
        Map<String, Object> serviceDegradation = new HashMap<>();
        serviceDegradation.put("type", "SERVICE_DEGRADATION");
        serviceDegradation.put("intensity", "MEDIUM");
        serviceDegradation.put("degradation_percent", 50.0);
        serviceDegradation.put("duration_seconds", 60);
        scenarios.add(serviceDegradation);
        
        // Resource exhaustion scenarios
        Map<String, Object> resourceExhaustion = new HashMap<>();
        resourceExhaustion.put("type", "RESOURCE_EXHAUSTION");
        resourceExhaustion.put("resource_type", "MEMORY");
        resourceExhaustion.put("intensity", "HIGH");
        resourceExhaustion.put("duration_seconds", 45);
        scenarios.add(resourceExhaustion);
        
        // Random failure scenarios
        Map<String, Object> randomFailure = new HashMap<>();
        randomFailure.put("type", "RANDOM_FAILURE");
        randomFailure.put("failure_rate_percent", 30.0);
        randomFailure.put("attempts", 20);
        randomFailure.put("duration_seconds", 120);
        scenarios.add(randomFailure);
        
        // Circuit breaker scenarios
        Map<String, Object> circuitBreaker = new HashMap<>();
        circuitBreaker.put("type", "CIRCUIT_BREAKER");
        circuitBreaker.put("service_name", "payment_service");
        circuitBreaker.put("initial_failure_rate", 80.0);
        circuitBreaker.put("duration_seconds", 90);
        scenarios.add(circuitBreaker);
        
        return scenarios;
    }

    /**
     * Executes chaos test based on scenario configuration
     */
    public static ChaosTestResult executeChaosTest(Map<String, Object> scenario, Runnable operation) {
        String testType = (String) scenario.get("type");
        
        switch (testType.toUpperCase()) {
            case "NETWORK_LATENCY":
                int maxLatency = (Integer) scenario.getOrDefault("max_acceptable_latency_ms", 5000);
                return NetworkLatencySimulator.testNetworkLatencyResilience(operation, maxLatency);
                
            case "SERVICE_DEGRADATION":
                double degradationPercent = (Double) scenario.getOrDefault("degradation_percent", 50.0);
                return ServiceDegradationSimulator.testServiceDegradationHandling(operation, degradationPercent);
                
            case "RESOURCE_EXHAUSTION":
                String resourceType = (String) scenario.getOrDefault("resource_type", "MEMORY");
                return ResourceExhaustionSimulator.testResourceExhaustionResilience(operation, resourceType);
                
            case "RANDOM_FAILURE":
                double failureRate = (Double) scenario.getOrDefault("failure_rate_percent", 30.0);
                int attempts = (Integer) scenario.getOrDefault("attempts", 10);
                return RandomFailureInjector.testRandomFailureHandling(operation, failureRate, attempts);
                
            case "CIRCUIT_BREAKER":
                String serviceName = (String) scenario.getOrDefault("service_name", "test_service");
                double initialFailureRate = (Double) scenario.getOrDefault("initial_failure_rate", 80.0);
                return CircuitBreakerTester.testCircuitBreakerPattern(serviceName, operation, initialFailureRate);
                
            default:
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("error", "Unknown test type: " + testType);
                return new ChaosTestResult(testType, false, 0, "Unknown test type", metrics);
        }
    }

    /**
     * Validates system resilience based on chaos test results
     */
    public static Map<String, Object> validateSystemResilience(List<ChaosTestResult> results) {
        Map<String, Object> resilienceReport = new HashMap<>();
        
        int totalTests = results.size();
        int passedTests = 0;
        long totalExecutionTime = 0;
        Map<String, Integer> testTypeResults = new HashMap<>();
        
        for (ChaosTestResult result : results) {
            if (result.isSuccessful()) {
                passedTests++;
            }
            totalExecutionTime += result.getExecutionTime();
            
            String testType = result.getTestType();
            testTypeResults.put(testType, testTypeResults.getOrDefault(testType, 0) + 1);
        }
        
        double resilienceScore = (double) passedTests / totalTests * 100;
        double averageExecutionTime = (double) totalExecutionTime / totalTests;
        
        resilienceReport.put("total_tests", totalTests);
        resilienceReport.put("passed_tests", passedTests);
        resilienceReport.put("failed_tests", totalTests - passedTests);
        resilienceReport.put("resilience_score_percent", resilienceScore);
        resilienceReport.put("average_execution_time_ms", averageExecutionTime);
        resilienceReport.put("test_type_distribution", testTypeResults);
        resilienceReport.put("resilience_level", determineResilienceLevel(resilienceScore));
        resilienceReport.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return resilienceReport;
    }

    /**
     * Determines resilience level based on score
     */
    private static String determineResilienceLevel(double resilienceScore) {
        if (resilienceScore >= 90) {
            return "EXCELLENT";
        } else if (resilienceScore >= 75) {
            return "GOOD";
        } else if (resilienceScore >= 60) {
            return "ACCEPTABLE";
        } else if (resilienceScore >= 40) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Generates chaos engineering test report
     */
    public static String generateChaosTestReport(List<ChaosTestResult> results) {
        StringBuilder report = new StringBuilder();
        Map<String, Object> resilienceReport = validateSystemResilience(results);
        
        report.append("=== CHAOS ENGINEERING TEST REPORT ===\n");
        report.append("Generated: ").append(resilienceReport.get("timestamp")).append("\n\n");
        
        report.append("OVERALL RESULTS:\n");
        report.append("- Total Tests: ").append(resilienceReport.get("total_tests")).append("\n");
        report.append("- Passed Tests: ").append(resilienceReport.get("passed_tests")).append("\n");
        report.append("- Failed Tests: ").append(resilienceReport.get("failed_tests")).append("\n");
        report.append("- Resilience Score: ").append(String.format("%.2f", (Double) resilienceReport.get("resilience_score_percent"))).append("%\n");
        report.append("- Resilience Level: ").append(resilienceReport.get("resilience_level")).append("\n");
        report.append("- Average Execution Time: ").append(String.format("%.2f", (Double) resilienceReport.get("average_execution_time_ms"))).append(" ms\n\n");
        
        report.append("DETAILED RESULTS:\n");
        for (ChaosTestResult result : results) {
            report.append("- ").append(result.getTestType()).append(": ");
            report.append(result.isSuccessful() ? "PASSED" : "FAILED");
            report.append(" (").append(result.getExecutionTime()).append(" ms)");
            if (!result.isSuccessful() && result.getErrorMessage() != null) {
                report.append(" - ").append(result.getErrorMessage());
            }
            report.append("\n");
        }
        
        return report.toString();
    }
}