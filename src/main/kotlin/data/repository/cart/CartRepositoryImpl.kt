package com.ltcn272.data.repository.cart

import com.ltcn272.data.database.CartItems
import com.ltcn272.data.model.CartItemRequest
import com.ltcn272.data.model.CartItemResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CartRepositoryImpl : CartRepository {
    override suspend fun addItem(userId: UUID, request: CartItemRequest): CartItemResponse? = transaction {
        val existingItem = CartItems.selectAll().where {
            (CartItems.userId eq userId) and (CartItems.productId eq request.productId)
        }.singleOrNull()

        if (existingItem != null) {
            val newQuantity = existingItem[CartItems.quantity] + request.quantity
            CartItems.update({ (CartItems.userId eq userId) and (CartItems.productId eq request.productId) }) {
                it[quantity] = newQuantity
            }
            return@transaction existingItem.let {
                CartItemResponse(
                    id = it[CartItems.id],
                    userId = userId.toString(),
                    productId = it[CartItems.productId],
                    quantity = newQuantity,
                    createdAt = it[CartItems.createdAt]
                )
            }
        } else {
            val insertedId = CartItems.insert {
                it[CartItems.userId] = userId
                it[CartItems.productId] = request.productId
                it[CartItems.quantity] = request.quantity
            } get CartItems.id

            val row = CartItems.selectAll().where { CartItems.id eq insertedId }.single()
            return@transaction CartItemResponse(
                id = row[CartItems.id],
                userId = userId.toString(),
                productId = row[CartItems.productId],
                quantity = row[CartItems.quantity],
                createdAt = row[CartItems.createdAt]
            )
        }
    }

    override suspend fun getCartItems(userId: UUID): List<CartItemResponse> = transaction {
        CartItems.selectAll().where { CartItems.userId eq userId }
            .map {
                CartItemResponse(
                    id = it[CartItems.id],
                    userId = userId.toString(),
                    productId = it[CartItems.productId],
                    quantity = it[CartItems.quantity],
                    createdAt = it[CartItems.createdAt]
                )
            }
    }

    override suspend fun updateItem(userId: UUID, itemId: Long, quantity: Int): Boolean = transaction {
        CartItems.update({ (CartItems.userId eq userId) and (CartItems.id eq itemId) }) {
            it[CartItems.quantity] = quantity
        } > 0
    }

    override suspend fun removeItem(userId: UUID, itemId: Long): Boolean = transaction {
        CartItems.deleteWhere { (CartItems.userId eq userId) and (CartItems.id eq itemId) } > 0
    }

    override suspend fun clearCart(userId: UUID): Boolean = transaction {
        CartItems.deleteWhere { CartItems.userId eq userId } > 0
    }
}
