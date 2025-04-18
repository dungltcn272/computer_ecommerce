package com.ltcn272.data.repository.product

import com.ltcn272.data.model.ProductMedia
import com.ltcn272.data.model.ProductRequest
import com.ltcn272.data.model.ProductResponse

interface ProductRepository {

    suspend fun getProductsPaginated(
        page: Int,
        limit: Int = 20,
        category: String? = null,
        sortBy: String? = null,
        order: String? = null,
        search: String? = null
    ): List<ProductResponse>

    suspend fun getAllProducts(): List<ProductResponse>
    suspend fun getProductById(productId: Long): ProductResponse?
    suspend fun createProduct(product: ProductRequest): Long
    suspend fun deleteProduct(productId: Long): Boolean
    suspend fun updateProduct(productId : Long, product: ProductRequest): Boolean
    suspend fun reduceStockQuantity(productId: Long, quantity : Int) : Boolean

    suspend fun addProductMedia(productMedia: ProductMedia): Boolean
    suspend fun getProductMedia(productId: Long): List<String>
}