package com.boji.backend.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val expirationMillis = 1000 * 60 * 60 * 2 // 2小时

    fun generateToken(userId: Long): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(secretKey)
            .compact()
    }
    // ✅ 超级管理员生成 Token（含角色标记）
    fun generateAdminToken(adminId: Long): String {
        return Jwts.builder()
            .setSubject(adminId.toString())
            .claim("role", "admin")  // 添加角色字段
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(secretKey)
            .compact()
    }

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
