#!/system/bin/sh
# Loosen perms on the backlight sysfs nodes so the priv-app can write them
# without shelling out to su. Magisk runs this after /data is mounted.

for node in \
  /sys/class/leds/lcd-backlight/brightness \
  /sys/class/leds/lcd-backlight-amber/brightness
do
  [ -e "$node" ] && chmod 0666 "$node"
done
