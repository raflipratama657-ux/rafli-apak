package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LeaderboardUser
import com.example.data.MurajaahReminderEntity
import com.example.prayer.PrayerSchedule
import com.example.ui.MurajaahViewModel
import com.example.ui.dialogs.GDriveInstallGuideDialog
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldSecondary
import com.example.ui.theme.StatusMutqin
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardAndSettingsScreen(
    viewModel: MurajaahViewModel
) {
    val context = LocalContext.current
    val leaderboard by viewModel.weeklyLeaderboard.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()
    val prayerSchedule by viewModel.prayerSchedule.collectAsState()

    val prayerAlarmSubuh by viewModel.prayerAlarmSubuh.collectAsState()
    val prayerAlarmDzuhur by viewModel.prayerAlarmDzuhur.collectAsState()
    val prayerAlarmAshar by viewModel.prayerAlarmAshar.collectAsState()
    val prayerAlarmMaghrib by viewModel.prayerAlarmMaghrib.collectAsState()
    val prayerAlarmIsya by viewModel.prayerAlarmIsya.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Peringkat, 1 = Pengingat & Sholat, 2 = Pasang GDrive
    var showGDriveGuideDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_settings_screen")
    ) {
        // Top sub-tabs
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Peringkat, Jadwal & Pengingat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                PrimaryTabRow(selectedTabIndex = selectedSubTab, containerColor = Color.Transparent) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = { Text("Peringkat 🏆", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = { Text("Jadwal & Sholat ⏰", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 },
                        text = { Text("Pasang GDrive 📲", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> WeeklyLeaderboardView(leaderboard = leaderboard)
            1 -> RemindersAndPrayerSettingsView(
                reminders = reminders,
                prayerSchedule = prayerSchedule,
                prayerAlarmSubuh = prayerAlarmSubuh,
                prayerAlarmDzuhur = prayerAlarmDzuhur,
                prayerAlarmAshar = prayerAlarmAshar,
                prayerAlarmMaghrib = prayerAlarmMaghrib,
                prayerAlarmIsya = prayerAlarmIsya,
                onToggleReminder = { viewModel.toggleReminder(it) },
                onAddReminder = { showAddReminderDialog = true },
                onTestMurajaahNotification = { viewModel.triggerTestMurajaahNotification("Sesi Emas Subuh") },
                onTestPrayerNotification = { viewModel.triggerTestPrayerNotification(prayerSchedule.nextPrayerName, prayerSchedule.nextPrayerTime) },
                onTogglePrayerSubuh = { viewModel.prayerAlarmSubuh.value = !prayerAlarmSubuh },
                onTogglePrayerDzuhur = { viewModel.prayerAlarmDzuhur.value = !prayerAlarmDzuhur },
                onTogglePrayerAshar = { viewModel.prayerAlarmAshar.value = !prayerAlarmAshar },
                onTogglePrayerMaghrib = { viewModel.prayerAlarmMaghrib.value = !prayerAlarmMaghrib },
                onTogglePrayerIsya = { viewModel.prayerAlarmIsya.value = !prayerAlarmIsya }
            )
            2 -> GDriveInstallGuideView(onOpenFullGuide = { showGDriveGuideDialog = true })
        }
    }

    if (showGDriveGuideDialog) {
        GDriveInstallGuideDialog(onDismiss = { showGDriveGuideDialog = false })
    }

    if (showAddReminderDialog) {
        AddCustomReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { name, hour, min, note ->
                viewModel.addReminder(name, hour, min, note)
                showAddReminderDialog = false
            }
        )
    }
}

@Composable
private fun WeeklyLeaderboardView(leaderboard: List<LeaderboardUser>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Weekly Reset & Info Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = IslamicGoldSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Papan Peringkat Mingguan Penghafal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Dihitung dari jumlah halaman muraja'ah, durasi waktu mengulang, dan istiqomah streak harian.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Top 3 Podium
        item {
            val top3 = leaderboard.take(3)
            if (top3.size >= 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    PodiumUser(user = top3[1], medal = "🥈", rank = 2, height = 110.dp)
                    PodiumUser(user = top3[0], medal = "🥇", rank = 1, height = 135.dp)
                    PodiumUser(user = top3[2], medal = "🥉", rank = 3, height = 95.dp)
                }
            }
        }

        // Complete List
        item {
            Text(
                text = "Peringkat Keseluruhan Minggu Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(leaderboard) { user ->
            LeaderboardItemRow(user = user)
        }
    }
}

@Composable
private fun PodiumUser(user: LeaderboardUser, medal: String, rank: Int, height: androidx.compose.ui.unit.Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(medal, style = MaterialTheme.typography.titleLarge)
        Surface(
            shape = CircleShape,
            color = Color(user.avatarBgColor),
            modifier = Modifier.size(if (rank == 1) 50.dp else 42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user.avatarInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = if (rank == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
                )
            }
        }
        Text(
            text = user.name.split(" ").first(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = "${user.points} Poin",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = if (rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .width(90.dp)
                .height(height)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rank == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LeaderboardItemRow(user: LeaderboardUser) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (user.isCurrentUser) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "#${user.rank}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (user.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = Color(user.avatarBgColor),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(user.avatarInitial, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Anda",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${user.title} • 🔥 ${user.streakDays} Hari Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.points} Poin",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${user.weeklyPages} Halaman",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RemindersAndPrayerSettingsView(
    reminders: List<MurajaahReminderEntity>,
    prayerSchedule: PrayerSchedule,
    prayerAlarmSubuh: Boolean,
    prayerAlarmDzuhur: Boolean,
    prayerAlarmAshar: Boolean,
    prayerAlarmMaghrib: Boolean,
    prayerAlarmIsya: Boolean,
    onToggleReminder: (MurajaahReminderEntity) -> Unit,
    onAddReminder: () -> Unit,
    onTestMurajaahNotification: () -> Unit,
    onTestPrayerNotification: () -> Unit,
    onTogglePrayerSubuh: () -> Unit,
    onTogglePrayerDzuhur: () -> Unit,
    onTogglePrayerAshar: () -> Unit,
    onTogglePrayerMaghrib: () -> Unit,
    onTogglePrayerIsya: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Murajaah Reminders Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal & Pengingat Muraja'ah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddReminder) {
                    Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
        }

        items(reminders, key = { it.id }) { rem ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rem.sessionName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "Jam %02d:%02d WIB", rem.hour, rem.minute),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (rem.note.isNotBlank()) {
                            Text(
                                text = rem.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = rem.isEnabled,
                        onCheckedChange = { onToggleReminder(rem) }
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onTestMurajaahNotification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tes Notifikasi Pengingat Muraja'ah")
            }
        }

        item {
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Prayer Times Notifications Section
        item {
            Text(
                text = "Pengingat Waktu Sholat 5 Waktu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrayerToggleRow(name = "Subuh", time = prayerSchedule.subuh, checked = prayerAlarmSubuh, onToggle = onTogglePrayerSubuh)
                    PrayerToggleRow(name = "Dzuhur", time = prayerSchedule.dzuhur, checked = prayerAlarmDzuhur, onToggle = onTogglePrayerDzuhur)
                    PrayerToggleRow(name = "Ashar", time = prayerSchedule.ashar, checked = prayerAlarmAshar, onToggle = onTogglePrayerAshar)
                    PrayerToggleRow(name = "Maghrib", time = prayerSchedule.maghrib, checked = prayerAlarmMaghrib, onToggle = onTogglePrayerMaghrib)
                    PrayerToggleRow(name = "Isya", time = prayerSchedule.isya, checked = prayerAlarmIsya, onToggle = onTogglePrayerIsya)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onTestPrayerNotification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tes Bunyi Notifikasi Waktu Sholat")
            }
        }
    }
}

@Composable
private fun PrayerToggleRow(name: String, time: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Sholat $name", fontWeight = FontWeight.SemiBold)
            Text(text = time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun GDriveInstallGuideView(onOpenFullGuide: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Mudah Dipasang di HP via Google Drive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Aplikasi ini dirancang siap pakai dan dapat dipasang di semua smartphone Android pengguna melalui link Google Drive tanpa memerlukan akun Play Store developer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Button(
                        onClick = onOpenFullGuide,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buka Panduan Langkah demi Langkah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Keuntungan Pasang via Google Drive:", fontWeight = FontWeight.Bold)
                    BenefitItem("✅ Siapapun yang punya link bisa mengunduh file APK dan langsung memasang.")
                    BenefitItem("✅ Tidak ada biaya pendaftaran akun pengembang Google Play.")
                    BenefitItem("✅ Tetap bisa diperbarui dengan menimpa APK versi terbaru di Google Drive.")
                    BenefitItem("✅ Data Room Database hafalan tersimpan aman dan privat di memori HP masing-masing.")
                }
            }
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AddCustomReminderDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, hour: Int, minute: Int, note: String) -> Unit
) {
    var sessionName by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(5) }
    var minute by remember { mutableIntStateOf(0) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pengingat Muraja'ah", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    label = { Text("Nama Sesi") },
                    placeholder = { Text("Misal: Muraja'ah Ba'da Dhuha") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", hour),
                        onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                        label = { Text("Jam (0-23)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", minute),
                        onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                        label = { Text("Menit (0-59)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan Pengingat") },
                    placeholder = { Text("Target 1 lembar hafalan baru") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = if (sessionName.isNotBlank()) sessionName else "Sesi Muraja'ah"
                    onSave(name, hour, minute, noteText)
                },
                enabled = sessionName.isNotBlank()
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
