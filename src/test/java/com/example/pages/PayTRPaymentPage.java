package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * PayTR Payment Page Object Model
 * PayTR ödeme sayfası için özel metodları içerir
 */
public class PayTRPaymentPage extends PayTRBasePage {
    
    // Payment form locators
    private static final By CARD_NUMBER_FIELD = By.xpath(
        "//input[contains(@name, 'card') or contains(@id, 'card') or " +
        "contains(@placeholder, 'kart') or contains(@placeholder, 'Card') or " +
        "contains(@name, 'number') or contains(@id, 'number') or " +
        "contains(@autocomplete, 'cc-number')]");
    
    private static final By CARD_HOLDER_NAME_FIELD = By.xpath(
        "//input[contains(@name, 'holder') or contains(@id, 'holder') or " +
        "contains(@name, 'owner') or contains(@id, 'owner') or " +
        "contains(@placeholder, 'Ad') or contains(@placeholder, 'Name') or " +
        "contains(@autocomplete, 'cc-name')]");
    
    private static final By EXPIRY_MONTH_FIELD = By.xpath(
        "//input[contains(@name, 'month') or contains(@id, 'month') or " +
        "contains(@placeholder, 'MM') or contains(@placeholder, 'Ay')] | " +
        "//select[contains(@name, 'month') or contains(@id, 'month')]");
    
    private static final By EXPIRY_YEAR_FIELD = By.xpath(
        "//input[contains(@name, 'year') or contains(@id, 'year') or " +
        "contains(@placeholder, 'YY') or contains(@placeholder, 'Yıl')] | " +
        "//select[contains(@name, 'year') or contains(@id, 'year')]");
    
    private static final By CVV_FIELD = By.xpath(
        "//input[contains(@name, 'cvv') or contains(@id, 'cvv') or " +
        "contains(@name, 'cvc') or contains(@id, 'cvc') or " +
        "contains(@name, 'security') or contains(@id, 'security') or " +
        "contains(@placeholder, 'CVV') or contains(@placeholder, 'CVC')]");
    
    private static final By AMOUNT_FIELD = By.xpath(
        "//input[contains(@name, 'amount') or contains(@id, 'amount') or " +
        "contains(@name, 'tutar') or contains(@id, 'tutar') or " +
        "contains(@placeholder, 'tutar') or contains(@placeholder, 'amount')]");
    
    private static final By CURRENCY_SELECT = By.xpath(
        "//select[contains(@name, 'currency') or contains(@id, 'currency') or " +
        "contains(@name, 'para') or contains(@id, 'para')]");
    
    private static final By INSTALLMENT_SELECT = By.xpath(
        "//select[contains(@name, 'taksit') or contains(@id, 'taksit') or " +
        "contains(@name, 'installment') or contains(@id, 'installment')]");
    
    private static final By INSTALLMENT_RADIO_BUTTONS = By.xpath(
        "//input[@type='radio'][contains(@name, 'taksit') or " +
        "contains(@name, 'installment') or contains(@value, 'taksit')]");
    
    private static final By PAY_BUTTON = By.xpath(
        "//button[contains(text(), 'Öde') or contains(text(), 'Pay') or " +
        "contains(text(), 'ödeme') or contains(text(), 'payment') or " +
        "contains(@value, 'Öde') or contains(@value, 'Pay') or " +
        "contains(@class, 'pay') or contains(@class, 'ödeme')] | " +
        "//input[@type='submit'][contains(@value, 'Öde') or contains(@value, 'Pay')]");
    
    private static final By PAYMENT_FORM = By.xpath(
        "//form[contains(@action, 'payment') or contains(@action, 'ödeme') or " +
        "contains(@class, 'payment') or contains(@class, 'ödeme')]");
    
    private static final By CARD_TYPE_IMAGES = By.xpath(
        "//img[contains(@src, 'visa') or contains(@src, 'mastercard') or " +
        "contains(@src, 'amex') or contains(@alt, 'Visa') or " +
        "contains(@alt, 'MasterCard') or contains(@alt, 'American Express')]");
    
    private static final By SECURE_PAYMENT_INDICATORS = By.xpath(
        "//*[contains(text(), 'güvenli') or contains(text(), 'secure') or " +
        "contains(text(), 'SSL') or contains(@class, 'secure') or " +
        "contains(@class, 'güvenli')]");
    
    private static final By PAYMENT_SUCCESS_MESSAGE = By.xpath(
        "//*[contains(@class, 'success') or contains(@class, 'başarılı')][contains(text(), 'ödeme') or " +
        "contains(text(), 'payment') or contains(text(), 'başarılı') or contains(text(), 'successful')]");
    
    private static final By PAYMENT_ERROR_MESSAGE = By.xpath(
        "//*[contains(@class, 'error') or contains(@class, 'hata')][contains(text(), 'ödeme') or " +
        "contains(text(), 'payment') or contains(text(), 'kart') or contains(text(), 'card')]");
    
