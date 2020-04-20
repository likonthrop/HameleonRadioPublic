package com.anisimov.radioonline.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R
import kotlinx.coroutines.*


private const val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
private const val PRIMARY_CHANNEL_NAME = "Плеер контроллер"
private const val NOTIFICATION_ID = 555

class MediaNotificationManager(private val service: RadioService) {

    private var stationName: String = service.resources.getString(R.string.live_broadcast)
    private var trackName: String = ""
    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(service)
    private var trackUpdater = makeTrackUpdater()
    private var destroy = false
    private var lastState: String? = null

    private fun makeTrackUpdater(): Job {
        return GlobalScope.launch(Dispatchers.Main) {
            while (!destroy) {
                service.station?.song?.let {
                    if (it.getTrackString() != trackName) {
                        trackName = it.getTrackString()
                        if (lastState != null) startNotify(lastState!!)
                    }
                }
                delay(5000)
            }
        }
    }

    fun startNotify(playbackStatus: String) {
        lastState = playbackStatus
        service.station?.let {
            stationName = it.name; trackName = it.song?.getTrackString() ?: ""
        }
        var icon = R.drawable.ic_pause_white
        val playbackAction = Intent(service, RadioService::class.java)
        playbackAction.action = ACTION_PAUSE
        var action = PendingIntent.getService(service, 1, playbackAction, 0)
        if (playbackStatus == PlaybackStatus.PAUSED) {
            icon = R.drawable.ic_play_white
            playbackAction.action = ACTION_PLAY
            action = PendingIntent.getService(service, 2, playbackAction, 0)
        }
        val stopIntent = Intent(service, RadioService::class.java)
        stopIntent.action = ACTION_STOP
        val stopAction = PendingIntent.getService(service, 3, stopIntent, 0)
        val intent = Intent(service, MainActivity::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent = PendingIntent.getActivity(service, 0, intent, 0)
        notificationManager.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                PRIMARY_CHANNEL,
                PRIMARY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }

        val isLollipopHuawei = (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) && Build.MANUFACTURER.equals(
                "HUAWEI", ignoreCase = true)


        val builder = if (isLollipopHuawei) {
            Builder(service, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setContentTitle(stationName)
                .setContentText(trackName)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop_white, "stop", stopAction)
                .setSmallIcon(R.drawable.ic_baseline_headset_white)

        } else {
            Builder(service, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setContentTitle(stationName)
                .setContentText(trackName)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_baseline_headset_white)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop_white, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setStyle(
                    MediaStyle()
                        .setMediaSession(service.mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopAction)
                )
        }

        service.startForeground(NOTIFICATION_ID, builder.build())

    }

    fun cancelNotify() {
        destroy = true
        trackUpdater.cancel()
        service.stopForeground(true)
    }
}