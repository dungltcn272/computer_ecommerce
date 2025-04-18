package com.ltcn272.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val orderCode: Long, // Đổi từ orderId thành orderCode
    val amount: Int,
    val description: String,
    val cancelUrl: String,
    val returnUrl: String,
    val buyerName: String? = null,
    val buyerEmail: String? = null,
    val buyerPhone: String? = null,
    val buyerAddress: String? = null, // Thêm trường buyerAddress
    val items: List<ProductResponse>? = null, // Thêm trường items
    val expiredAt: Long? = null, // Thêm trường expiredAt
    val signature: String
)

@Serializable
data class PaymentResponse(
    val checkoutUrl: String,
    val orderId: Long,
    val status: String
)

@Serializable
data class PayOSPaymentResponse(
    val code: String,          // Mã trạng thái (ví dụ: "00")
    val desc: String,          // Mô tả (ví dụ: "Success - Thành công")
    val data: PaymentData,     // Dữ liệu thanh toán
    val signature: String      // Chữ ký xác thực
)

@Serializable
data class PaymentData(
    val bin: String,           // BIN (ví dụ: "970422")
    val accountNumber: String, // Số tài khoản (ví dụ: "113366668888")
    val accountName: String,   // Tên tài khoản (ví dụ: "QUY VAC XIN PHONG CHONG COVID")
    val amount: Int,           // Số tiền (ví dụ: 3000)
    val description: String,   // Mô tả giao dịch (ví dụ: "VQRIO12546 Thanh toan iphone")
    val orderCode: Long,       // Mã đơn hàng (ví dụ: 1254)
    val currency: String,       // Đơn vị tiền tệ (ví dụ: "VND")
    val paymentLinkId: String, // ID liên kết thanh toán
    val status: String,        // Trạng thái (ví dụ: "PENDING")
    val checkoutUrl: String,   // URL thanh toán
    val qrCode: String         // Mã QR
)

@Serializable
data class WebhookData(
    val orderId: Long,
    val status: String,
    val amount: Int,
    val description: String,
    val signature: String
)