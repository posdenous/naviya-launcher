#!/bin/bash

# Emergency Data Retention Test Launcher
# This script helps run the EmergencyDataRetentionIntegrationTest on a connected device/emulator

# Colours for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Colour

echo -e "${BLUE}======================================================${NC}"
echo -e "${BLUE}   NAVIYA EMERGENCY DATA RETENTION TEST LAUNCHER      ${NC}"
echo -e "${BLUE}======================================================${NC}"
echo ""

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo -e "${RED}Error: adb command not found.${NC}"
    echo "Please ensure Android SDK platform tools are installed and in your PATH."
    exit 1
fi

# Check for connected devices
DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo -e "${RED}Error: No Android devices/emulators connected.${NC}"
    echo "Please connect a device or start an emulator first."
    exit 1
fi

# Show connected devices
echo -e "${YELLOW}Connected devices:${NC}"
adb devices | grep -v "List"
echo ""

# Build the test APK
echo -e "${YELLOW}Building test APK...${NC}"
./gradlew assembleDebugAndroidTest

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please fix any compilation errors and try again.${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful!${NC}"
echo ""

# Install the app and test APK
echo -e "${YELLOW}Installing app and test APK...${NC}"
./gradlew installDebug installDebugAndroidTest

if [ $? -ne 0 ]; then
    echo -e "${RED}Installation failed.${NC}"
    exit 1
fi

echo -e "${GREEN}Installation successful!${NC}"
echo ""

# Create a debug activity launcher
echo -e "${YELLOW}Launching test via debug activity...${NC}"
adb shell am start -n "com.naviya.launcher/.debug.EmergencyTestActivity" --ez "run_retention_test" true

echo ""
echo -e "${YELLOW}Retrieving test logs...${NC}"
echo -e "${BLUE}======================================================${NC}"
adb logcat -d | grep "EmergencyDataRetentionTest"
echo -e "${BLUE}======================================================${NC}"

echo ""
echo -e "${GREEN}Test execution complete!${NC}"
echo "Please check the logs above for test results."
echo "Look for '✅ TEST COMPLETED SUCCESSFULLY' or '❌ TEST FAILED' messages."
echo ""
echo -e "${YELLOW}Note: You'll need to implement the EmergencyTestActivity to run the test.${NC}"
echo "See README_EMERGENCY_DATA_RETENTION_TEST.md for implementation details."
