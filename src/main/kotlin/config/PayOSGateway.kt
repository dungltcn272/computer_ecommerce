package com.ltcn272.config

import com.ltcn272.data.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PayOSGateway {
    private const val CLIENT_ID = "38aa4f3c-998f-4c24-af6d-be6366ae6288"
    private const val API_KEY = "577822e9-5d21-41d3-98c4-1132ca6a2481"
    private const val CHECKSUM_KEY = "7670dcd4b8c0e8d124212e5fdd1b9e47bc5da33bc2a99eedcb435a40215cec84"

    private const val PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests"

    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }


    suspend fun createPaymentLink(
        orderCode: Long , // Đổi từ orderId thành orderCode
        amount: Int,
        description: String,
        returnUrl: String,
        cancelUrl: String,
        buyerName: String? = null,
        buyerEmail: String? = null,
        buyerPhone: String? = null,
        buyerAddress: String? = null, // Thêm buyerAddress
        items: List<ProductResponse>? = null, // Thêm items
        expiredAt: Long? = null // Thêm expiredAt
    ): PaymentResponse {
        // Tạo chuỗi dữ liệu theo thứ tự alphabet
        val dataString = "amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl"
        // Tính toán signature
        val signature = hmacSha256(CHECKSUM_KEY, dataString)

        val paymentRequest = PaymentRequest(
            orderCode = orderCode,
            amount = amount,
            description = description,
            returnUrl = returnUrl,
            cancelUrl = cancelUrl,
            signature = signature,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            buyerPhone = buyerPhone,
            buyerAddress = buyerAddress,
            items = items,
            expiredAt = expiredAt
        )

        println(paymentRequest)


        val response = client.post(PAYOS_API_URL) {
            contentType(ContentType.Application.Json)
            header("x-client-id", CLIENT_ID)
            header("x-api-key", API_KEY)
            setBody(paymentRequest)
        }

        val responseText = response.bodyAsText()
        println(responseText)

        val payOSPaymentResponse = Json.decodeFromString<PayOSPaymentResponse>(responseText)

        // Nếu thành công mới parse JSON thành PaymentResponse
        return PaymentResponse(
            checkoutUrl = payOSPaymentResponse.data.checkoutUrl,
            orderId = payOSPaymentResponse.data.orderCode,
            status = payOSPaymentResponse.data.status
        )
    }


    // Hàm mã hóa HMAC-SHA256
    private fun hmacSha256(key: String, data: String): String {
        val algorithm = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}