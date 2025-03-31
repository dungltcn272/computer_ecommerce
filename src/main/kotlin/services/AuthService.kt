package com.ltcn272.services

import com.ltcn272.config.JWTConfig
import com.ltcn272.config.PasswordHasher
import com.ltcn272.data.model.*
import com.ltcn272.data.repository.user.UserRepository
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class AuthService(private val userRepository: UserRepository) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) throw BadRequestException("Email đã được sử dụng")

        val hashedPassword = PasswordHasher.hashPassword(request.password)
        val user = User(
            id = UUID.randomUUID().toString(),
            fullName = request.fullName,
            email = request.email,
            passwordHash = hashedPassword,
            phone = request.phone,
            role = "CUSTOMER",
            provider = "LOCAL",
            createdAt = java.time.LocalDateTime.now()
        )

        val createdUser = userRepository.createUser(user) ?: throw Exception("Không thể tạo tài khoản")

        return AuthResponse(
            token = JWTConfig.generateToken(createdUser),
            user = createdUser
        )
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email) ?: throw BadRequestException("Sai tài khoản hoặc mật khẩu")

        if (!PasswordHasher.verifyPassword(request.password, user.passwordHash!!)) {
            throw BadRequestException("Sai tài khoản hoặc mật khẩu")
        }

        return AuthResponse(
            token = JWTConfig.generateToken(user),
            user = user
        )
    }

    suspend fun authenticateGoogleUser(email: String, fullName: String, picture: String?): AuthResponse {
        return newSuspendedTransaction {
            val existingUser = userRepository.findByEmail(email)

            val user = if (existingUser == null) {
                // Tạo user mới nếu chưa tồn tại
                val newUser = User(
                    id = UUID.randomUUID().toString(),
                    fullName = fullName,
                    email = email,
                    passwordHash = null, // Không cần password cho tài khoản Google
                    phone = null,
                    avatarUrl = picture,
                    role = "CUSTOMER",
                    provider = "GOOGLE",
                    createdAt = java.time.LocalDateTime.now()
                )
                userRepository.createUser(newUser) ?: throw Exception("Không thể tạo tài khoản Google")
            } else {
                // Nếu user đã tồn tại nhưng provider là LOCAL, cập nhật provider thành GOOGLE
                if (existingUser.provider == "LOCAL") {
                    userRepository.updateProvider(email, "GOOGLE")
                }

                // Cập nhật avatar nếu khác
                if (picture != null && picture != existingUser.avatarUrl) {
                    userRepository.updateAvatar(email, picture)
                }

                existingUser
            }

            return@newSuspendedTransaction AuthResponse(
                token = JWTConfig.generateToken(user),
                user = user
            )
        }
    }
}
