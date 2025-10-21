# PayTR Test Senaryoları Implementasyon Rehberi

## 📋 Genel Bakış

Bu dokümantasyon, PayTR test senaryolarının pratik implementasyonu için detaylı rehber niteliğindedir. 67 test senaryosunun TestNG ve Selenium WebDriver ile nasıl uygulanacağı, test data management, CI/CD entegrasyonu ve best practice'ler açıklanmaktadır.

---

## 🏗️ Test Framework Mimarisi

### 1. Proje Yapısı
```
src/test/java/
├── com/paytr/tests/
│   ├── functional/
│   │   ├── LoginTests.java
│   │   ├── PaymentTests.java
│   │   └── UserManagementTests.java
│   ├── security/
│   │   ├── SQLInjectionTests.java
│   │   ├── XSSTests.java
│   │   └── AuthenticationTests.java
│   ├── performance/
│   │   ├── LoadTests.java
│   │   └── StressTests.java
│   ├── api/
│   │   ├── RESTAPITests.java
│   │   └── GraphQLTests.java
│   ├── multicurrency/
│   │   ├── CurrencyConversionTests.java
│   │   └── LocalizationTests.java
│   ├── security3ds/
│   │   ├── ThreeDSecureTests.java
│   │   └── FraudDetectionTests.java
│   ├── webhook/
│   │   ├── WebhookDeliveryTests.java
│   │   └── NotificationTests.java
│   ├── accessibility/
│   │   ├── WCAGComplianceTests.java
│   │   └── KeyboardNavigationTests.java
│   ├── mobile/
│   │   ├── ResponsiveDesignTests.java
│   │   └── CrossBrowserTests.java
│   ├── database/
│   │   ├── TransactionIntegrityTests.java
│   │   └── DataBackupTests.java
│   ├── compliance/
│   │   ├── PCIDSSTests.java
│   │   └── GDPRTests.java
│   ├── disaster/
│   │   ├── FailoverTests.java
│   │   └── RecoveryTests.java
│   ├── ratelimiting/
│   │   ├── APIRateLimitTests.java
│   │   └── DDoSProtectionTests.java
│   ├── monitoring/
│   │   ├── PerformanceMonitoringTests.java
│   │   └── AlertingTests.java
│   ├── config/
│   │   ├── ConfigManagementTests.java
│   │   └── EnvironmentTests.java
│   └── audit/
│       ├── AuditLoggingTests.java
│       └── LogSecurityTests.java
├── com/paytr/utils/
│   ├── WebDriverManager.java
│   ├── TestDataFactory.java
│   ├── DatabaseUtils.java
│   ├── APIUtils.java
│   └── ReportUtils.java
├── com/paytr/pages/
│   ├── LoginPage.java
│   ├── PaymentPage.java
│   ├── DashboardPage.java
│   └── BasePage.java
└── com/paytr/listeners/
    ├── TestListener.java
    ├── RetryAnalyzer.java
    └── ScreenshotListener.java
```

### 2. Base Test Class
```java
package com.paytr.tests;

import com.paytr.utils.WebDriverManager;
import com.paytr.utils.TestDataFactory;
import org.testng.annotations.*;
import org.openqa.selenium.WebDriver;

public abstract class BaseTest {
    
    protected WebDriver driver;
    protected TestDataFactory testDataFactory;
    
    @BeforeMethod
    public void setUp() {
        driver = WebDriverManager.getDriver();
        testDataFactory = new TestDataFactory();
        driver.manage().window().maximize();
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            WebDriverManager.quitDriver();
        }
    }
    
    @BeforeClass
    public void classSetUp() {
        // Class level setup
    }
    
    @AfterClass
    public void classTearDown() {
        // Class level cleanup
    }
}
```

---

## 🧪 Test Implementasyon Örnekleri

### 1. Fonksiyonel Test Örneği - Multi-Factor Authentication

