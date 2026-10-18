package com.today.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchStatus
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayPosterBox
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.backdropUrl
import com.today.app.ui.components.formatDayLong
import com.today.app.ui.components.formatIsoDateLong
import com.today.app.ui.components.label
import com.today.app.ui.components.posterUrl
import com.today.app.ui.lists.AddToListDialog
import com.today.app.ui.social.OpinionViewModel
import com.today.app.ui.social.ShareOpinionDialog
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    title: Title,
    onBack: () -> Unit,
    onLog: () -> Unit,
    onEditEntry: (DiaryEntry) -> Unit,
    onRewatch: (DiaryEntry) -> Unit,
    onOpenTitle: (Title) -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
    opinionViewModel: OpinionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val inWatchlist by viewModel.inWatchlist.collectAsStateWithLifecycle()
    val opinionState by opinionViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAddToList by remember { mutableStateOf(false) }
    var shareEntry by remember { mutableStateOf<DiaryEntry?>(null) }

    LaunchedEffect(opinionState.message, opinionState.error) {
        (opinionState.message ?: opinionState.error)?.let {
            shareEntry = null
            snackbarHostState.showSnackbar(it)
            opinionViewModel.clearMessage()
        }
    }

    LaunchedEffect(title.id, title.mediaType) {
        viewModel.bind(title)
    }

    val watching = history.firstOrNull { it.status == WatchStatus.WATCHING }
    val latestEntry = history.firstOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TodayTopBar(
                title = title.name,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleWatchlist(title, inWatchlist) }) {
                        Icon(
                            imageVector = if (inWatchlist) Icons.Filled.Bookmark
                            else Icons.Filled.BookmarkBorder,
                            contentDescription = if (inWatchlist) "Hapus dari watchlist"
                            else "Tambah ke watchlist",
                            tint = if (inWatchlist) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Opsi")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(text = "Tambah ke daftar") },
                            onClick = {
                                showMenu = false
                                showAddToList = true
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            AsyncImage(
                model = backdropUrl(title.backdropPath) ?: posterUrl(title.posterPath),
                contentDescription = title.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(modifier = Modifier.padding(Spacing.screen)) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.mediaType.label(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.sm, vertical = 3.dp)
                    )
                    if (title.releaseDate.isNotBlank()) {
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Text(
                            text = formatIsoDateLong(title.releaseDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (title.rating > 0.0) {
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(title.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    TodayPrimaryButton(
                        text = if (latestEntry != null) "Catat lagi" else "Catat",
                        onClick = { if (latestEntry != null) onRewatch(latestEntry) else onLog() },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    if (title.mediaType == MediaType.TV) {
                        TodayTonalButton(
                            text = "Progres",
                            onClick = { showProgressDialog = true },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }

                if (watching != null) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    val progress = buildString {
                        watching.seasonNumber?.let { append("Musim $it") }
                        watching.episodeNumber?.let {
                            if (isNotEmpty()) append(" · ")
                            append("Episode $it")
                        }
                    }
                    Text(
                        text = if (progress.isNotEmpty()) "Sedang ditonton · $progress"
                        else "Sedang ditonton",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                when (val state = uiState) {
                    is DetailUiState.Loading -> LoadingIndicator()

                    is DetailUiState.Failed -> {
                        if (title.overview.isNotBlank()) {
                            Text(
                                text = title.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is DetailUiState.Ready -> {
                        if (state.detail.genreNames.isNotEmpty()) {
                            Text(
                                text = state.detail.genreNames.joinToString(" • "),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }

                        RangeText(
                            listOfNotNull(
                                state.detail.runtimeMinutes?.takeIf { it > 0 }?.let { minutes ->
                                    val suffix = if (title.mediaType == MediaType.TV) " menit/episode" else " menit"
                                    "$minutes$suffix"
                                },
                                buildString {
                                    state.detail.totalSeasons?.let { append("$it musim") }
                                    state.detail.totalEpisodes?.let {
                                        if (isNotEmpty()) append(" · ")
                                        append("$it episode")
                                    }
                                }.takeIf { it.isNotEmpty() }
                            )
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Text(
                            text = state.detail.overview.ifBlank { title.overview },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        state.detail.trailerKey?.let { key ->
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            TodayTonalButton(
                                text = "Tonton trailer",
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.youtube.com/watch?v=$key")
                                    )
                                    runCatching { context.startActivity(intent) }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }

                        if (state.detail.cast.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Spacing.xl))
                            SectionTitle(text = "Pemain")
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                text = state.detail.cast.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (state.detail.similar.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Spacing.xl))
                            SectionTitle(text = "Mirip dengan ini")
                            Spacer(modifier = Modifier.height(Spacing.md))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                contentPadding = PaddingValues(end = Spacing.sm)
                            ) {
                                items(
                                    items = state.detail.similar,
                                    key = { "${it.mediaType.value}-${it.id}" }
                                ) { item ->
                                    TodayPosterBox(
                                        titleName = item.name,
                                        posterPath = item.posterPath,
                                        width = 120.dp,
                                        onClick = { onOpenTitle(item) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
                SectionTitle(text = "Linimasa tontonan")
                Spacer(modifier = Modifier.height(Spacing.md))

                if (history.isEmpty()) {
                    Text(
                        text = "Belum ada catatan. Yuk mulai catat tontonanmu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    history.forEach { entry ->
                        DiaryEntryRow(
                            entry = entry,
                            onClick = { onEditEntry(entry) },
                            onRewatch = { onRewatch(entry) },
                            onShare = { shareEntry = entry }
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                    }
                }
            }
        }
    }

    if (showProgressDialog) {
        val detail = (uiState as? DetailUiState.Ready)?.detail
        ProgressDialog(
            currentSeason = watching?.seasonNumber,
            currentEpisode = watching?.episodeNumber,
            totalSeasons = detail?.totalSeasons,
            totalEpisodes = detail?.totalEpisodes,
            onDismiss = { showProgressDialog = false },
            onConfirm = { season, episode ->
                viewModel.saveProgress(title, season, episode)
                showProgressDialog = false
            }
        )
    }

    if (showAddToList) {
        AddToListDialog(
            item = CustomListItem(
                titleId = title.id,
                mediaType = title.mediaType,
                titleName = title.name,
                posterPath = title.posterPath
            ),
            onDismiss = { showAddToList = false },
            onSaved = { showAddToList = false }
        )
    }

    shareEntry?.let { entry ->
        ShareOpinionDialog(
            entry = entry,
            publishing = opinionState.publishing,
            onDismiss = { if (!opinionState.publishing) shareEntry = null },
            onShare = { note, spoiler -> opinionViewModel.publish(entry, note, spoiler) }
        )
    }
}

@Composable
private fun RangeText(parts: List<String>) {
    if (parts.isEmpty()) return
    Text(
        text = parts.joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun DiaryEntryRow(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onRewatch: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDayLong(entry.watchedOn),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (entry.rewatchOf != null) {
                    Icon(
                        imageVector = Icons.Filled.Celebration,
                        contentDescription = "Rewatch",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = entry.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (entry.rating > 0.0) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                RatingDisplay(rating = entry.rating)
            }

            entry.mood?.let { mood ->
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Mood: ${mood.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (entry.watchedWith?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Nonton bareng: ${entry.watchedWith}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Row {
                TextButton(onClick = onShare) {
                    Text(text = "Bagikan", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                TextButton(onClick = onRewatch) {
                    Text(text = "Tonton ulang", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ProgressDialog(
    currentSeason: Int?,
    currentEpisode: Int?,
    totalSeasons: Int?,
    totalEpisodes: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?) -> Unit
) {
    var season by remember { mutableStateOf(currentSeason?.toString() ?: "") }
    var episode by remember { mutableStateOf(currentEpisode?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Progres episode") },
        text = {
            Column {
                TodayTextField(
                    value = season,
                    onValueChange = { season = it.filter { c -> c.isDigit() } },
                    label = if (totalSeasons != null) "Musim (total $totalSeasons)" else "Musim"
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                TodayTextField(
                    value = episode,
                    onValueChange = { episode = it.filter { c -> c.isDigit() } },
                    label = if (totalEpisodes != null) "Episode (total $totalEpisodes)" else "Episode"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(season.toIntOrNull(), episode.toIntOrNull())
            }) { Text(text = "Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Batal") }
        }
    )
}
