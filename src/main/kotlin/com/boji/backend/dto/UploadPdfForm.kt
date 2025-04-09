package com.boji.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UploadPdfForm(

    @field:NotBlank(message = "customId 不能为空")
    val customId: String = "",

    @field:NotBlank(message = "标题不能为空")
    val title: String = "",

    @field:NotBlank(message = "一级分类不能为空")
    val category1: String = "",

    @field:NotBlank(message = "二级分类不能为空")
    val category2: String = "",

    @field:NotBlank(message = "位置不能为空")
    val location: String = "",

//    @field:NotBlank(message = "描述不能为空")
//    val description: String = "",
    @field:NotNull(message = "描述字段必须存在")
    val description: String? = "",

    @field:NotBlank(message = "形状不能为空")
    val shape: String = "",

    @field:NotBlank(message = "年份不能为空")
    val year: String = "",

    @field:NotNull(message = "价格不能为空")
    val price: Double, // <-- 改为非可空

    val householdId: Long? = null
)
