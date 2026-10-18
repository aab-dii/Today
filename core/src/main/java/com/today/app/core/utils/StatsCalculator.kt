package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.domain.model.CountItem
import com.today.app.core.domain.model.Stats
import java.util.concurrent.TimeUnit

object StatsCalculator {

    private const val TOP_LIMIT = 5

    fun calculate(entries: List<DiaryEntryEntity>): Stats {
        if (entries.isEmpty()) return Stats()

        val totalTitles = entries
            .map { "${it.mediaType}-${it.titleId}" }
            .distinct()
            .size

        val totalMinutes = entries.sumOf { (it.runtimeMinutes ?: 0).toLong() }

        val genreCounts = mutableMapOf<String, Int>()
        entries.forEach { entry ->
            GenreCatalog.namesFromCsv(entry.genreIds).forEach { genre ->
                genreCounts[genre] = (genreCounts[genre] ?: 0) + 1
            }
        }

        val countryCounts = mutableMapOf<String, Int>()
        entries.forEach { entry ->
            CountryCatalog.namesFromCsv(entry.country).forEach { country ->
                countryCounts[country] = (countryCounts[country] ?: 0) + 1
            }
        }

        val heatmap = entries.groupingBy {
            TimeUnit.MILLISECONDS.toDays(it.watchedOn)
        }.eachCount()

        return Stats(
            totalTitles = totalTitles,
            entryCount = entries.size,
            totalMinutes = totalMinutes,
            topGenres = genreCounts.toTopList(),
            topCountries = countryCounts.toTopList(),
            heatmap = heatmap
        )
    }

    private fun Map<String, Int>.toTopList(): List<CountItem> =
        entries
            .sortedByDescending { it.value }
            .take(TOP_LIMIT)
            .map { CountItem(label = it.key, count = it.value) }
}
