package com.llm.gateway.controller

import com.llm.gateway.common.exceptions.BizException
import com.llm.gateway.dal.mapper.UsersDynamicSqlSupport
import com.llm.gateway.dal.mapper.UsersMapper
import com.llm.gateway.dal.mapper.update
import com.llm.gateway.model.ApiResult
import com.llm.gateway.model.params.LoginParams
import com.llm.gateway.model.results.LoginResult
import com.llm.gateway.security.CustomUserDetails
import com.llm.gateway.security.jwt.JwtTokenProvider
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import java.util.Date
import javax.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["认证"])
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val usersMapper: UsersMapper,
) {

    @Value("\${jwt.expirationSeconds:86400}")
    private var expirationSeconds: Long = 86400

    @ApiOperation(value = "登录并获取JWT Token")
    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginParams): ApiResult<LoginResult> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )

            val userDetails = authentication.principal as CustomUserDetails
            val token = jwtTokenProvider.generateToken(userDetails)
            val roles = userDetails.authorities.map(GrantedAuthority::getAuthority)
            val user = userDetails.getUser()
            recordFirstUseTime(user.id, user.useTime)

            ApiResult.success(
                LoginResult(
                    token = token,
                    tokenType = "Bearer",
                    expiresInSeconds = expirationSeconds,
                    userId = user.id!!,
                    username = user.username ?: user.email ?: "",
                    email = user.email,
                    roles = roles,
                    passwordChanged = user.passwordChanged == true,
                    useTime = user.useTime
                )
            )
        } catch (_: BadCredentialsException) {
            throw BizException(BizException.UNAUTHORIZED, "用户名或密码错误")
        }
    }

    /**
     * 记录用户首次登录系统的时间，已有使用时间时不覆盖。
     */
    private fun recordFirstUseTime(userId: Long?, useTime: Date?) {
        if (userId == null || useTime != null) return
        usersMapper.update {
            set(UsersDynamicSqlSupport.Users.useTime).equalTo(Date())
            where { UsersDynamicSqlSupport.Users.id isEqualTo userId }
            and { UsersDynamicSqlSupport.Users.useTime.isNull() }
        }
    }
}
