package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.FarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    @Insert
    suspend fun insert(farm: FarmEntity)

    @Query("SELECT * FROM farms")
    suspend fun getAll(): List<FarmEntity>

    @Query("SELECT * FROM farms ORDER BY name COLLATE NOCASE, id")
    fun observeAll(): Flow<List<FarmEntity>>

    @Update
    suspend fun update(farm: FarmEntity)

    @Delete
    suspend fun delete(farm: FarmEntity)
}
