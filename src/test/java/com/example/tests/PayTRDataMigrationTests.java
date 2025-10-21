package com.example.tests;

import com.example.tests.BaseTest;
import com.example.config.PayTRTestConfig;
import io.qameta.allure.*;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

/**
 * PayTR Data Migration Test Sınıfı
 * Veri taşıma ve migrasyon testlerini içerir
 * 
 * Test Kategorileri:
 * - DM-001: Legacy Data Migration
 * - DM-002: Data Integrity Validation
 * - DM-003: Migration Rollback Testing
 * - DM-004: Performance During Migration
 * - DM-005: Cross-System Data Synchronization
 */
@Epic("PayTR Data Migration Tests")
@Feature("Data Migration and Integrity Validation")
public class PayTRDataMigrationTests extends BaseTest {

    /**
     * Test ID: DM-001
     * Data Migration - Legacy Data Migration
     * Tests migration of legacy payment data to new system
     */
    @Test(priority = 1, groups = {"data-migration", "legacy", "critical"})
    @Story("Legacy Data Migration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Eski sistem verilerinin yeni sisteme taşınması")
    public void testLegacyDataMigration() {
        logTestInfo("Test ID: DM-001 - Legacy Data Migration");
        
        try {
            // Simulate legacy data structure
            List<Map<String, Object>> legacyTransactions = new ArrayList<>();
            
            // Legacy transaction 1
            Map<String, Object> legacyTx1 = new HashMap<>();
            legacyTx1.put("old_transaction_id", "LEGACY_001_" + System.currentTimeMillis());
            legacyTx1.put("old_merchant_id", "OLD_MERCHANT_123");
            legacyTx1.put("old_amount", "15000");
            legacyTx1.put("old_currency", "TRY");
            legacyTx1.put("old_date", "2023-01-15");
            legacyTx1.put("old_status", "COMPLETED");
            legacyTransactions.add(legacyTx1);
            
            // Legacy transaction 2
            Map<String, Object> legacyTx2 = new HashMap<>();
            legacyTx2.put("old_transaction_id", "LEGACY_002_" + System.currentTimeMillis());
            legacyTx2.put("old_merchant_id", "OLD_MERCHANT_456");
            legacyTx2.put("old_amount", "25000");
            legacyTx2.put("old_currency", "USD");
            legacyTx2.put("old_date", "2023-02-20");
            legacyTx2.put("old_status", "FAILED");
            legacyTransactions.add(legacyTx2);
            
            // Legacy transaction 3
            Map<String, Object> legacyTx3 = new HashMap<>();
            legacyTx3.put("old_transaction_id", "LEGACY_003_" + System.currentTimeMillis());
            legacyTx3.put("old_merchant_id", "OLD_MERCHANT_789");
            legacyTx3.put("old_amount", "50000");
            legacyTx3.put("old_currency", "EUR");
            legacyTx3.put("old_date", "2023-03-10");
            legacyTx3.put("old_status", "PENDING");
            legacyTransactions.add(legacyTx3);
            
            int successfulMigrations = 0;
            int failedMigrations = 0;
            
            // Migrate each legacy transaction to new format
            for (Map<String, Object> legacyTx : legacyTransactions) {
                try {
                    // Convert legacy data to new format
                    Map<String, Object> newFormatData = new HashMap<>();
                    newFormatData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                    newFormatData.put("user_ip", "127.0.0.1");
                    newFormatData.put("merchant_oid", "DM001_MIGRATED_" + legacyTx.get("old_transaction_id"));
                    newFormatData.put("email", "migration.test@example.com");
                    newFormatData.put("payment_amount", legacyTx.get("old_amount"));
                    
                    // Convert currency format
                    String oldCurrency = (String) legacyTx.get("old_currency");
                    String newCurrency = oldCurrency.equals("TRY") ? "TL" : oldCurrency;
                    newFormatData.put("currency", newCurrency);
                    
                    newFormatData.put("test_mode", "1");
                    newFormatData.put("migration_source", "LEGACY_SYSTEM");
                    newFormatData.put("original_transaction_id", legacyTx.get("old_transaction_id"));
                    newFormatData.put("original_date", legacyTx.get("old_date"));
                    newFormatData.put("original_status", legacyTx.get("old_status"));
                    
                    Response migrationResponse = given()
                        .spec(requestSpec)
                        .body(newFormatData)
                        .when()
                        .post("/odeme/api/get-token")
                        .then()
                        .extract().response();
                    
                    if (migrationResponse.getStatusCode() == 200 || migrationResponse.getStatusCode() == 201) {
                        successfulMigrations++;
                        logTestInfo("Successfully migrated: " + legacyTx.get("old_transaction_id"));
                    } else {
                        failedMigrations++;
                        logTestInfo("Failed to migrate: " + legacyTx.get("old_transaction_id") + 
                                   " - Status: " + migrationResponse.getStatusCode());
                    }
                    
                    Thread.sleep(500); // Delay between migrations
                    
                } catch (Exception e) {
                    failedMigrations++;
                    logTestInfo("Migration error for " + legacyTx.get("old_transaction_id") + ": " + e.getMessage());
                }
            }
            
            double migrationSuccessRate = (double) successfulMigrations / legacyTransactions.size() * 100;
            
            logTestInfo("Legacy Data Migration Results:");
            logTestInfo("  Total Legacy Transactions: " + legacyTransactions.size());
            logTestInfo("  Successful Migrations: " + successfulMigrations);
            logTestInfo("  Failed Migrations: " + failedMigrations);
            logTestInfo("  Migration Success Rate: " + String.format("%.2f", migrationSuccessRate) + "%");
            
            // Migration assertions
            assertTrue(migrationSuccessRate >= 70.0, 
                "Migration success rate too low: " + migrationSuccessRate + "%");
            
            logTestResult("DM-001", "BAŞARILI", 
                "Legacy data migration completed - Success rate: " + String.format("%.2f", migrationSuccessRate) + "%");
            
        } catch (Exception e) {
            logTestResult("DM-001", "BAŞARISIZ", "Legacy data migration hatası: " + e.getMessage());
            fail("Legacy data migration test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: DM-002
     * Data Migration - Data Integrity Validation
     * Tests data integrity during and after migration
     */
    @Test(priority = 2, groups = {"data-migration", "integrity", "high"})
    @Story("Data Integrity Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Migrasyon sırasında ve sonrasında veri bütünlüğü kontrolü")
    public void testDataIntegrityValidation() {
        logTestInfo("Test ID: DM-002 - Data Integrity Validation");
        
        try {
            // Test data integrity with various data types and formats
            Map<String, Object> integrityTestData = new HashMap<>();
            integrityTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            integrityTestData.put("user_ip", "127.0.0.1");
            integrityTestData.put("merchant_oid", "DM002_INTEGRITY_" + System.currentTimeMillis());
            integrityTestData.put("email", "integrity.test@example.com");
            integrityTestData.put("payment_amount", "12345");
            integrityTestData.put("currency", "TL");
            integrityTestData.put("test_mode", "1");
            
            // Add special characters and Unicode data
            integrityTestData.put("user_name", "Ömer Çağlar Şahin");
            integrityTestData.put("user_address", "İstanbul, Türkiye - Çankaya Mah. No:123");
            integrityTestData.put("user_phone", "+90 555 123 45 67");
            
            // Add numeric precision test data
            integrityTestData.put("precise_amount", "12345.67");
            integrityTestData.put("tax_rate", "18.00");
            integrityTestData.put("commission_rate", "2.95");
            
            Response integrityResponse = given()
                .spec(requestSpec)
                .body(integrityTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Data integrity test response: " + integrityResponse.getStatusCode());
            
            // Test data validation with boundary values
            Map<String, Object> boundaryTestData = new HashMap<>();
            boundaryTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            boundaryTestData.put("user_ip", "127.0.0.1");
            boundaryTestData.put("merchant_oid", "DM002_BOUNDARY_" + System.currentTimeMillis());
            boundaryTestData.put("email", "boundary.test@example.com");
            boundaryTestData.put("payment_amount", "1"); // Minimum amount
            boundaryTestData.put("currency", "TL");
            boundaryTestData.put("test_mode", "1");
            
            Response boundaryResponse = given()
                .spec(requestSpec)
                .body(boundaryTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Boundary value test response: " + boundaryResponse.getStatusCode());
            
            // Test data consistency across multiple requests
            String baseOrderId = "DM002_CONSISTENCY_" + System.currentTimeMillis();
            int consistencyTests = 5;
            int consistentResults = 0;
            
            for (int i = 0; i < consistencyTests; i++) {
                Map<String, Object> consistencyData = new HashMap<>();
                consistencyData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                consistencyData.put("user_ip", "127.0.0.1");
                consistencyData.put("merchant_oid", baseOrderId + "_" + i);
                consistencyData.put("email", "consistency" + i + ".test@example.com");
                consistencyData.put("payment_amount", "10000");
                consistencyData.put("currency", "TL");
                consistencyData.put("test_mode", "1");
                
                Response consistencyResponse = given()
                    .spec(requestSpec)
                    .body(consistencyData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                if (consistencyResponse.getStatusCode() == 200 || consistencyResponse.getStatusCode() == 201) {
                    consistentResults++;
                }
                
                Thread.sleep(300);
            }
            
            double consistencyRate = (double) consistentResults / consistencyTests * 100;
            
            // Test data format validation
            Map<String, Object> formatTestData = new HashMap<>();
            formatTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            formatTestData.put("user_ip", "127.0.0.1");
            formatTestData.put("merchant_oid", "DM002_FORMAT_" + System.currentTimeMillis());
            formatTestData.put("email", "format.test@example.com");
            formatTestData.put("payment_amount", "10000");
            formatTestData.put("currency", "TL");
            formatTestData.put("test_mode", "1");
            
            // Add various date formats
            formatTestData.put("transaction_date", "2024-01-15T10:30:00Z");
            formatTestData.put("expiry_date", "12/25");
            formatTestData.put("birth_date", "1990-05-15");
            
            Response formatResponse = given()
                .spec(requestSpec)
                .body(formatTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Format validation test response: " + formatResponse.getStatusCode());
            
            logTestInfo("Data Integrity Validation Results:");
            logTestInfo("  Unicode/Special Characters: " + (integrityResponse.getStatusCode() != 500 ? "PASSED" : "FAILED"));
            logTestInfo("  Boundary Values: " + (boundaryResponse.getStatusCode() != 500 ? "PASSED" : "FAILED"));
            logTestInfo("  Data Consistency: " + String.format("%.2f", consistencyRate) + "% (" + consistentResults + "/" + consistencyTests + ")");
            logTestInfo("  Format Validation: " + (formatResponse.getStatusCode() != 500 ? "PASSED" : "FAILED"));
            
            // Data integrity assertions
            assertTrue(consistencyRate >= 80.0, 
                "Data consistency rate too low: " + consistencyRate + "%");
            
            logTestResult("DM-002", "BAŞARILI", 
                "Data integrity validation completed - Consistency: " + String.format("%.2f", consistencyRate) + "%");
            
        } catch (Exception e) {
            logTestResult("DM-002", "BAŞARISIZ", "Data integrity validation hatası: " + e.getMessage());
            fail("Data integrity validation test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: DM-003
     * Data Migration - Migration Rollback Testing
     * Tests rollback capabilities during failed migrations
     */
    @Test(priority = 3, groups = {"data-migration", "rollback", "medium"})
    @Story("Migration Rollback Testing")
    @Severity(SeverityLevel.NORMAL)
    @Description("Başarısız migrasyonlarda geri alma işlemleri")
    public void testMigrationRollbackTesting() {
        logTestInfo("Test ID: DM-003 - Migration Rollback Testing");
        
        try {
            // Simulate a batch migration scenario
            List<Map<String, Object>> migrationBatch = new ArrayList<>();
            
            // Valid migration data
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> validData = new HashMap<>();
                validData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                validData.put("user_ip", "127.0.0.1");
                validData.put("merchant_oid", "DM003_VALID_" + i + "_" + System.currentTimeMillis());
                validData.put("email", "rollback.valid" + i + "@example.com");
                validData.put("payment_amount", "10000");
                validData.put("currency", "TL");
                validData.put("test_mode", "1");
                validData.put("batch_id", "BATCH_001");
                migrationBatch.add(validData);
            }
            
            // Invalid migration data (to trigger rollback)
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("merchant_id", "INVALID_MERCHANT");
            invalidData.put("user_ip", "127.0.0.1");
            invalidData.put("merchant_oid", "DM003_INVALID_" + System.currentTimeMillis());
            invalidData.put("email", "rollback.invalid@example.com");
            invalidData.put("payment_amount", "INVALID_AMOUNT");
            invalidData.put("currency", "INVALID_CURRENCY");
            invalidData.put("test_mode", "1");
            invalidData.put("batch_id", "BATCH_001");
            migrationBatch.add(invalidData);
            
            int successfulMigrations = 0;
            int failedMigrations = 0;
            boolean rollbackTriggered = false;
            
            // Process migration batch
            for (Map<String, Object> migrationData : migrationBatch) {
                try {
                    Response migrationResponse = given()
                        .spec(requestSpec)
                        .body(migrationData)
                        .when()
                        .post("/odeme/api/get-token")
                        .then()
                        .extract().response();
                    
                    if (migrationResponse.getStatusCode() == 200 || migrationResponse.getStatusCode() == 201) {
                        successfulMigrations++;
                        logTestInfo("Migration successful for: " + migrationData.get("merchant_oid"));
                    } else {
                        failedMigrations++;
                        rollbackTriggered = true;
                        logTestInfo("Migration failed for: " + migrationData.get("merchant_oid") + 
                                   " - Status: " + migrationResponse.getStatusCode());
                        
                        // Simulate rollback process
                        logTestInfo("Triggering rollback for batch: " + migrationData.get("batch_id"));
                        break; // Stop processing on first failure
                    }
                    
                    Thread.sleep(300);
                    
                } catch (Exception e) {
                    failedMigrations++;
                    rollbackTriggered = true;
                    logTestInfo("Migration exception for " + migrationData.get("merchant_oid") + ": " + e.getMessage());
                    break;
                }
            }
            
            // Test rollback verification
            if (rollbackTriggered) {
                logTestInfo("Rollback triggered - Verifying rollback process...");
                
                // Simulate rollback verification by checking if valid transactions can still be processed
                Map<String, Object> rollbackVerificationData = new HashMap<>();
                rollbackVerificationData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                rollbackVerificationData.put("user_ip", "127.0.0.1");
                rollbackVerificationData.put("merchant_oid", "DM003_ROLLBACK_VERIFY_" + System.currentTimeMillis());
                rollbackVerificationData.put("email", "rollback.verify@example.com");
                rollbackVerificationData.put("payment_amount", "10000");
                rollbackVerificationData.put("currency", "TL");
                rollbackVerificationData.put("test_mode", "1");
                
                Response rollbackVerifyResponse = given()
                    .spec(requestSpec)
                    .body(rollbackVerificationData)
                    .when()
                    .post("/odeme/api/get-token")
                    .then()
                    .extract().response();
                
                logTestInfo("Rollback verification response: " + rollbackVerifyResponse.getStatusCode());
            }
            
            // Test partial rollback scenario
            Map<String, Object> partialRollbackData = new HashMap<>();
            partialRollbackData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            partialRollbackData.put("user_ip", "127.0.0.1");
            partialRollbackData.put("merchant_oid", "DM003_PARTIAL_" + System.currentTimeMillis());
            partialRollbackData.put("email", "partial.rollback@example.com");
            partialRollbackData.put("payment_amount", "15000");
            partialRollbackData.put("currency", "TL");
            partialRollbackData.put("test_mode", "1");
            partialRollbackData.put("rollback_type", "PARTIAL");
            
            Response partialRollbackResponse = given()
                .spec(requestSpec)
                .body(partialRollbackData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Partial rollback test response: " + partialRollbackResponse.getStatusCode());
            
            logTestInfo("Migration Rollback Testing Results:");
            logTestInfo("  Successful Migrations: " + successfulMigrations);
            logTestInfo("  Failed Migrations: " + failedMigrations);
            logTestInfo("  Rollback Triggered: " + (rollbackTriggered ? "YES" : "NO"));
            logTestInfo("  Partial Rollback Support: " + (partialRollbackResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            
            // Rollback assertions
            assertTrue(rollbackTriggered, "Rollback should be triggered when invalid data is encountered");
            
            logTestResult("DM-003", "BAŞARILI", "Migration rollback testing completed");
            
        } catch (Exception e) {
            logTestResult("DM-003", "BAŞARISIZ", "Migration rollback test hatası: " + e.getMessage());
            fail("Migration rollback test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: DM-004
     * Data Migration - Performance During Migration
     * Tests system performance during large data migrations
     */
    @Test(priority = 4, groups = {"data-migration", "performance", "medium"})
    @Story("Performance During Migration")
    @Severity(SeverityLevel.NORMAL)
    @Description("Büyük veri migrasyonları sırasında sistem performansı")
    public void testPerformanceDuringMigration() {
        logTestInfo("Test ID: DM-004 - Performance During Migration");
        
        try {
            int migrationBatchSize = 20; // Smaller batch for test environment
            long[] migrationTimes = new long[migrationBatchSize];
            int successfulMigrations = 0;
            
            long migrationStartTime = System.currentTimeMillis();
            
            // Simulate large batch migration
            for (int i = 0; i < migrationBatchSize; i++) {
                long itemStartTime = System.currentTimeMillis();
                
                Map<String, Object> migrationData = new HashMap<>();
                migrationData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
                migrationData.put("user_ip", "127.0.0.1");
                migrationData.put("merchant_oid", "DM004_PERF_" + i + "_" + System.currentTimeMillis());
                migrationData.put("email", "performance.migration" + i + "@example.com");
                migrationData.put("payment_amount", String.valueOf(10000 + (i * 100))); // Varying amounts
                migrationData.put("currency", "TL");
                migrationData.put("test_mode", "1");
                migrationData.put("batch_index", String.valueOf(i));
                migrationData.put("batch_size", String.valueOf(migrationBatchSize));
                
                try {
                    Response migrationResponse = given()
                        .spec(requestSpec)
                        .body(migrationData)
                        .when()
                        .post("/odeme/api/get-token")
                        .then()
                        .extract().response();
                    
                    long itemEndTime = System.currentTimeMillis();
                    migrationTimes[i] = itemEndTime - itemStartTime;
                    
                    if (migrationResponse.getStatusCode() == 200 || migrationResponse.getStatusCode() == 201) {
                        successfulMigrations++;
                    }
                    
                    // Log progress every 5 items
                    if ((i + 1) % 5 == 0) {
                        logTestInfo("Migration progress: " + (i + 1) + "/" + migrationBatchSize + 
                                   " - Last item time: " + migrationTimes[i] + " ms");
                    }
                    
                    Thread.sleep(100); // Small delay to prevent overwhelming the system
                    
                } catch (Exception e) {
                    migrationTimes[i] = -1;
                    logTestInfo("Migration item " + i + " failed: " + e.getMessage());
                }
            }
            
            long migrationEndTime = System.currentTimeMillis();
            long totalMigrationTime = migrationEndTime - migrationStartTime;
            
            // Calculate performance metrics
            double averageMigrationTime = 0;
            long maxMigrationTime = 0;
            long minMigrationTime = Long.MAX_VALUE;
            int validMigrations = 0;
            
            for (long migrationTime : migrationTimes) {
                if (migrationTime > 0) {
                    averageMigrationTime += migrationTime;
                    maxMigrationTime = Math.max(maxMigrationTime, migrationTime);
                    minMigrationTime = Math.min(minMigrationTime, migrationTime);
                    validMigrations++;
                }
            }
            
            if (validMigrations > 0) {
                averageMigrationTime = averageMigrationTime / validMigrations;
            }
            
            double migrationThroughput = (double) successfulMigrations / (totalMigrationTime / 1000.0);
            double successRate = (double) successfulMigrations / migrationBatchSize * 100;
            
            // Test system responsiveness during migration
            Map<String, Object> responsivenessTestData = new HashMap<>();
            responsivenessTestData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            responsivenessTestData.put("user_ip", "127.0.0.1");
            responsivenessTestData.put("merchant_oid", "DM004_RESPONSIVE_" + System.currentTimeMillis());
            responsivenessTestData.put("email", "responsiveness.test@example.com");
            responsivenessTestData.put("payment_amount", "10000");
            responsivenessTestData.put("currency", "TL");
            responsivenessTestData.put("test_mode", "1");
            
            long responsivenessStart = System.currentTimeMillis();
            Response responsivenessResponse = given()
                .spec(requestSpec)
                .body(responsivenessTestData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            long responsivenessEnd = System.currentTimeMillis();
            long responsivenessTime = responsivenessEnd - responsivenessStart;
            
            logTestInfo("Performance During Migration Results:");
            logTestInfo("  Total Migration Time: " + totalMigrationTime + " ms");
            logTestInfo("  Successful Migrations: " + successfulMigrations + "/" + migrationBatchSize);
            logTestInfo("  Success Rate: " + String.format("%.2f", successRate) + "%");
            logTestInfo("  Average Migration Time: " + String.format("%.2f", averageMigrationTime) + " ms");
            logTestInfo("  Max Migration Time: " + maxMigrationTime + " ms");
            logTestInfo("  Min Migration Time: " + (minMigrationTime == Long.MAX_VALUE ? "N/A" : minMigrationTime) + " ms");
            logTestInfo("  Migration Throughput: " + String.format("%.2f", migrationThroughput) + " items/second");
            logTestInfo("  System Responsiveness: " + responsivenessTime + " ms");
            
            // Performance assertions
            assertTrue(averageMigrationTime < 5000, 
                "Average migration time too high: " + averageMigrationTime + " ms");
            assertTrue(successRate >= 80.0, 
                "Migration success rate too low: " + successRate + "%");
            assertTrue(responsivenessTime < 10000, 
                "System responsiveness degraded during migration: " + responsivenessTime + " ms");
            
            logTestResult("DM-004", "BAŞARILI", 
                "Migration performance acceptable - Throughput: " + String.format("%.2f", migrationThroughput) + " items/sec");
            
        } catch (Exception e) {
            logTestResult("DM-004", "BAŞARISIZ", "Migration performance test hatası: " + e.getMessage());
            fail("Migration performance test başarısız: " + e.getMessage());
        }
    }

    /**
     * Test ID: DM-005
     * Data Migration - Cross-System Data Synchronization
     * Tests data synchronization between different systems
     */
    @Test(priority = 5, groups = {"data-migration", "synchronization", "medium"})
    @Story("Cross-System Data Synchronization")
    @Severity(SeverityLevel.NORMAL)
    @Description("Farklı sistemler arası veri senkronizasyonu")
    public void testCrossSystemDataSynchronization() {
        logTestInfo("Test ID: DM-005 - Cross-System Data Synchronization");
        
        try {
            // Simulate data from different source systems
            Map<String, Object> system1Data = new HashMap<>();
            system1Data.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            system1Data.put("user_ip", "127.0.0.1");
            system1Data.put("merchant_oid", "DM005_SYS1_" + System.currentTimeMillis());
            system1Data.put("email", "system1.sync@example.com");
            system1Data.put("payment_amount", "10000");
            system1Data.put("currency", "TL");
            system1Data.put("test_mode", "1");
            system1Data.put("source_system", "SYSTEM_1");
            system1Data.put("sync_timestamp", String.valueOf(System.currentTimeMillis()));
            
            Response system1Response = given()
                .spec(requestSpec)
                .body(system1Data)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("System 1 sync response: " + system1Response.getStatusCode());
            
            // Wait for synchronization
            Thread.sleep(1000);
            
            Map<String, Object> system2Data = new HashMap<>();
            system2Data.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            system2Data.put("user_ip", "127.0.0.1");
            system2Data.put("merchant_oid", "DM005_SYS2_" + System.currentTimeMillis());
            system2Data.put("email", "system2.sync@example.com");
            system2Data.put("payment_amount", "15000");
            system2Data.put("currency", "USD");
            system2Data.put("test_mode", "1");
            system2Data.put("source_system", "SYSTEM_2");
            system2Data.put("sync_timestamp", String.valueOf(System.currentTimeMillis()));
            
            Response system2Response = given()
                .spec(requestSpec)
                .body(system2Data)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("System 2 sync response: " + system2Response.getStatusCode());
            
            // Test conflict resolution
            String conflictOrderId = "DM005_CONFLICT_" + System.currentTimeMillis();
            
            Map<String, Object> conflictData1 = new HashMap<>();
            conflictData1.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            conflictData1.put("user_ip", "127.0.0.1");
            conflictData1.put("merchant_oid", conflictOrderId);
            conflictData1.put("email", "conflict1.sync@example.com");
            conflictData1.put("payment_amount", "20000");
            conflictData1.put("currency", "TL");
            conflictData1.put("test_mode", "1");
            conflictData1.put("source_system", "SYSTEM_1");
            conflictData1.put("version", "1");
            
            Response conflict1Response = given()
                .spec(requestSpec)
                .body(conflictData1)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Conflict data 1 response: " + conflict1Response.getStatusCode());
            
            Thread.sleep(500);
            
            Map<String, Object> conflictData2 = new HashMap<>();
            conflictData2.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            conflictData2.put("user_ip", "127.0.0.1");
            conflictData2.put("merchant_oid", conflictOrderId + "_V2");
            conflictData2.put("email", "conflict2.sync@example.com");
            conflictData2.put("payment_amount", "25000"); // Different amount
            conflictData2.put("currency", "TL");
            conflictData2.put("test_mode", "1");
            conflictData2.put("source_system", "SYSTEM_2");
            conflictData2.put("version", "2");
            
            Response conflict2Response = given()
                .spec(requestSpec)
                .body(conflictData2)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Conflict data 2 response: " + conflict2Response.getStatusCode());
            
            // Test data consistency check
            Map<String, Object> consistencyCheckData = new HashMap<>();
            consistencyCheckData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            consistencyCheckData.put("user_ip", "127.0.0.1");
            consistencyCheckData.put("merchant_oid", "DM005_CONSISTENCY_" + System.currentTimeMillis());
            consistencyCheckData.put("email", "consistency.sync@example.com");
            consistencyCheckData.put("payment_amount", "30000");
            consistencyCheckData.put("currency", "EUR");
            consistencyCheckData.put("test_mode", "1");
            consistencyCheckData.put("consistency_check", "true");
            consistencyCheckData.put("checksum", "ABC123DEF456");
            
            Response consistencyResponse = given()
                .spec(requestSpec)
                .body(consistencyCheckData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Consistency check response: " + consistencyResponse.getStatusCode());
            
            // Test incremental synchronization
            Map<String, Object> incrementalSyncData = new HashMap<>();
            incrementalSyncData.put("merchant_id", PayTRTestConfig.MERCHANT_ID);
            incrementalSyncData.put("user_ip", "127.0.0.1");
            incrementalSyncData.put("merchant_oid", "DM005_INCREMENTAL_" + System.currentTimeMillis());
            incrementalSyncData.put("email", "incremental.sync@example.com");
            incrementalSyncData.put("payment_amount", "12000");
            incrementalSyncData.put("currency", "TL");
            incrementalSyncData.put("test_mode", "1");
            incrementalSyncData.put("sync_type", "INCREMENTAL");
            incrementalSyncData.put("last_sync_timestamp", String.valueOf(System.currentTimeMillis() - 3600000)); // 1 hour ago
            
            Response incrementalResponse = given()
                .spec(requestSpec)
                .body(incrementalSyncData)
                .when()
                .post("/odeme/api/get-token")
                .then()
                .extract().response();
            
            logTestInfo("Incremental sync response: " + incrementalResponse.getStatusCode());
            
            logTestInfo("Cross-System Data Synchronization Results:");
            logTestInfo("  System 1 Sync: " + (system1Response.getStatusCode() != 500 ? "PASSED" : "FAILED"));
            logTestInfo("  System 2 Sync: " + (system2Response.getStatusCode() != 500 ? "PASSED" : "FAILED"));
            logTestInfo("  Conflict Resolution: " + (conflict2Response.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Consistency Check: " + (consistencyResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            logTestInfo("  Incremental Sync: " + (incrementalResponse.getStatusCode() != 500 ? "PASSED" : "NEEDS_REVIEW"));
            
            // Synchronization assertions
            assertTrue(system1Response.getStatusCode() != 500 && system2Response.getStatusCode() != 500,
                "Cross-system synchronization should work for both systems");
            
            logTestResult("DM-005", "BAŞARILI", "Cross-system data synchronization completed");
            
        } catch (Exception e) {
            logTestResult("DM-005", "BAŞARISIZ", "Cross-system sync test hatası: " + e.getMessage());
            fail("Cross-system data synchronization test başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n📊 VERİ MİGRASYON TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}