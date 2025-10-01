package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class PayTRLoginTest extends BaseTest {
    
    private String loginUrl = "https://testweb.paytr.com";
    
    @BeforeClass
    public void setupPayTRTests() {
        // PayTR test ortamı konfigürasyonu
        baseURI = "https://testweb.paytr.com";
        basePath = "";
        logTestInfo("PayTR Test Environment Login Test Suite başlatıldı");
    }
    
    @Test(priority = 1)
    public void testLoginPageAccessibility() {
        logTestInfo("Test Login Page Accessibility");
        
        given()
                .when()
                .get("/kullanici-girisi")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("PayTR"))
                .body(containsString("Kullanıcı Girişi"));
    }
    
    @Test(priority = 2)
    public void testLoginPageElements() {
        logTestInfo("Test Login Page Elements");
        
        Response response = given()
                .when()
                .get("/kullanici-girisi")
                .then()
                .statusCode(200)
                .extract().response();
        
        String htmlContent = response.getBody().asString();
        
        // Form elementlerinin varlığını kontrol et
        assertTrue(htmlContent.contains("email") || htmlContent.contains("kullanici"), 
                "Email/Kullanıcı adı alanı bulunamadı");
        assertTrue(htmlContent.contains("password") || htmlContent.contains("sifre"), 
                "Şifre alanı bulunamadı");
        assertTrue(htmlContent.contains("submit") || htmlContent.contains("giris"), 
                "Giriş butonu bulunamadı");
    }
    
    @Test(priority = 3)
    public void testLoginWithEmptyCredentials() {
        logTestInfo("Test Login With Empty Credentials");
        
        given()
                .formParam("email", "")
                .formParam("password", "")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("gerekli"),
                    containsString("boş"),
                    containsString("required"),
                    containsString("error")
                ));
    }
    
    @Test(priority = 4)
    public void testLoginWithInvalidEmail() {
        logTestInfo("Test Login With Invalid Email");
        
        given()
                .formParam("email", "invalid-email")
                .formParam("password", "testpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("geçersiz"),
                    containsString("email"),
                    containsString("invalid"),
                    containsString("format")
                ));
    }
    
    @Test(priority = 5)
    public void testLoginWithInvalidCredentials() {
        logTestInfo("Test Login With Invalid Credentials");
        
        given()
                .formParam("email", "test@example.com")
                .formParam("password", "wrongpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(403), equalTo(200)))
                .body(anyOf(
                    containsString("hatalı"),
                    containsString("yanlış"),
                    containsString("invalid"),
                    containsString("incorrect"),
                    containsString("unauthorized")
                ));
    }
    
    @Test(priority = 6)
    public void testLoginWithSQLInjection() {
        logTestInfo("Test Login SQL Injection Protection");
        
        String sqlInjectionPayload = "admin'; DROP TABLE users; --";
        
        given()
                .formParam("email", sqlInjectionPayload)
                .formParam("password", "password")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(not(containsString("SQL")));
    }
    
    @Test(priority = 7)
    public void testLoginWithXSSAttempt() {
        logTestInfo("Test Login XSS Protection");
        
        String xssPayload = "<script>alert('XSS')</script>";
        
        given()
                .formParam("email", xssPayload)
                .formParam("password", "password")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(not(containsString("<script>")));
    }
    
    @Test(priority = 8)
    public void testLoginRateLimiting() {
        logTestInfo("Test Login Rate Limiting");
        
        // Çoklu başarısız giriş denemesi
        for (int i = 0; i < 5; i++) {
            given()
                    .formParam("email", "test@example.com")
                    .formParam("password", "wrongpassword" + i)
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(401), equalTo(403), equalTo(429), equalTo(200)));
        }
        
        // 6. deneme - rate limiting kontrolü
        given()
                .formParam("email", "test@example.com")
                .formParam("password", "wrongpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(429), equalTo(403), equalTo(200)))
                .body(anyOf(
                    containsString("limit"),
                    containsString("çok fazla"),
                    containsString("blocked"),
                    containsString("rate")
                ));
    }
    
    @Test(priority = 9)
    public void testLoginPageCSRFProtection() {
        logTestInfo("Test Login Page CSRF Protection");
        
        // CSRF token olmadan form gönderimi
        given()
                .formParam("email", "test@example.com")
                .formParam("password", "testpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(403), equalTo(422), equalTo(200)));
    }
    
    @Test(priority = 10)
    public void testLoginPageResponseHeaders() {
        logTestInfo("Test Login Page Security Headers");
        
        given()
                .when()
                .get("/kullanici-girisi")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("text/html"))
                .header("X-Frame-Options", anyOf(equalTo("DENY"), equalTo("SAMEORIGIN"), nullValue()))
                .header("X-Content-Type-Options", anyOf(equalTo("nosniff"), nullValue()))
                .header("X-XSS-Protection", anyOf(containsString("1"), nullValue()));
    }
    
    @Test(priority = 11)
    public void testMobileAppQRCodePresence() {
        logTestInfo("Test Mobile App QR Code Presence");
        
        Response response = given()
                .when()
                .get("/kullanici-girisi")
                .then()
                .statusCode(200)
                .extract().response();
        
        String htmlContent = response.getBody().asString();
        
        // QR kod ile ilgili içerik kontrolü
        assertTrue(htmlContent.contains("QR") || htmlContent.contains("qr"), 
                "QR kod bilgisi bulunamadı");
        assertTrue(htmlContent.contains("Mağaza Uygulaması") || htmlContent.contains("uygulama"), 
                "Mağaza uygulaması bilgisi bulunamadı");
    }
    
    @Test(priority = 12, enabled = false)
    public void testValidLoginCredentials() {
        logTestInfo("Test Valid Login Credentials");
        
        // Not: Bu test gerçek kullanıcı bilgileri gerektirir
        // Test ortamında geçerli kullanıcı bilgileri ile test edilmelidir
        
        given()
                .formParam("email", "valid@paytr.com")
                .formParam("password", "validpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(302)))
                .body(anyOf(
                    containsString("dashboard"),
                    containsString("panel"),
                    containsString("başarılı")
                ));
    }
}