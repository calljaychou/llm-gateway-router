package com.llm.gateway.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = ["swagger.enable"], havingValue = "true")
class SwaggerConfig {

    @Bean
    fun docket(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(
                ApiInfoBuilder()
                    .title("接口文档")
                    .version("v1.0")
                    .description("多看，多做，多思考")
                    .build()
            )
            .select() //指定接口的位置
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
            .paths(PathSelectors.any())
            .build()
    }

}