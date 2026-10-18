package com.today.app.social.data.repository

import com.today.app.social.data.source.CircleDataSource
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import com.today.app.social.domain.repository.CircleRepository
import kotlinx.coroutines.flow.Flow

class CircleRepositoryImpl(
    private val dataSource: CircleDataSource
) : CircleRepository {

    override suspend fun createCircle(name: String): Result<Circle> =
        runCatching { dataSource.createCircle(name) }

    override suspend fun joinByCode(code: String): Result<Circle> =
        runCatching { dataSource.joinByCode(code) }

    override suspend fun leaveCircle(circleId: String): Result<Unit> =
        runCatching { dataSource.leaveCircle(circleId) }

    override fun observeMyCircles(): Flow<List<Circle>> = dataSource.observeMyCircles()

    override fun observeCircle(circleId: String): Flow<Circle?> = dataSource.observeCircle(circleId)

    override fun observeMembers(circleId: String): Flow<List<CircleMember>> =
        dataSource.observeMembers(circleId)
}
