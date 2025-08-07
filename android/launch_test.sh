#!/bin/bash

# Naviya Test Launcher - Emulator Launch Script
# This script builds and launches the test launcher on an Android emulator

echo "🚀 Naviya Test Launcher - Building and Launching..."

# Build the project
echo "📦 Building project..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Set Android SDK path - use environment variable if available
if [ -n "$ANDROID_HOME" ]; then
    ANDROID_SDK="$ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    ANDROID_SDK="$ANDROID_SDK_ROOT"
else
    ANDROID_SDK="$HOME/Library/Android/sdk"
    # Fallback paths
    if [ ! -d "$ANDROID_SDK" ]; then
        if [ -d "$HOME/Android/Sdk" ]; then
            ANDROID_SDK="$HOME/Android/Sdk"
        elif [ -d "/Users/brianwilliams/Library/Android/sdk" ]; then
            ANDROID_SDK="/Users/brianwilliams/Library/Android/sdk"
        fi
    fi
fi

ADB="$ANDROID_SDK/platform-tools/adb"

# Check if ADB exists and is executable
if [ ! -x "$ADB" ]; then
    echo "❌ Error: ADB not found at $ADB or not executable"
    echo "   Please ensure Android SDK is installed and ADB is available"
    exit 1
fi

# Check if emulator is running
if ! $ADB devices | grep -q "emulator"; then
    echo "❌ No emulator detected. Please start an Android emulator first."
    echo "   You can use Android Studio's AVD Manager or command line:"
    echo "   emulator -avd <your_avd_name>"
    exit 1
fi

# Wait for device to be fully booted
echo "⏳ Waiting for emulator to be fully booted..."
$ADB wait-for-device
$ADB shell 'while [[ "$(getprop sys.boot_completed)" != "1" ]]; do sleep 1; done'

# Find the APK file dynamically
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)
if [ -z "$APK_PATH" ]; then
    echo "❌ APK not found! Build may have failed."
    exit 1
fi

echo "📱 Installing APK on emulator..."
$ADB install -r "$APK_PATH"

if [ $? -ne 0 ]; then
    echo "❌ Installation failed!"
    exit 1
fi

# Extract package and activity name from APK (optional, using hardcoded for now)
PACKAGE_NAME="com.naviya.launcher.debug"
ACTIVITY_NAME="com.naviya.launcher.TestLauncherActivity"

echo "🎯 Launching Naviya Test Launcher..."
$ADB shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"

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
