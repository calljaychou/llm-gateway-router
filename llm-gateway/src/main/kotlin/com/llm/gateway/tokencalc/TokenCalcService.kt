package com.llm.gateway.tokencalc

import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult

interface TokenCalcService {

    fun estimate(params: TokenEstimateParams): TokenEstimateResult
}
