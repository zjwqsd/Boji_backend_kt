package com.boji.backend.service


import org.springframework.stereotype.Component

@Component
class UserIdGenerator {

    fun generateUserId(): String {
        // 示例逻辑，可以是 UUID、时间戳、随机码等
        val millis = System.currentTimeMillis().toString()
        return "U" + millis.takeLast(6)  // U123456
    }
}