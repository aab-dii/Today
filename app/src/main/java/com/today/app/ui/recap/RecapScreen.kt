package com.today.app.ui.recap

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.CountItem
import com.today.app.core.domain.model.YearlyRecap
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayChoiceChip
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

private enum class RecapPage { INTRO, TOTALS, GENRES, MOOD, TOP_TITLE, HIGHEST_RATED, RHYTHM, OUTRO }

private val monthNames = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

private fun pagesFor(recap: YearlyRecap): List<RecapPage> = buildList {
    add(RecapPage.INTRO)
    add(RecapPage.TOTALS)
    if (recap.topGenres.isNotEmpty()) add(RecapPage.GENRES)
    if (recap.topMood != null) add(RecapPage.MOOD)
    if (recap.topTitle != null) add(RecapPage.TOP_TITLE)
    if (recap.highestRated != null) add(RecapPage.HIGHEST_RATED)
    if (recap.busiestMonth != null || recap.longestStreak > 1 || recap.topCompanion != null) {
        add(RecapPage.RHYTHM)
    }
    add(RecapPage.OUTRO)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(
    onBack: () -> Unit,
    viewModel: RecapViewModel = koinViewModel()
) {
    val years by viewModel.years.collectAsStateWithLifecycle()
    val recap by viewModel.recap.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Rekap Tahunan", onBack = onBack) }
    ) { padding ->
        val current = recap
        when {
            current == null -> LoadingIndicator(modifier = Modifier.fillMaxSize().padding(padding))

            current.isEmpty -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TodayEmptyState(
                    title = "Belum ada rekap",
                    subtitle = "Belum ada tontonan yang tercatat tahun ini. Mulai catat untuk membuka rekap.",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            else -> {
                val pages = pagesFor(current)
                val pagerState = rememberPagerState(pageCount = { pages.size })

                LaunchedEffect(current.year) { pagerState.scrollToPage(0) }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (years.size > 1) {
                        YearSelector(
                            years = years,
                            selected = current.year,
                            onSelect = viewModel::selectYear
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = Spacing.md,
                        contentPadding = PaddingValues(horizontal = Spacing.screen),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) { index ->
                        RecapPageCard(
                            page = pages[index],
                            recap = current,
                            onShare = { context.shareRecap(current) }
                        )
                    }

                    PageDots(
                        count = pages.size,
                        selected = pagerState.currentPage,
                        modifier = Modifier.padding(vertical = Spacing.lg)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearSelector(
    years: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        years.forEach { year ->
            TodayChoiceChip(
                text = year.toString(),
                selected = year == selected,
                onClick = { onSelect(year) }
            )
        }
    }
}

@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == selected) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun RecapPageCard(page: RecapPage, recap: YearlyRecap, onShare: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (page) {
                RecapPage.INTRO -> IntroPage(recap)
                RecapPage.TOTALS -> TotalsPage(recap)
                RecapPage.GENRES -> GenresPage(recap)
                RecapPage.MOOD -> MoodPage(recap)
                RecapPage.TOP_TITLE -> TopTitlePage(recap)
                RecapPage.HIGHEST_RATED -> HighestRatedPage(recap)
                RecapPage.RHYTHM -> RhythmPage(recap)
                RecapPage.OUTRO -> OutroPage(recap, onShare)
            }
        }
    }
}

@Composable
private fun PageTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(Spacing.md))
}

@Composable
private fun BigNumber(value: String, caption: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    Text(
        text = caption,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun IntroPage(recap: YearlyRecap) {
    Text(
        text = "Rekap Tontonan",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
        text = recap.year.toString(),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(Spacing.lg))
    Text(
        text = "Inilah cerita menontonmu sepanjang tahun.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    Text(
        text = "Geser untuk melihat →",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TotalsPage(recap: YearlyRecap) {
    PageTitle("Tahun ini kamu menonton")
    BigNumber(recap.totalTitles.toString(), "judul berbeda")
    Spacer(modifier = Modifier.height(Spacing.xxl))
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxl)) {
        MiniStat(recap.entryCount.toString(), "catatan")
        MiniStat(recap.totalHours.toString(), "jam")
        if (recap.rewatchCount > 0) MiniStat(recap.rewatchCount.toString(), "nonton ulang")
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GenresPage(recap: YearlyRecap) {
    PageTitle("Genre favoritmu")
    BarList(recap.topGenres)
    if (recap.topCountries.isNotEmpty()) {
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = "Asal negara",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        BarList(recap.topCountries)
    }
}

@Composable
private fun MoodPage(recap: YearlyRecap) {
    val mood = recap.topMood ?: return
    PageTitle("Perasaanmu paling sering")
    Text(
        text = mood.label,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
        text = "Mood ini paling mewakili tontonanmu tahun ini.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TopTitlePage(recap: YearlyRecap) {
    val top = recap.topTitle ?: return
    PageTitle("Paling sering ditonton")
    AsyncImage(
        model = posterUrl(top.posterPath),
        contentDescription = top.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(width = 130.dp, height = 195.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(modifier = Modifier.height(Spacing.md))
    Text(
        text = top.name,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    Text(
        text = "Ditonton ${top.count} kali",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun HighestRatedPage(recap: YearlyRecap) {
    val best = recap.highestRated ?: return
    PageTitle("Rating tertinggi")
    Text(
        text = best.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(Spacing.md))
    RatingDisplay(rating = best.rating, starSize = 28.dp)
    Spacer(modifier = Modifier.height(Spacing.xs))
    Text(
        text = "%.1f".format(best.rating),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun RhythmPage(recap: YearlyRecap) {
    PageTitle("Irama menontonmu")
    recap.busiestMonth?.let { month ->
        val idx = (month - 1).coerceIn(0, monthNames.lastIndex)
        MiniStat(recap.busiestMonthCount.toString(), "catatan di ${monthNames[idx]}")
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
    if (recap.longestStreak > 1) {
        MiniStat(recap.longestStreak.toString(), "hari beruntun")
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
    recap.topCompanion?.let { companion ->
        MiniStat(companion, "teman nonton teratas")
    }
}

@Composable
private fun OutroPage(recap: YearlyRecap, onShare: () -> Unit) {
    PageTitle("Sampai jumpa di rekap berikutnya")
    Text(
        text = "${recap.year} penuh cerita. Terus catat tontonanmu!",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(Spacing.xl))
    TodayPrimaryButton(
        text = "Bagikan rekap",
        onClick = onShare,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Composable
private fun BarList(items: List<CountItem>) {
    val max = items.maxOf { it.count }.coerceAtLeast(1)
    Column(modifier = Modifier.fillMaxWidth()) {
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
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

private fun android.content.Context.shareRecap(recap: YearlyRecap) {
    val text = buildString {
        append("Rekap Tontonan ${recap.year} — Today\n")
        append("• ${recap.totalTitles} judul • ${recap.entryCount} catatan • ${recap.totalHours} jam\n")
        recap.topGenres.firstOrNull()?.let { append("• Genre favorit: ${it.label}\n") }
        recap.topMood?.let { append("• Mood dominan: ${it.label}\n") }
        recap.topTitle?.let { append("• Paling sering ditonton: ${it.name}\n") }
        recap.highestRated?.let { append("• Rating tertinggi: ${it.name} (${"%.1f".format(it.rating)})\n") }
        if (recap.longestStreak > 1) append("• Streak terpanjang: ${recap.longestStreak} hari\n")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Rekap Tontonan ${recap.year}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Bagikan rekap"))
}
