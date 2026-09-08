---
name: android-app-builder
description: Load when generating MODE_A app code that must compile with ecj on-device
applies_to: [MODE_A]
---

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
