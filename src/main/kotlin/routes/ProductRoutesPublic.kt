package com.ltcn272.routes

import com.ltcn272.services.ProductService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File


fun Route.productRoutesPublic(productService: ProductService) {
    route("/products") {
        get {
            try {
                val queryParameters = call.request.queryParameters
                if (queryParameters.isEmpty()) {
                    val products = productService.getAllProducts()
                    call.respond(HttpStatusCode.OK, products)
                } else {
                    println("GetProductsPaginated")
                    val page = queryParameters["page"]?.toIntOrNull() ?: 1
                    val limit = queryParameters["limit"]?.toIntOrNull() ?: 20
                    val category = queryParameters["category"]
                    val sortBy = queryParameters["sortBy"]
                    val order = queryParameters["order"]
                    val search = queryParameters["search"]

                    val response = productService.getProductsPaginated(
                        page = page,
                        limit = limit,
                        category = category,
                        sortBy = sortBy,
                        order = order,
                        search = search
                    )
                    call.respond(HttpStatusCode.OK, response)
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error getting products: ${e.message}")
                )
            }
        }
        get("/{id}") {
            val productId = call.parameters["id"]?.toLongOrNull()
            if (productId != null) {
                val product = productService.getProductById(productId)
                if (product != null) {
                    call.respond(product)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
            }
        }
        get("/media/{filename}") {
            val fileName = call.parameters["filename"]
            if (fileName != null) {
                val filePath = "uploads/products/$fileName"
                val file = File(filePath)
                println(filePath)

                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "File not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid parameters")
            }
        }
    }

}
