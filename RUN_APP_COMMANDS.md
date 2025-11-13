# 🚀 Quick Commands to Run DETECTALCHEMY

## Build & Install Commands

### 1. Build the App

```powershell
.\gradlew.bat assembleDebug
```

### 2. Check Available Emulators

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
```

### 3. Start Emulator

```powershell
# Start Pixel_9_Pro_XL
Start-Process -FilePath "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -ArgumentList "-avd","Pixel_9_Pro_XL"

# OR Medium_Phone_API_36.0
Start-Process -FilePath "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -ArgumentList "-avd","Medium_Phone_API_36.0"
```

### 4. Check Connected Devices

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

### 5. Install App on Emulator

```powershell
.\gradlew.bat installDebug
```

### 6. Launch App

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.detectalchemy/.MainActivity
```

## One-Line Quick Start (After Emulator is Running)

```powershell
.\gradlew.bat installDebug && & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.detectalchemy/.MainActivity
```

## Debugging Commands

### View Logs

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" logcat -s "DetectAlchemy:D"
```

### Clear App Data

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm clear com.example.detectalchemy
```

### Uninstall App

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" uninstall com.example.detectalchemy
```

## Current Status

✅ **App is now running on emulator!**

### What to Expect:

1. Camera permission dialog will appear
2. Tap "Grant Permission"
3. Camera feed will show
4. Mock detection will start (random objects every 500ms)
5. Tap ⚙️ icon (top right) to cycle lighting modes
6. Watch alerts appear at the bottom
7. Statistics update in real-time at the top

### Features Available:

- ✅ Live camera preview
- ✅ Mock detection with bounding boxes
- ✅ Statistics dashboard
- ✅ Alert system
- ✅ Lighting simulation (Normal → Low Light → Emergency)
- ✅ All UI animations

---

**Note**: The app uses mock detection currently. To integrate a real YOLO model, see
`INTEGRATION_GUIDE.md`.
