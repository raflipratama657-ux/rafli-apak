package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.JuzData
import com.example.data.MurajaahProgressEntity
import com.example.data.QuranConstants
import com.example.data.SurahData
import com.example.ui.MurajaahViewModel
import com.example.ui.dialogs.SurahProgressDialog
import com.example.ui.theme.IslamicEmeraldPrimary
import com.example.ui.theme.IslamicGoldSecondary
import com.example.ui.theme.StatusBelumHafal
import com.example.ui.theme.StatusLancar
import com.example.ui.theme.StatusMutqin
import com.example.ui.theme.StatusSedangHafal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranMurajaahScreen(
    viewModel: MurajaahViewModel,
    onNavigateToRecord: (Int) -> Unit
) {
    val allProgress by viewModel.allProgress.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surah, 1 = Juz
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua") } // "Semua", "MUTQIN", "LANCAR", "SEDANG_MENGHAFAL", "BELUM_HAFAL"
    var selectedSurahForDialog by remember { mutableStateOf<Int?>(null) }

    // Progress map
    val progressMap = remember(allProgress) {
        allProgress.associateBy { it.surahNumber }
    }

    val filteredSurahs = remember(searchQuery, statusFilter, allProgress) {
        QuranConstants.SURAHS.filter { surah ->
            val matchQuery = surah.nameLatin.contains(searchQuery, ignoreCase = true) ||
                    surah.meaning.contains(searchQuery, ignoreCase = true) ||
                    surah.number.toString() == searchQuery.trim()

            val progress = progressMap[surah.number]
            val currentStatus = progress?.status ?: "BELUM_HAFAL"
            val matchStatus = when (statusFilter) {
                "Semua" -> true
                "MUTQIN" -> currentStatus == "MUTQIN"
                "LANCAR" -> currentStatus == "LANCAR"
                "SEDANG_MENGHAFAL" -> currentStatus == "SEDANG_MENGHAFAL"
                "BELUM_HAFAL" -> currentStatus == "BELUM_HAFAL"
                else -> true
            }

            matchQuery && matchStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_murajaah_screen")
    ) {
        // Top App Bar Area
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pelacak Hafalan Al-Qur'an 30 Juz",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari surah, arti, atau nomor (mis: Al-Baqarah, 2)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_field")
                )

                // Tabs: Surah vs Juz
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("114 Surah", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("30 Juz", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Filter chips (only in Surah tab)
        if (selectedTab == 0) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "Semua" to "Semua",
                    "MUTQIN" to "Mutqin ✨",
                    "LANCAR" to "Lancar 🌿",
                    "SEDANG_MENGHAFAL" to "Sedang Hafal ⏳",
                    "BELUM_HAFAL" to "Belum 📖"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = (statusFilter == key),
                        onClick = { statusFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }

        // List View
        if (selectedTab == 0) {
            // Surahs List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val progress = progressMap[surah.number]
                    SurahListItemCard(
                        surah = surah,
                        progress = progress,
                        onClick = { selectedSurahForDialog = surah.number }
                    )
                }
            }
        } else {
            // 30 Juz List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(QuranConstants.JUZ_LIST, key = { it.juzNumber }) { juz ->
                    JuzListItemCard(
                        juz = juz,
                        progressMap = progressMap,
                        onSelectSurah = { surahNum -> selectedSurahForDialog = surahNum }
                    )
                }
            }
        }
    }

    // Dialog to update progress
    selectedSurahForDialog?.let { surahNum ->
        val progress = progressMap[surahNum]
        SurahProgressDialog(
            surahNumber = surahNum,
            currentProgress = progress,
            onDismiss = { selectedSurahForDialog = null },
            onNavigateToRecord = { onNavigateToRecord(it) },
            onSave = { memorized, status, rating, notes ->
                viewModel.updateSurahProgress(surahNum, memorized, status, rating, notes)
            }
        )
    }
}

@Composable
private fun SurahListItemCard(
    surah: SurahData,
    progress: MurajaahProgressEntity?,
    onClick: () -> Unit
) {
    val status = progress?.status ?: "BELUM_HAFAL"
    val memorized = progress?.memorizedAyahs ?: 0
    val progressPercent = (memorized.toFloat() / surah.totalVerses.toFloat()).coerceIn(0f, 1f)

    val (badgeText, badgeColor) = when (status) {
        "MUTQIN" -> "Mutqin" to StatusMutqin
        "LANCAR" -> "Lancar" to StatusLancar
        "SEDANG_MENGHAFAL" -> "Sedang Hafal" to StatusSedangHafal
        else -> "Belum" to StatusBelumHafal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Surah Number Emblem
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${surah.number}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Surah Latin Name & info
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = surah.nameLatin,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${surah.meaning} • Juz ${surah.juz} • Hal ${surah.startPage} • ${surah.totalVerses} Ayat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Arabic Title
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = badgeColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "$memorized/${surah.totalVerses} Ayat",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JuzListItemCard(
    juz: JuzData,
    progressMap: Map<Int, MurajaahProgressEntity>,
    onSelectSurah: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("juz_item_${juz.juzNumber}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${juz.juzNumber}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column {
                        Text(
                            text = juz.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Halaman ${juz.startPage} - ${juz.endPage} (20-22 Halaman)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "30 Juz",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Cakupan: ${juz.startSurahName} (Ayat ${juz.startAyah}) s/d ${juz.endSurahName} (Ayat ${juz.endAyah})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Quick Surah buttons inside this Juz
            val surahsInJuz = QuranConstants.SURAHS.filter { it.juz == juz.juzNumber }
            if (surahsInJuz.isNotEmpty()) {
                Text(
                    text = "Surah dalam Juz ini:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(surahsInJuz) { surah ->
                        val p = progressMap[surah.number]
                        val isMutqin = p?.status == "MUTQIN"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMutqin) StatusMutqin.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isMutqin) ButtonDefaults.outlinedButtonBorder(true).copy(brush = androidx.compose.ui.graphics.SolidColor(StatusMutqin)) else null,
                            modifier = Modifier.clickable { onSelectSurah(surah.number) }
                        ) {
                            Text(
                                text = "${surah.number}. ${surah.nameLatin}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMutqin) StatusMutqin else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
