package com.boji.backend.service.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
@Profile("dev")  // 只在 dev 环境生效
class LocalStorageService(
    @Value("\${file.local.base-path}") private val basePath: String,
    @Value("\${file.local.base-url}") private val baseUrl: String
) : FileStorageService {

    override fun upload(file: MultipartFile, folder: String): String {
        val ext = file.originalFilename?.substringAfterLast('.') ?: "bin"
        val filename = "${UUID.randomUUID()}.$ext"
        val dir = Paths.get(basePath, folder)
        Files.createDirectories(dir)

//        val filePath = dir.resolve(filename)
//        file.transferTo(filePath)

//        return "$baseUrl/$folder/$filename"
//        return filePath.toAbsolutePath().toString()
        val relativePath = "$folder/$filename"
        val filePath = dir.resolve(filename)
        file.transferTo(filePath)

        return relativePath // 返回 pdfPath 字段
    }
}
