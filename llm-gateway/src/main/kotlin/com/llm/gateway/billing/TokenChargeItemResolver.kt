package com.llm.gateway.billing

import com.llm.gateway.tokencalc.model.TokenCacheType
import com.llm.gateway.tokencalc.model.TokenDetailDto
import com.llm.gateway.tokencalc.model.TokenDirection
import com.llm.gateway.tokencalc.model.TokenType
import org.springframework.stereotype.Component

@Component
class TokenChargeItemResolver {

    /**
     * 将 Token 明细映射为计费项。
     * 计费项只由 direction、tokenType、cacheType 和 prediction 标记决定，不读取价格。
     */
    fun resolve(detail: TokenDetailDto): TokenChargeItem {
        return when (detail.direction) {
            TokenDirection.INPUT -> resolveInputChargeItem(detail)
            TokenDirection.OUTPUT -> resolveOutputChargeItem(detail)
        }
    }

    /** 解析输入方向计费项。 */
    private fun resolveInputChargeItem(detail: TokenDetailDto): TokenChargeItem {
        return when {
            detail.tokenType == TokenType.AUDIO -> TokenChargeItem.INPUT_AUDIO
            detail.tokenType == TokenType.IMAGE -> TokenChargeItem.INPUT_IMAGE
            detail.cacheType == TokenCacheType.CACHE_HIT -> TokenChargeItem.INPUT_CACHE_HIT
            detail.cacheType == TokenCacheType.CACHE_MISS -> TokenChargeItem.INPUT_CACHE_MISS
            detail.cacheType == TokenCacheType.CACHE_WRITE -> TokenChargeItem.INPUT_CACHE_WRITE
            else -> TokenChargeItem.INPUT_TEXT
        }
    }

    /** 解析输出方向计费项。 */
    private fun resolveOutputChargeItem(detail: TokenDetailDto): TokenChargeItem {
        return when (detail.tokenType) {
            TokenType.REASONING -> TokenChargeItem.OUTPUT_REASONING
            TokenType.AUDIO -> TokenChargeItem.OUTPUT_AUDIO
            TokenType.PREDICTION -> resolvePredictionChargeItem(detail)
            else -> TokenChargeItem.OUTPUT_TEXT
        }
    }

    /** 区分 accepted/rejected prediction。 */
    private fun resolvePredictionChargeItem(detail: TokenDetailDto): TokenChargeItem {
        val marker = "${detail.note.orEmpty()} ${detail.providerField.orEmpty()}".lowercase()
        return when {
            marker.contains("rejected") -> TokenChargeItem.OUTPUT_REJECTED_PREDICTION
            marker.contains("accepted") -> TokenChargeItem.OUTPUT_ACCEPTED_PREDICTION
            else -> TokenChargeItem.OUTPUT_TEXT
        }
    }
}
