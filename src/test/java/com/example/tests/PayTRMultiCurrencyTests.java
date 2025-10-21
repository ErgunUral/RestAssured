package com.example.tests;

import com.example.utils.PayTRTestDataProvider;
import com.example.utils.CurrencyUtils;
import com.example.utils.TestUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PayTR Multi-Currency Test Senaryoları
 * Çoklu para birimi desteği ve döviz kuru işlemlerini test eder
 */
public class PayTRMultiCurrencyTests extends BaseTest {

    @Test(groups = {"multicurrency", "critical", "payment"}, 
          priority = 1,
          description = "MC-001: Real-Time Currency Conversion Testi")
    public void testRealTimeCurrencyConversion() {
        logTestInfo("MC-001: Real-Time Currency Conversion Test");
        
        // Test Data
        String fromCurrency = "USD";
        String toCurrency = "TRY";
        BigDecimal amount = new BigDecimal("100.00");
        
        // Get current exchange rate
        BigDecimal exchangeRate = CurrencyUtils.getCurrentExchangeRate(fromCurrency, toCurrency);
        Assert.assertNotNull(exchangeRate, "Exchange rate should not be null");
        Assert.assertTrue(exchangeRate.compareTo(BigDecimal.ZERO) > 0, "Exchange rate should be positive");
        
        // Calculate expected converted amount
        BigDecimal expectedAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        
        // Perform payment with currency conversion
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": " + amount + ",\n" +
                      "  \"currency\": \"" + fromCurrency + "\",\n" +
                      "  \"targetCurrency\": \"" + toCurrency + "\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\"\n" +
                      "}")
            .when()
                .post("/api/payment/convert")
            .then()
                .statusCode(200)
                .extract().response();
        
        // Verify conversion
        BigDecimal convertedAmount = new BigDecimal(response.jsonPath().getString("convertedAmount"));
        String settlementCurrency = response.jsonPath().getString("settlementCurrency");
        
        Assert.assertEquals(settlementCurrency, toCurrency, "Settlement currency should match target currency");
        Assert.assertTrue(Math.abs(convertedAmount.subtract(expectedAmount).doubleValue()) < 0.1, 
                         "Converted amount should be within acceptable range");
        
