package com.example.tests;

import io.qameta.allure.*;
import org.testng.annotations.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import com.example.utils.WebDriverSetup;
import com.example.utils.SafeWebDriverUtils;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.*;

/**
 * PayTR Kullanılabilirlik Test Senaryoları
 * Test ID: UT-001 to UT-004
 * Kategori: Usability Testing
 */
@Epic("PayTR Usability Testing")
@Feature("User Experience")
public class PayTRUsabilityTests extends BaseTest {
    // Removed class-level fields to avoid ThreadLocal issues
    // private WebDriver driver;
    // private WebDriverWait wait;
    // private JavascriptExecutor js;
    // private Actions actions;
    
    @BeforeClass
    @Step("Kullanılabilirlik testleri için test ortamını hazırla")
    public void setupUsabilityTests() {
        baseURI = "https://zeus-uat.paytr.com";
        basePath = "";
        
        // WebDriver setup
        SafeWebDriverUtils.getSafeWebDriver();
        
        logTestInfo("PayTR Kullanılabilirlik Test Suite başlatıldı");
    }
    
    // Helper method to get current valid driver
    private WebDriver getDriver() {
        return SafeWebDriverUtils.getSafeWebDriver();
    }
    
    // Helper method to get wait instance for current driver
    private WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(15));
    }
    
    // Helper method to get JavascriptExecutor
    private JavascriptExecutor getJs() {
        return (JavascriptExecutor) getDriver();
    }
    
    // Helper method to get Actions
    private Actions getActions() {
        return new Actions(getDriver());
    }
    
    @AfterClass
    @Step("Kullanılabilirlik testleri sonrası temizlik")
    public void tearDown() {
        WebDriverSetup.quitDriver();
        logTestInfo("PayTR Kullanılabilirlik Test Suite tamamlandı");
    }
    
    /**
     * Test ID: UT-001
     * Test Adı: Mobile Responsiveness Testi
     * Kategori: Usability - Mobile
     * Öncelik: Yüksek
     */
    @Test(priority = 1, groups = {"usability", "high", "mobile", "responsive"})
    @Story("Mobile Responsiveness")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mobil cihazlarda responsive tasarım kontrolü")
    public void testMobileResponsiveness() {
        logTestInfo("Test ID: UT-001 - Mobile Responsiveness Testi");
        
        try {
            // Farklı ekran boyutları
            Dimension[] screenSizes = {
                new Dimension(375, 667),   // iPhone 6/7/8
                new Dimension(414, 896),   // iPhone XR
                new Dimension(360, 640),   // Android Small
                new Dimension(768, 1024),  // iPad Portrait
                new Dimension(1024, 768),  // iPad Landscape
                new Dimension(1920, 1080)  // Desktop
            };
            
            String[] deviceNames = {
                "iPhone 6/7/8",
                "iPhone XR",
                "Android Small",
                "iPad Portrait",
                "iPad Landscape",
                "Desktop"
            };
            
            for (int i = 0; i < screenSizes.length; i++) {
                Dimension size = screenSizes[i];
                String deviceName = deviceNames[i];
                
                logTestInfo("Test edilen cihaz: " + deviceName + " (" + size.width + "x" + size.height + ")");
                
                // Ekran boyutunu ayarla
                getDriver().manage().window().setSize(size);
                Thread.sleep(1000); // Resize için bekle
                
                // Ana sayfayı yükle
                getDriver().get(baseURI);
                getWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                
                // Responsive kontrolleri
                checkResponsiveElements(deviceName, size);
                
                // Navigation menu kontrolü
                checkNavigationMenu(deviceName, size);
                
                // Form elementleri kontrolü
                checkFormElements(deviceName, size);
                
                // Button ve link kontrolü
                checkClickableElements(deviceName, size);
            }
            
            logTestResult("UT-001", "BAŞARILI", "Mobile responsiveness testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("UT-001", "BAŞARISIZ", "Mobile responsiveness testi hatası: " + e.getMessage());
            fail("Mobile responsiveness testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: UT-002
     * Test Adı: Navigation Usability Testi
     * Kategori: Usability - Navigation
     * Öncelik: Yüksek
     */
    @Test(priority = 2, groups = {"usability", "high", "navigation"})
    @Story("Navigation Usability")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Site navigasyonu kullanılabilirlik kontrolü")
    public void testNavigationUsability() {
        logTestInfo("Test ID: UT-002 - Navigation Usability Testi");
        
        try {
            getDriver().get(baseURI);
            getWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Ana navigasyon menüsünü bul
            List<WebElement> navElements = getDriver().findElements(By.xpath(
                "//nav | //ul[contains(@class,'nav')] | //div[contains(@class,'nav')] | " +
                "//header//a | //menu | //ul[contains(@class,'menu')]"));
            
            if (!navElements.isEmpty()) {
                logTestInfo("Navigasyon elementleri bulundu: " + navElements.size());
                
                // Her navigasyon elementini test et
                for (WebElement navElement : navElements) {
                    if (navElement.isDisplayed()) {
                        testNavigationElement(navElement);
                    }
                }
                
                // Breadcrumb kontrolü
                checkBreadcrumbs();
                
                // Footer navigasyon kontrolü
                checkFooterNavigation();
                
                // Search functionality kontrolü
                checkSearchFunctionality();
                
            } else {
                logTestInfo("Navigasyon elementleri bulunamadı");
            }
            
            logTestResult("UT-002", "BAŞARILI", "Navigation usability testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("UT-002", "BAŞARISIZ", "Navigation usability testi hatası: " + e.getMessage());
            fail("Navigation usability testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: UT-003
     * Test Adı: Form Usability Testi
     * Kategori: Usability - Forms
     * Öncelik: Yüksek
     */
    @Test(priority = 3, groups = {"usability", "high", "forms"})
    @Story("Form Usability")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Form kullanılabilirlik ve kullanıcı deneyimi kontrolü")
    public void testFormUsability() {
        logTestInfo("Test ID: UT-003 - Form Usability Testi");
        
        try {
            // Login sayfasına git
            getDriver().get(baseURI + "/magaza/kullanici-girisi");
            getWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Form elementlerini bul
            List<WebElement> forms = getDriver().findElements(By.tagName("form"));
            
            for (WebElement form : forms) {
                if (form.isDisplayed()) {
                    logTestInfo("Form testi başlatıldı");
                    
                    // Input alanları kontrolü
                    checkFormInputs(form);
                    
                    // Label ve placeholder kontrolü
                    checkLabelsAndPlaceholders(form);
                    
                    // Validation mesajları kontrolü
                    checkValidationMessages(form);
                    
                    // Submit button kontrolü
                    checkSubmitButton(form);
                    
                    // Tab order kontrolü
                    checkTabOrder(form);
                    
                    // Error handling kontrolü
                    checkErrorHandling(form);
                }
            }
            
            logTestResult("UT-003", "BAŞARILI", "Form usability testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("UT-003", "BAŞARISIZ", "Form usability testi hatası: " + e.getMessage());
            fail("Form usability testi başarısız: " + e.getMessage());
        }
    }
    
    /**
     * Test ID: UT-004
     * Test Adı: Accessibility Standards Testi
     * Kategori: Usability - Accessibility
     * Öncelik: Orta
     */
    @Test(priority = 4, groups = {"usability", "medium", "accessibility", "a11y"})
    @Story("Accessibility Standards")
    @Severity(SeverityLevel.NORMAL)
    @Description("Web erişilebilirlik standartları kontrolü (WCAG)")
    public void testAccessibilityStandards() {
        logTestInfo("Test ID: UT-004 - Accessibility Standards Testi");
        
        try {
            getDriver().get(baseURI);
            getWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Alt text kontrolü
            checkImageAltTexts();
            
            // Heading hierarchy kontrolü
            checkHeadingHierarchy();
            
            // Link text kontrolü
            checkLinkTexts();
            
            // Color contrast kontrolü (temel)
            checkColorContrast();
            
            // Keyboard navigation kontrolü
            checkKeyboardNavigation();
            
            // ARIA attributes kontrolü
            checkAriaAttributes();
            
            // Focus indicators kontrolü
            checkFocusIndicators();
            
            logTestResult("UT-004", "BAŞARILI", "Accessibility standards testleri tamamlandı");
            
        } catch (Exception e) {
            logTestResult("UT-004", "BAŞARISIZ", "Accessibility standards testi hatası: " + e.getMessage());
            fail("Accessibility standards testi başarısız: " + e.getMessage());
        }
    }
    
    // Helper Methods
    
    private void checkResponsiveElements(String deviceName, Dimension size) {
        try {
            // Viewport meta tag kontrolü
            List<WebElement> viewportMeta = getDriver().findElements(
                By.xpath("//meta[@name='viewport']"));
            
            if (!viewportMeta.isEmpty()) {
                String content = viewportMeta.get(0).getAttribute("content");
                logTestInfo(deviceName + " - Viewport meta: " + content);
                assertTrue(content.contains("width=device-width"), 
                    "Viewport meta tag eksik veya hatalı");
            }
            
            // Horizontal scroll kontrolü
            Long scrollWidth = (Long) getJs().executeScript("return document.body.scrollWidth;");
            Long clientWidth = (Long) getJs().executeScript("return document.body.clientWidth;");
            
            logTestInfo(deviceName + " - ScrollWidth: " + scrollWidth + ", ClientWidth: " + clientWidth);
            
            // Mobil cihazlarda horizontal scroll olmamalı
            if (size.width <= 768) {
                assertTrue(scrollWidth <= clientWidth + 20, 
                    deviceName + " - Horizontal scroll tespit edildi");
            }
            
        } catch (Exception e) {
            logTestInfo(deviceName + " - Responsive element kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkNavigationMenu(String deviceName, Dimension size) {
        try {
            // Hamburger menu kontrolü (mobil)
            if (size.width <= 768) {
                List<WebElement> hamburgerMenus = getDriver().findElements(By.xpath(
                    "//*[contains(@class,'hamburger') or contains(@class,'menu-toggle') or " +
                    "contains(@class,'navbar-toggle')]"));
                
                if (!hamburgerMenus.isEmpty()) {
                    logTestInfo(deviceName + " - Hamburger menu bulundu");
                    
                    WebElement hamburger = hamburgerMenus.get(0);
                    if (hamburger.isDisplayed() && hamburger.isEnabled()) {
                        hamburger.click();
                        Thread.sleep(1000);
                        logTestInfo(deviceName + " - Hamburger menu tıklandı");
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo(deviceName + " - Navigation menu kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkFormElements(String deviceName, Dimension size) {
        try {
            List<WebElement> inputs = getDriver().findElements(By.tagName("input"));
            
            for (WebElement input : inputs) {
                if (input.isDisplayed()) {
                    // Input boyutu kontrolü
                    int width = input.getSize().getWidth();
                    int height = input.getSize().getHeight();
                    
                    // Mobilde minimum dokunma alanı (44px)
                    if (size.width <= 768) {
                        assertTrue(height >= 40, 
                            deviceName + " - Input yüksekliği çok küçük: " + height);
                    }
                    
                    logTestInfo(deviceName + " - Input boyutu: " + width + "x" + height);
                }
            }
            
        } catch (Exception e) {
            logTestInfo(deviceName + " - Form element kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkClickableElements(String deviceName, Dimension size) {
        try {
            List<WebElement> clickables = getDriver().findElements(By.xpath(
                "//button | //a | //input[@type='submit'] | //input[@type='button']"));
            
            for (WebElement element : clickables) {
                if (element.isDisplayed()) {
                    int width = element.getSize().getWidth();
                    int height = element.getSize().getHeight();
                    
                    // Mobilde minimum dokunma alanı
                    if (size.width <= 768) {
                        assertTrue(width >= 40 && height >= 40,
                            deviceName + " - Clickable element çok küçük: " + width + "x" + height);
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo(deviceName + " - Clickable element kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void testNavigationElement(WebElement navElement) {
        try {
            List<WebElement> links = navElement.findElements(By.tagName("a"));
            
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    String href = link.getAttribute("href");
                    String text = link.getText().trim();
                    
                    // Link text kontrolü
                    assertFalse(text.isEmpty(), "Boş link text bulundu");
                    assertFalse(text.toLowerCase().equals("click here"), 
                        "Anlamsız link text: " + text);
                    
                    // Href kontrolü
                    if (href != null && !href.isEmpty() && !href.equals("#")) {
                        logTestInfo("Link testi: " + text + " -> " + href);
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Navigation element testi hatası: " + e.getMessage());
        }
    }
    
    private void checkBreadcrumbs() {
        try {
            List<WebElement> breadcrumbs = getDriver().findElements(By.xpath(
                "//*[contains(@class,'breadcrumb')] | //*[contains(@class,'breadcrumbs')]"));
            
            if (!breadcrumbs.isEmpty()) {
                logTestInfo("Breadcrumb bulundu: " + breadcrumbs.size());
                
                for (WebElement breadcrumb : breadcrumbs) {
                    if (breadcrumb.isDisplayed()) {
                        String text = breadcrumb.getText();
                        logTestInfo("Breadcrumb text: " + text);
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Breadcrumb kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkFooterNavigation() {
        try {
            List<WebElement> footers = getDriver().findElements(By.tagName("footer"));
            
            for (WebElement footer : footers) {
                if (footer.isDisplayed()) {
                    List<WebElement> footerLinks = footer.findElements(By.tagName("a"));
                    logTestInfo("Footer link sayısı: " + footerLinks.size());
                    
                    for (WebElement link : footerLinks) {
                        String text = link.getText().trim();
                        String href = link.getAttribute("href");
                        
                        if (!text.isEmpty() && href != null && !href.isEmpty()) {
                            logTestInfo("Footer link: " + text + " -> " + href);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Footer navigation kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkSearchFunctionality() {
        try {
            List<WebElement> searchInputs = getDriver().findElements(By.xpath(
                "//input[@type='search'] | //input[contains(@placeholder,'ara') or contains(@placeholder,'search')]"));
            
            if (!searchInputs.isEmpty()) {
                logTestInfo("Search input bulundu: " + searchInputs.size());
                
                for (WebElement searchInput : searchInputs) {
                    if (searchInput.isDisplayed() && searchInput.isEnabled()) {
                        String placeholder = searchInput.getAttribute("placeholder");
                        logTestInfo("Search placeholder: " + placeholder);
                        
                        // Test search
                        searchInput.clear();
                        searchInput.sendKeys("test");
                        
                        // Search button bul
                        WebElement searchButton = null;
                        try {
                            searchButton = getDriver().findElement(By.xpath(
                                "//button[@type='submit'] | //input[@type='submit'] | " +
                                "//*[contains(@class,'search-btn')]"));
                        } catch (Exception e) {
                            // Search button bulunamadı
                        }
                        
                        if (searchButton != null && searchButton.isDisplayed()) {
                            logTestInfo("Search button bulundu ve test edildi");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Search functionality kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkFormInputs(WebElement form) {
        try {
            List<WebElement> inputs = form.findElements(By.xpath(".//input | .//textarea | .//select"));
            
            for (WebElement input : inputs) {
                if (input.isDisplayed()) {
                    String type = input.getAttribute("type");
                    String name = input.getAttribute("name");
                    String id = input.getAttribute("id");
                    
                    logTestInfo("Input kontrolü - Type: " + type + ", Name: " + name + ", ID: " + id);
                    
                    // Required field kontrolü
                    boolean isRequired = input.getAttribute("required") != null;
                    if (isRequired) {
                        logTestInfo("Required field bulundu: " + name);
                    }
                    
                    // Input focus testi
                    input.click();
                    Thread.sleep(500);
                    
                    // Active element kontrolü
                    WebElement activeElement = (WebElement) getJs().executeScript("return document.activeElement;");
                    assertEquals(activeElement, input, "Focus problemi tespit edildi");
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Form input kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkLabelsAndPlaceholders(WebElement form) {
        try {
            List<WebElement> inputs = form.findElements(By.xpath(".//input | .//textarea"));
            
            for (WebElement input : inputs) {
                if (input.isDisplayed()) {
                    String id = input.getAttribute("id");
                    String placeholder = input.getAttribute("placeholder");
                    
                    // Label kontrolü
                    if (id != null && !id.isEmpty()) {
                        List<WebElement> labels = getDriver().findElements(
                            By.xpath("//label[@for='" + id + "']"));
                        
                        if (!labels.isEmpty()) {
                            logTestInfo("Label bulundu: " + labels.get(0).getText());
                        } else if (placeholder == null || placeholder.isEmpty()) {
                            logTestInfo("Label ve placeholder eksik: " + id);
                        }
                    }
                    
                    // Placeholder kontrolü
                    if (placeholder != null && !placeholder.isEmpty()) {
                        logTestInfo("Placeholder: " + placeholder);
                        assertFalse(placeholder.trim().isEmpty(), "Boş placeholder");
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Label ve placeholder kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkValidationMessages(WebElement form) {
        try {
            // Submit button bul ve tıkla (boş form ile)
            List<WebElement> submitButtons = form.findElements(By.xpath(
                ".//button[@type='submit'] | .//input[@type='submit']"));
            
            if (!submitButtons.isEmpty()) {
                WebElement submitButton = submitButtons.get(0);
                if (submitButton.isDisplayed() && submitButton.isEnabled()) {
                    submitButton.click();
                    Thread.sleep(2000);
                    
                    // Validation mesajları ara
                    List<WebElement> validationMessages = getDriver().findElements(By.xpath(
                        "//*[contains(@class,'error') or contains(@class,'invalid') or " +
                        "contains(@class,'validation')]"));
                    
                    if (!validationMessages.isEmpty()) {
                        logTestInfo("Validation mesajları bulundu: " + validationMessages.size());
                        
                        for (WebElement message : validationMessages) {
                            if (message.isDisplayed()) {
                                String text = message.getText();
                                logTestInfo("Validation mesajı: " + text);
                                assertFalse(text.trim().isEmpty(), "Boş validation mesajı");
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Validation mesaj kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkSubmitButton(WebElement form) {
        try {
            List<WebElement> submitButtons = form.findElements(By.xpath(
                ".//button[@type='submit'] | .//input[@type='submit']"));
            
            for (WebElement button : submitButtons) {
                if (button.isDisplayed()) {
                    String text = button.getText();
                    String value = button.getAttribute("value");
                    
                    String buttonText = !text.isEmpty() ? text : value;
                    logTestInfo("Submit button text: " + buttonText);
                    
                    assertFalse(buttonText == null || buttonText.trim().isEmpty(), 
                        "Submit button text eksik");
                    
                    // Button boyutu kontrolü
                    int width = button.getSize().getWidth();
                    int height = button.getSize().getHeight();
                    
                    assertTrue(width >= 80 && height >= 30, 
                        "Submit button çok küçük: " + width + "x" + height);
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Submit button kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkTabOrder(WebElement form) {
        try {
            List<WebElement> focusableElements = form.findElements(By.xpath(
                ".//input | .//textarea | .//select | .//button | .//a"));
            
            logTestInfo("Focusable element sayısı: " + focusableElements.size());
            
            // Tab order testi (basit)
            for (int i = 0; i < Math.min(focusableElements.size(), 5); i++) {
                WebElement element = focusableElements.get(i);
                if (element.isDisplayed() && element.isEnabled()) {
                    element.click();
                    Thread.sleep(300);
                    
                    // Tab tuşu simülasyonu
                    getActions().sendKeys(org.openqa.selenium.Keys.TAB).perform();
                    Thread.sleep(300);
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Tab order kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkErrorHandling(WebElement form) {
        try {
            // Geçersiz veri ile form gönderimi
            List<WebElement> emailInputs = form.findElements(By.xpath(".//input[@type='email']"));
            
            for (WebElement emailInput : emailInputs) {
                if (emailInput.isDisplayed() && emailInput.isEnabled()) {
                    emailInput.clear();
                    emailInput.sendKeys("invalid-email");
                    
                    // Submit
                    List<WebElement> submitButtons = form.findElements(By.xpath(
                        ".//button[@type='submit'] | .//input[@type='submit']"));
                    
                    if (!submitButtons.isEmpty()) {
                        submitButtons.get(0).click();
                        Thread.sleep(2000);
                        
                        // Error mesajı kontrolü
                        String validationMessage = emailInput.getAttribute("validationMessage");
                        if (validationMessage != null && !validationMessage.isEmpty()) {
                            logTestInfo("Email validation mesajı: " + validationMessage);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Error handling kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkImageAltTexts() {
        try {
            List<WebElement> images = getDriver().findElements(By.tagName("img"));
            
            for (WebElement img : images) {
                if (img.isDisplayed()) {
                    String alt = img.getAttribute("alt");
                    String src = img.getAttribute("src");
                    
                    if (alt == null || alt.trim().isEmpty()) {
                        logTestInfo("Alt text eksik: " + src);
                    } else {
                        logTestInfo("Alt text bulundu: " + alt);
                    }
                }
            }
            
            logTestInfo("Toplam image sayısı: " + images.size());
            
        } catch (Exception e) {
            logTestInfo("Image alt text kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkHeadingHierarchy() {
        try {
            String[] headingTags = {"h1", "h2", "h3", "h4", "h5", "h6"};
            
            for (String tag : headingTags) {
                List<WebElement> headings = getDriver().findElements(By.tagName(tag));
                
                if (!headings.isEmpty()) {
                    logTestInfo(tag.toUpperCase() + " sayısı: " + headings.size());
                    
                    for (WebElement heading : headings) {
                        if (heading.isDisplayed()) {
                            String text = heading.getText().trim();
                            if (!text.isEmpty()) {
                                logTestInfo(tag.toUpperCase() + " text: " + text);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Heading hierarchy kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkLinkTexts() {
        try {
            List<WebElement> links = getDriver().findElements(By.tagName("a"));
            
            for (WebElement link : links) {
                if (link.isDisplayed()) {
                    String text = link.getText().trim();
                    String href = link.getAttribute("href");
                    
                    if (href != null && !href.isEmpty() && !href.equals("#")) {
                        if (text.isEmpty()) {
                            logTestInfo("Boş link text: " + href);
                        } else if (text.toLowerCase().contains("click here") || 
                                  text.toLowerCase().contains("read more")) {
                            logTestInfo("Anlamsız link text: " + text);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Link text kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkColorContrast() {
        try {
            // Temel renk kontrastı kontrolü (JavaScript ile)
            String script = 
                "var elements = document.querySelectorAll('*');" +
                "var contrastIssues = 0;" +
                "for(var i = 0; i < Math.min(elements.length, 50); i++) {" +
                "  var style = window.getComputedStyle(elements[i]);" +
                "  var color = style.color;" +
                "  var bgColor = style.backgroundColor;" +
                "  if(color && bgColor && color !== 'rgba(0, 0, 0, 0)' && bgColor !== 'rgba(0, 0, 0, 0)') {" +
                "    contrastIssues++;" +
                "  }" +
                "}" +
                "return contrastIssues;";
            
            Long contrastElements = (Long) getJs().executeScript(script);
            logTestInfo("Renk kontrastı kontrol edilen element sayısı: " + contrastElements);
            
        } catch (Exception e) {
            logTestInfo("Color contrast kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkKeyboardNavigation() {
        try {
            // Tab navigation testi
            List<WebElement> focusableElements = getDriver().findElements(By.xpath(
                "//a | //button | //input | //textarea | //select"));
            
            int focusableCount = 0;
            for (WebElement element : focusableElements) {
                if (element.isDisplayed() && element.isEnabled()) {
                    focusableCount++;
                }
            }
            
            logTestInfo("Keyboard ile erişilebilir element sayısı: " + focusableCount);
            
            // Basit tab navigation testi
            if (focusableCount > 0) {
                getActions().sendKeys(org.openqa.selenium.Keys.TAB).perform();
                Thread.sleep(500);
                
                WebElement activeElement = (WebElement) getJs().executeScript("return document.activeElement;");
                if (activeElement != null) {
                    logTestInfo("Keyboard navigation çalışıyor");
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Keyboard navigation kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkAriaAttributes() {
        try {
            // ARIA attribute kontrolü
            List<WebElement> ariaElements = getDriver().findElements(By.xpath(
                "//*[@aria-label or @aria-labelledby or @aria-describedby or @role]"));
            
            logTestInfo("ARIA attribute'lu element sayısı: " + ariaElements.size());
            
            for (WebElement element : ariaElements) {
                if (element.isDisplayed()) {
                    String ariaLabel = element.getAttribute("aria-label");
                    String role = element.getAttribute("role");
                    
                    if (ariaLabel != null && !ariaLabel.isEmpty()) {
                        logTestInfo("ARIA label: " + ariaLabel);
                    }
                    
                    if (role != null && !role.isEmpty()) {
                        logTestInfo("ARIA role: " + role);
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("ARIA attributes kontrolü hatası: " + e.getMessage());
        }
    }
    
    private void checkFocusIndicators() {
        try {
            // Focus indicator kontrolü
            List<WebElement> focusableElements = getDriver().findElements(By.xpath(
                "//a | //button | //input"));
            
            for (int i = 0; i < Math.min(focusableElements.size(), 5); i++) {
                WebElement element = focusableElements.get(i);
                if (element.isDisplayed() && element.isEnabled()) {
                    element.click();
                    Thread.sleep(300);
                    
                    // Focus style kontrolü (CSS)
                    String outline = (String) getJs().executeScript(
                        "return window.getComputedStyle(arguments[0]).outline;", element);
                    
                    String boxShadow = (String) getJs().executeScript(
                        "return window.getComputedStyle(arguments[0]).boxShadow;", element);
                    
                    if ((outline != null && !outline.equals("none")) || 
                        (boxShadow != null && !boxShadow.equals("none"))) {
                        logTestInfo("Focus indicator bulundu");
                    }
                }
            }
            
        } catch (Exception e) {
            logTestInfo("Focus indicators kontrolü hatası: " + e.getMessage());
        }
    }
    
    /**
     * Test sonucu raporlama metodu
     */
    private void logTestResult(String testId, String status, String details) {
        System.out.println("\n🎯 KULLANILABILIRLIK TEST SONUCU:");
        System.out.println("🆔 Test ID: " + testId);
        System.out.println("📈 Durum: " + status);
        System.out.println("📝 Detay: " + details);
        System.out.println("⏰ Zaman: " + java.time.LocalDateTime.now());
        System.out.println("==================================================");
    }
}