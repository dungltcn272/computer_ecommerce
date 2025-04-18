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
                val products = productService.getAllProducts()
                call.respond(HttpStatusCode.OK, products)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error getting products: ${e.message}")
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
