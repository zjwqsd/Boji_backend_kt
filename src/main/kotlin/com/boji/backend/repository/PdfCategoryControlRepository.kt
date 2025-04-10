package com.boji.backend.repository

import com.boji.backend.model.PdfCategoryControl
import org.springframework.data.jpa.repository.JpaRepository

interface PdfCategoryControlRepository : JpaRepository<PdfCategoryControl, Long> {
    fun findByName(name: String): PdfCategoryControl?
}
