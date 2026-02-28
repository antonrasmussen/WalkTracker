package com.carefuldata.walktracker.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carefuldata.walktracker.data.*
import com.carefuldata.walktracker.tracking.LocationService
import com.carefuldata.walktracker.tracking.TrackingStats
import com.carefuldata.walktracker.util.StepCounter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Main ViewModel for the WalkTracker app.
 * Manages the connection to LocationService and coordinates between UI and data layers.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val database = AppDb.getDatabase(context)
    private val walkDao = database.walkDao()
    private val walkPathDao = database.walkPathDao()
    
    // DataStore for user preferences
    private val dataStore = context.userPrefsDataStore
    private val userPrefsFlow = UserPrefs.getFlow(dataStore)
    
    // Service connection
    private var locationService: LocationService? = null
    private var isServiceBound = false
    
    // Step counter
    private var stepCounter: StepCounter? = null
    
    // UI State
    private val _uiState = MutableStateFlow(WalkTrackerUiState())
    val uiState: StateFlow<WalkTrackerUiState> = _uiState.asStateFlow()
    
    // Session data
    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()
    
    // History
    val walkHistory: StateFlow<List<WalkSession>> = walkDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Service connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isServiceBound = true
            
            // Observe service state
            observeServiceState()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            isServiceBound = false
        }
    }
    
    init {
        // Initialize step counter
        stepCounter = StepCounter(context)
        
        // Observe user preferences
        viewModelScope.launch {
            userPrefsFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(userPrefs = prefs)
                stepCounter?.updateStepLength(prefs.stepLengthMeters)
            }
        }
        
        // Check for existing active session on startup
        viewModelScope.launch {
            val activeSession = walkDao.getActiveSession()
            if (activeSession != null) {
                _currentSession.value = activeSession
                _uiState.value = _uiState.value.copy(isTracking = true)
            }
        }
    }
    
    /**
     * Start a new walking session.
     */
    fun startSession() {
        if (_uiState.value.isTracking) return
        
        viewModelScope.launch {
            try {
                // Create new session
                val session = WalkSession(
                    startTimeMillis = System.currentTimeMillis(),
                    isActive = true
                )
                val sessionId = walkDao.insertSession(session)
                _currentSession.value = session.copy(id = sessionId)
                
                // Start location service
                startLocationService()
                
                // Start step counter
                stepCounter?.startTracking()
                
                _uiState.value = _uiState.value.copy(isTracking = true)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to start session: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Pause the current session.
     */
    fun pauseSession() {
        if (!_uiState.value.isTracking) return
        
        locationService?.pauseTracking()
        stepCounter?.stopTracking()
        _uiState.value = _uiState.value.copy(isPaused = true)
    }
    
    /**
     * Resume the current session.
     */
    fun resumeSession() {
        if (!_uiState.value.isTracking || !_uiState.value.isPaused) return
        
        locationService?.resumeTracking()
        stepCounter?.startTracking(_uiState.value.totalDistance)
        _uiState.value = _uiState.value.copy(isPaused = false)
    }
    
    /**
     * Stop the current session and save to database.
     */
    fun stopSession() {
        if (!_uiState.value.isTracking) return
        
        viewModelScope.launch {
            try {
                val session = _currentSession.value ?: return@launch
                val endTime = System.currentTimeMillis()
                
                // Get final stats from service
                val stats = locationService?.getTrackingStats()
                val finalDistance = stats?.totalDistance ?: 0.0
                val finalSteps = stepCounter?.getCurrentStepCount() ?: 0
                
                // Update session with final data
                val completedSession = session.copy(
                    endTimeMillis = endTime,
                    distanceMeters = finalDistance,
                    isActive = false,
                    stepsFromSensor = if (stepCounter?.hasStepSensor() == true) finalSteps else null,
                    stepsFromDistance = if (stepCounter?.hasStepSensor() != true) finalSteps else null
                )
                
                walkDao.updateSession(completedSession)
                
                // Save path if available and maps are enabled
                if (_uiState.value.userPrefs.enableMaps && stats?.pathPoints?.isNotEmpty() == true) {
                    savePathToDatabase(completedSession.id, stats.pathPoints)
                }
                
                // Stop services
                stopLocationService()
                stepCounter?.stopTracking()
                
                // Reset state
                _currentSession.value = null
                _uiState.value = _uiState.value.copy(
                    isTracking = false,
                    isPaused = false,
                    totalDistance = 0.0,
                    currentSteps = 0
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to stop session: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete a completed session from history.
     */
    fun deleteSession(session: WalkSession) {
        viewModelScope.launch {
            try {
                walkDao.deleteSession(session)
                walkPathDao.deletePathsForSession(session.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete session: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Update user preferences.
     */
    fun updateUserPrefs(stepLength: Double? = null, units: String? = null, useStepSensor: Boolean? = null, enableMaps: Boolean? = null) {
        viewModelScope.launch {
            UserPrefs.update(dataStore, stepLength, units, useStepSensor, enableMaps)
        }
    }
    
    private fun startLocationService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START_TRACKING
        }
        context.startForegroundService(intent)
        
        // Bind to service to get updates
        context.bindService(
            Intent(context, LocationService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }
    
    private fun stopLocationService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
        
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    private fun observeServiceState() {
        viewModelScope.launch {
            locationService?.isTracking?.collect { isTracking ->
                if (!isTracking && _uiState.value.isTracking) {
                    // Service was stopped externally
                    stopSession()
                }
            }
        }
        
        viewModelScope.launch {
            locationService?.totalDistance?.collect { distance ->
                _uiState.value = _uiState.value.copy(totalDistance = distance)
                
                // Update step counter if using distance-based counting
                if (stepCounter?.hasStepSensor() != true) {
                    stepCounter?.updateFromDistance(distance)
                }
            }
        }
        
        viewModelScope.launch {
            stepCounter?.stepCount?.collect { steps ->
                _uiState.value = _uiState.value.copy(currentSteps = steps)
            }
        }
    }
    
    private suspend fun savePathToDatabase(sessionId: Long, pathPoints: List<android.location.Location>) {
        if (pathPoints.isEmpty()) return
        
        try {
            // Convert locations to encoded polyline
            val encodedPolyline = encodePolyline(pathPoints)
            
            // Save to database
            val walkPath = WalkPath(
                sessionId = sessionId,
                encodedPolyline = encodedPolyline,
                sequenceOrder = 0
            )
            walkPathDao.insertPath(walkPath)
        } catch (e: Exception) {
            // Log error but don't fail the session
            e.printStackTrace()
        }
    }
    
    private fun encodePolyline(points: List<android.location.Location>): String {
        // Simple polyline encoding (in production, use Google Maps Utils)
        val latLngs = points.map { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
        return com.google.maps.android.PolyUtil.encode(latLngs)
    }
    
    override fun onCleared() {
        super.onCleared()
        stepCounter?.cleanup()
        if (isServiceBound) {
            context.unbindService(serviceConnection)
        }
    }
}

/**
 * UI state for the WalkTracker app.
 */
data class WalkTrackerUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val totalDistance: Double = 0.0,
    val currentSteps: Int = 0,
    val userPrefs: UserPrefs = UserPrefs(),
    val errorMessage: String? = null
)
