# Vibe Forge

> AI agent Android IDE: on-device app builder (MODE_A) and AOSP SystemUI design assist (MODE_B).

## Modes

- **MODE_A - App Builder**: natural language -> real APK compiled and installed on-device. Java only, single-Activity, no external dependencies. Build pipeline: aapt2 -> ecj -> d8 -> sign -> install.
- **MODE_B - AOSP Design Assist**: edit AOSP SystemUI sources (Kotlin+Java+XML) with on-device mockup preview, then commit and push to a real Git working branch. Builds happen off-device (PC/CI) - never on-device.

## LLM Providers

Universal provider layer - bring your own key:
- Anthropic Claude
- OpenAI
- OpenRouter
- Google Gemini
- Custom OpenAI-compatible endpoint

Keys are stored in EncryptedSharedPreferences (Android Keystore) and never logged or sent anywhere except the chosen provider.

## UI

Adaptive Material 3 shell: NavigationRail on wide screens, bottom navigation on phones, modal working-tree drawer, setup wizard on first launch, welcome screen, and dedicated screens for Chat, Project, Mockup, Terminal, Git, and Build.

## Features

- **Agent loop with live reasoning**: tool calls and results stream into the chat as they execute.
- **Skill system**: 6 domain skills (app builder/design, SystemUI editing/design, XML resource safety, git conventions) dynamically loaded into the prompt based on mode and instruction. User-editable under `filesDir/agent/skills/`.
- **Per-project memory**: `.vibeforge/memory.md` auto-loaded into each session; new entries require one explicit user confirmation. `history.jsonl` audit log queryable via `search_history`.
- **Conversation persistence**: chat history survives app restarts per project, with automatic context trimming for long sessions.
- **Read-before-edit enforcement**: edit tools reject any file not read earlier in the session.
- **Build self-correction**: failed builds feed structured errors back to the agent for up to 3 automatic fix attempts.
- **AOSP mockup engine**: raw XML preview with custom LayoutInflater.Factory2; unknown system views render as red labeled placeholders; private `@*android:` resources sanitized automatically.
- **Syntax guards**: XML well-formedness and brace-balance checks before edits are accepted.
- **Git integration**: JGit clone/branch/diff/commit/push to working branches (`ai-mockup/<slug>`) - never main/master. Commit is gated by an explicit UI button.
- **On-device toolchain**: aapt2 (ARM64), ecj, d8 (r8), apksigner downloaded on demand into the private sandbox.
- **Sandboxed terminal**: shell execution inside the app workspace with sandboxed PATH.

## Demo scenarios

### MODE_A
1. Open Chat, select "App Builder" mode.
2. "Create a simple Java calculator app"
3. Agent writes sources -> run_build -> APK installs via Package Installer.

### MODE_B
1. Select "AOSP Assist" mode, configure Git remote/token in the Git tab.
2. "Make the quick settings panel dark blue"
3. Agent reads the real layout + logic files -> edits -> preview in Mockup tab -> diff in Git tab.
4. Press "Looks good, commit" -> pushed to `ai-mockup/<slug>`.

## Build

CI builds the debug APK on every push (artifact: `vibeforge-debug-apk`).

```bash
./gradlew :app:assembleDebug
```
