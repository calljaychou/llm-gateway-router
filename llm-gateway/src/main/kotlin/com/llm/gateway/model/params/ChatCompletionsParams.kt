package com.llm.gateway.model.params

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@ApiModel("OpenAI Chat Completions 请求参数")
data class ChatCompletionsParams(
    @ApiModelProperty(value = "模型别名", required = true, example = "gpt-4-enterprise")
    @field:NotBlank(message = "model 不能为空")
    val model: String,
    @ApiModelProperty(value = "消息列表", required = true)
    @field:NotEmpty(message = "messages 不能为空")
    @field:Valid
    val messages: List<ChatMessageParams>,
)

@ApiModel("聊天消息")
data class ChatMessageParams(
    @ApiModelProperty(value = "角色", required = true, example = "user")
    @field:NotBlank(message = "role 不能为空")
    val role: String,
    @ApiModelProperty(value = "内容", required = true, example = "hello")
    @field:NotBlank(message = "content 不能为空")
    val content: String,
)
