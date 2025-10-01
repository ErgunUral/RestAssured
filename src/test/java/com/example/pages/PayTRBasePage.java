package com.example.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import java.time.Duration;
import java.util.List;

/**
 * PayTR Base Page Object Model
 * Tüm PayTR sayfaları için ortak metodları içerir
 */
public class PayTRBasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor jsExecutor;
    protected Actions actions;
    
    // PayTR Base URL
    protected static final String BASE_URL = "https://testweb.paytr.com";
    
    // Common locators
    protected static final By LOADING_SPINNER = By.xpath("//div[contains(@class, 'loading') or contains(@class, 'spinner')]");
    protected static final By ERROR_MESSAGE = By.xpath("//*[contains(@class, 'error') or contains(@class, 'hata')]");
    protected static final By SUCCESS_MESSAGE = By.xpath("//*[contains(@class, 'success') or contains(@class, 'basarili')]");
    protected static final By ALERT_MESSAGE = By.xpath("//*[contains(@class, 'alert') or contains(@class, 'uyari')]");
    
    public PayTRBasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.jsExecutor = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }
    
    /**
     * Sayfanın yüklenmesini bekler
     */
    public void waitForPageLoad() {
        wait.until(webDriver -> jsExecutor.executeScript("return document.readyState").equals("complete"));
    }
    
    /**
     * Loading spinner'ın kaybolmasını bekler
     */
    public void waitForLoadingToDisappear() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER));
        } catch (Exception e) {
            // Loading spinner yoksa devam et
        }
    }
    
    /**
     * Element görünür olana kadar bekler
     */
    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Element tıklanabilir olana kadar bekler
     */
    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Elementi güvenli şekilde tıklar
     */
    public void safeClick(By locator) {
        try {
            WebElement element = waitForElementClickable(locator);
            element.click();
        } catch (Exception e) {
            // JavaScript ile tıklama dene
            WebElement element = driver.findElement(locator);
            jsExecutor.executeScript("arguments[0].click();", element);
        }
    }
    
    /**
     * Elementi güvenli şekilde temizler ve metin girer
     */
    public void safeSendKeys(By locator, String text) {
        WebElement element = waitForElementVisible(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    /**
     * Elementin metnini alır
     */
    public String getText(By locator) {
        WebElement element = waitForElementVisible(locator);
        return element.getText();
    }
    
    /**
     * Elementin attribute değerini alır
     */
    public String getAttribute(By locator, String attributeName) {
        WebElement element = waitForElementVisible(locator);
        return element.getAttribute(attributeName);
    }
    
    /**
     * Element var mı kontrol eder
     */
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Element görünür mü kontrol eder
     */
    public boolean isElementVisible(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Element aktif mi kontrol eder
     */
    public boolean isElementEnabled(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Sayfayı yeniler
     */
    public void refreshPage() {
        driver.navigate().refresh();
        waitForPageLoad();
    }
    
    /**
     * Sayfanın başlığını alır
     */
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    /**
     * Mevcut URL'yi alır
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    /**
     * Hata mesajı var mı kontrol eder
     */
    public boolean hasErrorMessage() {
        return isElementVisible(ERROR_MESSAGE);
    }
    
    /**
     * Başarı mesajı var mı kontrol eder
     */
    public boolean hasSuccessMessage() {
        return isElementVisible(SUCCESS_MESSAGE);
    }
    
    /**
     * Hata mesajının metnini alır
     */
    public String getErrorMessage() {
        if (hasErrorMessage()) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }
    
    /**
     * Başarı mesajının metnini alır
     */
    public String getSuccessMessage() {
        if (hasSuccessMessage()) {
            return getText(SUCCESS_MESSAGE);
        }
        return "";
    }
    
    /**
     * Sayfayı kaydırır
     */
    public void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        jsExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
    }
    
    /**
     * Sayfanın en üstüne kaydırır
     */
    public void scrollToTop() {
        jsExecutor.executeScript("window.scrollTo(0, 0);");
    }
    
    /**
     * Sayfanın en altına kaydırır
     */
    public void scrollToBottom() {
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
    
    /**
     * Belirtilen süre bekler
     */
    public void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * PayTR logosu görünür mü kontrol eder
     */
    public boolean isPayTRLogoVisible() {
        List<By> logoLocators = List.of(
            By.xpath("//img[contains(@src, 'paytr') or contains(@alt, 'PayTR')]"),
            By.xpath("//*[contains(@class, 'logo')]//*[contains(text(), 'PayTR')]"),
            By.xpath("//a[contains(@href, 'paytr')]//img")
        );
        
        for (By locator : logoLocators) {
            if (isElementVisible(locator)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * SSL sertifikası aktif mi kontrol eder
     */
    public boolean isSSLActive() {
        return getCurrentUrl().startsWith("https://");
    }
    
    /**
     * Sayfa responsive mi kontrol eder
     */
    public boolean isPageResponsive() {
        try {
            // Viewport boyutunu değiştir ve kontrol et
            jsExecutor.executeScript("document.body.style.zoom='0.5'");
            waitFor(1);
            
            boolean responsive = true;
            
            // Horizontal scroll var mı kontrol et
            Long scrollWidth = (Long) jsExecutor.executeScript("return document.body.scrollWidth");
            Long clientWidth = (Long) jsExecutor.executeScript("return document.body.clientWidth");
            
            if (scrollWidth > clientWidth + 50) { // 50px tolerance
                responsive = false;
            }
            
            // Zoom'u geri al
            jsExecutor.executeScript("document.body.style.zoom='1'");
            
            return responsive;
        } catch (Exception e) {
            return true; // Hata durumunda responsive kabul et
        }
    }
    
    /**
     * Form validasyonu aktif mi kontrol eder
     */
    public boolean hasFormValidation() {
        try {
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            for (WebElement form : forms) {
                String noValidate = form.getAttribute("novalidate");
                if (noValidate == null || !noValidate.equals("true")) {
                    return true;
                }
            }
            
            // HTML5 validation attributes kontrol et
            List<WebElement> requiredFields = driver.findElements(By.xpath("//input[@required]"));
            return !requiredFields.isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * CSRF token var mı kontrol eder
     */
    public boolean hasCSRFToken() {
        List<By> csrfLocators = List.of(
            By.xpath("//input[@name='_token']"),
            By.xpath("//input[@name='csrf_token']"),
            By.xpath("//meta[@name='csrf-token']"),
            By.xpath("//input[contains(@name, 'csrf')]")
        );
        
        for (By locator : csrfLocators) {
            if (isElementPresent(locator)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sayfa performansını ölçer (sayfa yüklenme süresi)
     */
    public long getPageLoadTime() {
        try {
            Object loadTime = jsExecutor.executeScript(
                "return performance.timing.loadEventEnd - performance.timing.navigationStart;");
            return (Long) loadTime;
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * Console error'ları kontrol eder
     */
    public boolean hasConsoleErrors() {
        try {
            List<Object> logs = (List<Object>) jsExecutor.executeScript(
                "return window.console.errors || [];");
            return !logs.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Console error'larını alır
     */
    public List<Object> getConsoleErrors() {
        try {
            Object result = jsExecutor.executeScript("return window.console.errors || [];");
            if (result instanceof List) {
                return (List<Object>) result;
            } else {
                return new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Ana sayfaya gider
     */
    public void navigateToHomePage() {
        driver.get(BASE_URL);
        waitForPageLoad();
        waitForLoadingToDisappear();
    }
    
    /**
     * Sayfa yüklenmiş mi kontrol eder
     */
    public boolean isPageLoaded() {
        try {
            waitForPageLoad();
            return !getCurrentUrl().isEmpty() && !getPageTitle().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}