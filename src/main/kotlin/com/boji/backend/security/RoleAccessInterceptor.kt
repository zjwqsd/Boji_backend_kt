package com.boji.backend.security

import com.boji.backend.security.annotation.RoleAllowed
import com.boji.backend.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor


// 📁 com.boji.backend.security

@Component
class RoleAccessInterceptor(
    private val jwtUtil: JwtUtil
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod) {
            val annotation = handler.method.getAnnotation(RoleAllowed::class.java)
            if (annotation != null) {
                val authHeader = request.getHeader("Authorization")
                if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效 token")
                    return false
                }

                val token = authHeader.removePrefix("Bearer ").trim()
                val role = jwtUtil.parseRole(token)
                val userId = jwtUtil.parseUserId(token)

                if (role == null || userId == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效 token")
                    return false
                }

                if (role !in annotation.roles) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足：仅 ${annotation.roles.joinToString()} 可访问")
                    return false
                }

                // 保存 userId 供 @CurrentUser 使用
                request.setAttribute("userId", userId)
                request.setAttribute("role", role)
            }
        }
        return true
    }
}
