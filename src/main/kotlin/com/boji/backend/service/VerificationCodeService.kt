package com.boji.backend.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class VerificationCodeService(
    private val redisTemplate: StringRedisTemplate,
    private val emailService: EmailService
) {
    fun generateAndSendCode(email: String) {
        val code = (100000..999999).random()
        println(code.toString())
        redisTemplate.opsForValue().set("email_code:$email", code.toString(), Duration.ofMinutes(5))
        emailService.sendVerificationCode(email, code)
    }
}