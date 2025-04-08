package com.boji.backend.service.impl

import com.boji.backend.service.EmailCodeVerifier
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class SimpleEmailCodeVerifier(
    private val redisTemplate: StringRedisTemplate
) : EmailCodeVerifier {

    override fun verify(email: String, code: Int, request: HttpServletRequest): Boolean {
        val storedCode = redisTemplate.opsForValue().get("email_code:$email")
        return storedCode != null && storedCode == code.toString()
    }
}