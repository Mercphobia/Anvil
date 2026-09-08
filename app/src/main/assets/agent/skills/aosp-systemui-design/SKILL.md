---
name: aosp-systemui-design
description: Load when the MODE_B request changes SystemUI visuals (colors, dimens, layout structure)
applies_to: [MODE_B]
---

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
