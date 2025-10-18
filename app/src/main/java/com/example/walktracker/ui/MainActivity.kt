package com.example.walktracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.walktracker.data.WalkSession
import com.example.walktracker.ui.screens.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

/**
 * Main activity for the WalkTracker app.
 * Handles navigation between screens and permission requests.
 */
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    // Permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, can start tracking
        } else {
            // Permission denied, show error
            viewModel.clearError()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WalkTrackerTheme {
                WalkTrackerApp(
                    viewModel = viewModel,
                    onRequestLocationPermission = ::requestLocationPermission
                )
            }
        }
    }
    
    private fun requestLocationPermission() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!fineLocationGranted || !coarseLocationGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@Composable
fun WalkTrackerApp(
    viewModel: MainViewModel,
    onRequestLocationPermission: () -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val walkHistory by viewModel.walkHistory.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    
    // Navigation state
    var selectedSessionForMap by remember { mutableStateOf<WalkSession?>(null) }
    
    // Show error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                uiState = uiState,
                walkHistory = walkHistory,
                onStartSession = {
                    onRequestLocationPermission()
                    viewModel.startSession()
                },
                onPauseSession = { viewModel.pauseSession() },
                onResumeSession = { viewModel.resumeSession() },
                onStopSession = { viewModel.stopSession() },
                onDeleteSession = { session -> viewModel.deleteSession(session) },
                onNavigateToMap = { session ->
                    selectedSessionForMap = session
                    navController.navigate("map")
                },
                onNavigateToCalibration = { navController.navigate("calibration") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("map") {
            MapScreen(
                session = selectedSessionForMap,
                pathPoints = getPathPointsForSession(selectedSessionForMap),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("calibration") {
            CalibrationScreen(
                currentStepLength = uiState.userPrefs.stepLengthMeters,
                isMetric = uiState.userPrefs.isMetric(),
                onNavigateBack = { navController.popBackStack() },
                onSaveStepLength = { stepLength ->
                    viewModel.updateUserPrefs(stepLength = stepLength)
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                userPrefs = uiState.userPrefs,
                onNavigateBack = { navController.popBackStack() },
                onUpdateStepLength = { stepLength ->
                    viewModel.updateUserPrefs(stepLength = stepLength)
                },
                onUpdateUnits = { units ->
                    viewModel.updateUserPrefs(units = units)
                },
                onUpdateUseStepSensor = { useSensor ->
                    viewModel.updateUserPrefs(useStepSensor = useSensor)
                },
                onUpdateEnableMaps = { enableMaps ->
                    viewModel.updateUserPrefs(enableMaps = enableMaps)
                }
            )
        }
    }
}

@Composable
private fun getPathPointsForSession(session: WalkSession?): List<LatLng> {
    // In a real implementation, this would fetch path data from the database
    // For now, return empty list
    return emptyList()
}

@Composable
fun WalkTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography(),
        content = content
    )
}
