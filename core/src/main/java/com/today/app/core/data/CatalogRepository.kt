package com.today.app.core.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.today.app.core.data.source.TitleRemoteMediator
import com.today.app.core.data.source.local.LocalDataSource
import com.today.app.core.data.source.local.room.TitleDatabase
import com.today.app.core.data.source.remote.RemoteDataSource
import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.Title
import com.today.app.core.domain.model.TitleDetail
import com.today.app.core.domain.repository.ICatalogRepository
import com.today.app.core.utils.DataMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
class CatalogRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val database: TitleDatabase
) : ICatalogRepository {

    override fun getTitles(): Flow<PagingData<Title>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = TitleRemoteMediator(remoteDataSource, database),
            pagingSourceFactory = { localDataSource.getPagedTitles() }
        ).flow.map { pagingData ->
            pagingData.map { DataMapper.mapEntityToDomain(it) }
        }
    }

    override suspend fun getCatalogRow(row: CatalogRow): List<Title> =
        DataMapper.mapResultsToDomain(remoteDataSource.getCatalogRow(row))

    override suspend fun searchTitles(query: String): List<Title> =
        remoteDataSource.searchTitles(query).mapNotNull { DataMapper.mapResultToDomain(it) }

    override suspend fun getTitleDetail(mediaType: MediaType, id: Int): TitleDetail =
        DataMapper.mapDetailToDomain(
            remoteDataSource.getTitleDetail(mediaType, id),
            mediaType,
            trailerKey = remoteDataSource.getTrailerKey(mediaType, id)
        )
}
