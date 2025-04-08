
package com.boji.backend.dto
import jakarta.validation.constraints.*

data class RegisterRequest(

    @field:NotBlank(message = "昵称不能为空")
    val nickname: String = "",

    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String = "",

    @field:NotBlank(message = "密码不能为空")
    val password: String = "",

    @field:NotNull(message = "验证码不能为空")
    val emailcode: Int = 0,

    val realname: String? = null,
    val address: String? = null,
    val company: String? = null,
    val phone: String? = null,
    val userId: String? = null
)