package com.boji.backend.controller

import com.boji.backend.dto.HouseholdRequest
import com.boji.backend.dto.PdfPreviewDTO
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
import java.io.File
import java.nio.file.Paths

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
        if(household?.category2 != form.category2){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse("户名不属于该二级分类"))
        }

        // 5. 存入数据库
        val newItem = PdfItem(
            customId = customId,
            title = form.title,
            category1 = form.category1,
            category2 = form.category2,
            location = form.location,
            description = form.description,
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


}