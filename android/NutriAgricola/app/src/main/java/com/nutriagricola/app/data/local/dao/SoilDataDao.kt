package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nutriagricola.app.data.local.entity.SoilDataEntity

@Dao
interface SoilDataDao {

    @Insert
    suspend fun insert(soilData: SoilDataEntity)

    @Query("SELECT * FROM soil_data WHERE recordId = :recordId")
    suspend fun getByRecordId(recordId: String): SoilDataEntity?

    @Update
    suspend fun update(soilData: SoilDataEntity)

    @Delete
    suspend fun delete(soilData: SoilDataEntity)
}