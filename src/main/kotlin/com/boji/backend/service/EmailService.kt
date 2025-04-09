package com.boji.backend.service

import com.boji.backend.exception.GlobalExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendVerificationCode(email: String, code: Int) {
        try {
            val message = SimpleMailMessage()
            message.setTo(email)
            message.setSubject("您的验证码")
            message.setText("您的验证码是：$code，5分钟内有效。")
            message.setFrom("1561365020@qq.com")
            javaMailSender.send(message)
        } catch (ex: MailException) {
            logger.error("发送验证码失败，收件人: $email", ex)
            // ✅ 抛出你全局异常类中定义的业务异常
            throw GlobalExceptionHandler.BusinessException("验证码发送失败，请稍后再试")
        }
    }
}


