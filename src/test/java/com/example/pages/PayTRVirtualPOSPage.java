package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * PayTR Virtual POS Page Object Model
 * PayTR sanal POS sayfası için özel metodları içerir
 */
public class PayTRVirtualPOSPage extends PayTRBasePage {
    
    // Virtual POS specific locators
    private static final By VIRTUAL_POS_FORM = By.xpath(
        "//form[contains(@action, 'pos') or contains(@action, 'virtual') or " +
        "contains(@class, 'pos') or contains(@class, 'virtual')]");
    
    private static final By MERCHANT_ID_FIELD = By.xpath(
        "//input[contains(@name, 'merchant') or contains(@id, 'merchant') or " +
        "contains(@name, 'magaza') or contains(@id, 'magaza')]");
    
    private static final By MERCHANT_KEY_FIELD = By.xpath(
        "//input[contains(@name, 'key') or contains(@id, 'key') or " +
        "contains(@name, 'anahtar') or contains(@id, 'anahtar')]");
    
    private static final By MERCHANT_SALT_FIELD = By.xpath(
        "//input[contains(@name, 'salt') or contains(@id, 'salt') or " +
        "contains(@name, 'tuz') or contains(@id, 'tuz')]");
    
    private static final By ORDER_ID_FIELD = By.xpath(
        "//input[contains(@name, 'order') or contains(@id, 'order') or " +
        "contains(@name, 'siparis') or contains(@id, 'siparis')]");
    
    private static final By CUSTOMER_EMAIL_FIELD = By.xpath(
        "//input[contains(@name, 'email') or contains(@id, 'email') or " +
        "contains(@name, 'musteri') or contains(@id, 'musteri')]");
    
    private static final By CUSTOMER_NAME_FIELD = By.xpath(
        "//input[contains(@name, 'name') or contains(@id, 'name') or " +
        "contains(@name, 'ad') or contains(@id, 'ad')]");
    
    private static final By CUSTOMER_PHONE_FIELD = By.xpath(
        "//input[contains(@name, 'phone') or contains(@id, 'phone') or " +
        "contains(@name, 'telefon') or contains(@id, 'telefon')]");
    
    private static final By SUCCESS_URL_FIELD = By.xpath(
        "//input[contains(@name, 'success') or contains(@id, 'success') or " +
        "contains(@name, 'basarili') or contains(@id, 'basarili')]");
    
    private static final By FAIL_URL_FIELD = By.xpath(
        "//input[contains(@name, 'fail') or contains(@id, 'fail') or " +
        "contains(@name, 'hata') or contains(@id, 'hata')]");
    
    private static final By HASH_FIELD = By.xpath(
        "//input[contains(@name, 'hash') or contains(@id, 'hash') or " +
        "contains(@name, 'token') or contains(@id, 'token')]");
    
    private static final By TEST_MODE_CHECKBOX = By.xpath(
        "//input[@type='checkbox'][contains(@name, 'test') or contains(@id, 'test') or " +
        "contains(@name, 'debug') or contains(@id, 'debug')]");
    
    private static final By LANGUAGE_SELECT = By.xpath(
        "//select[contains(@name, 'lang') or contains(@id, 'lang') or " +
        "contains(@name, 'dil') or contains(@id, 'dil')]");
    
    private static final By PAYMENT_TYPE_SELECT = By.xpath(
        "//select[contains(@name, 'type') or contains(@id, 'type') or " +
        "contains(@name, 'tip') or contains(@id, 'tip')]");
    
    private static final By SUBMIT_BUTTON = By.xpath(
        "//button[contains(text(), 'Gönder') or contains(text(), 'Submit') or " +
        "contains(text(), 'Ödeme') or contains(text(), 'Payment') or " +
        "contains(@value, 'Gönder') or contains(@value, 'Submit')] | " +
        "//input[@type='submit']");
    
    private static final By IFRAME_PAYMENT = By.xpath(
        "//iframe[contains(@src, 'paytr') or contains(@src, 'payment') or " +
        "contains(@name, 'paytr') or contains(@id, 'paytr')]");
    
    private static final By PAYMENT_IFRAME_CONTENT = By.xpath(
        "//div[contains(@class, 'iframe') or contains(@class, 'payment-frame')]");
    
    private static final By VIRTUAL_POS_STATUS = By.xpath(
        "//*[contains(@class, 'status') or contains(@class, 'durum')][contains(text(), 'pos') or " +
        "contains(text(), 'POS') or contains(text(), 'virtual')]");
    
    private static final By TRANSACTION_ID_DISPLAY = By.xpath(
        "//*[contains(@class, 'transaction') or contains(@class, 'islem')][contains(text(), 'ID') or " +
        "contains(text(), 'id') or contains(text(), 'No')]");
    
