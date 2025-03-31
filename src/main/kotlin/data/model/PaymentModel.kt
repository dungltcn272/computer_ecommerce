package com.ltcn272.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val method: String
)

@Serializable
data class PaymentResponse(
    val orderId: Long,
    val paymentStatus: String
)