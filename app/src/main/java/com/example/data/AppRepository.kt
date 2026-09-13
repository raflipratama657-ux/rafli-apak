package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {
    // Progress
    val allProgress: Flow<List<MurajaahProgressEntity>> = dao.getAllProgress()
    val mutqinCount: Flow<Int> = dao.getMutqinCount()
    val lancarCount: Flow<Int> = dao.getLancarCount()

    suspend fun getProgressForSurah(surahNumber: Int) = dao.getProgressForSurah(surahNumber)
    suspend fun updateProgress(progress: MurajaahProgressEntity) = dao.insertOrUpdateProgress(progress)

    // Logs
    val allLogs: Flow<List<DailyMurajaahLogEntity>> = dao.getAllLogs()
    fun getLogsForToday(todayDate: String): Flow<List<DailyMurajaahLogEntity>> = dao.getLogsForToday(todayDate)
    suspend fun insertLog(log: DailyMurajaahLogEntity) = dao.insertLog(log)
    suspend fun deleteLog(id: Long) = dao.deleteLog(id)

    // Recordings
    val allRecordings: Flow<List<VoiceRecordingEntity>> = dao.getAllRecordings()
    suspend fun insertRecording(recording: VoiceRecordingEntity) = dao.insertRecording(recording)
    suspend fun deleteRecording(id: Long) = dao.deleteRecording(id)

    // Community
    val allPosts: Flow<List<CommunityPostEntity>> = dao.getAllPosts()
    suspend fun insertPost(post: CommunityPostEntity) = dao.insertPost(post)
    suspend fun updatePost(post: CommunityPostEntity) = dao.updatePost(post)
    suspend fun deletePost(id: Long) = dao.deletePost(id)

    // Reminders
    val allReminders: Flow<List<MurajaahReminderEntity>> = dao.getAllReminders()
    suspend fun insertReminder(reminder: MurajaahReminderEntity) = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: MurajaahReminderEntity) = dao.updateReminder(reminder)
    suspend fun deleteReminder(id: Long) = dao.deleteReminder(id)

    // Settings
    val allSettings: Flow<List<UserSettingEntity>> = dao.getAllSettings()
    suspend fun getSetting(key: String): String? = dao.getSetting(key)
    suspend fun setSetting(key: String, value: String) = dao.setSetting(UserSettingEntity(key, value))
}
