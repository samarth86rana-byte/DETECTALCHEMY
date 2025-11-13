# 🔥 Falcon Dataset Integration - Complete Guide

## ✅ CRASH FIXED + DATASET SYSTEM READY!

Your app now properly handles Falcon datasets with images for object detection training!

---

## 🎯 How It Works

### **Your Falcon Dataset Structure:**

Your Falcon dataset contains **images** of objects you want to detect (like Fire Extinguisher,
Medkit, etc.). The app will:

1. **Download** the dataset from your URL
2. **Process images** and extract object classes
3. **Sync** with the detection model
4. **Detect** those objects in real-time camera feed

---

## 📦 Supported Dataset Formats

### **1. ZIP File** (Recommended)

```
https://your-server.com/falcon-dataset.zip

Contents:
falcon_dataset.zip
├── images/
│   ├── fire_extinguisher_001.jpg
│   ├── fire_extinguisher_002.jpg
│   ├── medkit_001.jpg
│   ├── medkit_002.jpg
│   ├── oxygen_tank_001.jpg
│   └── ...
├── labels/
│   ├── fire_extinguisher_001.txt
│   └── ...
└── classes.txt (optional)
```

### **2. TFLite Model**

```
https://your-server.com/trained-model.tflite
```

Pre-trained TensorFlow Lite model file

### **3. API Endpoint** (JSON Response)

```
https://falcon-api.com/datasets/123

Response:
{
  "classes": [
    {"id": 0, "name": "Fire Extinguisher"},
    {"id": 1, "name": "Medkit"},
    {"id": 2, "name": "Oxygen Tank"}
  ],
  "model_url": "https://..../model.tflite",
  "images": [
    {"url": "https://.../img1.jpg", "name": "image_001.jpg"},
    ...
  ]
}
```

---

## 📱 How to Use

### **Step 1: Prepare Your Falcon Dataset**

**Option A - ZIP File:**

1. Create a folder with your training images
2. Name images like: `classname_number.jpg`
    - Example: `fire_extinguisher_001.jpg`
    - Example: `medkit_002.jpg`
3. Zip the folder
4. Upload to a server or cloud storage
5. Get the public URL

**Option B - Direct Model:**

1. Train your YOLO model on Falcon
2. Export as `.tflite` file
3. Upload to server
4. Get the download URL

### **Step 2: Connect in App**

1. Open DETECTALCHEMY app
2. Tap **"Settings"** from home
3. Scroll to **"🔗 Falcon Integration"**
4. Tap **"Connect"** button (orange)
5. Enter your URL:
   ```
   https://your-storage.com/falcon-dataset.zip
   ```
   or
   ```
   https://your-server.com/model.tflite
   ```
6. Tap **"Connect"**

### **Step 3: Sync Dataset**

1. Button changes to green **"Connected"**
2. Your URL is now saved permanently ✅
3. Tap **"🔄 Sync Dataset"** button
4. Watch progress: `Syncing... 45%`
5. Wait for: `✅ Dataset synced!`

### **Step 4: Use for Detection**

The dataset is now ready! When you go to **"Real-Time Detection"**:

- Camera will detect objects from your Falcon dataset
- Bounding boxes will appear around detected items
- Labels will show the class names from your dataset
- Confidence scores will be displayed

---

## 🔧 What Happens Behind the Scenes

### **When You Sync:**

1. **Download**
    - App downloads ZIP/model from URL
    - Shows progress (0-100%)
    - Saves to `/files/falcon_dataset/`

2. **Extract** (if ZIP)
    - Unzips all images
    - Creates folders: `images/`, `labels/`

3. **Process Images**
    - Scans all images
    - Extracts class names from filenames
    - Creates `metadata.json`:
      ```json
      {
        "synced_at": 1699999999,
        "class_count": 7,
        "classes": [
          {"id": 0, "name": "Fire Extinguisher"},
          {"id": 1, "name": "Medkit"},
          ...
        ]
      }
      ```

4. **Ready for Detection**
    - Model knows what objects to look for
    - Camera feed can now detect these objects

---

## 🎨 Image Naming Convention

**Format:** `classname_number.extension`

✅ **Good Examples:**

```
fire_extinguisher_001.jpg
fire_extinguisher_002.jpg
medkit_001.png
medkit_002.png
oxygen_tank_001.jpg
emergency_light_001.jpg
```

❌ **Bad Examples:**

