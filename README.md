# LowEndBoost — Mod Tối Ưu Hiệu Năng Minecraft

> Mod Minecraft giúp **tăng FPS** và giảm giật lag cho laptop cấu hình yếu.
> Tương thích với **Forge + OptiFine**, hỗ trợ **Minecraft 1.17.1 → 1.20.1**.

---

## Mục lục

- [Tính năng](#tính-năng)
- [Cài đặt nhanh (dành cho người dùng)](#cài-đặt-nhanh-dành-cho-người-dùng)
- [Build từ source (dành cho dev)](#build-từ-source-dành-cho-dev)
  - [1. Cài JDK 17](#1-cài-jdk-17)
  - [2. Tải project](#2-tải-project)
  - [3. Build mod](#3-build-mod)
  - [4. Test trong IDE](#4-test-trong-ide)
- [Cấu hình](#cấu-hình)
- [Cấu trúc project](#cấu-trúc-project)
- [FAQ — Câu hỏi thường gặp](#faq--câu-hỏi-thường-gặp)
- [License](#license)

---

## Tính năng

✅ **Tự động phát hiện phần cứng** — mod sẽ kiểm tra CPU, RAM, GPU và áp dụng profile tối ưu phù hợp (LOW / MEDIUM / HIGH).

✅ **Giảm render distance thông minh** — tự động hạ render distance và simulation distance theo phần cứng.

✅ **Tối ưu particle** — chuyển từ "All" sang "Minimal" cho máy yếu.

✅ **Lazy chunk loading** — không load chunks ở xa khi không cần.

✅ **Tắt animation entity ở xa** — giảm tải CPU cho mob/player animation ngoài tầm nhìn.

✅ **Memory management** — kích hoạt GC định kỳ, giảm cache không cần thiết.

✅ **Giới hạn FPS** — chống nóng máy và tiết kiệm pin.

✅ **Multi-version** — 4 sub-projects build song song cho 1.17.1, 1.18.2, 1.19.2, 1.20.1.

✅ **Tương thích OptiFine** — không xung đột; có thể dùng cùng lúc.

✅ **Tiếng Việt + English** — đầy đủ localization.

### Kết quả mong đợi

| Phần cứng                  | FPS không mod | FPS có mod | Cải thiện |
|----------------------------|---------------|------------|-----------|
| Laptop 4GB RAM, Intel UHD  | 15-20         | 40-60+     | **~200%** |
| Laptop 8GB RAM, GTX 1050   | 30-40         | 80-120     | **~150%** |
| PC 16GB, RTX 3060          | 100-144       | 144 (giới hạn) | cap ổn định |

*(Kết quả thực tế tùy thuộc vào thế giới, mods khác, driver GPU, v.v.)*

---

## Cài đặt nhanh (dành cho người dùng)

> **Nếu bạn chỉ muốn dùng mod, không cần build.** Tải JAR prebuilt từ
> trang Releases của repo, copy vào thư mục `mods/` rồi chạy game.

### Bước 1: Cài Minecraft + Forge

1. Cài Minecraft launcher chính thức từ [minecraft.net](https://minecraft.net/).
2. Mở launcher, chọn tab **Installations** → **New installation**.
3. Chọn version **1.20.1** (hoặc bất kỳ version nào trong 1.17.1 → 1.20.1).
4. Bấm **Install**.
5. Tải Forge tương ứng từ [files.minecraftforge.net](https://files.minecraftforge.net/):
   - 1.20.1 → Forge 47.2.0
   - 1.19.2 → Forge 43.4.0
   - 1.18.2 → Forge 40.1.0
   - 1.17.1 → Forge 37.1.1
6. Chạy file JAR Forge vừa tải → chọn **Install client** → OK.

### Bước 2: Cài OptiFine (tùy chọn nhưng khuyến nghị)

1. Tải OptiFine HD U I7 (hoặc mới hơn) từ [optifine.net](https://optifine.net/).
2. Copy OptiFine JAR vào `%appdata%/.minecraft/mods/` (Windows) hoặc `~/.minecraft/mods/` (Linux/Mac).
3. Trong launcher, chọn profile **forge** rồi bấm **Play**.

### Bước 3: Cài LowEndBoost

1. Tải `lowendboost-1.X.X+mc1.20.1.jar` từ [Releases](../../releases).
2. Copy vào cùng thư mục `mods/` với Forge.
3. Chạy game.
4. Khi game khởi động, bạn sẽ thấy log trong console:
   ```
   [LowEndBoost] Detected: CPU cores=4, RAM=4096MB, HeapMax=2048MB, OS=Windows 10 amd64, Java=17.0.7, GPU=Intel(R) UHD Graphics 620 (Intel)
   [LowEndBoost] Detected: ... -> Tier=LOW
   [LowEndBoost] Config: Mode=AUTO, Tier=LOW, RenderDist=6, SimDist=4, Particles=0.10, MaxFPS=60, ...
   ```

### Bước 4: Cấu hình (tùy chọn)

Mở file `config/lowendboost.properties` trong thư mục `.minecraft` để tinh chỉnh.

---

## Build từ source (dành cho dev)

### 1. Cài JDK 17

Mod này yêu cầu **Java 17** trở lên (do Forge 1.17+ yêu cầu).

#### Windows

1. Tải **Adoptium Temurin 17 (LTS)** từ [adoptium.net](https://adoptium.net/temurin/releases/?version=17).
   - Chọn file `.msi` cho Windows.
2. Chạy installer, **bắt buộc tick** "Set JAVA_HOME variable" và "Add to PATH".
3. Mở **Command Prompt** mới và kiểm tra:
   ```bash
   java -version
   ```
   Kết quả phải hiện `openjdk version "17.x.x"`.

#### macOS

Dùng [Homebrew](https://brew.sh/):
```bash
brew install --cask temurin@17
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt install -y wget apt-transport-https
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/temurin.list
sudo apt update
sudo apt install -y temurin-17-jdk
```

Sau đó đặt `JAVA_HOME`:
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-17-amd64
echo "export JAVA_HOME=/usr/lib/jvm/temurin-17-amd64" >> ~/.bashrc
```

### 2. Tải project

```bash
git clone <repo-url> lowendboost
cd lowendboost
```

Hoặc tải ZIP từ GitHub và giải nén.

### 3. Build mod

```bash
# Build cho tất cả version (1.17.1, 1.18.2, 1.19.2, 1.20.1)
./gradlew build

# Hoặc chỉ build 1 version cụ thể
./gradlew :versions_1_20_1:build
```

JAR kết quả nằm ở:
```
versions/1.20.1-forge/build/libs/lowendboost-1.0.0+mc1.20.1.jar
versions/1.19.2-forge/build/libs/lowendboost-1.0.0+mc1.19.2.jar
versions/1.18.2-forge/build/libs/lowendboost-1.0.0+mc1.18.2.jar
versions/1.17.1-forge/build/libs/lowendboost-1.0.0+mc1.17.1.jar
```

Trên Windows, dùng `gradlew.bat` thay cho `./gradlew`:
```cmd
gradlew.bat :versions_1_20_1:build
```

### 4. Test trong IDE

#### IntelliJ IDEA (khuyến nghị)

1. Tải IntelliJ IDEA Community từ [jetbrains.com](https://www.jetbrains.com/idea/download/).
2. Mở project: **File → Open** → chọn thư mục gốc chứa `settings.gradle`.
3. IntelliJ sẽ tự nhận diện Gradle project. Đợi sync xong (lần đầu tải nhiều dependency).
4. Mở panel **Gradle** (góc phải), chạy task:
   ```
   :versions_1_20_1:Tasks > fg_runs > genIntellijRuns
   ```
   Sau khi chạy xong, restart IntelliJ để các run config xuất hiện.
5. Góc trên phải, chọn **run config** `1.20.1 - runClient` → bấm ▶ Play.
6. Minecraft sẽ khởi động với mod đã được gắn vào.

#### Eclipse

```bash
./gradlew eclipse
```

Sau đó **File → Import → Existing Projects into Workspace** chọn thư mục project.

#### VS Code

Cài extension **Extension Pack for Java** + **Gradle for Java**, mở thư mục project, IDE sẽ tự nhận.

---

## Cấu hình

File: `config/lowendboost.properties` (nằm trong thư mục `.minecraft/config/`).

```properties
# LowEndBoost configuration
# mode: AUTO | CUSTOM | OFF
# maxFps: 0 = unlimited, otherwise 30-240
#
mode=AUTO
maxFps=60
renderDistance=6
simulationDistance=4
particleMultiplier=0.1
entityAnimationDistance=8
enableChunkLazyLoad=true
aggressiveMemoryManagement=true
reduceEntityAnimations=true
enableAutoAdjust=true
disableDistantParticles=true
```

### Giải thích

| Key                          | Mặc định | Ý nghĩa                                            |
|------------------------------|----------|----------------------------------------------------|
| `mode`                       | AUTO     | AUTO = tự động, CUSTOM = dùng file này, OFF = tắt  |
| `maxFps`                     | 60       | 0 = không giới hạn, 30/60/144 = cap                |
| `renderDistance`             | 6        | Số chunks render (2-32). Máy yếu: 4-6              |
| `simulationDistance`         | 4        | Khoảng cách entities hoạt động (3-32)               |
| `particleMultiplier`         | 0.1      | 0.0 = tắt, 1.0 = full                              |
| `entityAnimationDistance`    | 8        | Tắt animation entities ở xa hơn X blocks           |
| `enableChunkLazyLoad`        | true     | Lazy load chunks                                    |
| `aggressiveMemoryManagement` | true     | GC thường xuyên + cache management                  |
| `reduceEntityAnimations`     | true     | Bật tắt animation ở xa                              |
| `enableAutoAdjust`           | true     | Tự giảm render distance khi FPS thấp                |
| `disableDistantParticles`    | true     | Tắt particle ở xa                                   |

---

## Cấu trúc project

```
fable 5/
├── build.gradle                 # Root build script
├── settings.gradle              # Sub-project declarations
├── gradle.properties            # Cấu hình chung
├── gradlew, gradlew.bat         # Gradle wrapper
├── README.md                    # File này
│
├── common/                      # Code chung, KHÔNG phụ thuộc MC
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/lowendboost/
│       │   ├── LowEndBoost.java              # Orchestrator
│       │   ├── config/
│       │   │   ├── LowEndBoostConfig.java
│       │   │   ├── ConfigLoader.java
│       │   │   ├── HardwareTier.java
│       │   │   └── OptimizationMode.java
│       │   ├── hardware/
│       │   │   ├── HardwareDetector.java
│       │   │   └── HardwareInfo.java
│       │   ├── optimizer/
│       │   │   ├── IOptimizer.java
│       │   │   ├── RenderDistanceOptimizer.java
│       │   │   ├── ParticleOptimizer.java
│       │   │   ├── ChunkLoadingOptimizer.java
│       │   │   ├── AnimationOptimizer.java
│       │   │   └── MemoryOptimizer.java
│       │   ├── platform/
│       │   │   └── Platform.java             # Interface trừu tượng
│       │   └── util/
│       │       └── FpsMonitor.java
│       └── resources/
│           ├── pack.mcmeta
│           └── assets/lowendboost/lang/
│               ├── en_us.json
│               └── vi_vn.json
│
└── versions/                    # Sub-projects cho từng MC version
    ├── 1.17.1-forge/           # Forge 37.x
    │   ├── build.gradle
    │   └── src/main/
    │       ├── java/com/lowendboost/platform/forge/
    │       │   ├── ForgePlatform1171.java    # Implement Platform
    │       │   └── LowEndBoostMod1171.java   # @Mod entry point
    │       └── resources/META-INF/mods.toml
    ├── 1.18.2-forge/           # Forge 40.x
    ├── 1.19.2-forge/           # Forge 43.x
    └── 1.20.1-forge/           # Forge 47.x
```

### Nguyên tắc thiết kế

- **Common module** chỉ chứa logic thuần Java, không có dependency Minecraft/Forge. Có thể compile/test mà không cần MDK.
- **Mỗi version Minecraft có 1 sub-project riêng** vì API của Minecraft thay đổi qua từng version (ví dụ: `Options.renderDistance` là `int` ở 1.18-1.19 nhưng là `Option<Integer>` ở 1.20+).
- **Bridge pattern** qua interface `Platform`: common code gọi `platform.setRenderDistance(...)` và implementation riêng của từng version sẽ dùng API phù hợp.

---

## FAQ — Câu hỏi thường gặp

### ❓ Mod có hoạt động với OptiFine không?

**Có.** Mod được thiết kế để không xung đột với OptiFine. Thực tế, dùng chung LowEndBoost + OptiFine cho kết quả tốt nhất trên máy yếu.

### ❓ Tôi cần bao nhiêu RAM để chạy mod?

Tối thiểu **2GB heap** cho Minecraft. Mod sẽ tự điều chỉnh nếu phát hiện RAM thấp.

### ❓ Mod có miễn phí không?

**Có, hoàn toàn miễn phí** và mã nguồn mở (MIT License).

### ❓ Tôi muốn thêm tối ưu mới, làm sao?

1. Tạo class mới trong `common/src/main/java/com/lowendboost/optimizer/`
   implement `IOptimizer`.
2. Đăng ký trong `LowEndBoost.java` (constructor).
3. Nếu cần hook API riêng của từng version, thêm method vào `Platform.java`
   và implement trong `ForgePlatform*.java` cho từng version.

### ❓ Build bị lỗi "Could not resolve net.minecraftforge:forge:..."

Có thể do mạng chậm hoặc bị firewall chặn. Forge MDK cần tải ~500MB từ Maven Central. Thử:
- Dùng VPN nếu mạng quốc tế chậm.
- Thêm Maven mirror trong `settings.gradle` (vd: Aliyun, Tencent).

### ❓ Tôi chỉ muốn dùng mod, không cần code. Tải ở đâu?

Xem mục [Cài đặt nhanh](#cài-đặt-nhanh-dành-cho-người-dùng). Tải JAR prebuilt từ trang Releases.

### ❓ Mod có an toàn cho multiplayer server không?

Mod chỉ chạy ở **client** (server chỉ cần thiết cho shutdown hook). Không ảnh hưởng gameplay multiplayer.

### ❓ Tại sao cần 4 sub-projects riêng?

Vì Forge MDK cho mỗi version là khác nhau, mappings khác nhau, ForgeGradle plugin khác version. Tách sub-project giúp build độc lập và dễ maintain.

### ❓ Sau khi tối ưu FPS không tăng?

Mở `config/lowendboost.properties`, đặt `mode=CUSTOM` rồi thử:
- Giảm `renderDistance` xuống 4.
- Đặt `particleMultiplier=0.0`.
- Đặt `maxFps=30` (cap thấp hơn có thể tăng stability).

Nếu vẫn không cải thiện, kiểm tra:
- Bạn có đang chơi với shader mod? Tắt shader giúp tăng FPS rất nhiều.
- Driver GPU đã cập nhật chưa?
- Có mod nào khác xung đột không? Thử chạy Minecraft với chỉ LowEndBoost + Forge.

---

## License

MIT License — xem [LICENSE](LICENSE) để biết chi tiết.

## Credits

Mod by **LowEndBoost Team**.
Sử dụng Forge MDK của MinecraftForge và LWJGL.