    private static final By BANK_RESPONSE_MESSAGE = By.xpath(
        "//*[contains(@class, 'bank') or contains(@class, 'banka')][contains(text(), 'response') or " +
        "contains(text(), 'yanit') or contains(text(), 'cevap')]");
    
    private static final By POS_ERROR_MESSAGE = By.xpath(
        "//*[contains(@class, 'error') or contains(@class, 'hata')][contains(text(), 'pos') or " +
        "contains(text(), 'POS') or contains(text(), 'banka') or contains(text(), 'bank')]");
    
    private static final By SECURITY_CHECK_INDICATOR = By.xpath(
        "//*[contains(text(), '3D') or contains(text(), 'güvenlik') or " +
        "contains(text(), 'security') or contains(text(), 'doğrulama')]");
    
    public PayTRVirtualPOSPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Virtual POS sayfasına gider
     */
    public void navigateToVirtualPOSPage() {
        driver.get(BASE_URL);
        waitForPageLoad();
        waitForLoadingToDisappear();
    }
    
    /**
     * Mağaza ID'sini girer
     */
    public void enterMerchantId(String merchantId) {
        if (isElementPresent(MERCHANT_ID_FIELD)) {
            safeSendKeys(MERCHANT_ID_FIELD, merchantId);
        }
    }
    
    /**
     * Mağaza anahtarını girer
     */
    public void enterMerchantKey(String merchantKey) {
        if (isElementPresent(MERCHANT_KEY_FIELD)) {
            safeSendKeys(MERCHANT_KEY_FIELD, merchantKey);
        }
    }
    
    /**
     * Mağaza salt değerini girer
     */
    public void enterMerchantSalt(String merchantSalt) {
        if (isElementPresent(MERCHANT_SALT_FIELD)) {
            safeSendKeys(MERCHANT_SALT_FIELD, merchantSalt);
        }
    }
    
    /**
     * Sipariş ID'sini girer
     */
    public void enterOrderId(String orderId) {
        if (isElementPresent(ORDER_ID_FIELD)) {
            safeSendKeys(ORDER_ID_FIELD, orderId);
        }
    }
    
    /**
     * Müşteri email'ini girer
     */
    public void enterCustomerEmail(String email) {
        if (isElementPresent(CUSTOMER_EMAIL_FIELD)) {
            safeSendKeys(CUSTOMER_EMAIL_FIELD, email);
        }
    }
    
    /**
     * Müşteri adını girer
     */
    public void enterCustomerName(String name) {
        if (isElementPresent(CUSTOMER_NAME_FIELD)) {
            safeSendKeys(CUSTOMER_NAME_FIELD, name);
        }
    }
    
    /**
     * Müşteri telefonunu girer
     */
    public void enterCustomerPhone(String phone) {
        if (isElementPresent(CUSTOMER_PHONE_FIELD)) {
            safeSendKeys(CUSTOMER_PHONE_FIELD, phone);
        }
    }
    
    /**
     * Başarı URL'sini girer
     */
    public void enterSuccessUrl(String successUrl) {
        if (isElementPresent(SUCCESS_URL_FIELD)) {
            safeSendKeys(SUCCESS_URL_FIELD, successUrl);
        }
    }
    
    /**
     * Hata URL'sini girer
     */
    public void enterFailUrl(String failUrl) {
        if (isElementPresent(FAIL_URL_FIELD)) {
            safeSendKeys(FAIL_URL_FIELD, failUrl);
        }
    }
    
    /**
     * Hash değerini girer
     */
    public void enterHash(String hash) {
        if (isElementPresent(HASH_FIELD)) {
            safeSendKeys(HASH_FIELD, hash);
        }
    }
    
    /**
     * Test modunu aktif eder
     */
    public void enableTestMode() {
        if (isElementPresent(TEST_MODE_CHECKBOX)) {
            WebElement checkbox = driver.findElement(TEST_MODE_CHECKBOX);
            if (!checkbox.isSelected()) {
                safeClick(TEST_MODE_CHECKBOX);
            }
        }
    }
    
    /**
     * Dil seçimi yapar
     */
    public void selectLanguage(String language) {
        if (isElementPresent(LANGUAGE_SELECT)) {
            Select languageSelect = new Select(driver.findElement(LANGUAGE_SELECT));
            languageSelect.selectByValue(language);
        }
    }
    
    /**
     * Ödeme tipini seçer
     */
    public void selectPaymentType(String paymentType) {
        if (isElementPresent(PAYMENT_TYPE_SELECT)) {
            Select paymentTypeSelect = new Select(driver.findElement(PAYMENT_TYPE_SELECT));
            paymentTypeSelect.selectByValue(paymentType);
        }
    }
    
