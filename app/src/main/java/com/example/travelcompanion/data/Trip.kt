package com.example.travelcompanion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val city: String,
    val startDate: String,
    val endDate: String
)
