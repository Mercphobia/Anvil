<div align="center">

# ⚒️ Anvil

**The Agentic Development Environment that lives in your pocket.**

An on-device coding agent for Android that plans, writes, builds, and ships —
no cloud IDE, no companion server, no compromises.

`anvil.dev`

</div>

---

## What is Anvil?

Anvil is a fully self-contained agentic IDE that runs entirely on your phone:

- 🧠 **Real agent loop** — an LLM drives a tool-use loop (read/write files, run shell, build, git) with live reasoning streamed into the chat, build self-correction, and per-project memory.
- ⚡ **True on-device builds** — aapt2 → ecj → d8 → sign → install, all in-process. Natural language in, installable APK out, no PC required.
- 🐚 **Real Unix environment** — a genuine Termux-derived bootstrap gives the agent an actual shell with bash, coreutils, and the full Unix toolbox, sandboxed inside the app.
- 🎨 **AOSP/SystemUI design assist** — edit SystemUI sources with a live mockup canvas, resource-safety guards, and one-tap commit to a real Git working branch (builds stay off-device by design).
- 🔑 **Bring your own model** — 15+ LLM providers (Claude, OpenAI, Gemini, OpenRouter, Groq, Mistral, DeepSeek, Together, Fireworks, Qwen, Kimi, GLM, Ollama, Azure, custom endpoints). Keys live in Android Keystore-backed encrypted storage and never leave the device except to your chosen provider.

## How it works

```
you ──▶ agent loop ──▶ tools ──▶ real results
             │
             ├── list_files / read_file / write_file / search
             ├── run_terminal  (gated by your approval)
             ├── run_build     (aapt2 + ecj + d8 + apksig, in-process)
             ├── git: get_diff / commit & push (working branch only, never main)
             └── self-improvement: skills & SOUL.md (proposals need your sign-off)
```

Every dangerous action — shell commands, memory writes, skill edits, SOUL.md
changes — is **gated behind an explicit one-tap approval**. The agent proposes;
you decide.

## Feature highlights

| | |
|---|---|
| **Agent loop** | Tool-use LLM loop with retry, context trimming, and build self-correction (3 attempts with structured error feedback) |
| **Skill system** | Domain skills dynamically injected into the prompt; user-editable in-app (Agent Config → Skills), the agent can propose updates |
| **Per-project memory** | `.anvil/memory.md` + `history.jsonl` audit log, queryable via `search_history` |
| **SOUL.md** | Editable agent identity — the agent can even propose changes to its own soul (with your approval) |
| **Editor** | Sora-editor with TextMate grammars, Material You dynamic theming, per-language syntax highlighting |
| **Git** | Clone, diff, commit, push — protected: physically refuses to push to `main`/`master` |
| **Embedded env** | Termux bootstrap (aarch64), extracted into the app sandbox; symlink-aware, self-repairing |

## Building

Builds run exclusively on **GitHub Actions** (phone-first project — the repo is
built by CI, not by a local Android SDK):

```yaml
# .github/workflows/build.yml
# every push -> assembleDebug + artifact upload
```

Grab the APK from the latest green run's artifacts.

## Roadmap: Universal Agent

Anvil is generalizing from "Android/AOSP tool" to a **universal coding agent** —
Python, Node, Rust, Go, C/C++ — via project-type detection, a generic
shell-based build pipeline, per-language skills, and more editor grammars.
The agent loop, memory, skills, and environment are already language-agnostic;
the generalization connects them. See the design reference in the repo docs.

## License

MIT
