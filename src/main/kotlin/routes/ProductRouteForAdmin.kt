package com.ltcn272.routes

import com.ltcn272.data.model.ProductRequest
import com.ltcn272.services.ProductService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import java.util.UUID

fun Route.productRoutesForAdmin(productService: ProductService) {
    route("/products") {
        post {
            val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val multipartData = call.receiveMultipart()
            var productRequest: ProductRequest? = null
            val fileUrls = mutableListOf<String>()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "product") {
                            productRequest = try {
                                Json.decodeFromString<ProductRequest>(part.value)
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.BadRequest, "Invalid product JSON")
                                return@forEachPart
                            }
                        }
                    }

                    is PartData.FileItem -> {
                        val fileUrl = productService.addProductMediaFile(adminId, part)
                        println(fileUrl)
                        fileUrl?.let { fileUrls.add(it) }
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (productRequest == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing product data")
                return@post
            }

            val productId = productService.createProduct(adminId, productRequest!!)
            fileUrls.map { url -> productService.addProductMediaTable(adminId, productId, url) }
            call.respond(HttpStatusCode.Created, "Product created with ID: $productId")

        }
        put("/{id}") {
            val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            println(adminId)
            val productId = call.parameters["id"]?.toLongOrNull()
            println(productId)
            val productRequest = call.receive<ProductRequest>()
            println(productRequest)
            if (productId != null) {
                val updated = productService.updateProduct(adminId, productId, productRequest)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Product updated")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
            }
        }
        delete("/{id}") {
            val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val productId = call.parameters["id"]?.toLongOrNull()
            if (productId != null) {
                val deleted = productService.deleteProduct(adminId, productId)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
            }
        }
        patch("/{id}/reduce-stock") {
            val productId = call.parameters["id"]?.toLongOrNull()
            val quantity = call.receive<Int>()
            if (productId != null) {
                val updated = productService.reduceStockQuantity(productId, quantity)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Stock quantity reduced")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
            }
        }
    }

}
