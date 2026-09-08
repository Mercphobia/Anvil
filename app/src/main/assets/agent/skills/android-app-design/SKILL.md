---
name: android-app-design
description: Load when the MODE_A request involves visual quality or layout decisions
applies_to: [MODE_A]
---

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
