package com.boji.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_pdf_permissions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "pdf_item_id"])]
)
class UserPdfPermission() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_item_id", nullable = false)
    lateinit var pdfItem: PdfItem

    @Column(name = "granted_at", nullable = false)
    var grantedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null

    // 方便代码构建使用的构造函数（非必须）
    constructor(
        user: User,
        pdfItem: PdfItem,
        grantedAt: LocalDateTime = LocalDateTime.now(),
        expiresAt: LocalDateTime? = null
    ) : this() {
        this.user = user
        this.pdfItem = pdfItem
        this.grantedAt = grantedAt
        this.expiresAt = expiresAt
    }
}