```java
package com.paytr.tests.functional;

import com.paytr.tests.BaseTest;
import com.paytr.pages.LoginPage;
import com.paytr.pages.DashboardPage;
import com.paytr.pages.TwoFactorPage;
import org.testng.annotations.Test;
import org.testng.Assert;

public class LoginTests extends BaseTest {
    
    @Test(groups = {"functional", "critical", "authentication"}, 
          priority = 1,
          description = "FT-001: Multi-Factor Authentication Giriş Testi")
    public void testMFALogin() {
        // Test ID: FT-001
        LoginPage loginPage = new LoginPage(driver);
        TwoFactorPage twoFactorPage = new TwoFactorPage(driver);
        DashboardPage dashboardPage = new DashboardPage(driver);
        
        // Test Data
        String email = testDataFactory.getMFAUserEmail();
        String password = testDataFactory.getMFAUserPassword();
        String mfaCode = testDataFactory.generateMFACode();
        
        // Test Steps
        loginPage.navigateToLoginPage();
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        loginPage.clickLoginButton();
        
        // Verify 2FA page is displayed
        Assert.assertTrue(twoFactorPage.isTwoFactorPageDisplayed(), 
                         "2FA page should be displayed");
        
        twoFactorPage.enterMFACode(mfaCode);
        twoFactorPage.clickVerifyButton();
        
        // Verify successful login
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(), 
                         "User should be redirected to dashboard");
        Assert.assertEquals(dashboardPage.getWelcomeMessage(), 
                           "Hoşgeldiniz, " + testDataFactory.getMFAUserName());
    }
    
    @Test(groups = {"functional", "security", "negative"}, 
          priority = 2,
          description = "FT-002: Account Lockout Mechanism Testi")
    public void testAccountLockout() {
        // Test ID: FT-002
        LoginPage loginPage = new LoginPage(driver);
        
        String email = testDataFactory.getValidUserEmail();
        String wrongPassword = "WrongPassword123";
        
        loginPage.navigateToLoginPage();
        
        // Attempt 5 failed logins
        for (int i = 1; i <= 5; i++) {
            loginPage.enterEmail(email);
            loginPage.enterPassword(wrongPassword);
            loginPage.clickLoginButton();
            
            if (i < 5) {
                Assert.assertTrue(loginPage.isErrorMessageDisplayed(), 
                                "Error message should be displayed for attempt " + i);
            }
        }
        
        // 6th attempt should show account locked message
        loginPage.enterEmail(email);
        loginPage.enterPassword(wrongPassword);
        loginPage.clickLoginButton();
        
        Assert.assertTrue(loginPage.isAccountLockedMessageDisplayed(), 
                         "Account locked message should be displayed");
        Assert.assertEquals(loginPage.getAccountLockedMessage(), 
                           "Hesabınız güvenlik nedeniyle kilitlenmiştir");
    }
}
```

### 2. Güvenlik Test Örneği - SQL Injection

```java
package com.paytr.tests.security;

import com.paytr.tests.BaseTest;
import com.paytr.pages.LoginPage;
import com.paytr.utils.SecurityTestUtils;
import org.testng.annotations.Test;
import org.testng.Assert;

public class SQLInjectionTests extends BaseTest {
    
    @Test(groups = {"security", "critical", "sql-injection"}, 
          priority = 1,
          description = "ST-001: Login Form SQL Injection Testi")
    public void testLoginSQLInjection() {
        // Test ID: ST-001
        LoginPage loginPage = new LoginPage(driver);
        SecurityTestUtils securityUtils = new SecurityTestUtils();
        
        String[] sqlPayloads = {
            "admin'--",
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT * FROM users--"
        };
        
        loginPage.navigateToLoginPage();
        
        for (String payload : sqlPayloads) {
            loginPage.clearEmailField();
            loginPage.enterEmail(payload);
            loginPage.enterPassword("test123");
            loginPage.clickLoginButton();
            
            // Verify SQL injection is blocked
            Assert.assertFalse(loginPage.isLoginSuccessful(), 
                             "SQL injection should be blocked for payload: " + payload);
            Assert.assertTrue(loginPage.isErrorMessageDisplayed(), 
                            "Error message should be displayed for payload: " + payload);
            
            // Verify no database error information is leaked
            String errorMessage = loginPage.getErrorMessage();
            Assert.assertFalse(securityUtils.containsDatabaseErrorInfo(errorMessage), 
                              "Error message should not contain database information");
        }
    }
    
    @Test(groups = {"security", "critical", "xss"}, 
          priority = 2,
          description = "ST-003: Stored XSS Testi")
    public void testStoredXSS() {
        // Test ID: ST-003
        // Implementation for stored XSS testing
        // This would involve profile update functionality
    }
}
```

