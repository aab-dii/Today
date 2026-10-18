package com.today.app.core.data.source.remote

import com.today.app.core.data.source.remote.network.ApiService
import com.today.app.core.data.source.remote.response.TitleDetailResponse
import com.today.app.core.data.source.remote.response.TitleResult
import com.today.app.core.domain.model.CatalogRow
import com.today.app.core.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDataSource(private val apiService: ApiService) {

    suspend fun getDiscoveryPage(page: Int = 1): List<TitleResult> =
        withContext(Dispatchers.IO) {
            val movies = apiService.getMovies(page = page).results.map {
                it.copy(mediaType = MediaType.MOVIE.value)
            }
            val tvShows = apiService.getTvShows(page = page).results.map {
                it.copy(mediaType = MediaType.TV.value)
            }

            (movies + tvShows).sortedByDescending { it.popularity ?: 0.0 }
        }

    suspend fun getCatalogRow(row: CatalogRow, page: Int = 1): List<TitleResult> =
        withContext(Dispatchers.IO) {
            when (row) {
                CatalogRow.TRENDING ->
                    apiService.getTrending(page = page).results.filter {
                        it.mediaType == MediaType.MOVIE.value ||
                            it.mediaType == MediaType.TV.value
                    }

                CatalogRow.KOREAN_DRAMA ->
                    apiService.getTvShows(
                        page = page,
                        originCountry = "KR",
                        sortBy = "popularity.desc"
                    ).results.map { it.copy(mediaType = MediaType.TV.value) }

                CatalogRow.ANIME ->
                    apiService.getTvShows(
                        page = page,
                        originCountry = "JP",
                        genres = "16"
                    ).results.map { it.copy(mediaType = MediaType.TV.value) }

                CatalogRow.INDONESIAN_MOVIE ->
                    apiService.getMovies(
                        page = page,
                        originCountry = "ID"
                    ).results.map { it.copy(mediaType = MediaType.MOVIE.value) }

                CatalogRow.NOW_PLAYING ->
                    apiService.getNowPlaying(
                        region = "ID",
                        page = page
                    ).results.map { it.copy(mediaType = MediaType.MOVIE.value) }

                CatalogRow.TOP_RATED -> {
                    val movies = apiService.getTopRatedMovies(page = page).results.map {
                        it.copy(mediaType = MediaType.MOVIE.value)
                    }
                    val tvShows = apiService.getTopRatedTvShows(page = page).results.map {
                        it.copy(mediaType = MediaType.TV.value)
                    }
                    (movies + tvShows).sortedByDescending { it.voteAverage ?: 0.0 }
                }
            }
        }

    suspend fun searchTitles(query: String, page: Int = 1): List<TitleResult> =
        withContext(Dispatchers.IO) {
            apiService.searchTitles(query = query, page = page).results.filter {
                it.id != null &&
                    it.mediaType != "person" &&
                    !it.posterPath.isNullOrBlank()
            }
        }

    suspend fun getTitleDetail(mediaType: MediaType, id: Int): TitleDetailResponse =
        withContext(Dispatchers.IO) {
            when (mediaType) {
                MediaType.MOVIE -> apiService.getMovieDetail(id = id)
                MediaType.TV -> apiService.getTvDetail(id = id)
            }
        }

    suspend fun getTrailerKey(mediaType: MediaType, id: Int): String? =
        withContext(Dispatchers.IO) {
            val videos = try {
                when (mediaType) {
                    MediaType.MOVIE -> apiService.getMovieVideos(id = id)
                    MediaType.TV -> apiService.getTvVideos(id = id)
                }.results
            } catch (e: Exception) {
                return@withContext null
            }

            val candidates = videos.filter {
                it.site.equals("YouTube", ignoreCase = true) && !it.key.isNullOrBlank()
            }
            val trailer = candidates.firstOrNull {
                it.type.equals("Trailer", ignoreCase = true) && it.official == true
            } ?: candidates.firstOrNull {
                it.type.equals("Trailer", ignoreCase = true)
            } ?: candidates.firstOrNull()

            trailer?.key
        }
}
