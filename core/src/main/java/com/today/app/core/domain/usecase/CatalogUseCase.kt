package com.today.app.core.domain.usecase

import androidx.paging.PagingData
import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.TitleDetail
import kotlinx.coroutines.flow.Flow

interface CatalogUseCase {
    fun getTitles(): Flow<PagingData<Title>>

    suspend fun getCatalogRow(row: CatalogRow): List<Title>

    suspend fun searchTitles(query: String): List<Title>

    suspend fun getTitleDetail(mediaType: MediaType, id: Int): TitleDetail
}
