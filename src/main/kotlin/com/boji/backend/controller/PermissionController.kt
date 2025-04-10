package com.boji.backend.controller

import com.boji.backend.model.PdfItem
import com.boji.backend.model.User
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.repository.UserRepository
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.AdminOnly
import com.boji.backend.service.SuperUserPermissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/admin/permissions")
class PermissionController(
    private val userRepo: UserRepository,
    private val pdfRepo: PdfItemRepository,
    private val superUserPermissionService: SuperUserPermissionService
) {

    @PostMapping("/grant")
    @AdminOnly
    fun grantPermission(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam userId: Long,
        @RequestParam pdfId: Long,
        @RequestParam(required = false) expiresAt: String? // ISO 字符串
    ): ResponseEntity<ApiResponse<Any>> {
        val user = userRepo.findById(userId).orElseThrow()
        val pdf = pdfRepo.findById(pdfId).orElseThrow()
        val expireDate = expiresAt?.let { LocalDateTime.parse(it) }

        superUserPermissionService.grantPdfToUser(user, pdf, expireDate)
        return ResponseEntity.ok(ApiResponse("授权成功"))
    }

    @GetMapping("/user/{userId}/pdfs")
    @AdminOnly
    fun getUserPdfs(@RequestHeader("Authorization") authHeader: String?,
                    @PathVariable userId: Long): ResponseEntity<ApiResponse< List<PdfItem>>> {
        val user = userRepo.findById(userId).orElseThrow()
        return ResponseEntity.ok(
            ApiResponse("授权成功",
                superUserPermissionService.getAccessiblePdfs(user)))
    }


    @GetMapping("/pdf/{pdfId}/users")
    @AdminOnly
    fun getPdfUsers(@RequestHeader("Authorization") authHeader: String?,
                    @PathVariable pdfId: Long):ResponseEntity<ApiResponse< List<User>>> {
        val pdf = pdfRepo.findById(pdfId).orElseThrow()
        return ResponseEntity.ok(
            ApiResponse("授权成功",
                superUserPermissionService.getPdfAuthorizedUsers(pdf)))

    }
}
