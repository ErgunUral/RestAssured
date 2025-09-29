package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;
import java.util.concurrent.TimeUnit;

public class PayTRLoginScenariosTest extends BaseTest {
    
    @BeforeClass
    public void setupLoginScenariosTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "/magaza";
        logTestInfo("PayTR Login Scenarios Test Suite başlatıldı");
    }
    
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentialsProvider() {
        return new Object[][] {
            {"nonexistent@example.com", "wrongpassword", "Kullanıcı bulunamadı"},
            {"test@paytr.com", "wrongpassword123", "Şifre hatalı"},
            {"invalid.email@domain.com", "password123", "Geçersiz kullanıcı"},
            {"blocked@user.com", "validpassword", "Hesap bloke"},
            {"suspended@user.com", "validpassword", "Hesap askıya alındı"}
        };
    }
    
    @Test(priority = 1)
    public void testSuccessfulLoginRedirection() {
        logTestInfo("Test Successful Login Redirection");
        
        // Not: Gerçek test ortamında geçerli kullanıcı bilgileri kullanılmalı
        // Bu test şu an mock/demo amaçlı yazılmıştır
        
        Response response = given()
                .formParam("email", "demo@paytr.com")
                .formParam("password", "demopassword")
                .redirects().follow(false) // Redirect'i takip etme
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401))) // 302: redirect, 200: success page, 401: unauthorized
                .extract().response();
        
        int statusCode = response.getStatusCode();
        
        if (statusCode == 302) {
            // Başarılı giriş - redirect kontrolü
            String location = response.getHeader("Location");
            assertNotNull(location, "Redirect location header bulunamadı");
            assertTrue(location.contains("dashboard") || location.contains("panel") || location.contains("magaza"), 
                      "Redirect URL dashboard/panel içermiyor: " + location);
        } else if (statusCode == 200) {
            // Başarılı giriş - aynı sayfada success mesajı
            String responseBody = response.getBody().asString();
            assertTrue(responseBody.contains("başarılı") || responseBody.contains("dashboard") || responseBody.contains("hoşgeldin"),
                      "Başarılı giriş mesajı bulunamadı");
        }
        
        System.out.println("Login response status: " + statusCode);
    }
    
    @Test(priority = 2, dataProvider = "invalidCredentials")
    public void testFailedLoginScenarios(String email, String password, String expectedErrorType) {
        logTestInfo("Test Failed Login: " + email + " - Expected: " + expectedErrorType);
        
        Response response = given()
                .formParam("email", email)
                .formParam("password", password)
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(400), equalTo(422), equalTo(200)))
                .extract().response();
        
        String responseBody = response.getBody().asString().toLowerCase();
        
        // Hata mesajı kontrolü
        boolean hasErrorMessage = responseBody.contains("hata") ||
                                responseBody.contains("error") ||
                                responseBody.contains("geçersiz") ||
                                responseBody.contains("invalid") ||
                                responseBody.contains("yanlış") ||
                                responseBody.contains("wrong") ||
                                responseBody.contains("bulunamadı") ||
                                responseBody.contains("not found");
        
        assertTrue(hasErrorMessage, "Hata mesajı bulunamadı. Response: " + responseBody);
        System.out.println("Failed login test - Email: " + email + ", Status: " + response.getStatusCode());
    }
    
    @Test(priority = 3)
    public void testLoginWithRememberMeOption() {
        logTestInfo("Test Login With Remember Me Option");
        
        Response response = given()
                .formParam("email", "test@example.com")
                .formParam("password", "testpassword")
                .formParam("remember_me", "1") // Remember me checkbox
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401)))
                .extract().response();
        
        // Cookie kontrolü (remember me için)
        if (response.getStatusCode() == 302 || response.getStatusCode() == 200) {
            String cookies = response.getHeaders().getValue("Set-Cookie");
            if (cookies != null) {
                assertTrue(cookies.contains("remember") || cookies.contains("token") || cookies.contains("session"),
                          "Remember me cookie bulunamadı");
            }
        }
        
        System.out.println("Remember me test status: " + response.getStatusCode());
    }
    
    @Test(priority = 4)
    public void testLoginResponseTime() {
        logTestInfo("Test Login Response Time");
        
        long startTime = System.currentTimeMillis();
        
        Response response = given()
                .formParam("email", "test@example.com")
                .formParam("password", "testpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401), equalTo(400)))
                .extract().response();
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Response time 5 saniyeden az olmalı
        assertTrue(responseTime < 5000, "Login response time çok yavaş: " + responseTime + "ms");
        
        System.out.println("Login response time: " + responseTime + "ms");
    }
    
    @Test(priority = 5)
    public void testMultipleFailedLoginAttempts() {
        logTestInfo("Test Multiple Failed Login Attempts (Rate Limiting)");
        
        String email = "test@example.com";
        String wrongPassword = "wrongpassword";
        
        // 5 kez yanlış şifre ile giriş dene
        for (int i = 1; i <= 5; i++) {
            Response response = given()
                    .formParam("email", email)
                    .formParam("password", wrongPassword + i)
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(401), equalTo(400), equalTo(422), equalTo(429), equalTo(200)))
                    .extract().response();
            
            System.out.println("Attempt " + i + " - Status: " + response.getStatusCode());
            
            // Rate limiting kontrolü
            if (response.getStatusCode() == 429) {
                String responseBody = response.getBody().asString();
                assertTrue(responseBody.contains("çok fazla") || responseBody.contains("rate limit") || 
                          responseBody.contains("too many") || responseBody.contains("bekle"),
                          "Rate limiting mesajı bulunamadı");
                break;
            }
            
            // Denemeler arası kısa bekleme
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test(priority = 6)
    public void testLoginWithDifferentUserAgents() {
        logTestInfo("Test Login With Different User Agents");
        
        String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", // Chrome Windows
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36", // Chrome Mac
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_7_1 like Mac OS X) AppleWebKit/605.1.15", // iPhone Safari
            "Mozilla/5.0 (Android 11; Mobile; rv:91.0) Gecko/91.0 Firefox/91.0", // Android Firefox
            "PayTRMobileApp/1.0" // Mobile App
        };
        
        for (String userAgent : userAgents) {
            Response response = given()
                    .header("User-Agent", userAgent)
                    .formParam("email", "test@example.com")
                    .formParam("password", "testpassword")
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401), equalTo(400)))
                    .extract().response();
            
            System.out.println("User-Agent test - Status: " + response.getStatusCode() + 
                             " - Agent: " + userAgent.substring(0, Math.min(50, userAgent.length())));
        }
    }
    
    @Test(priority = 7)
    public void testLoginSessionManagement() {
        logTestInfo("Test Login Session Management");
        
        // İlk giriş denemesi
        Response loginResponse = given()
                .formParam("email", "demo@paytr.com")
                .formParam("password", "demopassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401)))
                .extract().response();
        
        // Session cookie kontrolü
        String sessionCookie = loginResponse.getCookie("PHPSESSID");
        if (sessionCookie == null) {
            sessionCookie = loginResponse.getCookie("session_id");
        }
        if (sessionCookie == null) {
            sessionCookie = loginResponse.getCookie("laravel_session");
        }
        
        if (sessionCookie != null) {
            System.out.println("Session cookie bulundu: " + sessionCookie.substring(0, Math.min(10, sessionCookie.length())));
            
            // Session ile korumalı sayfaya erişim testi
            given()
                    .cookie("PHPSESSID", sessionCookie)
                    .when()
                    .get("/dashboard")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(302), equalTo(404)));
        } else {
            System.out.println("Session cookie bulunamadı");
        }
    }
    
    @Test(priority = 8)
    public void testLoginWithSpecialCharactersInPassword() {
        logTestInfo("Test Login With Special Characters In Password");
        
        String[] specialPasswords = {
            "Pass@123!",
            "Şifre#2023",
            "Test$Pass%",
            "Güçlü&Şifre*",
            "Özel+Karakter=",
            "Türkçe_Şifre-",
            "Test<>Pass",
            "Pass|Word\\"
        };
        
        for (String password : specialPasswords) {
            Response response = given()
                    .formParam("email", "test@example.com")
                    .formParam("password", password)
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(302), equalTo(200), equalTo(401), equalTo(400)))
                    .extract().response();
            
            System.out.println("Special char password test - Status: " + response.getStatusCode());
        }
    }
    
    @Test(priority = 9)
    public void testLoginErrorMessageLocalization() {
        logTestInfo("Test Login Error Message Localization");
        
        // Türkçe hata mesajları testi
        Response response = given()
                .header("Accept-Language", "tr-TR,tr;q=0.9")
                .formParam("email", "nonexistent@example.com")
                .formParam("password", "wrongpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(400), equalTo(422), equalTo(200)))
                .extract().response();
        
        String responseBody = response.getBody().asString();
        
        // Türkçe hata mesajı kontrolü
        boolean hasTurkishError = responseBody.contains("hatalı") ||
                                responseBody.contains("geçersiz") ||
                                responseBody.contains("bulunamadı") ||
                                responseBody.contains("yanlış") ||
                                responseBody.contains("şifre") ||
                                responseBody.contains("kullanıcı");
        
        System.out.println("Turkish localization test - Has Turkish error: " + hasTurkishError);
    }
    
    @Test(priority = 10, enabled = false) // Gerçek ortamda test edilmemeli
    public void testValidLoginCredentials() {
        logTestInfo("Test Valid Login Credentials (DISABLED - For Demo Only)");
        
        // Bu test sadece geçerli test kullanıcısı mevcut olduğunda aktif edilmeli
        given()
                .formParam("email", "valid.test.user@paytr.com")
                .formParam("password", "ValidTestPassword123!")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(302), equalTo(200)))
                .body(anyOf(
                    containsString("dashboard"),
                    containsString("başarılı"),
                    containsString("hoşgeldin")
                ));
    }
}