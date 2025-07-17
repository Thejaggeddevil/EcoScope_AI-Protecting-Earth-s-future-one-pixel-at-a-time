package com.mansi.ecoscope_frontend.ui.screens


import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mansi.ecoscope_frontend.AIViewModel
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import com.mansi.ecoscope_frontend.ui.components.MapPicker
import com.google.android.gms.maps.model.LatLng
import com.mansi.ecoscope_frontend.SosViewModel
import com.mansi.ecoscope_frontend.SatelliteCompareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadNetworksAnalysisScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SatelliteCompareViewModel = viewModel()
    val sosViewModel: SosViewModel = viewModel()

    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var beforeDate by remember { mutableStateOf("") }
    var afterDate by remember { mutableStateOf("") }

    var showMapPicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun openDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10151C))
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Road Networks Monitoring",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00C896),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "Detect road changes, anomalies, and hazards in real time.",
            color = Color(0xFFB0BEC5),
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 24.dp, bottom = 18.dp)
        )
        // Data Source Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 0.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Data Source Selection", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DataRoadSourceCard(
                        icon = Icons.Default.Map,
                        title = "Live Map Integration",
                        description = "Pick a location on the map for analysis",
                        selected = true,
                        onClick = { showMapPicker = true }
                    )
                    DataRoadSourceCard(
                        icon = Icons.Default.CloudUpload,
                        title = "Manual Upload",
                        description = "Upload your own satellite images for analysis",
                        selected = false,
                        onClick = { Toast.makeText(context, "Manual upload not implemented", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        // Location and Date Pickers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("📍 Target Location (lat,lon)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = lon,
                        onValueChange = { lon = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    IconButton(onClick = { showMapPicker = true }) {
                        Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = beforeDate,
                        onValueChange = {},
                        label = { Text("Before Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { openDatePicker { beforeDate = it } },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = afterDate,
                        onValueChange = {},
                        label = { Text("After Date") },
                        readOnly = true,
                        modifier = Modifier.weight(1f).clickable { openDatePicker { afterDate = it } },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (lat.isNotBlank() && lon.isNotBlank() && beforeDate.isNotBlank() && afterDate.isNotBlank()) {
                            viewModel.analyze(lat.toDouble(), lon.toDouble(), beforeDate, afterDate)
                            Toast.makeText(context, "Analysis started...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Run Analysis", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // AI Analysis Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color(0xFF22D3EE), modifier = Modifier.size(32.dp))
                } else if (viewModel.error != null) {
                    Text("Error: ${viewModel.error}", color = Color.Red)
                } else {
                    val result = viewModel.result
                    if (result != null) {
                        Text("AI Analysis: ${result.ai_analysis ?: "-"}", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(model = result.before_image_url, contentDescription = "Before Image", modifier = Modifier.fillMaxWidth().height(120.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(model = result.after_image_url, contentDescription = "After Image", modifier = Modifier.fillMaxWidth().height(120.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(model = result.impact_image_url, contentDescription = "Impact Image", modifier = Modifier.fillMaxWidth().height(120.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Change Detection: ${result.change_detection ?: "-"}", color = Color(0xFFB0BEC5), fontSize = 15.sp, textAlign = TextAlign.Center)
                    } else {
                        Text("Ready for analysis - Connect your data source to begin", color = Color(0xFFB0BEC5), fontSize = 15.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // Emergency Response System
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1818))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Response System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Report road accidents or infrastructure emergencies immediately", color = Color(0xFFFFBABA), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { 
                        val contacts = com.mansi.ecoscope_frontend.utils.PrefsManager.getContacts(context)
                        sosViewModel.sendSOS(context, contacts)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SEND SOS ALERT", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Emergency response team will be notified immediately", color = Color(0xFFFFBABA), fontSize = 11.sp)
            }
        }
    }

    if (showMapPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMapPicker = false },
            confirmButton = {},
            dismissButton = {},
            text = {
                MapPicker(
                    initialPosition = LatLng(
                        lat.toDoubleOrNull() ?: 28.7041,
                        lon.toDoubleOrNull() ?: 77.1025
                    ),
                    onLocationPicked = {
                        lat = it.latitude.toString()
                        lon = it.longitude.toString()
                        showMapPicker = false
                    },
                    onCancel = { showMapPicker = false }
                )
            }
        )
    }
}

@Composable
fun DataRoadSourceCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Row {
        Card(
            modifier = Modifier
                .height(100.dp)
                .weight(1f)
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) Color(0xFF232B36) else Color(0xFF181F2A)
            ),
            elevation = CardDefaults.cardElevation(if (selected) 8.dp else 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF00C896),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
