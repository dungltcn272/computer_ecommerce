package com.ltcn272.data.repository.user

import com.ltcn272.data.database.UserAddresses
import com.ltcn272.data.database.Users
import com.ltcn272.data.model.User
import com.ltcn272.data.database.toUser
import com.ltcn272.data.model.UserAddress
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepositoryImpl : UserRepository {
    override suspend fun createUser(user: User): User = transaction {
        val userId = UUID.randomUUID()
        val insertedId = Users.insert {
            it[id] = userId
            it[fullName] = user.fullName
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[phone] = user.phone
            it[role] = user.role
            it[provider] = user.provider
        } get Users.id
        user.copy(id = insertedId.toString())
    }

    override suspend fun findByEmail(email: String): User? = transaction {
        Users.selectAll().where { Users.email eq email }
            .map { row ->
                row.toUser()
            }.singleOrNull()
    }

    override suspend fun findById(id: UUID): User? = transaction {
        Users.selectAll().where { Users.id eq id }
            .mapNotNull { it.toUser() }
            .singleOrNull()
    }

    override suspend fun updateProvider(email: String, provider: String): Boolean = transaction {
        Users.update({ Users.email eq email }) {
            it[Users.provider] = provider
        } > 0
    }

    override suspend fun updateAvatar(email: String, avatarUrl: String): Boolean = transaction {
        Users.update({ Users.email eq email }) {
            it[Users.avatarUrl] = avatarUrl
        } > 0
    }

    override suspend fun updateAvatar(id: UUID, avatarUrl: String): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.avatarUrl] = avatarUrl
        } > 0
    }

    override suspend fun getAllUsers(): List<User> = transaction {
        Users.selectAll().map { it.toUser() }
    }

    override suspend fun addAddress(userId: UUID, address: UserAddress): UserAddress = transaction {
        val insertedId = UserAddresses.insert {
            it[UserAddresses.userId] = userId
            it[UserAddresses.address] = address.address
            it[city] = address.city
            it[state] = address.state
            it[country] = address.country
            it[postalCode] = address.postalCode
        } get UserAddresses.id
        address.copy(id = insertedId)
    }

    override suspend fun isAdmin(uuid: UUID): Boolean = transaction {
        Users.select(Users.role).where { Users.id eq uuid }
            .map { it[Users.role] }
            .singleOrNull() == "ADMIN"
    }

    override suspend fun updateAddress(userId: UUID, address: UserAddress): Boolean = transaction {
        val updatedRows =
            UserAddresses.update({(UserAddresses.userId eq userId) }) {
                it[UserAddresses.address] = address.address
                it[city] = address.city
                it[state] = address.state
                it[country] = address.country
                it[postalCode] = address.postalCode
            }
        updatedRows > 0
    }

    override suspend fun removeAddress(userId: UUID): Boolean = transaction {
        val deletedRows =
            UserAddresses.deleteWhere {(UserAddresses.userId eq userId) }
        deletedRows > 0
    }

    override suspend fun findAddressByUserId(userId: UUID): UserAddress? = transaction {
        UserAddresses.selectAll().where{ UserAddresses.userId eq userId }
            .map { row ->
                UserAddress(
                    id = row[UserAddresses.id],
                    userId = row[UserAddresses.userId].toString(),
                    address = row[UserAddresses.address],
                    city = row[UserAddresses.city],
                    state = row[UserAddresses.state],
                    country = row[UserAddresses.country],
                    postalCode = row[UserAddresses.postalCode]
                )
            }.singleOrNull()
    }

    override suspend fun deleteUser(userId: UUID): Boolean = transaction {
        UserAddresses.deleteWhere { UserAddresses.userId eq userId }
        Users.deleteWhere { Users.id eq userId } > 0
    }

    override suspend fun updateUser(userId: UUID, fullName: String?, phone: String?): Boolean =
        transaction {
            Users.update({ Users.id eq userId }) {
                if (fullName != null) {
                    it[Users.fullName] = fullName
                }
                if (phone != null) {
                    it[Users.phone] = phone
                }
            } > 0
        }
}