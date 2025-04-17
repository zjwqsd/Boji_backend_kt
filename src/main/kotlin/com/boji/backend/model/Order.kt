package com.boji.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class OrderType {
    PDF_ITEM, CATEGORY
}

enum class OrderStatus {
    PENDING, PAID, CANCELLED, FAILED
}

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long = 0,

    val amount: Long = 0, // 单位：分

    val description: String = "",

    @Enumerated(EnumType.STRING)
    val type: OrderType = OrderType.PDF_ITEM,

    val targetId: Long = 0, // 与数据库主键绑定

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var paidAt: LocalDateTime? = null
) {
    // JPA 无参构造函数
    protected constructor() : this(0, 0, 0, "", OrderType.PDF_ITEM, 0)
}

