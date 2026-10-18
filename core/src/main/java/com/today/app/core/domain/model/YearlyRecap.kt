package com.today.app.core.domain.model

data class RecapTitle(
    val titleId: Int,
    val mediaType: MediaType,
    val name: String,
    val posterPath: String?,
    val rating: Double,
    val count: Int
)

data class YearlyRecap(
    val year: Int,
    val totalTitles: Int = 0,
    val entryCount: Int = 0,
    val totalMinutes: Long = 0L,
    val rewatchCount: Int = 0,
    val topGenres: List<CountItem> = emptyList(),
    val topCountries: List<CountItem> = emptyList(),
    val topMood: Mood? = null,
    val topTitle: RecapTitle? = null,
    val highestRated: RecapTitle? = null,
    val busiestMonth: Int? = null,
    val busiestMonthCount: Int = 0,
    val longestStreak: Int = 0,
    val topCompanion: String? = null
) {
    val totalHours: Long get() = totalMinutes / 60
    val isEmpty: Boolean get() = entryCount == 0
}
