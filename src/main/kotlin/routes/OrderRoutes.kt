package com.ltcn272.routes

import com.ltcn272.config.AppConfig.BASE_URL
import com.ltcn272.config.PayOSGateway
import com.ltcn272.data.model.OrderCancelRequest
import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderUpdateRequest
import com.ltcn272.services.OrderService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.orderRoutes(orderService: OrderService) {

    route("/orders") {
        post {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderRequest = call.receive<List<OrderItemRequest>>()

            val orderResponse = orderService.createOrder(userId, orderRequest)

            call.respond(HttpStatusCode.OK, orderResponse)
        }
        get {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orders = orderService.getOrdersByUser(userId)
            call.respond(HttpStatusCode.OK, orders)
        }

        get("/{orderId}") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                return@get
            }

            val order = orderService.getOrderById(orderId, userId)
            if (order != null) {
                call.respond(HttpStatusCode.OK, order)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Order not found"))
            }
        }

        put("/{orderId}") {
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                return@put
            }

            val updateRequest = call.receive<OrderUpdateRequest>()
            val isUpdated = orderService.updateOrderStatus(orderId, updateRequest.status)

            if (isUpdated) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Order status updated successfully"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to update order status"))
            }
        }

        delete("/{orderId}") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                return@delete
            }

            val cancelRequest = call.receive<OrderCancelRequest>()
            val isCancelled = orderService.cancelOrder(orderId, userId, cancelRequest.reason)

            if (isCancelled) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Order cancelled successfully"))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to cancel order"))
            }
        }
        // Route tạo link thanh toán PayOS
        post("/{orderId}/payment") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                return@post
            }
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                return@post
            }

            try {
                val order = orderService.getOrderById(orderId, userId)
                if (order == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Order not found"))
                    return@post
                }

                if (order.paymentStatus != "UNPAID") {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Order already processed"))
                    return@post
                }

                // Chuyển totalPrice (Decimal) sang Int (đơn vị VND)
                val amount = order.totalPrice.toInt()

                // Tạo link thanh toán
                val paymentResponse = PayOSGateway.createPaymentLink(
                    orderCode = orderId,
                    amount = 5000,
                    description = "Thanh toán đơn hàng #$orderId",
                    returnUrl = "$BASE_URL/success?orderId=$orderId",
                    cancelUrl = "$BASE_URL/cancel?orderId=$orderId",
                    buyerName = null,
                    buyerEmail = null,
                    buyerPhone = null
                )
                call.respond(HttpStatusCode.OK, paymentResponse)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
