package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.QuranConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogDialog(
    initialSurahNumber: Int = 1,
    onDismiss: () -> Unit,
    onSave: (surahNumber: Int, startAyah: Int, endAyah: Int, pagesRead: Int, durationMinutes: Int, quality: String, notes: String) -> Unit
) {
    var selectedSurahNumber by remember { mutableStateOf(initialSurahNumber) }
    var startAyahText by remember { mutableStateOf("1") }
    var endAyahText by remember { mutableStateOf("7") }
    var pagesReadText by remember { mutableStateOf("2") }
    var durationText by remember { mutableStateOf("15") }
    var selectedQuality by remember { mutableStateOf("Sangat Lancar (Mutqin)") }
    var notesText by remember { mutableStateOf("") }
    var isSurahDropdownExpanded by remember { mutableStateOf(false) }

    val qualityOptions = listOf(
        "Sangat Lancar (Mutqin)",
        "Lancar",
        "Cukup Lancar (Sedikit Lupa)",
        "Perlu Banyak Diulang"
    )

    val currentSurah = QuranConstants.SURAHS.find { it.number == selectedSurahNumber } ?: QuranConstants.SURAHS.first()

    LaunchedEffect(selectedSurahNumber) {
        endAyahText = currentSurah.totalVerses.coerceAtMost(20).toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_log_dialog"),
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
                    Text(
                        text = "Catat Muraja'ah Harian",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Surah Selector
                ExposedDropdownMenuBox(
                    expanded = isSurahDropdownExpanded,
                    onExpandedChange = { isSurahDropdownExpanded = !isSurahDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${currentSurah.number}. ${currentSurah.nameLatin} (${currentSurah.totalVerses} Ayat)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Surah") },
                        leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSurahDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isSurahDropdownExpanded,
                        onDismissRequest = { isSurahDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        QuranConstants.SURAHS.forEach { surah ->
                            DropdownMenuItem(
                                text = { Text("${surah.number}. ${surah.nameLatin} (${surah.nameArabic})") },
                                onClick = {
                                    selectedSurahNumber = surah.number
                                    isSurahDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ayah Range
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startAyahText,
                        onValueChange = { startAyahText = it.filter { char -> char.isDigit() } },
                        label = { Text("Dari Ayat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endAyahText,
                        onValueChange = { endAyahText = it.filter { char -> char.isDigit() } },
                        label = { Text("Sampai Ayat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pages & Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = pagesReadText,
                        onValueChange = { pagesReadText = it.filter { char -> char.isDigit() } },
                        label = { Text("Halaman") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { char -> char.isDigit() } },
                        label = { Text("Durasi (Menit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quality Selector
                Text(
                    text = "Kualitas Kelancaran",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    qualityOptions.forEach { opt ->
                        FilterChip(
                            selected = (selectedQuality == opt),
                            onClick = { selectedQuality = opt },
                            label = { Text(opt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Ayat / Tajwid (Opsional)") },
                    placeholder = { Text("Contoh: Perlu hati-hati di ayat 15") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Save Button
                Button(
                    onClick = {
                        val startAyah = startAyahText.toIntOrNull() ?: 1
                        val endAyah = endAyahText.toIntOrNull() ?: currentSurah.totalVerses
                        val pages = pagesReadText.toIntOrNull() ?: 1
                        val duration = durationText.toIntOrNull() ?: 15
                        onSave(selectedSurahNumber, startAyah, endAyah, pages, duration, selectedQuality, notesText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_log_button")
                ) {
                    Text("Simpan Muraja'ah Hari Ini", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