### 3. API Test Örneği - RESTful API

```java
package com.paytr.tests.api;

import com.paytr.tests.BaseTest;
import com.paytr.utils.APIUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.Assert;

public class RESTAPITests extends BaseTest {
    
    private APIUtils apiUtils = new APIUtils();
    
    @Test(groups = {"api", "versioning", "high"}, 
          priority = 1,
          description = "AT-001: API Versioning Compatibility Testi")
    public void testAPIVersioningCompatibility() {
        // Test ID: AT-001
        String endpoint = "/api/payments";
        String paymentData = testDataFactory.getPaymentRequestJSON();
        
        // Test v1 API
        Response v1Response = RestAssured
            .given()
                .header("API-Version", "v1")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiUtils.getAuthToken())
                .body(paymentData)
            .when()
                .post(endpoint)
            .then()
                .extract().response();
        
        Assert.assertEquals(v1Response.getStatusCode(), 200, 
                           "V1 API should return 200");
        
        // Test v2 API
        Response v2Response = RestAssured
            .given()
                .header("API-Version", "v2")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiUtils.getAuthToken())
                .body(paymentData)
            .when()
                .post(endpoint)
            .then()
                .extract().response();
        
        Assert.assertEquals(v2Response.getStatusCode(), 200, 
                           "V2 API should return 200");
        
        // Verify backward compatibility
        String v1PaymentId = v1Response.jsonPath().getString("paymentId");
        String v2PaymentId = v2Response.jsonPath().getString("paymentId");
        
        Assert.assertNotNull(v1PaymentId, "V1 should return paymentId");
        Assert.assertNotNull(v2PaymentId, "V2 should return paymentId");
        
        // Verify deprecation warnings for v1
        Assert.assertTrue(v1Response.getHeaders().hasHeaderWithName("Deprecation"), 
                         "V1 should include deprecation header");
    }
}
```

### 4. Performance Test Örneği - Load Testing

```java
package com.paytr.tests.performance;

import com.paytr.tests.BaseTest;
import com.paytr.utils.PerformanceUtils;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class LoadTests extends BaseTest {
    
    @Test(groups = {"performance", "load", "critical"}, 
          priority = 1,
          description = "PT-001: Peak Load Payment Processing Testi")
    public void testPeakLoadPaymentProcessing() {
        // Test ID: PT-001
        int concurrentUsers = 1000;
        int testDurationMinutes = 15;
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Long> responseTimes = new ArrayList<>();
        List<Boolean> results = new ArrayList<>();
        
        PerformanceUtils perfUtils = new PerformanceUtils();
        
        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Simulate payment processing
                    boolean paymentResult = perfUtils.processPayment(
                        testDataFactory.getRandomPaymentData()
                    );
                    
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;
                    
                    synchronized (responseTimes) {
                        responseTimes.add(responseTime);
                        results.add(paymentResult);
                    }
                    
                } catch (Exception e) {
                    synchronized (results) {
                        results.add(false);
                    }
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(testDurationMinutes, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Analyze results
        double averageResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long successfulPayments = results.stream()
            .mapToLong(result -> result ? 1 : 0)
            .sum();
        
        double successRate = (double) successfulPayments / results.size() * 100;
        
        // Assertions
        Assert.assertTrue(averageResponseTime < 3000, 
                         "Average response time should be less than 3 seconds. Actual: " + averageResponseTime + "ms");
        Assert.assertTrue(successRate > 99.0, 
                         "Success rate should be greater than 99%. Actual: " + successRate + "%");
        
        System.out.println("Load Test Results:");
        System.out.println("Concurrent Users: " + concurrentUsers);
        System.out.println("Average Response Time: " + averageResponseTime + "ms");
        System.out.println("Success Rate: " + successRate + "%");
    }
}
```

