package com.boji.backend.dto

data class PdfPreviewDTO(
    val id: Long,
    val customId: String,
    val title: String,
    val category1: String?,
    val category2: String?,
    val householdId: Long?,
    val location: String?,
    val year: String?,
    val price: Double
)