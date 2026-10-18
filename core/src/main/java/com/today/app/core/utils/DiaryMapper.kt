package com.today.app.core.utils

import com.today.app.core.data.source.local.entity.CustomListEntryEntity
import com.today.app.core.data.source.local.entity.CustomListWithCount
import com.today.app.core.data.source.local.entity.DiaryEntryEntity
import com.today.app.core.data.source.local.entity.WatchlistItemEntity
import com.today.app.core.domain.model.CustomList
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Mood
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem

object DiaryMapper {

    fun toDomain(entity: DiaryEntryEntity): DiaryEntry = DiaryEntry(
        id = entity.id,
        titleId = entity.titleId,
        mediaType = MediaType.from(entity.mediaType) ?: MediaType.MOVIE,
        titleName = entity.titleName,
        posterPath = entity.posterPath,
        watchedOn = entity.watchedOn,
        rating = entity.rating,
        mood = Mood.from(entity.mood),
        note = entity.note,
        watchedWith = entity.watchedWith,
        status = WatchStatus.from(entity.status),
        seasonNumber = entity.seasonNumber,
        episodeNumber = entity.episodeNumber,
        rewatchOf = entity.rewatchOf,
        genreIds = entity.genreIds,
        country = entity.country,
        runtimeMinutes = entity.runtimeMinutes
    )

    fun toEntity(domain: DiaryEntry): DiaryEntryEntity = DiaryEntryEntity(
        id = domain.id,
        titleId = domain.titleId,
        mediaType = domain.mediaType.value,
        titleName = domain.titleName,
        posterPath = domain.posterPath,
        watchedOn = domain.watchedOn,
        rating = domain.rating,
        mood = domain.mood?.name,
        note = domain.note,
        watchedWith = domain.watchedWith,
        status = domain.status.name,
        seasonNumber = domain.seasonNumber,
        episodeNumber = domain.episodeNumber,
        rewatchOf = domain.rewatchOf,
        genreIds = domain.genreIds,
        country = domain.country,
        runtimeMinutes = domain.runtimeMinutes
    )

    fun watchlistToDomain(entity: WatchlistItemEntity): WatchlistItem = WatchlistItem(
        titleId = entity.titleId,
        mediaType = MediaType.from(entity.mediaType) ?: MediaType.MOVIE,
        titleName = entity.titleName,
        posterPath = entity.posterPath,
        addedOn = entity.addedOn
    )

    fun watchlistToEntity(domain: WatchlistItem): WatchlistItemEntity = WatchlistItemEntity(
        titleId = domain.titleId,
        mediaType = domain.mediaType.value,
        titleName = domain.titleName,
        posterPath = domain.posterPath,
        addedOn = domain.addedOn
    )

    fun listToDomain(entity: CustomListWithCount): CustomList = CustomList(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        createdAt = entity.createdAt,
        itemCount = entity.itemCount
    )

    fun listItemToDomain(entity: CustomListEntryEntity): CustomListItem = CustomListItem(
        titleId = entity.titleId,
        mediaType = MediaType.from(entity.mediaType) ?: MediaType.MOVIE,
        titleName = entity.titleName,
        posterPath = entity.posterPath
    )

    fun listItemToEntity(listId: Long, item: CustomListItem, addedAt: Long): CustomListEntryEntity =
        CustomListEntryEntity(
            listId = listId,
            titleId = item.titleId,
            mediaType = item.mediaType.value,
            titleName = item.titleName,
            posterPath = item.posterPath,
            addedAt = addedAt
        )
}
