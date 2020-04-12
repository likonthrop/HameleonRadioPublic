package com.anisimov.radioonline.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.anisimov.radioonline.MainActivity
import com.anisimov.radioonline.R

private const val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
private const val PRIMARY_CHANNEL_NAME = "Плеер контроллер"
private const val NOTIFICATION_ID = 555

class MediaNotificationManager(private val service: RadioService) {

    private var strAppName: String = service.resources.getString(R.string.live_broadcast)
    private var strLiveBroadcast: String = ""
    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(service)

    fun startNotify(playbackStatus: String) {
        val largeIcon = BitmapFactory.decodeResource(service.resources, R.mipmap.ic_launcher_round)
        service.station?.let { strAppName = it.name }
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
        val builder = Builder(service, PRIMARY_CHANNEL)
            .setAutoCancel(false)
            .setContentTitle(strAppName)
            .setContentText(strLiveBroadcast)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(android.R.drawable.stat_sys_headset)
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
        service.startForeground(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotify() {
        service.stopForeground(true)
    }

}