---

## 🔧 Utility Classes

### 1. WebDriver Manager
```java
package com.paytr.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class WebDriverManager {
    
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    public static WebDriver getDriver() {
        return driver.get();
    }
    
    public static void setDriver(String browserName) {
        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-popup-blocking");
                driver.set(new ChromeDriver(chromeOptions));
                break;
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver());
                break;
                
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver.set(new EdgeDriver());
                break;
                
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserName);
        }
    }
    
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
```

### 2. Test Data Factory
```java
package com.paytr.utils;

import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.util.Random;

public class TestDataFactory {
    
    private Faker faker = new Faker();
    private Random random = new Random();
    
    // User Data
    public String getValidUserEmail() {
        return "test.user@paytr.com";
    }
    
    public String getMFAUserEmail() {
        return "mfa.user@paytr.com";
    }
    
    public String getMFAUserPassword() {
        return "SecurePass123!";
    }
    
    public String getMFAUserName() {
        return "Test User";
    }
    
    public String generateMFACode() {
        return String.format("%06d", random.nextInt(999999));
    }
    
    // Payment Data
    public PaymentData getRandomPaymentData() {
        return PaymentData.builder()
            .cardNumber("4111111111111111")
            .expiryMonth("12")
            .expiryYear("25")
            .cvv("123")
            .amount(new BigDecimal(faker.number().randomDouble(2, 10, 1000)))
            .currency("TL")
            .build();
    }
    
    public String getPaymentRequestJSON() {
        return "{\n" +
               "  \"amount\": 100.00,\n" +
               "  \"currency\": \"TL\",\n" +
               "  \"cardNumber\": \"4111111111111111\",\n" +
               "  \"expiryMonth\": \"12\",\n" +
               "  \"expiryYear\": \"25\",\n" +
               "  \"cvv\": \"123\"\n" +
               "}";
    }
    
    // Security Test Data
    public String[] getSQLInjectionPayloads() {
        return new String[] {
            "admin'--",
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT * FROM users--",
            "admin'; INSERT INTO users VALUES('hacker','password'); --"
        };
    }
    
    public String[] getXSSPayloads() {
        return new String[] {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert('XSS')>",
            "';alert('XSS');//"
        };
    }
    
    // Multi-Currency Data
    public CurrencyTestData[] getMultiCurrencyTestData() {
        return new CurrencyTestData[] {
            new CurrencyTestData("USD", "TL", 100.0, "18.50"),
            new CurrencyTestData("EUR", "TL", 50.0, "19.80"),
            new CurrencyTestData("GBP", "TL", 75.0, "22.30")
        };
    }
}
```

### 3. Database Utils
```java
package com.paytr.utils;

import java.sql.*;
import java.util.Properties;

public class DatabaseUtils {
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/paytr_test";
    private static final String DB_USER = "test_user";
    private static final String DB_PASSWORD = "test_password";
    
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("ssl", "false");
        
        return DriverManager.getConnection(DB_URL, props);
    }
    
    public static void executeQuery(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        }
    }
    
    public static ResultSet executeSelectQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    public static void cleanupTestData() throws SQLException {
        String[] cleanupQueries = {
            "DELETE FROM payments WHERE created_at < NOW() - INTERVAL '1 day'",
            "DELETE FROM users WHERE email LIKE '%test%'",
            "DELETE FROM audit_logs WHERE created_at < NOW() - INTERVAL '1 day'"
        };
        
        for (String query : cleanupQueries) {
            executeQuery(query);
        }
    }
    
    public static boolean verifyTransactionIntegrity(String transactionId) throws SQLException {
        String query = "SELECT COUNT(*) FROM payments WHERE transaction_id = ? AND status = 'COMPLETED'";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }
        }
        return false;
    }
}
```

---

## 📊 TestNG Configuration Files

