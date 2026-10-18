package com.today.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayContinueCard
import com.today.app.ui.components.TodayEmpty
import com.today.app.ui.components.TodayPosterBox
import com.today.app.ui.components.TodaySectionHeader
import com.today.app.ui.components.TodayStatPill
import com.today.app.ui.components.formatDay
import com.today.app.ui.components.label
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    onLogNew: () -> Unit,
    onQuickComplete: (DiaryEntry) -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onOpenTitle: (Title) -> Unit,
    onOpenFeed: () -> Unit = {},
    onOpenSchedule: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val trending by viewModel.trending.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val throwback by viewModel.throwback.collectAsStateWithLifecycle()
    val weekly by viewModel.weekly.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLogNew,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "Catat", style = MaterialTheme.typography.labelLarge) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = Spacing.lg, bottom = 140.dp)
        ) {
            item {
                HomeHeader(
                    summary = weekly,
                    onOpenSchedule = onOpenSchedule,
                    onOpenFeed = onOpenFeed
                )
            }

            if (continueWatching.isNotEmpty()) {
                item { SectionGap() }
                item { TodaySectionHeader(title = "Lanjutkan menonton") }
                item { Spacer(Modifier.height(Spacing.md)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screen),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(continueWatching, key = { "c-${it.entry.id}" }) { item ->
                            TodayContinueCard(
                                titleName = item.entry.titleName,
                                posterPath = item.entry.posterPath,
                                seasonNumber = item.entry.seasonNumber,
                                episodeNumber = item.entry.episodeNumber,
                                totalEpisodes = item.totalEpisodes,
                                onOpen = { onQuickComplete(item.entry) },
                                onBump = { viewModel.bumpEpisode(item.entry) }
                            )
                        }
                    }
                }
            }

            if (watchlist.isNotEmpty()) {
                item { SectionGap() }
                item { TodaySectionHeader(title = "Dari watchlist-mu") }
                item { Spacer(Modifier.height(Spacing.md)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screen),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(watchlist, key = { "w-${it.mediaType.name}-${it.titleId}" }) { item ->
                            TodayPosterBox(
                                titleName = item.titleName,
                                posterPath = item.posterPath,
                                onClick = { onOpenTitle(item.toTitle()) }
                            )
                        }
                    }
                }
            }

            if (trending.isNotEmpty()) {
                item { SectionGap() }
                item { TodaySectionHeader(title = "Sedang tren minggu ini") }
                item { Spacer(Modifier.height(Spacing.md)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screen),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(trending, key = { "t-${it.mediaType.name}-${it.id}" }) { title ->
                            TodayPosterBox(
                                titleName = title.name,
                                posterPath = title.posterPath,
                                badge = title.rating.takeIf { it > 0.0 }?.let { "★ %.1f".format(it) },
                                onClick = { onOpenTitle(title) }
                            )
                        }
                    }
                }
            }

            if (recommendations.isNotEmpty()) {
                item { SectionGap() }
                item { TodaySectionHeader(title = "Rekomendasi untukmu") }
                item { Spacer(Modifier.height(Spacing.md)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screen),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(recommendations, key = { "r-${it.mediaType.name}-${it.id}" }) { title ->
                            TodayPosterBox(
                                titleName = title.name,
                                posterPath = title.posterPath,
                                badge = title.rating.takeIf { it > 0.0 }?.let { "★ %.1f".format(it) },
                                onClick = { onOpenTitle(title) }
                            )
                        }
                    }
                }
            }

            throwback?.let { entry ->
                item { SectionGap() }
                item { TodaySectionHeader(title = "Throwback") }
                item { Spacer(Modifier.height(Spacing.md)) }
                item {
                    ThrowbackCard(entry = entry, onClick = { onEntryClick(entry) })
                }
            }

            item { SectionGap() }
            item { TodaySectionHeader(title = "Catatan terbaru") }
            item { Spacer(Modifier.height(Spacing.md)) }

            if (entries.isEmpty()) {
                item {
                    TodayEmpty(
                        text = "Belum ada catatan.\nTekan \"Catat\" untuk mulai mencatat tontonanmu."
                    )
                }
            } else {
                items(entries, key = { "e-${it.id}" }) { entry ->
                    DiaryCard(entry = entry, onClick = { onEntryClick(entry) })
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    summary: WeeklySummary,
    onOpenSchedule: () -> Unit,
    onOpenFeed: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.screen)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Hari ini nonton apa?",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                HeaderAction(
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Jadwal Rilis") },
                    onClick = onOpenSchedule
                )
                HeaderAction(
                    icon = { Icon(Icons.Filled.DynamicFeed, contentDescription = "Feed Komunitas") },
                    onClick = onOpenFeed
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))

        if (summary.entryCount > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TodayStatPill(text = "${summary.titleCount} judul") {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                TodayStatPill(text = "${summary.hours} jam")
                TodayStatPill(text = "${summary.entryCount} catatan")
            }
        } else {
            Text(
                text = "Belum ada catatan minggu ini. Yuk mulai.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeaderAction(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun ThrowbackCard(entry: DiaryEntry, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 108.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = posterUrl(entry.posterPath),
                    contentDescription = entry.titleName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Setahun lalu kamu menonton",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = entry.titleName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Spacing.sm))
                if (entry.rating > 0.0) {
                    RatingDisplay(rating = entry.rating, starSize = 16.dp)
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Tonton ulang untuk memperbarui penilaianmu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiaryCard(entry: DiaryEntry, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.xs)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = posterUrl(entry.posterPath),
                    contentDescription = entry.titleName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.titleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = entry.mediaType.label(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = Spacing.sm, vertical = 3.dp)
                    )
                }

                Spacer(Modifier.height(Spacing.xs))

                Text(
                    text = formatDay(entry.watchedOn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (entry.rating > 0.0 || entry.mood != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.rating > 0.0) {
                            RatingDisplay(rating = entry.rating, starSize = 14.dp)
                        }
                        entry.mood?.let { mood ->
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = mood.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (entry.note.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionGap() {
    Spacer(Modifier.height(Spacing.sectionGap))
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> "SELAMAT PAGI"
        in 11..14 -> "SELAMAT SIANG"
        in 15..18 -> "SELAMAT SORE"
        else -> "SELAMAT MALAM"
    }
}

private fun WatchlistItem.toTitle(): Title = Title(
    id = titleId,
    mediaType = mediaType,
    name = titleName,
    originalName = "",
    country = "",
    language = "",
    genre = "",
    releaseDate = "",
    rating = 0.0,
    popularity = 0.0,
    overview = "",
    posterPath = posterPath,
    backdropPath = null
)
