package com.ltcn272

import com.ltcn272.config.AppConfig
import com.ltcn272.data.database.DatabaseFactory
import com.ltcn272.plugins.configureRouting
import com.ltcn272.plugins.configureSecurity
import com.ltcn272.plugins.configureSerialization
import io.ktor.server.application.*
fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    AppConfig.config(environment.config)
    configureSerialization()
    configureSecurity()
    configureRouting()
}
