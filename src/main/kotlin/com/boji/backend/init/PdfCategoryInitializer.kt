package com.boji.backend.init


import com.boji.backend.model.PdfCategoryControl
import com.boji.backend.repository.PdfCategoryControlRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class PdfCategoryInitializer(
    private val pdfCategoryControlRepo: PdfCategoryControlRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val categories = listOf("散叶", "另册", "归户")

        categories.forEach { name ->
            if (pdfCategoryControlRepo.findByName(name) == null) {
                val category = PdfCategoryControl(name = name, isOpen = true)
                pdfCategoryControlRepo.save(category)
                println("✅ 已初始化子库：$name")
            } else {
                println("ℹ️ 子库已存在：$name")
            }
        }
    }
}
