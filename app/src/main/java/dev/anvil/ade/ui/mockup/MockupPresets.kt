package dev.anvil.ade.ui.mockup

/**
 * Bundled SystemUI mockup XML presets (Quick Settings, Status Bar, Volume
 * Dialog) - extracted from AnvilViewModel so the ViewModel carries state and
 * behavior only.
 */
object MockupPresets {
    val INITIAL_MOCKUP_XML = """
      <com.android.systemui.qs.QSContainerImpl
          xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:background="?android:attr/colorSurfaceContainerHigh"
          android:padding="16dp"
          android:elevation="8dp">

          <TextView
              android:id="@+id/qs_clock"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="09:41"
              android:textSize="22sp"
              android:textColor="?android:attr/textColorPrimary" />

          <!-- Expressive Monet Quick Settings Grid (6 Tiles) -->
          <GridLayout
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:columnCount="2"
              android:rowCount="3"
              android:alignmentMode="alignMargins"
              android:useDefaultMargins="true">
              <!-- Rendered with live Monet pill shapes -->
          </GridLayout>
      </com.android.systemui.qs.QSContainerImpl>
    """.trimIndent()

    val STATUS_BAR_XML = """
      <com.android.systemui.statusbar.phone.PhoneStatusBarView
          xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="40dp"
          android:background="?android:attr/colorSurfaceContainer"
          android:paddingHorizontal="12dp">
          
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="09:41"
              android:textStyle="bold" />
      </com.android.systemui.statusbar.phone.PhoneStatusBarView>
    """.trimIndent()

    val VOLUME_DIALOG_XML = """
      <com.android.systemui.volume.VolumeDialogImpl
          xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:background="?android:attr/colorSurfaceContainerHighest"
          android:elevation="12dp"
          android:padding="8dp">
      </com.android.systemui.volume.VolumeDialogImpl>
    """.trimIndent()

}
