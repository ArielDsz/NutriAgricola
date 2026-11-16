package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.WaterDataEntity

@Dao
interface WaterDataDao {

    @Insert
    suspend fun insert(waterData: WaterDataEntity)

    @Query("SELECT * FROM water_data WHERE recordId = :recordId")
    suspend fun getByRecordId(recordId: String): WaterDataEntity?

    @Update
    suspend fun update(waterData: WaterDataEntity)

    @Delete
    suspend fun delete(waterData: WaterDataEntity)
}