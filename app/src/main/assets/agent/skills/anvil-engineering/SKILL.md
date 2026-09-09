---
name: anvil-engineering
description: Anvil core engineering skill - always load. Covers on-device Android builds, AOSP SystemUI editing, git conventions, XML safety, universal language conventions, and frontier LLM provider patterns.
applies_to: [ANDROID, GIT_LINKED_SYSTEM, NODE_JS, PYTHON, RUST, GO, C_CPP, GENERIC, MODE_A, MODE_B]
---

# Anvil Engineering Skill

You are Anvil's engineering core. Sections below are domain playbooks - apply the ones matching the active project type and task.

## 1. On-Device Android Builds (ANDROID / MODE_A)

## Konteks
MODE_A compiles generated code on-device with ecj (Eclipse Compiler for Java).
ecj only understands Java - no Kotlin, no external dependencies, single-Activity scope.

## Aturan/Pola yang harus diikuti
- Generate Java ONLY. Kotlin syntax anywhere in output will fail the build.
- Single Activity extending android.app.Activity (NOT AppCompatActivity - it is an external dependency).
- No imports outside the Android framework and java.* standard library.
- No lambdas with method references that ecj versions mishandle - prefer anonymous inner classes when in doubt.
- Always provide a complete AndroidManifest.xml with package, application label, and MAIN/LAUNCHER intent filter.
- Layout may be XML in res/layout/ or built programmatically - both are fine for ecj.
- R class references must match resources actually generated in the project.

## Contoh
```java
package com.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("Hello");
        setContentView(view);
    }
}
```

## 2. Android App Visual Quality (ANDROID / MODE_A)

## Konteks
Even simple generated apps should feel intentional, not like debug screens.
MODE_A apps use plain framework widgets (no Material Components library).

## Aturan/Pola yang harus diikuti
- Use dp for all dimensions, sp for text sizes; never raw px.
- Comfortable touch targets: minimum 48dp height for interactive elements.
- Readable defaults: body text 14-16sp, titles 18-22sp.
- Give the root layout padding (16dp) so content does not touch screen edges.
- Prefer vertical LinearLayout stacks for simple apps; avoid deeply nested layouts.
- Use high-contrast text colors against the chosen background.
- Center primary content both visually and in code (gravity/layout_gravity).

## Contoh
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">
    <Button
        android:layout_width="wrap_content"
        android:layout_height="48dp"
        android:textSize="16sp"
        android:text="Calculate" />
</LinearLayout>
```

## 3. AOSP SystemUI Editing (GIT_LINKED_SYSTEM / MODE_B)

## Konteks
SystemUI sources mix Kotlin and Java with heavy framework conventions.
Tile classes, controllers, and panels follow predictable structures - respect them.

## Aturan/Pola yang harus diikuti
- NEVER edit from assumption: read the actual file first (repo may differ from AOSP upstream).
- New quick-settings tiles extend the same base class the surrounding tiles use (often QSTileImpl or a vendor BaseTileImpl) - check siblings, not docs.
- Keep constructor injection patterns identical to neighboring classes (Hilt/Dagger modules exist in SystemUI - register new classes the same way existing ones are registered).
- Resource references (R.string/R.drawable) must exist or be added to the matching res/ files in the same edit.
- Preserve logging tags and code style of the file you edit (indent, braces, trailing commas).
- Kotlin files in SystemUI commonly use coroutines/flows - do not introduce blocking calls on the main thread.

## Contoh
```kotlin
// Pattern for a new tile, matching QSTileImpl-based siblings:
class MyTile @Inject constructor(
    host: QSTileHost
) : QSTileImpl<MyState>(host) {
    override fun newState(): MyState = MyState()
    override fun handleClick() { /* ... */ }
}
```

## 4. AOSP SystemUI Design (GIT_LINKED_SYSTEM / MODE_B)

## Konteks
SystemUI visuals are constrained by system tokens, status bar height, panel
metrics, and dark/light mode switching. Hardcoding values breaks consistency.

## Aturan/Pola yang harus diikuti
- Use system resource tokens where they exist (@*android:color/, @dimen/ in SystemUI res) instead of raw hex/dp.
- Never hardcode status bar or panel heights - they vary by device, rotation, and cutout.
- Keep quick-settings tiles visually uniform: same icon size, label style, and background shape as sibling tiles.
- Respect dark/light theming: colors must come from theme attributes or have explicit night qualifiers.
- Panels (QS, notification shade) have limited vertical space - do not add tall elements without checking collapsed-state metrics.
- Preview-bound edits must survive XmlResourceInterceptor sanitization: private @*android: references get replaced by fallbacks.

## Contoh
```xml
<!-- Good: themed accent, shared tile background -->
<ImageView
    android:layout_width="@dimen/qs_tile_icon_size"
    android:layout_height="@dimen/qs_tile_icon_size"
    android:tint="?attr/colorAccent"
    android:background="@drawable/qs_tile_background" />
```

## 5. XML Resource Safety (all XML work)

## Konteks
AOSP layout XMLs reference private framework resources (@*android:...) that do
not exist in the app process. The mockup engine sanitizes these before
inflation, and unknown view classes become red labeled placeholders.

## Aturan/Pola yang harus diikuti
- Patterns replaced by the interceptor:
  - "@*android:color/<name>" -> "#808080"
  - "@*android:dimen/<name>" -> "24dp"
  - "@*android:drawable/<name>" -> "#404040"
  - "@*android:style/<name>" -> "@android:style/TextAppearance"
- Do not "fix" these fallbacks in generated XML - they exist so preview never crashes.
- Custom SystemUI view tags (e.g. com.android.systemui.statusbar.phone.PhoneStatusBarView) are EXPECTED to render as red labeled boxes - that is correct behavior, not an error.
- Keep XML well-formed: single root element, closed tags, quoted attributes, escaped entities (&amp; &lt; &gt;).
- android: prefixes are required for framework attributes; app-specific attrs need their own namespace declaration.

## Contoh
```xml
<!-- This preview-safe pattern keeps working after sanitization -->
<com.android.systemui.qs.QSPanel
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#B31A1A1A" />
```

## 6. Git Commit Convention (all Git projects)

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

## 7. Universal Language Conventions (NODE_JS / PYTHON / RUST / GO / C_CPP)

- Follow each language's idioms: PEP8 + type hints (Python), ESM + async/await (Node), ownership-aware patterns (Rust), error-as-value (Go), RAII (C++).
- Install dependencies before running: npm install / pip install -r requirements.txt / cargo build.
- Parse compiler errors by the universal `file:line:col: message` pattern; fix root cause, not symptoms.
- Never mix languages in one project unless the marker files demand it (monorepo).

## 8. Frontier Provider Patterns (Claude Mythos, GPT Astra, and beyond)

When the configured provider is a frontier reasoning model (Claude 4.5/Mythos-class, GPT-5/Astra-class):

- Use extended reasoning for architecture decisions; keep final answers concise and actionable.
- Prefer tool-use over guessing: read files before editing, run builds to verify, diff before commit.
- For long refactors, propose a step plan first, then execute stepwise with verification after each.
- Respect Anvil's safety gates: shell commands, memory writes, skill edits, and SOUL.md changes always require user approval.
- When self-correcting builds (up to 3 attempts), attach the structured error list and fix the root cause.
