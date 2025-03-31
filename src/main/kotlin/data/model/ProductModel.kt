package com.ltcn272.data.model

import com.ltcn272.data.model.serializer.BigDecimalSerializer
import com.ltcn272.data.model.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class ProductRequest(
    val name: String,
    val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    val discount: Int = 0,
    val stockQuantity: Int = 0,
    val categoryId: Long? = null,
    val brand: String? = null
)

@Serializable
data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class) val price: BigDecimal,
    val discount: Int = 0,
    val stockQuantity: Int,
    val category: String? = null,
    val brand: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime,
    val listMedia: MutableList<String> = mutableListOf()
)

@Serializable
data class ProductMedia(
    val productId : Long,
    val mediaType: String,
    val url: String
)
