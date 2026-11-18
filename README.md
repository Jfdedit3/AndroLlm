# AndroLlm

Android LLM chat client, inspired by Stable Diffusion WebUI. **Version 0.1.0 (MVP)**.

## Available
- Dark Android chat UI
- OpenAI-compatible chat completion API (llama.cpp server, LM Studio, compatible remote providers)
- Configurable URL, model, API key and system prompt
- Conversation context within the current chat session
- GitHub Actions build of a debug APK

## Not implemented yet
- **On-device GGUF inference**, model downloads, streaming tokens, persistent chat history and advanced sampling controls.
- This APK is a client, not a standalone offline inference engine.

## Build
GitHub Actions > Build Android APK > Run workflow, or push to main.
Download artifact `AndroLlm-debug-apk` from the successful workflow run.
Locally: JDK 17, Android SDK 35, Gradle 8.10.2; run `gradle :app:assembleDebug`.

## Connect to llama.cpp
Start an OpenAI-compatible llama.cpp server on your computer with network access, e.g. `llama-server -m model.gguf --host 0.0.0.0 --port 8080`.
On Android configure `http://YOUR_PC_LAN_IP:8080/v1/chat/completions`. Use only trusted LAN servers; HTTP is not encrypted. For remote providers use HTTPS. API keys are stored in app preferences, not encrypted.

Minimum Android 8.0. Debug APK is signed with Android's development key; not for Play Store distribution.
