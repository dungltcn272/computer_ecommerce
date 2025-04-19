package com.ltcn272.routes

import com.ltcn272.config.PayOSGateway
import com.ltcn272.data.model.WebhookData
import com.ltcn272.data.model.WebhookResponse
import com.ltcn272.services.OrderService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

fun Route.configurePayOSCallbackRoutes(orderService: OrderService) {
    post("/payment/callback") {
        val webhookData = try {
            call.receive<WebhookResponse>()
        } catch (e: Exception) {
            println("Error parsing JSON: ${e.message}")  // Log nếu có lỗi trong quá trình phân tích
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format"))
            return@post
        }

        println("Parsed Webhook Data: $webhookData")

        // Cập nhật trạng thái thanh toán và đơn hàng
        withContext(Dispatchers.IO) {
            try {
                if (webhookData.code == "00" && webhookData.desc.contains("successful", ignoreCase = true)) {
                    orderService.updateOrderStatus(webhookData.data.orderCode.toLong(), "CONFIRMED")
                    orderService.updatePaymentStatus(webhookData.data.orderCode.toLong(), "PAID")
                } else {
                    orderService.updateOrderStatus(webhookData.data.orderCode.toLong(), "PENDING")
                    orderService.updatePaymentStatus(webhookData.data.orderCode.toLong(), "FAILED")
                }
                call.respond(HttpStatusCode.OK, mapOf("success" to true))
            } catch (e: Exception) {
                println("Error processing webhook: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, mapOf("success" to false))
            }
        }
    }
}