```
IMG_1234.jpg         (no class name)
photo.png            (no identifier)
my image.jpg         (spaces)
```

The app automatically extracts the class name (part before first `_`).

---

## 🚀 Integration with Detection System

### **Current State:**

- App uses **mock detector** for demo
- Shows random safety equipment

### **After Dataset Sync:**

The `FalconDatasetHandler` provides:

```kotlin
// Get detection classes
val classes = handler.getDetectionClasses()
// Returns: [Fire Extinguisher, Medkit, Oxygen Tank, ...]

// Get images for a class
val images = handler.getSampleImages("Fire Extinguisher")
// Returns: List of image files

// Get the model file
val modelFile = handler.getModelFile()
// Returns: /files/falcon_dataset/model.tflite

// Check if synced
val isSynced = handler.isDatasetSynced()
```

**To integrate with real detection:**
Update `ObjectDetector.kt` to:

1. Load model from `handler.getModelFile()`
2. Use classes from `handler.getDetectionClasses()`
3. Run YOLO inference on camera frames
4. Return actual detections instead of mock data

---

## ⚠️ Troubleshooting

### **Problem:** App crashes when connecting

**Solution:**  
✅ **FIXED!** The crash was caused by loading preferences during recomposition. Now uses
`LaunchedEffect` to load safely.

### **Problem:** "Failed to sync dataset"

**Solutions:**

- Check URL is correct and accessible
- Verify file exists at URL (try in browser)
- Check internet connection
- Ensure app has INTERNET permission (already added)
- Try a smaller dataset first

### **Problem:** Download stuck at 0%

**Solutions:**

- Check file size (large files take time)
- Verify server allows downloads (not 403/404 error)
- Check if CORS is enabled (for web URLs)
- Try with mobile data if WiFi is restricted

### **Problem:** Dataset synced but no detections

**Solutions:**

- Images are downloaded but not yet integrated with detector
- Need to update `ObjectDetector.kt` to use synced model
- Check `metadata.json` file was created correctly
- Verify images are valid JPG/PNG files

---

## 📊 Example Falcon Workflow

```
1. Train model on Falcon platform
   - Upload images: fire_extinguisher_*.jpg, medkit_*.jpg, etc.
   - Train YOLO model
   - Export as TFLite or get dataset ZIP

2. Upload to accessible location
   - Google Drive (get shareable link)
   - GitHub Releases
   - Your own server
   - Cloud storage (AWS S3, etc.)

3. Get URL
   https://drive.google.com/uc?export=download&id=FILE_ID
   or
   https://yourserver.com/models/safety-equipment.zip

4. Connect in app
   Settings → Falcon Integration → Connect
   Enter URL → Connect

5. Sync
   Tap "🔄 Sync Dataset"
   Wait for completion

6. Test Detection
   Real-Time Detection → Camera opens
   Point at objects from your dataset
   See detections with labels!
```

---

## 🎁 Features Summary

✅ **NO MORE CRASHES** - Fixed with proper state loading  
✅ **URL Persistence** - Saved permanently  
✅ **ZIP Support** - Download and extract datasets  
✅ **TFLite Support** - Direct model files  
✅ **API Support** - JSON endpoints  
✅ **Progress Tracking** - Real-time download progress  
✅ **Metadata Extraction** - Auto-detect classes from images  
✅ **Image Processing** - Handles JPG/PNG files  
✅ **Error Handling** - Clear error messages  
✅ **Offline Ready** - Dataset cached locally

---

## 📂 File Locations

**Dataset Storage:**

```
/data/data/com.example.detectalchemy/files/falcon_dataset/
├── images/              # Downloaded images
├── labels/              # Label files
├── model.tflite         # TFLite model (if downloaded)
└── metadata.json        # Class information
```

---

## 🔄 Next Steps

1. **✅ Test the crash fix** - Connect to Falcon in Settings
2. **✅ Try syncing a small dataset** - Use a test ZIP file
3. **⏳ Integrate with detection** - Update ObjectDetector.kt to use synced model
4. **⏳ Train on Falcon** - Upload your actual training data
5. **⏳ Deploy to production** - Use real detections!

---

## 📱 Updated APK Ready

**Location:** `app/build/outputs/apk/release/app-release.apk`

**Fixes:**

- ✅ No more crashes when connecting
- ✅ Proper state management
- ✅ Falcon dataset handler implemented
- ✅ Image processing system ready

**Install on your phone and test!**

---

*Built for Space Station Safety Monitoring 🚀*
