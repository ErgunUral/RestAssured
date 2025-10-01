package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

/**
 * PayTR Login Page Object Model
 * PayTR giriş sayfası için özel metodları içerir
 */
public class PayTRLoginPage extends PayTRBasePage {
    
    // Login page locators
    private static final By USERNAME_FIELD = By.xpath(
        "//input[contains(@name, 'username') or contains(@name, 'email') or " +
        "contains(@name, 'kullanici') or contains(@id, 'username') or " +
        "contains(@id, 'email') or contains(@placeholder, 'kullanıcı') or " +
        "contains(@placeholder, 'email')]");
    
    private static final By PASSWORD_FIELD = By.xpath(
        "//input[@type='password' or contains(@name, 'password') or " +
        "contains(@name, 'sifre') or contains(@id, 'password') or " +
        "contains(@id, 'sifre') or contains(@placeholder, 'şifre') or " +
        "contains(@placeholder, 'password')]");
    
    private static final By LOGIN_BUTTON = By.xpath(
        "//button[contains(text(), 'Giriş') or contains(text(), 'Login') or " +
        "contains(text(), 'giriş') or contains(text(), 'login') or " +
        "contains(@value, 'Giriş') or contains(@value, 'Login') or " +
        "contains(@class, 'login') or contains(@class, 'giriş')] | " +
        "//input[@type='submit'][contains(@value, 'Giriş') or contains(@value, 'Login')]");
    
    private static final By FORGOT_PASSWORD_LINK = By.xpath(
        "//a[contains(text(), 'Şifremi Unuttum') or contains(text(), 'Forgot Password') or " +
        "contains(text(), 'şifremi unuttum') or contains(text(), 'forgot password') or " +
        "contains(@href, 'forgot') or contains(@href, 'sifre')]");
    
    private static final By REMEMBER_ME_CHECKBOX = By.xpath(
        "//input[@type='checkbox'][contains(@name, 'remember') or " +
        "contains(@name, 'hatirla') or contains(@id, 'remember') or " +
        "contains(@id, 'hatirla')]");
    
    private static final By CAPTCHA_IMAGE = By.xpath(
        "//img[contains(@src, 'captcha') or contains(@alt, 'captcha') or " +
        "contains(@class, 'captcha')]");
    
    private static final By CAPTCHA_INPUT = By.xpath(
        "//input[contains(@name, 'captcha') or contains(@id, 'captcha') or " +
        "contains(@placeholder, 'captcha') or contains(@placeholder, 'güvenlik')]");
    
    private static final By LOGIN_FORM = By.xpath(
        "//form[contains(@action, 'login') or contains(@action, 'giriş') or " +
        "contains(@class, 'login') or contains(@class, 'giriş')]");
    
    private static final By REGISTER_LINK = By.xpath(
        "//a[contains(text(), 'Kayıt') or contains(text(), 'Register') or " +
        "contains(text(), 'kayıt') or contains(text(), 'register') or " +
        "contains(@href, 'register') or contains(@href, 'kayit')]");
    
    private static final By LOGIN_ERROR_MESSAGE = By.xpath(
        "//*[contains(@class, 'error') or contains(@class, 'hata')][contains(text(), 'giriş') or " +
        "contains(text(), 'login') or contains(text(), 'kullanıcı') or contains(text(), 'şifre')]");
    
    private static final By INVALID_CREDENTIALS_MESSAGE = By.xpath(
        "//*[contains(text(), 'geçersiz') or contains(text(), 'invalid') or " +
        "contains(text(), 'yanlış') or contains(text(), 'wrong') or " +
        "contains(text(), 'hatalı') or contains(text(), 'incorrect')]");
    
    public PayTRLoginPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Login sayfasına gider
     */
    public void navigateToLoginPage() {
        driver.get(BASE_URL);
        waitForPageLoad();
        waitForLoadingToDisappear();
    }
    
    /**
     * Kullanıcı adı alanına metin girer
     */
    public void enterUsername(String username) {
        if (isElementPresent(USERNAME_FIELD)) {
            safeSendKeys(USERNAME_FIELD, username);
        } else {
            // Alternatif locator'lar dene
            List<WebElement> inputFields = driver.findElements(By.xpath("//input[@type='text' or @type='email']"));
            if (!inputFields.isEmpty()) {
                inputFields.get(0).clear();
                inputFields.get(0).sendKeys(username);
            }
        }
    }
    
