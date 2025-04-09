package com.boji.backend.controller

import com.boji.backend.dto.HouseholdRequest
import com.boji.backend.dto.PdfPreviewDTO
import com.boji.backend.dto.UpdateItemRequest
import com.boji.backend.dto.UploadPdfForm

import com.boji.backend.exception.GlobalExceptionHandler
import com.boji.backend.model.Household
import com.boji.backend.model.PdfItem
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.AdminOnly
import com.boji.backend.service.storage.FileStorageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.boji.backend.repository.HouseholdRepository
import org.springframework.transaction.annotation.Transactional
//import jakarta.validation.constraints.Null

import java.io.File
//import java.nio.file.Paths

@RestController
@RequestMapping("/api/item")
class PdfItemController(
    private val fileStorageService: FileStorageService,
    private val pdfItemRepository: PdfItemRepository,
    private val householdRepository: HouseholdRepository
) {
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @AdminOnly
    fun uploadPdfItem(
        @RequestHeader("Authorization") authHeader: String?,
        @Valid @ModelAttribute form: UploadPdfForm,
        @RequestParam file: MultipartFile,
        @RequestParam(required = false) cover: MultipartFile?
    ): ResponseEntity<ApiResponse<Any>> {
        val customId = form.customId
        val householdId = form.householdId

        // 1. 检查编号是否已存在
        if (pdfItemRepository.existsByCustomId(customId)) {
//            return ResponseEntity.badRequest().body(ApiResponse("编号已存在"))
            throw GlobalExceptionHandler.BusinessException("编号已存在")
        }


        // 2. 上传 PDF 到存储系统（本地 or OSS）
        val fileExt = file.originalFilename?.substringAfterLast('.', "pdf") ?: "pdf"
        val pdfFileName = "$customId.$fileExt"
        val pdfUrl = fileStorageService.upload(file, "pdfs")

        // 3. 上传封面（可选）
        val coverUrl = cover?.let {
            val ext = it.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
            val coverFileName = "$customId.$ext"
            fileStorageService.upload(it, "covers")
        }

        // 4. 归户处理（可选）
        val household = householdId?.let {
            householdRepository.findById(it).orElse(null)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("归户不存在"))
        }
        //检查 household 和 category2 是否对应
        if (household != null) {
            if (household.category2 != form.category2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse("户名不属于该二级分类"))
            }
        }

        // 5. 存入数据库
        val newItem = PdfItem(
            customId = customId,
            title = form.title,
            category1 = form.category1,
            category2 = form.category2,
            location = form.location,
            description = form.description?: "",
            shape = form.shape,
            year = form.year,
            price = form.price,
            pdfPath = pdfUrl,
            coverPath = coverUrl,
            household = household
        )

        pdfItemRepository.save(newItem)

        return ResponseEntity.ok(ApiResponse("商品上传成功"))
    }

    @GetMapping("/filter")
    fun filterPdfItems(
        @RequestParam(required = false) category1: String?,
        @RequestParam(required = false) category2: String?
    ): ResponseEntity<ApiResponse<List<Long>>> {
        val items: List<PdfItem> = when {
            category1.isNullOrBlank() -> pdfItemRepository.findAll()
            !category2.isNullOrBlank() -> pdfItemRepository.findAllByCategory1AndCategory2(category1, category2)
            else -> pdfItemRepository.findAllByCategory1(category1)
        }

        val ids = items.map { it.id }
        return ResponseEntity.ok(ApiResponse("查询成功",ids))
    }

    @PostMapping("/household/create")
    @AdminOnly  // 你之前自定义的注解，假设只允许管理员创建
    fun createHousehold(
        @RequestHeader("Authorization") authHeader: String?,
        @Valid @RequestBody request: HouseholdRequest
    ): ResponseEntity<ApiResponse<Any>> {

        if (householdRepository.existsByName(request.name)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse("户名已存在"))
        }

        if (householdRepository.existsByCode(request.code)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse("户号已存在"))
        }

        val household = Household(
            name = request.name,
            code = request.code,
            description = request.description ?: "",
            category2 = request.category2
        )

        householdRepository.save(household)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse("归户创建成功", household))
    }

    data class BatchPreviewRequest(
        val ids: List<Long>
    )

    @PostMapping("/batch-preview")
    fun batchPreview(
        @RequestBody request: BatchPreviewRequest
    ): ResponseEntity<ApiResponse<List<PdfPreviewDTO>>> {
        val items = pdfItemRepository.findAllById(request.ids)

        val previews = items.map {
            PdfPreviewDTO(
                id = it.id,
                customId = it.customId,
                title = it.title,
                category1 = it.category1,
                category2 = it.category2,
                householdId = it.household?.id,
                location = it.location,
                year = it.year,
                price = it.price
            )
        }

        return ResponseEntity.ok(ApiResponse("预览成功", previews))
    }

    @PutMapping("/update/{id}")
    @AdminOnly
    fun updateItem(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody request: UpdateItemRequest
    ): ResponseEntity<ApiResponse<Any>>{
        val item = pdfItemRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("商品不存在"))

        // 🆔 检查 customId 是否要更新，并校验唯一性
        request.customId?.let { newCustomId ->
            if (newCustomId != item.customId) {
                val exists = pdfItemRepository.existsByCustomIdAndIdNot(newCustomId, item.id)
                if (exists) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse("商品编号已被其他商品使用"))
                }
