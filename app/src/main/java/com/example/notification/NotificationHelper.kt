package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_MURAJAAH_ID = "murajaah_schedule_channel"
        const val CHANNEL_PRAYER_ID = "prayer_time_channel"
        const val NOTIF_ID_MURAJAAH = 1001
        const val NOTIF_ID_PRAYER = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Murajaah channel
            val murajaahChannel = NotificationChannel(
                CHANNEL_MURAJAAH_ID,
                "Jadwal & Pengingat Muraja'ah",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat jadwal muraja'ah hafalan Al-Qur'an harian"
                enableVibration(true)
            }

            // Prayer channel
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "Pengingat Waktu Sholat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat masuk waktu salat 5 waktu"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(murajaahChannel)
            notificationManager.createNotificationChannel(prayerChannel)
        }
    }

    fun showMurajaahNotification(sessionName: String, notes: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, CHANNEL_MURAJAAH_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Saatnya Muraja'ah: $sessionName")
            .setContentText(notes)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Waktunya mengulang hafalan Al-Qur'an ($sessionName). $notes Jaga hafalanmu setiap hari agar tetap mutqin."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_MURAJAAH + (System.currentTimeMillis() % 1000).toInt(), notification)
    }

    fun showPrayerNotification(prayerName: String, prayerTime: String, cityName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Waktu Sholat $prayerName Telah Tiba ($prayerTime)")
            .setContentText("Wilayah $cityName dan sekitarnya. Mari tunaikan sholat tepat waktu.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Allahu Akbar, Allahu Akbar. Waktu sholat $prayerName telah tiba untuk wilayah $cityName dan sekitarnya. Mari segera bersiap dan dirikan sholat berjamaah."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_PRAYER + (System.currentTimeMillis() % 1000).toInt(), notification)
    }
}
