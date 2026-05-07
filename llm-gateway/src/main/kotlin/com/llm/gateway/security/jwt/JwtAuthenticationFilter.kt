package com.llm.gateway.security.jwt

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import com.llm.gateway.security.CustomUserDetailsService
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token) && SecurityContextHolder.getContext().authentication == null) {
            val roles = jwtTokenProvider.getRoles(token)
            val userDetails = jwtTokenProvider.getUserId(token)?.let { uid ->
                customUserDetailsService.loadUserById(uid, roles)
            } ?: run {
                val username = jwtTokenProvider.getUsername(token)
                customUserDetailsService.loadUserByUsername(username)
            }
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities).apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ")) return null
        val token = header.replace("Bearer ", "").trim()
        if (token.startsWith("sk-vkey-")) return null
        return token.ifEmpty { null }
    }
}
