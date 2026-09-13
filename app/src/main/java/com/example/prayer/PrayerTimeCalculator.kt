package com.example.prayer

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class PrayerSchedule(
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val timeRemainingFormatted: String
)

object PrayerTimeCalculator {

    fun calculateTimes(city: City, calendar: Calendar = Calendar.getInstance()): PrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date
        val julianDate = julianDate(year, month, day) - (city.longitude / (15.0 * 24.0))

        // Sun position
        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqOfTime = q / 15.0 - fixHour(ra)

        // Solar noon (Dhuhr)
        val noon = fixHour(12.0 + city.timezoneOffsetHours - (city.longitude / 15.0) - eqOfTime)

        // Sun angles for Indonesian standard (Kemenag RI)
        val fajrAngle = 20.0
        val ishaAngle = 18.0

        val fajrTime = noon - hourAngle(city.latitude, declination, -fajrAngle)
        val sunriseTime = noon - hourAngle(city.latitude, declination, -0.8333)
        val dhuhrTime = noon + (2.0 / 60.0) // 2 minutes ihtiyat (safety)
        val asrTime = noon + asrHourAngle(city.latitude, declination, 1.0)
        val sunsetTime = noon + hourAngle(city.latitude, declination, -0.8333)
        val maghribTime = sunsetTime + (2.0 / 60.0) // 2 minutes ihtiyat
        val ishaTime = noon + hourAngle(city.latitude, declination, -ishaAngle)

        val imsakTime = fajrTime - (10.0 / 60.0) // 10 minutes before Fajr
        val dhuhaTime = sunriseTime + (20.0 / 60.0) // 20 minutes after sunrise

        val imsakStr = formatTime(imsakTime)
        val subuhStr = formatTime(fajrTime)
        val terbitStr = formatTime(sunriseTime)
        val dhuhaStr = formatTime(dhuhaTime)
        val dzuhurStr = formatTime(dhuhrTime)
        val asharStr = formatTime(asrTime)
        val maghribStr = formatTime(maghribTime)
        val isyaStr = formatTime(ishaTime)

        // Calculate next prayer and countdown
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0 + calendar.get(Calendar.SECOND) / 3600.0

        val prayerList = listOf(
            Triple("Subuh", fajrTime, subuhStr),
            Triple("Dhuha", dhuhaTime, dhuhaStr),
            Triple("Dzuhur", dhuhrTime, dzuhurStr),
            Triple("Ashar", asrTime, asharStr),
            Triple("Maghrib", maghribTime, maghribStr),
            Triple("Isya", ishaTime, isyaStr)
        )

        var nextName = "Subuh"
        var nextTimeStr = subuhStr
        var diffHours = 0.0

        val upcoming = prayerList.firstOrNull { it.second > currentHour }
        if (upcoming != null) {
            nextName = upcoming.first
            nextTimeStr = upcoming.third
            diffHours = upcoming.second - currentHour
        } else {
            // Next is tomorrow's Fajr
            nextName = "Subuh"
            nextTimeStr = subuhStr
            diffHours = (24.0 - currentHour) + fajrTime
        }

        val totalSeconds = (diffHours * 3600).toInt().coerceAtLeast(0)
        val hoursRemaining = totalSeconds / 3600
        val minsRemaining = (totalSeconds % 3600) / 60
        val secsRemaining = totalSeconds % 60
        val remainingStr = String.format(Locale.getDefault(), "-%02d:%02d:%02d", hoursRemaining, minsRemaining, secsRemaining)

        return PrayerSchedule(
            imsak = imsakStr,
            subuh = subuhStr,
            terbit = terbitStr,
            dhuha = dhuhaStr,
            dzuhur = dzuhurStr,
            ashar = asharStr,
            maghrib = maghribStr,
            isya = isyaStr,
            nextPrayerName = nextName,
            nextPrayerTime = nextTimeStr,
            timeRemainingFormatted = remainingStr
        )
    }

    private fun hourAngle(latitude: Double, declination: Double, angle: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angRad = Math.toRadians(angle)
        val cosH = (sin(angRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    private fun asrHourAngle(latitude: Double, declination: Double, shadowFactor: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angle = -Math.toDegrees(atan(1.0 / (shadowFactor + tan(abs(latRad - decRad)))))
        return hourAngle(latitude, declination, angle)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var ang = a - 360.0 * floor(a / 360.0)
        if (ang < 0) ang += 360.0
        return ang
    }

    private fun fixHour(h: Double): Double {
        var hr = h - 24.0 * floor(h / 24.0)
        if (hr < 0) hr += 24.0
        return hr
    }

    private fun formatTime(timeInHours: Double): String {
        var fixedTime = fixHour(timeInHours)
        val totalMinutes = (fixedTime * 60.0 + 0.5).toInt()
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
