package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.UserCredentialDao
import com.nutriagricola.app.data.security.PasswordHasher
import com.nutriagricola.app.domain.model.user.User
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val userRepository: UserRepository,
    private val userCredentialDao: UserCredentialDao
) {

    suspend fun authenticate(email: String, password: String): User? {
        val user = userRepository.getByEmail(email.trim().lowercase(Locale.ROOT))
            ?: return null
        if (!user.active) return null

        val credential = userCredentialDao.getByUserId(user.id) ?: return null
        val isValid = withContext(Dispatchers.Default) {
            val passwordChars = password.toCharArray()
            try {
                PasswordHasher.verify(
                    password = passwordChars,
                    passwordHash = credential.passwordHash,
                    salt = credential.salt,
                    iterations = credential.iterations
                )
            } finally {
                passwordChars.fill('\u0000')
            }
        }

        return if (isValid) user else null
    }
}
