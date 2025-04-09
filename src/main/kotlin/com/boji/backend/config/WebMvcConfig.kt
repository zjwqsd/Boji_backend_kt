package com.boji.backend.config

import com.boji.backend.security.AdminAccessInterceptor
import com.boji.backend.security.CurrentUserArgumentResolver
import com.boji.backend.security.RoleAccessInterceptor
import com.boji.backend.security.UserAccessInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val adminAccessInterceptor: AdminAccessInterceptor,
    private val userAccessInterceptor: UserAccessInterceptor,
    private val roleAccessInterceptor: RoleAccessInterceptor,
    private val currentUserArgumentResolver: CurrentUserArgumentResolver
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(adminAccessInterceptor)
            .addPathPatterns("/api/**") // 拦截所有接口，根据注解决定是否校验
        registry.addInterceptor(userAccessInterceptor)
            .addPathPatterns("/api/**")
        registry.addInterceptor(roleAccessInterceptor)
            .addPathPatterns("/api/**")
    }


    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}