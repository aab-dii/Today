package com.today.app.social.domain.repository

import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    suspend fun createCircle(name: String): Result<Circle>

    suspend fun joinByCode(code: String): Result<Circle>

    suspend fun leaveCircle(circleId: String): Result<Unit>

    fun observeMyCircles(): Flow<List<Circle>>

    fun observeCircle(circleId: String): Flow<Circle?>

    fun observeMembers(circleId: String): Flow<List<CircleMember>>
}
