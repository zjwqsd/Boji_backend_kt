package com.boji.backend.controller

import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.Admin
import com.boji.backend.model.User
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.UserOnly
import com.boji.backend.security.annotation.CurrentUser
import com.boji.backend.security.annotation.RoleAllowed
import com.boji.backend.service.UserPdfPermissionService
import jakarta.servlet.http.HttpServletRequest
import net.coobird.thumbnailator.Thumbnails
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Role
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.io.File
import java.nio.file.Paths

@RestController
@RequestMapping("/api/pdf")
class PdfPreviewController(
    @Value("\${file.local.base-path}") private val basePath: String,
    val pdfItemRepository: PdfItemRepository,
    private val userPdfPermissionService: UserPdfPermissionService,

) {

    @GetMapping("/preview/{id}")
    @RoleAllowed("user","admin")
    fun previewPdf(
        @CurrentUser(role = "user") user: User?,  // 如果是 user 就注入
        @CurrentUser(role = "admin") admin: Admin?,  // 如果是 admin 就注入
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable id: Long, response: HttpServletResponse,
        request: HttpServletRequest,
    ) {

        val role = request.getAttribute("role") as? String
            ?: throw GlobalExceptionHandler.BusinessException("缺少身份信息")

        val item = pdfItemRepository.findById(id).orElse(null)
            ?: throw GlobalExceptionHandler.BusinessException("PDF 不存在")

        // 权限控制逻辑
        when (role) {
            "admin" -> {
                // 管理员直接通过，无需额外权限判断
            }
            "user" -> {
                if (user == null) {
                    throw GlobalExceptionHandler.BusinessException("无法获取当前用户信息")
                }
                val hasAccess = userPdfPermissionService.hasAccessToPdf(user, item)
                if (!hasAccess) {
                    throw GlobalExceptionHandler.BusinessException("你无权访问该 PDF")
                }
            }
            else -> {
                throw GlobalExceptionHandler.BusinessException("未知角色：$role")
            }
        }



        val pdfItem = pdfItemRepository.findById(id)
            .orElseThrow { GlobalExceptionHandler.NotFoundException("PDF 不存在或已被删除") }

//        val pdfFile = File(pdfItem.pdfPath)
        val pdfFile = File(Paths.get(basePath, pdfItem.pdfPath).toString())
        if (!pdfFile.exists()) {
            throw GlobalExceptionHandler.NotFoundException("PDF 不存在或者文件损坏")
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
            .orElseThrow {  GlobalExceptionHandler.NotFoundException("PDF 不存在或已被删除") }

        val coverPath = pdfItem.coverPath
        if (coverPath.isNullOrBlank()) {
            throw GlobalExceptionHandler.NotFoundException("该条目未设置封面")
        }

//        val imageFile = File(coverPath)
        val imageFile = File(Paths.get(basePath, pdfItem.coverPath).toString())
        if (!imageFile.exists()) {
            throw GlobalExceptionHandler.NotFoundException("封面不存在或者已损坏")
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
