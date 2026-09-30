package com.jesusjbriceno.alborada.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jesusjbriceno.alborada.MainActivity
import com.jesusjbriceno.alborada.R
import com.jesusjbriceno.alborada.data.local.Alarm

/** Posts the wake-up notification for a fired alarm. */
object AlarmNotifier {
    const val CHANNEL_ID = "alarms"
    private const val REQUEST_CODE = 1

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_alarms),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_alarms_description)
            }
        manager.createNotificationChannel(channel)
    }

    fun show(
        context: Context,
        alarm: Alarm,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted yet; the notification would be invisible anyway.
            return
        }

        ensureChannel(context)

        val contentIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_sun)
                .setContentTitle(context.getString(R.string.notification_alarm_title))
                .setContentText(alarm.label.ifBlank { alarm.formattedTime(context) })
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(alarm.id.toInt(), notification)
    }
}

private fun Alarm.formattedTime(context: Context): String =
    context.getString(R.string.notification_alarm_time, hour, minute)
