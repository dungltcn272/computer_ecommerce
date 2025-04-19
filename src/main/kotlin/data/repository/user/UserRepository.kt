package com.ltcn272.data.repository.user

import com.ltcn272.data.model.User
import com.ltcn272.data.model.UserAddress
import java.util.*

interface UserRepository {
    suspend fun createUser(user: User): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: UUID): User?
    suspend fun updateProvider(email: String, provider: String): Boolean
    suspend fun updateAvatar(email: String, avatarUrl: String): Boolean
    suspend fun updateAvatar(id: UUID, avatarUrl: String): Boolean
    suspend fun isAdmin(uuid: UUID): Boolean

    suspend fun getAllUsers(): List<User>

    suspend fun addAddress(userId: UUID, address: UserAddress): UserAddress
    suspend fun updateAddress(userId: UUID, address: UserAddress): Boolean
    suspend fun removeAddress(userId: UUID, addressId: Long): Boolean
    suspend fun findAddressByUserId(userId: UUID): List<UserAddress>

    suspend fun deleteUser(userId: UUID): Boolean
    suspend fun updateUser(
        userId: UUID,
        fullName: String?,
        phone: String?
    ): Boolean
}