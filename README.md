# PayTR Test Automation Framework

PayTR için kapsamlı test otomasyon framework'ü. Bu proje UI, API, güvenlik, performans ve entegrasyon testlerini içerir.

> **🚀 Yeni CI/CD Çözümü**: Multi-strategy GitHub Actions workflow ile güvenilir test otomasyonu!

## 📋 İçindekiler

- [Özellikler](#özellikler)
- [Teknolojiler](#teknolojiler)
- [Kurulum](#kurulum)
- [Kullanım](#kullanım)
- [Test Süitleri](#test-süitleri)
- [CI/CD](#cicd)
- [Raporlama](#raporlama)
- [Docker](#docker)
- [Katkıda Bulunma](#katkıda-bulunma)

## 🚀 Özellikler

- **Kapsamlı Test Kapsama**: UI, API, güvenlik, performans testleri
- **Cross-Browser Desteği**: Chrome, Firefox, Edge
- **Paralel Test Çalıştırma**: Hızlı test execution
- **CI/CD Entegrasyonu**: GitHub Actions, Jenkins
- **Detaylı Raporlama**: Allure, HTML raporları
- **Docker Desteği**: Containerized test execution
- **Selenium Grid**: Distributed test execution
- **Page Object Model**: Maintainable UI test structure
- **Data-Driven Testing**: TestNG DataProvider
- **Cross-Platform**: Windows, macOS, Linux

## 🛠 Teknolojiler

- **Java 17**: Programming language
- **Maven**: Build tool
- **TestNG**: Test framework
- **Selenium WebDriver**: UI automation
- **RestAssured**: API testing
- **Allure**: Test reporting
- **Docker**: Containerization
- **GitHub Actions**: CI/CD
- **Jenkins**: CI/CD alternative

## 📦 Kurulum

### Gereksinimler

- Java 17+
- Maven 3.8+
- Chrome/Firefox/Edge browser
- Docker (opsiyonel)
- Allure (raporlama için)

### Proje Kurulumu

```bash
# Projeyi klonla
git clone <repository-url>
cd RestAssured

# Dependencies'leri yükle
mvn clean install

# Allure kurulumu (macOS)
brew install allure

# Allure kurulumu (Windows)
scoop install allure
```

## 🎯 Kullanım

### Temel Test Çalıştırma

```bash
# Tüm testleri çalıştır
mvn clean test

# Belirli test süitini çalıştır
mvn clean test -P smoke
mvn clean test -P security
mvn clean test -P performance
mvn clean test -P api

# Belirli browser ile çalıştır
mvn clean test -Dbrowser=firefox

# Headless modda çalıştır
mvn clean test -Dheadless=true

# Paralel çalıştırma
mvn clean test -Dparallel=methods -DthreadCount=5
```

### Script ile Çalıştırma

#### Linux/macOS
```bash
# Executable yap
chmod +x scripts/run-tests.sh

# Comprehensive testler
./scripts/run-tests.sh -s comprehensive -b chrome -e staging

# Security testler
./scripts/run-tests.sh -s security -h true

# Docker ile çalıştır
./scripts/run-tests.sh -d -s performance -b firefox

# Önceki sonuçları temizle ve çalıştır
./scripts/run-tests.sh -c -s smoke
```

#### Windows (Batch)
```cmd
# Comprehensive testler
scripts\run-tests.bat -s comprehensive -b chrome -e staging

# Security testler
scripts\run-tests.bat -s security -h true

# Docker ile çalıştır
scripts\run-tests.bat -d -s performance -b firefox
```

#### Windows (PowerShell)
```powershell
# Comprehensive testler
.\scripts\run-tests.ps1 -TestSuite comprehensive -Browser chrome -Environment staging

# Security testler
.\scripts\run-tests.ps1 -TestSuite security -Headless $true

# Docker ile çalıştır
.\scripts\run-tests.ps1 -UseDocker -TestSuite performance -Browser firefox
```

## 📊 Test Süitleri

### 1. Comprehensive Tests (`testng-comprehensive.xml`)
- Tüm test türlerini içerir
- Smoke, Functional, Security, Performance, API testleri
- 3 thread ile paralel çalışır

### 2. Security Tests (`testng-security.xml`)
- Güvenlik odaklı testler
- SQL Injection, XSS, Authentication testleri
- 2 thread ile paralel çalışır

### 3. Performance Tests (`testng-performance.xml`)
- Performans testleri
- Page load, API response time, memory usage
- 5 thread ile paralel çalışır

### 4. API Tests (`testng-api.xml`)
- API endpoint testleri
- CRUD operations, error handling
- 4 thread ile paralel çalışır

## 🔄 CI/CD

### GitHub Actions

Otomatik olarak çalışan workflow'lar:

- **Push/PR**: Smoke testler
- **Scheduled**: Günde 4 kez comprehensive testler
- **Manual**: Parametre ile test çalıştırma

```yaml
# Manual trigger örneği
gh workflow run paytr-tests.yml \
  -f test_suite=security \
  -f browser=firefox \
  -f environment=staging
```

### Jenkins

Jenkins pipeline özellikleri:

- Parameterized builds
- Parallel execution
- Allure reporting
- Email/Slack notifications
- Artifact archiving

## 📈 Raporlama

### Allure Reports

```bash
# Allure raporu oluştur
mvn allure:report

# Allure server başlat
mvn allure:serve

# Raporu aç
open target/allure-report/index.html
```

### HTML Reports

Test sonuçları otomatik olarak `reports/` klasöründe HTML formatında oluşturulur.

### Test Results

- **Surefire Reports**: `target/surefire-reports/`
- **Allure Results**: `target/allure-results/`
- **Screenshots**: `screenshots/`
- **Logs**: `logs/`

## 🐳 Docker

### Docker ile Test Çalıştırma

```bash
# Docker image oluştur
docker build -t paytr-tests .

# Container'da test çalıştır
docker run --rm \
  -v $(pwd)/test-results:/app/test-results \
  -v $(pwd)/reports:/app/reports \
  -e TEST_SUITE=comprehensive \
  -e BROWSER=chrome \
  paytr-tests
```

### Docker Compose

```bash
# Selenium Grid ile çalıştır
docker-compose up -d

# Testleri çalıştır
docker-compose run test-automation

# Servisleri durdur
docker-compose down
```

## 🏗 Proje Yapısı

```
RestAssured/
├── src/
│   ├── main/java/
│   └── test/
│       ├── java/
│       │   └── com/paytr/
│       │       ├── tests/           # Test sınıfları
│       │       ├── pages/           # Page Object Model
│       │       ├── utils/           # Utility sınıfları
│       │       └── listeners/       # Test listeners
│       └── resources/
│           ├── testng-*.xml         # TestNG konfigürasyonları
│           └── *.properties         # Test data dosyaları
├── scripts/                         # Execution scripts
├── .github/workflows/               # GitHub Actions
├── docker-compose.yml              # Docker Compose
├── Dockerfile                      # Docker image
├── Jenkinsfile                     # Jenkins pipeline
└── pom.xml                         # Maven konfigürasyonu
```

## 🧪 Test Sınıfları

- **PayTRUIElementsTest**: UI element testleri
- **PayTRSecurityTests**: Güvenlik testleri
- **PayTRPerformanceTests**: Performans testleri
- **PayTRAPITests**: API testleri
- **PayTRBoundaryTests**: Sınır durumu testleri
- **PayTRUsabilityTests**: Kullanılabilirlik testleri
- **PayTRIntegrationTests**: Entegrasyon testleri

## 📝 Test Data

Test verileri properties dosyalarında saklanır:

- `security-test-data.properties`: Güvenlik test verileri
- `performance-test-data.properties`: Performans test verileri
- `api-test-data.properties`: API test verileri

## 🔧 Konfigürasyon

### Maven Profiles

```bash
# Farklı profiller
mvn test -P smoke          # Smoke testler
mvn test -P security       # Security testler
mvn test -P performance    # Performance testler
mvn test -P api           # API testler
mvn test -P grid          # Selenium Grid
mvn test -P production    # Production environment
```

### System Properties

```bash
mvn test \
  -Dbrowser=firefox \
  -Denvironment=production \
  -Dheadless=false \
  -Dparallel=classes \
  -DthreadCount=3
```

## 🚨 Troubleshooting

### Yaygın Sorunlar

1. **WebDriver Issues**
   ```bash
   # WebDriverManager otomatik olarak driver'ları indirir
   # Manuel indirme gerekirse:
   # Chrome: https://chromedriver.chromium.org/
   # Firefox: https://github.com/mozilla/geckodriver/releases
   ```

2. **Memory Issues**
   ```bash
   # JVM memory artır
   export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
   ```

3. **Port Conflicts**
   ```bash
   # Selenium Grid port değiştir
   docker-compose up -d --scale selenium-hub=1
   ```

## 📞 Destek

- **Issues**: GitHub Issues kullanın
- **Documentation**: Wiki sayfalarını kontrol edin
- **Logs**: `logs/` klasöründeki log dosyalarını inceleyin

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 📊 Test Metrikleri

- **Test Coverage**: %95+
- **Execution Time**: ~15 dakika (comprehensive)
- **Parallel Threads**: 3-5
- **Supported Browsers**: Chrome, Firefox, Edge
- **Environments**: Development, Staging, Production

## 🎯 Roadmap

- [ ] Mobile testing desteği
- [ ] Visual regression testing
- [ ] API contract testing
- [ ] Load testing entegrasyonu
- [ ] AI-powered test generation

---

**PayTR Test Automation Framework** - Güvenilir, hızlı ve kapsamlı test otomasyonu için tasarlandı.