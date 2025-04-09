package com.boji.backend.security


import com.boji.backend.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class UserAccessInterceptor(
    private val jwtUtil: JwtUtil
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 仅拦截标注了 @UserOnly 的方法
        if (handler is HandlerMethod) {
            val method = handler.method
            if (method.isAnnotationPresent(UserOnly::class.java)) {
                val authHeader = request.getHeader("Authorization")
                if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效 token")
                    return false
                }

                val token = authHeader.removePrefix("Bearer ").trim()
                val userId = jwtUtil.parseUserId(token)

                if (userId == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效或过期的 token")
                    return false
                }

                // 可选：也可以校验角色不为 admin 的情况，确保这是普通用户
                // val role = jwtUtil.parseRole(token)
                // if (role != "user") {
                //     response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足，仅普通用户可访问")
                //     return false
                // }

                // 将 userId 放入 request 中，便于 Controller 获取
                request.setAttribute("userId", userId)
            }
        }
        return true
    }
}