//                item.customId = newCustomId  // ✅ 安全更新
            }
        }

        // ⚠️ household 是对象，不是 id，需先查出来
        var household: Household? = null
        request.householdId?.let { hId ->
            household = householdRepository.findById(hId).orElse(null)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("归户不存在"))
        }


        if (household != null ) {
            val category2 = request.category2?:item.category2
            if (household!!.category2 != category2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse("户名不属于该二级分类"))
            }
        }

        // 更新字段（不能更新 customId 和 pdfPath）
        request.title?.let { item.title = it }
        request.customId?.let{item.customId=it}
        request.category1?.let { item.category1 = it }
        request.category2?.let { item.category2 = it }
        household?.let { item.household = it } // ✅ 用对象赋值
        request.location?.let { item.location = it }
        request.description?.let { item.description = it }
        request.shape?.let { item.shape = it }
        request.year?.let { item.year = it }
        request.price?.let { item.price = it }

        pdfItemRepository.save(item)

        val response = mapOf(
//            "message" to "商品信息已更新",
//            "updated_item" to mapOf(
                "id" to item.id,
                "customId" to item.customId,
                "title" to item.title,
                "category1" to item.category1,
                "category2" to item.category2,
                "householdId" to item.household?.id, // ✅ 返回 ID
                "location" to item.location,
                "description" to item.description,
                "shape" to item.shape,
                "year" to item.year,
                "price" to item.price
//            )
        )
        return ResponseEntity.ok(ApiResponse("更新成功", response))
    }

    @DeleteMapping("/delete/{id}")
    @AdminOnly
    fun deleteItem(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<ApiResponse<Any>> {
        val item = pdfItemRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("商品不存在"))

        // 🗑️ 删除 PDF 文件（如果存在）
        val pdfFilePath = item.pdfPath
        val file = File(pdfFilePath)
        if (file.exists()) {
            try {
                file.delete()
            } catch (ex: Exception) {
                // 可以记录日志
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse("删除时发生错误，请重试"))
            }
        }

        // 🔥 删除数据库记录
        pdfItemRepository.delete(item)

        return ResponseEntity.ok(ApiResponse("商品信息已删除"))
    }

    @GetMapping("/search")
    fun searchPdfs(@RequestParam query: String): ResponseEntity<List<Long>> {
        if (query.length < 1) {
            return ResponseEntity.badRequest().build()
        }

        val searchTerm = "%${query.lowercase()}%"
        val results = pdfItemRepository.searchByKeyword(searchTerm)

        val ids = results.map { it.id }
        return ResponseEntity.ok(ids)
    }

    data class HouseholdDTO(
        val id: Long,
        val name: String,
        val code: String,
        val category2: String,
        val description: String? = null,
    )


    @GetMapping("/household/all")
    fun getAllHouseholds(): ResponseEntity<ApiResponse<List<HouseholdDTO>>> {
        val households = householdRepository.findAll()
        val result = households.map { HouseholdDTO(it.id, it.name,it.category2, it.code,it.description) }
        return ResponseEntity.ok(ApiResponse("查询成功",result))
    }


    data class UpdateHouseholdRequest(
        val name: String? = null,
        val category2: String? = null
    )


    @PutMapping("/household/update/{id}")
    @Transactional
    @AdminOnly
    fun updateHousehold(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable id: Long,
        @RequestBody request: UpdateHouseholdRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val household = householdRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("归户不存在"))

        // 更新字段（非 null 才更新）
        request.name?.let { household.name = it }
        request.category2?.let { household.category2 = it }

        householdRepository.save(household)

        // ✅ 如果更新了 category2，同步更新 PdfItem 表
        request.category2?.let { newCategory2 ->
            pdfItemRepository.updateCategory2ByHouseholdId(newCategory2, id)
        }

        return ResponseEntity.ok(ApiResponse("归户信息已更新"))
    }

    @GetMapping("/household/filter/{id}")
    fun getHouseholdItems(@PathVariable id: Long): ResponseEntity<ApiResponse<List<Long>>> {
        val household = householdRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("归户不存在"))

        val itemIds = pdfItemRepository.findByHouseholdId(id).map { it.id }
        return ResponseEntity.ok(ApiResponse("查询归户条目成功", itemIds))
    }

    @DeleteMapping("/household/delete/{id}")
    @Transactional
    @AdminOnly
    fun deleteHousehold(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable id: Long): ResponseEntity<ApiResponse<Any>> {
        val household = householdRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("归户不存在"))

        // 👇 更新 PdfItem 表中的 householdId 为 null
        pdfItemRepository.clearHouseholdIdByHouseholdId(id)

        // 👇 删除归户
        householdRepository.delete(household)

        return ResponseEntity.ok(ApiResponse("删除归户成功，注意PDF归户信息的更新"))
    }

}