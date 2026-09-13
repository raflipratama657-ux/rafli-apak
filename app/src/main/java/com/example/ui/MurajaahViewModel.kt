package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.data.*
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import com.example.prayer.City
import com.example.prayer.IndonesianCities
import com.example.prayer.PrayerSchedule
import com.example.prayer.PrayerTimeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MurajaahViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())
    val recorderManager = AudioRecorderManager(application)
    val playerManager = AudioPlayerManager()
    private val notificationHelper = NotificationHelper(application)
    private val alarmScheduler = AlarmScheduler(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    // --- Quran Progress ---
    val allProgress: StateFlow<List<MurajaahProgressEntity>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mutqinCount: StateFlow<Int> = repository.mutqinCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lancarCount: StateFlow<Int> = repository.lancarCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedFilterStatus = MutableStateFlow("Semua") // "Semua", "MUTQIN", "LANCAR", "SEDANG_MENGHAFAL", "BELUM_HAFAL"
    val selectedJuzFilter = MutableStateFlow(0) // 0 = Semua Juz, 1-30

    // --- Daily Logs & Streak ---
    val allLogs: StateFlow<List<DailyMurajaahLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<DailyMurajaahLogEntity>> = repository.getLogsForToday(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTargetPages = MutableStateFlow(5) // Default 5 pages per day

    val streakDays: StateFlow<Int> = allLogs.map { logs ->
        calculateStreak(logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val totalPagesReadThisWeek: StateFlow<Int> = allLogs.map { logs ->
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        logs.filter { it.timestamp >= oneWeekAgo }.sumOf { it.pagesRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Voice Recordings ---
    val allRecordings: StateFlow<List<VoiceRecordingEntity>> = repository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Community Posts ---
    val allPosts: StateFlow<List<CommunityPostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Murajaah Reminders ---
    val allReminders: StateFlow<List<MurajaahReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Prayer Times ---
    val selectedCity = MutableStateFlow(IndonesianCities.CITIES.first())
    val prayerAlarmSubuh = MutableStateFlow(true)
    val prayerAlarmDzuhur = MutableStateFlow(true)
    val prayerAlarmAshar = MutableStateFlow(true)
    val prayerAlarmMaghrib = MutableStateFlow(true)
    val prayerAlarmIsya = MutableStateFlow(true)

    private val _prayerSchedule = MutableStateFlow(PrayerTimeCalculator.calculateTimes(selectedCity.value))
    val prayerSchedule: StateFlow<PrayerSchedule> = _prayerSchedule.asStateFlow()

    // --- Leaderboard ---
    val weeklyLeaderboard: StateFlow<List<LeaderboardUser>> = totalPagesReadThisWeek.map { userPages ->
        val userMinutes = userPages * 4
        val userStreak = streakDays.value
        val userPoints = (userPages * 10) + (userStreak * 15)

        val currentUser = LeaderboardUser(
            rank = 0,
            name = "Anda (Penghafal)",
            title = "Pejuang 30 Juz",
            avatarInitial = "U",
            avatarBgColor = 0xFF0F5B3E,
            weeklyPages = userPages.coerceAtLeast(12),
            weeklyMinutes = userMinutes.coerceAtLeast(45),
            streakDays = userStreak,
            points = userPoints.coerceAtLeast(165),
            isCurrentUser = true
        )

        val list = (LeaderboardData.BASE_LEADERBOARD + currentUser).sortedByDescending { it.points }
        list.mapIndexed { index, user ->
            user.copy(rank = index + 1)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LeaderboardData.BASE_LEADERBOARD)

    init {
        // Start prayer timer ticker (updates countdown every second)
        viewModelScope.launch {
            while (true) {
                _prayerSchedule.value = PrayerTimeCalculator.calculateTimes(selectedCity.value)
                delay(1000)
            }
        }
    }

    fun setSelectedCity(city: City) {
        selectedCity.value = city
        _prayerSchedule.value = PrayerTimeCalculator.calculateTimes(city)
    }

    // --- Progress operations ---
    fun updateSurahProgress(
        surahNumber: Int,
        memorizedAyahs: Int,
        status: String,
        qualityRating: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val surah = QuranConstants.SURAHS.find { it.number == surahNumber } ?: return@launch
            val progress = MurajaahProgressEntity(
                surahNumber = surahNumber,
                surahName = surah.nameLatin,
                juzNumber = surah.juz,
                totalAyahs = surah.totalVerses,
                memorizedAyahs = memorizedAyahs.coerceIn(0, surah.totalVerses),
                status = status,
                lastMurajaahDate = todayDateString,
                qualityRating = qualityRating,
                notes = notes
            )
            repository.updateProgress(progress)
        }
    }

    // --- Daily Log operations ---
    fun addDailyLog(
        surahNumber: Int,
        startAyah: Int,
        endAyah: Int,
        pagesRead: Int,
        durationMinutes: Int,
        quality: String,
        notes: String
    ) {
        viewModelScope.launch {
            val surah = QuranConstants.SURAHS.find { it.number == surahNumber }
            val log = DailyMurajaahLogEntity(
                date = todayDateString,
                surahNumber = surahNumber,
                surahName = surah?.nameLatin ?: "Surah #$surahNumber",
                startAyah = startAyah,
                endAyah = endAyah,
                juzNumber = surah?.juz ?: 1,
                pagesRead = pagesRead.coerceAtLeast(1),
                durationMinutes = durationMinutes.coerceAtLeast(1),
                quality = quality,
                notes = notes
            )
            repository.insertLog(log)

            // Also update surah's last murajaah date
            if (surah != null) {
                val current = repository.getProgressForSurah(surahNumber)
                if (current != null) {
                    repository.updateProgress(current.copy(lastMurajaahDate = todayDateString))
                }
            }
        }
    }

    fun deleteDailyLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }

    // --- Voice Recording & Evaluation operations ---
    fun saveRecordingEvaluation(
        surahNumber: Int,
        ayahRange: String,
        recordedFile: File,
        durationSecs: Int,
        tajwid: Int,
        kelancaran: Int,
        makhraj: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val surah = QuranConstants.SURAHS.find { it.number == surahNumber }
            val surahName = surah?.nameLatin ?: "Surah #$surahNumber"
            val title = "Evaluasi $surahName ($ayahRange)"

            val entity = VoiceRecordingEntity(
                title = title,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahRange = ayahRange,
                filePath = recordedFile.absolutePath,
                durationSeconds = durationSecs,
                recordedDate = todayDateString,
                scoreTajwid = tajwid,
                scoreKelancaran = kelancaran,
                scoreMakhraj = makhraj,
                selfNotes = notes
            )
            repository.insertRecording(entity)
        }
    }

    fun deleteRecording(recording: VoiceRecordingEntity) {
        viewModelScope.launch {
            repository.deleteRecording(recording.id)
            val file = File(recording.filePath)
            if (file.exists()) file.delete()
        }
    }

    // --- Community operations ---
    fun togglePostLike(post: CommunityPostEntity) {
        viewModelScope.launch {
            val updated = post.copy(
                isLikedByMe = !post.isLikedByMe,
                likesCount = if (post.isLikedByMe) (post.likesCount - 1).coerceAtLeast(0) else post.likesCount + 1
            )
            repository.updatePost(updated)
        }
    }

    fun addNewPost(content: String, tags: String) {
        viewModelScope.launch {
            val post = CommunityPostEntity(
                authorName = "Saya (Penghafal)",
                authorBadge = "Pejuang 30 Juz",
                avatarInitial = "S",
                avatarBgColor = 0xFF0F5B3E,
                content = content,
                tags = if (tags.isNotBlank()) tags else "#SemangatMurajaah #PejuangTahfidz",
                likesCount = 1,
                isLikedByMe = true,
                timeAgo = "Baru saja"
            )
            repository.insertPost(post)
        }
    }

    // --- Reminders operations ---
    fun toggleReminder(reminder: MurajaahReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            repository.updateReminder(updated)
            if (updated.isEnabled) {
                alarmScheduler.scheduleMurajaahReminder(
                    updated.id,
                    updated.hour,
                    updated.minute,
                    updated.sessionName,
                    updated.note
                )
            } else {
                alarmScheduler.cancelReminder(updated.id)
            }
        }
    }

    fun addReminder(sessionName: String, hour: Int, minute: Int, note: String) {
        viewModelScope.launch {
            val reminder = MurajaahReminderEntity(
                sessionName = sessionName,
                hour = hour,
                minute = minute,
                isEnabled = true,
                note = note
            )
            repository.insertReminder(reminder)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
            alarmScheduler.cancelReminder(id)
        }
    }

    fun triggerTestMurajaahNotification(sessionName: String) {
        notificationHelper.showMurajaahNotification(sessionName, "Ini adalah contoh notifikasi pengingat muraja'ah Al-Qur'an harian Anda.")
    }

    fun triggerTestPrayerNotification(prayerName: String, time: String) {
        notificationHelper.showPrayerNotification(prayerName, time, selectedCity.value.name)
    }

    private fun calculateStreak(logs: List<DailyMurajaahLogEntity>): Int {
        if (logs.isEmpty()) return 1
        val distinctDates = logs.map { it.date }.distinct().sortedDescending()
        if (distinctDates.isEmpty()) return 1

        var streak = 0
        val cal = Calendar.getInstance()
        var checkDateStr = dateFormat.format(cal.time)

        // Check if today or yesterday was logged
        if (distinctDates.contains(checkDateStr)) {
            streak = 1
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = dateFormat.format(cal.time)
            while (distinctDates.contains(checkDateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDateStr = dateFormat.format(cal.time)
            }
        } else {
            // Check yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkDateStr = dateFormat.format(cal.time)
            if (distinctDates.contains(checkDateStr)) {
                streak = 1
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDateStr = dateFormat.format(cal.time)
                while (distinctDates.contains(checkDateStr)) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDateStr = dateFormat.format(cal.time)
                }
            } else {
                streak = 1
            }
        }
        return streak.coerceAtLeast(1)
    }

    override fun onCleared() {
        super.onCleared()
        recorderManager.release()
        playerManager.release()
    }
}
