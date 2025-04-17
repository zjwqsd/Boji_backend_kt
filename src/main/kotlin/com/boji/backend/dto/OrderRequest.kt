package com.boji.backend.dto

import com.boji.backend.model.OrderStatus
import com.boji.backend.model.OrderType
import java.time.LocalDateTime

data class CreateOrderRequest(
    val userId: Long,
    val amount: Long,          // 单位：分
    val type: OrderType,       // PDF_ITEM 或 CATEGORY
    val targetId: Long         // PdfItem.id 或 Category.id
)

data class OrderStatusResponse(
    val status: OrderStatus,
    val paidAt: LocalDateTime?
)
