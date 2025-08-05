#!/bin/bash

# =====================================================
# Unread Tile Test Script for Naviya Launcher
# =====================================================
# This script builds and installs the debug version of the app
# and launches the UnreadTileTestActivity to verify the
# unread communications tile functionality.
# 
# The unread tile shows total count of missed calls + unread SMS
# and works offline using local call log and SMS inbox access.
# =====================================================

# Set terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print header
echo -e "${BLUE}=======================================================${NC}"
echo -e "${BLUE}       NAVIYA LAUNCHER - UNREAD TILE TEST SCRIPT       ${NC}"
echo -e "${BLUE}=======================================================${NC}"
echo ""

# Check if device is connected
echo -e "${YELLOW}Checking for connected devices...${NC}"
DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo -e "${RED}ERROR: No devices connected. Please connect a device or start an emulator.${NC}"
    exit 1
else
    echo -e "${GREEN}Found $DEVICES connected device(s).${NC}"
fi

# Navigate to project directory
cd "$(dirname "$0")" || exit 1
echo -e "${YELLOW}Working directory: $(pwd)${NC}"

# Build debug APK
echo -e "${YELLOW}Building debug APK...${NC}"
./gradlew :app:assembleDebug

# Check if build was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Build failed. Please check the build output for errors.${NC}"
    exit 1
else
    echo -e "${GREEN}Build successful.${NC}"
fi

# Install APK
echo -e "${YELLOW}Installing debug APK...${NC}"
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Check if installation was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Installation failed. Please check the device connection.${NC}"
    exit 1
else
    echo -e "${GREEN}Installation successful.${NC}"
fi

# Grant necessary permissions
echo -e "${YELLOW}Granting required permissions...${NC}"
adb shell pm grant com.naviya.launcher android.permission.READ_CALL_LOG
adb shell pm grant com.naviya.launcher android.permission.READ_SMS

# Launch the UnreadTileTestActivity
echo -e "${YELLOW}Launching Unread Tile Test Activity...${NC}"
adb shell am start -n com.naviya.launcher/.debug.UnreadTileTestActivity

# Check if launch was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Failed to launch the test activity. Please check the activity name.${NC}"
    exit 1
else
    echo -e "${GREEN}Test activity launched successfully.${NC}"
fi

# Display instructions
echo ""
echo -e "${BLUE}=======================================================${NC}"
echo -e "${BLUE}                      INSTRUCTIONS                      ${NC}"
echo -e "${BLUE}=======================================================${NC}"
echo -e "1. The Unread Tile Test Activity should now be running on your device."
echo -e "2. If permissions were not automatically granted, tap 'Request Permissions'."
echo -e "3. Toggle the 'Caregiver Online' switch to see different tile states."
echo -e "4. Tap 'Refresh Tile' to update the unread counts."
echo -e "5. To view logs, run: ${YELLOW}adb logcat -s UnreadTileManager:* UnreadTile:*${NC}"
echo ""
echo -e "${BLUE}To create test data:${NC}"
echo -e "- For missed calls: ${YELLOW}adb shell am start -a android.intent.action.VIEW -d tel:1234567890${NC}"
echo -e "- For unread SMS: ${YELLOW}adb shell am start -a android.intent.action.SENDTO -d sms:1234567890${NC}"
echo ""
echo -e "${GREEN}Test script completed successfully.${NC}"
echo -e "${BLUE}=======================================================${NC}"

exit 0
