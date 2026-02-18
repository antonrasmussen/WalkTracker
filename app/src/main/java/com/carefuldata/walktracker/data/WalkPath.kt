package com.carefuldata.walktracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Represents a path segment for a walking session.
 * Paths are stored as encoded polylines to save space and allow for efficient storage
 * of complex routes. Multiple segments can be stored per session for chunking.
 * 
 * @param id Unique identifier for the path segment
 * @param sessionId Foreign key to the associated WalkSession
 * @param encodedPolyline Encoded polyline string containing the path coordinates
 * @param sequenceOrder Order of this segment within the session (for proper reconstruction)
 */
@Entity(
    tableName = "walk_paths",
    foreignKeys = [
        ForeignKey(
            entity = WalkSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId", "sequenceOrder"])]
)
data class WalkPath(
    val id: Long = 0,
    val sessionId: Long,
    val encodedPolyline: String,
    val sequenceOrder: Int = 0
)
