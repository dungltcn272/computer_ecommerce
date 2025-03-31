package com.ltcn272.routes

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import com.ltcn272.services.AuthService
import io.ktor.client.call.*

fun Route.authRoutes(authService: AuthService, httpClient: HttpClient) {
    post("/register") {
        call.respond(HttpStatusCode.Created, authService.register(call.receive()))
    }

    post("/login") {
        call.respond(HttpStatusCode.OK, authService.login(call.receive()))
    }

    authenticate("auth-oauth-google") {
        get("/google_login") { call.respondRedirect("/callback") }

        get("/callback") {
            call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()?.let { principal ->
                runCatching { fetchGoogleUserInfo(httpClient, principal.accessToken) }
                    .onSuccess { userInfo ->
                        call.respond(HttpStatusCode.OK, authService.authenticateGoogleUser(userInfo.email, userInfo.name, userInfo.picture))
                    }
                    .onFailure { call.respond(HttpStatusCode.InternalServerError, "Lỗi khi lấy thông tin từ Google: ${it.localizedMessage}") }
            } ?: call.respond(HttpStatusCode.Unauthorized, "OAuth thất bại")
        }
    }
}

@Serializable
data class GoogleUserInfo(val email: String, val name: String, val picture: String?)

suspend fun fetchGoogleUserInfo(httpClient: HttpClient, accessToken: String): GoogleUserInfo =
    httpClient.get("https://www.googleapis.com/oauth2/v1/userinfo") {
        headers.apply { append(HttpHeaders.Authorization, "Bearer $accessToken") }
    }.takeIf { it.status == HttpStatusCode.OK }?.body()
        ?: throw Exception("Google API trả về lỗi")