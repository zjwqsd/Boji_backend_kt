package com.boji.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // 映射静态文件访问路径，如：http://localhost:8080/files/xxx.jpg
        registry.addResourceHandler("/files/**")
            .addResourceLocations("file:./uploads/")
    }
}