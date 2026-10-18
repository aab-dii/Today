package com.today.app.core.utils

import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.WatchStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvImportResult(
    val entries: List<DiaryEntry>,
    val skipped: Int
)

object DiaryCsvCodec {

    private val headers = listOf(
        "titleId", "mediaType", "titleName", "watchedOn", "watchedDate",
        "rating", "mood", "note", "watchedWith", "status", "seasonNumber", "episodeNumber"
    )

    fun export(entries: List<DiaryEntry>): String = buildString {
        appendLine(Csv.row(headers))
        entries.forEach { entry ->
            appendLine(
                Csv.row(
                    listOf(
                        entry.titleId.toString(),
                        entry.mediaType.value,
                        entry.titleName,
                        entry.watchedOn.toString(),
                        formatDate(entry.watchedOn),
                        entry.rating.toString(),
                        entry.mood?.name.orEmpty(),
                        entry.note,
                        entry.watchedWith.orEmpty(),
                        entry.status.name,
                        entry.seasonNumber?.toString().orEmpty(),
                        entry.episodeNumber?.toString().orEmpty()
                    )
                )
            )
        }
    }

    fun parse(text: String): CsvImportResult {
        val rows = Csv.parse(text.removePrefix("\uFEFF"))
        if (rows.isEmpty()) return CsvImportResult(emptyList(), 0)

        val header = rows.first().map { it.trim().lowercase(Locale.ROOT) }
        val index = header.withIndex().associate { it.value to it.index }
        val dataRows = rows.drop(1)

        val ours = index.containsKey("titleid") || index.containsKey("titlename")
        val letterboxd = !ours && (index.containsKey("name") || index.containsKey("watched date"))

        if (!ours && !letterboxd) {
            return CsvImportResult(emptyList(), dataRows.size)
        }

        val entries = mutableListOf<DiaryEntry>()
        var skipped = 0
        dataRows.forEach { row ->
            fun cell(key: String): String? = index[key]?.let { row.getOrNull(it) }?.trim()

            val entry = if (letterboxd) parseLetterboxd(::cell) else parseOurs(::cell)
            if (entry == null) skipped++ else entries.add(entry)
        }
        return CsvImportResult(entries, skipped)
    }

    private fun parseOurs(cell: (String) -> String?): DiaryEntry? {
        val titleName = cell("titlename").orEmpty()
        if (titleName.isBlank()) return null
        val watchedOn = cell("watchedon")?.toLongOrNull()
            ?: parseDate(cell("watcheddate"))
            ?: System.currentTimeMillis()
        return DiaryEntry(
            titleId = cell("titleid")?.toIntOrNull() ?: 0,
            mediaType = MediaType.from(cell("mediatype")) ?: MediaType.MOVIE,
            titleName = titleName,
            posterPath = null,
            watchedOn = watchedOn,
            rating = cell("rating")?.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: 0.0,
            mood = Mood.from(cell("mood")),
            note = cell("note").orEmpty(),
            watchedWith = cell("watchedwith")?.takeIf { it.isNotBlank() },
            status = WatchStatus.from(cell("status")),
            seasonNumber = cell("seasonnumber")?.toIntOrNull(),
            episodeNumber = cell("episodenumber")?.toIntOrNull()
        )
    }

    private fun parseLetterboxd(cell: (String) -> String?): DiaryEntry? {
        val name = cell("name").orEmpty()
        if (name.isBlank()) return null
        val date = cell("watched date") ?: cell("date")
        return DiaryEntry(
            titleId = 0,
            mediaType = MediaType.MOVIE,
            titleName = name,
            posterPath = null,
            watchedOn = parseDate(date) ?: System.currentTimeMillis(),
            rating = cell("rating")?.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: 0.0,
            mood = null,
            note = "",
            watchedWith = null,
            status = WatchStatus.COMPLETED
        )
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    private fun parseDate(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)?.time
        } catch (e: Exception) {
            null
        }
    }
}
