package com.boji.backend.controller


import com.boji.backend.dto.OrderItemRequest
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
    fun createOrder(
        @RequestParam userId: Long,
        @RequestBody items: List<OrderItemRequest>
    ): ResponseEntity<ApiResponse<Long>> {
        val order = orderService.createOrder(userId, items)
        return ResponseEntity.ok(ApiResponse("订单创建成功", order.id))
    }

    @PostMapping("/{id}/pay")
    fun payOrder(@PathVariable id: Long): ResponseEntity<ApiResponse<Any>> {
        paymentService.fakePay(id)  // 假支付逻辑
        return ResponseEntity.ok(ApiResponse("支付成功并已授权"))
    }

    @PostMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<ApiResponse<Any>> {
        orderService.cancelOrder(id)
        return ResponseEntity.ok(ApiResponse("订单已取消"))
    }

    @GetMapping("/user")
    fun getUserOrders(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Order>>> {
        val orders = orderService.getUserOrders(userId)
        return ResponseEntity.ok(ApiResponse("查询成功", orders))
    }
}
