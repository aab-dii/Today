package com.today.app.ui.schedule

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.notifications.ReleaseNotifications
import com.today.app.notifications.ReleaseNotificationScheduler
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodaySectionHeader
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.formatDay
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseScheduleScreen(
    onBack: () -> Unit,
    onTitleClick: (UpcomingRelease) -> Unit,
    viewModel: ReleaseScheduleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TodayTopBar(
                title = "Jadwal Rilis",
                onBack = onBack,
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Segarkan"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        var notificationsEnabled by remember {
            mutableStateOf(ReleaseNotifications.isEnabled(context))
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                ReleaseNotifications.setEnabled(context, true)
                ReleaseNotificationScheduler.schedule(context)
                notificationsEnabled = true
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NotificationToggle(
                enabled = notificationsEnabled,
                onToggle = { value ->
                    if (value) {
                        if (ReleaseNotifications.hasPermission(context)) {
                            ReleaseNotifications.setEnabled(context, true)
                            ReleaseNotificationScheduler.schedule(context)
                            notificationsEnabled = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        ReleaseNotifications.setEnabled(context, false)
                        ReleaseNotificationScheduler.cancel(context)
                        notificationsEnabled = false
                    }
                }
            )

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.loading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))

                    state.error != null -> Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.xxl)
                    )

                    state.releases.isEmpty() -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screen),
                        contentAlignment = Alignment.Center
                    ) {
                        TodayEmptyState(
                            title = "Belum ada jadwal mendatang",
                            subtitle = "Tambahkan film atau serial ke Watchlist untuk memantau rilisan berikutnya.",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }

                    else -> {
                        val now = System.currentTimeMillis()
                        val grouped = state.releases.groupBy { it.date }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            grouped.forEach { (_, releases) ->
                                item(key = "h-${releases.first().date}") {
                                    Spacer(Modifier.height(Spacing.sm))
                                    TodaySectionHeader(title = sectionLabel(releases.first().dateMillis, now))
                                    Spacer(Modifier.height(Spacing.sm))
                                }
                                items(releases, key = { "r-${it.titleId}-${it.mediaType.value}" }) { release ->
                                    ReleaseRow(
                                        release = release,
                                        onClick = { onTitleClick(release) }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(Spacing.xxl)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifikasi jadwal rilis",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Pengingat episode/film mendatang (sekitar pukul 08.00)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ReleaseRow(
    release: UpcomingRelease,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.xs)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = posterUrl(release.posterPath),
                contentDescription = release.titleName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 56.dp, height = 84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = release.titleName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = release.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = formatDay(release.dateMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun sectionLabel(dateMillis: Long, now: Long): String = when (dateMillis) {
    startOfDay(now) -> "Hari ini"
    startOfDay(now) + DAY_MILLIS -> "Besok"
    else -> formatDay(dateMillis)
}

private fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
