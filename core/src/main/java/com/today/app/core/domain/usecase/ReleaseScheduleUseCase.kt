package com.today.app.core.domain.usecase

import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.core.domain.repository.ICatalogRepository
import com.today.app.core.domain.repository.IDiaryRepository
import com.today.app.core.utils.ReleaseScheduleCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ReleaseScheduleUseCase {
    fun getUpcomingReleases(): Flow<List<UpcomingRelease>>
}

class ReleaseScheduleInteractor(
    private val catalogRepository: ICatalogRepository,
    private val diaryRepository: IDiaryRepository
) : ReleaseScheduleUseCase {

    override fun getUpcomingReleases(): Flow<List<UpcomingRelease>> =
        diaryRepository.getWatchlist().map { items ->
            val details = items.mapNotNull { item ->
                runCatching {
                    catalogRepository.getTitleDetail(item.mediaType, item.titleId)
                }.getOrNull()?.let { item to it }
            }
            ReleaseScheduleCalculator.build(details, System.currentTimeMillis())
        }
}
