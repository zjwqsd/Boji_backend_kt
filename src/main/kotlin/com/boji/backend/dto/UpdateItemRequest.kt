package com.boji.backend.dto

data class UpdateItemRequest(
    val title: String? = null,
    val customId: String? = null,
    val category1: String? = null,
    val category2: String? = null,
    val householdId: Long? = null,
    val location: String? = null,
    val description: String? = null,
    val shape: String? = null,
    val year: String? = null,
    val price: Double? = null
)