    /**
     * Virtual POS formunu gönderir
     */
    public void submitVirtualPOSForm() {
        if (isElementPresent(SUBMIT_BUTTON)) {
            safeClick(SUBMIT_BUTTON);
        } else {
            // Alternatif submit yöntemleri
            if (isElementPresent(VIRTUAL_POS_FORM)) {
                WebElement form = driver.findElement(VIRTUAL_POS_FORM);
                form.submit();
            }
        }
        waitForLoadingToDisappear();
    }
    
    /**
     * Tam Virtual POS işlemi yapar
     */
    public void processVirtualPOSPayment(String merchantId, String merchantKey, String merchantSalt,
                                       String orderId, String amount, String customerEmail, 
                                       String customerName, String successUrl, String failUrl) {
        enterMerchantId(merchantId);
        enterMerchantKey(merchantKey);
        enterMerchantSalt(merchantSalt);
        enterOrderId(orderId);
        enterCustomerEmail(customerEmail);
        enterCustomerName(customerName);
        enterSuccessUrl(successUrl);
        enterFailUrl(failUrl);
        enableTestMode();
        submitVirtualPOSForm();
    }
    
    /**
     * Virtual POS formu görünür mü kontrol eder
     */
    public boolean isVirtualPOSFormVisible() {
        return isElementVisible(VIRTUAL_POS_FORM) || 
               (isElementPresent(MERCHANT_ID_FIELD) && isElementPresent(SUBMIT_BUTTON));
    }
    
    /**
     * Mağaza bilgileri alanları görünür mü kontrol eder
     */
    public boolean areMerchantFieldsVisible() {
        return isElementVisible(MERCHANT_ID_FIELD) && 
               isElementVisible(MERCHANT_KEY_FIELD) && 
               isElementVisible(MERCHANT_SALT_FIELD);
    }
    
    /**
     * Müşteri bilgileri alanları görünür mü kontrol eder
     */
    public boolean areCustomerFieldsVisible() {
        return isElementVisible(CUSTOMER_EMAIL_FIELD) || 
               isElementVisible(CUSTOMER_NAME_FIELD) || 
               isElementVisible(CUSTOMER_PHONE_FIELD);
    }
    
    /**
     * URL alanları görünür mü kontrol eder
     */
    public boolean areUrlFieldsVisible() {
        return isElementVisible(SUCCESS_URL_FIELD) && isElementVisible(FAIL_URL_FIELD);
    }
    
    /**
     * Test modu aktif mi kontrol eder
     */
    public boolean isTestModeEnabled() {
        if (isElementPresent(TEST_MODE_CHECKBOX)) {
            WebElement checkbox = driver.findElement(TEST_MODE_CHECKBOX);
            return checkbox.isSelected();
        }
        return false;
    }
    
    /**
     * Ödeme iframe'i görünür mü kontrol eder
     */
    public boolean isPaymentIframeVisible() {
        return isElementVisible(IFRAME_PAYMENT) || isElementVisible(PAYMENT_IFRAME_CONTENT);
    }
    
    /**
     * Virtual POS durumu görünür mü kontrol eder
     */
    public boolean isVirtualPOSStatusVisible() {
        return isElementVisible(VIRTUAL_POS_STATUS);
    }
    
    /**
     * İşlem ID'si görünür mü kontrol eder
     */
    public boolean isTransactionIdVisible() {
        return isElementVisible(TRANSACTION_ID_DISPLAY);
    }
    
    /**
     * Banka yanıtı görünür mü kontrol eder
     */
    public boolean isBankResponseVisible() {
        return isElementVisible(BANK_RESPONSE_MESSAGE);
    }
    
    /**
     * POS hatası var mı kontrol eder
     */
    public boolean hasPOSError() {
        return isElementVisible(POS_ERROR_MESSAGE);
    }
    
    /**
     * Güvenlik kontrolü aktif mi kontrol eder
     */
    public boolean isSecurityCheckActive() {
        return isElementVisible(SECURITY_CHECK_INDICATOR);
    }
    
    /**
     * POS hata mesajının metnini alır
     */
    public String getPOSErrorMessage() {
        if (hasPOSError()) {
            return getText(POS_ERROR_MESSAGE);
        }
        return "";
    }
    
    /**
     * Virtual POS durumunu alır
     */
    public String getVirtualPOSStatus() {
        if (isVirtualPOSStatusVisible()) {
            return getText(VIRTUAL_POS_STATUS);
        }
        return "";
    }
    
    /**
     * İşlem ID'sini alır
     */
    public String getTransactionId() {
        if (isTransactionIdVisible()) {
            return getText(TRANSACTION_ID_DISPLAY);
        }
        return "";
    }
    
    /**
     * Banka yanıtını alır
     */
    public String getBankResponse() {
        if (isBankResponseVisible()) {
            return getText(BANK_RESPONSE_MESSAGE);
        }
        return "";
    }
    
