package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Murajaah Progress ---
    @Query("SELECT * FROM murajaah_progress ORDER BY surahNumber ASC")
    fun getAllProgress(): Flow<List<MurajaahProgressEntity>>

    @Query("SELECT * FROM murajaah_progress WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getProgressForSurah(surahNumber: Int): MurajaahProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(items: List<MurajaahProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: MurajaahProgressEntity)

    @Update
    suspend fun updateProgress(progress: MurajaahProgressEntity)

    @Query("SELECT COUNT(*) FROM murajaah_progress WHERE status = 'MUTQIN'")
    fun getMutqinCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM murajaah_progress WHERE status = 'LANCAR'")
    fun getLancarCount(): Flow<Int>

    // --- Daily Murajaah Logs ---
    @Query("SELECT * FROM daily_murajaah_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DailyMurajaahLogEntity>>

    @Query("SELECT * FROM daily_murajaah_logs WHERE date = :todayDate ORDER BY timestamp DESC")
    fun getLogsForToday(todayDate: String): Flow<List<DailyMurajaahLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyMurajaahLogEntity)

    @Query("DELETE FROM daily_murajaah_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)

    // --- Voice Recordings ---
    @Query("SELECT * FROM voice_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<VoiceRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: VoiceRecordingEntity)

    @Query("DELETE FROM voice_recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)

    // --- Community Posts ---
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostsList(posts: List<CommunityPostEntity>)

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Query("DELETE FROM community_posts WHERE id = :id")
    suspend fun deletePost(id: Long)

    // --- Murajaah Reminders ---
    @Query("SELECT * FROM murajaah_reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<MurajaahReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MurajaahReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemindersList(reminders: List<MurajaahReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: MurajaahReminderEntity)

    @Query("DELETE FROM murajaah_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    // --- User Settings ---
    @Query("SELECT value FROM user_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings")
    fun getAllSettings(): Flow<List<UserSettingEntity>>
}
