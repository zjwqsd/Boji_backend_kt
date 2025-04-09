package com.boji.backend.controller

import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.security.UserOnly
import net.coobird.thumbnailator.Thumbnails
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletResponse
import java.io.File

@RestController
@RequestMapping("/api/pdf")
class PdfPreviewController(
    val pdfItemRepository: PdfItemRepository
) {

    @GetMapping("/preview/{id}")
    @UserOnly
    fun previewPdf(@RequestHeader("Authorization") authHeader: String?,
                   @PathVariable id: Long, response: HttpServletResponse) {
        val pdfItem = pdfItemRepository.findById(id)
            .orElseThrow { RuntimeException("PDF not found") }

        val pdfFile = File(pdfItem.pdfPath)

        if (!pdfFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF 文件不存在")
            return
        }

        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "inline; filename=\"preview.pdf\"")
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate")

        pdfFile.inputStream().use { input ->
            response.outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    @GetMapping("/cover/{id}")
    fun previewCover(@PathVariable id: Long, response: HttpServletResponse) {
        val pdfItem = pdfItemRepository.findById(id)
            .orElseThrow { RuntimeException("PDF not found") }

        val coverPath = pdfItem.coverPath
        if (coverPath.isNullOrBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "未设置封面图")
            return
        }

        val imageFile = File(coverPath)
        if (!imageFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "封面图不存在")
            return
        }

        response.contentType = "image/jpeg"
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate")

        // 使用 Thumbnailator 进行图片压缩输出
        Thumbnails.of(imageFile)
            .size(300, 300) // 限制宽高，不改变比例
            .outputFormat("jpg")
            .outputQuality(0.7) // 降低图片质量（0.0~1.0）
            .toOutputStream(response.outputStream)
    }
}
