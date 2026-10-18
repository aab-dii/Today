package com.today.app.core.utils

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.TitleDetail
import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.core.domain.model.WatchlistItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReleaseScheduleCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun build(items: List<Pair<WatchlistItem, TitleDetail>>, now: Long): List<UpcomingRelease> {
        val todayStart = startOfDay(now)
        return items
            .mapNotNull { (item, detail) -> toRelease(item, detail, todayStart) }
            .sortedBy { it.dateMillis }
    }

    fun dueSoon(
        releases: List<UpcomingRelease>,
        now: Long,
        windowDays: Int = 1
    ): List<UpcomingRelease> {
        val start = startOfDay(now)
        val end = endOfDay(now, windowDays)
        return releases.filter { it.dateMillis in start..end }
    }

    private fun toRelease(
        item: WatchlistItem,
        detail: TitleDetail,
        todayStart: Long
    ): UpcomingRelease? {
        val episode = detail.nextEpisode
        val date = when {
            detail.mediaType == MediaType.TV && episode != null -> episode.airDate
            detail.mediaType == MediaType.MOVIE -> detail.releaseDate
            else -> null
        }
        if (date.isNullOrBlank()) return null

        val millis = parseDate(date) ?: return null
        if (millis < todayStart) return null

        return UpcomingRelease(
            titleId = item.titleId,
            mediaType = item.mediaType,
            titleName = item.titleName.ifBlank { detail.name },
            posterPath = item.posterPath ?: detail.posterPath,
            rating = detail.rating,
            date = date,
            dateMillis = millis,
            detail = label(detail, episode != null),
            isEpisode = detail.mediaType == MediaType.TV && episode != null
        )
    }

    private fun label(detail: TitleDetail, hasEpisode: Boolean): String {
        if (!hasEpisode) return "Rilis film"
        val episode = detail.nextEpisode ?: return "Episode baru"
        val code = buildString {
            episode.seasonNumber?.let { append("S$it ") }
            episode.episodeNumber?.let { append("E$it") }
        }.trim()
        return when {
            code.isNotBlank() && episode.name.isNotBlank() -> "$code · ${episode.name}"
            code.isNotBlank() -> code
            episode.name.isNotBlank() -> episode.name
            else -> "Episode baru"
        }
    }

    private fun parseDate(value: String): Long? = try {
        dateFormat.apply { isLenient = false }.parse(value)?.time
    } catch (e: Exception) {
        null
    }

    private fun startOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun endOfDay(millis: Long, plusDays: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_YEAR, plusDays)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
}
