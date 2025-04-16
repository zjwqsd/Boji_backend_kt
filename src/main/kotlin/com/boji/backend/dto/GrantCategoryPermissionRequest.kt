package com.boji.backend.dto

import jakarta.validation.constraints.NotBlank

data class GrantCategoryPermissionRequest(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: Long,
    @field:NotBlank(message = "库名不能为空")
    val categoryName: String,

    val expiresAt: String? = null // ISO 格式时间字符串
)