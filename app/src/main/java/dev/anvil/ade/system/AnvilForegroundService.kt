package dev.anvil.ade.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import dev.anvil.ade.MainActivity
import dev.anvil.ade.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android Foreground Service that keeps the Anvil agent alive in the
 * background with a persistent notification. Controlled via
 * [AnvilForegroundService.start] / [AnvilForegroundService.stop] or
 * bound through the ViewModel.
 *
 * The service holds a partial wake lock so the CPU stays awake while
 * long-running agent tasks (builds, model inference, git operations)
 * complete even when the screen is off.
 */
class AnvilForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "anvil_agent_running"
        private const val CHANNEL_NAME = "Anvil Agent"
        private const val NOTIFICATION_ID = 7741
        private const val ACTION_STOP = "dev.anvil.ade.action.STOP_SERVICE"

        /** Track whether the service is currently running. */
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        /**
         * Start the foreground service. Safe to call multiple times —
         * duplicate calls are no-ops.
         */
        fun start(context: Context) {
            if (_isRunning.value) return
            val intent = Intent(context, AnvilForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the foreground service and release the wake lock.
         */
        fun stop(context: Context) {
            val intent = Intent(context, AnvilForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        acquireWakeLock()
        _isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, AnvilForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Anvil Agent running")
            .setContentText("Agent is active — tap to open")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .build()

        // Guard: on some devices a service started without explicit
        // startForegroundService() (e.g. bound-only) will crash here.
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: IllegalStateException) {
            // Already in foreground — ignore
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _isRunning.value = false
        releaseWakeLock()
        super.onDestroy()
    }

    // ---- notification channel ----

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shown while the Anvil agent is active in the background"
            setShowBadge(false)
        }
        notificationManager?.createNotificationChannel(channel)
    }

    // ---- wake lock ----

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "anvil:agent-wakelock"
        ).apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // 10-minute timeout as safety net
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: RuntimeException) {
            // already released — safe
        }
        wakeLock = null
    }
}