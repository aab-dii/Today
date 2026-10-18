package com.today.app.ui.schedule

import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.UpcomingRelease
import com.today.app.core.domain.usecase.ReleaseScheduleUseCase
import com.today.app.ui.social.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun release(id: Int, name: String) = UpcomingRelease(
        titleId = id,
        mediaType = MediaType.TV,
        titleName = name,
        posterPath = null,
        date = "2026-10-20",
        dateMillis = 1_000L,
        detail = "S1 E1",
        isEpisode = true
    )

    private class FakeUseCase(
        val flow: MutableStateFlow<List<UpcomingRelease>> = MutableStateFlow(emptyList()),
        val fail: Boolean = false
    ) : ReleaseScheduleUseCase {
        override fun getUpcomingReleases(): Flow<List<UpcomingRelease>> =
            if (fail) flow { throw RuntimeException("jaringan putus") } else flow
    }

    @Test
    fun `loads releases on init`() = runTest {
        val useCase = FakeUseCase(MutableStateFlow(listOf(release(1, "Reply 1988"))))

        val viewModel = ReleaseScheduleViewModel(useCase)

        assertFalse(viewModel.state.value.loading)
        assertEquals(1, viewModel.state.value.releases.size)
        assertEquals("Reply 1988", viewModel.state.value.releases.first().titleName)
    }

    @Test
    fun `refresh picks up new emissions`() = runTest {
        val useCase = FakeUseCase(MutableStateFlow(emptyList()))
        val viewModel = ReleaseScheduleViewModel(useCase)
        assertTrue(viewModel.state.value.releases.isEmpty())

        useCase.flow.value = listOf(release(2, "VisionQuest"), release(3, "Parasite"))
        viewModel.refresh()

        assertEquals(2, viewModel.state.value.releases.size)
    }

    @Test
    fun `error is surfaced in state`() = runTest {
        val useCase = FakeUseCase(fail = true)

        val viewModel = ReleaseScheduleViewModel(useCase)

        assertFalse(viewModel.state.value.loading)
        assertNotNull(viewModel.state.value.error)
    }
}
