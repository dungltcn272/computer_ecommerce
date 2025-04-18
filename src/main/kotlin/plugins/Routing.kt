package com.ltcn272.plugins

import com.ltcn272.data.repository.cart.CartRepositoryImpl
import com.ltcn272.data.repository.category.CategoryRepositoryImpl
import com.ltcn272.data.repository.order.OrderRepositoryImpl
import com.ltcn272.data.repository.product.ProductRepositoryImpl
import com.ltcn272.data.repository.user.UserRepositoryImpl
import com.ltcn272.routes.*
import com.ltcn272.services.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: ${cause.message}" , status = HttpStatusCode.InternalServerError)
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid request")
        }
    }
    routing {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val userRepository = UserRepositoryImpl()
        val authService = AuthService(userRepository)
        authRoutes(authService, client)
        val productService = ProductService(ProductRepositoryImpl(), userRepository)
        productRoutesPublic(productService)
        get { call.respond("Hello Dung") }
        authenticate("auth-jwt", "auth-oauth-google") {
            userRoutes(UserService(userRepository))
            categoryRoutes(CategoryService(CategoryRepositoryImpl()))
            productRoutesForAdmin(productService)
            cartRoute(CartService(CartRepositoryImpl()))
            orderRoutes(OrderService(OrderRepositoryImpl()))
        }
    }
}
