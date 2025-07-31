#!/bin/bash

# Naviya Launcher - Complete Setup and Test Script
# This script helps you set up an emulator and test the Naviya launcher

echo "🚀 Naviya Launcher - Setup and Test"
echo "=================================="

# Set Android SDK paths
export ANDROID_HOME="/Users/brianwilliams/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Check if Android SDK is available
if [ ! -d "$ANDROID_HOME" ]; then
    echo "❌ Android SDK not found at $ANDROID_HOME"
    echo "   Please install Android Studio and SDK first"
    exit 1
fi

echo "✅ Android SDK found at $ANDROID_HOME"

# Check for available AVDs
echo ""
echo "📱 Checking for available Android Virtual Devices..."
avd_list=$(emulator -list-avds 2>/dev/null)

if [ -z "$avd_list" ]; then
    echo "⚠️  No Android Virtual Devices (AVDs) found."
    echo ""
    echo "To create an AVD:"
    echo "1. Open Android Studio"
    echo "2. Go to Tools > AVD Manager"
    echo "3. Create a new virtual device (recommended: Pixel 4, API 30+)"
    echo "4. Run this script again"
    echo ""
    echo "Or create one via command line:"
    echo "avdmanager create avd -n NaviyaTest -k \"system-images;android-30;google_apis;x86_64\""
    exit 1
fi

echo "Available AVDs:"
echo "$avd_list"
echo ""

# Get the first AVD name
first_avd=$(echo "$avd_list" | head -n1)
echo "🎯 Using AVD: $first_avd"

# Check if emulator is already running
running_devices=$(adb devices | grep -v "List of devices" | grep -v "^$")
if [ -n "$running_devices" ]; then
    echo "✅ Emulator already running:"
    echo "$running_devices"
else
    echo "🔄 Starting emulator: $first_avd"
    echo "   (This may take a few minutes...)"
    emulator -avd "$first_avd" -no-snapshot-save &
    
    # Wait for emulator to boot
    echo "⏳ Waiting for emulator to boot..."
    adb wait-for-device
    
    # Wait a bit more for full boot
    sleep 10
    
    # Check if device is ready
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null)" != "1" ]; do
        echo "   Still booting..."
        sleep 5
    done
fi

echo "✅ Emulator is ready!"

# Build the project
echo ""
echo "🔨 Building Naviya Launcher..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Install the APK
echo ""
echo "📦 Installing Naviya Launcher APK..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -ne 0 ]; then
    echo "❌ Installation failed!"
    exit 1
fi

echo "✅ APK installed successfully!"

# Launch the app
echo ""
echo "🚀 Launching Naviya Test Launcher..."
adb shell am start -n com.naviya.launcher/.TestLauncherActivity

if [ $? -eq 0 ]; then
    echo "✅ Naviya Launcher started successfully!"
    echo ""
    echo "🎉 TEST READY!"
    echo "=============="
    echo "The Naviya elderly launcher is now running on your emulator."
    echo ""
    echo "📋 Test Features:"
    echo "   • Switch between 3 modes (Essential/Comfort/Connected)"
    echo "   • Test Emergency SOS button"
    echo "   • Check Unread communications tile"
    echo "   • Verify elderly-friendly UI (large buttons, high contrast)"
    echo ""
    echo "🔍 To view logs:"
    echo "   adb logcat | grep Naviya"
    echo ""
    echo "🛑 To stop the emulator:"
    echo "   adb emu kill"
else
    echo "❌ Failed to launch app"
    exit 1
fi
