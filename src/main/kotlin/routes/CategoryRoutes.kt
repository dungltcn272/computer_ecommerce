package com.ltcn272.routes

import com.ltcn272.data.model.Category
import com.ltcn272.services.CategoryService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryService: CategoryService) {

    route("/categories") {

        get {
            call.respond(HttpStatusCode.OK ,categoryService.getAllCategories())
        }

        // Get 1 category theo ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@get
            }
            val category = categoryService.getCategoryById(id)
            if (category == null) {
                call.respond(HttpStatusCode.NotFound, "Category not found")
            } else {
                call.respond(category)
            }
        }

        post {
            val category = call.receive<Category>()
            val id = categoryService.createCategory(category)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@put
            }
            val category = call.receive<Category>()
            val updated = categoryService.updateCategory(id, category)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Category updated successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Category not found")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                return@delete
            }
            val deleted = categoryService.deleteCategory(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Category deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Category not found")
            }
        }
    }
}
