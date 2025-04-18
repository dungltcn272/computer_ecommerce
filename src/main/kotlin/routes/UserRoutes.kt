package com.ltcn272.routes

import com.ltcn272.config.AppConfig.BASE_URL
import com.ltcn272.data.model.RegisterRequest
import com.ltcn272.data.model.User
import com.ltcn272.data.model.UserAddress
import com.ltcn272.services.UserService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

fun Route.userRoutes(userService: UserService) {
    get("/users") {
        val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
        val users = userService.getAllUsers(adminId)
        if (users.isNotEmpty()) {
            call.respond(HttpStatusCode.OK, users)
        } else {
            call.respond(HttpStatusCode.NotFound, "No users found")
        }
    }
    post("/users") {
        val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
        val newUser = call.receive<User>()
        val userId = userService.createUser(newUser, adminId)
        if (userId != null) {
            call.respond(HttpStatusCode.Created, "User created with ID: $userId")
        } else {
            call.respond(HttpStatusCode.InternalServerError, "Failed to create user")
        }
    }
    delete("users/{id}") {
        val adminId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
        val userId = call.parameters["id"]
        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            return@delete
        }
        val uuid = UUID.fromString(userId)
        val deleted = userService.deleteUser(uuid, adminId)
        if (deleted) {
            call.respond(HttpStatusCode.OK, "User deleted successfully")
        } else {
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }
    route("/user") {
        get("{id}/address") {
            val userId = UUID.fromString(call.parameters["id"])
            val address = userService.getAddress(userId)
            call.respond(HttpStatusCode.OK, address)
        }
        put("/avatar") {
            val userId = UUID.fromString(call.principal<UserIdPrincipal>()!!.name)
            val multipartData = call.receiveMultipart()
            var avatarUrl: String? = null

            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileBytes = part.streamProvider().readBytes()
                    val filePath = "uploads/avatars/$userId"
                    val fileDir = File("uploads/avatars")
                    if (!fileDir.exists()) {
                        fileDir.mkdirs()
                    }

                    val file = File(filePath)
                    file.writeBytes(fileBytes)
                    avatarUrl = "$BASE_URL/$filePath"
                }
                part.dispose()
            }
            if (avatarUrl != null) {
                userService.updateAvatar(userId, avatarUrl!!)
                call.respond(HttpStatusCode.OK, "Avatar updated successfully")
            } else {
                call.respond(HttpStatusCode.BadRequest, "No file found")
            }
        }
        put("/address") {
            val uId = call.principal<UserIdPrincipal>()!!.name
            val data = call.receive<UserAddress>()
            val userAddress = UserAddress(
                userId = uId,
                address = data.address,
                city = data.city,
                state = data.state,
                country = data.country,
                postalCode = data.postalCode
            )

            val updated = userService.updateAddress(UUID.fromString(uId), userAddress)
            if (updated) {
                call.respond(HttpStatusCode.OK, "Address updated successfully")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update address")
            }
        }

        post("/address") {
            val uId = call.principal<UserIdPrincipal>()!!.name
            val data = call.receive<UserAddress>()
            val userAddress = UserAddress(
                userId = uId,
                address = data.address,
                city = data.city,
                state = data.state,
                country = data.country,
                postalCode = data.postalCode
            )
            userService.addAddress(UUID.fromString(uId), userAddress)
            call.respond(HttpStatusCode.Created, "Address created successfully")
        }
    }
}
