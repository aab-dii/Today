package com.today.app.core.domain.usecase

import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.repository.ICatalogRepository

class CatalogInteractor(private val catalogRepository: ICatalogRepository) : CatalogUseCase {

    override fun getTitles() = catalogRepository.getTitles()

    override suspend fun getCatalogRow(row: CatalogRow) = catalogRepository.getCatalogRow(row)

    override suspend fun searchTitles(query: String) = catalogRepository.searchTitles(query)

    override suspend fun getTitleDetail(mediaType: MediaType, id: Int) =
        catalogRepository.getTitleDetail(mediaType, id)
}
