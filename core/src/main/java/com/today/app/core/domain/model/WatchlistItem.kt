package com.today.app.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WatchlistItem(
    val titleId: Int,
    val mediaType: MediaType,
    val titleName: String,
    val posterPath: String?,
    val addedOn: Long
) : Parcelable
