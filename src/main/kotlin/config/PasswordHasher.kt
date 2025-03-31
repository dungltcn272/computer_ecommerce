package com.ltcn272.config

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PasswordHasher {
    fun hashPassword(password: String): String {
        val secretKey = AppConfig.secretPasswordKey
        val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(keySpec)
        val hashBytes = mac.doFinal(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        val password = hashPassword(plainPassword)
        println("pass = hashpass: ${password==hashedPassword}")
        return password == hashedPassword
    }
}