package com.ltcn272.services

import com.ltcn272.config.PasswordHasher
import com.ltcn272.data.model.User
import com.ltcn272.data.model.UserAddress
import com.ltcn272.data.repository.user.UserRepository
import java.util.*

class UserService(private val userRepository: UserRepository) {

    suspend fun updateAvatar(userId: UUID, avatarUrl: String): Boolean {
        return userRepository.updateAvatar(userId, avatarUrl)
    }

    suspend fun getAddress(userId: UUID): UserAddress? {
        return userRepository.findAddressByUserId(userId)
    }

    suspend fun adminGetUserAddress(userId: UUID, adminId: UUID): UserAddress? {
        if (userRepository.isAdmin(adminId))
            return userRepository.findAddressByUserId(userId) else throw Exception("You are not admin")
    }

    suspend fun updateAddress(userId: UUID, address: UserAddress): Boolean {
        return userRepository.updateAddress(userId, address)
    }

    suspend fun addAddress(userId: UUID, address: UserAddress): UserAddress {
        return userRepository.addAddress(userId, address)
    }

    suspend fun getAllUsers(adminId: UUID): List<User> {
        if (userRepository.isAdmin(adminId))
            return userRepository.getAllUsers() else throw Exception("You are not admin")
    }
    suspend fun deleteUser(userId: UUID, adminId: UUID): Boolean {
        if (userRepository.isAdmin(adminId))
            return userRepository.deleteUser(userId) else throw Exception("You are not admin")
    }

    suspend fun createUser(user: User, adminId: UUID): User? {
        if (userRepository.isAdmin(adminId)) {
            return userRepository.createUser(user)
        } else {
            throw Exception("You are not admin")
        }
    }
    suspend fun updateUser(
        userId: UUID,
        fullName: String? = null,
        phone: String? = null
    ): Boolean {
        return userRepository.updateUser(userId, fullName, phone)
    }
}