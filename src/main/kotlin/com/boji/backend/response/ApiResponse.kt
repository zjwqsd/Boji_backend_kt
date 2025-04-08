package com.boji.backend.response
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val message: String = "success",
    val data: T? = null
)