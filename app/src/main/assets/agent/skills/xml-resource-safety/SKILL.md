---
name: xml-resource-safety
description: Load whenever AOSP XML goes through mockup preview in MODE_B
applies_to: [MODE_B]
---

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
