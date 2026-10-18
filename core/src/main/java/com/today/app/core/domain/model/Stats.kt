package com.today.app.core.domain.model

data class CountItem(
    val label: String,
    val count: Int
)

data class Stats(
    val totalTitles: Int = 0,
    val entryCount: Int = 0,
    val totalMinutes: Long = 0L,
    val topGenres: List<CountItem> = emptyList(),
    val topCountries: List<CountItem> = emptyList(),
    val heatmap: Map<Long, Int> = emptyMap()
) {
    val totalHours: Long get() = totalMinutes / 60
}
