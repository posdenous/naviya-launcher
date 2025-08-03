# Build and Run Guide - Get Naviya Running on Your Pixel 8

**Current Status:** Core components are disabled, but test activities are available  
**Goal:** Get the app running on your Pixel 8 for validation testing

## 🔍 **What I Found:**

### **✅ Active Components:**
- `SimpleTestActivity.kt` - Basic test activity
- `TestLauncherActivity.kt` - Test launcher
- `TestNaviyaApplication.kt` - Test application
- Core data models and database
- Emergency system components
- UI themes and layouts

### **❌ Disabled Components:**
- `MainActivity.kt.disabled` - Main app entry point
- `NaviyaApplication.kt.disabled` - Main application class
- All onboarding UI components (`.disabled`)
- Main launcher view model

## 🚀 **Step 1: Enable Core Components**

### **Enable Main Application:**
```bash
cd /Users/brianwilliams/Documents/GitHub/naviya/naviya-launcher/android/app/src/main/java/com/naviya/launcher

# Enable main application
mv NaviyaApplication.kt.disabled NaviyaApplication.kt

# Enable main activity (choose one)
mv MainActivity.kt.disabled MainActivity.kt
# OR use minimal version
# mv MinimalMainActivity.kt.disabled MinimalMainActivity.kt
```

### **Enable Onboarding Components:**
```bash
cd onboarding/ui
mv FamilyOnboardingScreen.kt.disabled FamilyOnboardingScreen.kt
mv FamilyOnboardingViewModel.kt.disabled FamilyOnboardingViewModel.kt
mv OnboardingSteps.kt.disabled OnboardingSteps.kt

cd ../data
mv OnboardingDao.kt.disabled OnboardingDao.kt
mv OnboardingModels.kt.disabled OnboardingModels.kt
```

## 🔧 **Step 2: Android Studio Setup**

### **Open Project in Android Studio:**
1. **Open Android Studio**
2. **File → Open** 
3. **Navigate to:** `/Users/brianwilliams/Documents/GitHub/naviya/naviya-launcher/android`
4. **Click "Open"**

### **Wait for Gradle Sync:**
- Android Studio will automatically sync Gradle
- This may take 5-10 minutes first time
- Watch the bottom status bar for "Gradle sync finished"

### **Check Build Configuration:**
1. **Go to:** Build → Select Build Variant
2. **Ensure:** "debug" is selected (not release)
3. **Target:** Should show your module name

## 📱 **Step 3: Setup Your Pixel 8**

### **Enable Developer Options:**
1. **Settings → About phone**
2. **Tap "Build number" 7 times** (you'll see "You are now a developer!")
3. **Go back to Settings → System → Developer options**

### **Enable USB Debugging:**
1. **In Developer options:**
2. **Turn on "USB debugging"**
3. **Turn on "Install via USB"** (if available)

### **Connect to Computer:**
1. **Connect Pixel 8 via USB cable**
2. **On phone:** Allow USB debugging when prompted
3. **Check "Always allow from this computer"**

## 🏃 **Step 4: Build and Run**

### **In Android Studio:**
1. **Check device is detected:**
   - Top toolbar should show your Pixel 8
   - If not, click dropdown and select your device

2. **Build the project:**
   - **Build → Make Project** (Ctrl+F9 / Cmd+F9)
   - Wait for build to complete (watch bottom status)

3. **Run the app:**
   - **Click green "Run" button** (or Shift+F10)
   - **OR Run → Run 'app'**

### **Expected Result:**
- App installs on your Pixel 8
- App launches (may show test activity initially)
- You can interact with the interface

## 🐛 **Step 5: Troubleshooting Common Issues**

### **If Gradle Sync Fails:**
```bash
# In terminal, navigate to android directory
cd /Users/brianwilliams/Documents/GitHub/naviya/naviya-launcher/android

# Clean and rebuild
./gradlew clean
./gradlew build
```

### **If Build Fails with Import Errors:**
- **Check:** All `.disabled` files are renamed correctly
- **Look for:** Red underlined imports in Android Studio
- **Fix:** Update import statements if needed

### **If Device Not Detected:**
1. **Unplug and replug** USB cable
2. **Try different USB port**
3. **In Android Studio:** Tools → SDK Manager → SDK Tools → Google USB Driver (install if missing)

### **If App Crashes on Launch:**
1. **Check Logcat** in Android Studio (bottom panel)
2. **Look for red error messages**
3. **Common issue:** Missing dependencies or import errors

## 📋 **Step 6: Verify Core Functionality**

### **Once App Runs, Test:**
- [ ] **App launches** without immediate crash
- [ ] **Basic UI appears** (even if minimal)
- [ ] **Can navigate** through available screens
- [ ] **Emergency button** is visible (if implemented)
- [ ] **Mode switching** works (Essential/Comfort/Connected)

### **Check Android Studio Logcat:**
- **Bottom panel → Logcat**
- **Filter by your app** (com.naviya.launcher)
- **Look for errors** (red lines) or warnings (yellow)

## 🎯 **Step 7: Create Demo Version**

### **Once Basic App Works:**
1. **Identify working features** vs. broken ones
2. **Hide/disable broken features** temporarily
3. **Focus on core functionality:**
   - Launcher interface
   - Emergency SOS button
   - Mode switching
   - Basic navigation

### **Prepare for User Testing:**
- **Screen recording capability** (Android built-in or ADB)
- **Screenshot functionality** for documentation
- **Stable demo flow** (5-10 minutes of core features)

## 💡 **Alternative: Start with Test Activities**

### **If Main App Won't Run:**
**Use existing test activities first:**
1. **Modify `TestLauncherActivity.kt`** to be your demo
2. **Update AndroidManifest.xml** to launch test activity
3. **Build basic functionality** in test environment
4. **Move to main app** once stable

### **Benefits of Test Activity Approach:**
- **Simpler setup** - fewer dependencies
- **Faster iteration** - less complex build
- **Easier debugging** - isolated functionality
- **Good for validation** - core features only

## 📞 **Next Steps After App Runs:**

### **Immediate:**
1. **Document what works** vs. what's broken
2. **Create list of features** available for testing
3. **Identify critical bugs** that need fixing
4. **Plan demo flow** for elderly user testing

### **For Validation:**
1. **Record demo video** of working features
2. **Take screenshots** of key screens
3. **Prepare user testing scenarios**
4. **Start recruiting elderly volunteers**

## 🎯 **Success Criteria:**

### **Minimum for Validation:**
- [ ] **App installs and launches** on Pixel 8
- [ ] **Core launcher interface** visible and functional
- [ ] **Emergency SOS button** present (even if not fully functional)
- [ ] **Can demonstrate** 3-mode concept
- [ ] **Stable enough** for 5-minute demo

### **Ready for User Testing:**
- [ ] **No immediate crashes** during basic usage
- [ ] **Key features work** as intended
- [ ] **Demo flow** is smooth and presentable
- [ ] **Can screen record** for remote user testing

**Let me know when you've tried these steps and what happens! I'll help troubleshoot any issues that come up.**
