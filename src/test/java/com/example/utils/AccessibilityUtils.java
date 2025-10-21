package com.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Accessibility Testing Utilities
 * Erişilebilirlik testleri için yardımcı sınıf
 * 
 * Özellikler:
 * - WCAG 2.1 AA compliance testing
 * - Screen reader compatibility
 * - Keyboard navigation testing
 * - Mobile accessibility testing
 * - Multi-language and RTL support
 * - Color contrast validation
 * - Focus management
 */
public class AccessibilityUtils {

    private static final Map<String, String> ARIA_ROLES = new HashMap<>();
    private static final Map<String, List<String>> WCAG_GUIDELINES = new HashMap<>();
    private static final List<String> KEYBOARD_NAVIGABLE_ELEMENTS = Arrays.asList(
        "a", "button", "input", "select", "textarea", "area", "object", "embed", "iframe"
    );

    static {
        initializeAriaRoles();
        initializeWCAGGuidelines();
    }

    /**
     * Accessibility Test Result
     */
    public static class AccessibilityTestResult {
        private final String testType;
        private final boolean passed;
        private final List<String> violations;
        private final List<String> warnings;
        private final Map<String, Object> metrics;
        private final String summary;

        public AccessibilityTestResult(String testType, boolean passed, List<String> violations,
                                     List<String> warnings, Map<String, Object> metrics, String summary) {
            this.testType = testType;
            this.passed = passed;
            this.violations = violations != null ? new ArrayList<>(violations) : new ArrayList<>();
            this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
            this.metrics = metrics != null ? new HashMap<>(metrics) : new HashMap<>();
            this.summary = summary;
        }

        // Getters
        public String getTestType() { return testType; }
        public boolean isPassed() { return passed; }
        public List<String> getViolations() { return violations; }
        public List<String> getWarnings() { return warnings; }
        public Map<String, Object> getMetrics() { return metrics; }
        public String getSummary() { return summary; }
    }

    /**
     * WCAG 2.1 AA Compliance Checker
     */
    public static class WCAGComplianceChecker {

        public static AccessibilityTestResult checkWCAGCompliance(WebDriver driver) {
            List<String> violations = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();

            // Check images for alt text
            List<WebElement> images = driver.findElements(By.tagName("img"));
            int imagesWithoutAlt = 0;
            for (WebElement img : images) {
                String alt = img.getAttribute("alt");
                if (alt == null || alt.trim().isEmpty()) {
                    imagesWithoutAlt++;
                    violations.add("Image without alt text: " + img.getAttribute("src"));
                }
            }
            metrics.put("images_total", images.size());
            metrics.put("images_without_alt", imagesWithoutAlt);

            // Check form labels
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            int inputsWithoutLabels = 0;
            for (WebElement input : inputs) {
                String type = input.getAttribute("type");
                if (!"hidden".equals(type) && !"submit".equals(type) && !"button".equals(type)) {
                    String id = input.getAttribute("id");
                    String ariaLabel = input.getAttribute("aria-label");
                    String ariaLabelledBy = input.getAttribute("aria-labelledby");
                    
                    boolean hasLabel = false;
                    if (id != null && !id.isEmpty()) {
                        List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + id + "']"));
                        hasLabel = !labels.isEmpty();
                    }
                    
                    if (!hasLabel && (ariaLabel == null || ariaLabel.isEmpty()) && 
                        (ariaLabelledBy == null || ariaLabelledBy.isEmpty())) {
                        inputsWithoutLabels++;
                        violations.add("Input without label: " + input.getAttribute("name"));
                    }
                }
            }
            metrics.put("inputs_total", inputs.size());
            metrics.put("inputs_without_labels", inputsWithoutLabels);

            // Check headings hierarchy
            List<String> headingIssues = checkHeadingHierarchy(driver);
            violations.addAll(headingIssues);
            metrics.put("heading_violations", headingIssues.size());

            // Check color contrast (basic check)
            List<String> contrastIssues = checkBasicColorContrast(driver);
            warnings.addAll(contrastIssues);
            metrics.put("contrast_warnings", contrastIssues.size());

            // Check focus indicators
            List<String> focusIssues = checkFocusIndicators(driver);
            violations.addAll(focusIssues);
            metrics.put("focus_violations", focusIssues.size());