    private static final By COMMISSION_INFO = By.xpath(
        "//*[contains(text(), 'komisyon') or contains(text(), 'commission') or " +
        "contains(text(), 'fee') or contains(text(), '%')]");
    
    private static final By TOTAL_AMOUNT_DISPLAY = By.xpath(
        "//*[contains(@class, 'total') or contains(@class, 'toplam') or " +
        "contains(text(), 'Toplam') or contains(text(), 'Total')]");
    
    public PayTRPaymentPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Ödeme sayfasına gider
     */
    public void navigateToPaymentPage() {
        driver.get(BASE_URL);
        waitForPageLoad();
        waitForLoadingToDisappear();
    }
    
    /**
     * Kart numarasını girer
     */
    public void enterCardNumber(String cardNumber) {
        if (isElementPresent(CARD_NUMBER_FIELD)) {
            safeSendKeys(CARD_NUMBER_FIELD, cardNumber);
        } else {
            // Alternatif locator'lar dene
            List<WebElement> inputFields = driver.findElements(By.xpath(
                "//input[@type='text' or @type='tel'][contains(@placeholder, 'kart') or " +
                "contains(@placeholder, 'card') or contains(@maxlength, '16')]"));
            if (!inputFields.isEmpty()) {
                inputFields.get(0).clear();
                inputFields.get(0).sendKeys(cardNumber);
            }
        }
    }
    
    /**
     * Kart sahibi adını girer
     */
    public void enterCardHolderName(String cardHolderName) {
        if (isElementPresent(CARD_HOLDER_NAME_FIELD)) {
            safeSendKeys(CARD_HOLDER_NAME_FIELD, cardHolderName);
        } else {
            // Alternatif locator dene
            List<WebElement> nameFields = driver.findElements(By.xpath(
                "//input[@type='text'][contains(@placeholder, 'ad') or " +
                "contains(@placeholder, 'name')]"));
            if (!nameFields.isEmpty()) {
                nameFields.get(0).clear();
                nameFields.get(0).sendKeys(cardHolderName);
            }
        }
    }
    
    /**
     * Son kullanma tarihini girer (ay)
     */
    public void enterExpiryMonth(String month) {
        if (isElementPresent(EXPIRY_MONTH_FIELD)) {
            WebElement monthElement = driver.findElement(EXPIRY_MONTH_FIELD);
            if (monthElement.getTagName().equals("select")) {
                Select monthSelect = new Select(monthElement);
                monthSelect.selectByValue(month);
            } else {
                safeSendKeys(EXPIRY_MONTH_FIELD, month);
            }
        }
    }
    
    /**
     * Son kullanma tarihini girer (yıl)
     */
    public void enterExpiryYear(String year) {
        if (isElementPresent(EXPIRY_YEAR_FIELD)) {
            WebElement yearElement = driver.findElement(EXPIRY_YEAR_FIELD);
            if (yearElement.getTagName().equals("select")) {
                Select yearSelect = new Select(yearElement);
                yearSelect.selectByValue(year);
            } else {
                safeSendKeys(EXPIRY_YEAR_FIELD, year);
            }
        }
    }
    
    /**
     * CVV kodunu girer
     */
    public void enterCVV(String cvv) {
        if (isElementPresent(CVV_FIELD)) {
            safeSendKeys(CVV_FIELD, cvv);
        } else {
            // Alternatif locator dene
            List<WebElement> securityFields = driver.findElements(By.xpath(
                "//input[@type='password' or @type='text'][contains(@maxlength, '3') or " +
                "contains(@maxlength, '4')]"));
            if (!securityFields.isEmpty()) {
                securityFields.get(0).clear();
                securityFields.get(0).sendKeys(cvv);
            }
        }
    }
    
    /**
     * Ödeme tutarını girer
     */
    public void enterAmount(String amount) {
        if (isElementPresent(AMOUNT_FIELD)) {
            safeSendKeys(AMOUNT_FIELD, amount);
        }
    }
    
    /**
     * Para birimini seçer
     */
    public void selectCurrency(String currency) {
        if (isElementPresent(CURRENCY_SELECT)) {
            Select currencySelect = new Select(driver.findElement(CURRENCY_SELECT));
            currencySelect.selectByValue(currency);
        }
    }
    
