package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "households")
data class Household(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var code: String = "",

    @Column(nullable = true)
    var description: String? = null,

    @Column(nullable = false)
    var category2: String = "",

    @OneToMany(mappedBy = "household", fetch = FetchType.LAZY)
    val pdfItems: List<PdfItem> = emptyList()  // 可选反向关系
)