package com.boji.backend.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender
) {
    fun sendVerificationCode(email: String, code: Int) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.setSubject("您的验证码")
        message.setText("您的验证码是：$code，5分钟内有效。")
        message.setFrom("1561365020@qq.com")
        javaMailSender.send(message)
    }
}
