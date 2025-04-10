package com.boji.backend.controller

import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.PdfItem
import com.boji.backend.model.User
import com.boji.backend.repository.PdfCategoryControlRepository
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.repository.UserRepository
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.AdminOnly
import com.boji.backend.service.SuperUserPermissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/admin/permissions")
class PermissionController(
    private val userRepo: UserRepository,
    private val pdfRepo: PdfItemRepository,
    private val superUserPermissionService: SuperUserPermissionService,
    private val pdfCategoryControlRepo: PdfCategoryControlRepository,
) {

    @PostMapping("/grant")
    @AdminOnly
    fun grantPermission(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam userId: Long,
        @RequestParam pdfId: Long,
        @RequestParam(required = false) expiresAt: String? // ISO 字符串
    ): ResponseEntity<ApiResponse<Any>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }

        val pdf = pdfRepo.findById(pdfId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("PDF 不存在（pdfId=$pdfId）")
        }
//        val expireDate = expiresAt?.let { LocalDateTime.parse(it) }

        val expireDate = try {
            expiresAt?.let { LocalDateTime.parse(it) }
        } catch (e: DateTimeParseException) {
            throw GlobalExceptionHandler.BusinessException("参数 expiresAt 格式不正确，预期格式为 yyyy-MM-dd'T'HH:mm:ss，例如 2025-04-15T15:00:00")
        }
        superUserPermissionService.grantPdfToUser(user, pdf, expireDate)
        return ResponseEntity.ok(ApiResponse("授权成功"))
    }

    @DeleteMapping("/revoke")
    @AdminOnly
    fun revokePermission(
        @RequestParam userId: Long,
        @RequestParam pdfId: Long
    ): ResponseEntity<ApiResponse<Any>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }

        val pdf = pdfRepo.findById(pdfId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("PDF 不存在（pdfId=$pdfId）")
        }
        superUserPermissionService.revokePdfFromUser(user, pdf)
        return ResponseEntity.ok(ApiResponse("权限已移除"))
    }

    @GetMapping("/pdf/authorized-users")
    @AdminOnly
    fun getPdfAuthorizedUsers(@RequestParam pdfId: Long): ResponseEntity<ApiResponse<List<Long>>> {
//        val pdf = pdfRepo.findById(pdfId).orElseThrow()
        val pdf = pdfRepo.findById(pdfId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("PDF 不存在（pdfId=$pdfId）")
        }
        val users = superUserPermissionService.getPdfAuthorizedUsers(pdf)
        val ids = users.map { it.id }
        return ResponseEntity.ok(ApiResponse("获取成功", ids))
    }


    @GetMapping("/user/accessible-pdfs")
    @AdminOnly
    fun getUserPdfs(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Long>>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }
        val pdfs = superUserPermissionService.getAccessiblePdfs(user)
//        return ResponseEntity.ok(ApiResponse("获取成功", pdfs))
        val ids = pdfs.map { it.id }
        return ResponseEntity.ok(ApiResponse("获取成功", ids))
    }


    @PostMapping("/grant-category")
    @AdminOnly
    fun grantCategoryPermission(
        @RequestParam userId: Long,
        @RequestParam categoryName: String,
        @RequestParam(required = false) expiresAt: String?
    ): ResponseEntity<ApiResponse<Any>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }
        val expireDate = expiresAt?.let { LocalDateTime.parse(it) }

        superUserPermissionService.grantCategoryToUser(user, categoryName, expireDate)
        return ResponseEntity.ok(ApiResponse("子库权限授权成功"))
    }

    @DeleteMapping("/revoke-category")
    @AdminOnly
    fun revokeCategoryPermission(
        @RequestParam userId: Long,
        @RequestParam categoryName: String
    ): ResponseEntity<ApiResponse<Any>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }
        superUserPermissionService.revokeCategoryFromUser(user, categoryName)
        return ResponseEntity.ok(ApiResponse("子库权限已移除"))
    }

    @GetMapping("/check/pdf")
    @AdminOnly
    fun checkUserPdfPermission(
        @RequestParam userId: Long,
        @RequestParam pdfId: Long
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }

        val pdf = pdfRepo.findById(pdfId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("PDF 不存在（pdfId=$pdfId）")
        }
        val hasAccess = superUserPermissionService.hasAccessToPdf(user, pdf)
        return ResponseEntity.ok(ApiResponse("查询成功", hasAccess))
    }

    @GetMapping("/check/category")
    @AdminOnly
    fun checkUserCategoryPermission(
        @RequestParam userId: Long,
        @RequestParam categoryName: String
    ): ResponseEntity<ApiResponse<Boolean>> {
        val user = userRepo.findById(userId).orElseThrow {
            throw GlobalExceptionHandler.BusinessException("用户不存在（userId=$userId）")
        }
        val hasAccess = superUserPermissionService.hasAccessToCategory(user, categoryName)
        return ResponseEntity.ok(ApiResponse("查询成功", hasAccess))
    }


    @PostMapping("/categories/open")
    @AdminOnly
    fun openCategory(@RequestParam categoryName: String): ResponseEntity<ApiResponse<Any>> {
        val category = pdfCategoryControlRepo.findByName(categoryName)
            ?: throw GlobalExceptionHandler.BusinessException("子库 $categoryName 不存在")
        category.isOpen = true
        pdfCategoryControlRepo.save(category)
        return ResponseEntity.ok(ApiResponse("子库已开启"))
    }

    @PostMapping("/categories/close")
    @AdminOnly
    fun closeCategory(@RequestParam categoryName: String): ResponseEntity<ApiResponse<Any>> {
        val category = pdfCategoryControlRepo.findByName(categoryName)
            ?: throw GlobalExceptionHandler.BusinessException("子库 $categoryName 不存在")
        category.isOpen = false
        pdfCategoryControlRepo.save(category)
        return ResponseEntity.ok(ApiResponse("子库已关闭"))
    }
}
