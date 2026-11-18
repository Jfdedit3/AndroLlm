# AndroLinux native ARM64

This repository now builds a **real standalone Linux distribution runner** using the open-source [oonid/pr](https://github.com/oonid/pr) Android runtime as its engine and UI. It does **not** require Termux. It supports Alpine, Debian, Ubuntu and other ARM64 OCI images, with interactive terminal, installation, launch and removal.

## Build
GitHub Actions → **Build AndroLinux Native Runtime APK** → Run workflow. Download the `AndroLinux-Native-ARM64-APK` artifact after the workflow succeeds.

## Important status and scope
The GitHub Actions build fetches and compiles the upstream Android app, including patched PRoot, Rust pr-cli, and terminal. It **does not yet rebrand the upstream application or add an XFCE desktop**. Its application ID and appearance are those of the upstream `pr` app. This is a functional upstream-based baseline, not a custom integrated AndroLinux UI. Android device runtime has not been tested in this repository.

This is Linux userspace via PRoot, not a separate kernel/VM. See upstream license and notices: https://github.com/oonid/pr .
