package com.example.walktracker.ui.screens

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
import com.example.walktracker.util.Formatters

/**
 * Screen for calibrating step length by walking a known distance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    currentStepLength: Double,
    isMetric: Boolean,
    onNavigateBack: () -> Unit,
    onSaveStepLength: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var knownDistance by remember { mutableStateOf("100") }
    var stepCount by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var calculatedStepLength by remember { mutableStateOf(0.0) }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("Calibrate Step Length") },
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
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to Calibrate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "1. Find a straight path of known length\n" +
                                "2. Walk the distance at your normal pace\n" +
                                "3. Count your steps carefully\n" +
                                "4. Enter the distance and step count below",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Current step length display
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Step Length",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = Formatters.formatStepLength(currentStepLength, isMetric),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Input fields
            OutlinedTextField(
                value = knownDistance,
                onValueChange = { knownDistance = it },
                label = { 
                    Text("Known Distance (${if (isMetric) "meters" else "feet"})") 
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = stepCount,
                onValueChange = { stepCount = it },
                label = { Text("Steps Counted") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Calculate button
            Button(
                onClick = {
                    val distance = knownDistance.toDoubleOrNull()
                    val steps = stepCount.toIntOrNull()
                    
                    if (distance != null && steps != null && distance > 0 && steps > 0) {
                        // Convert distance to meters if using imperial
                        val distanceMeters = if (isMetric) {
                            distance
                        } else {
                            distance * 0.3048 // feet to meters
                        }
                        
                        calculatedStepLength = distanceMeters / steps
                        showResult = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = knownDistance.isNotBlank() && stepCount.isNotBlank()
            ) {
                Text("Calculate Step Length")
            }
            
            // Result display
            if (showResult && calculatedStepLength > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Calculated Step Length",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = Formatters.formatStepLength(calculatedStepLength, isMetric),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showResult = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    onSaveStepLength(calculatedStepLength)
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
            
            // Tips
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
                        text = "Tips for Accurate Calibration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Use a track, football field, or measured path\n" +
                                "• Walk at your normal, comfortable pace\n" +
                                "• Count every step, including partial steps\n" +
                                "• Repeat the measurement 2-3 times and average",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
