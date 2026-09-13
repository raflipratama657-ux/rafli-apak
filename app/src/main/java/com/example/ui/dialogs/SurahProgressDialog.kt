package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.MurajaahProgressEntity
import com.example.data.QuranConstants
import com.example.ui.theme.IslamicGoldSecondary
import com.example.ui.theme.StatusLancar
import com.example.ui.theme.StatusMutqin
import com.example.ui.theme.StatusSedangHafal

@Composable
fun SurahProgressDialog(
    surahNumber: Int,
    currentProgress: MurajaahProgressEntity?,
    onDismiss: () -> Unit,
    onNavigateToRecord: (surahNumber: Int) -> Unit,
    onSave: (memorizedAyahs: Int, status: String, qualityRating: Int, notes: String) -> Unit
) {
    val surah = QuranConstants.SURAHS.find { it.number == surahNumber } ?: return

    var memorizedCount by remember {
        mutableIntStateOf(currentProgress?.memorizedAyahs ?: 0)
    }
    var selectedStatus by remember {
        mutableStateOf(currentProgress?.status ?: "BELUM_HAFAL")
    }
    var starRating by remember {
        mutableIntStateOf(currentProgress?.qualityRating ?: 0)
    }
    var notesText by remember {
        mutableStateOf(currentProgress?.notes ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("surah_progress_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${surah.number}. ${surah.nameLatin}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${surah.nameArabic} • ${surah.meaning} • ${surah.totalVerses} Ayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Memorized verses slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jumlah Ayat Dihafal:", fontWeight = FontWeight.SemiBold)
                        Text(
                            "$memorizedCount / ${surah.totalVerses} Ayat",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = memorizedCount.toFloat(),
                        onValueChange = {
                            memorizedCount = it.toInt()
                            if (memorizedCount == surah.totalVerses && selectedStatus == "BELUM_HAFAL") {
                                selectedStatus = "MUTQIN"
                            }
                        },
                        valueRange = 0f..surah.totalVerses.toFloat(),
                        steps = if (surah.totalVerses > 1) surah.totalVerses - 1 else 0
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { memorizedCount = 0; selectedStatus = "BELUM_HAFAL" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset 0")
                        }
                        OutlinedButton(
                            onClick = { memorizedCount = surah.totalVerses; selectedStatus = "MUTQIN" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hafal Semua")
                        }
                    }
                }

                // Status Chips
                Text("Status Kelancaran Hafalan:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = (selectedStatus == "MUTQIN"),
                        onClick = {
                            selectedStatus = "MUTQIN"
                            memorizedCount = surah.totalVerses
                        },
                        label = { Text("Mutqin") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusMutqin.copy(alpha = 0.2f),
                            selectedLabelColor = StatusMutqin
                        )
                    )
                    FilterChip(
                        selected = (selectedStatus == "LANCAR"),
                        onClick = { selectedStatus = "LANCAR" },
                        label = { Text("Lancar") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusLancar.copy(alpha = 0.2f),
                            selectedLabelColor = StatusLancar
                        )
                    )
                    FilterChip(
                        selected = (selectedStatus == "SEDANG_MENGHAFAL"),
                        onClick = { selectedStatus = "SEDANG_MENGHAFAL" },
                        label = { Text("Sedang Hafal") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusSedangHafal.copy(alpha = 0.2f),
                            selectedLabelColor = StatusSedangHafal
                        )
                    )
                }

                // Rating
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tingkat Kualitas Bacaan:", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { starRating = i }) {
                                Icon(
                                    imageVector = if (i <= starRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "$i Bintang",
                                    tint = if (i <= starRating) IslamicGoldSecondary else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Hafalan & Tajwid") },
                    placeholder = { Text("Contoh: Waspada ayat mutasyabih dengan Surah Ali Imran") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Shortcut to Audio Recording
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onNavigateToRecord(surah.number)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rekam Suara Evaluasi Surah Ini")
                }

                // Save button
                Button(
                    onClick = {
                        onSave(memorizedCount, selectedStatus, starRating, notesText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_surah_progress_button")
                ) {
                    Text("Perbarui Progres", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
