package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("MED_ID", 0L)
        val medName = intent.getStringExtra("MED_NAME") ?: "دواؤك"
        val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""
        val medInstructions = intent.getStringExtra("MED_INSTRUCTIONS") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "medication_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات مواعيد الأدوية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إشعارات للتذكير بمواعيد أخذ الأدوية اليومية"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (medDosage.isNotBlank()) append("الجرعة: $medDosage")
            if (medInstructions.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append("التعليمات: $medInstructions")
            }
        }.ifEmpty { "حان الموعد المحدد لتناول الدواء، نتمنى لك دوام الصحة والعافية." }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("تذكير بموعد الدواء: $medName 💊")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(medId.toInt().coerceAtLeast(1001), builder.build())
    }
}
