package com.llm.gateway.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import com.llm.gateway.security.CustomUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expirationSeconds}") private val expirationSeconds: Long,
) {

    private val signingKey: Key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray().also {
            if (it.size < 32) throw IllegalStateException("jwt.secret length must be at least 32 bytes")
        })
    }

    fun generateToken(userDetails: UserDetails): String {
        val now = Date()
        val expiry = Date(now.time + expirationSeconds * 1000)
        val roles = userDetails.authorities.map(GrantedAuthority::getAuthority)
        val userId = (userDetails as? CustomUserDetails)?.getUser()?.id

        val builder = Jwts.builder()
            .setSubject(userDetails.username)
            .claim("uid", userId)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(signingKey, SignatureAlgorithm.HS256)

        return builder.compact()
    }

    fun getUsername(token: String): String {
        return getClaims(token).subject
    }

    fun getUserId(token: String): Long? {
        val uid = getClaims(token)["uid"] ?: return null
        return when (uid) {
            is Int -> uid.toLong()
            is Long -> uid
            is String -> uid.toLongOrNull()
            else -> null
        }
    }

    fun getRoles(token: String): List<String> {
        val roles = getClaims(token)["roles"]
        return when (roles) {
            is List<*> -> roles.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (_: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}