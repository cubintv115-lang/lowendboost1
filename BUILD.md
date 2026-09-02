# Hướng dẫn Build Nhanh

## Yêu cầu

- **JDK 17** ([Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17) khuyến nghị)
- **Internet** (Gradle tải dependencies lần đầu, ~500MB)
- **RAM trống** ≥ 4GB cho Gradle

## Lệnh build

### Tất cả version
```bash
./gradlew build
```

### Từng version
```bash
./gradlew :versions_1_17_1:build
./gradlew :versions_1_18_2:build
./gradlew :versions_1_19_2:build
./gradlew :versions_1_20_1:build
```

### Trên Windows (PowerShell hoặc cmd)
```cmd
gradlew.bat build
```

## Lệnh hữu ích

| Lệnh | Mô tả |
|------|-------|
| `./gradlew projects` | Liệt kê tất cả sub-projects |
| `./gradlew tasks` | Liệt kê tất cả task |
| `./gradlew :versions_1_20_1:tasks` | Tasks cho sub-project 1.20.1 |
| `./gradlew :versions_1_20_1:build --info` | Build với log chi tiết |
| `./gradlew :versions_1_20_1:runClient` | Chạy Minecraft client với mod (cần IDE trước) |
| `./gradlew :versions_1_20_1:runServer` | Chạy Minecraft server với mod |
| `./gradlew clean` | Xóa tất cả build outputs |
| `./gradlew :versions_1_20_1:jar` | Chỉ tạo jar (không deobfuscate) |

## Output

Sau khi build thành công, JAR nằm ở:
```
versions/1.20.1-forge/build/libs/lowendboost-1.0.0+mc1.20.1.jar
```

Để dùng, copy file JAR vào `%appdata%/.minecraft/mods/` rồi chạy Minecraft với profile Forge.

## Lỗi thường gặp

### 1. "Could not find tools.jar"
→ Bạn đang dùng JRE thay vì JDK. Cài lại JDK đầy đủ.

### 2. "Unsupported class file major version 65"
→ Java version quá mới (>17). Cài JDK 17 chính xác.

### 3. "Plugin [id: 'net.minecraftforge.gradle'] was not found"
→ Mất kết nối. Kiểm tra mạng và thử lại.

### 4. "BUILD FAILED: Could not resolve net.minecraftforge:forge:1.20.1-47.2.0"
→ Forge MDK không tải được. Có thể do mạng quốc tế chậm. Thử:
```bash
# Thêm Maven mirror vào settings.gradle repositories:
maven { url = 'https://maven.aliyun.com/repository/public' }
maven { url = 'https://maven.tencent.com/repository/maven-public' }
```

### 5. "OutOfMemoryError" khi build
Tăng heap cho Gradle trong `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8
```

### 6. Trên macOS: "cannot be opened because the developer cannot be verified"
```bash
xattr -d com.apple.quarantine gradlew
```

## Test trong IDE

### IntelliJ IDEA (khuyến nghị)
```bash
./gradlew :versions_1_20_1:genIntellijRuns
```
Sau đó mở project trong IntelliJ, chọn run config `1.20.1 - runClient`.

### Eclipse
```bash
./gradlew eclipse
```
Import vào Eclipse: **File → Import → Existing Projects**.

### VS Code
Mở thư mục project, cài extension "Extension Pack for Java" + "Gradle for Java", IDE tự nhận.
