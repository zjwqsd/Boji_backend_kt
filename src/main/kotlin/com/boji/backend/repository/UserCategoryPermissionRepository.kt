package com.boji.backend.repository

import com.boji.backend.model.User
import com.boji.backend.model.UserCategoryPermission
import org.springframework.data.jpa.repository.JpaRepository

interface UserCategoryPermissionRepository : JpaRepository<UserCategoryPermission, Long> {
    fun findByUser(user: User): List<UserCategoryPermission>
    fun existsByUserAndCategoryName(user: User, categoryName: String): Boolean
    fun findByUserAndCategoryName(user: User, categoryName: String): UserCategoryPermission?
}
