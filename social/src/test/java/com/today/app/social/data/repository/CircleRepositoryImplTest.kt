package com.today.app.social.data.repository

import com.today.app.social.data.source.CircleDataSource
import com.today.app.social.domain.model.Circle
import com.today.app.social.domain.model.CircleMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CircleRepositoryImplTest {

    private val sample = Circle(
        id = "circle-1",
        name = "Nobar",
        ownerUid = "uid-1",
        ownerName = "Rina",
        inviteCode = "ABC123",
        memberCount = 1,
        createdAt = 10L
    )

    private class FakeCircleDataSource(
        var onCreate: (String) -> Circle = { error("not set") },
        var onJoin: (String) -> Circle = { error("not set") },
        var onLeave: (String) -> Unit = { error("not set") }
    ) : CircleDataSource {
        override suspend fun createCircle(name: String): Circle = onCreate(name)
        override suspend fun joinByCode(code: String): Circle = onJoin(code)
        override suspend fun leaveCircle(circleId: String) = onLeave(circleId)
        override fun observeMyCircles(): Flow<List<Circle>> = flowOf(emptyList())
        override fun observeCircle(circleId: String): Flow<Circle?> = flowOf(null)
        override fun observeMembers(circleId: String): Flow<List<CircleMember>> = flowOf(emptyList())
    }

    @Test
    fun `createCircle wraps success in Result`() = runTest {
        val repository = CircleRepositoryImpl(FakeCircleDataSource(onCreate = { sample }))

        val result = repository.createCircle("Nobar")

        assertTrue(result.isSuccess)
        assertEquals("circle-1", result.getOrNull()?.id)
    }

    @Test
    fun `createCircle wraps failure in Result`() = runTest {
        val repository = CircleRepositoryImpl(
            FakeCircleDataSource(onCreate = { error("Belum masuk") })
        )

        val result = repository.createCircle("Nobar")

        assertTrue(result.isFailure)
    }

    @Test
    fun `joinByCode passes trimmed error through Result`() = runTest {
        val repository = CircleRepositoryImpl(
            FakeCircleDataSource(onJoin = { error("Kode undangan tidak ditemukan") })
        )

        val result = repository.joinByCode("zzz")

        assertTrue(result.isFailure)
        assertEquals("Kode undangan tidak ditemukan", result.exceptionOrNull()?.message)
    }

    @Test
    fun `leaveCircle wraps success in Result`() = runTest {
        var left: String? = null
        val repository = CircleRepositoryImpl(
            FakeCircleDataSource(onLeave = { left = it })
        )

        val result = repository.leaveCircle("circle-9")

        assertTrue(result.isSuccess)
        assertEquals("circle-9", left)
    }

    @Test
    fun `observeMyCircles delegates to data source`() = runTest {
        val repository = CircleRepositoryImpl(FakeCircleDataSource())

        val circles = repository.observeMyCircles().first()

        assertEquals(emptyList<Circle>(), circles)
    }
}
