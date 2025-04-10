package com.boji.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_category_permissions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "category_name"])]
)
class UserCategoryPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @Column(name = "category_name", nullable = false)
    lateinit var categoryName: String

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null
    // ✅ 无参构造函数（JPA 必需）
    constructor()

    // ✅ 带参构造函数（业务逻辑可用）
    constructor(user: User, categoryName: String) {
        this.user = user
        this.categoryName = categoryName
    }

    constructor(user: User, categoryName: String, expiresAt: LocalDateTime?) {
        this.user = user
        this.categoryName = categoryName
        this.expiresAt = expiresAt
    }
}
