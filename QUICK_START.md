# ⚡ Quick Start Guide - DETECTALCHEMY

Get the Space Station Safety Monitor running in 5 minutes!

---

## 🏃 Immediate Setup

### Step 1: Open Project (30 seconds)

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `DETECTALCHEMY` folder
4. Wait for Gradle sync to complete (~2-3 minutes)

### Step 2: Connect Device (1 minute)

**Option A: Physical Device**

- Enable Developer Options on Android phone
- Enable USB Debugging
- Connect phone via USB
- Allow USB debugging prompt

**Option B: Emulator**

- Click "Device Manager" in Android Studio
- Create new device (Pixel 7 recommended)
- Select Android 11+ system image
- Launch emulator

### Step 3: Run App (1 minute)

1. Click the green Run button (▶️) or press `Shift+F10`
2. Select your device/emulator
3. Wait for build and installation (~30-60 seconds)
4. App launches automatically

### Step 4: Grant Permissions (10 seconds)

1. App will request Camera permission
2. Tap "Grant Permission"
3. You're ready to go! 🚀

---

## 🎮 Using the App

### What You'll See

1. **Camera View**: Live camera feed taking up the screen
2. **Top Bar**: App title and lighting mode toggle 💡
3. **Detection Dashboard**: Real-time stats showing:
    - Total detections
    - Critical items found
    - Missing critical items
    - Average confidence meter
4. **Alert Panel**: Bottom section showing safety alerts
5. **Bounding Boxes**: Colored rectangles around detected objects (mock data for now)

### Features to Try

#### 1. **Camera Detection** 📷

- Point camera at objects
- Mock detector will simulate finding safety equipment
- Watch bounding boxes appear with labels and confidence scores

#### 2. **Lighting Modes** 🌗

- Tap the 💡 icon in top-right corner
- Cycles through 3 modes:
    - **Normal** (white icon): Standard lighting
    - **Low Light** (cyan icon): Darker overlay simulating dim conditions
    - **Emergency** (red icon): Red tint for emergency lighting

#### 3. **Statistics Dashboard** 📊

- Top card shows live metrics
- Watch confidence meter update
- See critical items counter change

#### 4. **Alert System** ⚠️

- Alerts appear when critical items are "missing"
- Different severity levels (color-coded)
- Tap ✕ to dismiss individual alerts
- Tap "Clear All" to remove all alerts
- Critical alerts pulse for attention

---

## 🔧 Current State

### ✅ What Works Now

- **Full UI**: Complete, polished interface
- **Camera Preview**: Live camera feed using CameraX
- **Mock Detection**: Simulates YOLO object detection
- **Statistics**: Real-time tracking and visualization
- **Alerts**: Smart alert system with severity levels
- **Lighting Modes**: Three simulation modes
- **Permissions**: Proper camera permission handling
- **Animations**: Smooth transitions and effects

### 🔄 What's Simulated (Mock Data)

The app currently uses **mock detection** which means:

- Random safety objects are "detected" every ~500ms
- Bounding boxes appear at random positions
- Confidence scores are randomly generated (60-100%)
- Not actually analyzing camera frames (yet!)

This is **intentional** so you can:

- Demo the UI without a trained model
- Test all features independently
- Present to judges before model integration
- Develop and test the app framework

---

## 🚀 Next Steps for Real Detection

When you're ready to integrate your YOLO model, refer to `INTEGRATION_GUIDE.md`:

### Quick Integration Path

1. **Export your model** to TensorFlow Lite (.tflite)
2. **Add model file** to `app/src/main/assets/safety_detector.tflite`
3. **Update `ObjectDetector.kt`** with TFLite inference code
4. **Test and tune** detection parameters

Estimated integration time: 2-4 hours

---

## 🐛 Troubleshooting

### App Won't Build

```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

Or in Android Studio:

- Build → Clean Project
- Build → Rebuild Project

### Camera Permission Issues

- Go to device Settings → Apps → DETECTALCHEMY → Permissions
- Enable Camera manually
- Restart app

### Emulator Camera Not Working

- In emulator settings, enable "Virtual camera"
- Use front camera which provides a test pattern

### Gradle Sync Failed

- Check internet connection (needs to download dependencies)
- File → Invalidate Caches / Restart
- Update Android Studio to latest version

### App Crashes on Launch

Check Logcat (View → Tool Windows → Logcat) for errors:

- Common issue: Missing dependency after Gradle sync
- Solution: Sync project with Gradle files again

---

## 📱 Supported Devices

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 36)
- **Required Features**: Camera
- **Recommended**: Android 10+ for best performance

---

## 🎯 Demo Tips for Hackathon

### What to Show Judges

1. **Professional UI**
    - Clean, futuristic design
    - Smooth animations
    - Material Design 3

2. **Real-time Processing**
    - Live camera feed
    - Instant "detection" feedback
    - Dynamic statistics

3. **Smart Features**
    - Lighting simulation modes
    - Severity-based alert system
    - Confidence tracking

4. **Architecture Quality**
    - MVVM pattern
    - Kotlin Coroutines & Flow
    - Clean separation of concerns

5. **Integration Readiness**
    - Prepared for TFLite model
    - Falcon API structure planned
    - Scalable design

### Demo Script (2 minutes)

```
[LAUNCH APP]
"This is DETECTALCHEMY, a Space Station Safety Monitor..."

[SHOW CAMERA]
"Real-time camera feed with simulated YOLO detection..."

[TAP LIGHTING MODE]
"Multiple lighting modes to test model robustness..."

[SHOW STATS]
"Live dashboard tracks critical equipment and confidence..."

[SHOW ALERTS]
"Smart alert system notifies when critical items are missing..."

[CONCLUSION]
"Fully prepared for YOLO model integration and Falcon API connectivity"
```

---

## 💡 Tips

- **Battery**: App uses camera continuously, may drain battery
- **Performance**: Works best on Android 10+ devices
- **Testing**: Mock detector generates 1-4 objects every 500ms
- **Presentation**: Portrait mode is enforced for better UX

---

## 📞 Quick Reference

- **Project**: DETECTALCHEMY
- **Type**: Android Mobile App (Jetpack Compose)
- **Language**: Kotlin 2.0
- **Framework**: CameraX, TensorFlow Lite (ready)
- **Architecture**: MVVM with StateFlow
- **Status**: ✅ Full UI & Framework Complete, Ready for Model Integration

---

## 🎉 You're All Set!

The app is now running with full functionality (mock detection).

**Next actions:**

1. ✅ Test all features
2. ✅ Practice your demo
3. 🔄 Integrate real YOLO model (see `INTEGRATION_GUIDE.md`)
4. 🔄 Connect Falcon API (see `INTEGRATION_GUIDE.md`)

**Questions?** Check `README.md` for comprehensive documentation.

---

<div align="center">
  <p><strong>🚀 Happy Hacking! 🚀</strong></p>
</div>
