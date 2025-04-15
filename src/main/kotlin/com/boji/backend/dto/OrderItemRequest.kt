package com.boji.backend.dto

import com.boji.backend.model.PurchaseType
import jakarta.validation.constraints.*

data class OrderItemRequest(
    @field:NotBlank(message = "购买类型不存在")
    val type: PurchaseType,

    @field:NotBlank(message = "购买目标不能为空")
    val targetId: Long,

    @field:NotBlank(message = "购买时长不能为空")
    val durationDays: Int
)