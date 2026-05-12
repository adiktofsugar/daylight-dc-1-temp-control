# Daylight DC-1 Temp Control Quick Tile

When you install a GSI on the Daylight DC-1, there's no way to adjust the amber-ness of the backlight. This project adds a quick settings tile that pops up a slider that allows you to adjust it.

It's packaged as a magisk app because it's just a wrapper over writing directly to /sys/class/leds/lcd-backlight/brightness (and the amber version).

![screenshot](/screenshot.png)

## Installation

- clone the repo
- run `./build.sh`
- `adb push dist/backlight-control-magisk.zip /sdcard`
- install with magisk
- reboot
- drag the tile into your quick settings
- click
- slide

### Required software
- java 21+
- gradle
- bash

In case you're not super familiar with installing java build tools, you 
- [install sdkman](https://sdkman.io/install/) 
- `source "$HOME/.sdkman/bin/sdkman-init.sh"`
- `sdk install java`
- `sdk install gradle`
