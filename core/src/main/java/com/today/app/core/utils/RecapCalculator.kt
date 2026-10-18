package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.domain.model.CountItem
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.RecapTitle
import com.today.app.core.domain.model.YearlyRecap
import java.util.Calendar
import java.util.concurrent.TimeUnit

object RecapCalculator {

    private const val TOP_LIMIT = 3
    private const val MILLIS_PER_DAY = 86_400_000L

    fun availableYears(entries: List<DiaryEntryEntity>): List<Int> =
        entries.map { yearOf(it.watchedOn) }.distinct().sortedDescending()

    fun calculate(entries: List<DiaryEntryEntity>, year: Int): YearlyRecap {
        val inYear = entries.filter { yearOf(it.watchedOn) == year }
        if (inYear.isEmpty()) return YearlyRecap(year = year)

        val totalTitles = inYear
            .map { "${it.mediaType}-${it.titleId}" }
            .distinct()
            .size

        val totalMinutes = inYear.sumOf { (it.runtimeMinutes ?: 0).toLong() }
        val rewatchCount = inYear.count { it.rewatchOf != null }

        val topGenres = inYear
            .flatMap { GenreCatalog.namesFromCsv(it.genreIds) }
            .groupingBy { it }.eachCount().toTopList()

        val topCountries = inYear
            .flatMap { CountryCatalog.namesFromCsv(it.country) }
            .groupingBy { it }.eachCount().toTopList()

        val topMood = inYear
            .mapNotNull { Mood.from(it.mood) }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }
            ?.key

        val byTitle = inYear.groupBy { "${it.mediaType}-${it.titleId}" }
        val topTitle = byTitle.values
            .maxByOrNull { it.size }
            ?.let { group -> group.first().toRecapTitle(count = group.size) }

        val highestRated = inYear
            .filter { it.rating > 0.0 }
            .maxByOrNull { it.rating }
            ?.toRecapTitle(count = 1)

        val monthCounts = inYear
            .groupingBy { monthOf(it.watchedOn) }
            .eachCount()
        val busiest = monthCounts.maxByOrNull { it.value }

        val topCompanion = inYear
            .mapNotNull { it.watchedWith?.trim()?.takeIf { name -> name.isNotEmpty() } }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }
            ?.key

        return YearlyRecap(
            year = year,
            totalTitles = totalTitles,
            entryCount = inYear.size,
            totalMinutes = totalMinutes,
            rewatchCount = rewatchCount,
            topGenres = topGenres,
            topCountries = topCountries,
            topMood = topMood,
            topTitle = topTitle,
            highestRated = highestRated,
            busiestMonth = busiest?.key,
            busiestMonthCount = busiest?.value ?: 0,
            longestStreak = longestStreak(inYear),
            topCompanion = topCompanion
        )
    }

    private fun DiaryEntryEntity.toRecapTitle(count: Int): RecapTitle = RecapTitle(
        titleId = titleId,
        mediaType = MediaType.from(mediaType) ?: MediaType.MOVIE,
        name = titleName,
        posterPath = posterPath,
        rating = rating,
        count = count
    )

    private fun longestStreak(entries: List<DiaryEntryEntity>): Int {
        val days = entries
            .map { TimeUnit.MILLISECONDS.toDays(it.watchedOn) }
            .distinct()
            .sorted()
        if (days.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (i in 1 until days.size) {
            if (days[i] == days[i - 1] + 1) {
                current += 1
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun Map<String, Int>.toTopList(): List<CountItem> =
        entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(TOP_LIMIT)
            .map { CountItem(label = it.key, count = it.value) }

    private fun yearOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)

    private fun monthOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MONTH) + 1
}
