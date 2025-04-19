package com.ltcn272.data.repository.order

import com.ltcn272.data.database.OrderItems
import com.ltcn272.data.database.Orders
import com.ltcn272.data.database.Products
import com.ltcn272.data.model.OrderItemRequest
import com.ltcn272.data.model.OrderItemResponse
import com.ltcn272.data.model.OrderResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

class OrderRepositoryImpl : OrderRepository {

    private val logger = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    override suspend fun createOrder(userId: UUID, items: List<OrderItemRequest>): OrderResponse = transaction {
        try {
            // Bước 1: Kiểm tra đầu vào
            if (items.isEmpty()) {
                throw IllegalArgumentException("Order items cannot be empty")
            }

            // Bước 2: Kiểm tra quantity hợp lệ
            val validatedItems = items.map { item ->
                if (item.quantity <= 0) {
                    throw IllegalArgumentException("Quantity must be greater than 0 for product ID ${item.productId}")
                }
                item
            }

            // Bước 3: Lấy thông tin sản phẩm từ DB (bao gồm stock nếu có)
            val productIds = validatedItems.map { it.productId }
            val products = Products.select(Products.id, Products.price, Products.discount)
                .where { Products.id inList productIds }
                .associateBy(
                    { it[Products.id] },
                    { row ->
                        val originalPrice = row[Products.price]
                        val discount = row[Products.discount]
                        val discountAmount = originalPrice.multiply(BigDecimal(discount))
                            .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                        val discountedPrice = originalPrice.subtract(discountAmount)
                        Pair(originalPrice, discountedPrice)
                    }
                )

            // Bước 4: Kiểm tra xem tất cả sản phẩm có tồn tại không
            val missingProducts = productIds.filterNot { it in products }
            if (missingProducts.isNotEmpty()) {
                throw IllegalArgumentException("Products not found: $missingProducts")
            }

            // Bước 5: Tính tổng giá gốc và tổng giá đã giảm
            val orderItemsDetails = validatedItems.map { item ->
                val (originalPrice, discountedPrice) = products[item.productId]!!
                val quantity = item.quantity.toBigDecimal()
                Triple(item.productId, quantity, Pair(originalPrice, discountedPrice))
            }

            val totalOriginalPrice = orderItemsDetails.sumOf { (_, quantity, prices) ->
                val (originalPrice, _) = prices
                originalPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP)
            }.setScale(2, RoundingMode.HALF_UP)

            val totalPrice = orderItemsDetails.sumOf { (_, quantity, prices) ->
                val (_, discountedPrice) = prices
                discountedPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP)
            }.setScale(2, RoundingMode.HALF_UP)

            // Bước 6: Tạo đơn hàng trong bảng Orders
            val createdAt = LocalDateTime.now()
            val orderId = Orders.insert {
                it[Orders.userId] = userId
                it[Orders.totalPrice] = totalPrice
                it[Orders.status] = "PENDING"
                it[Orders.paymentStatus] = "UNPAID"
                it[Orders.createdAt] = createdAt
                it[Orders.cancellationReason] = null
            } get Orders.id

            // Bước 7: Lưu chi tiết đơn hàng vào bảng OrderItems
            val orderItemsResponses = orderItemsDetails.map { (productId, quantity, prices) ->
                val (originalPrice, discountedPrice) = prices
                val orderItemId = OrderItems.insert {
                    it[OrderItems.orderId] = orderId
                    it[OrderItems.productId] = productId
                    it[OrderItems.quantity] = quantity.toInt()
                    it[OrderItems.price] = discountedPrice
                    it[OrderItems.originalPrice] = originalPrice
                } get OrderItems.id

                OrderItemResponse(
                    id = orderItemId,
                    productId = productId,
                    quantity = quantity.toInt(),
                    price = discountedPrice,
                    originalPrice = originalPrice
                )
            }

