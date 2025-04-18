package com.ltcn272.services

import com.ltcn272.config.AppConfig.BASE_URL
import com.ltcn272.data.model.ProductMedia
import com.ltcn272.data.model.ProductRequest
import com.ltcn272.data.model.ProductResponse
import com.ltcn272.data.repository.product.ProductRepository
import com.ltcn272.data.repository.user.UserRepository
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import java.io.File
import java.util.UUID

class ProductService(private val productRepository: ProductRepository, private val userRepository: UserRepository) {

    suspend fun getProductById(productId: Long): ProductResponse? {
        return productRepository.getProductById(productId)
    }

    suspend fun createProduct(userId: UUID, product: ProductRequest): Long {
        if (!userRepository.isAdmin(userId)) {
            throw IllegalAccessException("Bạn không có quyền thêm sản phẩm")
        }
        return productRepository.createProduct(product)
    }

    suspend fun deleteProduct(userId: UUID, productId: Long): Boolean {
        if (!userRepository.isAdmin(userId)) {
            throw IllegalAccessException("Bạn không có quyền thêm sản phẩm")
        }
        val directoryPath = "uploads/products/$productId"
        val dir = File(directoryPath)

        if (dir.exists() && dir.isDirectory) {
            dir.deleteRecursively()
        }
        return productRepository.deleteProduct(productId)
    }

    suspend fun updateProduct(userId: UUID, productId: Long, product: ProductRequest): Boolean {
        isAdmin(userId)
        return productRepository.updateProduct(productId, product)
    }

    suspend fun reduceStockQuantity(productId: Long, quantity: Int): Boolean {
        return productRepository.reduceStockQuantity(productId, quantity)
    }

    suspend fun addProductMediaFile(userId: UUID, file: PartData.FileItem): String? {
        isAdmin(userId)
        val fileExtension = file.originalFileName?.substringAfterLast(".")?.lowercase()
        val uploadDir = File("uploads/products").apply { mkdirs() }
        val newFileName = UUID.randomUUID().toString() + "." + fileExtension// Đảm bảo không trùng tên
        val fileToSave = File(uploadDir, newFileName)

        return try {
            val fileBytes = file.provider().readRemaining().readByteArray()
            fileToSave.writeBytes(fileBytes)
            "$BASE_URL/products/media/$newFileName" // Trả về URL file
        } catch (e: Exception) {
            println("Error saving file: ${e.message}")
            null
        }
    }

    suspend fun addProductMediaTable(userId: UUID, productId: Long, fileUrl: String): Boolean {
        isAdmin(userId)
        val fileExtension = fileUrl.substringAfterLast(".").lowercase()

        val mediaType = when {
            listOf("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(fileExtension) -> "IMAGE"
            listOf("mp4", "mov", "avi", "mkv", "flv", "wmv").contains(fileExtension) -> "VIDEO"
            else -> throw IllegalArgumentException("Unsupported media type: $fileExtension")
        }

        val productMedia = ProductMedia(
            productId = productId,
            mediaType = mediaType,
            url = fileUrl
        )

        return productRepository.addProductMedia(productMedia)
    }

    private suspend fun isAdmin(userId: UUID): Boolean {
        if (userRepository.isAdmin(userId)) {
            return true
        } else {
            throw IllegalAccessException("Bạn không có quyền thực hiện hành động này")
        }
    }

    suspend fun getProducts(
        page: Int?,
        limit: Int?,
        category: String?,
        sortBy: String?,
        order: String?,
        search: String?
    ): List<ProductResponse> {
        if (page != null && page < 1) throw IllegalArgumentException("Page must be at least 1")
        if (limit != null && (limit < 1 || limit > 100)) throw IllegalArgumentException("Limit must be between 1 and 100")
        if (sortBy != null && sortBy !in listOf("price", "name", "created_at")) {
            throw IllegalArgumentException("SortBy must be price, name, or created_at")
        }
        if (order != null && order !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Order must be asc or desc")
        }
        return productRepository.getProducts(page, limit, category, sortBy, order, search)
    }

}