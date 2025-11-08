package com.example.openwebuieink.data

import kotlinx.coroutines.flow.Flow

class ConnectionProfileRepository(private val connectionProfileDao: ConnectionProfileDao) {

    fun getAll(): Flow<List<ConnectionProfile>> = connectionProfileDao.getAll()

    suspend fun insert(profile: ConnectionProfile) {
        connectionProfileDao.insert(profile)
    }

    suspend fun update(profile: ConnectionProfile) {
        connectionProfileDao.update(profile)
    }

    suspend fun delete(profile: ConnectionProfile) {
        connectionProfileDao.delete(profile)
    }
}
