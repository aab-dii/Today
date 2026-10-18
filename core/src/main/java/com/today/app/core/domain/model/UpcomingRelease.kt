package com.today.app.core.domain.model

data class UpcomingRelease(
    val titleId: Int,
    val mediaType: MediaType,
    val titleName: String,
    val posterPath: String?,
    val rating: Double = 0.0,
    val date: String,
    val dateMillis: Long,
    val detail: String,
    val isEpisode: Boolean
)
