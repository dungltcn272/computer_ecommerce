package com.ltcn272.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ltcn272.config.AppConfig.jwtAudience
import com.ltcn272.config.AppConfig.jwtDomain
import com.ltcn272.config.AppConfig.jwtRealm
import com.ltcn272.config.AppConfig.jwtSecret
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("id").asString() // Lấy ID từ JWT
                if (userId != null) UserIdPrincipal(userId) else null
            }
        }
    }
    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = "265968426726-3bgdoqo633rbvood0o2avcdl408jsl2g.apps.googleusercontent.com",
                    clientSecret = "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",
                    defaultScopes = listOf(
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/userinfo.email"
                    ),
                    extraAuthParameters = listOf("access_type" to "offline"),
                )
            }
            client = HttpClient(CIO)
        }
    }
}

