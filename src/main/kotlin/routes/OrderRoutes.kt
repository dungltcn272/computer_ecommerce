package com.ltcn272.routes

import com.ltcn272.config.VNPayGateway
import com.ltcn272.data.model.OrderCancelRequest
import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderUpdateRequest
import com.ltcn272.data.model.PaymentRequest
import com.ltcn272.services.OrderService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.orderRoutes(orderService: OrderService) {

    vnPayReturnRoutes(orderService, VNPayGateway.vnpTmnCode)
    route("/orders") {
        post {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderRequest = call.receive<List<OrderItemRequest>>()

            val isSuccess = orderService.createOrder(userId, orderRequest)

            if (isSuccess) {
                call.respond(
                    HttpStatusCode.Created, mapOf("message" to "Order created successfully")
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create order"))
            }
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
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid order ID"))
                return@put
            }

            val updateRequest = call.receive<OrderUpdateRequest>()
            val isUpdated = orderService.updateOrderStatus(orderId, userId, updateRequest.status)

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
        post("/{orderId}/pay") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val orderId = call.parameters["orderId"]?.toLongOrNull()

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                return@post
            }

            val paymentMethod = call.receive<PaymentRequest>().method
            when (paymentMethod) {
                "COD" -> {
                    val response = orderService.processCODPayment(orderId)
                    call.respond(HttpStatusCode.OK, response)
                }

                "VNPAY" -> {
                    val order = orderService.getOrderById(orderId, userId)
                    if (order == null) {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                        return@post
                    }
                    val clientIp = call.request.origin.remoteHost
                    val paymentUrl = orderService.processVNPayPayment(orderId, order.totalPrice, clientIp)
                    call.respond(HttpStatusCode.OK, mapOf("paymentUrl" to paymentUrl))
                }

                "VietQR" -> {

                }

                else -> call.respond(HttpStatusCode.BadRequest, "Invalid payment method")
            }
        }
    }
}
