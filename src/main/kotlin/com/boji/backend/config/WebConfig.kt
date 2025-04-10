package com.boji.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val coverPath = Paths.get("uploads/covers").toAbsolutePath().toUri().toString()
        println("Mapped static path: $coverPath")

        registry.addResourceHandler("/files/covers/**") // 仅映射 covers 子目录
            .addResourceLocations(coverPath)
    }
}