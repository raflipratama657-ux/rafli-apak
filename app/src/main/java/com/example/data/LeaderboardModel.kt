package com.example.data

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val title: String, // e.g. "Hafidz 30 Juz", "Santriwati Tahfidz"
    val avatarInitial: String,
    val avatarBgColor: Long,
    val weeklyPages: Int,
    val weeklyMinutes: Int,
    val streakDays: Int,
    val points: Int,
    val isCurrentUser: Boolean = false
)

object LeaderboardData {
    val BASE_LEADERBOARD = listOf(
        LeaderboardUser(1, "Muhammad Al-Farisi", "Hafidz 30 Juz (Mutqin)", "M", 0xFF0F5B3E, 142, 420, 48, 1420),
        LeaderboardUser(2, "Aisyah Humaira", "Penghafal 25 Juz", "A", 0xFFB88E2D, 128, 380, 35, 1280),
        LeaderboardUser(3, "Farhan Nurhadi", "Santri Tahfidz 20 Juz", "F", 0xFF2E6566, 115, 340, 29, 1150),
        LeaderboardUser(4, "Zahra Salsabila", "Penghafal 15 Juz", "Z", 0xFF1B8A5A, 98, 290, 21, 980),
        LeaderboardUser(5, "Umar Abdullah", "Pejuang 10 Juz", "U", 0xFF073826, 85, 260, 18, 850),
        LeaderboardUser(6, "Nabila Khansa", "Pejuang 10 Juz", "N", 0xFF227855, 76, 230, 14, 760),
        LeaderboardUser(7, "Rizky Firmansyah", "Pejuang Juz 'Amma", "R", 0xFFB38E37, 65, 195, 12, 650),
        LeaderboardUser(8, "Salma Syahidah", "Pejuang Juz 'Amma", "S", 0xFF2A7065, 54, 160, 9, 540)
    )
}
