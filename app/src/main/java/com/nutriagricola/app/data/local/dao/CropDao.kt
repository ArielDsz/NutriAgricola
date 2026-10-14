package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.CropEntity

@Dao
interface CropDao {

    @Insert
    suspend fun insert(crop: CropEntity)

    @Query("SELECT * FROM crops")
    suspend fun getAll(): List<CropEntity>

    @Query("SELECT * FROM crops WHERE farmId = :farmId")
    suspend fun getByFarmId(farmId: String): List<CropEntity>

    @Query("SELECT * FROM crops WHERE id = :id")
    suspend fun getById(id: String): CropEntity?

    @Update
    suspend fun update(crop: CropEntity)

    @Delete
    suspend fun delete(crop: CropEntity)
}
