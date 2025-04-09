package com.boji.backend.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    // 默认过期时间：普通用户 2 小时，管理员 4 小时
    private val defaultUserExpiry = 1000 * 60 * 60 * 2L     // 2 小时
    private val defaultAdminExpiry = 1000 * 60 * 60 * 4L    // 4 小时

    // ✅ 通用构建函数
    fun generateTokenWithExpiry(userId: Long, role: String, expiryMillis: Long): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role)
            .setExpiration(Date(System.currentTimeMillis() + expiryMillis))
            .signWith(secretKey)
            .compact()
    }

    // ✅ 普通用户默认 token
    fun generateToken(userId: Long): String {
        return generateTokenWithExpiry(userId, "user", defaultUserExpiry)
    }

    // ✅ 管理员默认 token
    fun generateAdminToken(adminId: Long): String {
        return generateTokenWithExpiry(adminId, "admin", defaultAdminExpiry)
    }

    // ✅ 提取用户ID
    fun parseUserId(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims.subject.toLong()
        } catch (e: Exception) {
            null
        }
    }

    // ✅ 提取角色
    fun parseRole(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
            claims["role"] as? String
        } catch (e: Exception) {
            null
        }
    }
}
