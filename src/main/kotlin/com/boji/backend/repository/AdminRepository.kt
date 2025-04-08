package com.boji.backend.repository


import com.boji.backend.model.Admin
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByUsername(username: String): Admin?
    fun existsByUsername(username: String): Boolean  // ✅ 自动实现
}