package com.nutriagricola.app.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** CPU-intensive operations: call from a background dispatcher. */
object PasswordHasher {

    private const val ITERATIONS = 600_000
    private const val SALT_BYTES = 16
    private const val HASH_BYTES = 32

    data class HashedPassword(
        val passwordHash: String,
        val salt: String,
        val iterations: Int
    )

    fun hash(password: CharArray): HashedPassword {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt, ITERATIONS)

        return HashedPassword(
            passwordHash = Base64.getEncoder().encodeToString(hash),
            salt = Base64.getEncoder().encodeToString(salt),
            iterations = ITERATIONS
        )
    }

    fun verify(
        password: CharArray,
        passwordHash: String,
        salt: String,
        iterations: Int
    ): Boolean {
        if (iterations <= 0) return false

        val expectedHash: ByteArray
        val decodedSalt: ByteArray
        try {
            expectedHash = Base64.getDecoder().decode(passwordHash)
            decodedSalt = Base64.getDecoder().decode(salt)
        } catch (_: IllegalArgumentException) {
            return false
        }

        if (expectedHash.size != HASH_BYTES || decodedSalt.size != SALT_BYTES) {
            return false
        }

        return MessageDigest.isEqual(
            expectedHash,
            derive(password, decodedSalt, iterations)
        )
    }

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, HASH_BYTES * 8)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
