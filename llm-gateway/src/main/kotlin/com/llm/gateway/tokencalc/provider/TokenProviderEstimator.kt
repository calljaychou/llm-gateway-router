package com.llm.gateway.tokencalc.provider

import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult

interface TokenProviderEstimator {

    fun supports(params: TokenEstimateParams): Boolean

    fun estimate(params: TokenEstimateParams): TokenEstimateResult
}
