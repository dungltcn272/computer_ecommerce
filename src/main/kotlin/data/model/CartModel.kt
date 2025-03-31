package com.ltcn272.data.model

import com.ltcn272.data.model.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CartItemRequest(
    val productId: Long,
    val quantity: Int
)

@Serializable
data class CartItemResponse(
    val id: Long,
    val userId: String,
    val productId: Long,
    val quantity: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime
)
