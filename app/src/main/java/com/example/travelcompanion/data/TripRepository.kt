package com.example.travelcompanion.data

class TripRepository(private val tripDao: TripDao) {

    suspend fun getTripsForUser(userId: Long): List<Trip> = tripDao.getByUserId(userId)

    suspend fun getTripById(tripId: Long): Trip? = tripDao.getById(tripId)

    suspend fun createTrip(userId: Long, city: String, startDate: String, endDate: String): Long {
        return tripDao.insert(
            Trip(userId = userId, city = city, startDate = startDate, endDate = endDate)
        )
    }

    suspend fun deleteTrip(trip: Trip) = tripDao.delete(trip)
}
