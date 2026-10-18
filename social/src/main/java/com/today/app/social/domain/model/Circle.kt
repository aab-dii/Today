package com.today.app.social.domain.model

data class Circle(
    val id: String,
    val name: String,
    val ownerUid: String,
    val ownerName: String,
    val inviteCode: String,
    val memberCount: Int,
    val createdAt: Long
)

data class CircleMember(
    val uid: String,
    val name: String,
    val joinedAt: Long
)
