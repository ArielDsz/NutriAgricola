package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.Transaction
import com.nutriagricola.app.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {

    @Upsert
    suspend fun upsertAll(crops: List<CropEntity>)

    @Query("UPDATE crops SET active = 0 WHERE farmId = :farmId")
    suspend fun deactivateByFarm(farmId: String)

    @Transaction
    suspend fun replaceRemoteCrops(farmId: String, crops: List<CropEntity>) {
        require(crops.all { it.farmId == farmId })
        // Retain rows referenced by local records, including crops no longer returned.
        deactivateByFarm(farmId)
        upsertAll(crops)
    }

    @Insert
    suspend fun insert(crop: CropEntity)

    @Query("SELECT * FROM crops")
    suspend fun getAll(): List<CropEntity>

    @Query("SELECT * FROM crops WHERE farmId = :farmId")
    suspend fun getByFarmId(farmId: String): List<CropEntity>

    @Query("SELECT * FROM crops WHERE farmId = :farmId ORDER BY name COLLATE NOCASE, id")
    fun observeByFarmId(farmId: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :id")
    suspend fun getById(id: String): CropEntity?

    @Update
    suspend fun update(crop: CropEntity)

    @Delete
    suspend fun delete(crop: CropEntity)
}
