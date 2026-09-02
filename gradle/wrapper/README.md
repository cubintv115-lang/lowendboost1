# Gradle Wrapper

The file `gradle-wrapper.jar` is the actual Gradle wrapper executable and is
required for `gradlew` / `gradlew.bat` to work.

## How to get gradle-wrapper.jar

The wrapper jar is not committed to source control. Generate it once with one of
the methods below:

### Option A: Use a system Gradle install (recommended)

```bash
# Install Gradle 8.1+ once on your machine
# Then from the project root:
gradle wrapper --gradle-version 8.1.1
```

This creates `gradle/wrapper/gradle-wrapper.jar` and updates
`gradle-wrapper.properties` to match.

### Option B: Download the jar directly

Download the official wrapper jar matching Gradle 8.1.1 from the Gradle
distribution and place it at `gradle/wrapper/gradle-wrapper.jar`:

```bash
# from a temporary directory
curl -L -o gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.1.1/gradle/wrapper/gradle-wrapper.jar
mv gradle-wrapper.jar /path/to/project/gradle/wrapper/gradle-wrapper.jar
```

### Option C: Open the project in IntelliJ IDEA

IntelliJ will detect the missing wrapper jar and offer to download it
automatically the first time you sync the project.

Once `gradle-wrapper.jar` is in place, the `./gradlew` (Linux/macOS) and
`gradlew.bat` (Windows) scripts in the project root will work normally.
