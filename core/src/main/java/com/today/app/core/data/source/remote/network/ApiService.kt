package com.today.app.core.data.source.remote.network

import com.today.app.core.data.source.remote.response.TitleDetailResponse
import com.today.app.core.data.source.remote.response.TitleResponse
import com.today.app.core.data.source.remote.response.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("discover/movie")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("with_origin_country") originCountry: String? = null,
        @Query("with_genres") genres: String? = null,
        @Query("sort_by") sortBy: String? = null
    ): TitleResponse

    @GET("discover/tv")
    suspend fun getTvShows(
        @Query("page") page: Int = 1,
        @Query("with_origin_country") originCountry: String? = null,
        @Query("with_genres") genres: String? = null,
        @Query("sort_by") sortBy: String? = null
    ): TitleResponse

    @GET("trending/all/week")
    suspend fun getTrending(
        @Query("page") page: Int = 1
    ): TitleResponse

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("region") region: String? = null,
        @Query("page") page: Int = 1
    ): TitleResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): TitleResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1
    ): TitleResponse

    @GET("search/multi")
    suspend fun searchTitles(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TitleResponse

    @GET("movie/{id}")
    suspend fun getMovieDetail(
        @Path("id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,recommendations"
    ): TitleDetailResponse

    @GET("tv/{id}")
    suspend fun getTvDetail(
        @Path("id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,recommendations"
    ): TitleDetailResponse

    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") id: Int
    ): VideoResponse

    @GET("tv/{id}/videos")
    suspend fun getTvVideos(
        @Path("id") id: Int
    ): VideoResponse
}
