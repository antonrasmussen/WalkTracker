package com.carefuldata.walktracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a walking session with tracking data.
 * 
 * @param id Unique identifier for the session
 * @param startTimeMillis When the session started (in milliseconds since epoch)
 * @param endTimeMillis When the session ended (null if still active)
 * @param distanceMeters Total distance covered in meters
 * @param isActive Whether the session is currently active
 * @param stepsFromSensor Steps counted by device sensor (if available)
 * @param stepsFromDistance Steps estimated from distance using step length
 */
@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val distanceMeters: Double = 0.0,
    val isActive: Boolean = false,
    val stepsFromSensor: Int? = null,
    val stepsFromDistance: Int? = null
) {
    /**
     * Get the duration of the session in milliseconds.
     * Returns current time - start time if session is active.
     */
    fun getDurationMillis(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        val endTime = endTimeMillis ?: currentTimeMillis
        return endTime - startTimeMillis
    }
    
    /**
     * Get the primary step count (sensor if available, otherwise distance-based).
     */
    fun getPrimaryStepCount(): Int {
        return stepsFromSensor ?: stepsFromDistance ?: 0
    }
    
    /**
     * Check if the session is completed (has an end time).
     */
    fun isCompleted(): Boolean = endTimeMillis != null
}
