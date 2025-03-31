package com.ltcn272.data.repository.category

import com.ltcn272.data.database.Categories
import com.ltcn272.data.database.toCategory
import com.ltcn272.data.model.Category
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CategoryRepositoryImpl : CategoryRepository {

    override suspend fun getAllCategories(): List<Category> = newSuspendedTransaction {
        Categories.selectAll().map { it.toCategory() }
    }

    override suspend fun getCategoryById(id: Long): Category? = newSuspendedTransaction {
        Categories.selectAll().where {Categories.id eq id }
            .mapNotNull { it.toCategory() }
            .singleOrNull()
    }

    override suspend fun createCategory(category: Category): Long = newSuspendedTransaction {
        Categories.insert {
            it[name] = category.name
            it[parentId] = category.parentId
        } get Categories.id
    }

    override suspend fun updateCategory(id: Long, category: Category): Boolean = newSuspendedTransaction {
        Categories.update({ Categories.id eq id }) {
            it[name] = category.name
            it[parentId] = category.parentId
        } > 0
    }

    override suspend fun deleteCategory(id: Long): Boolean = newSuspendedTransaction {
        Categories.deleteWhere { Categories.id eq id } > 0
    }

}