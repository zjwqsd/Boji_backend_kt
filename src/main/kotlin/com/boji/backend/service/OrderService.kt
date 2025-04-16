package com.boji.backend.service

import com.boji.backend.dto.OrderItemRequest
import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.*
import com.boji.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepo: OrderRepository,
    private val userRepo: UserRepository,
    private val pdfItemRepo: PdfItemRepository,
    private val pdfCategoryControlRepo: PdfCategoryControlRepository,
    private val userPdfPermissionService: UserPdfPermissionService
) {

    fun createOrder(userId: Long, items: List<OrderItemRequest>): Order {
        val user = userRepo.findById(userId).orElseThrow {
            GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }

        val order = Order().apply {
            this.user = user
            this.status = OrderStatus.PENDING
            this.createdAt = LocalDateTime.now()
        }

        val purchaseItems = items.map {
            val price = calculatePrice(it.type, it.durationDays)
            PurchaseItem().apply {
                this.order = order
                this.type = it.type
                this.targetId = it.targetId
                this.durationDays = it.durationDays
                this.price = price
            }
        }

        order.items.addAll(purchaseItems)
        order.totalPrice = purchaseItems.sumOf { it.price }

        return orderRepo.save(order)
    }

    @Transactional
    fun processOrder(orderId: Long) {
        val order = orderRepo.findById(orderId).orElseThrow {
            GlobalExceptionHandler.BusinessException("订单不存在（orderId=$orderId）")
        }

        if (order.status != OrderStatus.PAID) {
            throw GlobalExceptionHandler.BusinessException("订单未支付，无法授权")
        }

        val user = order.user
        val now = LocalDateTime.now()

        for (item in order.items) {
            val expiresAt = now.plusDays(item.durationDays.toLong())

            when (item.type) {
                PurchaseType.PDF -> {
                    val pdf = pdfItemRepo.findById(item.targetId).orElseThrow {
                        GlobalExceptionHandler.BusinessException("PDF 不存在（id=${item.targetId}）")
                    }
                    userPdfPermissionService.grantPdfToUser(user, pdf, expiresAt)
                }

                PurchaseType.CATEGORY -> {
                    val category = pdfCategoryControlRepo.findById(item.targetId).orElseThrow {
                        GlobalExceptionHandler.BusinessException("子库不存在（id=${item.targetId}）")
                    }
                    userPdfPermissionService.grantCategoryToUser(user, category.name, expiresAt)
                }
            }
        }
    }

    fun getUserOrders(userId: Long): List<Order> {
        val user = userRepo.findById(userId).orElseThrow {
            GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }
        return orderRepo.findAll().filter { it.user.id == user.id }
    }

    fun cancelOrder(orderId: Long) {
        val order = orderRepo.findById(orderId).orElseThrow {
            GlobalExceptionHandler.BusinessException("订单不存在（orderId=$orderId）")
        }
        if (order.status != OrderStatus.PENDING) {
            throw GlobalExceptionHandler.BusinessException("仅支持取消待支付订单")
        }
        order.status = OrderStatus.CANCELED
        orderRepo.save(order)
    }

    private fun calculatePrice(type: PurchaseType, durationDays: Int): Double {
        // 简单示例：定价逻辑
        return when (type) {
            PurchaseType.PDF -> 1.0 * durationDays / 30
            PurchaseType.CATEGORY -> 5.0 * durationDays / 30
        }
    }
}


