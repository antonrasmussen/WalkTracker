# Android SDK & Command-Line Tools (Without Android Studio)

Use this guide to build and test WalkTracker from the command line on macOS. You need **Java 17** (required by current Android Gradle Plugin) and the Android SDK.

---

## 1. Prerequisites

- **Java 17**  
  Check: `java -version`  
  Install: [Adoptium](https://adoptium.net/) or `brew install openjdk@17`, then set `JAVA_HOME` (e.g. `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`).

---

## 2. Download Command-Line Tools

1. Go to: **https://developer.android.com/studio#command-line-tools-only**
2. Under **Command line tools only**, download the **Mac** zip (e.g. `commandlinetools-mac-*_latest.zip`).
3. Pick a permanent SDK location, for example:
   ```bash
   export ANDROID_HOME="$HOME/Library/Android/sdk"
   mkdir -p "$ANDROID_HOME"
   cd "$ANDROID_HOME"
   ```
4. Unzip the downloaded file. You should get a `cmdline-tools` folder.
5. **Important:** The Gradle Android plugin expects this layout:
   ```
   $ANDROID_HOME/cmdline-tools/latest/   ← "latest" is required
   ```
   So either:
   - Unzip so that the contents (e.g. `bin`, `lib`) end up in `$ANDROID_HOME/cmdline-tools/latest/`, **or**
   - If the zip extracted as `cmdline-tools/`, rename it:
     ```bash
     mv cmdline-tools cmdline-tools-latest
     mkdir -p cmdline-tools
     mv cmdline-tools-latest cmdline-tools/latest
     ```
   Result: `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager` exists.

---

## 3. Set Environment Variables

Add to `~/.zshrc` or `~/.bash_profile`:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
```

Then run `source ~/.zshrc` (or your shell’s rc file).

---

## 4. Install SDK Components (for WalkTracker)

WalkTracker uses **compileSdk 34** and **targetSdk 34**. Install the platform and build-tools:

```bash
sdkmanager --sdk_root="$ANDROID_HOME" "platforms;android-34"
sdkmanager --sdk_root="$ANDROID_HOME" "build-tools;34.0.0"
sdkmanager --sdk_root="$ANDROID_HOME" "platform-tools"
```

Accept licenses when prompted (`y`). To accept all non-interactively:

```bash
yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses
```

Optional (for emulator):

```bash
sdkmanager --sdk_root="$ANDROID_HOME" "emulator"
sdkmanager --sdk_root="$ANDROID_HOME" "system-images;android-34;google_apis;arm64-v8a"
```

---

## 5. Verify

```bash
echo $ANDROID_HOME
sdkmanager --sdk_root="$ANDROID_HOME" --list_installed
```

Then from the WalkTracker repo:

```bash
cd /path/to/WalkTracker
./gradlew tasks
./gradlew test
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 6. Run on a Device (No IDE)

- Enable **Developer options** and **USB debugging** on the phone.
- Connect via USB, then:
  ```bash
  adb devices
  ./gradlew installDebug
  ```
- Launch the app from the device.

---

## 7. Run on an Emulator (No IDE)

Create an AVD and start it from the command line:

```bash
# List available system images (install one if needed, see step 4)
avdmanager list target

# Create AVD (example; adjust id/device/abi as needed)
avdmanager create avd -n WalkTracker_34 -k "system-images;android-34;google_apis;arm64-v8a" -d "pixel_6"

# Start emulator (run in background or another terminal)
emulator -avd WalkTracker_34
```

Then `./gradlew installDebug` and open the app on the emulator.

---

## References

- [Android command-line tools (official)](https://developer.android.com/studio#command-line-tools-only)
- [sdkmanager](https://developer.android.com/studio/command-line/sdkmanager)
- [avdmanager](https://developer.android.com/studio/command-line/avdmanager)
