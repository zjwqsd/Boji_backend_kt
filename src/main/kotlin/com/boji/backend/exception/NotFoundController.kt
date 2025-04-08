package com.boji.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class NotFoundController {

    @RequestMapping("/**")
    fun handleUnknownPath(): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(mapOf("message" to "接口不存在"))
    }
}
