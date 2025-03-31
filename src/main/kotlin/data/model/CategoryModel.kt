package com.ltcn272.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Long? = null,
    val name: String,
    val parentId: Long? = null
)