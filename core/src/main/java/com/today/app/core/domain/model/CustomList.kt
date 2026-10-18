package com.today.app.core.domain.model

data class CustomList(
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAt: Long,
    val itemCount: Int = 0
)

data class CustomListItem(
    val titleId: Int,
    val mediaType: MediaType,
    val titleName: String,
    val posterPath: String?
)
