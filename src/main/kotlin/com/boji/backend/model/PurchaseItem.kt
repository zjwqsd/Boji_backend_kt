package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "purchase_items")
class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    lateinit var order: Order

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: PurchaseType // PDF 或 Category

    @Column(name = "target_id", nullable = false)
    var targetId: Long = 0 // pdfId 或者 categoryName 的哈希值/主键

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int = 30 // 有效天数

    @Column(name = "price", nullable = false)
    var price: Double = 0.0
}

enum class PurchaseType {
    PDF, CATEGORY
}
