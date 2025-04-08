package com.boji.backend.dto
import jakarta.validation.constraints.NotBlank

data class HouseholdRequest(
    @field:NotBlank(message = "户名不能为空")
    val name: String = "",

    @field:NotBlank(message = "户号不能为空")
    val code: String= "",

    val description: String? = null,

    @field:NotBlank(message = "所属二级分类不能为空")
    val category2: String= ""
)