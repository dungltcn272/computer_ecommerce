package com.ltcn272.data.repository.category

import com.ltcn272.data.model.Category

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun createCategory(category: Category): Long
    suspend fun updateCategory(id: Long, category: Category): Boolean
    suspend fun deleteCategory(id: Long): Boolean
}