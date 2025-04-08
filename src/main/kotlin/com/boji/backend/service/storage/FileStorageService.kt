package com.boji.backend.service.storage

import org.springframework.web.multipart.MultipartFile

interface FileStorageService {
    fun upload(file: MultipartFile, folder: String): String
}