### 1. Master Test Suite
```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="PayTR_Master_Test_Suite" parallel="methods" thread-count="5">
    
    <parameter name="browser" value="chrome"/>
    <parameter name="baseUrl" value="https://zeus-uat.paytr.com"/>
    <parameter name="environment" value="uat"/>
    
    <listeners>
        <listener class-name="com.paytr.listeners.TestListener"/>
        <listener class-name="com.paytr.listeners.ScreenshotListener"/>
    </listeners>
    
    <test name="Smoke_Tests" preserve-order="true">
        <groups>
            <run>
                <include name="smoke"/>
                <include name="critical"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests.*"/>
        </packages>
    </test>
    
    <test name="Functional_Tests">
        <groups>
            <run>
                <include name="functional"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests.functional"/>
        </packages>
    </test>
    
    <test name="Security_Tests">
        <groups>
            <run>
                <include name="security"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests.security"/>
        </packages>
    </test>
    
    <test name="API_Tests">
        <groups>
            <run>
                <include name="api"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests.api"/>
        </packages>
    </test>
    
    <test name="Performance_Tests">
        <groups>
            <run>
                <include name="performance"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests.performance"/>
        </packages>
    </test>
    
</suite>
```

### 2. Regression Test Suite
```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="PayTR_Regression_Suite" parallel="classes" thread-count="3">
    
    <parameter name="browser" value="chrome"/>
    <parameter name="baseUrl" value="https://zeus-uat.paytr.com"/>
    
    <test name="Critical_Path_Regression">
        <groups>
            <run>
                <include name="regression"/>
                <include name="critical"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests"/>
        </packages>
    </test>
    
</suite>
```

### 3. Nightly Test Suite
```xml
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="PayTR_Nightly_Suite" parallel="methods" thread-count="10">
    
    <parameter name="browser" value="chrome"/>
    <parameter name="baseUrl" value="https://zeus-uat.paytr.com"/>
    
    <test name="Comprehensive_Tests">
        <groups>
            <run>
                <exclude name="manual"/>
                <exclude name="wip"/>
            </run>
        </groups>
        <packages>
            <package name="com.paytr.tests"/>
        </packages>
    </test>
    
</suite>
```

---

## 🚀 CI/CD Pipeline Entegrasyonu

### 1. GitHub Actions Workflow
```yaml
name: PayTR Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * *'  # Nightly at 2 AM

jobs:
  smoke-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Run Smoke Tests
        run: mvn test -Dsuite=smoke-tests.xml
      
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: smoke-test-results
          path: target/surefire-reports/
  
  functional-tests:
    runs-on: ubuntu-latest
    needs: smoke-tests
    strategy:
      matrix:
        browser: [chrome, firefox]
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run Functional Tests
        run: mvn test -Dsuite=functional-tests.xml -Dbrowser=${{ matrix.browser }}
      
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: functional-test-results-${{ matrix.browser }}
          path: target/surefire-reports/
  
  security-tests:
    runs-on: ubuntu-latest
    needs: smoke-tests
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run Security Tests
        run: mvn test -Dsuite=security-tests.xml
      
      - name: Upload Security Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: security-test-results
          path: target/surefire-reports/
  
  performance-tests:
    runs-on: ubuntu-latest
    if: github.event_name == 'schedule'
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Run Performance Tests
        run: mvn test -Dsuite=performance-tests.xml
      
      - name: Upload Performance Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: performance-test-results
          path: target/surefire-reports/
  
  generate-report:
    runs-on: ubuntu-latest
    needs: [functional-tests, security-tests]
    if: always()
    steps:
      - uses: actions/checkout@v3
      
      - name: Download All Test Results
        uses: actions/download-artifact@v3
      
      - name: Generate Consolidated Report
        run: |
          # Script to generate consolidated test report
          python scripts/generate_report.py
      
      - name: Upload Consolidated Report
        uses: actions/upload-artifact@v3
        with:
          name: consolidated-test-report
          path: reports/
```

