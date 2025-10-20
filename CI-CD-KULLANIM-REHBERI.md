# 🚀 PayTR Test Automation CI/CD Kullanım Rehberi

## 📋 İçindekiler
1. [Genel Bakış](#genel-bakış)
2. [Lokal Test Çalıştırma](#lokal-test-çalıştırma)
3. [CI/CD Pipeline Kullanımı](#cicd-pipeline-kullanımı)
4. [Test Raporları](#test-raporları)
5. [Docker ile Test Çalıştırma](#docker-ile-test-çalıştırma)
6. [Test Sonuçlarını Yorumlama](#test-sonuçlarını-yorumlama)
7. [Sorun Giderme](#sorun-giderme)

## 🎯 Genel Bakış

PayTR Test Automation framework'ü, kod kalitesini ve uygulama stabilitesini sağlamak için kapsamlı CI/CD entegrasyonu sunar.

### ✅ Mevcut Test Suitleri
- **Smoke Tests**: Temel fonksiyonalite kontrolü (4 test)
- **Comprehensive Tests**: Kapsamlı test senaryoları
- **Performance Tests**: Performans ve yük testleri (11 test)
- **API Tests**: REST API endpoint testleri
- **Security Tests**: Güvenlik testleri
- **Integration Tests**: Entegrasyon testleri

## 🖥️ Lokal Test Çalıştırma

### Hızlı Başlangıç
```bash
# Test script'ini çalıştırılabilir yap
chmod +x ./scripts/run-tests.sh

# Smoke testleri çalıştır
./scripts/run-tests.sh -s smoke -b chrome -e staging -h true

# Comprehensive testleri çalıştır
./scripts/run-tests.sh -s comprehensive -b chrome -e staging -h true

# Performance testleri çalıştır
./scripts/run-tests.sh -s performance -b chrome -e staging -h true
```

### Parametreler
- `-s, --suite`: Test suite (smoke, comprehensive, performance, api, security)
- `-b, --browser`: Browser (chrome, firefox, edge)
- `-e, --environment`: Environment (staging, production, development)
- `-h, --headless`: Headless mode (true/false)

### Maven ile Test Çalıştırma
```bash
# Smoke testleri
mvn test -Dsuite=smoke -Dbrowser=chrome -Denvironment=staging

# Comprehensive testleri
mvn test -Dsuite=comprehensive -Dbrowser=chrome -Denvironment=staging

# Performance testleri
mvn test -Dsuite=performance -Dbrowser=chrome -Denvironment=staging
```

## 🔄 CI/CD Pipeline Kullanımı

### Otomatik Tetikleyiciler
CI/CD pipeline aşağıdaki durumlarda otomatik olarak çalışır:

1. **Push Events**: `main`, `develop`, `feature/*` branch'lerine push
2. **Pull Requests**: `main` ve `develop` branch'lerine PR
3. **Scheduled Runs**: Günde 4 kez (06:00, 12:00, 18:00, 00:00 UTC)
4. **Manual Trigger**: GitHub Actions'dan manuel çalıştırma

### Manuel Test Çalıştırma
1. GitHub repository'ye git
2. **Actions** sekmesine tıkla
3. **PayTR Test Automation CI/CD** workflow'unu seç
4. **Run workflow** butonuna tıkla
5. Test suite'ini seç (comprehensive, smoke, performance)
6. **Run workflow** ile başlat

### Workflow Dosyası
```yaml
# .github/workflows/paytr-tests.yml
name: PayTR Test Automation CI/CD
on:
  push:
    branches: [main, develop, feature/*]
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: '0 6,12,18,0 * * *'
  workflow_dispatch:
    inputs:
      test_suite:
        description: 'Test Suite to run'
        required: true
        default: 'comprehensive'
        type: choice
        options:
          - comprehensive
          - smoke
          - performance
          - api
          - security
```

## 📊 Test Raporları

### HTML Raporları
Test çalıştırıldıktan sonra `reports/` klasöründe HTML raporları oluşturulur:
- `test-report-YYYYMMDD_HHMMSS.html`: Ana test raporu
- `test-summary-YYYYMMDD_HHMMSS.txt`: Özet rapor

### Allure Raporları
```bash
# Allure raporu oluştur
allure generate target/allure-results --clean --output target/allure-report

# Allure raporunu aç
allure open target/allure-report
```

### Surefire Raporları
Maven Surefire plugin raporları `target/surefire-reports/` klasöründe bulunur.

## 🐳 Docker ile Test Çalıştırma

### Docker Compose ile Çalıştırma
```bash
# Tüm servisleri başlat
docker-compose up -d

# Sadece testleri çalıştır
docker-compose run paytr-tests

# Belirli test suite ile çalıştır
TEST_SUITE=smoke docker-compose run paytr-tests

# Servisleri durdur
docker-compose down
```

### Docker Build
```bash
# Docker image'ı build et
docker build -t paytr-test-automation .

# Container'ı çalıştır
docker run -v $(pwd)/reports:/app/reports paytr-test-automation
```

### Selenium Grid ile Test
Docker Compose otomatik olarak Selenium Grid kurar:
- **Hub**: http://localhost:4444
- **Chrome Node**: Otomatik scaling
- **Firefox Node**: Otomatik scaling

## 📈 Test Sonuçlarını Yorumlama

### Başarı Oranları
- **%90+ Başarı**: Mükemmel ✅
- **%75-89 Başarı**: İyi ⚠️
- **%60-74 Başarı**: Orta ⚠️
- **%60 Altı**: Kritik ❌

### Örnek Test Sonuçları
```
=== PayTR Test Automation Summary ===
Test Suite: smoke
Environment: staging
Browser: chrome
Total Tests: 4
Passed: 3
Failed: 1
Skipped: 0
Success Rate: 75%
Execution Time: 45.2 seconds
```

### Hata Analizi
Yaygın hata türleri:
1. **Element Not Found**: UI elementleri bulunamadı
2. **Timeout**: Sayfa yükleme zaman aşımı
3. **WebDriver Null**: WebDriver başlatma hatası
4. **Network Error**: Ağ bağlantı problemi

## 🔧 Sorun Giderme

### Yaygın Problemler

#### 1. WebDriver Null Hatası
```bash
# ChromeDriver'ı güncelle
webdriver-manager chrome --linkpath /usr/local/bin

# Browser binary path'ini kontrol et
which google-chrome
```

#### 2. Element Bulunamadı
- Sayfa yükleme süresini artır
- Element selector'ları güncelle
- Explicit wait kullan

#### 3. Test Environment Erişim Sorunu
- VPN bağlantısını kontrol et
- Environment URL'lerini doğrula
- Network ayarlarını kontrol et

#### 4. CI/CD Pipeline Hatası
```bash
# Logs'ları kontrol et
git log --oneline -10

# Workflow status'unu kontrol et
gh workflow list
gh run list
```

### Debug Modunda Çalıştırma
```bash
# Debug mode ile test çalıştır
./scripts/run-tests.sh -s smoke -b chrome -e staging -h false --debug

# Maven debug mode
mvn test -Dsuite=smoke -Ddebug=true -X
```

### Log Dosyaları
- **Test Logs**: `logs/test-execution.log`
- **WebDriver Logs**: `logs/webdriver.log`
- **Application Logs**: `logs/application.log`

## 📞 Destek

### Dokümantasyon
- [TestNG Documentation](https://testng.org/doc/)
- [Selenium Documentation](https://selenium-python.readthedocs.io/)
- [Allure Documentation](https://docs.qameta.io/allure/)

### İletişim
- **Test Team**: test-team@paytr.com
- **DevOps Team**: devops@paytr.com
- **GitHub Issues**: Repository issues sekmesi

---

## 🎉 Sonuç

PayTR Test Automation CI/CD sistemi ile:
- ✅ Otomatik test çalıştırma
- ✅ Kapsamlı raporlama
- ✅ Docker desteği
- ✅ Cross-platform uyumluluk
- ✅ Continuous monitoring

**Kod kalitesi ve uygulama stabilitesi artık otomatik olarak kontrol ediliyor!**