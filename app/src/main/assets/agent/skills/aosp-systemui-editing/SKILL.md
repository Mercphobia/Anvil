---
name: aosp-systemui-editing
description: Load when editing AOSP SystemUI Kotlin/Java logic in MODE_B
applies_to: [MODE_B]
---

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
