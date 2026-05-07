package com.llm.gateway.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@RestController
@RequestMapping("/test")
class TestController {

    @GetMapping
    fun hello() = "hello"
}