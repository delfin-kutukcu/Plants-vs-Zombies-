# Plants vs Zombies — BIL 211 Projesi

> **YouTube Demo Linki:** (https://www.youtube.com/watch?v=ngkyZIII2NE)
---
**Delfin Kütükcü**
## Projeyi Çalıştırma

### Gereksinimler
- Java 8 veya üzeri (JDK kurulu olmalı)

### Derleme ve Çalıştırma (Tek Adım)
```
build_and_run.bat
```

### Ayrı Ayrı
```
compile.bat   ← Önce derle
run.bat       ← Sonra çalıştır
```

> **Not:** Herhangi bir kod değişikliğinden sonra mutlaka `compile.bat` çalıştırılmalıdır.

---

## Proje Yapısı

```
PvZ/
├── src/pvz/
│   ├── Main.java                  — Giriş noktası
│   ├── GameConstants.java         — Tüm oyun sabitleri
│   ├── GameFrame.java             — Ana pencere (CardLayout ekran yönetimi)
│   ├── ImageLoader.java           — Görsel yükleme ve önbellekleme
│   ├── model/
│   │   ├── GameState.java         — Serileştirilebilir oyun durumu
│   │   ├── Sun.java               — Güneş nesnesi
│   │   ├── Projectile.java        — Mermi nesnesi
│   │   ├── plants/
│   │   │   ├── Plant.java         — Soyut bitki taban sınıfı
│   │   │   ├── PeaShooter.java
│   │   │   ├── SunFlower.java
│   │   │   ├── WallNut.java
│   │   │   ├── SnowPea.java
│   │   │   └── CherryBomb.java
│   │   └── zombies/
│   │       ├── Zombie.java        — Soyut zombi taban sınıfı
│   │       ├── BasicZombie.java
│   │       ├── FastZombie.java
│   │       ├── RunZombie.java
│   │       └── TankZombie.java
│   ├── threads/
│   │   └── WaveThread.java        — Dalga üretim thread'i (oyun thread'inden ayrı)
│   ├── io/
│   │   └── SaveManager.java       — Kaydet / Yükle (Java Serialization)
│   └── ui/
│       ├── MenuPanel.java         — Başlangıç ekranı
│       └── GamePanel.java         — Oyun ekranı + oyun döngüsü
├── resources/                     — Görsel dosyalar (.png)
├── out/                           — Derlenmiş .class dosyaları
├── compile.bat
├── run.bat
└── build_and_run.bat
```

---

## Uygulanan Özellikler

### 1. Oyun Alanı
- 5 satır × 9 sütun grid
- Her hücre: en fazla 1 bitki, birden fazla zombi / mermi / güneş

### 2. Bitkiler

| Bitki | Can | Maliyet | Özellik |
|-------|-----|---------|---------|
| PeaShooter | 300 | 100 ☀ | Her 1.5 sn'de mermi atar |
| SunFlower | 300 | 50 ☀ | Her ~11 sn'de güneş üretir |
| WallNut | 4000 | 50 ☀ | Zombileri engeller, saldırmaz |
| SnowPea | 300 | 175 ☀ | Yavaşlatıcı mermi atar |
| CherryBomb | — | 150 ☀ | 2 sn sonra patlar, hücresindeki tüm zombileri öldürür |

- Fare ile bitki seçilir ve grid'e yerleştirilir
- Güneş yetersizse yerleştirme başarısız olur
- Aynı hücreye ikinci bitki yerleştirilemez
- Kürek (shovel) ile yerleştirilmiş bitkiler kaldırılabilir

### 3. Zombiler

| Zombi | Can | Hız | Dalga |
|-------|-----|-----|-------|
| BasicZombie | 200 | Düşük | Normal + Dalga 1-2 |
| FastZombie | 200 | Orta | Normal + Dalga 1-2 |
| RunZombie | 200 | Yüksek | Dalga 2 |
| TankZombie | 600 | Orta-Yüksek | Dalga 2 |

- Sağdan sola hareket ederler
- Önlerinde bitki varsa saldırırlar, bitki ölünce ilerlerler
- Sol sınıra ulaşan zombi oyunu bitirir

### 4. Zombi Üretim Sistemi

**Normal Üretim:**
- Yalnızca BasicZombie ve FastZombie, rastgele satırda
- 1. dalga öncesi: her ~10 sn'de bir
- 2. dalga öncesi: her ~9 sn'de bir
- Her 8 normal zombiden sonra dalga başlar; dalga aktifken normal üretim durur

**Dalgalar (oyun thread'inden ayrı WaveThread):**

| | Dalga 1 | Dalga 2 |
|-|---------|---------|
| Zombi sayısı | 8 | 13 |
| Spawn aralığı | 3 sn | 1.8 sn (daha sık) |
| HP çarpanı | ×1.0 | ×1.6 (daha dayanıklı) |
| Türler | Basic + Fast | Basic + Fast + Run + Tank |

- Her dalga için yeni thread başlatılır, dalga bitince thread sonlanır
- Thread yalnızca üretim yapar, hareket (movement) yapmaz
- Son dalga tamamlanıp tüm zombiler temizlenince **Zafer** ekranı gösterilir

### 5. Güneş Sistemi
- Başlangıç güneşi: 150 ☀
- SunFlower kendi hücresinde güneş üretir (her ~11 sn)
- Güneş ikonuna tıklayınca +25 güneş toplanır ve nesne silinir
- Güncel güneş miktarı üst panelde gösterilir

### 6. Mermiler
- Soldan sağa hareket eder
- Zombiyle çarpışınca: zombi hasar alır, mermi yok olur
- SnowPea mermisi ek olarak zombiyi geçici yavaşlatır

### 7. Oyun Durumları
| Ekran | Açıklama |
|-------|----------|
| Başlangıç Ekranı | Yeni Oyun / Devam Et |
| Oyun | Ana oyun ekranı |
| Durdurma | P tuşu veya Durdur butonu |
| Yenilgi | Zombi sol sınıra ulaştığında |
| Zafer | Tüm dalgalar temizlendiğinde |

### 8. Kaydet / Yükle
- Oyun duraklatıldığında **Kaydet** butonu ile `pvz_save.dat` dosyasına kaydedilir
- Kaydedilen veri: grid'deki tüm bitkiler (tür + konum), sahadaki zombiler (konum + can), mermiler, güneşler, dalga numarası, güneş miktarı — eksiksiz oyun durumu
- Ana menüden **Devam Et** ile kaldığı yerden devam edilir
- Hatalı / eksik dosyada `IOException` yakalanır, kullanıcıya hata mesajı gösterilir

---

## Kontroller

| Eylem | Kontrol |
|-------|---------|
| Bitki seç | Üst panelden tıkla |
| Bitki yerleştir | Grid hücresine tıkla |
| Bitki kaldır | Kürek seç → hücreye tıkla |
| Güneş topla | Güneş ikonuna tıkla |
| Duraklat / Devam | **P** tuşu veya ekrandaki buton |
| Kaydet | Kaydet butonu (önce durdur) |
| Ana Menü | Menü butonu |

## Yazar
* **Delfin Kütükcü**
