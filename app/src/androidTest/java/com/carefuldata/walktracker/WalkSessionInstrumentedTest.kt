package com.carefuldata.walktracker

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carefuldata.walktracker.data.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for WalkSession database operations.
 */
@RunWith(AndroidJUnit4::class)
class WalkSessionInstrumentedTest {
    
    private lateinit var database: AppDb
    private lateinit var walkDao: WalkDao
    private lateinit var walkPathDao: WalkPathDao
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDb::class.java
        ).allowMainThreadQueries().build()
        
        walkDao = database.walkDao()
        walkPathDao = database.walkPathDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetSession() = runBlocking {
        val session = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            distanceMeters = 100.0,
            isActive = false
        )
        
        val sessionId = walkDao.insertSession(session)
        val retrievedSession = walkDao.getSessionById(sessionId)
        
        assertNotNull(retrievedSession)
        assertEquals(sessionId, retrievedSession?.id)
        assertEquals(100.0, retrievedSession?.distanceMeters, 0.01)
        assertFalse(retrievedSession?.isActive ?: true)
    }
    
    @Test
    fun insertAndGetActiveSession() = runBlocking {
        val activeSession = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            isActive = true
        )
        
        val completedSession = WalkSession(
            startTimeMillis = System.currentTimeMillis() - 10000,
            endTimeMillis = System.currentTimeMillis() - 5000,
            distanceMeters = 50.0,
            isActive = false
        )
        
        walkDao.insertSession(activeSession)
        walkDao.insertSession(completedSession)
        
        val retrievedActiveSession = walkDao.getActiveSession()
        
        assertNotNull(retrievedActiveSession)
        assertTrue(retrievedActiveSession?.isActive ?: false)
    }
    
    @Test
    fun updateSession() = runBlocking {
        val session = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            isActive = true
        )
        
        val sessionId = walkDao.insertSession(session)
        val updatedSession = session.copy(
            id = sessionId,
            endTimeMillis = System.currentTimeMillis(),
            distanceMeters = 200.0,
            isActive = false
        )
        
        walkDao.updateSession(updatedSession)
        val retrievedSession = walkDao.getSessionById(sessionId)
        
        assertNotNull(retrievedSession)
        assertEquals(200.0, retrievedSession?.distanceMeters, 0.01)
        assertFalse(retrievedSession?.isActive ?: true)
        assertNotNull(retrievedSession?.endTimeMillis)
    }
    
    @Test
    fun deleteSession() = runBlocking {
        val session = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            distanceMeters = 100.0,
            isActive = false
        )
        
        val sessionId = walkDao.insertSession(session)
        val insertedSession = session.copy(id = sessionId)
        
        walkDao.deleteSession(insertedSession)
        val retrievedSession = walkDao.getSessionById(sessionId)
        
        assertNull(retrievedSession)
    }
    
    @Test
    fun insertAndGetWalkPath() = runBlocking {
        val session = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            distanceMeters = 100.0,
            isActive = false
        )
        
        val sessionId = walkDao.insertSession(session)
        val walkPath = WalkPath(
            sessionId = sessionId,
            encodedPolyline = "test_polyline_data",
            sequenceOrder = 0
        )
        
        walkPathDao.insertPath(walkPath)
        val retrievedPaths = walkPathDao.getPathsForSession(sessionId)
        
        assertEquals(1, retrievedPaths.size)
        assertEquals("test_polyline_data", retrievedPaths[0].encodedPolyline)
    }
    
    @Test
    fun getTotalDistance() = runBlocking {
        val session1 = WalkSession(
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() + 1000,
            distanceMeters = 100.0,
            isActive = false
        )
        
        val session2 = WalkSession(
            startTimeMillis = System.currentTimeMillis() + 2000,
            endTimeMillis = System.currentTimeMillis() + 3000,
            distanceMeters = 200.0,
            isActive = false
        )
        
        walkDao.insertSession(session1)
        walkDao.insertSession(session2)
        
        val totalDistance = walkDao.getTotalDistance()
        
        assertEquals(300.0, totalDistance, 0.01)
    }
}
