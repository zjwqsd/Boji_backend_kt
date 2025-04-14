package com.boji.backend.exception

import com.boji.backend.response.ApiResponse
//import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import jakarta.validation.ConstraintViolationException

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


    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        val path = ex.resourcePath
//        println(path)
        val message = when {
            path.startsWith("/files/") || path.startsWith("/static/") -> "静态资源不存在: $path"
            path.startsWith("api/") -> "接口不存在: $path"
            else -> "资源不存在: $path"
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(message))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        val supportedMethods = ex.supportedHttpMethods?.joinToString("/") ?: "未知"
        val message = "请求方法 ${ex.method} 不被支持，支持的方法有：$supportedMethods"
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse(message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Nothing>> {
        val msg = "缺少必要参数：${ex.parameterName}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse(msg))
    }


    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse("请求体格式错误，请检查 JSON 格式"))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ApiResponse<Nothing>> {
        val msg = ex.constraintViolations.joinToString("; ") { it.message }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse("参数校验失败：$msg"))
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
