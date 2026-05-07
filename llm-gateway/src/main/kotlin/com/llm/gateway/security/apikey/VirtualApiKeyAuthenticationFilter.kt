package com.llm.gateway.security.apikey

import com.llm.gateway.service.VirtualApiKeyService
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class VirtualApiKeyAuthenticationFilter(
    private val virtualApiKeyService: VirtualApiKeyService,
) : OncePerRequestFilter() {

    companion object {
        private const val VIRTUAL_API_KEY_PREFIX = "sk-vkey-"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val virtualApiKey = resolveVirtualApiKey(request)
        if (virtualApiKey != null && SecurityContextHolder.getContext().authentication == null) {
            val userDetails = virtualApiKeyService.authenticateByVirtualKey(virtualApiKey)
            if (userDetails != null) {
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/v1/")
    }

    private fun resolveVirtualApiKey(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        val token = header.removePrefix("Bearer ").trim()
        if (!token.startsWith(VIRTUAL_API_KEY_PREFIX)) return null
        return token.ifEmpty { null }
    }
}