package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.QuranConstants
import com.example.data.VoiceRecordingEntity
import com.example.ui.MurajaahViewModel
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldSecondary
import com.example.ui.theme.StatusMutqin
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioScreen(
    viewModel: MurajaahViewModel,
    initialSurahNumber: Int = 1
) {
    val context = LocalContext.current
    val recorderManager = viewModel.recorderManager
    val playerManager = viewModel.playerManager

    val isRecording by recorderManager.isRecording.collectAsState()
    val recordingDuration by recorderManager.recordingDurationSeconds.collectAsState()
    val amplitude by recorderManager.currentAmplitude.collectAsState()

    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPlayingFile by playerManager.currentPlayingFile.collectAsState()
    val currentPositionMs by playerManager.currentPositionMs.collectAsState()
    val durationMs by playerManager.durationMs.collectAsState()
    val playbackSpeed by playerManager.playbackSpeed.collectAsState()

    val recordingsList by viewModel.allRecordings.collectAsState()

    var selectedSurahNumber by remember { mutableIntStateOf(initialSurahNumber) }
    var ayahRangeText by remember { mutableStateOf("Ayat 1 - 10") }
    var isSurahDropdownOpen by remember { mutableStateOf(false) }

    // Dialog after stopping recording
    var pendingRecordedFile by remember { mutableStateOf<File?>(null) }
    var pendingDurationSecs by remember { mutableIntStateOf(0) }
    var showEvaluationDialog by remember { mutableStateOf(false) }

    // Permission launcher
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasRecordPermission = granted
    }

    val currentSurah = QuranConstants.SURAHS.find { it.number == selectedSurahNumber } ?: QuranConstants.SURAHS.first()

    // Pulsing animation for mic button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("audio_studio_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Studio Rekaman & Evaluasi Mandiri",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Rekam bacaan muraja'ah Anda dan dengarkan kembali untuk mengevaluasi tajwid, fashahah, dan kelancaran secara mandiri.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Recording Studio Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recording_studio_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Surah selection & Ayah range inputs
                    if (!isRecording) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = isSurahDropdownOpen,
                                onExpandedChange = { isSurahDropdownOpen = !isSurahDropdownOpen },
                                modifier = Modifier.weight(1.3f)
                            ) {
                                OutlinedTextField(
                                    value = "${currentSurah.number}. ${currentSurah.nameLatin}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Surah") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSurahDropdownOpen) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = isSurahDropdownOpen,
                                    onDismissRequest = { isSurahDropdownOpen = false },
                                    modifier = Modifier.heightIn(max = 280.dp)
                                ) {
                                    QuranConstants.SURAHS.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("${s.number}. ${s.nameLatin}") },
                                            onClick = {
                                                selectedSurahNumber = s.number
                                                isSurahDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = ayahRangeText,
                                onValueChange = { ayahRangeText = it },
                                label = { Text("Cakupan Ayat") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // While recording info
                        Text(
                            text = "Sedang Merekam: ${currentSurah.nameLatin} ($ayahRangeText)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Recording Timer Display
                    val minutes = recordingDuration / 60
                    val seconds = recordingDuration % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    // Waveform / Amplitude visualization
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..19) {
                            val barHeight = if (isRecording) {
                                val normalized = (amplitude.toFloat() / 32768f)
                                ((kotlin.math.sin(i * 0.5 + recordingDuration) + 1.2) * 12 * (normalized + 0.3f)).coerceIn(4.0, 30.0).dp
                            } else {
                                4.dp
                            }
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isRecording) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f))
                            )
                        }
                    }

                    // Big Mic Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(76.dp)
                                .clickable {
                                    if (!hasRecordPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (isRecording) {
                                            val dur = recordingDuration
                                            val file = recorderManager.stopRecording()
                                            if (file != null && dur > 1) {
                                                pendingRecordedFile = file
                                                pendingDurationSecs = dur
                                                showEvaluationDialog = true
                                            }
                                        } else {
                                            recorderManager.startRecording(selectedSurahNumber)
                                        }
                                    }
                                }
                                .testTag("record_toggle_button"),
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = if (isRecording) "Stop Rekaman" else "Mulai Rekaman",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isRecording) "Ketuk tombol merah untuk selesai & evaluasi" else "Ketuk mikrofon untuk mulai rekam bacaan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Saved Recordings List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Rekaman & Hasil Evaluasi (${recordingsList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (recordingsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.MicNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Belum Ada Rekaman Hafalan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Merekam hafalan dan mendengarkannya kembali adalah metode terbaik para huffazh untuk menemukan kesalahan kecil pada makhraj dan panjang mad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recordingsList, key = { it.id }) { rec ->
                val isThisPlaying = (currentPlayingFile == rec.filePath && isPlaying)
                RecordingItemCard(
                    recording = rec,
                    isPlaying = isThisPlaying,
                    isCurrentFile = (currentPlayingFile == rec.filePath),
                    currentPositionMs = if (currentPlayingFile == rec.filePath) currentPositionMs else 0,
                    durationMs = if (currentPlayingFile == rec.filePath) durationMs else (rec.durationSeconds * 1000),
                    playbackSpeed = playbackSpeed,
                    onPlayToggle = { playerManager.playFile(rec.filePath) },
                    onSeek = { playerManager.seekTo(it) },
                    onSpeedChange = { playerManager.setSpeed(it) },
                    onDelete = { viewModel.deleteRecording(rec) }
                )
            }
        }
    }

    // Self-Evaluation Dialog on Recording Finish
    if (showEvaluationDialog && pendingRecordedFile != null) {
        SelfEvaluationDialog(
            surahName = currentSurah.nameLatin,
            ayahRange = ayahRangeText,
            durationSecs = pendingDurationSecs,
            onDismiss = {
                showEvaluationDialog = false
                pendingRecordedFile?.delete()
                pendingRecordedFile = null
            },
            onSave = { tajwid, kelancaran, makhraj, notes ->
                pendingRecordedFile?.let { file ->
                    viewModel.saveRecordingEvaluation(
                        surahNumber = selectedSurahNumber,
                        ayahRange = ayahRangeText,
                        recordedFile = file,
                        durationSecs = pendingDurationSecs,
                        tajwid = tajwid,
                        kelancaran = kelancaran,
                        makhraj = makhraj,
                        notes = notes
                    )
                }
                showEvaluationDialog = false
                pendingRecordedFile = null
            }
        )
    }
}

