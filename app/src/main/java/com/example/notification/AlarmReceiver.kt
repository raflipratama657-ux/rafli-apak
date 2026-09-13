package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)
        val type = intent.getStringExtra("REMINDER_TYPE") ?: "MURAJAAH"

        when (type) {
            "MURAJAAH" -> {
                val session = intent.getStringExtra("SESSION_NAME") ?: "Muraja'ah Harian"
                val note = intent.getStringExtra("NOTE") ?: "Waktunya muraja'ah hafalan Al-Qur'an!"
                helper.showMurajaahNotification(session, note)
            }
            "PRAYER" -> {
                val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Sholat"
                val time = intent.getStringExtra("PRAYER_TIME") ?: ""
                val city = intent.getStringExtra("CITY_NAME") ?: "Indonesia"
                helper.showPrayerNotification(prayerName, time, city)
            }
        }
    }
}
