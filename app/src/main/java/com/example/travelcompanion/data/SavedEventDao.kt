package com.example.travelcompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedEventDao {
    @Insert
    suspend fun insert(savedEvent: SavedEvent): Long

    @Delete
    suspend fun delete(savedEvent: SavedEvent)

    @Query("SELECT * FROM saved_events WHERE tripId = :tripId ORDER BY startDate")
    suspend fun getByTripId(tripId: Long): List<SavedEvent>

    @Query("SELECT * FROM saved_events WHERE tripId = :tripId AND eventId = :eventId LIMIT 1")
    suspend fun getByTripAndEvent(tripId: Long, eventId: String): SavedEvent?

    @Query("DELETE FROM saved_events WHERE tripId = :tripId AND eventId = :eventId")
    suspend fun deleteByTripAndEvent(tripId: Long, eventId: String)
}
