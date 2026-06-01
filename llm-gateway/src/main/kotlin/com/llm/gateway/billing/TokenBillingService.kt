package com.llm.gateway.billing

interface TokenBillingService {

    fun calculate(params: TokenBillingParams): TokenBillingResult
}
