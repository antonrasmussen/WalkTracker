package com.example.walktracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WalkPath operations.
 * Provides methods to interact with the walk_paths table.
 */
@Dao
interface WalkPathDao {
    
    /**
     * Get all path segments for a specific session, ordered by sequence.
     */
    @Query("SELECT * FROM walk_paths WHERE sessionId = :sessionId ORDER BY sequenceOrder ASC")
    suspend fun getPathsForSession(sessionId: Long): List<WalkPath>
    
    /**
     * Get all path segments for a specific session as a Flow.
     */
    @Query("SELECT * FROM walk_paths WHERE sessionId = :sessionId ORDER BY sequenceOrder ASC")
    fun getPathsForSessionFlow(sessionId: Long): Flow<List<WalkPath>>
    
    /**
     * Insert a new path segment.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPath(path: WalkPath): Long
    
    /**
     * Insert multiple path segments.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaths(paths: List<WalkPath>)
    
    /**
     * Delete all path segments for a specific session.
     */
    @Query("DELETE FROM walk_paths WHERE sessionId = :sessionId")
    suspend fun deletePathsForSession(sessionId: Long)
    
    /**
     * Delete a specific path segment.
     */
    @Delete
    suspend fun deletePath(path: WalkPath)
    
    /**
     * Get the latest path segment for a session (for active tracking).
     */
    @Query("SELECT * FROM walk_paths WHERE sessionId = :sessionId ORDER BY sequenceOrder DESC LIMIT 1")
    suspend fun getLatestPathForSession(sessionId: Long): WalkPath?
}
