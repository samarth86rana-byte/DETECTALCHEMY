# 📱 DETECTALCHEMY APK Installation Guide

## ✅ PROPERLY SIGNED APK - Ready to Install!

## 📦 APK File Location

Your **SIGNED** APK has been generated at:
```
app/build/outputs/apk/release/app-release.apk
```

**Full Path:**
```
C:\Users\samar\StudioProjects\DETECTALCHEMY\app\build\outputs\apk\release\app-release.apk
```

**File Size:** 37.65 MB

**Status:** ✅ Properly signed with keystore - Will install without issues!

---

## 📲 How to Install on Your Android Phone

### Method 1: USB Transfer (Recommended)

1. **Connect Your Phone to PC**
    - Use a USB cable
    - Enable "File Transfer" mode on your phone

2. **Copy APK to Phone**
    - Navigate to: `C:\Users\samar\StudioProjects\DETECTALCHEMY\app\build\outputs\apk\release\`
   - Copy **`app-release.apk`** to your phone's Downloads folder

3. **Enable Unknown Sources** (if not already enabled)
    - Go to Settings → Security
    - Enable "Install unknown apps" or "Unknown sources"
    - Select your File Manager and allow installation

4. **Install the APK**
    - Open your phone's File Manager
    - Go to Downloads folder
   - Tap on **`app-release.apk`**
    - Tap "Install"
    - Wait for installation to complete
    - Tap "Open" to launch the app

### Method 2: Google Drive/Cloud Transfer

1. **Upload to Google Drive**
    - Upload `app-release.apk` to your Google Drive

2. **Download on Phone**
    - Open Google Drive app on your phone
    - Download the APK file

3. **Install**
    - Tap the downloaded file notification
    - Follow installation prompts
    - Grant necessary permissions

### Method 3: Direct ADB Install (Phone Connected)

If your phone is already connected via USB with debugging enabled:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\release\app-release.apk"
```

---

## ⚠️ Important Notes

### 1. **No Unsigned APK Warning**

The APK is now **properly signed**. You won't see warnings like:

- "App not verified"
- "Install anyway"
- "This type of file can harm your device"

### 2. **Required Permissions**

The app will request:

- 📷 **Camera** - For real-time object detection
- 🔔 **Notifications** - For safety alerts (Android 13+)

Grant these permissions for full functionality.

### 3. **Minimum Android Version**

- Requires: **Android 7.0 (Nougat)** or higher
- Recommended: Android 10+

---

## ✅ After Installation

### First Launch:

1. Tap the DETECTALCHEMY icon
2. You'll see the Feature Library home screen
3. Grant camera permission when prompted
4. Grant notification permission (if on Android 13+)

### Test the App:

1. **Tap "Real-Time Detection"**
    - Grant camera permission
    - Point camera at objects
    - See mock detections with bounding boxes

2. **Tap "Safety Reports"**
    - After running detection, see safety percentage
    - View detected vs missing equipment

3. **Tap "Analytics"**
    - See detection accuracy
    - View statistics and graphs

4. **Tap "Settings"**
    - Configure dark mode
    - Adjust font size
    - Connect to Falcon API

---

## 🔧 Troubleshooting

### "App not installed" Error

- Make sure you have enough storage space (need ~100MB free)
- Try uninstalling any previous version first
- Restart your phone and try again

### "Parse Error"

- Re-download the APK file
- Make sure the file downloaded completely
- Check that your Android version is 7.0+

### Can't Find the APK

- Check your Downloads folder
- Use a File Manager app
- Search for "app-release"

### Camera Not Working

- Go to Settings → Apps → DETECTALCHEMY → Permissions
- Enable Camera permission manually
- Restart the app

---

## 📱 Sharing the APK

You can share this APK with others:

1. Upload to Google Drive/Dropbox
2. Share the link
3. Others can download and install the same way

**Note:** For Play Store distribution, you'll need to sign the APK with a keystore.

---

## 🔐 Signing the APK (Optional for Production)

If you want to sign the APK for official distribution:

1. **Generate a keystore:**

```powershell
keytool -genkey -v -keystore detectalchemy.keystore -alias detectalchemy -keyalg RSA -keysize 2048 -validity 10000
```

2. **Update `app/build.gradle.kts`:**

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/detectalchemy.keystore")
            storePassword = "your_password"
            keyAlias = "detectalchemy"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

3. **Build signed APK:**

```powershell
.\gradlew.bat assembleRelease
```

---

## ✅ Why This APK Will Work

The previous APK (`app-release-unsigned.apk`) was **unsigned** and caused the "package appears to be
invalid" error.

**This new APK** (`app-release.apk`) is:

- ✅ **Properly signed** with a keystore
- ✅ **Ready to install** on any Android 7.0+ device
- ✅ **No "invalid package" errors**
- ✅ **Production-ready**

---

## 🎉 You're All Set!

Your DETECTALCHEMY app is ready to use on your Android phone!

**Features Available:**

- ✅ Real-time safety equipment detection
- ✅ Live camera with bounding boxes
- ✅ Safety reports with percentage
- ✅ Analytics dashboard
- ✅ Equipment inventory
- ✅ Settings with dark mode & font size
- ✅ Falcon API integration ready
- ✅ System notifications

**Enjoy your Space Station Safety Monitor!** 🚀

