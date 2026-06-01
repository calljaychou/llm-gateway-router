package com.llm.gateway.billing

import com.llm.gateway.common.logger
import com.llm.gateway.dal.mapper.ModelPriceRuleDynamicSqlSupport
import com.llm.gateway.dal.mapper.ModelPriceRuleMapper
import com.llm.gateway.dal.mapper.select
import java.math.BigDecimal
import org.springframework.stereotype.Component

interface ModelPriceRuleResolver {

    fun resolve(vendorId: Long, modelId: Long, currency: String = "CNY"): ModelPriceRuleSnapshot
}

@Component
class DbModelPriceRuleResolver(
    private val modelPriceRuleMapper: ModelPriceRuleMapper,
) : ModelPriceRuleResolver {

    private val log = logger()

    /**
     * 查询模型启用中的价格规则。
     * 价格规则以 modelId + chargeItem 为准，vendorId 用于防止跨供应商模型误用。
     */
    override fun resolve(vendorId: Long, modelId: Long, currency: String): ModelPriceRuleSnapshot {
        val records = modelPriceRuleMapper.select {
            where { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.vendorId isEqualTo vendorId }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.modelId isEqualTo modelId }
            and { ModelPriceRuleDynamicSqlSupport.ModelPriceRule.active isEqualTo true }
        }
        val rules = records.mapNotNull { record ->
            val chargeItem = record.chargeItem?.let { parseChargeItem(it) } ?: return@mapNotNull null
            val price = record.priceCnyPerMillion ?: BigDecimal.ZERO
            chargeItem to ModelPriceRuleItem(
                chargeItem = chargeItem,
                priceCnyPerMillion = price.max(BigDecimal.ZERO),
                pricingRule = "model_price_rule:${record.id ?: "unknown"}:${chargeItem.value}",
            )
        }.toMap()

        log.info(
            "模型价格规则加载 vendorId={}, modelId={}, currency={}, ruleCount={}",
            vendorId,
            modelId,
            currency,
            rules.size,
        )
        return ModelPriceRuleSnapshot(
            vendorId = vendorId,
            modelId = modelId,
            currency = currency,
            rules = rules,
        )
    }

    /** 将数据库 charge_item 字符串转换为枚举，未知值忽略并记录日志。 */
    private fun parseChargeItem(value: String): TokenChargeItem? {
        return runCatching { TokenChargeItem.valueOf(value) }
            .onFailure { log.warn("模型价格规则存在未知计费项 chargeItem={}", value) }
            .getOrNull()
    }
}
