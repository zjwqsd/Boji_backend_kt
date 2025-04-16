package com.boji.backend.dto

import jakarta.validation.constraints.NotBlank

data class GrantPermissionRequest(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: Long,
    @field:NotBlank(message = "PDF ID不能为空")
    val pdfId: Long,
    val expiresAt: String? = null // ISO 格式时间字符串
)
