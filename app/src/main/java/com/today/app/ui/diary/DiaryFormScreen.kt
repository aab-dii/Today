package com.today.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchStatus
import com.today.app.ui.components.RatingBar
import com.today.app.ui.components.TodayChoiceChip
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.formatDay
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiaryFormScreen(
    title: Title?,
    diary: DiaryEntry?,
    rewatchOf: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: DiaryFormViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.start(title, diary, rewatchOf)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TodayTopBar(
                title = when {
                    rewatchOf != null -> "Tonton ulang"
                    state.isEditing -> "Ubah catatan"
                    else -> "Catat tontonan"
                },
                onBack = onBack,
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Hapus catatan",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = posterUrl(state.posterPath),
                            contentDescription = state.titleName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        text = state.titleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            FieldSection(label = "Tanggal nonton") {
                TodayTonalButton(
                    text = formatDay(state.watchedOn),
                    onClick = { showDatePicker = true },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null)
                    }
                )
            }

            FieldSection(label = "Nilai") {
                RatingBar(rating = state.rating, onRatingChange = viewModel::setRating)
            }

            FieldSection(label = "Status") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    WatchStatus.entries.forEach { status ->
                        TodayChoiceChip(
                            text = status.label,
                            selected = state.status == status,
                            onClick = { viewModel.setStatus(status) }
                        )
                    }
                }
            }

            if (state.mediaType == MediaType.TV) {
                FieldSection(label = "Progres episode") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        TodayTextField(
                            value = state.seasonNumber,
                            onValueChange = viewModel::setSeason,
                            label = "Musim",
                            modifier = Modifier.weight(1f)
                        )
                        TodayTextField(
                            value = state.episodeNumber,
                            onValueChange = viewModel::setEpisode,
                            label = "Episode",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            FieldSection(label = "Mood") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Mood.entries.forEach { mood ->
                        TodayChoiceChip(
                            text = mood.label,
                            selected = state.mood == mood,
                            onClick = { viewModel.setMood(if (state.mood == mood) null else mood) }
                        )
                    }
                }
            }

            TodayTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = "Catatan (opsional)",
                singleLine = false,
                minLines = 4
            )

            TodayTextField(
                value = state.watchedWith,
                onValueChange = viewModel::setWatchedWith,
                label = "Nonton bareng siapa? (opsional)"
            )

            TodayPrimaryButton(
                text = "Simpan",
                onClick = { viewModel.save(onDone) },
                enabled = state.isLoaded && (state.titleId != 0 || state.isEditing)
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.watchedOn
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setWatchedOn(it) }
                    showDatePicker = false
                }) { Text(text = "Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(text = "Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FieldSection(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        content()
    }
}
