package com.boji.backend.service

import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.OrderStatus
import com.boji.backend.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val orderRepo: OrderRepository,
    private val orderService: OrderService
) {

    @Transactional
    fun fakePay(orderId: Long) {
        val order = orderRepo.findById(orderId).orElseThrow {
            GlobalExceptionHandler.BusinessException("订单不存在（orderId=$orderId）")
        }

        if (order.status != OrderStatus.PENDING) {
            throw GlobalExceptionHandler.BusinessException("订单非待支付状态，无法支付")
        }

        // 模拟设置为“已支付”
        order.status = OrderStatus.PAID
        orderRepo.save(order)

        // 完成授权
        orderService.processOrder(orderId)
    }
}
