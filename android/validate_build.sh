#!/bin/bash
# Naviya Launcher Build Validation Script
# Created for elderly-friendly launcher with unread tile functionality

echo "=== Naviya Launcher Build Validation ==="
echo "Validating build for elderly-friendly launcher with unread tile"

# Set Android SDK environment variables if not already set
if [ -z "$ANDROID_HOME" ]; then
  if [ -d "$HOME/Library/Android/sdk" ]; then
    export ANDROID_HOME="$HOME/Library/Android/sdk"
    echo "Set ANDROID_HOME to $ANDROID_HOME"
  else
    echo "ERROR: Android SDK not found. Please set ANDROID_HOME manually."
    exit 1
  fi
fi

# Add Android tools to PATH
if [ -n "$ANDROID_HOME" ]; then
  export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools"
fi

echo ""
echo "=== Step 1: Cleaning Project ==="
./gradlew clean

echo ""
echo "=== Step 2: Running Lint Checks ==="
./gradlew lint

echo ""
echo "=== Step 3: Compiling Debug Variant ==="
./gradlew assembleDebug

echo ""
echo "=== Step 4: Running Unit Tests ==="
./gradlew testDebugUnitTest

echo ""
echo "=== Step 5: Validating Unread Tile Functionality ==="
echo "Checking for UnreadTileServiceStub.kt..."
if [ -f "app/src/main/java/com/naviya/launcher/unread/UnreadTileServiceStub.kt" ]; then
  echo "✅ UnreadTileServiceStub.kt found"
else
  echo "❌ UnreadTileServiceStub.kt not found"
fi

echo ""
echo "=== Build Validation Complete ==="
echo "If all steps passed, the build is ready for testing on an emulator."
echo "Run ./launch_test.sh to deploy to emulator and test the unread tile functionality."
