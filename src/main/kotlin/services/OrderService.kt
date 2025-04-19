package com.ltcn272.services

import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderResponse
import com.ltcn272.data.repository.order.OrderRepository
import java.util.UUID

class OrderService(private val orderRepository: OrderRepository) {
    suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): OrderResponse {
        return orderRepository.createOrder(userId, items)
    }
    suspend fun getOrdersByUser(userId: UUID): List<OrderResponse> {
        return orderRepository.getOrdersByUser(userId)
    }

    suspend fun getOrderById(orderId: Long, userId: UUID): OrderResponse? {
        return orderRepository.getOrderById(orderId, userId)
    }

    suspend fun updateOrderStatus(orderId: Long, status: String, reason: String?= null): Boolean {
        return orderRepository.updateOrderStatus(orderId, status, reason)
    }

    suspend fun cancelOrder(orderId: Long, userId: UUID, reason: String?): Boolean {
        return orderRepository.cancelOrder(orderId, userId, reason)
    }


    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String): Boolean {
        return orderRepository.updatePaymentStatus(orderId, paymentStatus)
    }
}