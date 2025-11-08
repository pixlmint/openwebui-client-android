package com.example.openwebuieink.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionProfileDao {
    @Query("SELECT * FROM connection_profiles")
    fun getAll(): Flow<List<ConnectionProfile>>

    @Insert
    suspend fun insert(profile: ConnectionProfile)

    @Update
    suspend fun update(profile: ConnectionProfile)

    @Delete
    suspend fun delete(profile: ConnectionProfile)
}
