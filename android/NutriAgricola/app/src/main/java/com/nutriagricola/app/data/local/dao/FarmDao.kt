package com.nutriagricola.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import androidx.room.Transaction
import com.nutriagricola.app.data.local.entity.FarmUserEntity
import com.nutriagricola.app.data.local.entity.FarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    @Query("SELECT f.* FROM farms f INNER JOIN farm_users fu ON f.id = fu.farmId WHERE fu.userId = :userId AND f.active = 1 ORDER BY f.name COLLATE NOCASE, f.id")
    fun observeByUser(userId: String): Flow<List<FarmEntity>>

    @Upsert
    suspend fun upsertAll(farms: List<FarmEntity>)

    @Insert
    suspend fun insertAssignments(assignments: List<FarmUserEntity>)

    @Query("DELETE FROM farm_users WHERE userId = :userId")
    suspend fun deleteAssignments(userId: String)

    @Transaction
    suspend fun replaceAssignedFarms(userId: String, farms: List<FarmEntity>) {
        upsertAll(farms)
        deleteAssignments(userId)
        insertAssignments(farms.map { FarmUserEntity(userId, it.id) })
    }

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
