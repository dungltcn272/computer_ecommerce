package com.ltcn272.data.model
import com.ltcn272.data.model.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class User(
    val id: String? = null,
    val fullName: String,
    val email: String,
    val passwordHash: String? = null,
    val phone: String? = null,
    val avatarUrl : String? = null,
    val role: String = "CUSTOMER",
    val provider : String = "LOCAL",
    @Serializable(with = LocalDateTimeSerializer::class) val createdAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
)
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)

@Serializable
data class UserAddress(
    val id: Long? = null,  // ID của địa chỉ, sẽ auto increment trong cơ sở dữ liệu
    val userId: String,     // ID người dùng, liên kết với người dùng trong bảng Users
    val address: String,  // Địa chỉ
    val city: String,     // Thành phố
    val state: String?,   // Tỉnh/Thành phố (Có thể là null)
    val country: String,  // Quốc gia
    val postalCode: String? // Mã bưu chính (Có thể là null)
)
