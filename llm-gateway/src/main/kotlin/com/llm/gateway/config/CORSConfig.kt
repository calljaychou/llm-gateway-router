package com.llm.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CORSConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration()
        config.addAllowedOrigin("*")
        //是否发送Cookie信息
        config.allowCredentials = true
        //放行哪些原始域(请求方式)
        config.addAllowedMethod("*")
        //放行哪些原始域(头部信息)
        config.addAllowedHeader("*")

        //2.添加映射路径
        val configSource = UrlBasedCorsConfigurationSource()
        configSource.registerCorsConfiguration("/**", config)

        return CorsFilter(configSource)
    }
}