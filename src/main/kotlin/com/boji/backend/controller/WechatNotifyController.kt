package com.boji.backend.controller

import com.boji.backend.service.PaymentService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments/notify")
class WechatNotifyController(
    private val paymentService: PaymentService
) {

    @PostMapping("/wechat")
    fun handleWechatCallback(
        @RequestBody body: String,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val headers = request.headerNames.toList().associateWith { request.getHeader(it) }
        val success = paymentService.handleWechatNotify(body, headers)

        return if (success) {
            ResponseEntity.ok(mapOf("code" to "SUCCESS", "message" to "成功"))
        } else {
            ResponseEntity.status(500).body(mapOf("code" to "FAIL", "message" to "失败"))
        }
    }
}
