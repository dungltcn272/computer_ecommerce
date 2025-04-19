package com.ltcn272.data.model

import com.ltcn272.data.model.serializer.BigDecimalSerializer
import com.ltcn272.data.model.serializer.LocalDateTimeSerializer
import java.math.BigDecimal
import java.util.UUID
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

@Serializable
data class OrderResponse(
    val id: Long,
    val userId: String,
    val status: String,
    @Serializable(with = BigDecimalSerializer::class) val totalPrice: BigDecimal,
    val paymentStatus: String,
    val cancellationReason: String?,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    val items: List<OrderItemResponse>
)

@Serializable
data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class) val originalPrice: BigDecimal
)

@Serializable
data class OrderUpdateRequest(
    val status: String
)

@Serializable
data class OrderCancelRequest(
    val reason: String?
)
