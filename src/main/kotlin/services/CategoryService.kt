package com.ltcn272.services

import com.ltcn272.data.model.Category
import com.ltcn272.data.repository.category.CategoryRepository

class CategoryService(private val categoryRepository: CategoryRepository) {

    suspend fun getAllCategories(): List<Category> {
        return categoryRepository.getAllCategories()
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryRepository.getCategoryById(id)
    }

    suspend fun createCategory(category: Category): Long {
        return categoryRepository.createCategory(category)
    }

    suspend fun updateCategory(id: Long, category: Category): Boolean {
        return categoryRepository.updateCategory(id, category)
    }

    suspend fun deleteCategory(id: Long): Boolean {
        return categoryRepository.deleteCategory(id)
    }
}
