package com.boji.backend.repository


import com.boji.backend.model.PdfItem
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PdfItemRepository : JpaRepository<PdfItem, Long> {

    fun findByCustomId(customId: String): PdfItem?

    fun existsByCustomId(customId: String): Boolean

    fun findAllByCategory1(category1: String): List<PdfItem>

    fun findAllByCategory1AndCategory2(category1: String, category2: String): List<PdfItem>

    fun findAllByHouseholdId(householdId: Long): List<PdfItem>

    fun existsByCustomIdAndIdNot(newCustomId: String, id: Long): Boolean

    @Query("""
    SELECT p FROM PdfItem p 
    WHERE LOWER(p.customId) LIKE :term 
       OR LOWER(p.title) LIKE :term 
       OR LOWER(p.location) LIKE :term 
       OR LOWER(p.description) LIKE :term 
       OR LOWER(p.shape) LIKE :term 
       OR LOWER(p.year) LIKE :term
""")

    fun searchByKeyword(@Param("term") term: String): List<PdfItem>
}
