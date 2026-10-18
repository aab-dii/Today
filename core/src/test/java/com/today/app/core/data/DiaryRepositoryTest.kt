package com.today.app.core.data

import com.today.app.core.data.source.local.DiaryLocalDataSource
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.core.domain.model.MediaType
import com.today.app.core.domain.model.WatchStatus
import com.today.app.core.domain.model.WatchlistItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiaryRepositoryTest {

    private lateinit var diaryDao: FakeDiaryDao
    private lateinit var watchlistDao: FakeWatchlistDao
    private lateinit var customListDao: FakeCustomListDao
    private lateinit var repository: DiaryRepository

    @Before
    fun setUp() {
        diaryDao = FakeDiaryDao()
        watchlistDao = FakeWatchlistDao()
        customListDao = FakeCustomListDao()
        repository = DiaryRepository(
            DiaryLocalDataSource(diaryDao, watchlistDao, customListDao)
        )
    }

    private fun entry(
        titleId: Int = 1,
        mediaType: MediaType = MediaType.MOVIE,
        watchedOn: Long = 100,
        status: WatchStatus = WatchStatus.COMPLETED,
        runtimeMinutes: Int? = 120
    ) = DiaryEntry(
        titleId = titleId,
        mediaType = mediaType,
        titleName = "Title $titleId",
        posterPath = null,
        watchedOn = watchedOn,
        rating = 4.0,
        status = status,
        runtimeMinutes = runtimeMinutes
    )

    @Test
    fun `saveDiaryEntry persists and returns id`() = runTest {
        val id = repository.saveDiaryEntry(entry(titleId = 5))

        assertTrue(id > 0)
        val saved = repository.getDiaryEntry(id)
        assertEquals(5, saved?.titleId)
        assertEquals(MediaType.MOVIE, saved?.mediaType)
    }

    @Test
    fun `getDiaryEntries maps entities to domain`() = runTest {
        repository.saveDiaryEntry(entry(titleId = 1))
        repository.saveDiaryEntry(entry(titleId = 2))

        val entries = repository.getDiaryEntries().first()

        assertEquals(2, entries.size)
        assertEquals(setOf(1, 2), entries.map { it.titleId }.toSet())
    }

    @Test
    fun `saveDiaryEntry removes matching title from watchlist`() = runTest {
        repository.addToWatchlist(WatchlistItem(1, MediaType.MOVIE, "A", null, 0))
        assertTrue(repository.isInWatchlist(1, MediaType.MOVIE).first())

        repository.saveDiaryEntry(entry(titleId = 1, mediaType = MediaType.MOVIE))

        assertFalse(repository.isInWatchlist(1, MediaType.MOVIE).first())
        assertTrue(repository.getWatchlist().first().isEmpty())
    }

    @Test
    fun `watchlist add and remove`() = runTest {
        val item = WatchlistItem(9, MediaType.TV, "Severance", "/p", 42)
        repository.addToWatchlist(item)

        assertEquals(listOf(item), repository.getWatchlist().first())

        repository.removeFromWatchlist(9, MediaType.TV)

        assertTrue(repository.getWatchlist().first().isEmpty())
        assertFalse(repository.isInWatchlist(9, MediaType.TV).first())
    }

    @Test
    fun `getLatestEntryForTitle returns most recent by watchedOn`() = runTest {
        repository.saveDiaryEntry(entry(titleId = 7, watchedOn = 10))
        repository.saveDiaryEntry(entry(titleId = 7, watchedOn = 99))

        val latest = repository.getLatestEntryForTitle(7, MediaType.MOVIE)

        assertEquals(99L, latest?.watchedOn)
    }

    @Test
    fun `getEntriesByStatus filters by status`() = runTest {
        repository.saveDiaryEntry(entry(titleId = 1, status = WatchStatus.WATCHING))
        repository.saveDiaryEntry(entry(titleId = 2, status = WatchStatus.COMPLETED))

        val watching = repository.getEntriesByStatus(WatchStatus.WATCHING).first()

        assertEquals(1, watching.size)
        assertEquals(1, watching.first().titleId)
    }

    @Test
    fun `custom list create add and query ids`() = runTest {
        val listId = repository.createCustomList("Favorit", "desc")
        val item = CustomListItem(3, MediaType.MOVIE, "Dune", null)

        repository.addTitleToCustomList(listId, item)

        val lists = repository.getCustomLists().first()
        assertEquals(1, lists.size)
        assertEquals("Favorit", lists.first().name)
        assertEquals(1, lists.first().itemCount)

        val items = repository.getCustomListItems(listId).first()
        assertEquals(listOf(item), items)

        assertEquals(listOf(listId), repository.getCustomListIdsForTitle(3, MediaType.MOVIE))
    }

    @Test
    fun `deleteDiaryEntry removes entry`() = runTest {
        val id = repository.saveDiaryEntry(entry(titleId = 1))

        repository.deleteDiaryEntry(id)

        assertEquals(null, repository.getDiaryEntry(id))
        assertTrue(repository.getDiaryEntries().first().isEmpty())
    }

    @Test
    fun `getStats computes totals from entries`() = runTest {
        repository.saveDiaryEntry(entry(titleId = 1, runtimeMinutes = 120))
        repository.saveDiaryEntry(entry(titleId = 2, runtimeMinutes = null))

        val stats = repository.getStats().first()

        assertEquals(2, stats.totalTitles)
        assertEquals(2, stats.entryCount)
        assertEquals(120L, stats.totalMinutes)
        assertEquals(2L, stats.totalHours)
    }
}