@Composable
private fun RecordingItemCard(
    recording: VoiceRecordingEntity,
    isPlaying: Boolean,
    isCurrentFile: Boolean,
    currentPositionMs: Int,
    durationMs: Int,
    playbackSpeed: Float,
    onPlayToggle: () -> Unit,
    onSeek: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recording_card_${recording.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${recording.recordedDate} • Durasi ${recording.durationSeconds / 60}m ${recording.durationSeconds % 60}d",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Scores badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreChip(label = "Tajwid", score = recording.scoreTajwid)
                ScoreChip(label = "Kelancaran", score = recording.scoreKelancaran)
                ScoreChip(label = "Makhraj", score = recording.scoreMakhraj)
            }

            if (recording.selfNotes.isNotBlank()) {
                Text(
                    text = "Catatan: ${recording.selfNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Player Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledIconButton(
                    onClick = onPlayToggle,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Slider
                val maxRange = durationMs.toFloat().coerceAtLeast(1f)
                val currentPos = if (isCurrentFile) currentPositionMs.toFloat() else 0f
                Slider(
                    value = currentPos.coerceIn(0f, maxRange),
                    onValueChange = { onSeek(it.toInt()) },
                    valueRange = 0f..maxRange,
                    modifier = Modifier.weight(1f)
                )

                // Speed button (toggle between 1x, 1.25x, 1.5x)
                if (isCurrentFile) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            val nextSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                else -> 1.0f
                            }
                            onSpeedChange(nextSpeed)
                        },
                        label = { Text("${playbackSpeed}x", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreChip(label: String, score: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$score/5 ⭐",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IslamicGoldSecondary
            )
        }
    }
}

@Composable
private fun SelfEvaluationDialog(
    surahName: String,
    ayahRange: String,
    durationSecs: Int,
    onDismiss: () -> Unit,
    onSave: (tajwid: Int, kelancaran: Int, makhraj: Int, notes: String) -> Unit
) {
    var scoreTajwid by remember { mutableIntStateOf(5) }
    var scoreKelancaran by remember { mutableIntStateOf(5) }
    var scoreMakhraj by remember { mutableIntStateOf(5) }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("self_evaluation_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Evaluasi Mandiri Bacaan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$surahName ($ayahRange) • Durasi: ${durationSecs / 60}m ${durationSecs % 60}d",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                ScoreSlider(title = "Tajwid & Ghunnah", score = scoreTajwid, onScoreChange = { scoreTajwid = it })
                ScoreSlider(title = "Kelancaran & Hafalan", score = scoreKelancaran, onScoreChange = { scoreKelancaran = it })
                ScoreSlider(title = "Makhorijul Huruf & Fashahah", score = scoreMakhraj, onScoreChange = { scoreMakhraj = it })

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Koreksi Mandiri") },
                    placeholder = { Text("Contoh: Hati-hati mad di ayat 7, dengungkan ikhfa lebih lama") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { onSave(scoreTajwid, scoreKelancaran, scoreMakhraj, notesText) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreSlider(title: String, score: Int, onScoreChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("$score / 5", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = IslamicGoldSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 1..5) {
                IconButton(
                    onClick = { onScoreChange(i) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (i <= score) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i <= score) IslamicGoldSecondary else Color.Gray
                    )
                }
            }
        }
    }
}
