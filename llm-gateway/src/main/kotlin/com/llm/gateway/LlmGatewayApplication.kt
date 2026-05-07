package com.llm.gateway

import kotlin.jvm.java
import org.mybatis.spring.annotation.MapperScan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@SpringBootApplication
class LlmGatewayApplication

val log: Logger = LoggerFactory.getLogger(LlmGatewayApplication::class.java)

fun main(args: Array<String>) {
    runApplication<LlmGatewayApplication>(*args)

    log.info(
        "\n" + """
          ____ ______   _____ ________ ____     ____ __   ___ _____    __      _  _____   
         / __ (_  __ \ / ____(___  ___/ __ \   / ___() ) / __(_   _)  /  \    / )/ ___ \  
        ( (  ) )) ) \ ( (___     ) ) / /  \ \ / /   ( (_/ /    | |   / /\ \  / // /   \_) 
        ( (  ) ( (   ) \___ \   ( ( ( ()  () ( (    ()   (     | |   ) ) ) ) ) ( (  ____  
        ( (  ) )) )  ) )   ) )   ) )( ()  () ( (    () /\ \    | |  ( ( ( ( ( (( ( (__  ) 
        ( (__) / /__/ /___/ /   ( (  \ \__/ / \ \___( (  \ \  _| |__/ /  \ \/ / \ \__/ /  
         \____(______//____/    /__\  \____/   \____()_)  \_\/_____(_/    \__/   \____/   
                                                                                         
    """.trimIndent()
    )
}