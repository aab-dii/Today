package com.today.app.ui.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.core.domain.model.CountItem
import com.today.app.core.domain.model.Stats
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodaySectionHeader
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import java.util.concurrent.TimeUnit
import org.koin.androidx.compose.koinViewModel

private const val HEATMAP_DAYS = 91
private const val HEATMAP_COLUMNS = 13

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: (() -> Unit)? = null,
    viewModel: StatsViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Statistik", onBack = onBack) }
    ) { padding ->
        if (stats.entryCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TodayEmptyState(
                    title = "Belum ada data",
                    subtitle = "Mulai catat tontonan untuk melihat statistik dan pola menontonmu.",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                Spacer(Modifier.height(Spacing.sm))
                SummaryRow(stats)

                Spacer(Modifier.height(Spacing.sectionGap))
                TodaySectionHeader(title = "Aktivitas ${HEATMAP_DAYS / 7} minggu terakhir")
                Spacer(Modifier.height(Spacing.md))
                Heatmap(stats)

                if (stats.topGenres.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.sectionGap))
                    TodaySectionHeader(title = "Genre favorit")
                    Spacer(Modifier.height(Spacing.md))
                    BarList(stats.topGenres)
                }

                if (stats.topCountries.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.sectionGap))
                    TodaySectionHeader(title = "Asal negara")
                    Spacer(Modifier.height(Spacing.md))
                    BarList(stats.topCountries)
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}

@Composable
private fun SummaryRow(stats: Stats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        SummaryCard("Judul", stats.totalTitles.toString(), Modifier.weight(1f))
        SummaryCard("Catatan", stats.entryCount.toString(), Modifier.weight(1f))
        SummaryCard("Jam", stats.totalHours.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Heatmap(stats: Stats) {
    val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val startDay = today - (HEATMAP_DAYS - 1)
    val maxCount = stats.heatmap.values.maxOrNull() ?: 1

    Row(
        modifier = Modifier.padding(horizontal = Spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (col in 0 until HEATMAP_COLUMNS) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (row in 0 until 7) {
                    val day = startDay + col * 7 + row
                    val count = if (day > today) 0 else stats.heatmap[day] ?: 0
                    val alpha = if (count == 0) 0.08f else (0.3f + 0.7f * (count.toFloat() / maxCount))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun BarList(items: List<CountItem>) {
    val max = items.maxOf { it.count }.coerceAtLeast(1)
    Column(modifier = Modifier.padding(horizontal = Spacing.screen)) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(110.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.count.toFloat() / max)
                            .height(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = item.count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
