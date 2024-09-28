package dev.ridill.rivo.core.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao
interface BaseDao<T> {

    @Upsert
    suspend fun upsert(vararg entities: T): List<Long>

    @Delete
    suspend fun delete(vararg entities: T)
}