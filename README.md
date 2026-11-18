# AndroLinux

Android launcher for ARM64 Linux distributions via **Termux + proot-distro**. No root required.

## Features
- Select Debian, Ubuntu, Alpine Linux or Arch Linux (availability depends on your proot-distro version)
- Install, launch and remove distributions via Termux
- Open interactive terminal sessions
- GitHub Actions builds a debug APK artifact named `AndroLinux-APK`

## Requirements
1. Install a recent Termux from F-Droid or its official GitHub releases. Do not mix Termux sources.
2. In Termux: `pkg update && pkg install proot-distro`.
3. Run `mkdir -p ~/.termux && echo 'allow-external-apps=true' >> ~/.termux/termux.properties`, then restart Termux.
4. Grant AndroLinux the Termux RUN_COMMAND permission when prompted.
5. Select and install a distro in AndroLinux.

## Limitations
This is **not a standalone Linux VM**. It requires Termux and PRoot-Distro and shares the Android kernel. Linux desktop GUI and VNC setup are not yet implemented. The app sends commands to Termux; command output is shown in Termux, not embedded in the app. Distribution names depend on PRoot-Distro availability. Android may restrict background execution.

## Build
Push to main or run GitHub Actions > Build AndroLinux APK > Run workflow. Download the `AndroLinux-APK` artifact from a successful run.
