package com.boji.backend.controller


import com.boji.backend.dto.CreateOrderRequest
import com.boji.backend.dto.OrderItemRequest
import com.boji.backend.dto.OrderStatusResponse
import com.boji.backend.model.Order
import com.boji.backend.model.User
//import com.boji.backend.model.OrderItemRequest
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.AdminOnly
import com.boji.backend.security.annotation.CurrentUser
import com.boji.backend.security.annotation.RoleAllowed

import com.boji.backend.service.PaymentService
import com.boji.backend.service.OrderService
import org.springframework.http.HttpStatus
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

    @GetMapping("/all")
    @AdminOnly
    fun getAllOrders(): ResponseEntity<ApiResponse<List<Order>>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(ApiResponse("获取所有订单成功", orders))
    }

    @GetMapping("/my")
    @RoleAllowed("user")
    fun getMyOrders(
        @CurrentUser(role = "user") user: User?
    ): ResponseEntity<ApiResponse<List<Order>>> {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse("无法获取当前用户信息"))
        }

        val orders = orderService.getOrdersByUserId(user.id)
        return ResponseEntity.ok(ApiResponse("获取当前用户订单成功", orders))
    }



}

