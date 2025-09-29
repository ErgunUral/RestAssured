package com.example.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class WebController {

    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<String> index() {
        try {
            // HTML dosyasını proje kök dizininden oku
            Path htmlPath = Paths.get("test-scenario-manager.html");
            if (Files.exists(htmlPath)) {
                String content = Files.readString(htmlPath, StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(content);
            } else {
                // Fallback: Basit bir HTML sayfası döndür
                String fallbackHtml = generateFallbackHtml();
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(fallbackHtml);
            }
        } catch (IOException e) {
            String errorHtml = generateErrorHtml(e.getMessage());
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml);
        }
    }

    @GetMapping("/test-scenario-manager.js")
    @ResponseBody
    public ResponseEntity<String> javascript() {
        try {
            // JavaScript dosyasını proje kök dizininden oku
            Path jsPath = Paths.get("test-scenario-manager.js");
            if (Files.exists(jsPath)) {
                String content = Files.readString(jsPath, StandardCharsets.UTF_8);
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf("application/javascript"))
                        .body(content);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateFallbackHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang='tr'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <title>Test Scenario Manager</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n" +
                "        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
                "        .alert { padding: 15px; margin: 20px 0; border-radius: 4px; }\n" +
                "        .alert-info { background: #d1ecf1; border: 1px solid #bee5eb; color: #0c5460; }\n" +
                "        .alert-warning { background: #fff3cd; border: 1px solid #ffeaa7; color: #856404; }\n" +
                "        .btn { padding: 10px 20px; margin: 5px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }\n" +
                "        .btn-primary { background: #007bff; color: white; }\n" +
                "        .btn-success { background: #28a745; color: white; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <h1>🧪 Test Scenario Management System</h1>\n" +
                "        <div class='alert alert-warning'>\n" +
                "            <strong>Uyarı:</strong> Ana HTML dosyası bulunamadı. Lütfen <code>test-scenario-manager.html</code> dosyasının proje kök dizininde olduğundan emin olun.\n" +
                "        </div>\n" +
                "        <div class='alert alert-info'>\n" +
                "            <strong>Sistem Durumu:</strong> Backend API çalışıyor ✅<br>\n" +
                "            <strong>API Endpoint:</strong> <code>/api/test-scenarios</code><br>\n" +
                "            <strong>Beklenen Dosyalar:</strong>\n" +
                "            <ul>\n" +
                "                <li><code>test-scenario-manager.html</code> - Ana arayüz dosyası</li>\n" +
                "                <li><code>test-scenario-manager.js</code> - JavaScript dosyası</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "        <h3>API Test</h3>\n" +
                "        <button class='btn btn-primary' onclick='testAPI()'>API Bağlantısını Test Et</button>\n" +
                "        <div id='apiResult' style='margin-top: 15px;'></div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        async function testAPI() {\n" +
                "            const resultDiv = document.getElementById('apiResult');\n" +
                "            resultDiv.innerHTML = 'Test ediliyor...';\n" +
                "            try {\n" +
                "                const response = await fetch('/api/test-scenarios');\n" +
                "                const data = await response.json();\n" +
                "                resultDiv.innerHTML = '<div class=\"alert alert-info\"><strong>API Testi Başarılı!</strong><br>Yanıt: ' + JSON.stringify(data, null, 2) + '</div>';\n" +
                "            } catch (error) {\n" +
                "                resultDiv.innerHTML = '<div class=\"alert alert-warning\"><strong>API Testi Başarısız:</strong> ' + error.message + '</div>';\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateErrorHtml(String error) {
        return "<!DOCTYPE html>\n" +
                "<html lang='tr'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Hata - Test Scenario Manager</title>\n" +
                "    <style>body { font-family: Arial, sans-serif; margin: 40px; } .error { color: red; }</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Test Scenario Manager</h1>\n" +
                "    <div class='error'>\n" +
                "        <h3>Hata Oluştu:</h3>\n" +
                "        <p>" + error + "</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}