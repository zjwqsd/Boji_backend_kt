package com.boji.backend.init


import com.boji.backend.model.Admin
import com.boji.backend.repository.AdminRepository
import com.boji.backend.util.PasswordEncoder
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class AdminInitializer(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val defaultUsername = "admin"
        val defaultPassword = "supersecret"

        if (!adminRepository.existsByUsername(defaultUsername)) {
            val admin = Admin(
                username = defaultUsername,
                password = passwordEncoder.encode(defaultPassword)
            )
            adminRepository.save(admin)
            println("✅ 已创建默认超级管理员账号：$defaultUsername")
        } else {
            println("ℹ️ 超级管理员账号已存在：$defaultUsername")
        }
    }
}