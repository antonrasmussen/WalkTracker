package com.carefuldata.walktracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WalkSession operations.
 * Provides methods to interact with the walk_sessions table.
 */
@Dao
interface WalkDao {
    
    /**
     * Get all walk sessions ordered by start time (newest first).
     */
    @Query("SELECT * FROM walk_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<WalkSession>>
    
    /**
     * Get the currently active session.
     */
    @Query("SELECT * FROM walk_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): WalkSession?
    
    /**
     * Get a specific session by ID.
     */
    @Query("SELECT * FROM walk_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WalkSession?
    
    /**
     * Insert a new walk session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WalkSession): Long
    
    /**
     * Update an existing walk session.
     */
    @Update
    suspend fun updateSession(session: WalkSession)
    
    /**
     * Delete a walk session.
     */
    @Delete
    suspend fun deleteSession(session: WalkSession)
    
    /**
     * Delete all completed sessions (for cleanup).
     */
    @Query("DELETE FROM walk_sessions WHERE isActive = 0")
    suspend fun deleteCompletedSessions()
    
    /**
     * Get total distance across all sessions.
     */
    @Query("SELECT SUM(distanceMeters) FROM walk_sessions WHERE isActive = 0")
    suspend fun getTotalDistance(): Double?
    
    /**
     * Get total steps across all sessions.
     */
    @Query("SELECT SUM(CASE WHEN stepsFromSensor IS NOT NULL THEN stepsFromSensor ELSE stepsFromDistance END) FROM walk_sessions WHERE isActive = 0")
    suspend fun getTotalSteps(): Int?
}
