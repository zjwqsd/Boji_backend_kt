package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "pdf_category_controls")
class PdfCategoryControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(unique = true, nullable = false)
    lateinit var name: String  // 与 PdfItem.category1 一致

    @Column(name = "is_open", nullable = false)
    var isOpen: Boolean = true

    var price: Double = 0.0

    // 无参构造函数（JPA 要求）
    constructor()

    // 可选：带参构造函数，方便使用
    constructor(name: String, isOpen: Boolean = true) {
        this.name = name
        this.isOpen = isOpen
    }
}

