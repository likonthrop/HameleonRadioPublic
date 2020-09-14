package com.anisimov.radioonline

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.anisimov.requester.HttpResponseCallback
import com.anisimov.requester.getHttpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject

const val CHANNEL_ID = "com.anisimov.radioonline.channel.1"

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        registerNotifyChannels()
        getPushToken()
    }

    private fun getPushToken() {
        val sp = getSharedPreferences("firebase_token", Context.MODE_PRIVATE)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                var saveToken = false

                sp?.apply {
                    val spToken = getString("token", "")
                    val authorize = getLong("authorize", -1)
                    if (token != spToken || authorize < 0) {
                        saveToken = true
                    }
                }

                if (saveToken) {
                    getHttpResponse("/authorize?token=${token}", object : HttpResponseCallback {
                        override fun onResponse(response: String) {
                            sp?.edit()?.apply {
                                putString("token", token)
                                val js = JSONObject(response)
                                if (js.has("authorize")) {
                                    putLong("authorize", js.getLong("authorize"))
                                }
                                apply()
                            }
                        }
                    })
                }
            })
    }

    private fun registerNotifyChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Push Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "Push notify channel"
            }
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null)
                notificationManager.createNotificationChannel(channel)
        }
    }
}