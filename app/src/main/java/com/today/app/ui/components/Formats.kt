package com.today.app.ui.components

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val idLocale = Locale("id", "ID")

private val dayFormat = SimpleDateFormat("d MMM yyyy", idLocale)
private val dayLongFormat = SimpleDateFormat("d MMMM yyyy", idLocale)
private val monthYearFormat = SimpleDateFormat("MMMM yyyy", idLocale)

private val isoDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val isoMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
private val isoYearFormat = SimpleDateFormat("yyyy", Locale.US)

fun formatDay(millis: Long): String = dayFormat.format(Date(millis))

fun formatDayLong(millis: Long): String = dayLongFormat.format(Date(millis))

fun formatIsoDate(value: String): String = formatIso(value, dayFormat)

fun formatIsoDateLong(value: String): String = formatIso(value, dayLongFormat)

private enum class IsoPrecision { DAY, MONTH, YEAR }

private fun formatIso(value: String, dayFormat: SimpleDateFormat): String {
    val raw = value.trim()
    if (raw.isEmpty()) return raw
    val parsed = parseIso(raw) ?: return raw
    return when (parsed.second) {
        IsoPrecision.DAY -> dayFormat.format(Date(parsed.first))
        IsoPrecision.MONTH -> monthYearFormat.format(Date(parsed.first))
        IsoPrecision.YEAR -> isoYearFormat.format(Date(parsed.first))
    }
}

private fun parseIso(value: String): Pair<Long, IsoPrecision>? {
    val attempts = listOf(
        isoDayFormat to IsoPrecision.DAY,
        isoMonthFormat to IsoPrecision.MONTH,
        isoYearFormat to IsoPrecision.YEAR
    )
    for ((formatter, precision) in attempts) {
        val position = ParsePosition(0)
        val parsed = formatter.parse(value, position) ?: continue
        if (position.index == value.length) return parsed.time to precision
    }
    return null
}
