package com.ltcn272.config

import com.ltcn272.config.AppConfig.BASE_URL
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object VNPayGateway {
    const val vnpTmnCode = "XPE2S1K3"
    private const val vnpHashSecret = "ZAHAE5X44ZMV5K6YS5Y9ZMGKOT2372L3"
    private const val vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    private val vnpReturnUrl = "$BASE_URL/orders/vnpay_return"

    fun createPaymentUrl(orderId: Long, amount: BigDecimal, clientIp: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT+7")

        val currentTime = dateFormat.format(Date())
        val calendar = Calendar.getInstance().apply {
            time = Date()
            add(Calendar.MINUTE, 15)
        }
        val expireTime = dateFormat.format(calendar.time)

        val vnpParams = mutableMapOf<String, String>().apply {
            put("vnp_Version", "2.1.0")
            put("vnp_Command", "pay")
            put("vnp_TmnCode", vnpTmnCode)
            put("vnp_Amount", (amount.multiply(BigDecimal(100)).toBigInteger()).toString())
            put("vnp_CurrCode", "VND")
            put("vnp_TxnRef", orderId.toString())
            put("vnp_OrderInfo", "Thanh toan đon hang $orderId") // Dùng khoảng trắng đúng format
            put("vnp_OrderType", "other") // Đổi thành mã danh mục hợp lệ
            put("vnp_Locale", "vn")
            put("vnp_ReturnUrl", vnpReturnUrl)
            put("vnp_CreateDate", currentTime)
            put("vnp_ExpireDate", expireTime)
            put("vnp_IpAddr", clientIp) // Dùng đúng IP của người dùng
        }

        // 🔹 **Bước 1: Sắp xếp tham số theo thứ tự alphabet**
        val sortedParams = vnpParams.toSortedMap()

        // 🔹 **Bước 2: Tạo query string**
        val hashData = sortedParams.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.US_ASCII.toString())}"
        }

        // 🔹 **Bước 4: Tạo `vnp_SecureHash` bằng HMAC SHA512**
        val secureHash = hmacSHA512(vnpHashSecret, hashData)

        return "$vnpUrl?$hashData&vnp_SecureHash=$secureHash"
    }

    fun hmacSHA512(key: String, data: String): String {
        val hmac512 = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA512")
        hmac512.init(secretKey)
        val result = hmac512.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return result.joinToString("") { "%02x".format(it) }
    }
}
