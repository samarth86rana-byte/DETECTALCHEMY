# 🔗 Falcon Dataset Connection Guide

## 🎯 Overview

The DETECTALCHEMY app now supports connecting to your Falcon dataset using a **URL** instead of an
API key. This makes it easier to sync your trained YOLO model directly.

---

## 📱 How to Connect Your Falcon Dataset

### Step 1: Open Settings

1. Launch DETECTALCHEMY app
2. From the home screen, tap **"Settings"**
3. Scroll to the **"Integration"** section

### Step 2: Connect to Falcon

1. Find the **"Connect to Falcon"** card
2. Tap the **"Connect"** button (orange)
3. A dialog will appear: **"Connect to Falcon Dataset"**

### Step 3: Enter Your URL

In the dialog, enter your Falcon dataset or model URL:

**Supported URL formats:**

- `https://falcon.com/dataset/your-dataset-id`
- `https://your-falcon-server.com/models/model.tflite`
- `http://localhost:8000/model/latest.tflite` (for local testing)
- Any valid HTTP/HTTPS URL pointing to your model or dataset

**Examples:**

```
https://falcon-api.example.com/datasets/space-safety
https://storage.googleapis.com/your-bucket/model.tflite
https://github.com/your-repo/releases/download/v1.0/model.tflite
```

### Step 4: Validation

The app will automatically validate your URL:

- ✅ **Valid**: URL starts with `http://` or `https://`
- ⚠️ **Invalid**: URL doesn't start with `http://` or `https://`

The **"Connect"** button will only be enabled when the URL is valid.

### Step 5: Connected!

Once connected:

- You'll see **"✅ Connected"** badge
- The connected URL will be displayed
- Model updates will sync automatically (when implemented)

---

## 🔄 Managing Your Connection

### View Connected URL

1. Go to **Settings**
2. Look at the **"Connect to Falcon"** section
3. If connected, you'll see:
    - Green checkmark
    - "Dataset URL: [your-url]"
    - "Model updates will sync automatically"

### Disconnect

1. Tap the **"Connected"** button (green)
2. The connection will be removed
3. You can connect to a different URL anytime

---

## 🛠️ Integration with Your Workflow

### For Development:

```kotlin
// Example: How the app will use the URL in the future
val falconUrl = "https://your-falcon-server.com/model"
val modelFile = downloadModel(falconUrl)
loadYoloModel(modelFile)
```

### For Production:

1. **Train your model** on Falcon
2. **Host the model** (Falcon API, Google Cloud, AWS S3, GitHub Releases, etc.)
3. **Get the direct URL** to the model file
4. **Enter URL** in the app
5. **App downloads and syncs** the model automatically

---

## 📋 URL Requirements

### ✅ Valid URLs:

- Must start with `http://` or `https://`
- Can point to:
    - `.tflite` model files
    - API endpoints that return model data
    - Dataset management systems
    - Cloud storage (S3, GCS, etc.)

### ❌ Invalid URLs:

- File paths: `C:\models\model.tflite`
- Relative paths: `../models/model.tflite`
- Missing protocol: `falcon.com/dataset`

---

## 🎨 What You'll See

### Before Connection:

```
Connect to Falcon
Sync with Falcon dataset URL
[Connect Button]
```

### After Connection:

```
Connect to Falcon
✅ Connected to dataset

✓ Dataset URL: https://your-url.com/model
  Model updates will sync automatically from this dataset.

[Connected Button (Green)]
```

---

## 🚀 Future Implementation

When you integrate a real YOLO model, the URL will be used to:

1. **Download Model**: Fetch the `.tflite` file from the URL
2. **Verify Model**: Check model format and compatibility
3. **Update Model**: Replace the current model with the new one
4. **Sync Periodically**: Check for updates at the URL
5. **Cache Locally**: Store model for offline use

### Example Implementation:

```kotlin
// In DetectionViewModel or ModelManager
suspend fun syncModelFromUrl(url: String) {
    try {
        // Download model from URL
        val modelData = downloadFile(url)
        
        // Save to app storage
        val modelFile = File(context.filesDir, "yolo_model.tflite")
        modelFile.writeBytes(modelData)
        
        // Reload detector with new model
        objectDetector.loadModel(modelFile)
        
        // Show success notification
        notificationHelper.sendNotification("Model updated successfully!")
    } catch (e: Exception) {
        // Handle error
        notificationHelper.sendNotification("Model sync failed: ${e.message}")
    }
}
```

---

## 🔐 Security Considerations

- **HTTPS Recommended**: Use HTTPS URLs for secure connections
- **Validate Content**: Verify downloaded files are valid models
- **Error Handling**: Handle network errors gracefully
- **User Consent**: Only sync when user explicitly connects

---

## 💡 Tips

1. **Test URLs First**: Make sure your URL is accessible before entering it
2. **Use Direct Links**: Point directly to model files when possible
3. **Keep URLs Updated**: If your model URL changes, reconnect with the new URL
4. **Monitor Sync**: Check app logs to ensure models sync correctly

---

## 📞 Troubleshooting

### "URL validation failed"

- Make sure URL starts with `http://` or `https://`
- Check for typos in the URL

### "Cannot connect to dataset"

- Verify the URL is accessible from your phone
- Check your internet connection
- Ensure the server is online

### "Model sync failed"

- Verify the URL points to a valid model file
- Check file format (.tflite expected)
- Ensure you have storage space available

---

## ✅ Summary

**Old Way (API Key):**

```
Enter API Key: falcon_abc123xyz
```

**New Way (URL):**

```
Enter Dataset URL: https://your-falcon.com/model.tflite
✅ More flexible
✅ Direct model access
✅ Works with any hosting
✅ Easy to test and debug
```

---

**Your Falcon integration is now ready with URL-based connections!** 🎉