### 2. Maven POM Configuration
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.paytr</groupId>
    <artifactId>paytr-test-automation</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <testng.version>7.8.0</testng.version>
        <selenium.version>4.15.0</selenium.version>
        <webdrivermanager.version>5.6.2</webdrivermanager.version>
        <restassured.version>5.3.2</restassured.version>
        <allure.version>2.24.0</allure.version>
    </properties>
    
    <dependencies>
        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
        </dependency>
        
        <!-- Selenium WebDriver -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>
        
        <!-- WebDriverManager -->
        <dependency>
            <groupId>io.github.bonigarcia</groupId>
            <artifactId>webdrivermanager</artifactId>
            <version>${webdrivermanager.version}</version>
        </dependency>
        
        <!-- REST Assured -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${restassured.version}</version>
        </dependency>
        
        <!-- Allure TestNG -->
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-testng</artifactId>
            <version>${allure.version}</version>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.6.0</version>
        </dependency>
        
        <!-- Test Data Generation -->
        <dependency>
            <groupId>com.github.javafaker</groupId>
            <artifactId>javafaker</artifactId>
            <version>1.0.2</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.2</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>${suite}</suiteXmlFile>
                    </suiteXmlFiles>
                    <systemPropertyVariables>
                        <browser>${browser}</browser>
                        <baseUrl>${baseUrl}</baseUrl>
                        <environment>${environment}</environment>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>io.qameta.allure</groupId>
                <artifactId>allure-maven</artifactId>
                <version>2.12.0</version>
            </plugin>
        </plugins>
    </build>
    
    <profiles>
        <profile>
            <id>smoke</id>
            <properties>
                <suite>src/test/resources/testng-smoke.xml</suite>
            </properties>
        </profile>
        
        <profile>
            <id>regression</id>
            <properties>
                <suite>src/test/resources/testng-regression.xml</suite>
            </properties>
        </profile>
        
        <profile>
            <id>nightly</id>
            <properties>
                <suite>src/test/resources/testng-nightly.xml</suite>
            </properties>
        </profile>
    </profiles>
</project>
```

---

## 📈 Test Reporting ve Monitoring

### 1. Allure Report Configuration
```java
package com.paytr.listeners;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import com.paytr.utils.WebDriverManager;

public class AllureListener implements ITestListener {
    
    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = WebDriverManager.getDriver();
        if (driver != null) {
            saveScreenshot(driver);
            savePageSource(driver);
        }
        saveTestLog(result);
    }
    
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] saveScreenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
    
    @Attachment(value = "Page Source", type = "text/html")
    public String savePageSource(WebDriver driver) {
        return driver.getPageSource();
    }
    
    @Attachment(value = "Test Log", type = "text/plain")
    public String saveTestLog(ITestResult result) {
        return "Test: " + result.getMethod().getMethodName() + "\n" +
               "Status: " + getTestStatus(result.getStatus()) + "\n" +
               "Duration: " + (result.getEndMillis() - result.getStartMillis()) + "ms\n" +
               "Error: " + (result.getThrowable() != null ? result.getThrowable().getMessage() : "None");
    }
    
    private String getTestStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASSED";
            case ITestResult.FAILURE: return "FAILED";
            case ITestResult.SKIP: return "SKIPPED";
            default: return "UNKNOWN";
        }
    }
}
```

### 2. Custom Test Reporter
```java
package com.paytr.reporting;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class CustomTestReporter implements IReporter {
    
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        try {
            generateHTMLReport(suites, outputDirectory);
            generateJSONReport(suites, outputDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void generateHTMLReport(List<ISuite> suites, String outputDirectory) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>PayTR Test Report</title></head><body>");
        html.append("<h1>PayTR Test Execution Report</h1>");
        
        for (ISuite suite : suites) {
            html.append("<h2>Suite: ").append(suite.getName()).append("</h2>");
            // Add suite details, test results, etc.
        }
        
        html.append("</body></html>");
        
        try (FileWriter writer = new FileWriter(outputDirectory + "/custom-report.html")) {
            writer.write(html.toString());
        }
    }
    
    private void generateJSONReport(List<ISuite> suites, String outputDirectory) throws IOException {
        // Generate JSON report for CI/CD integration
    }
}
```

Bu kapsamlı implementasyon rehberi, PayTR test senaryolarının pratik uygulanması için gerekli tüm bileşenleri içermektedir. Test framework'ü modern yazılım geliştirme standartlarına uygun olarak tasarlanmış ve CI/CD pipeline entegrasyonu için optimize edilmiştir.