#!/usr/bin/env bash
# Build the APK and package the Magisk module zip.
#
#   ./build.sh           # debug APK
#   ./build.sh release   # release-unsigned APK
#
# Output: dist/backlight-control-magisk.zip
#
# Install on device:
#   adb push dist/backlight-control-magisk.zip /sdcard/
#   then flash from the Magisk app, or:
#   adb shell su -c 'magisk --install-module /sdcard/backlight-control-magisk.zip'
#   adb reboot

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
ANDROID_DIR="$ROOT/android"
MODULE_SRC="$ROOT/magisk-module"
DIST="$ROOT/dist"
BUILD="$ROOT/.build/magisk"

VARIANT="${1:-debug}"
case "$VARIANT" in
    debug)   GRADLE_TASK="assembleDebug";   APK_REL="app/build/outputs/apk/debug/app-debug.apk" ;;
    release) GRADLE_TASK="assembleRelease"; APK_REL="app/build/outputs/apk/release/app-release-unsigned.apk" ;;
    *) echo "unknown variant: $VARIANT (use debug|release)"; exit 1 ;;
esac

echo "==> gradle $GRADLE_TASK"
( cd "$ANDROID_DIR" && gradle "$GRADLE_TASK" )

APK_SRC="$ANDROID_DIR/$APK_REL"
[ -f "$APK_SRC" ] || { echo "APK not found at $APK_SRC"; exit 1; }

echo "==> staging Magisk module at $BUILD"
rm -rf "$BUILD"
mkdir -p "$BUILD"
cp -r "$MODULE_SRC"/. "$BUILD"/
# Drop the .gitkeep placeholder, drop the APK in
rm -f "$BUILD/system/priv-app/BacklightControl/.gitkeep"
cp "$APK_SRC" "$BUILD/system/priv-app/BacklightControl/BacklightControl.apk"

mkdir -p "$DIST"
ZIP="$DIST/backlight-control-magisk.zip"
rm -f "$ZIP"
( cd "$BUILD" && zip -qr "$ZIP" . )

echo
echo "Built: $ZIP"
echo "Flash via Magisk app, or:"
echo "  adb push $ZIP /sdcard/"
echo "  adb shell su -c 'magisk --install-module /sdcard/$(basename "$ZIP")'"
echo "  adb reboot"
