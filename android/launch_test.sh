#!/bin/bash

# Naviya Test Launcher - Emulator Launch Script
# This script builds and launches the test launcher on an Android emulator

echo "🚀 Naviya Test Launcher - Building and Launching..."

# Build the project
echo "📦 Building project..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo " Build failed!"
    exit 1
fi

echo " Build successful!"

# Set Android SDK path
ANDROID_SDK="/Users/brianwilliams/Library/Android/sdk"
ADB="$ANDROID_SDK/platform-tools/adb"

# Check if emulator is running
if ! "$ADB" devices | grep -q "emulator"; then
    echo "  No emulator detected. Please start an Android emulator first."
    echo "   You can use Android Studio's AVD Manager or command line:"
    echo "   emulator -avd <your_avd_name>"
    exit 1
fi

echo "📱 Installing APK on emulator..."
"$ADB" install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -ne 0 ]; then
    echo "❌ Installation failed!"
    exit 1
fi

echo "🎯 Launching Naviya Test Launcher..."
"$ADB" shell am start -n com.naviya.launcher.debug/com.naviya.launcher.TestLauncherActivity

echo "✅ Test launcher started successfully!"
echo ""
echo "📋 Test Features Available:"
echo "   • 3-Mode System (Essential/Comfort/Connected)"
echo "   • Emergency SOS Button"
echo "   • Unread Communications Tile"
echo "   • Elderly-Friendly UI"
echo ""
echo "🔍 To view logs:"
echo "   adb logcat | grep Naviya"
