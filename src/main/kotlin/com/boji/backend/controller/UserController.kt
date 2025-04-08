package com.boji.backend.controller

import com.boji.backend.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController {

    @GetMapping("/hello")
    fun sayHello(): ApiResponse<String> {
        return ApiResponse("Hello, Kotlin Spring Boot!")
    }

//    @GetMapping("/fail")
//    fun fail(): ApiResponse<String> {
//        throw IllegalArgumentException("你访问错啦")
//    }
}