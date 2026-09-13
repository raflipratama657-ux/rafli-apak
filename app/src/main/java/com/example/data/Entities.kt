package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "murajaah_progress")
data class MurajaahProgressEntity(
    @PrimaryKey val surahNumber: Int,
    val surahName: String,
    val juzNumber: Int,
    val totalAyahs: Int,
    val memorizedAyahs: Int = 0,
    val status: String = "BELUM_HAFAL", // "MUTQIN", "LANCAR", "SEDANG_MENGHAFAL", "BELUM_HAFAL"
    val lastMurajaahDate: String = "",
    val qualityRating: Int = 0, // 1 to 5 stars
    val notes: String = ""
)

@Entity(tableName = "daily_murajaah_logs")
data class DailyMurajaahLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: yyyy-MM-dd
    val surahNumber: Int,
    val surahName: String,
    val startAyah: Int,
    val endAyah: Int,
    val juzNumber: Int,
    val pagesRead: Int = 1,
    val durationMinutes: Int = 15,
    val quality: String = "Lancar",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_recordings")
data class VoiceRecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val surahNumber: Int,
    val surahName: String,
    val ayahRange: String,
    val filePath: String,
    val durationSeconds: Int = 0,
    val recordedDate: String,
    val scoreTajwid: Int = 5,      // 1-5
    val scoreKelancaran: Int = 5,  // 1-5
    val scoreMakhraj: Int = 5,     // 1-5
    val selfNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorBadge: String, // e.g. "Hafidz 30 Juz", "Santri Tahfidz", "Pejuang Juz 30"
    val avatarInitial: String,
    val avatarBgColor: Long, // Color int
    val content: String,
    val tags: String = "#MurajaahHarian",
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val timeAgo: String = "Baru saja",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "murajaah_reminders")
data class MurajaahReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionName: String, // e.g. "Ba'da Subuh", "Ba'da Ashar", "Qobla Tidur"
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val note: String = "Waktunya muraja'ah hafalan Al-Qur'an harian!"
)

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
