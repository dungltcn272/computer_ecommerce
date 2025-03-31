package com.ltcn272.config

import io.ktor.server.config.*

object AppConfig {


    fun config(appConfig: ApplicationConfig) {
        BASE_URL = appConfig.tryGetString("app.baseUrl") ?: throw Exception("Missing baseUrl")
        apiKey = appConfig.tryGetString("security.apiKey") ?: throw Exception("Missing apiKey")
        secretPasswordKey = appConfig.tryGetString("security.secretPasswordKey") ?: throw Exception("Missing secretPasswordKey")
        jwtDomain = appConfig.tryGetString("jwt.domain") ?: throw Exception("Missing jwt.domain")
        jwtAudience = appConfig.tryGetString("jwt.audience") ?: throw Exception("Missing jwt.audience")
        jwtRealm = appConfig.tryGetString("jwt.realm") ?: throw Exception("Missing jwt.realm")
        jwtSecret = appConfig.tryGetString("jwt.secret") ?: throw Exception("Missing jwt.secret")
    }
    lateinit var BASE_URL : String
    lateinit var apiKey: String
    lateinit var secretPasswordKey : String
    lateinit var jwtDomain: String
    lateinit var jwtAudience: String
    lateinit var jwtRealm: String
    lateinit var jwtSecret: String

}
