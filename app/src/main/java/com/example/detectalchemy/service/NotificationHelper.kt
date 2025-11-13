package com.example.detectalchemy.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.detectalchemy.MainActivity
import com.example.detectalchemy.R
import com.example.detectalchemy.data.AlertSeverity

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_CRITICAL = "safety_critical"
        private const val CHANNEL_ID_HIGH = "safety_high"
        private const val CHANNEL_ID_MEDIUM = "safety_medium"
        private const val CHANNEL_ID_LOW = "safety_low"
        private const val CHANNEL_ID_INFO = "safety_info"

        private var notificationId = 1000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val criticalChannel = NotificationChannel(
                CHANNEL_ID_CRITICAL,
                "Critical Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical safety equipment alerts requiring immediate attention"
                enableVibration(true)
                enableLights(true)
            }

            val highChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                "High Priority Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority safety alerts"
                enableVibration(true)
            }

            val mediumChannel = NotificationChannel(
                CHANNEL_ID_MEDIUM,
                "Medium Priority Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Medium priority safety alerts"
            }

            val lowChannel = NotificationChannel(
                CHANNEL_ID_LOW,
                "Low Priority Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Low priority safety notifications"
            }

            val infoChannel = NotificationChannel(
                CHANNEL_ID_INFO,
                "Information Alerts",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Informational notifications about system status"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(criticalChannel)
            manager.createNotificationChannel(highChannel)
            manager.createNotificationChannel(mediumChannel)
            manager.createNotificationChannel(lowChannel)
            manager.createNotificationChannel(infoChannel)
        }
    }

    fun sendSafetyAlert(
        title: String,
        message: String,
        severity: AlertSeverity
    ) {
        val channelId = when (severity) {
            AlertSeverity.CRITICAL -> CHANNEL_ID_CRITICAL
            AlertSeverity.HIGH -> CHANNEL_ID_HIGH
            AlertSeverity.MEDIUM -> CHANNEL_ID_MEDIUM
            AlertSeverity.LOW -> CHANNEL_ID_LOW
            AlertSeverity.INFO -> CHANNEL_ID_INFO
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                when (severity) {
                    AlertSeverity.CRITICAL, AlertSeverity.HIGH -> NotificationCompat.PRIORITY_HIGH
                    AlertSeverity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
                    AlertSeverity.LOW -> NotificationCompat.PRIORITY_LOW
                    AlertSeverity.INFO -> NotificationCompat.PRIORITY_MIN
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (severity == AlertSeverity.CRITICAL) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
            e.printStackTrace()
        }
    }

    fun sendDetectionSummary(totalDetections: Int, criticalItems: Int) {
        val message = "Detected $totalDetections items. $criticalItems critical items found."

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_LOW)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Detection Complete")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
