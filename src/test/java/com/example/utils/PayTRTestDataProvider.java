package com.example.utils;

import com.example.config.PayTRTestConfig;
import org.testng.annotations.DataProvider;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * PayTR Test Veri Sağlayıcısı
 * PayTR testleri için gerekli test verilerini sağlar
 */
public class PayTRTestDataProvider {
    
    /**
     * Geçerli kart bilgileri sağlar
     */
    @DataProvider(name = "validCards")
    public static Object[][] getValidCards() {
        List<Object[]> cards = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, String>> entry : PayTRTestConfig.TEST_CARDS.entrySet()) {
            String cardType = entry.getKey();
            Map<String, String> cardData = entry.getValue();
            
            if ("success".equals(cardData.get("expected_result"))) {
                cards.add(new Object[]{
                    cardData.get("number"),
                    cardData.get("holder"),
                    cardData.get("expiry_month"),
                    cardData.get("expiry_year"),
                    cardData.get("cvv"),
                    cardData.get("type"),
                    cardType
                });
            }
        }
        
        return cards.toArray(new Object[0][]);
    }
    
    /**
     * Geçersiz kart bilgileri sağlar
     */
    @DataProvider(name = "invalidCards")
    public static Object[][] getInvalidCards() {
        return new Object[][] {
            {"1234567890123456", "Invalid Card", "12", "2025", "123", "Geçersiz kart numarası"},
            {"4111111111111112", "Wrong Checksum", "12", "2025", "123", "Yanlış checksum"},
            {"", "Empty Card", "12", "2025", "123", "Boş kart numarası"},
            {"411111111111111", "Short Card", "12", "2025", "123", "Kısa kart numarası"},
            {"41111111111111111111", "Long Card", "12", "2025", "123", "Uzun kart numarası"},
            {"abcd1111efgh2222", "Non-numeric", "12", "2025", "123", "Sayısal olmayan karakter"},
            {"4111 1111 1111 1111", "With Spaces", "12", "2025", "123", "Boşluk içeren kart"},
            {"4111-1111-1111-1111", "With Dashes", "12", "2025", "123", "Tire içeren kart"}
        };
    }
    
    /**
     * Başarısız olması beklenen kart bilgileri sağlar
     */
    @DataProvider(name = "failCards")
    public static Object[][] getFailCards() {
        List<Object[]> cards = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, String>> entry : PayTRTestConfig.TEST_CARDS.entrySet()) {
            String cardType = entry.getKey();
            Map<String, String> cardData = entry.getValue();
            
            if ("fail".equals(cardData.get("expected_result")) || 
                "error".equals(cardData.get("expected_result")) ||
                "expired".equals(cardData.get("expected_result"))) {
                cards.add(new Object[]{
                    cardData.get("number"),
                    cardData.get("holder"),
                    cardData.get("expiry_month"),
                    cardData.get("expiry_year"),
                    cardData.get("cvv"),
                    cardData.get("type"),
                    cardData.get("expected_result")
                });
            }
        }
        
        return cards.toArray(new Object[0][]);
    }
    
    /**
     * Geçerli son kullanma tarihleri sağlar
     */
    @DataProvider(name = "validExpiryDates")
    public static Object[][] getValidExpiryDates() {
        return new Object[][] {
            {"01", "2025", "Ocak 2025"},
            {"06", "2025", "Haziran 2025"},
            {"12", "2025", "Aralık 2025"},
            {"03", "2026", "Mart 2026"},
            {"09", "2027", "Eylül 2027"},
            {"12", "2030", "Aralık 2030"}
        };
    }
    
    /**
     * Geçersiz son kullanma tarihleri sağlar
     */
    @DataProvider(name = "invalidExpiryDates")
    public static Object[][] getInvalidExpiryDates() {
        return new Object[][] {
            {"00", "2025", "Geçersiz ay (00)"},
            {"13", "2025", "Geçersiz ay (13)"},
            {"01", "2020", "Geçmiş yıl"},
            {"12", "2023", "Geçmiş tarih"},
            {"", "2025", "Boş ay"},
            {"01", "", "Boş yıl"},
            {"ab", "2025", "Sayısal olmayan ay"},
            {"01", "abcd", "Sayısal olmayan yıl"},
            {"1", "2025", "Tek haneli ay"},
            {"01", "25", "İki haneli yıl"}
        };
    }
    
    /**
     * Geçerli CVV değerleri sağlar
     */
    @DataProvider(name = "validCVV")
    public static Object[][] getValidCVV() {
        return new Object[][] {
            {"123", "visa", "3 haneli CVV"},
            {"456", "mastercard", "3 haneli CVV"},
            {"1234", "amex", "4 haneli CVV (Amex)"},
            {"000", "visa", "Sıfır CVV"},
            {"999", "mastercard", "Maksimum CVV"}
        };
    }
    
    /**
     * Geçersiz CVV değerleri sağlar
     */
    @DataProvider(name = "invalidCVV")
    public static Object[][] getInvalidCVV() {
        return new Object[][] {
            {"", "visa", "Boş CVV"},
            {"12", "visa", "Kısa CVV"},
            {"12345", "visa", "Uzun CVV"},
            {"abc", "visa", "Sayısal olmayan CVV"},
            {"1a3", "visa", "Karışık CVV"},
            {"123", "amex", "Amex için kısa CVV"},
            {"12345", "amex", "Amex için uzun CVV"}
        };
    }
    
    /**
     * Ödeme tutarları ve para birimleri sağlar
     */
    @DataProvider(name = "paymentAmounts")
    public static Object[][] getPaymentAmounts() {
        List<Object[]> amounts = new ArrayList<>();
        List<String> currencies = PayTRTestConfig.getSupportedCurrencies();
        
        Double[] testAmounts = {1.00, 10.50, 100.00, 999.99, 1500.00};
        
        for (Double amount : testAmounts) {
            for (String currency : currencies) {
                amounts.add(new Object[]{amount, currency, amount + " " + currency});
            }
        }
        
        return amounts.toArray(new Object[0][]);
    }
    
    /**
     * Geçersiz ödeme tutarları sağlar
     */
    @DataProvider(name = "invalidAmounts")
    public static Object[][] getInvalidAmounts() {
        return new Object[][] {
            {0.0, "TRY", "Sıfır tutar"},
            {-10.0, "TRY", "Negatif tutar"},
            {0.5, "TRY", "Minimum altı tutar"},
            {15000.0, "TRY", "Maksimum üstü tutar"},
            {null, "TRY", "Null tutar"}
        };
    }
    
    /**
     * Taksit seçenekleri sağlar
     */
    @DataProvider(name = "installmentOptions")
    public static Object[][] getInstallmentOptions() {
        List<Object[]> installments = new ArrayList<>();
        List<Integer> availableInstallments = PayTRTestConfig.getAvailableInstallments();
        
        for (Integer installment : availableInstallments) {
            Double commission = PayTRTestConfig.getInstallmentCommission(installment);
            Double amount = 100.0 * installment; // Taksit sayısına göre tutar
            
            installments.add(new Object[]{
                amount, 
                installment, 
                commission,
                installment + " taksit, %" + commission + " komisyon"
            });
        }
        
        return installments.toArray(new Object[0][]);
    }
    
    /**
     * Geçersiz taksit seçenekleri sağlar
     */
    @DataProvider(name = "invalidInstallments")
    public static Object[][] getInvalidInstallments() {
        return new Object[][] {
            {50.0, 2, "Minimum tutar altı"},
            {100.0, 0, "Sıfır taksit"},
            {100.0, -1, "Negatif taksit"},
            {100.0, 24, "Maksimum taksit üstü"},
            {100.0, 5, "Desteklenmeyen taksit sayısı"}
        };
    }
    
    /**
     * Kart sahibi isimleri sağlar
     */
    @DataProvider(name = "cardHolderNames")
    public static Object[][] getCardHolderNames() {
        return new Object[][] {
            {"JOHN DOE", "Standart isim"},
            {"JANE SMITH", "Standart isim"},
            {"AHMET YILMAZ", "Türkçe isim"},
            {"MEHMET ALI ÖZKAN", "Üç kelimeli isim"},
            {"MARIA GARCIA-LOPEZ", "Tire içeren isim"},
            {"JEAN-PIERRE MARTIN", "Fransızca isim"},
            {"O'CONNOR PATRICK", "Apostrof içeren isim"},
            {"VAN DER BERG", "Hollandaca isim"},
            {"A", "Tek karakter"},
            {"VERY LONG NAME THAT EXCEEDS NORMAL LENGTH", "Uzun isim"}
        };
    }
    
    /**
     * Geçersiz kart sahibi isimleri sağlar
     */
    @DataProvider(name = "invalidCardHolderNames")
    public static Object[][] getInvalidCardHolderNames() {
        return new Object[][] {
            {"", "Boş isim"},
            {"123456", "Sayısal isim"},
            {"JOHN@DOE", "Özel karakter içeren isim"},
            {"JOHN#DOE", "Hash içeren isim"},
            {"JOHN$DOE", "Dolar işareti içeren isim"},
            {"JOHN%DOE", "Yüzde işareti içeren isim"},
            {"JOHN&DOE", "Ampersand içeren isim"},
            {"JOHN*DOE", "Yıldız içeren isim"},
            {"john doe", "Küçük harf"},
            {"   ", "Sadece boşluk"}
        };
    }
    
    /**
     * Güvenlik test payloadları sağlar
     */
    @DataProvider(name = "securityPayloads")
    public static Object[][] getSecurityPayloads() {
        return new Object[][] {
            {"<script>alert('XSS')</script>", "XSS"},
            {"'; DROP TABLE users; --", "SQL Injection"},
            {"<img src=x onerror=alert('XSS')>", "HTML Injection"},
            {"; rm -rf /", "Command Injection"},
            {"../../../etc/passwd", "Path Traversal"},
            {"*()|&'", "LDAP Injection"},
            {"<?xml version=\"1.0\"?><!DOCTYPE test [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>", "XML Injection"},
            {"{$gt: ''}", "NoSQL Injection"},
            {"test\r\nSet-Cookie: malicious=true", "CRLF Injection"},
            {"test\nX-Malicious: true", "Header Injection"},
            {"javascript:alert('XSS')", "JavaScript Protocol"},
            {"data:text/html,<script>alert('XSS')</script>", "Data URI"},
            {"\\x3cscript\\x3ealert('XSS')\\x3c/script\\x3e", "Encoded XSS"},
            {"eval('alert(\"XSS\")')", "JavaScript Eval"},
            {"${7*7}", "Expression Language Injection"}
        };
    }
    
    /**
     * Kullanıcı giriş bilgileri sağlar
     */
    @DataProvider(name = "loginCredentials")
    public static Object[][] getLoginCredentials() {
        return new Object[][] {
            {PayTRTestConfig.TEST_USERNAME, PayTRTestConfig.TEST_PASSWORD, "success", "Geçerli kullanıcı"},
            {"wronguser", PayTRTestConfig.TEST_PASSWORD, "fail", "Yanlış kullanıcı adı"},
            {PayTRTestConfig.TEST_USERNAME, "wrongpass", "fail", "Yanlış şifre"},
            {"", PayTRTestConfig.TEST_PASSWORD, "fail", "Boş kullanıcı adı"},
            {PayTRTestConfig.TEST_USERNAME, "", "fail", "Boş şifre"},
            {"", "", "fail", "Boş bilgiler"},
            {"admin", "admin", "fail", "Varsayılan admin"},
            {"test", "test", "fail", "Basit test bilgileri"},
            {"user@domain.com", "password123", "fail", "Email formatı"},
            {PayTRTestConfig.TEST_USERNAME.toUpperCase(), PayTRTestConfig.TEST_PASSWORD, "fail", "Büyük harf kullanıcı"}
        };
    }
    
    /**
     * API test verisi sağlar
     */
    @DataProvider(name = "apiTestData")
    public static Object[][] getApiTestData() {
        return new Object[][] {
            {"GET", "/api/payment/status", 200, "Ödeme durumu sorgulama"},
            {"POST", "/api/payment/process", 200, "Ödeme işlemi"},
            {"POST", "/api/auth/login", 200, "Kullanıcı girişi"},
            {"GET", "/api/installment/options", 200, "Taksit seçenekleri"},
            {"POST", "/api/card/validate", 200, "Kart doğrulama"},
            {"GET", "/api/currency/rates", 200, "Döviz kurları"},
            {"POST", "/api/payment/refund", 200, "İade işlemi"},
            {"POST", "/api/payment/cancel", 200, "İptal işlemi"},
            {"GET", "/api/transaction/history", 200, "İşlem geçmişi"},
            {"POST", "/api/pos/virtual", 200, "Sanal POS işlemi"}
        };
    }
    
    /**
     * Browser test konfigürasyonu sağlar
     */
    @DataProvider(name = "browserConfigs")
    public static Object[][] getBrowserConfigs() {
        return new Object[][] {
            {"chrome", "1920x1080", false, "Chrome Desktop"},
            {"chrome", "1366x768", false, "Chrome Laptop"},
            {"chrome", "375x667", true, "Chrome Mobile"},
            {"firefox", "1920x1080", false, "Firefox Desktop"},
            {"firefox", "1366x768", false, "Firefox Laptop"},
            {"edge", "1920x1080", false, "Edge Desktop"}
        };
    }
    
    /**
     * Performance test verisi sağlar
     */
    @DataProvider(name = "performanceTestData")
    public static Object[][] getPerformanceTestData() {
        return new Object[][] {
            {1, 5, "Tek kullanıcı, 5 işlem"},
            {5, 10, "5 kullanıcı, 10 işlem"},
            {10, 20, "10 kullanıcı, 20 işlem"},
            {20, 50, "20 kullanıcı, 50 işlem"},
            {50, 100, "50 kullanıcı, 100 işlem"}
        };
    }
    
    /**
     * Responsive test verisi sağlar
     */
    @DataProvider(name = "responsiveTestData")
    public static Object[][] getResponsiveTestData() {
        return new Object[][] {
            {375, 667, "iPhone SE"},
            {414, 896, "iPhone 11 Pro Max"},
            {768, 1024, "iPad"},
            {1024, 768, "iPad Landscape"},
            {1366, 768, "Laptop"},
            {1920, 1080, "Desktop"},
            {2560, 1440, "Large Desktop"}
        };
    }
    
    /**
     * Localization test verisi sağlar
     */
    @DataProvider(name = "localizationTestData")
    public static Object[][] getLocalizationTestData() {
        return new Object[][] {
            {"tr", "TR", "Türkçe"},
            {"en", "US", "English"},
            {"de", "DE", "Deutsch"},
            {"fr", "FR", "Français"},
            {"es", "ES", "Español"},
            {"it", "IT", "Italiano"},
            {"ru", "RU", "Русский"},
            {"ar", "SA", "العربية"}
        };
    }
}