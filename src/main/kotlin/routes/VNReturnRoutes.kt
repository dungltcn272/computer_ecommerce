package com.ltcn272.routes

import com.ltcn272.services.OrderService
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*

fun Route.vnPayReturnRoutes(orderService: OrderService, vnp_HashSecret: String) {
    get("/orders/vnpay_return") {
        val params = call.request.queryParameters.toMap().toMutableMap()
        val secureHash = params.remove("vnp_SecureHash")?.firstOrNull() // Lấy và xóa hash để kiểm tra

        if (secureHash.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing vnp_SecureHash")
            return@get
        }

        // 🔹 Bước 1: Sắp xếp tham số theo thứ tự a-z (loại bỏ tham số hash)
        val sortedParams = params.entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value.first()}" }

        // 🔹 Bước 2: Tạo chữ ký HMAC-SHA512
        val computedHash = hmacSHA512(vnp_HashSecret, sortedParams)

        // 🔹 Bước 3: Kiểm tra chữ ký
        if (!secureHash.equals(computedHash, ignoreCase = true)) {
            call.respond(HttpStatusCode.BadRequest, "Invalid signature")
            return@get
        }

        // 🔹 Bước 4: Lấy orderId và kiểm tra trạng thái giao dịch
        val orderId = params["vnp_TxnRef"]?.firstOrNull()
        val responseCode = params["vnp_ResponseCode"]?.firstOrNull()
        val transactionStatus = params["vnp_TransactionStatus"]?.firstOrNull()

        if (orderId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
            return@get
        }

        // Kiểm tra mã phản hồi và trạng thái giao dịch
        val paymentStatus = if (responseCode == "00" && transactionStatus == "00") "PAID" else "FAILED"

        // Cập nhật trạng thái đơn hàng
        orderService.updatePaymentStatus(orderId.toLong(), paymentStatus)

        // Trả kết quả
        call.respond(HttpStatusCode.OK, mapOf("orderId" to orderId, "status" to paymentStatus))
    }
}

// 🔹 Hàm tạo HMAC-SHA512
fun hmacSHA512(secret: String, data: String): String {
    val hmac = Mac.getInstance("HmacSHA512")
    val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA512")
    hmac.init(secretKey)
    return hmac.doFinal(data.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}