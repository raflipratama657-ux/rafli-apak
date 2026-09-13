package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MurajaahProgressEntity::class,
        DailyMurajaahLogEntity::class,
        VoiceRecordingEntity::class,
        CommunityPostEntity::class,
        MurajaahReminderEntity::class,
        UserSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "murajaah_quran_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: AppDao) {
                // Pre-populate all 114 Surahs
                val initialSurahs = QuranConstants.SURAHS.map { surah ->
                    // Set Surah Al-Fatihah, Al-Ikhlas, Al-Falaq, An-Nas as Mutqin initially as common known surahs
                    val defaultStatus = when (surah.number) {
                        1, 112, 113, 114 -> "MUTQIN"
                        67, 108, 109, 110, 111 -> "LANCAR"
                        else -> "BELUM_HAFAL"
                    }
                    val memorized = when (defaultStatus) {
                        "MUTQIN", "LANCAR" -> surah.totalVerses
                        else -> 0
                    }
                    MurajaahProgressEntity(
                        surahNumber = surah.number,
                        surahName = surah.nameLatin,
                        juzNumber = surah.juz,
                        totalAyahs = surah.totalVerses,
                        memorizedAyahs = memorized,
                        status = defaultStatus,
                        qualityRating = if (defaultStatus == "MUTQIN") 5 else if (defaultStatus == "LANCAR") 4 else 0
                    )
                }
                dao.insertProgressList(initialSurahs)

                // Pre-populate inspiring community posts
                val defaultPosts = listOf(
                    CommunityPostEntity(
                        authorName = "Ahmad Zaki Al-Hafizh",
                        authorBadge = "Hafidz 30 Juz",
                        avatarInitial = "A",
                        avatarBgColor = 0xFF0F5B3E,
                        content = "Alhamdulillah, hari ini tuntas muraja'ah Juz 28 sampai 30 ba'da Subuh. Kunci mutqin adalah pengulangan minimal 3 kali sehari. Semangat semuanya para pejuang Al-Qur'an!",
                        tags = "#MurajaahSubuh #Pejuang30Juz #Mutqin",
                        likesCount = 28,
                        timeAgo = "10 menit lalu"
                    ),
                    CommunityPostEntity(
                        authorName = "Fatimah Azzahra",
                        authorBadge = "Santri Tahfidz 15 Juz",
                        avatarInitial = "F",
                        avatarBgColor = 0xFFB88E2D,
                        content = "Tips untuk yang sering ketukar ayat mutasyabihat di Surah Al-Baqarah & Ali Imran: tandai dengan stabilo dan rekam bacaan sendiri, lalu dengarkan saat senggang. Terbukti sangat membantu!",
                        tags = "#TipsTahfidz #EvaluasiSuara #AlBaqarah",
                        likesCount = 45,
                        timeAgo = "1 jam lalu"
                    ),
                    CommunityPostEntity(
                        authorName = "Ustadz Rizky Pratama",
                        authorBadge = "Pengajar Al-Qur'an",
                        avatarInitial = "R",
                        avatarBgColor = 0xFF2E6566,
                        content = "\"Siapa yang membaca satu huruf dari Kitabullah maka baginya satu kebaikan, dan satu kebaikan dilipatgandakan sepuluh kali.\" Jangan biarkan hari berlalu tanpa lantunan ayat suci.",
                        tags = "#MotivasiQurani #KeutamaanMurajaah",
                        likesCount = 62,
                        timeAgo = "3 jam lalu"
                    ),
                    CommunityPostEntity(
                        authorName = "Ibrahim Nur",
                        authorBadge = "Pejuang Juz 'Amma",
                        avatarInitial = "I",
                        avatarBgColor = 0xFF1B8A5A,
                        content = "Baru selesai setor hafalan Surah An-Naba dan An-Nazi'at mandiri pakai fitur rekaman. Terasa sekali bedanya saat dievaluasi tajwidnya. Bismillah target minggu ini selesai Juz 30!",
                        tags = "#JuzAmma #Mandiri #EvaluasiTajwid",
                        likesCount = 19,
                        timeAgo = "5 jam lalu"
                    )
                )
                dao.insertPostsList(defaultPosts)

                // Pre-populate default muraja'ah reminders
                val defaultReminders = listOf(
                    MurajaahReminderEntity(
                        sessionName = "Ba'da Subuh (Sesi Pagi Emas)",
                        hour = 5,
                        minute = 15,
                        isEnabled = true,
                        note = "Waktu terbaik mengulang hafalan baru dan melancarkan hafalan kemarin."
                    ),
                    MurajaahReminderEntity(
                        sessionName = "Ba'da Ashar (Sesi Sore)",
                        hour = 16,
                        minute = 0,
                        isEnabled = true,
                        note = "Muraja'ah 1/2 juz menjelang sore agar hafalan semakin menancap di dada."
                    ),
                    MurajaahReminderEntity(
                        sessionName = "Ba'da Maghrib / Isya (Sesi Malam)",
                        hour = 19,
                        minute = 30,
                        isEnabled = true,
                        note = "Muraja'ah santai bersama keluarga atau mandiri sebelum istirahat."
                    )
                )
                dao.insertRemindersList(defaultReminders)

                // Pre-populate default settings
                dao.setSetting(UserSettingEntity("daily_target_pages", "5"))
                dao.setSetting(UserSettingEntity("selected_city", "Jakarta"))
                dao.setSetting(UserSettingEntity("prayer_alarm_subuh", "true"))
                dao.setSetting(UserSettingEntity("prayer_alarm_dzuhur", "true"))
                dao.setSetting(UserSettingEntity("prayer_alarm_ashar", "true"))
                dao.setSetting(UserSettingEntity("prayer_alarm_maghrib", "true"))
                dao.setSetting(UserSettingEntity("prayer_alarm_isya", "true"))
            }
        }
    }
}
