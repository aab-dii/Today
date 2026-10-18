package com.today.app.social.domain.mapper

import com.today.app.core.domain.model.DiaryEntry
import com.today.app.social.domain.model.Opinion

object OpinionMapper {

    fun fromDiaryEntry(
        entry: DiaryEntry,
        authorUid: String,
        authorName: String,
        note: String,
        spoiler: Boolean,
        createdAt: Long = System.currentTimeMillis()
    ): Opinion = Opinion(
        id = "",
        authorUid = authorUid,
        authorName = authorName,
        titleId = entry.titleId,
        mediaType = entry.mediaType,
        titleName = entry.titleName,
        posterPath = entry.posterPath,
        rating = entry.rating,
        mood = entry.mood,
        note = note.trim(),
        spoiler = spoiler,
        createdAt = createdAt,
        likeCount = 0,
        commentCount = 0
    )
}
