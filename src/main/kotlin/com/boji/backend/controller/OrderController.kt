package com.boji.backend.controller


import com.boji.backend.dto.CreateOrderRequest
import com.boji.backend.dto.OrderItemRequest
import com.boji.backend.dto.OrderStatusResponse
import com.boji.backend.model.Order
//import com.boji.backend.model.OrderItemRequest
import com.boji.backend.response.ApiResponse
import com.boji.backend.service.PaymentService
import com.boji.backend.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val paymentService: PaymentService
) {

    @PostMapping("/create")
    fun createOrder(@RequestBody request: CreateOrderRequest): ResponseEntity<ApiResponse<Long>> {
        val orderId = orderService.createOrder(request)
        return ResponseEntity.ok(ApiResponse("订单创建成功", orderId))
    }

    @PostMapping("/{id}/prepare-payment")
    fun preparePayment(@PathVariable id: Long): ResponseEntity<ApiResponse<Map<String, String>>> {
        val params = paymentService.createWechatPayParams(id)
        return ResponseEntity.ok(ApiResponse("支付参数生成成功", params))
    }

    @PostMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<ApiResponse<Any>> {
        orderService.cancelOrder(id)
        return ResponseEntity.ok(ApiResponse("订单已取消"))
    }

    @GetMapping("/{id}/status")
    fun getOrderStatus(@PathVariable id: Long): ResponseEntity<ApiResponse<OrderStatusResponse>> {
        val status = orderService.getOrderStatus(id)
        return ResponseEntity.ok(ApiResponse("查询成功", status))
    }
}

