package com.carefuldata.walktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carefuldata.walktracker.data.WalkSession
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

/**
 * Map screen showing the path of a walk session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    session: WalkSession?,
    pathPoints: List<LatLng>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL
            )
        )
    }
    
    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true
            )
        )
    }
    
    // Calculate camera position based on path points
    val cameraPosition = remember(pathPoints) {
        if (pathPoints.isNotEmpty()) {
            val builder = LatLngBounds.Builder()
            pathPoints.forEach { builder.include(it) }
            val bounds = builder.build()
            
            CameraPosition.fromLatLngZoom(bounds.center, 15f)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { 
                Text(
                    text = if (session != null) {
                        "Walk Path"
                    } else {
                        "Current Walk"
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Map
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                properties = mapProperties,
                uiSettings = uiSettings,
                cameraPositionState = rememberCameraPositionState {
                    position = cameraPosition
                }
            ) {
                // Draw path polyline
                if (pathPoints.size > 1) {
                    Polyline(
                        points = pathPoints,
                        color = Color.Blue,
                        width = 8f
                    )
                }
                
                // Add start marker
                if (pathPoints.isNotEmpty()) {
                    Marker(
                        state = MarkerState(position = pathPoints.first()),
                        title = "Start"
                    )
                }
                
                // Add end marker (if different from start)
                if (pathPoints.size > 1) {
                    Marker(
                        state = MarkerState(position = pathPoints.last()),
                        title = "End"
                    )
                }
            }
            
            // Show message if no path available
            if (pathPoints.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.Center)
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (session != null) {
                            "No path data available for this walk"
                        } else {
                            "Start walking to see your path on the map"
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
