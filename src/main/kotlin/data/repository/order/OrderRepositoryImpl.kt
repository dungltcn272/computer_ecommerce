package com.ltcn272.data.repository.order

import com.ltcn272.data.database.OrderItems
import com.ltcn272.data.database.Orders
import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderItemResponse
import com.ltcn272.data.model.OrderResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class OrderRepositoryImpl : OrderRepository {
    override suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): Boolean = transaction {
        try {
            val totalPrice = items.sumOf { it.price * it.quantity.toBigDecimal() }

            val orderId = Orders.insert {
                it[Orders.userId] = userId
                it[Orders.totalPrice] = totalPrice
            } get Orders.id

            items.forEach { item ->
                OrderItems.insert {
                    it[OrderItems.orderId] = orderId
                    it[productId] = item.productId
                    it[quantity] = item.quantity
                    it[price] = item.price
                    it[originalPrice] = item.originalPrice
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getOrdersByUser(userId: UUID): List<OrderResponse> = transaction {
        Orders.selectAll().where { Orders.userId eq userId }
            .map { row ->
                val orderId = row[Orders.id]
                val items = getOrderItems(orderId)

                OrderResponse(
                    id = orderId,
                    userId = userId.toString(),
                    status = row[Orders.status],
                    totalPrice = row[Orders.totalPrice],
                    paymentStatus = row[Orders.paymentStatus],
                    cancellationReason = row[Orders.cancellationReason],
                    createdAt = row[Orders.createdAt],
                    items = items
                )
            }
    }

    override suspend fun getOrderById(orderId: Long, userId: UUID): OrderResponse? = transaction {
        Orders.selectAll().where { (Orders.id eq orderId) and (Orders.userId eq userId) }
            .mapNotNull { row ->
                val items = getOrderItems(orderId)

                OrderResponse(
                    id = row[Orders.id],
                    userId = userId.toString(),
                    status = row[Orders.status],
                    totalPrice = row[Orders.totalPrice],
                    paymentStatus = row[Orders.paymentStatus],
                    cancellationReason = row[Orders.cancellationReason],
                    createdAt = row[Orders.createdAt],
                    items = items
                )
            }.singleOrNull()
    }

    override suspend fun updateOrderStatus(orderId: Long, userId: UUID, status: String, reason: String?): Boolean = transaction {
        val updatedRows = Orders.update({ (Orders.id eq orderId) and (Orders.userId eq userId) }) {
            it[Orders.status] = status
            if (status == "CANCELLED") {
                it[Orders.cancellationReason] = reason
            }
        }
        return@transaction updatedRows > 0
    }
    private fun getOrderItems(orderId: Long): List<OrderItemResponse> {
        return OrderItems.selectAll().where { OrderItems.orderId eq orderId }
            .map { row ->
                OrderItemResponse(
                    id = row[OrderItems.id],
                    productId = row[OrderItems.productId],
                    quantity = row[OrderItems.quantity],
                    price = row[OrderItems.price],
                    originalPrice = row[OrderItems.originalPrice]
                )
            }
    }
    override suspend fun cancelOrder(orderId: Long, userId: UUID, reason: String?): Boolean = transaction {
        val updatedRows = Orders.update({ (Orders.id eq orderId) and (Orders.userId eq userId) }) {
            it[Orders.status] = "CANCELLED"
            it[Orders.cancellationReason] = reason
        }
        return@transaction updatedRows > 0
    }
    override suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String): Boolean = transaction {
        val updatedRows = Orders.update({ Orders.id eq orderId }) {
            it[Orders.paymentStatus] = paymentStatus
        }
        updatedRows > 0
    }
}
