package com.boji.backend.repository


import com.boji.backend.model.PdfItem
import org.springframework.data.jpa.repository.JpaRepository

interface PdfItemRepository : JpaRepository<PdfItem, Long> {

    fun findByCustomId(customId: String): PdfItem?

    fun existsByCustomId(customId: String): Boolean

    fun findAllByCategory1(category1: String): List<PdfItem>

    fun findAllByCategory1AndCategory2(category1: String, category2: String): List<PdfItem>

    fun findAllByHouseholdId(householdId: Long): List<PdfItem>
}