        System.out.println("Original Amount: " + amount + " " + fromCurrency);
        System.out.println("Exchange Rate: " + exchangeRate);
        System.out.println("Converted Amount: " + convertedAmount + " " + toCurrency);
    }

    @Test(groups = {"multicurrency", "precision", "medium"}, 
          priority = 2,
          description = "MC-002: Currency Rounding Rules Testi")
    public void testCurrencyRoundingRules() {
        logTestInfo("MC-002: Currency Rounding Rules Test");
        
        // Test different currency precisions
        testCurrencyPrecision("JPY", new BigDecimal("100.567"), 0); // 0 decimal places
        testCurrencyPrecision("EUR", new BigDecimal("100.567"), 2); // 2 decimal places
        testCurrencyPrecision("BHD", new BigDecimal("100.5678"), 3); // 3 decimal places
        testCurrencyPrecision("TRY", new BigDecimal("100.567"), 2); // 2 decimal places
    }
    
    private void testCurrencyPrecision(String currency, BigDecimal amount, int expectedDecimals) {
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": " + amount + ",\n" +
                      "  \"currency\": \"" + currency + "\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\"\n" +
                      "}")
            .when()
                .post("/api/payment")
            .then()
                .statusCode(200)
                .extract().response();
        
        String processedAmount = response.jsonPath().getString("processedAmount");
        BigDecimal roundedAmount = new BigDecimal(processedAmount);
        
        // Verify decimal places
        int actualDecimals = roundedAmount.scale();
        Assert.assertEquals(actualDecimals, expectedDecimals, 
                           "Currency " + currency + " should have " + expectedDecimals + " decimal places");
        
        // Verify rounding is correct
        BigDecimal expectedRounded = amount.setScale(expectedDecimals, RoundingMode.HALF_UP);
        Assert.assertEquals(roundedAmount, expectedRounded, 
                           "Amount should be properly rounded for " + currency);
        
        System.out.println(currency + " - Original: " + amount + ", Rounded: " + roundedAmount);
    }

    @Test(groups = {"multicurrency", "localization", "medium"}, 
          priority = 3,
          description = "MC-003: Multi-Language Payment Flow Testi")
    public void testMultiLanguagePaymentFlow() {
        logTestInfo("MC-003: Multi-Language Payment Flow Test");
        
        // Test English
        testLanguagePaymentFlow("en", "Payment successful", "USD");
        
        // Test Turkish
        testLanguagePaymentFlow("tr", "Ödeme başarılı", "TRY");
        
        // Test German
        testLanguagePaymentFlow("de", "Zahlung erfolgreich", "EUR");
    }
    
    private void testLanguagePaymentFlow(String language, String expectedMessage, String currency) {
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Accept-Language", language)
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"" + currency + "\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\"\n" +
                      "}")
            .when()
                .post("/api/payment")
            .then()
                .statusCode(200)
                .extract().response();
        
        String message = response.jsonPath().getString("message");
        String responseCurrency = response.jsonPath().getString("currency");
        
        Assert.assertTrue(message.contains(expectedMessage) || message.toLowerCase().contains("success"), 
                         "Response should contain success message in " + language);
        Assert.assertEquals(responseCurrency, currency, "Currency should match locale");
        
        System.out.println("Language: " + language + ", Message: " + message + ", Currency: " + currency);
    }

    @Test(groups = {"multicurrency", "rtl", "low"}, 
          priority = 4,
          description = "MC-004: Right-to-Left (RTL) Language Support Testi")
    public void testRTLLanguageSupport() {
        logTestInfo("MC-004: RTL Language Support Test");
        
        // Test Arabic language support
        Response response = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ar")
                .header("Text-Direction", "rtl")
                .body("{\n" +
                      "  \"amount\": 100.00,\n" +
                      "  \"currency\": \"SAR\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\"\n" +
                      "}")
            .when()
                .post("/api/payment")
            .then()
                .statusCode(200)
                .extract().response();
        
        String textDirection = response.jsonPath().getString("textDirection");
        String language = response.jsonPath().getString("language");
        String currency = response.jsonPath().getString("currency");
        
        Assert.assertEquals(textDirection, "rtl", "Text direction should be RTL for Arabic");
        Assert.assertEquals(language, "ar", "Language should be Arabic");
        Assert.assertEquals(currency, "SAR", "Currency should be SAR for Arabic locale");
        
        // Verify number formatting for RTL
        String formattedAmount = response.jsonPath().getString("formattedAmount");
        Assert.assertNotNull(formattedAmount, "Formatted amount should be provided for RTL");
        
        System.out.println("RTL Test - Language: " + language + ", Direction: " + textDirection);
        System.out.println("Formatted Amount: " + formattedAmount);
    }

    @Test(groups = {"multicurrency", "exchange", "high"}, 
          priority = 5,
          description = "MC-005: Exchange Rate Fluctuation Handling Testi")
    public void testExchangeRateFluctuationHandling() {
        logTestInfo("MC-005: Exchange Rate Fluctuation Handling Test");
        
        String fromCurrency = "EUR";
        String toCurrency = "TRY";
        BigDecimal amount = new BigDecimal("50.00");
        
        // Get initial exchange rate
        BigDecimal initialRate = CurrencyUtils.getCurrentExchangeRate(fromCurrency, toCurrency);
        
        // Start payment process
        Response quoteResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"amount\": " + amount + ",\n" +
                      "  \"fromCurrency\": \"" + fromCurrency + "\",\n" +
                      "  \"toCurrency\": \"" + toCurrency + "\"\n" +
                      "}")
            .when()
                .post("/api/payment/quote")
            .then()
                .statusCode(200)
                .extract().response();
        
        String quoteId = quoteResponse.jsonPath().getString("quoteId");
        BigDecimal quotedRate = new BigDecimal(quoteResponse.jsonPath().getString("exchangeRate"));
        long quoteValidUntil = quoteResponse.jsonPath().getLong("validUntil");
        
        Assert.assertNotNull(quoteId, "Quote ID should be provided");
        Assert.assertTrue(quotedRate.compareTo(BigDecimal.ZERO) > 0, "Quoted rate should be positive");
        Assert.assertTrue(quoteValidUntil > System.currentTimeMillis(), "Quote should be valid in future");
        
        // Execute payment with quote
        Response paymentResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"quoteId\": \"" + quoteId + "\",\n" +
                      "  \"cardNumber\": \"4111111111111111\",\n" +
                      "  \"expiryMonth\": \"12\",\n" +
                      "  \"expiryYear\": \"25\",\n" +
                      "  \"cvv\": \"123\"\n" +
                      "}")
            .when()
                .post("/api/payment/execute")
            .then()
                .statusCode(200)
                .extract().response();
        
        BigDecimal executedRate = new BigDecimal(paymentResponse.jsonPath().getString("executedRate"));
        
        // Verify rate consistency
        Assert.assertEquals(executedRate, quotedRate, "Executed rate should match quoted rate");
        
        System.out.println("Initial Rate: " + initialRate);
        System.out.println("Quoted Rate: " + quotedRate);
        System.out.println("Executed Rate: " + executedRate);
    }
}