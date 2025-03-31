package com.ltcn272.data.database

import com.ltcn272.data.model.Category
import com.ltcn272.data.model.User
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUser() = User(
    id = this[Users.id].toString(),
    fullName = this[Users.fullName],
    email = this[Users.email],
    passwordHash = this[Users.passwordHash],
    phone = this[Users.phone],
    avatarUrl = this[Users.avatarUrl],
    role = this[Users.role],
    provider = this[Users.provider],
    createdAt = this[Users.createdAt]
)

fun ResultRow.toCategory() = Category(
    id = this[Categories.id],
    name = this[Categories.name],
    parentId = this[Categories.parentId]
)