            boolean passed = violations.isEmpty();
            String summary = String.format("WCAG 2.1 AA Compliance: %s (%d violations, %d warnings)",
                passed ? "PASSED" : "FAILED", violations.size(), warnings.size());

            return new AccessibilityTestResult("WCAG_2_1_AA", passed, violations, warnings, metrics, summary);
        }

        private static List<String> checkHeadingHierarchy(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
            
            int previousLevel = 0;
            for (WebElement heading : headings) {
                int currentLevel = Integer.parseInt(heading.getTagName().substring(1));
                
                if (previousLevel == 0 && currentLevel != 1) {
                    issues.add("Page should start with h1, found: " + heading.getTagName());
                } else if (currentLevel > previousLevel + 1) {
                    issues.add("Heading hierarchy skip: " + heading.getTagName() + " after h" + previousLevel);
                }
                
                previousLevel = currentLevel;
            }
            
            return issues;
        }

        private static List<String> checkBasicColorContrast(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Basic color contrast check using JavaScript
            String script = 
                "var elements = document.querySelectorAll('*');" +
                "var issues = [];" +
                "for (var i = 0; i < elements.length; i++) {" +
                "  var el = elements[i];" +
                "  var style = window.getComputedStyle(el);" +
                "  var color = style.color;" +
                "  var bgColor = style.backgroundColor;" +
                "  if (color && bgColor && color !== 'rgba(0, 0, 0, 0)' && bgColor !== 'rgba(0, 0, 0, 0)') {" +
                "    if (color === bgColor) {" +
                "      issues.push('Same color and background color detected');" +
                "    }" +
                "  }" +
                "}" +
                "return issues;";
            
            try {
                @SuppressWarnings("unchecked")
                List<String> contrastIssues = (List<String>) js.executeScript(script);
                warnings.addAll(contrastIssues);
            } catch (Exception e) {
                warnings.add("Could not perform color contrast check: " + e.getMessage());
            }
            
            return warnings;
        }

        private static List<String> checkFocusIndicators(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            List<WebElement> focusableElements = driver.findElements(
                By.cssSelector("a, button, input, select, textarea, [tabindex]:not([tabindex='-1'])")
            );
            
            for (WebElement element : focusableElements) {
                try {
                    element.click();
                    WebElement activeElement = driver.switchTo().activeElement();
                    if (!element.equals(activeElement)) {
                        issues.add("Element not focusable: " + element.getTagName());
                    }
                } catch (Exception e) {
                    // Element might not be interactable
                }
            }
            
            return issues;
        }
    }

    /**
     * Screen Reader Compatibility Checker
     */
    public static class ScreenReaderChecker {

        public static AccessibilityTestResult checkScreenReaderCompatibility(WebDriver driver) {
            List<String> violations = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();

            // Check ARIA attributes
            List<String> ariaIssues = checkAriaAttributes(driver);
            violations.addAll(ariaIssues);
            metrics.put("aria_violations", ariaIssues.size());

            // Check semantic HTML
            List<String> semanticIssues = checkSemanticHTML(driver);
            warnings.addAll(semanticIssues);
            metrics.put("semantic_warnings", semanticIssues.size());

            // Check landmark roles
            List<String> landmarkIssues = checkLandmarkRoles(driver);
            violations.addAll(landmarkIssues);
            metrics.put("landmark_violations", landmarkIssues.size());

            // Check live regions
            List<String> liveRegionIssues = checkLiveRegions(driver);
            warnings.addAll(liveRegionIssues);
            metrics.put("live_region_warnings", liveRegionIssues.size());

            boolean passed = violations.isEmpty();
            String summary = String.format("Screen Reader Compatibility: %s (%d violations, %d warnings)",
                passed ? "PASSED" : "FAILED", violations.size(), warnings.size());

            return new AccessibilityTestResult("SCREEN_READER", passed, violations, warnings, metrics, summary);
        }

        private static List<String> checkAriaAttributes(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            
            // Check for invalid ARIA attributes
            List<WebElement> elementsWithAria = driver.findElements(By.cssSelector("[aria-*]"));
            for (WebElement element : elementsWithAria) {
                String role = element.getAttribute("role");
                if (role != null && !ARIA_ROLES.containsKey(role)) {
                    issues.add("Invalid ARIA role: " + role);
                }
                
                // Check aria-labelledby references
                String labelledBy = element.getAttribute("aria-labelledby");
                if (labelledBy != null && !labelledBy.isEmpty()) {
                    String[] ids = labelledBy.split("\\s+");
                    for (String id : ids) {
                        List<WebElement> referencedElements = driver.findElements(By.id(id));
                        if (referencedElements.isEmpty()) {
                            issues.add("aria-labelledby references non-existent element: " + id);
                        }
                    }
                }
            }
            
            return issues;
        }

        private static List<String> checkSemanticHTML(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            // Check for proper use of semantic elements
            List<WebElement> divs = driver.findElements(By.tagName("div"));
            List<WebElement> spans = driver.findElements(By.tagName("span"));
            
            if (divs.size() > spans.size() * 2) {
                warnings.add("Excessive use of div elements - consider semantic alternatives");
            }
            
            // Check for missing main element
            List<WebElement> mainElements = driver.findElements(By.tagName("main"));
            if (mainElements.isEmpty()) {
                warnings.add("Missing main element for primary content");
            }
            
            return warnings;
        }

        private static List<String> checkLandmarkRoles(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            
            // Check for essential landmarks
            String[] essentialLandmarks = {"banner", "navigation", "main", "contentinfo"};
            for (String landmark : essentialLandmarks) {
                List<WebElement> elements = driver.findElements(
                    By.cssSelector("[role='" + landmark + "'], " + getSemanticEquivalent(landmark))
                );
                if (elements.isEmpty()) {
                    issues.add("Missing landmark: " + landmark);
                }
            }
            
            return issues;
        }

        private static List<String> checkLiveRegions(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            // Check for proper live region usage
            List<WebElement> liveRegions = driver.findElements(By.cssSelector("[aria-live]"));
            for (WebElement region : liveRegions) {
                String liveValue = region.getAttribute("aria-live");
                if (!"polite".equals(liveValue) && !"assertive".equals(liveValue) && !"off".equals(liveValue)) {
                    warnings.add("Invalid aria-live value: " + liveValue);
                }
            }
            
            return warnings;
        }

        private static String getSemanticEquivalent(String landmark) {
            switch (landmark) {
                case "banner": return "header";
                case "navigation": return "nav";
                case "main": return "main";
                case "contentinfo": return "footer";
                default: return "";
            }
        }
    }

    /**
     * Keyboard Navigation Tester
     */
    public static class KeyboardNavigationTester {

        public static AccessibilityTestResult testKeyboardNavigation(WebDriver driver) {
            List<String> violations = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();

            Actions actions = new Actions(driver);
            
            // Test tab navigation
            List<String> tabIssues = testTabNavigation(driver, actions);
            violations.addAll(tabIssues);
            metrics.put("tab_violations", tabIssues.size());

            // Test keyboard shortcuts
            List<String> shortcutIssues = testKeyboardShortcuts(driver, actions);
            warnings.addAll(shortcutIssues);
            metrics.put("shortcut_warnings", shortcutIssues.size());

            // Test focus management
            List<String> focusIssues = testFocusManagement(driver, actions);
            violations.addAll(focusIssues);
            metrics.put("focus_violations", focusIssues.size());

            // Test escape key functionality
            List<String> escapeIssues = testEscapeKey(driver, actions);
            warnings.addAll(escapeIssues);
            metrics.put("escape_warnings", escapeIssues.size());

            boolean passed = violations.isEmpty();
            String summary = String.format("Keyboard Navigation: %s (%d violations, %d warnings)",
                passed ? "PASSED" : "FAILED", violations.size(), warnings.size());

            return new AccessibilityTestResult("KEYBOARD_NAVIGATION", passed, violations, warnings, metrics, summary);
        }

        private static List<String> testTabNavigation(WebDriver driver, Actions actions) {
            List<String> issues = new ArrayList<>();
            
            try {
                // Get all focusable elements
                List<WebElement> focusableElements = driver.findElements(
                    By.cssSelector("a, button, input, select, textarea, [tabindex]:not([tabindex='-1'])")
                );
                
                if (focusableElements.isEmpty()) {
                    issues.add("No focusable elements found on page");
                    return issues;
                }
                
                // Test tab order
                WebElement firstElement = focusableElements.get(0);
                firstElement.click();
                
                WebElement currentFocus = driver.switchTo().activeElement();
                int tabCount = 0;
                Set<WebElement> visitedElements = new HashSet<>();
                
                while (tabCount < focusableElements.size() && !visitedElements.contains(currentFocus)) {
                    visitedElements.add(currentFocus);
                    actions.sendKeys(Keys.TAB).perform();
                    
                    WebElement newFocus = driver.switchTo().activeElement();
                    if (newFocus.equals(currentFocus)) {
                        issues.add("Tab navigation stuck at element: " + currentFocus.getTagName());
                        break;
                    }
                    
                    currentFocus = newFocus;
                    tabCount++;
                }
                
                // Test reverse tab navigation
                actions.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB)).perform();
                
            } catch (Exception e) {
                issues.add("Error during tab navigation test: " + e.getMessage());
            }
            
            return issues;
        }

        private static List<String> testKeyboardShortcuts(WebDriver driver, Actions actions) {
            List<String> warnings = new ArrayList<>();
            
            // Test common keyboard shortcuts
            try {
                // Test Enter key on buttons
                List<WebElement> buttons = driver.findElements(By.tagName("button"));
                for (WebElement button : buttons) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        button.click();
                        actions.sendKeys(Keys.ENTER).perform();
                        // Could add more specific validation here
                        break; // Test only first button to avoid side effects
                    }
                }
                
                // Test Space key on buttons
                for (WebElement button : buttons) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        button.click();
                        actions.sendKeys(Keys.SPACE).perform();
                        break; // Test only first button to avoid side effects
                    }
                }
                
            } catch (Exception e) {
                warnings.add("Error during keyboard shortcuts test: " + e.getMessage());
            }
            
            return warnings;
        }

        private static List<String> testFocusManagement(WebDriver driver, Actions actions) {
            List<String> issues = new ArrayList<>();
            
            try {
                // Test focus visibility
                List<WebElement> focusableElements = driver.findElements(
                    By.cssSelector("a, button, input, select, textarea")
                );
                
                for (WebElement element : focusableElements) {
                    if (element.isDisplayed()) {
                        element.click();
                        WebElement activeElement = driver.switchTo().activeElement();
                        
                        if (!element.equals(activeElement)) {
                            issues.add("Element does not receive focus: " + element.getTagName());
                        }
                        
                        // Check if focus is visible (basic check)
                        String outline = element.getCssValue("outline");
                        String outlineWidth = element.getCssValue("outline-width");
                        String boxShadow = element.getCssValue("box-shadow");
                        
                        if ("none".equals(outline) && "0px".equals(outlineWidth) && "none".equals(boxShadow)) {
                            issues.add("No visible focus indicator on: " + element.getTagName());
                        }
                        
                        break; // Test only first element to avoid excessive testing
                    }
                }
                
            } catch (Exception e) {
                issues.add("Error during focus management test: " + e.getMessage());
            }
            
            return issues;
        }

        private static List<String> testEscapeKey(WebDriver driver, Actions actions) {
            List<String> warnings = new ArrayList<>();
            
            try {
                // Test escape key on modals/dialogs
                List<WebElement> modals = driver.findElements(
                    By.cssSelector("[role='dialog'], .modal, .popup")
                );
                
                for (WebElement modal : modals) {
                    if (modal.isDisplayed()) {
                        actions.sendKeys(Keys.ESCAPE).perform();
                        
                        // Check if modal is still visible after escape
                        if (modal.isDisplayed()) {
                            warnings.add("Modal does not close with Escape key");
                        }
                        break;
                    }
                }
                
            } catch (Exception e) {
                warnings.add("Error during escape key test: " + e.getMessage());
            }
            
            return warnings;
        }
    }

    /**
     * Mobile Accessibility Tester
     */
    public static class MobileAccessibilityTester {

        public static AccessibilityTestResult testMobileAccessibility(WebDriver driver) {
            List<String> violations = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();

            // Test touch target sizes
            List<String> touchTargetIssues = checkTouchTargetSizes(driver);
            violations.addAll(touchTargetIssues);
            metrics.put("touch_target_violations", touchTargetIssues.size());

            // Test viewport and responsive design
            List<String> viewportIssues = checkViewportConfiguration(driver);
            warnings.addAll(viewportIssues);
            metrics.put("viewport_warnings", viewportIssues.size());

            // Test gesture support
            List<String> gestureIssues = checkGestureSupport(driver);
            warnings.addAll(gestureIssues);
            metrics.put("gesture_warnings", gestureIssues.size());

            boolean passed = violations.isEmpty();
            String summary = String.format("Mobile Accessibility: %s (%d violations, %d warnings)",
                passed ? "PASSED" : "FAILED", violations.size(), warnings.size());

            return new AccessibilityTestResult("MOBILE_ACCESSIBILITY", passed, violations, warnings, metrics, summary);
        }

        private static List<String> checkTouchTargetSizes(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            String script = 
                "var elements = document.querySelectorAll('a, button, input, select, textarea');" +
                "var smallTargets = [];" +
                "for (var i = 0; i < elements.length; i++) {" +
                "  var el = elements[i];" +
                "  var rect = el.getBoundingClientRect();" +
                "  if (rect.width > 0 && rect.height > 0 && (rect.width < 44 || rect.height < 44)) {" +
                "    smallTargets.push(el.tagName + ' (' + rect.width + 'x' + rect.height + ')');" +
                "  }" +
                "}" +
                "return smallTargets;";
            
            try {
                @SuppressWarnings("unchecked")
                List<String> smallTargets = (List<String>) js.executeScript(script);
                for (String target : smallTargets) {
                    issues.add("Touch target too small (minimum 44x44px): " + target);
                }
            } catch (Exception e) {
                issues.add("Error checking touch target sizes: " + e.getMessage());
            }
            
            return issues;
        }

        private static List<String> checkViewportConfiguration(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            try {
                List<WebElement> viewportMetas = driver.findElements(
                    By.cssSelector("meta[name='viewport']")
                );
                
                if (viewportMetas.isEmpty()) {
                    warnings.add("Missing viewport meta tag");
                } else {
                    String content = viewportMetas.get(0).getAttribute("content");
                    if (content != null) {
                        if (!content.contains("width=device-width")) {
                            warnings.add("Viewport should include width=device-width");
                        }
                        if (content.contains("user-scalable=no")) {
                            warnings.add("Viewport prevents user scaling (user-scalable=no)");
                        }
                        if (content.contains("maximum-scale=1")) {
                            warnings.add("Viewport limits maximum scale to 1");
                        }
                    }
                }
                
            } catch (Exception e) {
                warnings.add("Error checking viewport configuration: " + e.getMessage());
            }
            
            return warnings;
        }

        private static List<String> checkGestureSupport(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            // Check for elements that might require complex gestures
            List<WebElement> draggableElements = driver.findElements(By.cssSelector("[draggable='true']"));
            if (!draggableElements.isEmpty()) {
                warnings.add("Draggable elements detected - ensure alternative interaction methods");
            }
            
            // Check for hover-only interactions
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = 
                "var elements = document.querySelectorAll('*');" +
                "var hoverOnlyElements = [];" +
                "for (var i = 0; i < elements.length; i++) {" +
                "  var el = elements[i];" +
                "  var style = window.getComputedStyle(el, ':hover');" +
                "  if (style && style.display !== 'none') {" +
                "    // Element has hover styles" +
                "  }" +
                "}" +
                "return hoverOnlyElements;";
            
            try {
                js.executeScript(script);
                // This is a simplified check - in practice, you'd need more sophisticated detection
            } catch (Exception e) {
                warnings.add("Error checking gesture support: " + e.getMessage());
            }
            
            return warnings;
        }
    }

    /**
     * Multi-language and RTL Support Tester
     */
    public static class MultiLanguageTester {

        public static AccessibilityTestResult testMultiLanguageSupport(WebDriver driver) {
            List<String> violations = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();

            // Check language attributes
            List<String> langIssues = checkLanguageAttributes(driver);
            violations.addAll(langIssues);
            metrics.put("language_violations", langIssues.size());

            // Check RTL support
            List<String> rtlIssues = checkRTLSupport(driver);
            warnings.addAll(rtlIssues);
            metrics.put("rtl_warnings", rtlIssues.size());

            // Check text direction
            List<String> directionIssues = checkTextDirection(driver);
            warnings.addAll(directionIssues);
            metrics.put("direction_warnings", directionIssues.size());

            boolean passed = violations.isEmpty();
            String summary = String.format("Multi-language Support: %s (%d violations, %d warnings)",
                passed ? "PASSED" : "FAILED", violations.size(), warnings.size());

            return new AccessibilityTestResult("MULTI_LANGUAGE", passed, violations, warnings, metrics, summary);
        }

        private static List<String> checkLanguageAttributes(WebDriver driver) {
            List<String> issues = new ArrayList<>();
            
            // Check html lang attribute
            List<WebElement> htmlElements = driver.findElements(By.tagName("html"));
            if (!htmlElements.isEmpty()) {
                String lang = htmlElements.get(0).getAttribute("lang");
                if (lang == null || lang.trim().isEmpty()) {
                    issues.add("Missing lang attribute on html element");
                } else if (!isValidLanguageCode(lang)) {
                    issues.add("Invalid language code: " + lang);
                }
            }
            
            // Check for elements with different languages
            List<WebElement> elementsWithLang = driver.findElements(By.cssSelector("[lang]"));
            for (WebElement element : elementsWithLang) {
                String lang = element.getAttribute("lang");
                if (!isValidLanguageCode(lang)) {
                    issues.add("Invalid language code on element: " + lang);
                }
            }
            
            return issues;
        }

        private static List<String> checkRTLSupport(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            // Check for RTL language indicators
            List<WebElement> rtlElements = driver.findElements(By.cssSelector("[dir='rtl'], [lang^='ar'], [lang^='he'], [lang^='fa']"));
            
            if (!rtlElements.isEmpty()) {
                // Check if CSS supports RTL
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String script = 
                    "var rtlSupported = false;" +
                    "var stylesheets = document.styleSheets;" +
                    "for (var i = 0; i < stylesheets.length; i++) {" +
                    "  try {" +
                    "    var rules = stylesheets[i].cssRules || stylesheets[i].rules;" +
                    "    for (var j = 0; j < rules.length; j++) {" +
                    "      if (rules[j].selectorText && rules[j].selectorText.includes('[dir=\"rtl\"]')) {" +
                    "        rtlSupported = true;" +
                    "        break;" +
                    "      }" +
                    "    }" +
                    "  } catch (e) {}" +
                    "}" +
                    "return rtlSupported;";
                
                try {
                    Boolean rtlSupported = (Boolean) js.executeScript(script);
                    if (!rtlSupported) {
                        warnings.add("RTL content detected but no RTL CSS support found");
                    }
                } catch (Exception e) {
                    warnings.add("Could not check RTL CSS support: " + e.getMessage());
                }
            }
            
            return warnings;
        }

        private static List<String> checkTextDirection(WebDriver driver) {
            List<String> warnings = new ArrayList<>();
            
            // Check for mixed text directions
            List<WebElement> textElements = driver.findElements(By.cssSelector("p, div, span, h1, h2, h3, h4, h5, h6"));
            
            for (WebElement element : textElements) {
                String dir = element.getAttribute("dir");
                String lang = element.getAttribute("lang");
                
                if (lang != null && isRTLLanguage(lang) && !"rtl".equals(dir)) {
                    warnings.add("RTL language without dir='rtl' attribute: " + lang);
                }
                
                if (dir != null && !"ltr".equals(dir) && !"rtl".equals(dir) && !"auto".equals(dir)) {
                    warnings.add("Invalid dir attribute value: " + dir);
                }
            }
            
            return warnings;
        }

        private static boolean isValidLanguageCode(String lang) {
            // Basic language code validation (ISO 639-1 and some common extensions)
            Pattern langPattern = Pattern.compile("^[a-z]{2,3}(-[A-Z]{2})?(-[a-z]{4})?(-[A-Z]{2})?$");
            return langPattern.matcher(lang).matches();
        }

        private static boolean isRTLLanguage(String lang) {
            String[] rtlLanguages = {"ar", "he", "fa", "ur", "yi"};
            String baseLang = lang.split("-")[0].toLowerCase();
            return Arrays.asList(rtlLanguages).contains(baseLang);
        }
    }

    /**
     * Utility Methods
     */
    private static void initializeAriaRoles() {
        ARIA_ROLES.put("alert", "A message with important, and usually time-sensitive, information");
        ARIA_ROLES.put("alertdialog", "A type of dialog that contains an alert message");
        ARIA_ROLES.put("application", "A region declared as a web application");
        ARIA_ROLES.put("article", "A section of a page that consists of a composition");
        ARIA_ROLES.put("banner", "A region that contains mostly site-oriented content");
        ARIA_ROLES.put("button", "An input that allows for user-triggered actions");
        ARIA_ROLES.put("checkbox", "A checkable input that has three possible values");
        ARIA_ROLES.put("columnheader", "A cell containing header information for a column");
        ARIA_ROLES.put("combobox", "A composite widget containing a single-line textbox");
        ARIA_ROLES.put("complementary", "A supporting section of the document");
        ARIA_ROLES.put("contentinfo", "A large perceivable region that contains information");
        ARIA_ROLES.put("definition", "A definition of a term or concept");
        ARIA_ROLES.put("dialog", "An application window that is designed to interrupt");
        ARIA_ROLES.put("directory", "A list of references to members of a group");
        ARIA_ROLES.put("document", "A region containing related information");
        ARIA_ROLES.put("form", "A landmark region that contains a collection of items");
        ARIA_ROLES.put("grid", "A composite widget containing a collection of one or more rows");
        ARIA_ROLES.put("gridcell", "A cell in a grid or treegrid");
        ARIA_ROLES.put("group", "A set of user interface objects");
        ARIA_ROLES.put("heading", "A heading for a section of the page");
        ARIA_ROLES.put("img", "A container for a collection of elements");
        ARIA_ROLES.put("link", "An interactive reference to an internal or external resource");
        ARIA_ROLES.put("list", "A group of non-interactive list items");
        ARIA_ROLES.put("listbox", "A widget that allows the user to select one or more items");
        ARIA_ROLES.put("listitem", "A single item in a list or directory");
        ARIA_ROLES.put("log", "A type of live region where new information is added");
        ARIA_ROLES.put("main", "The main content of a document");
        ARIA_ROLES.put("marquee", "A type of live region with non-essential information");
        ARIA_ROLES.put("math", "Content that represents a mathematical expression");
        ARIA_ROLES.put("menu", "A type of widget that offers a list of choices");
        ARIA_ROLES.put("menubar", "A presentation of menu that usually remains visible");
        ARIA_ROLES.put("menuitem", "An option in a group of choices contained by a menu");
        ARIA_ROLES.put("menuitemcheckbox", "A checkable menuitem");
        ARIA_ROLES.put("menuitemradio", "A checkable menuitem in a group of menuitemradio roles");
        ARIA_ROLES.put("navigation", "A collection of navigational elements");
        ARIA_ROLES.put("none", "An element whose implicit native role semantics will not be mapped");
        ARIA_ROLES.put("note", "A section whose content is parenthetic");
        ARIA_ROLES.put("option", "A selectable item in a select list");
        ARIA_ROLES.put("presentation", "An element whose implicit native role semantics will not be mapped");
        ARIA_ROLES.put("progressbar", "An element that displays the progress status");
        ARIA_ROLES.put("radio", "A checkable input in a group of radio roles");
        ARIA_ROLES.put("radiogroup", "A group of radio buttons");
        ARIA_ROLES.put("region", "A perceivable section containing content");
        ARIA_ROLES.put("row", "A row of cells in a grid");
        ARIA_ROLES.put("rowgroup", "A group containing one or more row elements");
        ARIA_ROLES.put("rowheader", "A cell containing header information for a row");
        ARIA_ROLES.put("scrollbar", "A graphical object that controls the scrolling");
        ARIA_ROLES.put("search", "A landmark region that contains a collection of items");
        ARIA_ROLES.put("searchbox", "A type of textbox intended for specifying search criteria");
        ARIA_ROLES.put("separator", "A divider that separates and distinguishes sections");
        ARIA_ROLES.put("slider", "A user input where the user selects a value");
        ARIA_ROLES.put("spinbutton", "A form of range that expects the user to select");
        ARIA_ROLES.put("status", "A container whose content is advisory information");
        ARIA_ROLES.put("switch", "A type of checkbox that represents on/off values");
        ARIA_ROLES.put("tab", "A grouping label providing a mechanism for selecting");
        ARIA_ROLES.put("table", "A section containing data arranged in rows and columns");
        ARIA_ROLES.put("tablist", "A list of tab elements");
        ARIA_ROLES.put("tabpanel", "A container for the resources associated with a tab");
        ARIA_ROLES.put("term", "A word or phrase with a corresponding definition");
        ARIA_ROLES.put("textbox", "A type of input that allows free-form text");
        ARIA_ROLES.put("timer", "A type of live region containing a numerical counter");
        ARIA_ROLES.put("toolbar", "A collection of commonly used function buttons");
        ARIA_ROLES.put("tooltip", "A contextual popup that displays a description");
        ARIA_ROLES.put("tree", "A type of list that may contain sub-level nested groups");
        ARIA_ROLES.put("treegrid", "A grid whose rows can be expanded and collapsed");
        ARIA_ROLES.put("treeitem", "An option item of a tree");
    }

    private static void initializeWCAGGuidelines() {
        WCAG_GUIDELINES.put("1.1.1", Arrays.asList("Non-text Content", "All non-text content has text alternative"));
        WCAG_GUIDELINES.put("1.3.1", Arrays.asList("Info and Relationships", "Information and relationships are programmatically determined"));
        WCAG_GUIDELINES.put("1.4.3", Arrays.asList("Contrast (Minimum)", "Text has contrast ratio of at least 4.5:1"));
        WCAG_GUIDELINES.put("2.1.1", Arrays.asList("Keyboard", "All functionality available from keyboard"));
        WCAG_GUIDELINES.put("2.1.2", Arrays.asList("No Keyboard Trap", "Keyboard focus can be moved away from component"));
        WCAG_GUIDELINES.put("2.4.1", Arrays.asList("Bypass Blocks", "Mechanism available to bypass blocks of content"));
        WCAG_GUIDELINES.put("2.4.2", Arrays.asList("Page Titled", "Web pages have titles that describe topic or purpose"));
        WCAG_GUIDELINES.put("2.4.3", Arrays.asList("Focus Order", "Focusable components receive focus in logical order"));
        WCAG_GUIDELINES.put("2.4.4", Arrays.asList("Link Purpose (In Context)", "Purpose of each link determined from link text"));
        WCAG_GUIDELINES.put("3.1.1", Arrays.asList("Language of Page", "Default human language of page programmatically determined"));
        WCAG_GUIDELINES.put("3.2.1", Arrays.asList("On Focus", "Component does not initiate change of context when receiving focus"));
        WCAG_GUIDELINES.put("3.2.2", Arrays.asList("On Input", "Changing setting does not automatically cause change of context"));
        WCAG_GUIDELINES.put("3.3.1", Arrays.asList("Error Identification", "Input errors are identified and described to user"));
        WCAG_GUIDELINES.put("3.3.2", Arrays.asList("Labels or Instructions", "Labels or instructions provided when content requires user input"));
        WCAG_GUIDELINES.put("4.1.1", Arrays.asList("Parsing", "Content can be parsed unambiguously"));
        WCAG_GUIDELINES.put("4.1.2", Arrays.asList("Name, Role, Value", "Name and role can be programmatically determined"));
    }

    /**
     * Generate comprehensive accessibility test report
     */
    public static String generateAccessibilityReport(List<AccessibilityTestResult> results) {
        StringBuilder report = new StringBuilder();
        
        report.append("=== ACCESSIBILITY TEST REPORT ===\n");
        report.append("Generated: ").append(new Date()).append("\n\n");
        
        int totalTests = results.size();
        int passedTests = 0;
        int totalViolations = 0;
        int totalWarnings = 0;
        
        for (AccessibilityTestResult result : results) {
            if (result.isPassed()) {
                passedTests++;
            }
            totalViolations += result.getViolations().size();
            totalWarnings += result.getWarnings().size();
        }
        
        report.append("OVERALL RESULTS:\n");
        report.append("- Total Tests: ").append(totalTests).append("\n");
        report.append("- Passed Tests: ").append(passedTests).append("\n");
        report.append("- Failed Tests: ").append(totalTests - passedTests).append("\n");
        report.append("- Total Violations: ").append(totalViolations).append("\n");
        report.append("- Total Warnings: ").append(totalWarnings).append("\n");
        report.append("- Success Rate: ").append(String.format("%.1f%%", (double) passedTests / totalTests * 100)).append("\n\n");
        
        report.append("DETAILED RESULTS:\n");
        for (AccessibilityTestResult result : results) {
            report.append("- ").append(result.getTestType()).append(": ");
            report.append(result.isPassed() ? "PASSED" : "FAILED").append("\n");
            report.append("  Summary: ").append(result.getSummary()).append("\n");
            
            if (!result.getViolations().isEmpty()) {
                report.append("  Violations:\n");
                for (String violation : result.getViolations()) {
                    report.append("    * ").append(violation).append("\n");
                }
            }
            
            if (!result.getWarnings().isEmpty()) {
                report.append("  Warnings:\n");
                for (String warning : result.getWarnings()) {
                    report.append("    * ").append(warning).append("\n");
                }
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
}