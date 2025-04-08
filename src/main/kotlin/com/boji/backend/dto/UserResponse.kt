package com.boji.backend.dto

data class UserWithSubsResponse(
    val id: Long,
    val userId: String?,
    val nickname: String?,
    val realName: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val company: String?,
    val subUsers: List<SubUserResponse>
)

data class SubUserResponse(
    val id: Long,
    val userId: String?,     // ✅ 接受 null
    val nickname: String?,
    val realName: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val company: String?,
)