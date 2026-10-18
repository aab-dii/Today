package com.today.app.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class Mood(val label: String) {
    AMAZED("Kagum"),
    MOVED("Terharu"),
    HAPPY("Senang"),
    OKAY("Biasa saja"),
    DISAPPOINTED("Kecewa"),
    TENSE("Tegang"),
    BORED("Bosan");

    companion object {
        fun from(value: String?): Mood? = entries.firstOrNull { it.name == value }
    }
}

enum class WatchStatus(val label: String) {
    WATCHING("Sedang ditonton"),
    COMPLETED("Selesai"),
    DROPPED("Di-drop");

    companion object {
        fun from(value: String?): WatchStatus =
            entries.firstOrNull { it.name == value } ?: COMPLETED
    }
}

@Parcelize
data class DiaryEntry(
    val id: Long = 0,
    val titleId: Int,
    val mediaType: MediaType,
    val titleName: String,
    val posterPath: String?,
    val watchedOn: Long,
    val rating: Double = 0.0,
    val mood: Mood? = null,
    val note: String = "",
    val watchedWith: String? = null,
    val status: WatchStatus = WatchStatus.COMPLETED,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val rewatchOf: Long? = null,
    val genreIds: String = "",
    val country: String = "",
    val runtimeMinutes: Int? = null
) : Parcelable
