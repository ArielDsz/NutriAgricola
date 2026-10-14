package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nutriagricola.app.data.local.entity.UserCredentialEntity

@Dao
interface UserCredentialDao {

    @Insert
    suspend fun insert(credential: UserCredentialEntity)

    @Query("SELECT * FROM user_credentials WHERE userId = :userId")
    suspend fun getByUserId(userId: String): UserCredentialEntity?
}
