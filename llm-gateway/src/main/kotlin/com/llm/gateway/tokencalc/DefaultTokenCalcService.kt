package com.llm.gateway.tokencalc

import com.llm.gateway.common.logger
import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult
import com.llm.gateway.tokencalc.model.TokenEstimateSource
import com.llm.gateway.tokencalc.provider.TokenProviderEstimator
import org.springframework.stereotype.Service

@Service
class DefaultTokenCalcService(
    private val estimators: List<TokenProviderEstimator>,
) : TokenCalcService {

    private val log = logger()

    /**
     * 根据协议选择对应的 Token 估算器。
     * Token 计算层只负责生成可审计的 usage 和 detail，不参与计费、配额和落库。
     */
    override fun estimate(params: TokenEstimateParams): TokenEstimateResult {
        val estimator = estimators.firstOrNull { it.supports(params) }
            ?: return unsupportedResult(params, "当前协议暂不支持 Token 计算").also {
                log.warn(
                    "Token计算不支持当前协议 protocol={}, requestModel={}, upstreamModel={}, stream={}",
                    params.protocol.value,
                    params.requestModel,
                    params.upstreamModel,
                    params.stream,
                )
            }
        log.info(
            "Token计算选择估算器 protocol={}, estimator={}, requestModel={}, upstreamModel={}, stream={}",
            params.protocol.value,
            estimator.javaClass.simpleName,
            params.requestModel,
            params.upstreamModel,
            params.stream,
        )
        return estimator.estimate(params)
    }

    /** 构造不支持计算时的统一返回结果。 */
    private fun unsupportedResult(params: TokenEstimateParams, note: String): TokenEstimateResult {
        return TokenEstimateResult(
            resolvedModel = params.upstreamModel ?: params.requestModel.orEmpty(),
            source = TokenEstimateSource.UNSUPPORTED,
            supported = false,
            note = note,
        )
    }
}
