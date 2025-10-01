# PayTR Test Automation Project

[![CI/CD Pipeline](https://github.com/ErgunUral/RestAssured/actions/workflows/ci-cd-pipeline.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/ci-cd-pipeline.yml)
[![Nightly Tests](https://github.com/ErgunUral/RestAssured/actions/workflows/nightly-tests.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/nightly-tests.yml)
[![Security Scan](https://github.com/ErgunUral/RestAssured/actions/workflows/security-scan.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/security-scan.yml)
[![Release Tests](https://github.com/ErgunUral/RestAssured/actions/workflows/release-tests.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/release-tests.yml)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![TestNG](https://img.shields.io/badge/TestNG-7.8+-green.svg)](https://testng.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.15+-yellow.svg)](https://selenium.dev/)
[![Allure](https://img.shields.io/badge/Allure-2.24+-purple.svg)](https://docs.qameta.io/allure/)

Bu proje, PayTR ödeme sistemi için kapsamlı test otomasyonu sağlayan, REST API ve Web UI testlerini içeren gelişmiş bir test otomasyon projesidir.

## 🚀 Özellikler

### 🧪 Test Framework'leri
- **Selenium WebDriver**: Web UI test otomasyonu
- **RestAssured**: REST API testleri için güçlü ve esnek kütüphane
- **TestNG**: Test framework'ü ve test yönetimi
- **Maven**: Proje yönetimi ve bağımlılık yönetimi

### 🔧 Test Araçları
- **Jackson**: JSON işleme
- **Hamcrest**: Assertion matchers
- **Allure**: Gelişmiş test raporlama
- **WebDriverManager**: Otomatik driver yönetimi

### 🎯 PayTR Spesifik Testler
- **Payment Process Tests**: Ödeme süreci testleri
- **Virtual POS Tests**: Virtual POS entegrasyonu testleri
- **Security Tests**: Güvenlik ve penetrasyon testleri
- **Card Validation Tests**: Kart doğrulama testleri
- **Installment Tests**: Taksit işlemleri testleri

### 🚀 CI/CD Pipeline
- **GitHub Actions**: Otomatik test çalıştırma
- **Nightly Tests**: Gece testleri
- **Release Tests**: Release öncesi testler
- **Security Scans**: Güvenlik taramaları
- **Performance Tests**: Performans testleri

## 📁 Proje Yapısı

```
RestAssured/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   └── com/example/
│       │       ├── tests/
│       │       │   ├── BaseTest.java
│       │       │   ├── ApiTest.java
│       │       │   └── UserApiTest.java
│       │       └── utils/
│       │           └── TestUtils.java
│       └── resources/
│           └── testng.xml
├── pom.xml
└── README.md
```

## 🛠️ Kurulum

### Gereksinimler

- Java 11 veya üzeri
- Maven 3.6 veya üzeri

### Kurulum Adımları

1. Projeyi klonlayın:
```bash
git clone <repository-url>
cd RestAssured
```

2. Bağımlılıkları yükleyin:
```bash
mvn clean compile
```

3. Test sınıflarını derleyin:
```bash
mvn test-compile
```

## 🧪 Testleri Çalıştırma

### PayTR Test Suite'leri

#### Hızlı Smoke Testler (5 dakika)
```bash
mvn test -Dsurefire.suiteXmlFiles=testng-paytr-smoke.xml
```

#### Kapsamlı Regression Testler
```bash
mvn test -Dsurefire.suiteXmlFiles=testng-paytr-regression.xml
```

#### Tam Test Suite'i
```bash
mvn test -Dsurefire.suiteXmlFiles=testng-paytr-full.xml
```

#### Basit Test Suite'i
```bash
mvn test -Dsurefire.suiteXmlFiles=testng-paytr-simple.xml
```

### Belirli Test Grupları

#### Payment Testleri
```bash
mvn test -Dgroups=payment
```

#### Security Testleri
```bash
mvn test -Dgroups=security
```

#### UI Testleri
```bash
mvn test -Dgroups=ui
```

#### API Testleri
```bash
mvn test -Dgroups=api
```

### Paralel Test Çalıştırma
```bash
mvn test -DthreadCount=3 -Dparallel=methods
```

### Headless Browser Testleri
```bash
mvn test -Dheadless=true
```

### Belirli Test Sınıfını Çalıştırma
```bash
mvn test -Dtest=PayTRSmokeTest
mvn test -Dtest=PayTRPaymentProcessTest
mvn test -Dtest=PayTRSecurityTest
```

## 📊 Test Raporları

### Allure Raporu Oluşturma
```bash
mvn allure:report
```

### Allure Raporunu Görüntüleme
```bash
mvn allure:serve
```

### HTML Test Raporları
```bash
# Surefire raporları
open target/surefire-reports/index.html

# PayTR özel raporları
open PayTR_Test_Report_Final.md
```

## 🚀 CI/CD Pipeline

### GitHub Actions Workflow'ları

#### 1. Ana CI/CD Pipeline (`ci-cd-pipeline.yml`)
- **Tetikleyiciler**: Push, Pull Request, Schedule (günlük)
- **Test Suite'leri**: Smoke, Regression testleri
- **Özellikler**:
  - Java 17 kurulumu
  - Maven dependency cache
  - Paralel test execution
  - Allure raporları
  - Screenshot capture on failure
  - Slack notifications

```bash
# Manuel çalıştırma
gh workflow run ci-cd-pipeline.yml -f test_suite=smoke
```

#### 2. Nightly Tests (`nightly-tests.yml`)
- **Tetikleyici**: Her gece saat 01:00 (UTC)
- **Özellikler**:
  - Cross-browser testing (Chrome, Firefox)
  - Performance monitoring
  - Comprehensive test coverage
  - Email notifications on failure

#### 3. Release Tests (`release-tests.yml`)
- **Tetikleyiciler**: Release tags, Manual dispatch
- **Özellikler**:
  - Pre-release validation
  - Smoke → Regression → Comprehensive test flow
  - Security validation
  - Performance validation
  - Release approval process

#### 4. Security Scan (`security-scan.yml`)
- **Tetikleyiciler**: Push, PR, Weekly schedule
- **Özellikler**:
  - OWASP Dependency Check
  - Code security analysis
  - Secrets detection
  - Web security tests
  - ZAP security scanning

### Pipeline Durumu

| Workflow | Status | Açıklama |
|----------|--------|----------|
| CI/CD Pipeline | [![CI/CD](https://github.com/ErgunUral/RestAssured/actions/workflows/ci-cd-pipeline.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/ci-cd-pipeline.yml) | Ana test pipeline'ı |
| Nightly Tests | [![Nightly](https://github.com/ErgunUral/RestAssured/actions/workflows/nightly-tests.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/nightly-tests.yml) | Gece testleri |
| Release Tests | [![Release](https://github.com/ErgunUral/RestAssured/actions/workflows/release-tests.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/release-tests.yml) | Release testleri |
| Security Scan | [![Security](https://github.com/ErgunUral/RestAssured/actions/workflows/security-scan.yml/badge.svg)](https://github.com/ErgunUral/RestAssured/actions/workflows/security-scan.yml) | Güvenlik taramaları |

### Workflow Tetikleme

#### Manuel Workflow Çalıştırma
```bash
# GitHub CLI ile
gh workflow run ci-cd-pipeline.yml -f test_suite=regression
gh workflow run nightly-tests.yml -f environment=staging
gh workflow run security-scan.yml -f scan_type=comprehensive
```

#### Release Workflow
```bash
# Release tag oluştur
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

## 🔧 Konfigürasyon

### Base URL Değiştirme

`BaseTest.java` dosyasında `RestAssured.baseURI` değerini değiştirerek farklı API'leri test edebilirsiniz:

```java
RestAssured.baseURI = "https://your-api-url.com";
```

### Test Verileri

Test verileri `TestUtils.java` sınıfında yardımcı metodlar kullanılarak oluşturulabilir:

```java
String randomEmail = TestUtils.generateRandomEmail();
String randomString = TestUtils.generateRandomString(10);
```

## 📝 Test Sınıfları

### BaseTest.java
- Temel test konfigürasyonu
- Request ve Response spesifikasyonları
- Ortak setup metodları

### ApiTest.java
- Post CRUD operasyonları
- GET, POST, PUT, DELETE testleri
- Response validasyonları

### UserApiTest.java
- User API testleri
- Kullanıcı CRUD operasyonları
- İlişkili veri testleri (posts, albums)

### TestUtils.java
- Yardımcı metodlar
- JSON işleme
- Random veri üretimi
- Response debug metodları

## 🎯 Test Senaryoları

### 💳 PayTR Payment Tests
- ✅ Payment form validation
- ✅ Credit card validation (Visa, MasterCard, American Express)
- ✅ Installment options testing
- ✅ Payment process flow
- ✅ Payment success scenarios
- ✅ Payment failure scenarios
- ✅ Multi-currency support

### 🏦 Virtual POS Tests
- ✅ Virtual POS integration
- ✅ Bank selection functionality
- ✅ POS terminal simulation
- ✅ Transaction processing
- ✅ Receipt generation
- ✅ Error handling

### 🔒 Security Tests
- ✅ XSS (Cross-Site Scripting) protection
- ✅ SQL Injection prevention
- ✅ CSRF (Cross-Site Request Forgery) protection
- ✅ SSL/TLS certificate validation
- ✅ Input sanitization
- ✅ Authentication bypass attempts
- ✅ Session management

### 🖥️ UI Tests
- ✅ Login functionality
- ✅ Form validations
- ✅ Navigation testing
- ✅ Responsive design
- ✅ Browser compatibility
- ✅ Element interactions
- ✅ Page load performance

### 🔌 API Tests
- ✅ Payment API endpoints
- ✅ Authentication API
- ✅ Transaction API
- ✅ Webhook testing
- ✅ Rate limiting
- ✅ Error response validation
- ✅ API versioning

### 📊 Performance Tests
- ✅ Load testing
- ✅ Stress testing
- ✅ Response time validation
- ✅ Concurrent user simulation
- ✅ Memory usage monitoring
- ✅ Database performance

## 🔍 Debugging

### Response Detaylarını Görüntüleme
```java
Response response = given()
    .when()
    .get("/posts/1");
    
TestUtils.printResponse(response);
```

### Verbose Logging
```java
given()
    .log().all()
    .when()
    .get("/posts/1")
    .then()
    .log().all();
```

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 📞 İletişim

Sorularınız için issue açabilir veya pull request gönderebilirsiniz.

---

**Not**: Bu proje JSONPlaceholder (https://jsonplaceholder.typicode.com) API'sini kullanarak örnek testler içermektedir. Gerçek projelerinizde kendi API endpoint'lerinizi kullanın.