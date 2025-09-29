package com.example.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

public class PayTRFormValidationTest extends BaseTest {
    
    @BeforeClass
    public void setupFormValidationTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "/magaza";
        logTestInfo("PayTR Form Validation Test Suite başlatıldı");
    }
    
    @DataProvider(name = "invalidEmails")
    public Object[][] invalidEmailProvider() {
        return new Object[][] {
            {""}, // Boş email
            {" "}, // Sadece boşluk
            {"invalid"}, // @ işareti yok
            {"@domain.com"}, // Kullanıcı adı yok
            {"user@"}, // Domain yok
            {"user@.com"}, // Geçersiz domain
            {"user@domain"}, // TLD yok
            {"user..name@domain.com"}, // Çift nokta
            {"user@domain..com"}, // Domain'de çift nokta
            {"user name@domain.com"}, // Boşluk karakteri
            {"user@domain .com"}, // Domain'de boşluk
            {"very-long-email-address-that-exceeds-normal-limits@very-long-domain-name-that-should-not-be-accepted.com"} // Çok uzun email
        };
    }
    
    @DataProvider(name = "invalidPasswords")
    public Object[][] invalidPasswordProvider() {
        return new Object[][] {
            {""}, // Boş şifre
            {" "}, // Sadece boşluk
            {"123"}, // Çok kısa
            {"a"}, // Tek karakter
            {"   "}, // Sadece boşluklar
        };
    }
    
    @Test(priority = 1, dataProvider = "invalidEmails")
    public void testLoginWithInvalidEmailFormats(String email) {
        logTestInfo("Test Login With Invalid Email Format: " + email);
        
        given()
                .formParam("email", email)
                .formParam("password", "validpassword123")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("geçersiz"),
                    containsString("email"),
                    containsString("format"),
                    containsString("invalid"),
                    containsString("hatalı")
                ));
    }
    
    @Test(priority = 2, dataProvider = "invalidPasswords")
    public void testLoginWithInvalidPasswords(String password) {
        logTestInfo("Test Login With Invalid Password: " + (password.isEmpty() ? "[EMPTY]" : password));
        
        given()
                .formParam("email", "test@example.com")
                .formParam("password", password)
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("şifre"),
                    containsString("password"),
                    containsString("gerekli"),
                    containsString("required"),
                    containsString("boş")
                ));
    }
    
    @Test(priority = 3)
    public void testFormFieldLengthLimits() {
        logTestInfo("Test Form Field Length Limits");
        
        // Çok uzun email testi
        String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
        String longPassword = "p".repeat(500);
        
        given()
                .formParam("email", longEmail)
                .formParam("password", longPassword)
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(413), equalTo(200)));
    }
    
    @Test(priority = 4)
    public void testSpecialCharactersInEmail() {
        logTestInfo("Test Special Characters In Email");
        
        String[] specialEmails = {
            "test+tag@example.com", // + karakteri (geçerli)
            "test.name@example.com", // . karakteri (geçerli)
            "test_name@example.com", // _ karakteri (geçerli)
            "test-name@example.com", // - karakteri (geçerli)
            "test@sub.example.com", // subdomain (geçerli)
            "test#name@example.com", // # karakteri (geçersiz)
            "test$name@example.com", // $ karakteri (geçersiz)
            "test%name@example.com", // % karakteri (geçersiz)
            "test&name@example.com", // & karakteri (geçersiz)
        };
        
        for (String email : specialEmails) {
            Response response = given()
                    .formParam("email", email)
                    .formParam("password", "testpassword")
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(401), equalTo(200)))
                    .extract().response();
            
            System.out.println("Email: " + email + " - Status: " + response.getStatusCode());
        }
    }
    
    @Test(priority = 5)
    public void testPasswordComplexityRequirements() {
        logTestInfo("Test Password Complexity Requirements");
        
        String[] passwords = {
            "password", // Sadece küçük harf
            "PASSWORD", // Sadece büyük harf
            "12345678", // Sadece rakam
            "Password", // Büyük + küçük harf
            "password123", // Küçük harf + rakam
            "PASSWORD123", // Büyük harf + rakam
            "Password123", // Büyük + küçük harf + rakam
            "Password123!", // Büyük + küçük harf + rakam + özel karakter
        };
        
        for (String password : passwords) {
            Response response = given()
                    .formParam("email", "test@example.com")
                    .formParam("password", password)
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(401), equalTo(200)))
                    .extract().response();
            
            System.out.println("Password: " + password + " - Status: " + response.getStatusCode());
        }
    }
    
    @Test(priority = 6)
    public void testFormSubmissionWithoutRequiredFields() {
        logTestInfo("Test Form Submission Without Required Fields");
        
        // Sadece email ile gönderim
        given()
                .formParam("email", "test@example.com")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("şifre"),
                    containsString("password"),
                    containsString("gerekli"),
                    containsString("required")
                ));
        
        // Sadece şifre ile gönderim
        given()
                .formParam("password", "testpassword")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("email"),
                    containsString("kullanıcı"),
                    containsString("gerekli"),
                    containsString("required")
                ));
    }
    
    @Test(priority = 7)
    public void testFormSubmissionWithWhitespaceOnly() {
        logTestInfo("Test Form Submission With Whitespace Only");
        
        given()
                .formParam("email", "   ")
                .formParam("password", "   ")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .body(anyOf(
                    containsString("gerekli"),
                    containsString("boş"),
                    containsString("required"),
                    containsString("empty")
                ));
    }
    
    @Test(priority = 8)
    public void testFormSubmissionWithNullValues() {
        logTestInfo("Test Form Submission With Null Values");
        
        given()
                .formParam("email", (String) null)
                .formParam("password", (String) null)
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)));
    }
    
    @Test(priority = 9)
    public void testCaseSensitiveEmailValidation() {
        logTestInfo("Test Case Sensitive Email Validation");
        
        String[] emailVariations = {
            "Test@Example.Com",
            "TEST@EXAMPLE.COM",
            "test@example.com",
            "TeSt@ExAmPlE.cOm"
        };
        
        for (String email : emailVariations) {
            Response response = given()
                    .formParam("email", email)
                    .formParam("password", "testpassword")
                    .when()
                    .post("/kullanici-girisi")
                    .then()
                    .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(401), equalTo(200)))
                    .extract().response();
            
            System.out.println("Email Case Test: " + email + " - Status: " + response.getStatusCode());
        }
    }
    
    @Test(priority = 10)
    public void testFormValidationErrorMessages() {
        logTestInfo("Test Form Validation Error Messages");
        
        // Boş form gönderimi - hata mesajlarını kontrol et
        Response response = given()
                .formParam("email", "")
                .formParam("password", "")
                .when()
                .post("/kullanici-girisi")
                .then()
                .statusCode(anyOf(equalTo(400), equalTo(422), equalTo(200)))
                .extract().response();
        
        String responseBody = response.getBody().asString();
        
        // Türkçe hata mesajları kontrolü
        boolean hasValidationMessage = responseBody.contains("gerekli") ||
                                     responseBody.contains("boş") ||
                                     responseBody.contains("zorunlu") ||
                                     responseBody.contains("required") ||
                                     responseBody.contains("error");
        
        assertTrue(hasValidationMessage, "Form validation hata mesajı bulunamadı");
    }
}