package com.ltcn272.services

import com.ltcn272.config.VNPayGateway
import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderResponse
import com.ltcn272.data.model.PaymentResponse
import com.ltcn272.data.repository.order.OrderRepository
import java.math.BigDecimal
import java.util.UUID

class OrderService(private val orderRepository: OrderRepository) {
    suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): Boolean {
        return orderRepository.createOrder(userId, items)
    }
    suspend fun getOrdersByUser(userId: UUID): List<OrderResponse> {
        return orderRepository.getOrdersByUser(userId)
    }

    suspend fun getOrderById(orderId: Long, userId: UUID): OrderResponse? {
        return orderRepository.getOrderById(orderId, userId)
    }

    suspend fun updateOrderStatus(orderId: Long, userId: UUID, status: String, reason: String?= null): Boolean {
        return orderRepository.updateOrderStatus(orderId, userId, status, reason)
    }

    suspend fun cancelOrder(orderId: Long, userId: UUID, reason: String?): Boolean {
        return orderRepository.cancelOrder(orderId, userId, reason)
    }


    suspend fun processCODPayment(orderId: Long): PaymentResponse {
        orderRepository.updatePaymentStatus(orderId, "UNPAID")
        return PaymentResponse(orderId, "UNPAID")
    }

    suspend fun processVNPayPayment(orderId: Long, amount: BigDecimal, clientIp : String): String {
        return VNPayGateway.createPaymentUrl(orderId, amount, clientIp)
    }

    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String): Boolean {
        return orderRepository.updatePaymentStatus(orderId, paymentStatus)
    }
}