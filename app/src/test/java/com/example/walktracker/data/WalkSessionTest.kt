package com.example.walktracker.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for WalkSession data class.
 */
class WalkSessionTest {
    
    @Test
    fun getDurationMillis_completedSession_returnsCorrectDuration() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = 5000,
            distanceMeters = 100.0,
            isActive = false
        )
        
        val duration = session.getDurationMillis()
        assertEquals(4000, duration)
    }
    
    @Test
    fun getDurationMillis_activeSession_returnsCurrentDuration() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = null,
            distanceMeters = 100.0,
            isActive = true
        )
        
        val currentTime = 3000L
        val duration = session.getDurationMillis(currentTime)
        assertEquals(2000, duration)
    }
    
    @Test
    fun getPrimaryStepCount_hasSensorSteps_returnsSensorSteps() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = 5000,
            distanceMeters = 100.0,
            isActive = false,
            stepsFromSensor = 150,
            stepsFromDistance = 128
        )
        
        val stepCount = session.getPrimaryStepCount()
        assertEquals(150, stepCount)
    }
    
    @Test
    fun getPrimaryStepCount_noSensorSteps_returnsDistanceSteps() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = 5000,
            distanceMeters = 100.0,
            isActive = false,
            stepsFromSensor = null,
            stepsFromDistance = 128
        )
        
        val stepCount = session.getPrimaryStepCount()
        assertEquals(128, stepCount)
    }
    
    @Test
    fun getPrimaryStepCount_noSteps_returnsZero() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = 5000,
            distanceMeters = 100.0,
            isActive = false,
            stepsFromSensor = null,
            stepsFromDistance = null
        )
        
        val stepCount = session.getPrimaryStepCount()
        assertEquals(0, stepCount)
    }
    
    @Test
    fun isCompleted_hasEndTime_returnsTrue() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = 5000,
            distanceMeters = 100.0,
            isActive = false
        )
        
        assertTrue(session.isCompleted())
    }
    
    @Test
    fun isCompleted_noEndTime_returnsFalse() {
        val session = WalkSession(
            id = 1,
            startTimeMillis = 1000,
            endTimeMillis = null,
            distanceMeters = 100.0,
            isActive = true
        )
        
        assertFalse(session.isCompleted())
    }
}
