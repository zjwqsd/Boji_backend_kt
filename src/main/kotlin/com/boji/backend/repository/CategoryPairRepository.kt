package com.boji.backend.repository

import com.boji.backend.model.CategoryPair
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryPairRepository : JpaRepository<CategoryPair, Long> {
    fun existsByCategory1AndCategory2(category1: String, category2: String): Boolean
    fun findAllByCategory1(category1: String): List<CategoryPair>
}
