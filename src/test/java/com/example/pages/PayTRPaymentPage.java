package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import io.qameta.allure.Step;
import java.time.Duration;
import java.util.List;

/**
 * PayTR Payment Page Object Model
 * Bu sınıf PayTR ödeme sayfasının elementlerini ve işlemlerini yönetir
 */
public class PayTRPaymentPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // Page URL
    private static final String PAYMENT_URL = "https://www.paytr.com";
    
    // Payment Form Elements
    @FindBy(xpath = "//input[@name='card_number' or @id='card_number' or contains(@placeholder, 'Kart') or @type='tel']")
    private WebElement cardNumberField;
    
    @FindBy(xpath = "//input[@name='expiry' or @id='expiry' or contains(@placeholder, 'MM/YY')]")
    private WebElement expiryDateField;
    
    @FindBy(xpath = "//input[@name='cvv' or @id='cvv' or contains(@placeholder, 'CVV')]")
    private WebElement cvvField;
    
    @FindBy(xpath = "//input[@name='cardholder' or @id='cardholder' or contains(@placeholder, 'Kart Sahibi')]")
    private WebElement cardHolderField;
    
    @FindBy(xpath = "//input[@name='amount' or @id='amount' or contains(@placeholder, 'Tutar')]")
    private WebElement amountField;
    
    @FindBy(xpath = "//select[@name='currency' or @id='currency']")
    private WebElement currencySelect;
    
    @FindBy(xpath = "//button[@type='submit' or contains(@class, 'pay') or contains(text(), 'Öde')]")
    private WebElement payButton;
    
    // Customer Information Elements
    @FindBy(xpath = "//input[@name='customer_name' or @id='customer_name']")
    private WebElement customerNameField;
    
    @FindBy(xpath = "//input[@name='customer_email' or @id='customer_email']")
    private WebElement customerEmailField;
    
    @FindBy(xpath = "//input[@name='customer_phone' or @id='customer_phone']")
    private WebElement customerPhoneField;
    
    @FindBy(xpath = "//textarea[@name='customer_address' or @id='customer_address']")
    private WebElement customerAddressField;
    
    // Payment Method Elements
    @FindBy(xpath = "//input[@value='credit_card' or @id='credit_card']")
    private WebElement creditCardOption;
    
    @FindBy(xpath = "//input[@value='bank_transfer' or @id='bank_transfer']")
    private WebElement bankTransferOption;
    
    @FindBy(xpath = "//input[@value='digital_wallet' or @id='digital_wallet']")
    private WebElement digitalWalletOption;
    
    // Security Elements
    @FindBy(xpath = "//input[@name='_token' or @name='csrf_token']")
    private WebElement csrfTokenField;
    
    @FindBy(xpath = "//*[contains(@class, 'ssl') or contains(text(), 'Güvenli')]")
    private WebElement sslIndicator;
    
    @FindBy(xpath = "//*[contains(@class, 'security') or contains(text(), 'Güvenlik')]")
    private WebElement securityBadge;
    
    // Error and Success Messages
    @FindBy(xpath = "//*[contains(@class, 'error') or contains(@class, 'alert-danger')]")
    private WebElement errorMessage;
    
    @FindBy(xpath = "//*[contains(@class, 'success') or contains(@class, 'alert-success')]")
    private WebElement successMessage;
    
    @FindBy(xpath = "//*[contains(@class, 'warning') or contains(@class, 'alert-warning')]")
    private WebElement warningMessage;
    
    // Loading and Progress Elements
    @FindBy(xpath = "//*[contains(@class, 'loading') or contains(@class, 'spinner')]")
    private WebElement loadingIndicator;
    
    @FindBy(xpath = "//*[contains(@class, 'progress')]")
    private WebElement progressBar;
    
    // Constructor
    public PayTRPaymentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }
    
    // Navigation Methods
    
    @Step("PayTR ödeme sayfasına git")
    public PayTRPaymentPage navigateToPaymentPage() {
        driver.get(PAYMENT_URL);
        waitForPageToLoad();
        return this;
    }
    
    @Step("Sayfa yüklenene kadar bekle")
    public PayTRPaymentPage waitForPageToLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        return this;
    }
    
    // Card Information Methods
    
    @Step("Kart numarasını gir: {cardNumber}")
    public PayTRPaymentPage enterCardNumber(String cardNumber) {
        if (isCardNumberFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(cardNumberField));
            cardNumberField.clear();
            cardNumberField.sendKeys(cardNumber);
        }
        return this;
    }
    
    @Step("Son kullanma tarihini gir: {expiryDate}")
    public PayTRPaymentPage enterExpiryDate(String expiryDate) {
        if (isExpiryDateFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(expiryDateField));
            expiryDateField.clear();
            expiryDateField.sendKeys(expiryDate);
        }
        return this;
    }
    
    @Step("CVV kodunu gir")
    public PayTRPaymentPage enterCVV(String cvv) {
        if (isCvvFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(cvvField));
            cvvField.clear();
            cvvField.sendKeys(cvv);
        }
        return this;
    }
    
    @Step("Kart sahibi adını gir: {cardHolder}")
    public PayTRPaymentPage enterCardHolder(String cardHolder) {
        if (isCardHolderFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(cardHolderField));
            cardHolderField.clear();
            cardHolderField.sendKeys(cardHolder);
        }
        return this;
    }
    
    // Payment Amount Methods
    
    @Step("Ödeme tutarını gir: {amount}")
    public PayTRPaymentPage enterAmount(String amount) {
        if (isAmountFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(amountField));
            amountField.clear();
            amountField.sendKeys(amount);
        }
        return this;
    }
    
    @Step("Para birimini seç: {currency}")
    public PayTRPaymentPage selectCurrency(String currency) {
        if (isCurrencySelectPresent()) {
            Select currencyDropdown = new Select(currencySelect);
            currencyDropdown.selectByValue(currency);
        }
        return this;
    }
    
    // Customer Information Methods
    
    @Step("Müşteri adını gir: {customerName}")
    public PayTRPaymentPage enterCustomerName(String customerName) {
        if (isCustomerNameFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(customerNameField));
            customerNameField.clear();
            customerNameField.sendKeys(customerName);
        }
        return this;
    }
    
    @Step("Müşteri email'ini gir: {customerEmail}")
    public PayTRPaymentPage enterCustomerEmail(String customerEmail) {
        if (isCustomerEmailFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(customerEmailField));
            customerEmailField.clear();
            customerEmailField.sendKeys(customerEmail);
        }
        return this;
    }
    
    @Step("Müşteri telefonunu gir: {customerPhone}")
    public PayTRPaymentPage enterCustomerPhone(String customerPhone) {
        if (isCustomerPhoneFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(customerPhoneField));
            customerPhoneField.clear();
            customerPhoneField.sendKeys(customerPhone);
        }
        return this;
    }
    
    @Step("Müşteri adresini gir: {customerAddress}")
    public PayTRPaymentPage enterCustomerAddress(String customerAddress) {
        if (isCustomerAddressFieldPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(customerAddressField));
            customerAddressField.clear();
            customerAddressField.sendKeys(customerAddress);
        }
        return this;
    }
    
    // Payment Method Selection
    
    @Step("Kredi kartı ödeme yöntemini seç")
    public PayTRPaymentPage selectCreditCardPayment() {
        if (isCreditCardOptionPresent()) {
            creditCardOption.click();
        }
        return this;
    }
    
    @Step("Banka havalesi ödeme yöntemini seç")
    public PayTRPaymentPage selectBankTransferPayment() {
        if (isBankTransferOptionPresent()) {
            bankTransferOption.click();
        }
        return this;
    }
    
    @Step("Dijital cüzdan ödeme yöntemini seç")
    public PayTRPaymentPage selectDigitalWalletPayment() {
        if (isDigitalWalletOptionPresent()) {
            digitalWalletOption.click();
        }
        return this;
    }
    
    // Payment Action
    
    @Step("Ödeme butonuna tıkla")
    public PayTRPaymentPage clickPayButton() {
        if (isPayButtonPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(payButton));
            payButton.click();
        }
        return this;
    }
    
    @Step("Tam ödeme işlemini gerçekleştir")
    public PayTRPaymentPage performPayment(String cardNumber, String expiryDate, String cvv, 
                                          String cardHolder, String amount) {
        enterCardNumber(cardNumber);
        enterExpiryDate(expiryDate);
        enterCVV(cvv);
        enterCardHolder(cardHolder);
        enterAmount(amount);
        clickPayButton();
        return this;
    }
    
    // Verification Methods
    
    @Step("Kart numarası alanının mevcut olduğunu doğrula")
    public boolean isCardNumberFieldPresent() {
        try {
            return cardNumberField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Son kullanma tarihi alanının mevcut olduğunu doğrula")
    public boolean isExpiryDateFieldPresent() {
        try {
            return expiryDateField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("CVV alanının mevcut olduğunu doğrula")
    public boolean isCvvFieldPresent() {
        try {
            return cvvField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Kart sahibi alanının mevcut olduğunu doğrula")
    public boolean isCardHolderFieldPresent() {
        try {
            return cardHolderField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Tutar alanının mevcut olduğunu doğrula")
    public boolean isAmountFieldPresent() {
        try {
            return amountField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Para birimi seçiminin mevcut olduğunu doğrula")
    public boolean isCurrencySelectPresent() {
        try {
            return currencySelect.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Ödeme butonunun mevcut olduğunu doğrula")
    public boolean isPayButtonPresent() {
        try {
            return payButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Müşteri adı alanının mevcut olduğunu doğrula")
    public boolean isCustomerNameFieldPresent() {
        try {
            return customerNameField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Müşteri email alanının mevcut olduğunu doğrula")
    public boolean isCustomerEmailFieldPresent() {
        try {
            return customerEmailField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Müşteri telefon alanının mevcut olduğunu doğrula")
    public boolean isCustomerPhoneFieldPresent() {
        try {
            return customerPhoneField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Müşteri adres alanının mevcut olduğunu doğrula")
    public boolean isCustomerAddressFieldPresent() {
        try {
            return customerAddressField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Kredi kartı seçeneğinin mevcut olduğunu doğrula")
    public boolean isCreditCardOptionPresent() {
        try {
            return creditCardOption.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Banka havalesi seçeneğinin mevcut olduğunu doğrula")
    public boolean isBankTransferOptionPresent() {
        try {
            return bankTransferOption.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Dijital cüzdan seçeneğinin mevcut olduğunu doğrula")
    public boolean isDigitalWalletOptionPresent() {
        try {
            return digitalWalletOption.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("CSRF token'ının mevcut olduğunu doğrula")
    public boolean isCSRFTokenPresent() {
        try {
            return csrfTokenField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("SSL göstergesinin mevcut olduğunu doğrula")
    public boolean isSSLIndicatorPresent() {
        try {
            return sslIndicator.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Güvenlik rozetinin mevcut olduğunu doğrula")
    public boolean isSecurityBadgePresent() {
        try {
            return securityBadge.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Hata mesajının görünür olduğunu doğrula")
    public boolean isErrorMessageVisible() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Başarı mesajının görünür olduğunu doğrula")
    public boolean isSuccessMessageVisible() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Yükleme göstergesinin görünür olduğunu doğrula")
    public boolean isLoadingIndicatorVisible() {
        try {
            return loadingIndicator.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Getter Methods
    
    @Step("Hata mesajı metnini al")
    public String getErrorMessageText() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Step("Başarı mesajı metnini al")
    public String getSuccessMessageText() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Step("Uyarı mesajı metnini al")
    public String getWarningMessageText() {
        try {
            wait.until(ExpectedConditions.visibilityOf(warningMessage));
            return warningMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    @Step("Sayfa başlığını al")
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    @Step("Mevcut URL'yi al")
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    // Validation Methods
    
    @Step("Ödeme sayfasında olduğunu doğrula")
    public boolean isOnPaymentPage() {
        String currentUrl = getCurrentUrl();
        String pageSource = driver.getPageSource();
        return currentUrl.contains("paytr.com") && 
               (pageSource.contains("payment") || 
                pageSource.contains("ödeme") ||
                isPayButtonPresent());
    }
    
    @Step("Ödeme formunun tam olduğunu doğrula")
    public boolean isPaymentFormComplete() {
        return isCardNumberFieldPresent() && 
               isExpiryDateFieldPresent() && 
               isCvvFieldPresent() && 
               isPayButtonPresent();
    }
    
    @Step("Güvenlik özelliklerinin mevcut olduğunu doğrula")
    public boolean areSecurityFeaturesPresent() {
        String currentUrl = getCurrentUrl();
        boolean isHTTPS = currentUrl.startsWith("https://");
        return isHTTPS && (isCSRFTokenPresent() || isSSLIndicatorPresent());
    }
    
    @Step("Yükleme tamamlanana kadar bekle")
    public PayTRPaymentPage waitForLoadingToComplete() {
        try {
            wait.until(ExpectedConditions.invisibilityOf(loadingIndicator));
        } catch (Exception e) {
            // Loading indicator might not be present
        }
        return this;
    }
    
    @Step("Kart sahibi adını gir")
    public PayTRPaymentPage enterCardHolderName(String cardHolderName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(cardHolderField));
            cardHolderField.clear();
            cardHolderField.sendKeys(cardHolderName);
        } catch (Exception e) {
            System.out.println("Kart sahibi alanı bulunamadı: " + e.getMessage());
        }
        return this;
    }
    
    @Step("Kart sahibi adını al")
    public String getCardHolderNameValue() {
        try {
            return cardHolderField.getAttribute("value");
        } catch (Exception e) {
            System.out.println("Kart sahibi değeri alınamadı: " + e.getMessage());
            return "";
        }
    }
    
    @Step("Son kullanma ayını gir")
    public PayTRPaymentPage enterExpiryMonth(String month) {
        try {
            // Eğer ayrı ay alanı varsa
            WebElement monthField = driver.findElement(By.xpath("//input[@name='expiry_month' or @id='expiry_month']"));
            monthField.clear();
            monthField.sendKeys(month);
        } catch (Exception e) {
            // Birleşik tarih alanına ay ekle
            try {
                expiryDateField.clear();
                expiryDateField.sendKeys(month);
            } catch (Exception ex) {
                System.out.println("Son kullanma ayı girilemedi: " + ex.getMessage());
            }
        }
        return this;
    }
    
    @Step("Son kullanma yılını gir")
    public PayTRPaymentPage enterExpiryYear(String year) {
        try {
            // Eğer ayrı yıl alanı varsa
            WebElement yearField = driver.findElement(By.xpath("//input[@name='expiry_year' or @id='expiry_year']"));
            yearField.clear();
            yearField.sendKeys(year);
        } catch (Exception e) {
            // Birleşik tarih alanına yıl ekle
            try {
                String currentValue = expiryDateField.getAttribute("value");
                if (currentValue.length() >= 2) {
                    expiryDateField.clear();
                    expiryDateField.sendKeys(currentValue.substring(0, 2) + "/" + year);
                }
            } catch (Exception ex) {
                System.out.println("Son kullanma yılı girilemedi: " + ex.getMessage());
            }
        }
        return this;
    }
    
    @Step("Ödeme hatası var mı kontrol et")
    public boolean hasPaymentError() {
        try {
            WebElement errorElement = driver.findElement(By.xpath("//div[contains(@class, 'error') or contains(@class, 'alert-danger')]"));
            return errorElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Güvenli ödeme formu var mı kontrol et")
    public boolean hasSecurePaymentForm() {
        try {
            String currentUrl = getCurrentUrl();
            boolean isHTTPS = currentUrl.startsWith("https://");
            boolean hasSSLIndicator = isSSLIndicatorPresent();
            boolean hasCSRF = isCSRFTokenPresent();
            return isHTTPS && (hasSSLIndicator || hasCSRF);
        } catch (Exception e) {
            return false;
        }
    }
}