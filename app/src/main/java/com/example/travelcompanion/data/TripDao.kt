package com.example.travelcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips")
    suspend fun getAll(): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: Long): Trip?

    @Query("SELECT * FROM trips WHERE userId = :userId")
    suspend fun getByUserId(userId: Long): List<Trip>
}