            // Bước 8: Trả về OrderResponse
            OrderResponse(
                id = orderId,
                userId = userId.toString(),
                status = "PENDING",
                totalPrice = totalPrice,
                paymentStatus = "UNPAID",
                cancellationReason = null,
                createdAt = createdAt,
                items = orderItemsResponses
            )
        } catch (e: Exception) {
            logger.error("Error creating order for user $userId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getOrdersByUser(userId: UUID): List<OrderResponse> = transaction {
        // Tối ưu hóa truy vấn bằng cách sử dụng JOIN
        val orderItemsMap = OrderItems.selectAll()
            .where { OrderItems.orderId inSubQuery Orders.select(Orders.id).where { Orders.userId eq userId } }
            .groupBy { it[OrderItems.orderId] }
            .mapValues { entry ->
                entry.value.map { row ->
                    OrderItemResponse(
                        id = row[OrderItems.id],
                        productId = row[OrderItems.productId],
                        quantity = row[OrderItems.quantity],
                        price = row[OrderItems.price],
                        originalPrice = row[OrderItems.originalPrice]
                    )
                }
            }

        Orders.selectAll().where { Orders.userId eq userId }
            .map { row ->
                val orderId = row[Orders.id]
                OrderResponse(
                    id = orderId,
                    userId = userId.toString(),
                    status = row[Orders.status],
                    totalPrice = row[Orders.totalPrice],
                    paymentStatus = row[Orders.paymentStatus],
                    cancellationReason = row[Orders.cancellationReason],
                    createdAt = row[Orders.createdAt],
                    items = orderItemsMap[orderId] ?: emptyList()
                )
            }
    }

    override suspend fun getOrderById(orderId: Long, userId: UUID): OrderResponse? = transaction {
        val order = Orders.selectAll().where { (Orders.id eq orderId) and (Orders.userId eq userId) }
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

        if (order == null) {
            logger.warn("Order $orderId not found for user $userId")
        }
        order
    }

    override suspend fun updateOrderStatus(orderId: Long, status: String, reason: String?): Boolean = transaction {
        try {
            // Kiểm tra trạng thái hợp lệ
            val validStatuses = setOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED")
            if (status !in validStatuses) {
                throw IllegalArgumentException("Invalid status: $status. Must be one of $validStatuses")
            }

            // Kiểm tra trạng thái hiện tại của đơn hàng
            val currentOrder = Orders.selectAll().where { Orders.id eq orderId }.singleOrNull()
                ?: throw IllegalArgumentException("Order $orderId not found")

            // Không cho phép hủy nếu đơn hàng đã giao
            if (currentOrder[Orders.status] == "DELIVERED" && status == "CANCELLED") {
                throw IllegalStateException("Cannot cancel order $orderId because it has been delivered")
            }

            val updatedRows = Orders.update({ Orders.id eq orderId }) {
                it[Orders.status] = status
                if (status == "CANCELLED") {
                    it[Orders.cancellationReason] = reason
                }
            }
            updatedRows > 0
        } catch (e: Exception) {
            logger.error("Error updating order status for order $orderId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelOrder(orderId: Long, userId: UUID, reason: String?): Boolean = transaction {
        try {
            // Kiểm tra trạng thái hiện tại của đơn hàng
            val currentOrder = Orders.selectAll().where { (Orders.id eq orderId) and (Orders.userId eq userId) }
                .singleOrNull() ?: throw IllegalArgumentException("Order $orderId not found for user $userId")

            if (currentOrder[Orders.status] == "DELIVERED") {
                throw IllegalStateException("Cannot cancel order $orderId because it has been delivered")
            }

            val updatedRows = Orders.update({ (Orders.id eq orderId) and (Orders.userId eq userId) }) {
                it[Orders.status] = "CANCELLED"
                it[Orders.cancellationReason] = reason
            }
            updatedRows > 0
        } catch (e: Exception) {
            logger.error("Error cancelling order $orderId for user $userId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String): Boolean = transaction {
        try {
            // Kiểm tra paymentStatus hợp lệ
            val validPaymentStatuses = setOf("UNPAID", "PAID", "FAILED")
            if (paymentStatus !in validPaymentStatuses) {
                throw IllegalArgumentException("Invalid payment status: $paymentStatus. Must be one of $validPaymentStatuses")
            }

            val updatedRows = Orders.update({ Orders.id eq orderId }) {
                it[Orders.paymentStatus] = paymentStatus
            }
            updatedRows > 0
        } catch (e: Exception) {
            logger.error("Error updating payment status for order $orderId: ${e.message}", e)
            throw e
        }
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
}