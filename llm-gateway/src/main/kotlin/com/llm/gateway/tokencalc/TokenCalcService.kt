package com.llm.gateway.tokencalc

import com.llm.gateway.tokencalc.model.TokenEstimateParams
import com.llm.gateway.tokencalc.model.TokenEstimateResult

interface TokenCalcService {

    /**
     * 提供不同协议的token计算统一入口方法
     * @param [params] 参数
     * @return [TokenEstimateResult]
     */
    fun estimate(params: TokenEstimateParams): TokenEstimateResult
}
