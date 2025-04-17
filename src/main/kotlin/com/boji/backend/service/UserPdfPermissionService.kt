package com.boji.backend.service

import com.boji.backend.model.*
import com.boji.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserPdfPermissionService(
    private val userPdfPermissionRepo: UserPdfPermissionRepository,
    private val userCategoryPermissionRepo: UserCategoryPermissionRepository,
    private val pdfCategoryControlRepo: PdfCategoryControlRepository,
    private val pdfItemRepo: PdfItemRepository
) {

    /**
     * 授权某个 PDF 给普通用户（仅允许主用户）
     */
    fun grantPdfToUser(user: User, pdf: PdfItem, expiresAt: LocalDateTime? = null): UserPdfPermission {
        if (user.isSubUser) {
            throw IllegalArgumentException("不能给附属用户授权，请授权其主账户")
        }

        val existingPermission = userPdfPermissionRepo.findByUserAndPdfItem(user, pdf)

        return if (existingPermission != null) {
            existingPermission.expiresAt = expiresAt
            existingPermission.grantedAt = LocalDateTime.now() // 可选：更新时间戳
            userPdfPermissionRepo.save(existingPermission)
        } else {
            userPdfPermissionRepo.save(UserPdfPermission(user, pdf, LocalDateTime.now(), expiresAt))
        }
    }


    /**
     * 获取用户所有可访问的 PDF（包含继承权限，排除关闭的子库）
     */
//    fun getAccessiblePdfs(user: User): List<PdfItem> {
//        val effectiveUser = getEffectiveUser(user)
//        val now = LocalDateTime.now()
//
//        // 获取 PDF 权限并排除过期
//        val pdfsFromDirectPermission = userPdfPermissionRepo.findByUser(effectiveUser)
//            .filter {
//                val expiresAt = it.expiresAt
//                expiresAt == null || expiresAt.isAfter(now)
//            }
//            .map { it.pdfItem }
//
//        // 获取子库权限并排除过期
//        val allowedCategories = userCategoryPermissionRepo.findByUser(effectiveUser)
//            .filter {
//                val expiresAt = it.expiresAt
//                expiresAt == null || expiresAt.isAfter(now)
//            }
//            .map { it.categoryName }
//            .toSet()
//
//        // 获取开启状态的子库
//        val openCategoryNames = pdfCategoryControlRepo.findAll()
//            .filter { it.isOpen }
//            .map { it.name }
//            .toSet()
//
//        // 最终过滤逻辑
//        val allPdfs = pdfItemRepo.findAll()
//        return allPdfs.filter { pdf ->
//            val category = pdf.category1
//            if (category !in openCategoryNames) return@filter false
//
//            pdfsFromDirectPermission.contains(pdf) || allowedCategories.contains(category)
//        }
//    }

    fun getAccessiblePdfIds(user: User): Set<Long> {
        val effectiveUser = getEffectiveUser(user)
        val now = LocalDateTime.now()

        // 1. 获取所有开启状态的子库名
        val openCategoryNames = pdfCategoryControlRepo.findAll()
            .filter { it.isOpen }
            .map { it.name }
            .toSet()

        // 2. 过滤出有效的子库权限
        val allowedCategories = userCategoryPermissionRepo.findByUser(effectiveUser)
            .filter {
                val expiresAt = it.expiresAt
                expiresAt == null || expiresAt.isAfter(now)
            }
            .map { it.categoryName }
            .filter { it in openCategoryNames } // 必须是开放子库
            .toSet()

        // 3. 获取用户拥有直接权限的 PDF 条目（必须属于开放子库）
        val directPdfIds = userPdfPermissionRepo.findByUser(effectiveUser)
            .filter {
                val expiresAt = it.expiresAt
                val pdf = it.pdfItem
                (expiresAt == null || expiresAt.isAfter(now)) &&
                        pdf.category1 in openCategoryNames // 子库必须是开放状态
            }
            .map { it.pdfItem.id }

        // 4. 获取用户具有子库权限的所有 PDF 条目
        val categoryPdfIds = pdfItemRepo.findAll()
            .asSequence()
            .filter { it.category1 in allowedCategories }
            .map { it.id }
            .toList()

        return (directPdfIds + categoryPdfIds).toSet()
    }

    /**
     * 查询 PDF 被授权给了哪些主用户（不含附属用户）
     */
    fun getPdfAuthorizedUsers(pdf: PdfItem): List<User> {
        return userPdfPermissionRepo
            .findByPdfItem(pdf)
            .map { it.user }
            .filter { !it.isSubUser }
    }

    /**
     * 获取有效主用户（处理附属用户继承权限）
     */
    private fun getEffectiveUser(user: User): User {
        return if (user.isSubUser) {
            user.parentUser ?: error("附属用户未绑定主用户")
        } else user
    }

    fun revokePdfFromUser(user: User, pdf: PdfItem) {
        val record = userPdfPermissionRepo.findByUserAndPdfItem(user, pdf)
            ?: throw IllegalArgumentException("该用户未拥有该 PDF 权限")
        userPdfPermissionRepo.delete(record)
    }

    @Transactional
    fun grantCategoryToUser(user: User, categoryName: String, expiresAt: LocalDateTime? = null) {
        if (user.isSubUser) {
            throw IllegalArgumentException("不能给附属用户授权子库权限")
        }

        val existingPermission = userCategoryPermissionRepo.findByUserAndCategoryName(user, categoryName)

        if (existingPermission != null) {
            existingPermission.expiresAt = expiresAt
            userCategoryPermissionRepo.save(existingPermission)
        } else {
            userCategoryPermissionRepo.save(UserCategoryPermission(user, categoryName, expiresAt))
        }
    }


    fun revokeCategoryFromUser(user: User, categoryName: String) {
        val permission = userCategoryPermissionRepo.findByUserAndCategoryName(user, categoryName)
            ?: throw IllegalArgumentException("该用户未拥有该子库权限")
        userCategoryPermissionRepo.delete(permission)
    }

    fun hasAccessToPdf(user: User, pdf: PdfItem): Boolean {
        val effectiveUser = getEffectiveUser(user)
        val now = LocalDateTime.now()

        val categoryName = pdf.category1
        val categoryControl = pdfCategoryControlRepo.findByName(categoryName) ?: return false

        // 子库未开放，直接拒绝
        if (!categoryControl.isOpen) return false

        // 条目权限有效
        val direct = userPdfPermissionRepo.findByUserAndPdfItem(effectiveUser, pdf)
        val hasPdfPermission = direct?.let {
            val expiresAt = it.expiresAt
            expiresAt == null || expiresAt.isAfter(now)
        } ?: false

        // 子库权限有效
        val categoryPermission = userCategoryPermissionRepo.findByUserAndCategoryName(effectiveUser, categoryName)
        val hasCategoryPermission = categoryPermission?.let {
            val expiresAt = it.expiresAt
            expiresAt == null || expiresAt.isAfter(now)
        } ?: false

        return hasPdfPermission || hasCategoryPermission
    }

    fun hasAccessToCategory(user: User, categoryName: String): Boolean {
        val effectiveUser = getEffectiveUser(user)
        val now = LocalDateTime.now()

        val categoryControl = pdfCategoryControlRepo.findByName(categoryName) ?: return false
        if (!categoryControl.isOpen) return false

        val permission = userCategoryPermissionRepo.findByUserAndCategoryName(effectiveUser, categoryName)
        return permission?.let {
            val expiresAt = it.expiresAt
            expiresAt == null || expiresAt.isAfter(now)
        } ?: false
    }

    fun getCategoryPermissionStatus(user: User, categoryName: String): Pair<Boolean, LocalDateTime?> {
        val effectiveUser = getEffectiveUser(user)
        val now = LocalDateTime.now()

        val categoryControl = pdfCategoryControlRepo.findByName(categoryName)
            ?: return Pair(false, null)

        if (!categoryControl.isOpen) return Pair(false, null)

        val permission = userCategoryPermissionRepo.findByUserAndCategoryName(effectiveUser, categoryName)
            ?: return Pair(false, null)  // ❗️关键修复：没有权限记录 = 没有 access

        val expiresAt = permission.expiresAt
        val isValid = expiresAt == null || expiresAt.isAfter(now)

        return Pair(isValid, expiresAt)
    }


}
