package com.today.app.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @field:SerializedName("results")
    val results: List<VideoResult> = emptyList()
)

data class VideoResult(
    @field:SerializedName("key")
    val key: String? = null,

    @field:SerializedName("site")
    val site: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("official")
    val official: Boolean? = null
)
