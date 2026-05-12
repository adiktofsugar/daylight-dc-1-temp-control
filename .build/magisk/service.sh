#!/system/bin/sh
# Re-apply perms once late in boot, in case a hardware init resets them.
sleep 20
for node in \
  /sys/class/leds/lcd-backlight/brightness \
  /sys/class/leds/lcd-backlight-amber/brightness
do
  [ -e "$node" ] && chmod 0666 "$node"
done
