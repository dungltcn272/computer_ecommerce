package com.ltcn272.data.repository.cart

import com.ltcn272.data.model.CartItemRequest
import com.ltcn272.data.model.CartItemResponse
import java.util.*

interface CartRepository {
    suspend fun addItem(userId: UUID, request: CartItemRequest): CartItemResponse?
    suspend fun getCartItems(userId: UUID): List<CartItemResponse>
    suspend fun updateItem(userId: UUID, itemId: Long, quantity: Int): Boolean
    suspend fun removeItem(userId: UUID, itemId: Long): Boolean
    suspend fun clearCart(userId: UUID): Boolean
}
