package com.boji.backend.service

import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.OrderStatus
import com.boji.backend.model.OrderType
import com.boji.backend.repository.OrderRepository
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.repository.UserRepository
import com.boji.backend.service.UserPdfPermissionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentService(
    private val orderRepo: OrderRepository,
    private val userRepo: UserRepository,
    private val userPermissionService: UserPdfPermissionService,
    private val wechatClient: WechatPayClient,
    private val pdfItemRepo: PdfItemRepository,
) {

    fun createWechatPayParams(orderId: Long): Map<String, String> {
        val order = orderRepo.findById(orderId).orElseThrow { Exception("订单不存在") }

        return wechatClient.buildJsapiParams(order)
    }

    fun handleWechatNotify(body: String, headers: Map<String, String>): Boolean {
        if (!wechatClient.verifySignature(body, headers)) return false

        val orderId = wechatClient.decryptOrderId(body) ?: return false
        val order = orderRepo.findById(orderId).orElseThrow { GlobalExceptionHandler.BusinessException("订单不存在") }

        if (order.status == OrderStatus.PAID) return true

        // ✅ 授权处理
        val user = userRepo.findById(order.userId).orElseThrow()
        when (order.type) {
            OrderType.PDF_ITEM -> {
                val pdf = pdfItemRepo.findById(order.targetId).orElseThrow {
                    Exception("PDF不存在，id=${order.targetId}")
                }
                userPermissionService.grantPdfToUser(user, pdf)
            }
            OrderType.CATEGORY -> {
                val expiresAt = LocalDateTime.now().plusMonths(6)
                userPermissionService.grantCategoryToUser(user, order.targetId.toString(), expiresAt)
            }
        }

        order.status = OrderStatus.PAID
        order.paidAt = LocalDateTime.now()
        orderRepo.save(order)
        return true
    }
}

