package com.boji.backend.repository


import com.boji.backend.model.Order
import org.springframework.data.jpa.repository.JpaRepository


interface OrderRepository : JpaRepository<Order, Long> {
    fun findByUserId(userId: Long): List<Order>
}

