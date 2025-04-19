package com.ltcn272.data.repository.order

import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderResponse
import java.util.UUID

interface OrderRepository {
    suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): OrderResponse
    suspend fun getOrdersByUser(userId: UUID): List<OrderResponse>
    suspend fun getOrderById(orderId: Long, userId: UUID): OrderResponse?
    suspend fun updateOrderStatus(orderId: Long, status: String, reason: String?): Boolean
    suspend fun cancelOrder(orderId: Long, userId: UUID, reason: String?): Boolean
    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String): Boolean
}
