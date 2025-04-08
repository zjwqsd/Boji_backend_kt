package com.boji.backend.dto

import jakarta.validation.constraints.*

data class LoginRequest(
    @field:NotBlank(message = "邮箱不能为空")
    val email: String = "",

    @field:NotBlank(message = "密码不能为空")
    val password: String = ""
)