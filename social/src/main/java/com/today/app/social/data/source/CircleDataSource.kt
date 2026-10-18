package com.today.app.social.data.source

import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import kotlinx.coroutines.flow.Flow

interface CircleDataSource {
    suspend fun createCircle(name: String): Circle

    suspend fun joinByCode(code: String): Circle

    suspend fun leaveCircle(circleId: String)

    fun observeMyCircles(): Flow<List<Circle>>

    fun observeCircle(circleId: String): Flow<Circle?>

    fun observeMembers(circleId: String): Flow<List<CircleMember>>
}
