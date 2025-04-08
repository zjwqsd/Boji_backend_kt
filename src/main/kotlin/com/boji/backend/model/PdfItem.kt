package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "pdf_items")
data class PdfItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val customId: String = "",

    @Column(nullable = false)
    val title: String = "",

    val category1: String = "",
    val category2: String = "",
    val location: String = "",
    val description: String = "",
    val shape: String = "",
    val year: String = "",
    val price: Double = 0.0,

    @Column(nullable = false)
    val pdfPath: String = "",

    val coverPath: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    val household: Household? = null
)