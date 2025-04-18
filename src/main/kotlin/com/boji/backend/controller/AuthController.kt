package com.boji.backend.controller

import com.boji.backend.dto.LoginRequest
import com.boji.backend.dto.RegisterRequest
import com.boji.backend.dto.ResetPasswordRequest
import com.boji.backend.model.User
import com.boji.backend.repository.UserRepository
import com.boji.backend.response.ApiResponse
import com.boji.backend.util.JwtUtil
import com.boji.backend.util.PasswordEncoder
import com.boji.backend.service.UserIdGenerator
import com.boji.backend.service.EmailCodeVerifier
import com.boji.backend.dto.AdminLoginRequest
import com.boji.backend.repository.AdminRepository
//import com.boji.backend.security.UserOnly
import com.boji.backend.security.annotation.CurrentUser
import com.boji.backend.security.annotation.RoleAllowed
import com.boji.backend.service.VerificationCodeService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val emailCodeVerifier: EmailCodeVerifier,   // ✅ 注入接口
    private val userIdGenerator: UserIdGenerator ,    // ✅ 注入生成器
    private val verificationCodeService: VerificationCodeService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<Any>> {
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse("用户不存在"))

        if (!passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse("密码错误"))
        }

        val token = jwtUtil.generateToken(user.id)

        val data = mapOf(
            "token" to token,
//            "user" to mapOf(
//                "id" to user.id,
            "nickname" to user.nickname,
//                "email" to user.email,
//                "isSubUser" to user.isSubUser
//            )
        )

        return ResponseEntity.ok(ApiResponse("登录成功", data))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest,request: HttpServletRequest): ResponseEntity<ApiResponse<Any>> {
        // 1️⃣ 校验验证码
        if (!emailCodeVerifier.verify(req.email, req.emailcode, request)) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse("注册失败：验证码错误"))
        }

        // 2️⃣ 附属用户绑定逻辑
        if (!req.userId.isNullOrBlank()) {
            val existingUser = userRepository.findByUserId(req.userId)
                ?: return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse("绑定失败：用户 ID 不存在"))

            if (!existingUser.email.isNullOrBlank()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse("绑定失败：该用户已绑定邮箱"))
            }

            // 更新附属用户资料
            val updatedUser = existingUser.copy(
                email = req.email,
                password = req.password,
                emailVerified = true,
                nickname = req.nickname,
                realname = req.realname,
                phone = req.phone,
                address = req.address,
                company = req.company
            )
            userRepository.save(updatedUser)

//            val response = mapOf(
////                "message" to "附属用户绑定成功",
//                "nickname" to updatedUser.userId,
//                "email" to updatedUser.email
//            )

            return ResponseEntity.ok(ApiResponse("绑定成功"))
        }

        // 3️⃣ 检查邮箱是否已被注册
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse("注册失败：邮箱已被绑定"))
        }

        // 4️⃣ 自动生成唯一 userId
        var userId: String
        do {
            userId = userIdGenerator.generateUserId()
        } while (userRepository.findByUserId(userId) != null)

        // 5️⃣ 创建用户
        val newUser = User(
            userId = userId,
            nickname = req.nickname,
            realname = req.realname,
            address = req.address,
            company = req.company,
            phone = req.phone,
            email = req.email,
            password = passwordEncoder.encode(req.password),
            emailVerified = true,
            isSubUser = false,
            parentUser = null
        )

        userRepository.save(newUser)



        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse("注册成功"))
    }

    @GetMapping("/me")
    @RoleAllowed("user")
    fun getCurrentUser(
        @RequestHeader("Authorization") authHeader: String?,
        @CurrentUser(role = "user") user: User,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        val data = mapOf(
            "id" to user.id,
            "userId" to user.userId,
            "email" to user.email,
            "realname" to user.realname,
            "nickname" to user.nickname,
            "phone" to user.phone,
            "company" to user.company,
            "address" to user.address,
            "is_sub_user" to user.isSubUser
            // "parent_id" to user.parentUser?.id  // 可选
        )

        return ResponseEntity.ok(ApiResponse("获取用户信息成功", data))
    }

    data class UpdateUserRequest(
        val nickname: String?,
        val realname: String?,
        val address: String?,
        val company: String?,
        val phone: String?
    )


    @PostMapping("/update")
    @RoleAllowed("user")
    fun updateCurrentUser(
        @RequestHeader("Authorization") authHeader: String?,
        @CurrentUser(role = "user") user: User,
        @RequestBody updateRequest: UpdateUserRequest
    ): ResponseEntity<ApiResponse<Any>> {

        // 更新字段
        user.nickname = updateRequest.nickname ?: user.nickname
        user.realname = updateRequest.realname ?: user.realname
        user.address = updateRequest.address ?: user.address
        user.company = updateRequest.company ?: user.company
        user.phone = updateRequest.phone ?: user.phone

        userRepository.save(user)

        return ResponseEntity.ok(ApiResponse("用户信息更新成功"))
    }


    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody req: ResetPasswordRequest,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<Any>> {

        // 1️⃣ 验证邮箱验证码
        if (!emailCodeVerifier.verify(req.email, req.emailcode, request)) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse("重置密码失败：验证码错误"))
        }

        // 2️⃣ 查找用户
        val user = userRepository.findByEmail(req.email)
            ?: return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse("重置密码失败：用户不存在"))

        // 3️⃣ 更新密码
        user.password = passwordEncoder.encode(req.newpassword)
        userRepository.save(user)

        return ResponseEntity.ok(ApiResponse("密码已重置"))
    }

    data class EmailRequest(
        val email: String
    )
    @PostMapping("/send-code")
    fun sendCode(@RequestBody request: EmailRequest): ResponseEntity<ApiResponse<Any>> {
        verificationCodeService.generateAndSendCode(request.email)
        return ResponseEntity.ok(ApiResponse("验证码已发送"))
    }
}


@RestController
@RequestMapping("/api/auth/super")
class SuperAuthController(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: AdminLoginRequest): ResponseEntity<ApiResponse<Any>> {
        val admin = adminRepository.findByUsername(req.username)
            ?: return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse("用户名或密码错误"))

        if (!passwordEncoder.matches(req.password, admin.password)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse("用户名或密码错误"))
        }

        val token = jwtUtil.generateAdminToken(admin.id)

        val response = mapOf(
            "token" to token,
            "role" to "admin"
        )

        return ResponseEntity.ok(ApiResponse("登录成功", response))
    }
}
