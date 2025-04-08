package com.boji.backend.dto

import jakarta.validation.constraints.NotBlank

data class AssignSubUserRequest(
    @field:NotBlank(message = "父用户 ID 不能为空")
    val parentUserId: String = ""
)