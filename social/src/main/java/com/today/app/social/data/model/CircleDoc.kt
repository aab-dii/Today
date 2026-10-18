package com.today.app.social.data.model

data class CircleDoc(
    var name: String = "",
    var ownerUid: String = "",
    var ownerName: String = "",
    var inviteCode: String = "",
    var memberUids: List<String> = emptyList(),
    var memberCount: Int = 0,
    var createdAt: Long = 0L
)

data class CircleMemberDoc(
    var name: String = "",
    var joinedAt: Long = 0L
)