    /**
     * Mevcut dil seçeneklerini alır
     */
    public List<String> getAvailableLanguages() {
        List<String> languages = new java.util.ArrayList<>();
        
        if (isElementPresent(LANGUAGE_SELECT)) {
            Select languageSelect = new Select(driver.findElement(LANGUAGE_SELECT));
            List<WebElement> options = languageSelect.getOptions();
            for (WebElement option : options) {
                languages.add(option.getText());
            }
        }
        
        return languages;
    }
    
    /**
     * Mevcut ödeme tiplerini alır
     */
    public List<String> getAvailablePaymentTypes() {
        List<String> paymentTypes = new java.util.ArrayList<>();
        
        if (isElementPresent(PAYMENT_TYPE_SELECT)) {
            Select paymentTypeSelect = new Select(driver.findElement(PAYMENT_TYPE_SELECT));
            List<WebElement> options = paymentTypeSelect.getOptions();
            for (WebElement option : options) {
                paymentTypes.add(option.getText());
            }
        }
        
        return paymentTypes;
    }
    
    /**
     * Virtual POS form alanlarının required attribute'unu kontrol eder
     */
    public boolean areVirtualPOSFieldsRequired() {
        List<By> requiredFieldLocators = List.of(
            MERCHANT_ID_FIELD, MERCHANT_KEY_FIELD, MERCHANT_SALT_FIELD,
            ORDER_ID_FIELD, CUSTOMER_EMAIL_FIELD
        );
        
        int requiredCount = 0;
        for (By locator : requiredFieldLocators) {
            if (isElementPresent(locator)) {
                String required = getAttribute(locator, "required");
                if (required != null) {
                    requiredCount++;
                }
            }
        }
        
        return requiredCount >= 3; // En az 3 alan required olmalı
    }
    
    /**
     * Virtual POS form elementlerinin sayısını döndürür
     */
    public int getVirtualPOSFormElementCount() {
        int count = 0;
        
        if (isElementPresent(MERCHANT_ID_FIELD)) count++;
        if (isElementPresent(MERCHANT_KEY_FIELD)) count++;
        if (isElementPresent(MERCHANT_SALT_FIELD)) count++;
        if (isElementPresent(ORDER_ID_FIELD)) count++;
        if (isElementPresent(CUSTOMER_EMAIL_FIELD)) count++;
        if (isElementPresent(CUSTOMER_NAME_FIELD)) count++;
        if (isElementPresent(CUSTOMER_PHONE_FIELD)) count++;
        if (isElementPresent(SUCCESS_URL_FIELD)) count++;
        if (isElementPresent(FAIL_URL_FIELD)) count++;
        if (isElementPresent(HASH_FIELD)) count++;
        if (isElementPresent(TEST_MODE_CHECKBOX)) count++;
        if (isElementPresent(LANGUAGE_SELECT)) count++;
        if (isElementPresent(PAYMENT_TYPE_SELECT)) count++;
        if (isElementPresent(SUBMIT_BUTTON)) count++;
        
        return count;
    }
    
    /**
     * Virtual POS güvenlik özelliklerini kontrol eder
     */
    public Map<String, Boolean> getVirtualPOSSecurityFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        
        features.put("SSL", isSSLActive());
        features.put("CSRF_Token", hasCSRFToken());
        features.put("Form_Validation", areVirtualPOSFieldsRequired());
        features.put("Test_Mode", isTestModeEnabled());
        features.put("Security_Check", isSecurityCheckActive());
        features.put("Hash_Field", isElementPresent(HASH_FIELD));
        
        return features;
    }
    
    /**
     * Iframe'e geçiş yapar
     */
    public void switchToPaymentIframe() {
        if (isElementPresent(IFRAME_PAYMENT)) {
            WebElement iframe = driver.findElement(IFRAME_PAYMENT);
            driver.switchTo().frame(iframe);
        }
    }
    
    /**
     * Ana frame'e geri döner
     */
    public void switchToMainFrame() {
        driver.switchTo().defaultContent();
    }
    
    /**
     * Virtual POS işlem sonucunu kontrol eder
     */
    public boolean isVirtualPOSTransactionSuccessful() {
        waitFor(3);
        
        // URL kontrolü
        String currentUrl = getCurrentUrl();
        if (currentUrl.contains("success") || currentUrl.contains("başarılı")) {
            return true;
        }
        
        // Başarı mesajı kontrolü
        if (hasSuccessMessage()) {
            return true;
        }
        
        // İşlem ID varlığı kontrolü
        if (isTransactionIdVisible()) {
            return true;
        }
        
        // Hata yoksa başarılı kabul et
        return !hasPOSError() && !hasErrorMessage();
    }
}