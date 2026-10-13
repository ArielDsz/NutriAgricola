package com.nutriagricola.app.data.repository

import com.nutriagricola.app.data.local.dao.UserDao
import com.nutriagricola.app.data.mapper.toDomain
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.domain.model.user.User

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun getAll(): List<User> {
        return userDao.getAll().map { it.toDomain() }
    }

    suspend fun getById(id: String): User? {
        return userDao.getById(id)?.toDomain()
    }

    suspend fun getByEmail(email: String): User? {
        return userDao.getByEmail(email)?.toDomain()
    }

    suspend fun insert(user: User) {
        userDao.insert(user.toEntity())
    }

    suspend fun update(user: User) {
        userDao.update(user.toEntity())
    }

    suspend fun delete(user: User) {
        userDao.delete(user.toEntity())
    }
}