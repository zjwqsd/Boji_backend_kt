package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "households")
data class Household(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String = "",

    @Column(nullable = false, unique = true)
    val code: String = "",

    @Column(nullable = true)
    val description: String? = null,

    @Column(nullable = false)
    val category2: String = "",

    @OneToMany(mappedBy = "household", fetch = FetchType.LAZY)
    val pdfItems: List<PdfItem> = emptyList()  // 可选反向关系
)