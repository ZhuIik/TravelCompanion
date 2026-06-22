package com.example.travelcompanion.data

class SavedEventRepository(private val dao: SavedEventDao) {

    suspend fun getForTrip(tripId: Long): List<SavedEvent> = dao.getByTripId(tripId)

    suspend fun isSaved(tripId: Long, eventId: String): Boolean =
        dao.getByTripAndEvent(tripId, eventId) != null

    suspend fun save(savedEvent: SavedEvent): Long = dao.insert(savedEvent)

    suspend fun delete(savedEvent: SavedEvent) = dao.delete(savedEvent)

    suspend fun removeByEvent(tripId: Long, eventId: String) =
        dao.deleteByTripAndEvent(tripId, eventId)
}
