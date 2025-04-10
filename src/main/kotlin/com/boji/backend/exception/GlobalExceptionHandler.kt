package com.boji.backend.exception

import com.boji.backend.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * 表单字段验证失败（如 @Valid 校验不通过）
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "无效参数") }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse("参数校验失败", errors))
    }

    /**
     * URL 参数类型错误，如 ?page=abc 而非 int
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "参数 '${ex.name}' 类型错误，应为 ${ex.requiredType?.simpleName}"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(message))
    }

    /**
     * 找不到接口路径
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(ex: NoHandlerFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse("接口不存在: ${ex.requestURL}"))
    }

    /**
     * 非法参数抛出
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(ex.message ?: "参数错误"))
    }

    class BusinessException(message: String) : RuntimeException(message)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(ex.message ?: "业务异常"))
    }

    class NotFoundException(message: String) : RuntimeException(message)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(ex.message ?: "资源不存在"))
    }
    /**
     * 通用异常处理（兜底）
     */
    @ExceptionHandler(Exception::class)
    fun handleServerError(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        ex.printStackTrace()
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse("服务器错误，请稍后再试"))
    }
}
