package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import io.qameta.allure.Step;
import java.time.Duration;
import java.util.List;

/**
 * PayTR Login Page Object Model
 * Bu sınıf PayTR giriş sayfasının elementlerini ve işlemlerini yönetir
 */
public class PayTRLoginPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // Page URL
    private static final String LOGIN_URL = "https://zeus-uat.paytr.com/magaza/kullanici-girisi";
    
    // Page Elements using @FindBy annotations
    @FindBy(xpath = "//input[@type='email' or @name='email' or @id='email']")
    private WebElement emailField;
    
    @FindBy(xpath = "//input[@type='password' or @name='password' or @id='password']")
    private WebElement passwordField;
    
    @FindBy(xpath = "//button[@type='submit'] | //input[@type='submit']")
    private WebElement loginButton;
    
    @FindBy(xpath = "//a[contains(text(), 'Şifremi Unuttum') or contains(text(), 'Forgot Password')]")
    private WebElement forgotPasswordLink;
    
    @FindBy(xpath = "//a[contains(text(), 'Kayıt Ol') or contains(text(), 'Register')]")
    private WebElement registerLink;
    
    @FindBy(xpath = "//*[contains(@class, 'error') or contains(@class, 'alert-danger')]")
    private WebElement errorMessage;
    
    @FindBy(xpath = "//*[contains(@class, 'success') or contains(@class, 'alert-success')]")
    private WebElement successMessage;
    
    @FindBy(xpath = "//input[@name='remember' or @id='remember']")
    private WebElement rememberMeCheckbox;
    
    @FindBy(xpath = "//*[contains(@class, 'logo') or contains(@alt, 'PayTR')]")
    private WebElement logo;
    
    @FindBy(xpath = "//form")
    private WebElement loginForm;
    
    // Constructor
    public PayTRLoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }
    
    // Page Actions
    
    @Step("PayTR giriş sayfasına git")
    public PayTRLoginPage navigateToLoginPage() {
        driver.get(LOGIN_URL);
        waitForPageToLoad();
        return this;
    }
    
    @Step("Sayfa yüklenene kadar bekle")
    public PayTRLoginPage waitForPageToLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        return this;
    }
    
    @Step("Email alanına '{email}' gir")
    public PayTRLoginPage enterEmail(String email) {
        wait.until(ExpectedConditions.elementToBeClickable(emailField));
        emailField.clear();
        emailField.sendKeys(email);
        return this;
    }
    
    @Step("Şifre alanına şifre gir")
    public PayTRLoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }
    
    @Step("Giriş butonuna tıkla")
    public PayTRLoginPage clickLoginButton() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
        return this;
    }
    
    @Step("'{email}' ve şifre ile giriş yap")
    public PayTRLoginPage login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
        return this;
    }
    
    @Step("Beni hatırla checkbox'ını işaretle")
    public PayTRLoginPage checkRememberMe() {
        if (isRememberMeCheckboxPresent() && !rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
        return this;
    }
    
    @Step("Şifremi unuttum linkine tıkla")
    public PayTRLoginPage clickForgotPasswordLink() {
        if (isForgotPasswordLinkPresent()) {
            forgotPasswordLink.click();
        }
        return this;
    }
    
    @Step("Kayıt ol linkine tıkla")
    public PayTRLoginPage clickRegisterLink() {
        if (isRegisterLinkPresent()) {
            registerLink.click();
        }
        return this;
    }
    
    // Verification Methods
    
    @Step("Email alanının görünür olduğunu doğrula")
    public boolean isEmailFieldVisible() {
        try {
            return emailField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Şifre alanının görünür olduğunu doğrula")
    public boolean isPasswordFieldVisible() {
        try {
            return passwordField.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Giriş butonunun görünür olduğunu doğrula")
    public boolean isLoginButtonVisible() {
        try {
            return loginButton.isDisplayed();
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
    
    @Step("Beni hatırla checkbox'ının mevcut olduğunu doğrula")
    public boolean isRememberMeCheckboxPresent() {
        try {
            return rememberMeCheckbox.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Şifremi unuttum linkinin mevcut olduğunu doğrula")
    public boolean isForgotPasswordLinkPresent() {
        try {
            return forgotPasswordLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Kayıt ol linkinin mevcut olduğunu doğrula")
    public boolean isRegisterLinkPresent() {
        try {
            return registerLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Logo'nun görünür olduğunu doğrula")
    public boolean isLogoVisible() {
        try {
            return logo.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Giriş formunun mevcut olduğunu doğrula")
    public boolean isLoginFormPresent() {
        try {
            return loginForm.isDisplayed();
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
    
    @Step("Sayfa başlığını al")
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    @Step("Mevcut URL'yi al")
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    @Step("Email alanının değerini al")
    public String getEmailFieldValue() {
        try {
            return emailField.getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }
    
    // Validation Methods
    
    @Step("Giriş sayfasında olduğunu doğrula")
    public boolean isOnLoginPage() {
        String currentUrl = getCurrentUrl();
        return currentUrl.contains("kullanici-girisi") || currentUrl.contains("login");
    }
    
    @Step("Giriş başarılı olduğunu doğrula")
    public boolean isLoginSuccessful() {
        String currentUrl = getCurrentUrl();
        return !currentUrl.contains("kullanici-girisi") && 
               !currentUrl.contains("login") &&
               (currentUrl.contains("dashboard") || 
                currentUrl.contains("panel") ||
                currentUrl.contains("magaza"));
    }
    
    @Step("Form alanlarının boş olduğunu doğrula")
    public boolean areFormFieldsEmpty() {
        String emailValue = getEmailFieldValue();
        String passwordValue = passwordField.getAttribute("value");
        return (emailValue == null || emailValue.isEmpty()) && 
               (passwordValue == null || passwordValue.isEmpty());
    }
    
    @Step("Tüm gerekli elementlerin mevcut olduğunu doğrula")
    public boolean areAllRequiredElementsPresent() {
        return isEmailFieldVisible() && 
               isPasswordFieldVisible() && 
               isLoginButtonVisible() && 
               isLoginFormPresent();
    }
    
    // Utility Methods
    
    @Step("Sayfa kaynak kodunu al")
    public String getPageSource() {
        return driver.getPageSource();
    }
    
    @Step("JavaScript ile element bul")
    public List<WebElement> findElementsByXPath(String xpath) {
        return driver.findElements(By.xpath(xpath));
    }
    
    @Step("Sayfanın yüklendiğini bekle")
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }
    
    @Step("Element görünür olana kadar bekle")
    public void waitForElementVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    @Step("Element tıklanabilir olana kadar bekle")
    public void waitForElementClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    @Step("Şifre alanının güvenli olup olmadığını kontrol et")
    public boolean isPasswordFieldSecure() {
        try {
            String type = passwordField.getAttribute("type");
            return "password".equals(type);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Şifre alanının autocomplete özelliğini al")
    public String getPasswordFieldAutocomplete() {
        try {
            return passwordField.getAttribute("autocomplete");
        } catch (Exception e) {
            return "";
        }
    }
    
    @Step("Şifre hatası var mı kontrol et")
    public boolean hasPasswordError() {
        try {
            return errorMessage.isDisplayed() && 
                   errorMessage.getText().toLowerCase().contains("şifre") ||
                   errorMessage.getText().toLowerCase().contains("password");
        } catch (Exception e) {
            return false;
        }
    }
}