    /**
     * Taksit seçeneğini seçer
     */
    public void selectInstallment(String installmentCount) {
        // Select dropdown ile taksit seçimi
        if (isElementPresent(INSTALLMENT_SELECT)) {
            Select installmentSelect = new Select(driver.findElement(INSTALLMENT_SELECT));
            installmentSelect.selectByValue(installmentCount);
            return;
        }
        
        // Radio button ile taksit seçimi
        List<WebElement> radioButtons = driver.findElements(INSTALLMENT_RADIO_BUTTONS);
        for (WebElement radio : radioButtons) {
            String value = radio.getAttribute("value");
            if (value.equals(installmentCount)) {
                radio.click();
                return;
            }
        }
        
        // Button ile taksit seçimi
        List<WebElement> installmentButtons = driver.findElements(By.xpath(
            "//button[contains(text(), '" + installmentCount + "') or " +
            "contains(@data-installment, '" + installmentCount + "')]"));
        if (!installmentButtons.isEmpty()) {
            installmentButtons.get(0).click();
        }
    }
    
    /**
     * Ödeme butonuna tıklar
     */
    public void clickPayButton() {
        if (isElementPresent(PAY_BUTTON)) {
            safeClick(PAY_BUTTON);
        } else {
            // Alternatif submit yöntemleri
            List<WebElement> submitButtons = driver.findElements(By.xpath(
                "//button[@type='submit'] | //input[@type='submit']"));
            if (!submitButtons.isEmpty()) {
                submitButtons.get(0).click();
            } else {
                // Form submit et
                if (isElementPresent(PAYMENT_FORM)) {
                    WebElement form = driver.findElement(PAYMENT_FORM);
                    form.submit();
                }
            }
        }
        waitForLoadingToDisappear();
    }
    
    /**
     * Tam ödeme işlemi yapar
     */
    public void makePayment(String cardNumber, String cardHolderName, String expiryMonth, 
                           String expiryYear, String cvv) {
        enterCardNumber(cardNumber);
        enterCardHolderName(cardHolderName);
        enterExpiryMonth(expiryMonth);
        enterExpiryYear(expiryYear);
        enterCVV(cvv);
        clickPayButton();
    }
    
    /**
     * Taksitli ödeme işlemi yapar
     */
    public void makeInstallmentPayment(String cardNumber, String cardHolderName, String expiryMonth, 
                                     String expiryYear, String cvv, String installmentCount) {
        enterCardNumber(cardNumber);
        enterCardHolderName(cardHolderName);
        enterExpiryMonth(expiryMonth);
        enterExpiryYear(expiryYear);
        enterCVV(cvv);
        selectInstallment(installmentCount);
        clickPayButton();
    }
    
    /**
     * Tutarlı ödeme işlemi yapar
     */
    public void makePaymentWithAmount(String amount, String currency, String cardNumber, 
                                    String cardHolderName, String expiryMonth, String expiryYear, String cvv) {
        enterAmount(amount);
        selectCurrency(currency);
        enterCardNumber(cardNumber);
        enterCardHolderName(cardHolderName);
        enterExpiryMonth(expiryMonth);
        enterExpiryYear(expiryYear);
        enterCVV(cvv);
        clickPayButton();
    }
    
    /**
     * Ödeme formu görünür mü kontrol eder
     */
    public boolean isPaymentFormVisible() {
        return isElementVisible(PAYMENT_FORM) || 
               (isElementPresent(CARD_NUMBER_FIELD) && isElementPresent(CVV_FIELD));
    }
    
    /**
     * Kart numarası alanı görünür mü kontrol eder
     */
    public boolean isCardNumberFieldVisible() {
        return isElementVisible(CARD_NUMBER_FIELD);
    }
    
    /**
     * CVV alanı görünür mü kontrol eder
     */
    public boolean isCVVFieldVisible() {
        return isElementVisible(CVV_FIELD);
    }
    
    /**
     * Taksit seçenekleri görünür mü kontrol eder
     */
    public boolean areInstallmentOptionsVisible() {
        return isElementVisible(INSTALLMENT_SELECT) || 
               !driver.findElements(INSTALLMENT_RADIO_BUTTONS).isEmpty();
    }
    
    /**
     * Kart türü görselleri görünür mü kontrol eder
     */
    public boolean areCardTypeImagesVisible() {
        return isElementVisible(CARD_TYPE_IMAGES);
    }
    
    /**
     * Güvenli ödeme göstergeleri görünür mü kontrol eder
     */
    public boolean areSecurePaymentIndicatorsVisible() {
        return isElementVisible(SECURE_PAYMENT_INDICATORS);
    }
    
    /**
     * Ödeme başarılı mı kontrol eder
     */
    public boolean isPaymentSuccessful() {
        waitFor(3);
        return isElementVisible(PAYMENT_SUCCESS_MESSAGE) || 
               getCurrentUrl().contains("success") || 
               getCurrentUrl().contains("başarılı");
    }
    
    /**
     * Ödeme hatası var mı kontrol eder
     */
    public boolean hasPaymentError() {
        return isElementVisible(PAYMENT_ERROR_MESSAGE);
    }
    
    /**
     * Ödeme hata mesajının metnini alır
     */
    public String getPaymentErrorMessage() {
        if (hasPaymentError()) {
            return getText(PAYMENT_ERROR_MESSAGE);
        }
        return "";
    }
    
