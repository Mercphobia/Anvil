package dev.anvil.ade.workspace

import dev.anvil.ade.model.ProjectFile
import dev.anvil.ade.model.ProjectType

/**
 * Project template scaffolding - pure functions returning the starter file
 * tree for a template id. The ViewModel owns state application.
 */
object ProjectTemplates {

    data class TemplateResult(
        val projectName: String,
        val projectType: ProjectType,
        val files: List<ProjectFile>,
        val notice: String? = null,
        /** Template that opens the GitHub-clone dialog instead of scaffolding. */
        val wantsCloneDialog: Boolean = false
    )

    fun scaffold(templateId: String): TemplateResult? = when (templateId) {
        "github_clone" -> TemplateResult(
            projectName = "", projectType = ProjectType.GENERIC, files = emptyList(),
            notice = "Silakan masukkan URL GitHub untuk dikloning.",
            wantsCloneDialog = true
        )
        "empty_activity" -> emptyActivity()
        "no_activity" -> noActivity()
        "basic_views" -> basicViews()
        "aosp_overlay" -> aospOverlay()
        else -> null
    }


    private fun empty_activity(): TemplateResult {
        val mainJava = ProjectFile(
          name = "MainActivity.java",
          path = "app/src/main/java/MainActivity.java",
          language = "java",
          content = """
            package dev.anvil.ade.emptyapp;

            import android.app.Activity;
            import android.os.Bundle;

            public class MainActivity extends Activity {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.activity_main);
                }
            }
          """.trimIndent()
        )
        val activityXml = ProjectFile(
          name = "activity_main.xml",
          path = "app/src/main/res/layout/activity_main.xml",
          language = "xml",
          content = """
            <?xml version="1.0" encoding="utf-8"?>
            <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="?android:attr/colorSurface">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="Empty Activity • Anvil"
                    android:textSize="18sp"
                    android:textColor="?android:attr/textColorPrimary" />
            </FrameLayout>
          """.trimIndent()
        )
        val manifest = ProjectFile(
          name = "AndroidManifest.xml",
          path = "app/src/main/AndroidManifest.xml",
          language = "xml",
          content = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="dev.anvil.ade.emptyapp">
                <application 
                    android:label="Empty App" 
                    android:theme="@android:style/Theme.Material.Light.NoActionBar">
                    <activity 
                        android:name=".MainActivity" 
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
          """.trimIndent()
        )
        return TemplateResult(
            projectName = "Empty Activity App",
            projectType = ProjectType.ANDROID,
            files = listOf(mainJava, activityXml, manifest),
            notice = "Template Empty Activity berhasil dimuat!"
        )
    }


    private fun no_activity(): TemplateResult {
        val serviceJava = ProjectFile(
          name = "AppService.java",
          path = "app/src/main/java/AppService.java",
          language = "java",
          content = """
            package dev.anvil.ade.service;

            import android.app.Service;
            import android.content.Intent;
            import android.os.IBinder;
            import android.util.Log;

            public class AppService extends Service {
                private static final String TAG = "AppService";

                @Override
                public int onStartCommand(Intent intent, int flags, int startId) {
                    Log.d(TAG, "Anvil background service started");
                    return START_STICKY;
                }

                @Override
                public IBinder onBind(Intent intent) {
                    return null;
                }
            }
          """.trimIndent()
        )
        val manifest = ProjectFile(
          name = "AndroidManifest.xml",
          path = "app/src/main/AndroidManifest.xml",
          language = "xml",
          content = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="dev.anvil.ade.service">
                <application android:label="Background Service Module">
                    <service 
                        android:name=".AppService" 
                        android:exported="false" />
                </application>
            </manifest>
          """.trimIndent()
        )
        return TemplateResult(
            projectName = "No Activity Service",
            projectType = ProjectType.ANDROID,
            files = listOf(serviceJava, manifest),
            notice = "Template No Activity (Background Service) berhasil dimuat!"
        )
    }


    private fun basic_views(): TemplateResult {

        return TemplateResult(
            projectName = "Calculator & Counter",
            projectType = ProjectType.ANDROID,
            files = listOf(),
            notice = "Template Basic Views Activity berhasil dimuat!"
        )
    }


    private fun aosp_overlay(): TemplateResult {
        val overlayXml = ProjectFile(
          name = "qs_panel.xml",
          path = "packages/SystemUI/res/layout/qs_panel.xml",
          language = "xml",
          content = INITIAL_MOCKUP_XML
        )
        val overlayManifest = ProjectFile(
          name = "AndroidManifest.xml",
          path = "packages/SystemUI/AndroidManifest.xml",
          language = "xml",
          content = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="dev.anvil.ade.systemui.overlay">
                <overlay 
                    android:targetPackage="com.android.systemui" 
                    android:priority="1000"
                    android:isStatic="true" />
            </manifest>
          """.trimIndent()
        )
        return TemplateResult(
            projectName = "AOSP SystemUI Overlay",
            projectType = ProjectType.GIT_LINKED_SYSTEM,
            files = listOf(overlayXml, overlayManifest),
            notice = "Template AOSP SystemUI Overlay berhasil dimuat!"
        )
    }

}