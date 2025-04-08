package com.boji.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // 禁用 CSRF，前后端分离用不到
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll() // 登录、注册接口放行
                    .anyRequest().permitAll() // 其余全部放行（你也可以改成 authenticated）
            }
            .formLogin { it.disable() } // 关闭默认登录页面
            .httpBasic { it.disable() } // 关闭 Basic 认证

        return http.build()
    }
}
