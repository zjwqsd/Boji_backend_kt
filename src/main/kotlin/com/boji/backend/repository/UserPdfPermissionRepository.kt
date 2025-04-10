package com.boji.backend.repository

import com.boji.backend.model.UserPdfPermission
import com.boji.backend.model.User
import com.boji.backend.model.PdfItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserPdfPermissionRepository : JpaRepository<UserPdfPermission, Long> {

    fun findByUser(user: User): List<UserPdfPermission>

    fun findByUserAndPdfItem(user: User, pdfItem: PdfItem): UserPdfPermission?

    fun existsByUserAndPdfItem(user: User, pdfItem: PdfItem): Boolean

    fun deleteByUserAndPdfItem(user: User, pdfItem: PdfItem)

    fun findByPdfItem(pdfItem: PdfItem): List<UserPdfPermission>
}
