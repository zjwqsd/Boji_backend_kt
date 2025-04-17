package com.boji.backend.service

import com.boji.backend.dto.CreateOrderRequest
import com.boji.backend.dto.OrderStatusResponse
import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.Order
import com.boji.backend.model.OrderStatus
import com.boji.backend.repository.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    @Transactional
    fun createOrder(request: CreateOrderRequest): Long {
        // 你可以在这里校验 targetId 是否存在，例如 PdfItem / Category

        val order = Order(
            userId = request.userId,
            amount = request.amount,
            type = request.type,
            targetId = request.targetId,
            description = generateDescription(request),
            status = OrderStatus.PENDING,
            paidAt = null
        )
        return orderRepository.save(order).id
    }

    fun getOrderStatus(orderId: Long): OrderStatusResponse {
        val order = orderRepository.findById(orderId).orElseThrow {
            GlobalExceptionHandler.BusinessException("订单不存在，ID = $orderId")
        }
        return OrderStatusResponse(
            status = order.status,
            paidAt = order.paidAt
        )
    }

    @Transactional
    fun cancelOrder(orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow {
            GlobalExceptionHandler.BusinessException("订单不存在，ID = $orderId")
        }
        if (order.status == OrderStatus.PAID) {
            throw GlobalExceptionHandler.BusinessException("订单已支付，无法取消")
        }
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
    }

    private fun generateDescription(request: CreateOrderRequest): String {
        return when (request.type) {
            com.boji.backend.model.OrderType.PDF_ITEM ->
                "购买 PDF（ID=${request.targetId}）"
            com.boji.backend.model.OrderType.CATEGORY ->
                "购买子库权限（分类ID=${request.targetId}）"
        }
    }
}
