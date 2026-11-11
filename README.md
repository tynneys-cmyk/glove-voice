# Glove Voice (Android source project)

This app connects to a paired ESP32 over Bluetooth Classic SPP (the protocol used by Arduino `BluetoothSerial.h`). It reads newline-terminated text messages and speaks each message using Android Text-to-Speech.

## ESP32 compatibility
Use `BluetoothSerial.h` and send one complete command per line, for example:

```cpp
SerialBT.println("I NEED HELP");
```

`println()` is important because the app reads one message per line. Avoid sending only `SerialBT.print()` without a newline.

## Build and install
1. On a computer, install Android Studio.
2. Open this `GloveVoice` folder in Android Studio.
3. Allow Gradle to sync, then choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Install the generated APK on your Android phone.
5. On the phone, open Bluetooth settings and pair with the ESP32 (common pairing PINs are `1234` or `0000`, depending on the ESP32 setup).
6. Open **Glove Voice**, grant Bluetooth permission, select the paired ESP32, and tap **Connect to ESP32**.
7. Make sure media volume is up. The app speaks each non-empty line received.

## Notes
- This project uses Bluetooth Classic, not BLE. It matches ESP32 sketches using `BluetoothSerial.h` on a classic-Bluetooth-capable ESP32 board.
- The phone's built-in Android Text-to-Speech engine provides the voice. Install/update speech data in Android Text-to-Speech settings if no voice is heard.
- This is source code, not a precompiled APK.
