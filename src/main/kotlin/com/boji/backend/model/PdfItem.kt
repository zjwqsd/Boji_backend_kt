package com.boji.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "pdf_items")
data class PdfItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var customId: String = "",

    @Column(nullable = false)
    var title: String = "",

    var category1: String = "",
    var category2: String = "",
    var location: String = "",
    var description: String = "",
    var shape: String = "",
    var year: String = "",
    var price: Double = 0.0,

    @Column(nullable = false)
    val pdfPath: String = "",

    val coverPath: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    var household: Household? = null,

    @OneToMany(mappedBy = "pdfItem")
    val userPermissions: List<UserPdfPermission> = emptyList()


)