---
name: git-commit-convention
description: Load before any commit_and_push in MODE_B
applies_to: [MODE_B]
---

## Konteks
MODE_B pushes real commits to the user's AOSP/SystemUI repo. Commit quality
affects their build pipeline and code review.

## Aturan/Pola yang harus diikuti
- NEVER push to main/master directly. Default branch: ai-mockup/<slug-of-feature>.
- Commit message format: "<area>: <imperative summary>" (e.g. "systemui: darken quick settings background").
- One logical change per commit - split unrelated edits into separate commits.
- The diff must be shown to the user BEFORE commit; the commit button is the only confirmation needed after diff+preview are visible.
- Do not commit build outputs, .idea/, or local config files - sources only.
- If the push fails due to auth, tell the user to re-check their stored token; never print the token.

## Contoh
```
Branch: ai-mockup/dark-qs-panel
Message: systemui: apply dark blue token to quick settings panel
```
