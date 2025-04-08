package com.boji.backend.service.storage

import com.aliyun.oss.OSSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*



@Service
@Profile("prod")  // 只在生产环境生效
class OssStorageService(
    @Value("\${aliyun.oss.endpoint}") private val endpoint: String,
    @Value("\${aliyun.oss.accessKeyId}") private val accessKeyId: String,
    @Value("\${aliyun.oss.accessKeySecret}") private val accessKeySecret: String,
    @Value("\${aliyun.oss.bucketName}") private val bucketName: String,
    @Value("\${aliyun.oss.base-url}") private val baseUrl: String,
) : FileStorageService {

    override fun upload(file: MultipartFile, folder: String): String {
        val ossClient = OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret)
        try {
            val ext = file.originalFilename?.substringAfterLast('.') ?: "bin"
            val key = "$folder/${UUID.randomUUID()}.$ext"
            ossClient.putObject(bucketName, key, file.inputStream)
            return "$baseUrl$key"
        } finally {
            ossClient.shutdown()
        }
    }
}
