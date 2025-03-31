package com.ltcn272.data.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

object DatabaseFactory {
    private const val DB_NAME = "computer_ecommerce"
    private const val DB_URL = "jdbc:mysql://localhost:3306/"
    private const val USER = "root"
    private const val PASSWORD = "123456"

    fun init() {
        createDatabaseIfNotExists()

        Database.connect(
            url = "$DB_URL$DB_NAME?useSSL=false&serverTimezone=UTC",
            driver = "com.mysql.cj.jdbc.Driver",
            user = USER,
            password = PASSWORD
        )

        transaction {
            SchemaUtils.create(Users, UserAddresses, Categories, Products, Orders, OrderItems, CartItems, Payments, Reviews, ProductMedias)
        }
    }

    private fun createDatabaseIfNotExists() {
        try {
            DriverManager.getConnection(DB_URL, USER, PASSWORD).use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS $DB_NAME")
                println("✅ Database '$DB_NAME' checked/created successfully!")
            }
        } catch (e: Exception) {
            println("❌ Error creating database: ${e.message}")
        }
    }
}