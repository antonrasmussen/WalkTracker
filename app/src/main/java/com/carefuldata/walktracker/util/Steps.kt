package com.carefuldata.walktracker.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility class for step counting using device sensors.
 * Provides both sensor-based step counting and distance-based estimation.
 */
class StepCounter(
    private val context: Context,
    private val stepLengthMeters: Double = 0.78
) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()
    
    private val _isSensorAvailable = MutableStateFlow(stepCounterSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()
    
    private var baseStepCount = 0
    private var isTracking = false
    
    /**
     * Check if the device has a step counter sensor.
     */
    fun hasStepSensor(): Boolean = stepCounterSensor != null
    
    /**
     * Start step counting. If sensor is available, use it; otherwise use distance-based estimation.
     * 
     * @param initialDistanceMeters Current distance for fallback calculation
     */
    fun startTracking(initialDistanceMeters: Double = 0.0) {
        isTracking = true
        
        if (hasStepSensor()) {
            // Use sensor-based counting
            baseStepCount = _stepCount.value
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // Use distance-based estimation
            _stepCount.value = (initialDistanceMeters / stepLengthMeters).toInt()
        }
    }
    
    /**
     * Stop step counting and unregister sensor listener.
     */
    fun stopTracking() {
        isTracking = false
        if (hasStepSensor()) {
            sensorManager.unregisterListener(this)
        }
    }
    
    /**
     * Update step count based on distance (fallback method).
     * 
     * @param distanceMeters Total distance covered
     */
    fun updateFromDistance(distanceMeters: Double) {
        if (!hasStepSensor() && isTracking) {
            _stepCount.value = (distanceMeters / stepLengthMeters).toInt()
        }
    }
    
    /**
     * Reset step count to zero.
     */
    fun reset() {
        _stepCount.value = 0
        baseStepCount = 0
    }
    
    /**
     * Get the current step count.
     */
    fun getCurrentStepCount(): Int = _stepCount.value
    
    /**
     * Calculate steps from distance using the configured step length.
     * 
     * @param distanceMeters Distance in meters
     * @return Estimated number of steps
     */
    fun calculateStepsFromDistance(distanceMeters: Double): Int {
        return (distanceMeters / stepLengthMeters).toInt()
    }
    
    /**
     * Calculate step length from known distance and step count.
     * 
     * @param distanceMeters Known distance in meters
     * @param stepCount Number of steps taken
     * @return Calculated step length in meters
     */
    fun calculateStepLength(distanceMeters: Double, stepCount: Int): Double {
        return if (stepCount > 0) distanceMeters / stepCount else 0.0
    }
    
    /**
     * Update the step length for distance-based calculations.
     * 
     * @param newStepLengthMeters New step length in meters
     */
    fun updateStepLength(newStepLengthMeters: Double) {
        // If we're currently using distance-based counting, recalculate
        if (!hasStepSensor() && isTracking) {
            val currentDistance = _stepCount.value * stepLengthMeters
            _stepCount.value = (currentDistance / newStepLengthMeters).toInt()
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && isTracking) {
            val currentSensorSteps = event.values[0].toInt()
            _stepCount.value = maxOf(0, currentSensorSteps - baseStepCount)
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    /**
     * Clean up resources when done.
     */
    fun cleanup() {
        stopTracking()
    }
}
