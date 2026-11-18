# AndroLinux

Standalone Android ARM64 Linux runtime project, inspired by DroidDeck's no-Termux installation flow.

## Current implementation
Android application that downloads ARM64 Linux rootfs archives into app-private storage. No Termux dependency.

**Important:** Linux execution, PRoot native runtime, extraction, terminal, desktop and distribution boot are NOT implemented yet. This is a development milestone, not a functional Linux runner. The UI explicitly reports this.

## Planned runtime
- Build/package Android-compatible ARM64 PRoot binary with NDK
- Verify checksums, safely extract rootfs tarballs (reject path traversal)
- Launch rootfs using bundled PRoot without Termux
- Interactive PTY terminal and later a software-rendered desktop

DroidDeck reference: https://github.com/Droid-Deck/DroidDeck (GPL-3.0). This project does not copy DroidDeck's runtime binaries.

## APK
GitHub Actions > Build AndroLinux APK > artifact AndroLinux-APK.
