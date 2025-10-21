package com.example.tests;

import com.example.utils.TestUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

/**
 * PayTR Accessibility Test Senaryoları
 * WCAG 2.1 AA compliance ve accessibility testlerini içerir
 */
public class PayTRAccessibilityTests extends BaseTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeMethod
    public void setupWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDownWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(groups = {"accessibility", "wcag", "critical"}, 
          priority = 1,
          description = "AC-001: WCAG 2.1 AA Compliance Testi")
    public void testWCAG21AACompliance() {
        logTestInfo("AC-001: WCAG 2.1 AA Compliance Test");
        
        // Step 1: Navigate to PayTR payment form
        driver.get("https://test.paytr.com/payment-form");
        
        // Step 2: Check page title and meta information
        String pageTitle = driver.getTitle();
        Assert.assertNotNull(pageTitle, "Page should have a title");
        Assert.assertFalse(pageTitle.trim().isEmpty(), "Page title should not be empty");
        
        WebElement metaDescription = driver.findElement(By.xpath("//meta[@name='description']"));
        Assert.assertNotNull(metaDescription, "Page should have meta description");
        
        // Step 3: Check heading structure (H1, H2, H3 hierarchy)
        List<WebElement> h1Elements = driver.findElements(By.tagName("h1"));
        Assert.assertEquals(h1Elements.size(), 1, "Page should have exactly one H1 element");
        
        List<WebElement> headings = driver.findElements(By.xpath("//h1 | //h2 | //h3 | //h4 | //h5 | //h6"));
        Assert.assertTrue(headings.size() > 0, "Page should have heading elements");
        
        // Step 4: Check form labels and accessibility
        List<WebElement> inputElements = driver.findElements(By.tagName("input"));
        for (WebElement input : inputElements) {
            String inputId = input.getAttribute("id");
            String inputType = input.getAttribute("type");
            
            if (inputType != null && !inputType.equals("hidden") && !inputType.equals("submit")) {
                // Check for associated label
                WebElement label = null;
                try {
                    if (inputId != null && !inputId.isEmpty()) {
                        label = driver.findElement(By.xpath("//label[@for='" + inputId + "']"));
                    }
                } catch (Exception e) {
                    // Check for aria-label or aria-labelledby
                    String ariaLabel = input.getAttribute("aria-label");
                    String ariaLabelledBy = input.getAttribute("aria-labelledby");
                    
                    Assert.assertTrue(ariaLabel != null || ariaLabelledBy != null, 
                        "Input element should have label, aria-label, or aria-labelledby: " + inputType);
                }
                
                if (label != null) {
                    Assert.assertFalse(label.getText().trim().isEmpty(), 
                        "Label text should not be empty for input: " + inputType);
                }
            }
        }
        
        // Step 5: Check color contrast (simulate with API call)
        Response contrastResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"url\": \"https://test.paytr.com/payment-form\",\n" +
                      "  \"checkType\": \"color_contrast\",\n" +
                      "  \"standard\": \"WCAG_AA\"\n" +
                      "}")
            .when()
                .post("/api/accessibility/check-contrast")
            .then()
                .statusCode(200)
                .extract().response();
        
        String contrastResult = contrastResponse.jsonPath().getString("result");
        String contrastRatio = contrastResponse.jsonPath().getString("averageContrastRatio");
        
        Assert.assertEquals(contrastResult, "PASS", "Color contrast should meet WCAG AA standards");
        Assert.assertNotNull(contrastRatio, "Contrast ratio should be calculated");
        
        // Step 6: Check keyboard navigation
        WebElement firstInput = driver.findElement(By.xpath("//input[@type='text' or @type='email'][1]"));
        firstInput.click();
        
        // Simulate Tab navigation
        for (int i = 0; i < 5; i++) {
            WebElement activeElement = driver.switchTo().activeElement();
            Assert.assertNotNull(activeElement, "Should have active element during tab navigation");
            
            // Check focus indicator
            String outline = activeElement.getCssValue("outline");
            String boxShadow = activeElement.getCssValue("box-shadow");
            String border = activeElement.getCssValue("border");
            
            boolean hasFocusIndicator = !outline.equals("none") || 
                                      !boxShadow.equals("none") || 
                                      border.contains("focus");
            
            Assert.assertTrue(hasFocusIndicator, 
                "Element should have visible focus indicator: " + activeElement.getTagName());
            
            // Move to next element
            activeElement.sendKeys(org.openqa.selenium.Keys.TAB);
        }
        
        System.out.println("Page Title: " + pageTitle);
        System.out.println("Headings Count: " + headings.size());
        System.out.println("Input Elements: " + inputElements.size());
        System.out.println("Contrast Result: " + contrastResult + " (Ratio: " + contrastRatio + ")");
    }

    @Test(groups = {"accessibility", "screenreader", "high"}, 
          priority = 2,
          description = "AC-002: Screen Reader Compatibility Testi")
    public void testScreenReaderCompatibility() {
        logTestInfo("AC-002: Screen Reader Compatibility Test");
        
        // Step 1: Navigate to payment form
        driver.get("https://test.paytr.com/payment-form");
        
        // Step 2: Check ARIA attributes
        List<WebElement> elementsWithAria = driver.findElements(By.xpath("//*[@aria-*]"));
        Assert.assertTrue(elementsWithAria.size() > 0, "Page should have elements with ARIA attributes");
        
        // Step 3: Check form elements for screen reader support
        WebElement cardNumberInput = driver.findElement(By.id("card-number"));
        String ariaLabel = cardNumberInput.getAttribute("aria-label");
        String ariaDescribedBy = cardNumberInput.getAttribute("aria-describedby");
        String ariaRequired = cardNumberInput.getAttribute("aria-required");
        
        Assert.assertTrue(ariaLabel != null || ariaDescribedBy != null, 
            "Card number input should have aria-label or aria-describedby");
        Assert.assertEquals(ariaRequired, "true", "Required fields should have aria-required='true'");
        
        // Step 4: Check error messages accessibility
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.click();
        
        // Wait for error messages
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<WebElement> errorMessages = driver.findElements(By.className("error-message"));
        for (WebElement errorMsg : errorMessages) {
            String role = errorMsg.getAttribute("role");
            String ariaLive = errorMsg.getAttribute("aria-live");
            
            Assert.assertTrue(role != null || ariaLive != null, 
                "Error messages should have role or aria-live attributes");
        }
        
        // Step 5: Check landmark roles
        List<WebElement> landmarks = driver.findElements(By.xpath("//*[@role='main' or @role='navigation' or @role='banner' or @role='contentinfo']"));
        Assert.assertTrue(landmarks.size() > 0, "Page should have landmark roles for navigation");
        
        // Step 6: Test with screen reader simulation API
        Response screenReaderResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"url\": \"https://test.paytr.com/payment-form\",\n" +
                      "  \"checkType\": \"screen_reader\",\n" +
                      "  \"simulateScreenReader\": \"NVDA\"\n" +
                      "}")
            .when()
                .post("/api/accessibility/screen-reader-test")
            .then()
                .statusCode(200)
                .extract().response();
        
        String screenReaderResult = screenReaderResponse.jsonPath().getString("result");
        String readabilityScore = screenReaderResponse.jsonPath().getString("readabilityScore");
        String issues = screenReaderResponse.jsonPath().getString("issues");
        
        Assert.assertEquals(screenReaderResult, "PASS", "Screen reader test should pass");
        Assert.assertNotNull(readabilityScore, "Readability score should be provided");
        
        System.out.println("ARIA Elements: " + elementsWithAria.size());
        System.out.println("Error Messages: " + errorMessages.size());
        System.out.println("Landmarks: " + landmarks.size());
        System.out.println("Screen Reader Result: " + screenReaderResult + " (Score: " + readabilityScore + ")");
        if (issues != null && !issues.equals("null")) {
            System.out.println("Issues Found: " + issues);
        }
    }

    @Test(groups = {"accessibility", "keyboard", "medium"}, 
          priority = 3,
          description = "AC-003: Keyboard Navigation ve Focus Management Testi")
    public void testKeyboardNavigationAndFocusManagement() {
        logTestInfo("AC-003: Keyboard Navigation and Focus Management Test");
        
        // Step 1: Navigate to payment form
        driver.get("https://test.paytr.com/payment-form");
        
        // Step 2: Test tab order
        List<WebElement> focusableElements = driver.findElements(
            By.xpath("//input[not(@type='hidden')] | //button | //select | //textarea | //a[@href]"));
        
        Assert.assertTrue(focusableElements.size() > 0, "Page should have focusable elements");
        
        // Start from first element
        WebElement firstElement = focusableElements.get(0);
        firstElement.click();
        
        // Test tab navigation through all elements
        for (int i = 0; i < focusableElements.size(); i++) {
            WebElement activeElement = driver.switchTo().activeElement();
            
            // Check if element is visible and focusable
            Assert.assertTrue(activeElement.isDisplayed(), "Focused element should be visible");
            Assert.assertTrue(activeElement.isEnabled(), "Focused element should be enabled");
            
            // Check tabindex
            String tabIndex = activeElement.getAttribute("tabindex");
            if (tabIndex != null && !tabIndex.isEmpty()) {
                int tabIndexValue = Integer.parseInt(tabIndex);
                Assert.assertTrue(tabIndexValue >= 0, "Tabindex should not be negative for focusable elements");
            }
            
            // Move to next element
            activeElement.sendKeys(org.openqa.selenium.Keys.TAB);
        }
        
        // Step 3: Test reverse tab navigation (Shift+Tab)
        for (int i = 0; i < 3; i++) {
            WebElement activeElement = driver.switchTo().activeElement();
            activeElement.sendKeys(org.openqa.selenium.Keys.chord(
                org.openqa.selenium.Keys.SHIFT, org.openqa.selenium.Keys.TAB));
        }
        
        // Step 4: Test Enter and Space key functionality
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.sendKeys(org.openqa.selenium.Keys.TAB);
        
        WebElement focusedButton = driver.switchTo().activeElement();
        if (focusedButton.getTagName().equals("button")) {
            // Test Space key activation
            focusedButton.sendKeys(org.openqa.selenium.Keys.SPACE);
            
            // Check if action was triggered (form validation should show)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            List<WebElement> validationMessages = driver.findElements(By.className("validation-error"));
            Assert.assertTrue(validationMessages.size() > 0, "Space key should trigger button action");
        }
        
        // Step 5: Test escape key functionality
        WebElement modalTrigger = null;
        try {
            modalTrigger = driver.findElement(By.className("modal-trigger"));
            modalTrigger.click();
            
            // Wait for modal
            Thread.sleep(1000);
            
            // Press Escape
            driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            
            // Check if modal is closed
            Thread.sleep(500);
            List<WebElement> openModals = driver.findElements(By.className("modal-open"));
            Assert.assertEquals(openModals.size(), 0, "Escape key should close modal");
            
        } catch (Exception e) {
            // Modal test is optional if no modal exists
            System.out.println("No modal found for escape key test");
        }
        
        System.out.println("Focusable Elements: " + focusableElements.size());
        System.out.println("Tab Navigation: Completed successfully");
        System.out.println("Keyboard Activation: Tested with Space key");
    }

    @Test(groups = {"accessibility", "mobile", "medium"}, 
          priority = 4,
          description = "AC-004: Mobile Accessibility ve Touch Support Testi")
    public void testMobileAccessibilityAndTouchSupport() {
        logTestInfo("AC-004: Mobile Accessibility and Touch Support Test");
        
        // Step 1: Set mobile viewport
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667)); // iPhone size
        
        // Step 2: Navigate to payment form
        driver.get("https://test.paytr.com/payment-form");
        
        // Step 3: Check touch target sizes
        List<WebElement> touchTargets = driver.findElements(
            By.xpath("//button | //input[@type='submit'] | //input[@type='button'] | //a"));
        
        for (WebElement target : touchTargets) {
            if (target.isDisplayed()) {
                int width = target.getSize().getWidth();
                int height = target.getSize().getHeight();
                
                // WCAG recommends minimum 44x44 pixels for touch targets
                Assert.assertTrue(width >= 44 || height >= 44, 
                    "Touch target should be at least 44px in one dimension: " + 
                    target.getTagName() + " (" + width + "x" + height + ")");
            }
        }
        
        // Step 4: Check viewport meta tag
        WebElement viewportMeta = driver.findElement(By.xpath("//meta[@name='viewport']"));
        String viewportContent = viewportMeta.getAttribute("content");
        
        Assert.assertNotNull(viewportContent, "Page should have viewport meta tag");
        Assert.assertTrue(viewportContent.contains("width=device-width"), 
            "Viewport should include width=device-width");
        
        // Step 5: Test mobile-specific accessibility features
        Response mobileAccessibilityResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"url\": \"https://test.paytr.com/payment-form\",\n" +
                      "  \"checkType\": \"mobile_accessibility\",\n" +
                      "  \"deviceType\": \"mobile\",\n" +
                      "  \"screenSize\": \"375x667\"\n" +
                      "}")
            .when()
                .post("/api/accessibility/mobile-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String mobileResult = mobileAccessibilityResponse.jsonPath().getString("result");
        String touchTargetScore = mobileAccessibilityResponse.jsonPath().getString("touchTargetScore");
        String responsiveScore = mobileAccessibilityResponse.jsonPath().getString("responsiveScore");
        
        Assert.assertEquals(mobileResult, "PASS", "Mobile accessibility test should pass");
        Assert.assertNotNull(touchTargetScore, "Touch target score should be provided");
        Assert.assertNotNull(responsiveScore, "Responsive score should be provided");
        
        // Step 6: Check text scaling
        driver.executeScript("document.body.style.fontSize = '150%';");
        
        // Wait for reflow
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check if content is still accessible
        List<WebElement> inputsAfterScale = driver.findElements(By.tagName("input"));
        for (WebElement input : inputsAfterScale) {
            if (input.isDisplayed()) {
                Assert.assertTrue(input.getSize().getWidth() > 0, 
                    "Input should remain visible after text scaling");
            }
        }
        
        System.out.println("Touch Targets Checked: " + touchTargets.size());
        System.out.println("Viewport Content: " + viewportContent);
        System.out.println("Mobile Result: " + mobileResult);
        System.out.println("Touch Target Score: " + touchTargetScore);
        System.out.println("Responsive Score: " + responsiveScore);
    }

    @Test(groups = {"accessibility", "language", "medium"}, 
          priority = 5,
          description = "AC-005: Multi-language ve RTL Support Testi")
    public void testMultiLanguageAndRTLSupport() {
        logTestInfo("AC-005: Multi-language and RTL Support Test");
        
        // Step 1: Test Turkish language support
        driver.get("https://test.paytr.com/payment-form?lang=tr");
        
        WebElement htmlElement = driver.findElement(By.tagName("html"));
        String lang = htmlElement.getAttribute("lang");
        
        Assert.assertEquals(lang, "tr", "HTML lang attribute should be set to Turkish");
        
        // Check for Turkish content
        List<WebElement> textElements = driver.findElements(By.xpath("//*[contains(text(), 'Kart')]"));
        Assert.assertTrue(textElements.size() > 0, "Page should contain Turkish text");
        
        // Step 2: Test English language support
        driver.get("https://test.paytr.com/payment-form?lang=en");
        
        htmlElement = driver.findElement(By.tagName("html"));
        lang = htmlElement.getAttribute("lang");
        
        Assert.assertEquals(lang, "en", "HTML lang attribute should be set to English");
        
        // Step 3: Test RTL language support (Arabic)
        driver.get("https://test.paytr.com/payment-form?lang=ar");
        
        htmlElement = driver.findElement(By.tagName("html"));
        lang = htmlElement.getAttribute("lang");
        String dir = htmlElement.getAttribute("dir");
        
        Assert.assertEquals(lang, "ar", "HTML lang attribute should be set to Arabic");
        Assert.assertEquals(dir, "rtl", "HTML dir attribute should be set to rtl for Arabic");
        
        // Check RTL layout
        WebElement bodyElement = driver.findElement(By.tagName("body"));
        String textAlign = bodyElement.getCssValue("text-align");
        String direction = bodyElement.getCssValue("direction");
        
        Assert.assertEquals(direction, "rtl", "Body direction should be rtl for Arabic");
        
        // Step 4: Test language switching
        Response languageResponse = RestAssured
            .given()
                .header("Content-Type", "application/json")
                .body("{\n" +
                      "  \"url\": \"https://test.paytr.com/payment-form\",\n" +
                      "  \"checkType\": \"language_support\",\n" +
                      "  \"languages\": [\"tr\", \"en\", \"ar\"]\n" +
                      "}")
            .when()
                .post("/api/accessibility/language-check")
            .then()
                .statusCode(200)
                .extract().response();
        
        String languageResult = languageResponse.jsonPath().getString("result");
        String supportedLanguages = languageResponse.jsonPath().getString("supportedLanguages");
        String rtlSupport = languageResponse.jsonPath().getString("rtlSupport");
        
        Assert.assertEquals(languageResult, "PASS", "Language support test should pass");
        Assert.assertNotNull(supportedLanguages, "Supported languages should be listed");
        Assert.assertEquals(rtlSupport, "true", "RTL support should be available");
        
        System.out.println("Language Support Test Results:");
        System.out.println("  Turkish (tr): " + (lang.equals("tr") ? "✓" : "✗"));
        System.out.println("  English (en): ✓");
        System.out.println("  Arabic (ar): " + (dir != null && dir.equals("rtl") ? "✓" : "✗"));
        System.out.println("  Supported Languages: " + supportedLanguages);
        System.out.println("  RTL Support: " + rtlSupport);
    }
}