    /**
     * Şifre alanına metin girer
     */
    public void enterPassword(String password) {
        if (isElementPresent(PASSWORD_FIELD)) {
            safeSendKeys(PASSWORD_FIELD, password);
        } else {
            // Alternatif locator dene
            List<WebElement> passwordFields = driver.findElements(By.xpath("//input[@type='password']"));
            if (!passwordFields.isEmpty()) {
                passwordFields.get(0).clear();
                passwordFields.get(0).sendKeys(password);
            }
        }
    }
    
    /**
     * Captcha kodunu girer
     */
    public void enterCaptcha(String captcha) {
        if (isElementPresent(CAPTCHA_INPUT)) {
            safeSendKeys(CAPTCHA_INPUT, captcha);
        }
    }
    
    /**
     * Beni hatırla checkbox'ını işaretler
     */
    public void checkRememberMe() {
        if (isElementPresent(REMEMBER_ME_CHECKBOX)) {
            WebElement checkbox = driver.findElement(REMEMBER_ME_CHECKBOX);
            if (!checkbox.isSelected()) {
                safeClick(REMEMBER_ME_CHECKBOX);
            }
        }
    }
    
    /**
     * Giriş butonuna tıklar
     */
    public void clickLoginButton() {
        if (isElementPresent(LOGIN_BUTTON)) {
            safeClick(LOGIN_BUTTON);
        } else {
            // Alternatif submit yöntemleri
            List<WebElement> submitButtons = driver.findElements(By.xpath(
                "//button[@type='submit'] | //input[@type='submit']"));
            if (!submitButtons.isEmpty()) {
                submitButtons.get(0).click();
            } else {
                // Form submit et
                if (isElementPresent(LOGIN_FORM)) {
                    WebElement form = driver.findElement(LOGIN_FORM);
                    form.submit();
                }
            }
        }
        waitForLoadingToDisappear();
    }
    
    /**
     * Şifremi unuttum linkine tıklar
     */
    public void clickForgotPasswordLink() {
        if (isElementPresent(FORGOT_PASSWORD_LINK)) {
            safeClick(FORGOT_PASSWORD_LINK);
            waitForPageLoad();
        }
    }
    
    /**
     * Kayıt ol linkine tıklar
     */
    public void clickRegisterLink() {
        if (isElementPresent(REGISTER_LINK)) {
            safeClick(REGISTER_LINK);
            waitForPageLoad();
        }
    }
    
    /**
     * Tam login işlemi yapar
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
    }
    
    /**
     * Captcha ile login işlemi yapar
     */
    public void loginWithCaptcha(String username, String password, String captcha) {
        enterUsername(username);
        enterPassword(password);
        enterCaptcha(captcha);
        clickLoginButton();
    }
    
    /**
     * Login formu görünür mü kontrol eder
     */
    public boolean isLoginFormVisible() {
        return isElementVisible(LOGIN_FORM) || 
               (isElementPresent(USERNAME_FIELD) && isElementPresent(PASSWORD_FIELD));
    }
    
    /**
     * Kullanıcı adı alanı görünür mü kontrol eder
     */
    public boolean isUsernameFieldVisible() {
        return isElementVisible(USERNAME_FIELD);
    }
    
    /**
     * Şifre alanı görünür mü kontrol eder
     */
    public boolean isPasswordFieldVisible() {
        return isElementVisible(PASSWORD_FIELD);
    }
    
    /**
     * Giriş butonu görünür mü kontrol eder
     */
    public boolean isLoginButtonVisible() {
        return isElementVisible(LOGIN_BUTTON);
    }
    
    /**
     * Captcha görünür mü kontrol eder
     */
    public boolean isCaptchaVisible() {
        return isElementVisible(CAPTCHA_IMAGE) && isElementVisible(CAPTCHA_INPUT);
    }
    
    /**
     * Beni hatırla checkbox'ı görünür mü kontrol eder
     */
    public boolean isRememberMeVisible() {
        return isElementVisible(REMEMBER_ME_CHECKBOX);
    }
    
    /**
     * Şifremi unuttum linki görünür mü kontrol eder
     */
    public boolean isForgotPasswordLinkVisible() {
        return isElementVisible(FORGOT_PASSWORD_LINK);
    }
    
    /**
     * Kayıt ol linki görünür mü kontrol eder
     */
    public boolean isRegisterLinkVisible() {
        return isElementVisible(REGISTER_LINK);
    }
    
