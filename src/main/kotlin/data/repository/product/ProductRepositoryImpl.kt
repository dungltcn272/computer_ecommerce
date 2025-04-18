package com.ltcn272.data.repository.product

import com.ltcn272.data.database.Categories
import com.ltcn272.data.database.ProductMedias
import com.ltcn272.data.database.Products
import com.ltcn272.data.model.ProductMedia
import com.ltcn272.data.model.ProductRequest
import com.ltcn272.data.model.ProductResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(
        page: Int?,
        limit: Int?,
        category: String?,
        sortBy: String?,
        order: String?,
        search: String?
    ): List<ProductResponse> = transaction {
        val productMap = mutableMapOf<Long, ProductResponse>()

        // Build the base query
        var query = (Products leftJoin Categories)
            .select(
                Products.id,
                Products.name,
                Products.description,
                Products.price,
                Products.discount,
                Products.stockQuantity,
                Categories.name,
                Products.brand,
                Products.createdAt
            )

        // Apply filters
        if (!category.isNullOrBlank()) {
            query = query.andWhere { Categories.name eq category }
        }

        if (!search.isNullOrBlank()) {
            query = query.andWhere { Products.name like "%$search%" }
        }

        // Apply sorting
        if (!sortBy.isNullOrBlank()) {
            val column = when (sortBy.lowercase()) {
                "price" -> Products.price
                "name" -> Products.name
                "created_at" -> Products.createdAt
                else -> Products.id
            }
            query = if (order?.lowercase() == "desc") {
                query.orderBy(column to SortOrder.DESC)
            } else {
                query.orderBy(column to SortOrder.ASC)
            }
        } else {
            query = if (order?.lowercase() == "desc") {
                query.orderBy(Products.id to SortOrder.DESC)
            } else {
                query.orderBy(Products.id to SortOrder.ASC)
            }
        }

        // Apply pagination if provided
        if (page != null && limit != null) {
            val validPage = maxOf(1, page)
            val validLimit = maxOf(1, limit)
            val offset = ((validPage - 1) * validLimit).toLong()
            query = query.limit(validLimit).offset(offset)
        }

        // Fetch product details
        query.forEach { row ->
            val productId = row[Products.id]
            productMap[productId] = ProductResponse(
                id = productId,
                name = row[Products.name],
                description = row[Products.description],
                price = row[Products.price],
                discount = row[Products.discount],
                stockQuantity = row[Products.stockQuantity],
                category = row.getOrNull(Categories.name),
                brand = row[Products.brand],
                createdAt = row[Products.createdAt],
                listMedia = mutableListOf()
            )
        }

        // Fetch product media
        if (productMap.isNotEmpty()) {
            ProductMedias
                .selectAll()
                .where { ProductMedias.productId inList productMap.keys }
                .orderBy(ProductMedias.id to SortOrder.ASC)
                .forEach { mediaRow ->
                    val productId = mediaRow[ProductMedias.productId]
                    productMap[productId]?.listMedia?.add(mediaRow[ProductMedias.url])
                }
        }

        productMap.values.toList()
    }

    override suspend fun getProductById(productId: Long): ProductResponse? = transaction {
        val productMap = mutableMapOf<Long, ProductResponse>()

        (Products leftJoin Categories leftJoin ProductMedias)
            .selectAll().where { Products.id eq productId }
            .forEach { row ->
                val mediaUrl = row[ProductMedias.url]

                val product = productMap.getOrPut(productId) {
                    ProductResponse(
                        id = productId,
                        name = row[Products.name],
                        description = row[Products.description],
                        price = row[Products.price],
                        discount = row[Products.discount],
                        stockQuantity = row[Products.stockQuantity],
                        category = row[Categories.name],
                        brand = row[Products.brand],
                        createdAt = row[Products.createdAt],
                        listMedia = mutableListOf()
                    )
                }

                (product.listMedia).add(mediaUrl)
            }

        return@transaction productMap[productId]
    }

    override suspend fun createProduct(product: ProductRequest): Long {
        return transaction {
            Products.insert {
                it[name] = product.name
                it[description] = product.description
                it[price] = product.price
                it[discount] = product.discount
                it[stockQuantity] = product.stockQuantity
                it[categoryId] = product.categoryId
                it[brand] = product.brand
            } get (Products.id)
        }
    }

    override suspend fun deleteProduct(productId: Long): Boolean {
        return transaction {
            ProductMedias.deleteWhere { ProductMedias.productId eq productId }
            Products.deleteWhere { Products.id eq productId } > 0
        }
    }

    override suspend fun updateProduct(productId: Long, product: ProductRequest): Boolean {
        return transaction {
            Products.update({ Products.id eq productId }) {
                it[name] = product.name
                it[description] = product.description
                it[price] = product.price
                it[discount] = product.discount
                it[stockQuantity] = product.stockQuantity
                it[categoryId] = product.categoryId
                it[brand] = product.brand
            } > 0
        }
    }

    override suspend fun reduceStockQuantity(productId: Long, quantity: Int): Boolean = transaction {
        val currentStock = Products
            .selectAll().where { Products.id eq productId }
            .map { it[Products.stockQuantity] }
            .singleOrNull() ?: return@transaction false

        if (currentStock < quantity) {
            return@transaction false  // Không đủ hàng
        }

        val updatedRows = Products.update({ Products.id eq productId }) {
            with(SqlExpressionBuilder) {
                it.update(stockQuantity, stockQuantity - quantity)
            }
        }

        return@transaction updatedRows > 0
    }

    override suspend fun addProductMedia(productMedia: ProductMedia): Boolean {
        return transaction {
            ProductMedias.insert {
                it[productId] = productMedia.productId
                it[mediaType] = productMedia.mediaType
                it[url] = productMedia.url
            }
            true
        }

    }


    override suspend fun getProductMedia(productId: Long): List<String> {
        return transaction {
            ProductMedias
                .selectAll().where { ProductMedias.productId eq productId }
                .map { it[ProductMedias.url] }
        }
    }

}
