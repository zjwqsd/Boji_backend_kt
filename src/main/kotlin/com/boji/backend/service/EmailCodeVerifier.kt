package com.boji.backend.service

import jakarta.servlet.http.HttpServletRequest

interface EmailCodeVerifier {
    fun verify(email: String, code: Int, request: HttpServletRequest): Boolean
}