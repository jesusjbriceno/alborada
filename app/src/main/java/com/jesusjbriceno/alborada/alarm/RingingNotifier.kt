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
import com.jesusjbriceno.alborada.R
import com.jesusjbriceno.alborada.data.local.Alarm

/**
 * The ringing notification with a full-screen intent. This is the canonical
 * way for alarm apps to surface their activity on modern Android: exact
 * alarm receivers cannot start activities from the background, but a
 * full-screen notification is the exempt path that opens over the lock
 * screen (including in Doze).
 */
object RingingNotifier {
    const val CHANNEL_ID = "alarms"
    private const val FULL_SCREEN_REQUEST_CODE = 2000

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

    fun showRing(
        context: Context,
        alarm: Alarm,
        alarmStartEpoch: Long,
    ) {
        if (!hasNotificationPermission(context)) return

        ensureChannel(context)

        val fullScreenIntent = activityPendingIntent(context, alarm, alarmStartEpoch)

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_sun)
                .setContentTitle(context.getString(R.string.ringing_notification_title))
                .setContentText(alarm.label.ifBlank { context.ringingTimeText(alarm) })
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenIntent, true)
                .setContentIntent(fullScreenIntent)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(alarm.id.toInt(), notification)
    }

    fun canUseFullScreenIntent(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            context
                .getSystemService(NotificationManager::class.java)
                .canUseFullScreenIntent()

    fun dismiss(
        context: Context,
        alarmId: Long,
    ) {
        NotificationManagerCompat.from(context).cancel(alarmId.toInt())
    }

    private fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun activityPendingIntent(
        context: Context,
        alarm: Alarm,
        alarmStartEpoch: Long,
    ): PendingIntent {
        val intent =
            Intent(context, AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                putExtra(AlarmReceiver.EXTRA_ALARM_START_EPOCH, alarmStartEpoch)
                putExtra(AlarmReceiver.EXTRA_ANTICIPATION_MINUTES, alarm.anticipationMinutes)
                putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            }
        return PendingIntent.getActivity(
            context,
            FULL_SCREEN_REQUEST_CODE + alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun Context.ringingTimeText(alarm: Alarm): String = getString(R.string.ringing_notification_time, alarm.hour, alarm.minute)
}
