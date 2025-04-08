package com.boji.backend.controller

import com.boji.backend.dto.AssignSubUserRequest
import com.boji.backend.dto.UploadPdfForm
import com.boji.backend.response.ApiResponse
import com.boji.backend.security.AdminOnly
import org.springframework.http.ResponseEntity
import com.boji.backend.repository.UserRepository
import com.boji.backend.dto.SubUserResponse
import com.boji.backend.dto.UserWithSubsResponse
import com.boji.backend.model.PdfItem
import com.boji.backend.model.User
import com.boji.backend.service.UserIdGenerator
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.boji.backend.repository.PdfItemRepository
import com.boji.backend.repository.HouseholdRepository
import java.io.File
import java.nio.file.Paths

@RestController
@RequestMapping("/api/admin")
class AdminManageController(
    private val userRepository: UserRepository,
    private val userIdGenerator: UserIdGenerator,
    private val pdfItemRepository: PdfItemRepository,
    private val householdRepository: HouseholdRepository
) {

    @GetMapping("/users-with-subs")
    @AdminOnly
    fun getUsersWithSubs(): ResponseEntity<ApiResponse<Any>> {
        val users = userRepository.findAllByIsSubUserFalse()

        val result = users.map { user ->
            UserWithSubsResponse(
                id = user.id,
                userId = user.userId,
                nickname = user.nickname,
                email = user.email,
                realName = user.realname,
                phone = user.phone,
                address = user.address,
                company = user.company,
                subUsers = user.subUsers.map { sub ->
                    SubUserResponse(
                        id = sub.id,
                        userId = sub.userId,
                        nickname = sub.nickname,
                        email = sub.email,
                        realName = sub.realname,
                        phone = sub.phone,
                        address = sub.address,
                        company = sub.company,
                    )
                }
            )
        }

        return ResponseEntity.ok(ApiResponse("获取用户成功", result))
    }


    @PostMapping("/assign-sub-user")
    @AdminOnly
    fun assignSubUser(@Valid @RequestBody req: AssignSubUserRequest): ResponseEntity<ApiResponse<Any>> {
        val parentUser = userRepository.findByUserIdAndIsSubUserFalse(req.parentUserId)
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse("用户不存在或者无法绑定附属用户"))

        // 生成唯一子用户 ID
        var subUserId: String
        do {
            subUserId = userIdGenerator.generateUserId()
        } while (userRepository.existsByUserId(subUserId))

        // 创建附属用户
        val subUser = User(
            userId = subUserId,
            isSubUser = true,
            parentUser = parentUser,
            emailVerified = false,
            address = parentUser.address,
            company = parentUser.company,
            password = parentUser.password
        )

        userRepository.save(subUser)

        return ResponseEntity.ok(
            ApiResponse("附属用户已创建", mapOf("sub_user_id" to subUserId))
        )
    }


}


