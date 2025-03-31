package com.ltcn272.routes

import com.ltcn272.data.model.CartItemRequest
import com.ltcn272.services.CartService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlin.text.get

fun Route.cartRoute(cartService: CartService) {
    route("/cart") {
        post {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val request = call.receive<CartItemRequest>()
            val response = cartService.addItemToCart(userId, request)
            if (response != null) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to add item")
            }
        }

        get {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val cartItems = cartService.getUserCart(userId)
            call.respond(HttpStatusCode.OK, cartItems)
        }

        put("/{itemId}") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val itemId = call.parameters["itemId"]?.toLongOrNull()
            val request = call.receive<CartItemRequest>()

            if (itemId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid item ID")
                return@put
            }

            val updated = cartService.updateCartItem(userId, itemId, request.quantity)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Updated successfully")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to update item")
            }
        }

        delete("/{itemId}") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val itemId = call.parameters["itemId"]?.toLongOrNull()

            if (itemId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid item ID")
                return@delete
            }

            val removed = cartService.removeCartItem(userId, itemId)
            if (removed) {
                call.respond(HttpStatusCode.OK, "Item removed successfully")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to remove item")
            }
        }

        delete("/clear") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val cleared = cartService.clearCart(userId)
            if (cleared) {
                call.respond(HttpStatusCode.OK, "Cart cleared successfully")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to clear cart")
            }
        }
    }

}
