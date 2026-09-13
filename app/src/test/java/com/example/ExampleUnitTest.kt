package com.example

import com.example.data.QuranConstants
import com.example.prayer.IndonesianCities
import com.example.prayer.PrayerTimeCalculator
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun quranConstants_contains114SurahsAnd30Juz() {
        assertEquals(114, QuranConstants.SURAHS.size)
        assertEquals(30, QuranConstants.JUZ_LIST.size)
        assertEquals("Al-Fatihah", QuranConstants.SURAHS.first().nameLatin)
        assertEquals("An-Nas", QuranConstants.SURAHS.last().nameLatin)
    }

    @Test
    fun prayerTimeCalculator_producesValidSchedule() {
        val jakarta = IndonesianCities.findCityByName("Jakarta")
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 13, 10, 0, 0)
        }
        val schedule = PrayerTimeCalculator.calculateTimes(jakarta, calendar)

        assertTrue(schedule.subuh.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(schedule.dzuhur.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(schedule.ashar.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(schedule.maghrib.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(schedule.isya.matches(Regex("\\d{2}:\\d{2}")))
        assertNotNull(schedule.nextPrayerName)
    }
}
