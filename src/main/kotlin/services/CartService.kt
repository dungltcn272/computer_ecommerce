package com.ltcn272.services

import com.ltcn272.data.model.CartItemRequest
import com.ltcn272.data.model.CartItemResponse
import com.ltcn272.data.repository.cart.CartRepository
import java.util.*

class CartService(private val cartRepository: CartRepository) {
    suspend fun addItemToCart(userId: UUID, request: CartItemRequest): CartItemResponse? {
        return cartRepository.addItem(userId, request)
    }

    suspend fun getUserCart(userId: UUID): List<CartItemResponse> {
        return cartRepository.getCartItems(userId)
    }

    suspend fun updateCartItem(userId: UUID, itemId: Long, quantity: Int): Boolean {
        return cartRepository.updateItem(userId, itemId, quantity)
    }

    suspend fun removeCartItem(userId: UUID, itemId: Long): Boolean {
        return cartRepository.removeItem(userId, itemId)
    }

    suspend fun clearCart(userId: UUID): Boolean {
        return cartRepository.clearCart(userId)
    }
}
