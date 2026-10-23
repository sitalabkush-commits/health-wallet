package com.example.medwallet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM medical_profile WHERE id = 1")
    fun observe(): Flow<MedicalProfile?>

    @Query("SELECT * FROM medical_profile WHERE id = 1")
    suspend fun get(): MedicalProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: MedicalProfile)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM medical_documents ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<MedicalDocument>>

    @Insert
    suspend fun insert(doc: MedicalDocument): Long

    @Delete
    suspend fun delete(doc: MedicalDocument)
}
