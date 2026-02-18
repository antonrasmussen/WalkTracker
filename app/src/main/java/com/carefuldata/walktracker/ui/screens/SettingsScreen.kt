package com.carefuldata.walktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carefuldata.walktracker.data.UserPrefs
import com.carefuldata.walktracker.util.Formatters

/**
 * Settings screen for configuring app preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPrefs: UserPrefs,
    onNavigateBack: () -> Unit,
    onUpdateStepLength: (Double) -> Unit,
    onUpdateUnits: (String) -> Unit,
    onUpdateUseStepSensor: (Boolean) -> Unit,
    onUpdateEnableMaps: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var stepLengthText by remember { mutableStateOf(userPrefs.stepLengthMeters.toString()) }
    var showStepLengthError by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Length Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Step Length",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your average step length is used to estimate steps from distance when the step sensor is not available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = stepLengthText,
                        onValueChange = { 
                            stepLengthText = it
                            showStepLengthError = false
                        },
                        label = { 
                            Text("Step Length (${if (userPrefs.isMetric()) "cm" else "inches"})") 
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = showStepLengthError,
                        supportingText = if (showStepLengthError) {
                            { Text("Please enter a valid step length") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Current: ${Formatters.formatStepLength(userPrefs.stepLengthMeters, userPrefs.isMetric())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val newStepLength = stepLengthText.toDoubleOrNull()
                            if (newStepLength != null && newStepLength > 0) {
                                // Convert from display units to meters
                                val stepLengthMeters = if (userPrefs.isMetric()) {
                                    newStepLength / 100.0 // cm to meters
                                } else {
                                    newStepLength * 0.0254 // inches to meters
                                }
                                onUpdateStepLength(stepLengthMeters)
                            } else {
                                showStepLengthError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Step Length")
                    }
                }
            }
            
            // Units Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Choose between metric (km, m) or imperial (miles, feet) units.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { onUpdateUnits("metric") },
                            label = { Text("Metric") },
                            selected = userPrefs.isMetric()
                        )
                        
                        FilterChip(
                            onClick = { onUpdateUnits("imperial") },
                            label = { Text("Imperial") },
                            selected = !userPrefs.isMetric()
                        )
                    }
                }
            }
            
            // Sensor Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Step Sensor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Use the device's step counter sensor when available for more accurate step counting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Step Sensor",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = userPrefs.useStepSensor,
                            onCheckedChange = onUpdateUseStepSensor
                        )
                    }
                }
            }
            
            // Maps Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Maps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enable Google Maps to view your walking paths. Requires internet connection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Maps",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = userPrefs.enableMaps,
                            onCheckedChange = onUpdateEnableMaps
                        )
                    }
                }
            }
            
            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "App Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "WalkTracker v1.0\n" +
                                "A simple GPS-based walking tracker\n" +
                                "Works offline, no data collection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
