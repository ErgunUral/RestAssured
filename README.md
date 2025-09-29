# RestAssured Test Automation Project

Bu proje, REST API'leri test etmek için RestAssured kütüphanesi kullanılarak geliştirilmiş bir test otomasyon projesidir.

## 🚀 Özellikler

- **RestAssured**: REST API testleri için güçlü ve esnek kütüphane
- **TestNG**: Test framework'ü ve test yönetimi
- **Maven**: Proje yönetimi ve bağımlılık yönetimi
- **Jackson**: JSON işleme
- **Hamcrest**: Assertion matchers
- **Allure**: Test raporlama

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

### Tüm Testleri Çalıştırma
```bash
mvn test
```

### Belirli Test Sınıfını Çalıştırma
```bash
mvn test -Dtest=ApiTest
mvn test -Dtest=UserApiTest
```

### TestNG XML ile Çalıştırma
```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
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

### API Testleri
- ✅ Tüm postları getirme
- ✅ Tek post getirme
- ✅ Post oluşturma
- ✅ Post güncelleme
- ✅ Post silme
- ⚠️ Olmayan post getirme (devre dışı)

### User API Testleri
- ✅ Tüm kullanıcıları getirme
- ✅ Tek kullanıcı getirme
- ✅ Kullanıcı oluşturma
- ✅ Kullanıcı postlarını getirme
- ✅ Kullanıcı albümlerini getirme
- ✅ Kullanıcı güncelleme

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