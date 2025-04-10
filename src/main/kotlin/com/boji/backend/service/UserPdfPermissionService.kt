package com.boji.backend.service

import com.boji.backend.model.User
import com.boji.backend.model.PdfItem
import com.boji.backend.model.UserPdfPermission
import com.boji.backend.repository.UserPdfPermissionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SuperUserPermissionService(
    private val userPdfPermissionRepo: UserPdfPermissionRepository
) {

    /**
     * 授权某个 PDF 给普通用户（仅允许主用户）
     */
    fun grantPdfToUser(user: User, pdf: PdfItem, expiresAt: LocalDateTime? = null): UserPdfPermission {
        if (user.isSubUser) {
            throw IllegalArgumentException("不能给附属用户授权，请授权其主账户")
        }

        if (userPdfPermissionRepo.existsByUserAndPdfItem(user, pdf)) {
            throw IllegalArgumentException("该用户已经拥有此 PDF 权限")
        }

        return userPdfPermissionRepo.save(UserPdfPermission(user, pdf, LocalDateTime.now(), expiresAt))
    }

    /**
     * 获取用户所有可访问的 PDF（包括附属用户继承）
     */
    fun getAccessiblePdfs(user: User): List<PdfItem> {
        val effectiveUser = if (user.isSubUser) user.parentUser ?: error("附属用户未绑定主用户") else user
        return userPdfPermissionRepo.findByUser(effectiveUser).map { it.pdfItem }
    }

    /**
     * 查询 PDF 被授权给了哪些主用户（不含附属用户）
     */
    fun getPdfAuthorizedUsers(pdf: PdfItem): List<User> {
        return userPdfPermissionRepo
            .findAll()
            .filter { it.pdfItem.id == pdf.id && !it.user.isSubUser }
            .map { it.user }
    }
}

