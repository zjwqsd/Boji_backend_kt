package com.boji.backend.service

import org.codehaus.jettison.json.JSONObject
//import org.json.JSONObject
import org.springframework.stereotype.Component

@Component
class WechatPayClient {

    fun buildJsapiParams(order: com.boji.backend.model.Order): Map<String, String> {
        return mapOf(
            "appId" to "wx1234567890",
            "timeStamp" to System.currentTimeMillis().toString(),
            "nonceStr" to java.util.UUID.randomUUID().toString(),
            "package" to "prepay_id=mock123456",
            "signType" to "RSA",
            "paySign" to "mock_signature"
        )
    }

    fun verifySignature(body: String, headers: Map<String, String>): Boolean {
        return true // mock 校验成功
    }

    fun decryptOrderId(body: String): Long? {
        return try {
            val json = JSONObject(body)
            val id = json.optString("id") // 这里对应你回调里模拟的 "id": "1"
            id.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
