package com.boji.backend.repository

import com.boji.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findByUserId(userId: String): User?
    fun findAllByIsSubUserFalse(): List<User>
    fun findByUserIdAndIsSubUserFalse(userId: String): User?
    fun existsByUserId(userId: String): Boolean
}