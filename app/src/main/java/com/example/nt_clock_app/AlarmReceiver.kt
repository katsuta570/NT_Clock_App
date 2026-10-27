package com.example.nt_clock_app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()
        Log.d(
            "AlarmReceiver",
            "アラームが作動しました。"
        )

        val alarmId = intent.getIntExtra(
            "alarm_id",
            -1
        )

        if (alarmId == -1) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                showAlarmNotification(context, alarmId)

                val alarms = loadAlarms1(context)
                val alarm = alarms.find { it.id == alarmId }

                if (alarm != null && alarm.enabled) {
                    scheduleAlarm(context, alarm.hour, alarm.minute, alarm.enabledDays, alarm.id)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

private const val ALARM_CHANNEL_ID = "alarm_channel"

fun createAlarmNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "アラーム",
            NotificationManager.IMPORTANCE_HIGH
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}

private fun showAlarmNotification(context: Context, alarmId: Int) {
    createAlarmNotificationChannel(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("アラーム")
        .setContentText("設定した時刻になりました。")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .notify(alarmId, notification)
}
