package com.ltcn272.routes

import com.ltcn272.config.PayOSGateway
import com.ltcn272.data.model.WebhookData
import com.ltcn272.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.configurePayOSCallbackRoutes(orderService: OrderService) {
    post("/payment/callback") {
        val webhookData = call.receive<WebhookData>()

        // Xác minh chữ ký
        if (!PayOSGateway.verifyWebhookSignature(webhookData)) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid signature"))
            return@post
        }

        // Cập nhật trạng thái thanh toán và đơn hàng
        withContext(Dispatchers.IO) {
            when (webhookData.status) {
                "PAID" -> {
                    orderService.updateOrderStatus(webhookData.orderId, "CONFIRMED")
                    orderService.updatePaymentStatus(webhookData.orderId, "PAID")
                }

                "CANCELLED" -> {
                    orderService.updateOrderStatus(webhookData.orderId, "PENDING")
                    orderService.updatePaymentStatus(webhookData.orderId, "FAILED")
                }
                else ->{}
            }
        }

        call.respond(HttpStatusCode.OK, mapOf("message" to "Webhook processed"))
    }
}