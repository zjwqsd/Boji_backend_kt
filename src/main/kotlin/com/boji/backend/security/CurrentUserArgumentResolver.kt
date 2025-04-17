package com.boji.backend.security

import com.boji.backend.model.User
import com.boji.backend.repository.AdminRepository
import com.boji.backend.repository.UserRepository
import com.boji.backend.security.annotation.CurrentUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

// 📁 com.boji.backend.security

// 📁 com.boji.backend.security

@Component
class CurrentUserArgumentResolver(
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getParameterAnnotation(CurrentUser::class.java) != null
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.nativeRequest as HttpServletRequest
        val userId = request.getAttribute("userId") as? Long
            ?: throw RuntimeException("未找到当前用户ID")
        val tokenRole = request.getAttribute("role") as? String
            ?: throw RuntimeException("未找到当前用户角色")

        val annotation = parameter.getParameterAnnotation(CurrentUser::class.java)
            ?: return null // 不应该发生，保底

        val expectedRole = annotation.role

        // ⚠️ 如果角色不匹配，返回 null（而不是抛异常）
        if (expectedRole != tokenRole) {
            return null
        }

        return when (expectedRole) {
            "user" -> userRepository.findById(userId).orElse(null)
            "admin" -> adminRepository.findById(userId).orElse(null)
            else -> null
        }
    }
}

