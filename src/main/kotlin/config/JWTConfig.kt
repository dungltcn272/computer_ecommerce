package com.ltcn272.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ltcn272.config.AppConfig.jwtAudience
import com.ltcn272.config.AppConfig.jwtDomain
import com.ltcn272.config.AppConfig.jwtSecret
import com.ltcn272.data.model.User
import java.util.*

object JWTConfig {
    private const val expiration = 86400 * 30 * 1000L

    fun generateToken(user: User): String {
        return JWT.create()
            .withIssuer(jwtDomain)
            .withAudience(jwtAudience)
            .withClaim("id", user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role)
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(Date(System.currentTimeMillis() + expiration))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}