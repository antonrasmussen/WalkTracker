package com.carefuldata.walktracker.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * Room database for the WalkTracker app.
 * Contains tables for walk sessions and their associated paths.
 */
@Database(
    entities = [WalkSession::class, WalkPath::class],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    
    abstract fun walkDao(): WalkDao
    abstract fun walkPathDao(): WalkPathDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null
        
        /**
         * Get the database instance using singleton pattern.
         * Creates the database if it doesn't exist.
         */
        fun getDatabase(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "walktracker_database"
                )
                .fallbackToDestructiveMigration() // For development - in production, implement proper migrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
