package com.boji.backend.security


import com.boji.backend.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAccessInterceptor(
    private val jwtUtil: JwtUtil
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 仅拦截标注了 @AdminOnly 的方法
        if (handler is HandlerMethod) {
            val method = handler.method
            if (method.isAnnotationPresent(AdminOnly::class.java)) {
                val authHeader = request.getHeader("Authorization")
                if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效 token")
                    return false
                }

                val token = authHeader.removePrefix("Bearer ").trim()
                val role = jwtUtil.parseRole(token)

                if (role != "admin") {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足，仅管理员可访问")
                    return false
                }
            }
        }
        return true
    }
}