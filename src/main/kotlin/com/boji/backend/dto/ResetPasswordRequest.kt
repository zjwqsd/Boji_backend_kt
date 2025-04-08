package com.boji.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:Email(message = "邮箱格式不正确")
    val email: String = "",

    val emailcode: Int = 0,

    @field:NotBlank(message = "新密码不能为空")
    val newpassword: String = ""
)