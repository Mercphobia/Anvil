package dev.anvil.ade.workspace

import dev.anvil.ade.model.AgentStep
import dev.anvil.ade.model.ProjectFile
import dev.anvil.ade.model.StepKind
import java.util.UUID

/**
 * Demo workspace shown before any real project is opened - a small
 * calculator sample so first-launch screens are not empty. Replaced as soon
 * as the user opens/clones a real project (workspaceRoot switches).
 */
object DemoWorkspace {

    val initialTree: List<ProjectFile> =
listOf(
      ProjectFile(
        name = "app",
        path = "app",
        isDirectory = true,
        children = listOf(
          ProjectFile(
            name = "src/main/java",
            path = "app/src/main/java",
            isDirectory = true,
            children = listOf(
              ProjectFile(
                name = "MainActivity.java",
                path = "app/src/main/java/MainActivity.java",
                language = "java",
                content = """
                  package dev.anvil.ade.calculator;

                  import android.app.Activity;
                  import android.os.Bundle;
                  import android.widget.TextView;
                  import android.widget.Button;

                  public class MainActivity extends Activity {
                      private TextView display;
                      private double firstVal = 0;
                      private String op = "";

                      @Override
                      protected void onCreate(Bundle savedInstanceState) {
                          super.onCreate(savedInstanceState);
                          setContentView(R.layout.activity_main);
                          display = findViewById(R.id.txt_display);
                      }
                  }
                """.trimIndent()
              )
            )
          ),
          ProjectFile(
            name = "src/main/res/layout",
            path = "app/src/main/res/layout",
            isDirectory = true,
            children = listOf(
              ProjectFile(
                name = "activity_main.xml",
                path = "app/src/main/res/layout/activity_main.xml",
                language = "xml",
                content = """
                  <?xml version="1.0" encoding="utf-8"?>
                  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                      android:layout_width="match_parent"
                      android:layout_height="match_parent"
                      android:orientation="vertical"
                      android:padding="16dp"
                      android:background="?android:attr/colorBackground">
                      
                      <TextView
                          android:id="@+id/txt_display"
                          android:layout_width="match_parent"
                          android:layout_height="120dp"
                          android:gravity="bottom|end"
                          android:textSize="48sp"
                          android:text="0" />
                  </LinearLayout>
                """.trimIndent()
              )
            )
          ),
          ProjectFile(
            name = "AndroidManifest.xml",
            path = "app/src/main/AndroidManifest.xml",
            language = "xml",
            content = """
              <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                  package="dev.anvil.ade.calculator">
                  <application
                      android:label="Java Calc"
                      android:theme="@android:style/Theme.Material.Light.NoActionBar">
                      <activity android:name=".MainActivity" android:exported="true">
                          <intent-filter>
                              <action android:name="android.intent.action.MAIN" />
                              <category android:name="android.intent.category.LAUNCHER" />
                          </intent-filter>
                      </activity>
                  </application>
              </manifest>
            """.trimIndent()
          )
        )
      ),
      ProjectFile(
        name = ".anvil",
        path = ".anvil",
        isDirectory = true,
        children = listOf(
          ProjectFile(
            name = "memory.md",
            path = ".anvil/memory.md",
            language = "markdown",
            content = """
              # Anvil Project Memory
              - Architecture: Single Activity Java, zero external Gradle dependencies.
              - UI Scheme: Material You Monet Dynamic Color with high-contrast surfaces.
              - Git Working Branch: ai-mockup/qs-monet-expressive.
              - Target Device: On-device aapt2/ecj/d8 toolchain.
            """.trimIndent()
          )
        )
      )
    )

    val initialSteps: List<AgentStep> = listOf(      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.INFO,
        text = "Anvil IDE ready. Universal provider connected. Project type: ANDROID (App Builder).",
        timestamp = "09:40"
      ),
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.USER,
        text = "Buat aplikasi kalkulator Java sederhana dengan UI Material 3 Monet.",
        timestamp = "09:41"
      ),
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.TOOL_CALL,
        text = "write_file(path='app/src/main/res/layout/activity_main.xml')",
        toolName = "write_file",
        timestamp = "09:41",
        executionMs = 180
      ),
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.TOOL_RESULT,
        text = "File written: app/src/main/res/layout/activity_main.xml (312 bytes). XML syntax guarded OK.",
        timestamp = "09:41"
      ),
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.AGENT_TEXT,
        text = "Saya telah merancang layout kalkulator dan kode Activity Java bebas dependensi eksternal. Kode siap dikompilasi menggunakan toolchain on-device (aapt2 -> ecj -> d8). Tekan tab Build atau minta saya untuk menjalankan build!",
        timestamp = "09:42"
      )
    )


    val initialBuildLogs: String = ""
}