    /**
     * Login hata mesajı var mı kontrol eder
     */
    public boolean hasLoginError() {
        return isElementVisible(LOGIN_ERROR_MESSAGE) || isElementVisible(INVALID_CREDENTIALS_MESSAGE);
    }
    
    /**
     * Login hata mesajının metnini alır
     */
    public String getLoginErrorMessage() {
        if (isElementVisible(LOGIN_ERROR_MESSAGE)) {
            return getText(LOGIN_ERROR_MESSAGE);
        } else if (isElementVisible(INVALID_CREDENTIALS_MESSAGE)) {
            return getText(INVALID_CREDENTIALS_MESSAGE);
        }
        return "";
    }
    
    /**
     * Login başarılı mı kontrol eder (URL değişimi ile)
     */
    public boolean isLoginSuccessful() {
        waitFor(2);
        String currentUrl = getCurrentUrl();
        return !currentUrl.contains("login") && !currentUrl.contains("giriş") && 
               !hasLoginError();
    }
    
    /**
     * Kullanıcı adı alanının placeholder metnini alır
     */
    public String getUsernamePlaceholder() {
        if (isElementPresent(USERNAME_FIELD)) {
            return getAttribute(USERNAME_FIELD, "placeholder");
        }
        return "";
    }
    
    /**
     * Şifre alanının placeholder metnini alır
     */
    public String getPasswordPlaceholder() {
        if (isElementPresent(PASSWORD_FIELD)) {
            return getAttribute(PASSWORD_FIELD, "placeholder");
        }
        return "";
    }
    
    /**
     * Login form alanlarının required attribute'unu kontrol eder
     */
    public boolean areFieldsRequired() {
        boolean usernameRequired = false;
        boolean passwordRequired = false;
        
        if (isElementPresent(USERNAME_FIELD)) {
            String required = getAttribute(USERNAME_FIELD, "required");
            usernameRequired = required != null;
        }
        
        if (isElementPresent(PASSWORD_FIELD)) {
            String required = getAttribute(PASSWORD_FIELD, "required");
            passwordRequired = required != null;
        }
        
        return usernameRequired && passwordRequired;
    }
    
    /**
     * Login form alanlarının maxlength attribute'unu kontrol eder
     */
    public boolean hasFieldLengthLimits() {
        boolean usernameHasLimit = false;
        boolean passwordHasLimit = false;
        
        if (isElementPresent(USERNAME_FIELD)) {
            String maxLength = getAttribute(USERNAME_FIELD, "maxlength");
            usernameHasLimit = maxLength != null && !maxLength.isEmpty();
        }
        
        if (isElementPresent(PASSWORD_FIELD)) {
            String maxLength = getAttribute(PASSWORD_FIELD, "maxlength");
            passwordHasLimit = maxLength != null && !maxLength.isEmpty();
        }
        
        return usernameHasLimit || passwordHasLimit;
    }
    
    /**
     * Şifre alanının güvenli olup olmadığını kontrol eder
     */
    public boolean isPasswordFieldSecure() {
        if (isElementPresent(PASSWORD_FIELD)) {
            String type = getAttribute(PASSWORD_FIELD, "type");
            return "password".equals(type);
        }
        return false;
    }
    
    /**
     * Şifre alanının autocomplete özelliğini alır
     */
    public String getPasswordFieldAutocomplete() {
        if (isElementPresent(PASSWORD_FIELD)) {
            return getAttribute(PASSWORD_FIELD, "autocomplete");
        }
        return "";
    }
    
    /**
     * Şifre hata mesajı var mı kontrol eder
     */
    public boolean hasPasswordError() {
        return hasErrorMessage() || hasLoginError();
    }
    
    /**
     * Login sayfasının güvenlik özelliklerini kontrol eder
     */
    public boolean hasSecurityFeatures() {
        return isSSLActive() && hasCSRFToken() && hasFormValidation();
    }
    
    /**
     * Login form elementlerinin sayısını döndürür
     */
    public int getLoginFormElementCount() {
        int count = 0;
        
        if (isElementPresent(USERNAME_FIELD)) count++;
        if (isElementPresent(PASSWORD_FIELD)) count++;
        if (isElementPresent(LOGIN_BUTTON)) count++;
        if (isElementPresent(CAPTCHA_INPUT)) count++;
        if (isElementPresent(REMEMBER_ME_CHECKBOX)) count++;
        if (isElementPresent(FORGOT_PASSWORD_LINK)) count++;
        if (isElementPresent(REGISTER_LINK)) count++;
        
        return count;
    }
}