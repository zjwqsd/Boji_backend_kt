package com.boji.backend.service

import com.boji.backend.model.CategoryPair
import com.boji.backend.repository.CategoryPairRepository
import org.springframework.stereotype.Service

@Service
class CategoryPairService(
    private val categoryPairRepository: CategoryPairRepository
) {

    fun addCategoryPair(category1: String, category2: String): CategoryPair {
        if (categoryPairRepository.existsByCategory1AndCategory2(category1, category2)) {
            throw IllegalArgumentException("该分类组合已存在")
        }
        return categoryPairRepository.save(CategoryPair(category1 = category1, category2 = category2))
    }

    fun deleteCategoryPair(id: Long) {
        categoryPairRepository.deleteById(id)
    }

    fun getAll(): List<CategoryPair> = categoryPairRepository.findAll()

    fun getByCategory1(category1: String): List<String> =
        categoryPairRepository.findAllByCategory1(category1).map { it.category2 }
}
