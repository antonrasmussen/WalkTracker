package com.carefuldata.walktracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User preferences data class containing app settings.
 * 
 * @param stepLengthMeters Average step length in meters (default: 0.78m)
 * @param units Unit system: "metric" or "imperial"
 * @param useStepSensor Whether to use device step sensor if available
 * @param enableMaps Whether to enable Google Maps functionality
 */
data class UserPrefs(
    val stepLengthMeters: Double = 0.78,
    val units: String = "metric",
    val useStepSensor: Boolean = true,
    val enableMaps: Boolean = true
) {
    companion object {
        private val STEP_LENGTH_KEY = doublePreferencesKey("step_length_meters")
        private val UNITS_KEY = stringPreferencesKey("units")
        private val USE_STEP_SENSOR_KEY = booleanPreferencesKey("use_step_sensor")
        private val ENABLE_MAPS_KEY = booleanPreferencesKey("enable_maps")
        
        /**
         * Convert DataStore preferences to UserPrefs object.
         */
        fun fromPreferences(preferences: Preferences): UserPrefs {
            return UserPrefs(
                stepLengthMeters = preferences[STEP_LENGTH_KEY] ?: 0.78,
                units = preferences[UNITS_KEY] ?: "metric",
                useStepSensor = preferences[USE_STEP_SENSOR_KEY] ?: true,
                enableMaps = preferences[ENABLE_MAPS_KEY] ?: true
            )
        }
        
        /**
         * Get a Flow of UserPrefs from DataStore.
         */
        fun getFlow(dataStore: DataStore<Preferences>): Flow<UserPrefs> {
            return dataStore.data.map { preferences ->
                fromPreferences(preferences)
            }
        }
        
        /**
         * Update DataStore with new preference values (only non-null params are written).
         */
        suspend fun update(
            dataStore: DataStore<Preferences>,
            stepLengthMeters: Double? = null,
            units: String? = null,
            useStepSensor: Boolean? = null,
            enableMaps: Boolean? = null
        ) {
            dataStore.edit { prefs ->
                stepLengthMeters?.let { prefs[STEP_LENGTH_KEY] = it }
                units?.let { prefs[UNITS_KEY] = it }
                useStepSensor?.let { prefs[USE_STEP_SENSOR_KEY] = it }
                enableMaps?.let { prefs[ENABLE_MAPS_KEY] = it }
            }
        }
    }
    
    /**
     * Check if using metric units (km, m).
     */
    fun isMetric(): Boolean = units == "metric"
    
    /**
     * Get distance unit string.
     */
    fun getDistanceUnit(): String = if (isMetric()) "km" else "mi"
    
    /**
     * Get small distance unit string.
     */
    fun getSmallDistanceUnit(): String = if (isMetric()) "m" else "ft"
    
    /**
     * Convert meters to the user's preferred unit.
     */
    fun convertDistance(meters: Double): Double {
        return if (isMetric()) {
            meters / 1000.0 // Convert to km
        } else {
            meters * 3.28084 / 1000.0 // Convert to miles
        }
    }
    
    /**
     * Convert user's preferred unit back to meters.
     */
    fun convertToMeters(distance: Double): Double {
        return if (isMetric()) {
            distance * 1000.0 // Convert from km
        } else {
            distance * 1609.34 // Convert from miles
        }
    }
}
