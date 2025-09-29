package com.example.api;

import com.example.integration.TestScenarioExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test Scenario REST API Controller
 * Web arayüzünden gelen istekleri karşılar ve test çalıştırma işlemlerini yönetir
 */
@RestController
@RequestMapping("/api/test-scenarios")
@CrossOrigin(origins = "*") // CORS için
public class TestScenarioController {
    
    private static final String SCENARIOS_FILE = "test-scenarios.json";
    private static final String RESULTS_FILE = "test-results.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Aktif test çalıştırma işlemlerini takip etmek için
    private static final Map<String, TestScenarioExecutor> activeExecutors = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<?>> activeTasks = new ConcurrentHashMap<>();
    
    /**
     * Tüm test senaryolarını listeler
     * GET /api/test-scenarios
     */
    @GetMapping
    public ResponseEntity<?> getAllScenarios() {
        try {
            if (Files.exists(Paths.get(SCENARIOS_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(SCENARIOS_FILE)));
                JsonNode scenarios = objectMapper.readTree(content);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", scenarios);
                response.put("count", scenarios.size());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", new ArrayList<>());
                response.put("count", 0);
                response.put("message", "Henüz senaryo bulunamadı");
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return createErrorResponse("Senaryolar yüklenirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Yeni test senaryosu ekler
     * POST /api/test-scenarios
     */
    @PostMapping
    public ResponseEntity<?> addScenario(@RequestBody Map<String, Object> scenarioData) {
        try {
            // Mevcut senaryoları yükle
            List<Map<String, Object>> scenarios = loadScenariosFromFile();
            
            // Yeni ID oluştur
            String newId = generateNewId(scenarios);
            scenarioData.put("id", newId);
            scenarioData.put("createdAt", new Date().toString());
            scenarioData.put("updatedAt", new Date().toString());
            
            // Senaryoyu listeye ekle
            scenarios.add(scenarioData);
            
            // Dosyaya kaydet
            saveScenariosToFile(scenarios);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Senaryo başarıyla eklendi");
            response.put("data", scenarioData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return createErrorResponse("Senaryo eklenirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test senaryosunu günceller
     * PUT /api/test-scenarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateScenario(@PathVariable String id, @RequestBody Map<String, Object> scenarioData) {
        try {
            List<Map<String, Object>> scenarios = loadScenariosFromFile();
            
            // Senaryoyu bul ve güncelle
            boolean found = false;
            for (int i = 0; i < scenarios.size(); i++) {
                Map<String, Object> scenario = scenarios.get(i);
                if (id.equals(scenario.get("id"))) {
                    scenarioData.put("id", id);
                    scenarioData.put("updatedAt", new Date().toString());
                    scenarios.set(i, scenarioData);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return createErrorResponse("Senaryo bulunamadı: " + id);
            }
            
            saveScenariosToFile(scenarios);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Senaryo başarıyla güncellendi");
            response.put("data", scenarioData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Senaryo güncellenirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test senaryosunu siler
     * DELETE /api/test-scenarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScenario(@PathVariable String id) {
        try {
            List<Map<String, Object>> scenarios = loadScenariosFromFile();
            
            // Senaryoyu bul ve sil
            boolean removed = scenarios.removeIf(scenario -> id.equals(scenario.get("id")));
            
            if (!removed) {
                return createErrorResponse("Senaryo bulunamadı: " + id);
            }
            
            saveScenariosToFile(scenarios);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Senaryo başarıyla silindi");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Senaryo silinirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Seçili senaryoları çalıştırır
     * POST /api/test-scenarios/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeScenarios(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> scenarioIds = (List<String>) request.get("scenarioIds");
            
            if (scenarioIds == null || scenarioIds.isEmpty()) {
                return createErrorResponse("Çalıştırılacak senaryo seçilmedi");
            }
            
            // Execution ID oluştur
            String executionId = "exec_" + System.currentTimeMillis();
            
            // Test executor oluştur
            TestScenarioExecutor executor = new TestScenarioExecutor();
            executor.loadScenariosFromFile(SCENARIOS_FILE);
            
            // Aktif executor'ları kaydet
            activeExecutors.put(executionId, executor);
            
            // Asenkron olarak testleri çalıştır
            CompletableFuture<?> task = executor.executeScenarios(scenarioIds)
                .thenAccept(results -> {
                    // Test tamamlandığında cleanup
                    activeExecutors.remove(executionId);
                    activeTasks.remove(executionId);
                    executor.cleanup();
                });
            
            activeTasks.put(executionId, task);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test çalıştırması başlatıldı");
            response.put("executionId", executionId);
            response.put("scenarioCount", scenarioIds.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Test çalıştırılırken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test çalıştırma durumunu kontrol eder
     * GET /api/test-scenarios/execution/{executionId}/status
     */
    @GetMapping("/execution/{executionId}/status")
    public ResponseEntity<?> getExecutionStatus(@PathVariable String executionId) {
        try {
            TestScenarioExecutor executor = activeExecutors.get(executionId);
            CompletableFuture<?> task = activeTasks.get(executionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("executionId", executionId);
            
            if (executor == null || task == null) {
                response.put("status", "completed");
                response.put("running", false);
                response.put("message", "Test çalıştırması tamamlandı veya bulunamadı");
            } else {
                response.put("status", executor.isRunning() ? "running" : "completed");
                response.put("running", executor.isRunning());
                response.put("completed", task.isDone());
                
                // Mevcut sonuçları al
                response.put("currentResults", executor.getResults());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Durum kontrol edilirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test çalıştırmasını durdurur
     * POST /api/test-scenarios/execution/{executionId}/stop
     */
    @PostMapping("/execution/{executionId}/stop")
    public ResponseEntity<?> stopExecution(@PathVariable String executionId) {
        try {
            TestScenarioExecutor executor = activeExecutors.get(executionId);
            CompletableFuture<?> task = activeTasks.get(executionId);
            
            if (executor != null) {
                executor.stopExecution();
            }
            
            if (task != null) {
                task.cancel(true);
            }
            
            // Cleanup
            activeExecutors.remove(executionId);
            activeTasks.remove(executionId);
            
            if (executor != null) {
                executor.cleanup();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test çalıştırması durduruldu");
            response.put("executionId", executionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Test durdurulurken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test sonuçlarını getirir
     * GET /api/test-scenarios/results
     */
    @GetMapping("/results")
    public ResponseEntity<?> getTestResults() {
        try {
            if (Files.exists(Paths.get(RESULTS_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(RESULTS_FILE)));
                JsonNode results = objectMapper.readTree(content);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", results);
                response.put("count", results.size());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", new ArrayList<>());
                response.put("count", 0);
                response.put("message", "Henüz test sonucu bulunamadı");
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return createErrorResponse("Sonuçlar yüklenirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryoları JSON dosyasından içe aktarır
     * POST /api/test-scenarios/import
     */
    @PostMapping("/import")
    public ResponseEntity<?> importScenarios(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return createErrorResponse("Dosya seçilmedi");
            }
            
            String content = new String(file.getBytes());
            JsonNode importedScenarios = objectMapper.readTree(content);
            
            if (!importedScenarios.isArray()) {
                return createErrorResponse("Geçersiz dosya formatı. JSON array bekleniyor.");
            }
            
            List<Map<String, Object>> existingScenarios = loadScenariosFromFile();
            int importedCount = 0;
            
            for (JsonNode scenarioNode : importedScenarios) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scenario = objectMapper.convertValue(scenarioNode, Map.class);
                
                // Yeni ID oluştur
                String newId = generateNewId(existingScenarios);
                scenario.put("id", newId);
                scenario.put("createdAt", new Date().toString());
                scenario.put("updatedAt", new Date().toString());
                
                existingScenarios.add(scenario);
                importedCount++;
            }
            
            saveScenariosToFile(existingScenarios);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", importedCount + " senaryo başarıyla içe aktarıldı");
            response.put("importedCount", importedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("Senaryolar içe aktarılırken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Senaryoları JSON dosyası olarak dışa aktarır
     * GET /api/test-scenarios/export
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportScenarios() {
        try {
            List<Map<String, Object>> scenarios = loadScenariosFromFile();
            
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenarios);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=test-scenarios.json")
                .header("Content-Type", "application/json")
                .body(json);
                
        } catch (Exception e) {
            return createErrorResponse("Senaryolar dışa aktarılırken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * Test istatistiklerini getirir
     * GET /api/test-scenarios/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            List<Map<String, Object>> scenarios = loadScenariosFromFile();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalScenarios", scenarios.size());
            
            // Kategori bazında istatistikler
            Map<String, Long> categoryStats = new HashMap<>();
            Map<String, Long> priorityStats = new HashMap<>();
            
            for (Map<String, Object> scenario : scenarios) {
                String category = (String) scenario.getOrDefault("category", "Diğer");
                String priority = (String) scenario.getOrDefault("priority", "Orta");
                
                categoryStats.put(category, categoryStats.getOrDefault(category, 0L) + 1);
                priorityStats.put(priority, priorityStats.getOrDefault(priority, 0L) + 1);
            }
            
            stats.put("categoryStats", categoryStats);
            stats.put("priorityStats", priorityStats);
            
            // Son test sonuçları
            if (Files.exists(Paths.get(RESULTS_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(RESULTS_FILE)));
                JsonNode results = objectMapper.readTree(content);
                
                long successCount = 0;
                long failedCount = 0;
                
                for (JsonNode result : results) {
                    String status = result.get("status").asText();
                    if ("success".equals(status)) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }
                
                stats.put("lastTestResults", Map.of(
                    "total", results.size(),
                    "success", successCount,
                    "failed", failedCount,
                    "successRate", results.size() > 0 ? (successCount * 100.0 / results.size()) : 0
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse("İstatistikler yüklenirken hata oluştu: " + e.getMessage());
        }
    }
    
    // Yardımcı metodlar
    
    private List<Map<String, Object>> loadScenariosFromFile() throws IOException {
        if (Files.exists(Paths.get(SCENARIOS_FILE))) {
            String content = new String(Files.readAllBytes(Paths.get(SCENARIOS_FILE)));
            JsonNode jsonNode = objectMapper.readTree(content);
            
            if (jsonNode.isArray()) {
                List<Map<String, Object>> scenarios = new ArrayList<>();
                for (JsonNode node : jsonNode) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> scenario = objectMapper.convertValue(node, Map.class);
                    scenarios.add(scenario);
                }
                return scenarios;
            }
        }
        return new ArrayList<>();
    }
    
    private void saveScenariosToFile(List<Map<String, Object>> scenarios) throws IOException {
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenarios);
        Files.write(Paths.get(SCENARIOS_FILE), json.getBytes());
    }
    
    private String generateNewId(List<Map<String, Object>> scenarios) {
        int maxId = 0;
        for (Map<String, Object> scenario : scenarios) {
            try {
                int id = Integer.parseInt(scenario.get("id").toString());
                maxId = Math.max(maxId, id);
            } catch (NumberFormatException e) {
                // ID sayı değilse, timestamp kullan
            }
        }
        return String.valueOf(maxId + 1);
    }
    
    private ResponseEntity<?> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}