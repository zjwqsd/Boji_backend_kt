package com.boji.backend.controller

import com.boji.backend.model.CategoryPair
import com.boji.backend.service.CategoryPairService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/category-pairs")
class CategoryPairController(
    private val categoryPairService: CategoryPairService
) {

    @PostMapping
    fun addPair(@RequestParam category1: String, @RequestParam category2: String): ResponseEntity<CategoryPair> {
        val pair = categoryPairService.addCategoryPair(category1, category2)
        return ResponseEntity.ok(pair)
    }

    @DeleteMapping("/{id}")
    fun deletePair(@PathVariable id: Long): ResponseEntity<Void> {
        categoryPairService.deleteCategoryPair(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<CategoryPair>> = ResponseEntity.ok(categoryPairService.getAll())

    @GetMapping("/by-category1")
    fun getByCategory1(@RequestParam category1: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(categoryPairService.getByCategory1(category1))
    }
}
