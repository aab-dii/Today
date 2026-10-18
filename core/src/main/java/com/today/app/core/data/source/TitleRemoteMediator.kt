package com.today.app.core.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.today.app.core.data.source.local.entity.TitleEntity
import com.today.app.core.data.source.local.room.TitleDatabase
import com.today.app.core.data.source.remote.RemoteDataSource
import com.today.app.core.utils.DataMapper

@OptIn(ExperimentalPagingApi::class)
class TitleRemoteMediator(
    private val remoteDataSource: RemoteDataSource,
    private val database: TitleDatabase
) : RemoteMediator<Int, TitleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TitleEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.APPEND -> state.lastItemOrNull()?.page?.plus(1) ?: 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }

            val results = remoteDataSource.getDiscoveryPage(page)
            val entities = DataMapper.mapResultsToEntities(results, page)

            database.withTransaction {
                database.titleDao().upsertTitles(entities)
            }

            MediatorResult.Success(endOfPaginationReached = results.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
