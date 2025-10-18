package com.example.walktracker.tracking

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.walktracker.R
import com.example.walktracker.ui.MainActivity
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Foreground service for tracking GPS location during walking sessions.
 * Implements high-accuracy GPS tracking with noise filtering and distance accumulation.
 */
class LocationService : Service() {
    
    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    // Location tracking state
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()
    
    private val _pathPoints = MutableStateFlow<List<Location>>(emptyList())
    val pathPoints: StateFlow<List<Location>> = _pathPoints.asStateFlow()
    
    // Tracking parameters
    private var lastValidLocation: Location? = null
    private var sessionStartTime: Long = 0
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        
        // Location filtering parameters
        private const val MAX_ACCURACY_METERS = 25.0
        private const val MAX_AGE_MILLIS = 30000 // 30 seconds
        private const val MIN_MOVE_THRESHOLD_METERS = 1.0
        private const val PATH_SMOOTHING_THRESHOLD_METERS = 3.0
        
        // Location request parameters
        private const val UPDATE_INTERVAL_MILLIS = 2000L // 2 seconds
        private const val MIN_DISTANCE_METERS = 5.0f
        private const val MAX_UPDATE_DELAY_MILLIS = 10000L // 10 seconds
    }
    
    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
            ACTION_PAUSE_TRACKING -> pauseTracking()
            ACTION_RESUME_TRACKING -> resumeTracking()
        }
        return START_STICKY
    }
    
    /**
     * Start location tracking with foreground notification.
     */
    fun startTracking() {
        if (_isTracking.value) return
        
        if (!hasLocationPermission()) {
            // Permission not granted - service will be stopped
            stopSelf()
            return
        }
        
        _isTracking.value = true
        sessionStartTime = System.currentTimeMillis()
        _totalDistance.value = 0.0
        _pathPoints.value = emptyList()
        lastValidLocation = null
        
        startForeground(NOTIFICATION_ID, createNotification())
        requestLocationUpdates()
    }
    
    /**
     * Stop location tracking and remove foreground notification.
     */
    fun stopTracking() {
        if (!_isTracking.value) return
        
        _isTracking.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }
    
    /**
     * Pause location tracking (keeps service running but stops updates).
     */
    fun pauseTracking() {
        if (!_isTracking.value) return
        
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    /**
     * Resume location tracking after pause.
     */
    fun resumeTracking() {
        if (!_isTracking.value) return
        
        requestLocationUpdates()
    }
    
    /**
     * Get current tracking statistics.
     */
    fun getTrackingStats(): TrackingStats {
        val duration = if (_isTracking.value) {
            System.currentTimeMillis() - sessionStartTime
        } else 0L
        
        return TrackingStats(
            isTracking = _isTracking.value,
            totalDistance = _totalDistance.value,
            duration = duration,
            pathPoints = _pathPoints.value.toList()
        )
    }
    
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    processLocationUpdate(location)
                }
            }
        }
    }
    
    private fun processLocationUpdate(location: Location) {
        // Apply location filtering
        if (!isLocationValid(location)) return
        
        val currentTime = System.currentTimeMillis()
        
        // Update current location
        _currentLocation.value = location
        
        // Calculate distance from last valid location
        lastValidLocation?.let { lastLocation ->
            val distance = lastLocation.distanceTo(location)
            
            // Only accumulate distance if movement is significant
            if (distance >= MIN_MOVE_THRESHOLD_METERS) {
                _totalDistance.value += distance
                
                // Add to path if movement is significant enough for smoothing
                if (distance >= PATH_SMOOTHING_THRESHOLD_METERS) {
                    val currentPath = _pathPoints.value.toMutableList()
                    currentPath.add(location)
                    _pathPoints.value = currentPath
                }
            }
        }
        
        lastValidLocation = location
    }
    
    private fun isLocationValid(location: Location): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Check accuracy
        if (location.accuracy > MAX_ACCURACY_METERS) return false
        
        // Check age
        if (currentTime - location.time > MAX_AGE_MILLIS) return false
        
        // Check for obvious errors (speed > 100 m/s is unrealistic for walking)
        lastValidLocation?.let { lastLocation ->
            val timeDiff = (location.time - lastLocation.time) / 1000.0
            if (timeDiff > 0) {
                val distance = lastLocation.distanceTo(location)
                val speed = distance / timeDiff
                if (speed > 100.0) return false // > 360 km/h is unrealistic
            }
        }
        
        return true
    }
    
    private fun requestLocationUpdates() {
        if (!hasLocationPermission()) return
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MILLIS
        )
        .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
        .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_MILLIS)
        .setWaitForAccurateLocation(false)
        .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission was revoked
            stopTracking()
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when location tracking is active"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WalkTracker - Tracking Active")
            .setContentText("GPS location tracking is running")
            .setSmallIcon(R.drawable.ic_walking)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (_isTracking.value) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    companion object {
        const val ACTION_START_TRACKING = "com.example.walktracker.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.example.walktracker.STOP_TRACKING"
        const val ACTION_PAUSE_TRACKING = "com.example.walktracker.PAUSE_TRACKING"
        const val ACTION_RESUME_TRACKING = "com.example.walktracker.RESUME_TRACKING"
    }
}

/**
 * Data class containing current tracking statistics.
 */
data class TrackingStats(
    val isTracking: Boolean,
    val totalDistance: Double,
    val duration: Long,
    val pathPoints: List<Location>
)