    /**
     * Komisyon bilgisi görünür mü kontrol eder
     */
    public boolean isCommissionInfoVisible() {
        return isElementVisible(COMMISSION_INFO);
    }
    
    /**
     * Toplam tutar görünür mü kontrol eder
     */
    public boolean isTotalAmountVisible() {
        return isElementVisible(TOTAL_AMOUNT_DISPLAY);
    }
    
    /**
     * Mevcut taksit seçeneklerini alır
     */
    public List<String> getAvailableInstallmentOptions() {
        List<String> options = new java.util.ArrayList<>();
        
        if (isElementPresent(INSTALLMENT_SELECT)) {
            Select installmentSelect = new Select(driver.findElement(INSTALLMENT_SELECT));
            List<WebElement> optionElements = installmentSelect.getOptions();
            for (WebElement option : optionElements) {
                options.add(option.getText());
            }
        } else {
            List<WebElement> radioButtons = driver.findElements(INSTALLMENT_RADIO_BUTTONS);
            for (WebElement radio : radioButtons) {
                String value = radio.getAttribute("value");
                options.add(value);
            }
        }
        
        return options;
    }
    
    /**
     * Kart numarası alanının maksimum uzunluğunu alır
     */
    public String getCardNumberMaxLength() {
        if (isElementPresent(CARD_NUMBER_FIELD)) {
            return getAttribute(CARD_NUMBER_FIELD, "maxlength");
        }
        return "";
    }
    
    /**
     * CVV alanının maksimum uzunluğunu alır
     */
    public String getCVVMaxLength() {
        if (isElementPresent(CVV_FIELD)) {
            return getAttribute(CVV_FIELD, "maxlength");
        }
        return "";
    }
    
    /**
     * Form validasyonu aktif mi kontrol eder
     */
    public boolean hasPaymentFormValidation() {
        boolean hasRequiredFields = false;
        
        List<By> requiredFieldLocators = List.of(
            CARD_NUMBER_FIELD, CARD_HOLDER_NAME_FIELD, EXPIRY_MONTH_FIELD, 
            EXPIRY_YEAR_FIELD, CVV_FIELD
        );
        
        for (By locator : requiredFieldLocators) {
            if (isElementPresent(locator)) {
                String required = getAttribute(locator, "required");
                if (required != null) {
                    hasRequiredFields = true;
                    break;
                }
            }
        }
        
        return hasRequiredFields || hasFormValidation();
    }
    
    /**
     * Ödeme form elementlerinin sayısını döndürür
     */
    public int getPaymentFormElementCount() {
        int count = 0;
        
        if (isElementPresent(CARD_NUMBER_FIELD)) count++;
        if (isElementPresent(CARD_HOLDER_NAME_FIELD)) count++;
        if (isElementPresent(EXPIRY_MONTH_FIELD)) count++;
        if (isElementPresent(EXPIRY_YEAR_FIELD)) count++;
        if (isElementPresent(CVV_FIELD)) count++;
        if (isElementPresent(AMOUNT_FIELD)) count++;
        if (isElementPresent(CURRENCY_SELECT)) count++;
        if (isElementPresent(INSTALLMENT_SELECT)) count++;
        if (isElementPresent(PAY_BUTTON)) count++;
        
        return count;
    }
    
    /**
     * Ödeme butonu görünür mü kontrol eder
     */
    public boolean isPayButtonVisible() {
        return isElementVisible(PAY_BUTTON);
    }
    
    /**
     * Kart numarası alanının değerini alır
     */
    public String getCardNumberValue() {
        if (isElementPresent(CARD_NUMBER_FIELD)) {
            return getAttribute(CARD_NUMBER_FIELD, "value");
        }
        return "";
    }
    
    /**
     * Güvenli ödeme formu var mı kontrol eder
     */
    public boolean hasSecurePaymentForm() {
        return isSSLActive() && hasCSRFToken() && hasPaymentFormValidation();
    }
    
    /**
     * Kart sahibi adı alanının değerini alır
     */
    public String getCardHolderNameValue() {
        if (isElementPresent(CARD_HOLDER_NAME_FIELD)) {
            return getAttribute(CARD_HOLDER_NAME_FIELD, "value");
        }
        return "";
    }
    
    /**
     * Ödeme sayfasının güvenlik özelliklerini kontrol eder
     */
    public Map<String, Boolean> getSecurityFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        
        features.put("SSL", isSSLActive());
        features.put("CSRF_Token", hasCSRFToken());
        features.put("Form_Validation", hasPaymentFormValidation());
        features.put("Secure_Indicators", areSecurePaymentIndicatorsVisible());
        features.put("Card_Type_Detection", areCardTypeImagesVisible());
        
        return features;
    }
}