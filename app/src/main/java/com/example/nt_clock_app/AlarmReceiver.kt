package com.example.nt_clock_app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Log.d(
            "AlarmReceiver",
            "アラームが作動しました。"
        )

        val channelId = "alarm_channel"

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("アラーム")
            .setContentText("設定した時刻になりました。")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            1001,
            notification
        )

        val alarmId = intent.getIntExtra(
            "alarm_id",
            -1
        )

        if (alarmId == -1) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val alarms = loadAlarms1(context)

            val alarm = alarms.find {
                it.id == alarmId
            }

            if (alarm != null && alarm.enabled) {
                scheduleAlarm(
                    context = context,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    enabledDays = alarm.enabledDays,
                    requestCode = alarm.id
                )
            }
        }
    }
}
