package com.llm.gateway.common.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * @author JayCHou <a href="calljaychou@qq.com">Email</a>
 */
@Component
class SpringContextUtil : ApplicationContextAware {

    companion object {
        lateinit var applicationContext: ApplicationContext
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

}