package com.ltcn272.data.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.time.LocalDateTime
import java.util.UUID

object Users : Table("users") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val phone = varchar("phone", 20).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val role = varchar("role", 20).check { it inList listOf("CUSTOMER", "ADMIN", "STAFF") }.default("CUSTOMER")
    val provider = varchar("provider", 20).check { it inList listOf("LOCAL", "GOOGLE", "FACEBOOK") }.default("LOCAL")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

object UserAddresses : Table("user_addresses") {
    val id = long("id").autoIncrement()
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val address = varchar("address", 500)
    val city = varchar("city", 100)
    val state = varchar("state", 100).nullable()
    val country = varchar("country", 100)
    val postalCode = varchar("postal_code", 20).nullable()
    override val primaryKey = PrimaryKey(id)
}

object Categories : Table("categories") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    val parentId = optReference("parent_id", id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val discount = integer("discount_price").default(0)
    val stockQuantity = integer("stock_quantity").default(0)
    val categoryId = optReference("category_id", Categories.id, onDelete = ReferenceOption.SET_NULL)
    val brand = varchar("brand", 100).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

object ProductMedias : Table("product_media") {
    val id = long("id").autoIncrement()
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val mediaType = varchar("media_type", 10).check { it inList listOf("IMAGE", "VIDEO") }
    val url = varchar("url", 500)
    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id = long("id").autoIncrement()
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).check { it inList listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED") }.default("PENDING")
    val totalPrice = decimal("total_price", 10, 2)
    val paymentStatus = varchar("payment_status", 20).check { it inList listOf("UNPAID", "PAID", "REFUNDED", "FAILED") }.default("UNPAID") // 🔹 Thêm cột này
    val cancellationReason = text("cancellation_reason").nullable() // 🔹 Lưu lý do huỷ đơn hàng
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").references(Orders.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity").check { it greater 0 }
    val price = decimal("price", 10, 2)
    val originalPrice = decimal("original_price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}

object CartItems : Table("cart_items") {
    val id = long("id").autoIncrement()
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity").check { it greater 0 }
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val expiresAt = datetime("expires_at").nullable()
    override val primaryKey = PrimaryKey(id)
    init {
        uniqueIndex("idx_user_product", userId, productId)
    }
}

object Payments : Table("payments") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").references(Orders.id, onDelete = ReferenceOption.CASCADE)
    val paymentMethod = varchar("payment_method", 50).check { it inList listOf("CREDIT_CARD", "PAYPAL", "MOMO", "VNPAY", "COD") }
    val status = varchar("status", 20).check { it inList listOf("PENDING", "SUCCESS", "FAILED", "REFUNDED") }.default("PENDING")
    val transactionId = varchar("transaction_id", 255).uniqueIndex().nullable()
    val amountPaid = decimal("amount_paid", 10, 2).nullable()
    val paidAt = datetime("paid_at").nullable() // 🔹 Lưu thời gian thanh toán thành công
    val refundAt = datetime("refund_at").nullable() // 🔹 Lưu thời gian hoàn tiền
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}

object Reviews : Table("reviews") {
    val id = long("id").autoIncrement()
    val userId = uuid("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val rating = integer("rating").check { it greaterEq 1 and (it lessEq 5) }
    val comment = text("comment").nullable()
    val verifiedPurchase = bool("verified_purchase").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id)
}
