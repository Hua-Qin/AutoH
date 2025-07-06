package com.stardust.autojs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.stardust.app.foreground.AbstractBroadcastService
import com.stardust.autojs.core.pref.Pref
import com.stardust.autojs.servicecomponents.ScriptBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class IndependentScriptService : AbstractBroadcastService() {
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        Log.i(TAG, "Pid: ${Process.myPid()}")
        if (Pref.isForegroundServiceEnabled) {
            startForeground(false)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    private fun startForeground(mediaProjection: Boolean) {
        ServiceCompat.startForeground(
            this, 25, buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                if (mediaProjection) {
                    serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                } else {
                    serviceType
                }
            } else {
                0
            },
        )
        isRunning = true
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name: CharSequence = "AutoJS Service"
        val description = "script foreground service"
        val channel = NotificationChannel(
            CHANEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = description
        channel.enableLights(false)
        manager.createNotificationChannel(channel)

        // 设置必要的 Flags
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        val contentIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle(getString(R.string.foreground_notification_title))
            .setContentText(getString(R.string.foreground_notification_text))
            .setSmallIcon(R.drawable.autojs_logo).setWhen(System.currentTimeMillis())
            .setContentIntent(contentIntent).setChannelId(CHANEL_ID).setVibrate(LongArray(0))
            .setOngoing(true).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_FOREGROUND -> {
                startForeground(intent.getBooleanExtra(PROJECTION_KEY, false))
            }

            ACTION_STOP_FOREGROUND -> {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                mediaProjection?.stop()
                isRunning = false
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        mediaProjection?.stop()
        isRunning = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onBind(intent: Intent?): IBinder {
        return ScriptBinder(this, scope)
    }


    companion object {
        var mediaProjection: MediaProjection? = null
        var isRunning: Boolean = false
        private const val TAG = "ScriptService"
        private const val PROJECTION_KEY = "mediaProjection"
        private val CHANEL_ID = IndependentScriptService::class.java.name + "_foreground"
        const val ACTION_START_FOREGROUND = "action_start_foreground"
        const val ACTION_STOP_FOREGROUND = "action_stop_foreground"

        fun startForeground(context: Context, mediaProjection: Boolean) {
            val intent = Intent(context, IndependentScriptService::class.java)
            intent.action = ACTION_START_FOREGROUND
            intent.putExtra(PROJECTION_KEY, mediaProjection)
            context.startForegroundService(intent)
        }

        fun startForeground(context: Context) {
            startForeground(context, false)
        }

        fun stopForeground(context: Context) {
            val intent = Intent(context, IndependentScriptService::class.java)
            intent.action = ACTION_STOP_FOREGROUND
            context.startService(intent)
        }
    }
}