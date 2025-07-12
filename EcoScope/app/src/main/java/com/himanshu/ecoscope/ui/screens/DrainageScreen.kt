package com.himanshu.ecoscope.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.himanshu.ecoscope.ui.viewmodel.DrainageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrainageSystemsMonitoringScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: DrainageViewModel = viewModel()

    var location by remember { mutableStateOf("") }
    var beforeDate by remember { mutableStateOf("") }
    var afterDate by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun openDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(dateFormat.format(date.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Drainage Systems Monitoring", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF22D3EE))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Select location and date range for analysis", fontSize = 14.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            item {
                Column {
                    Text("\uD83D\uDCCD Target Location", color = Color.White, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Enter latitude,longitude") },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("\uD83D\uDCC5 Before Date", color = Color.White)
                        OutlinedTextField(
                            value = beforeDate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.clickable { openDatePicker { beforeDate = it } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                unfocusedBorderColor = Color(0xFF22D3EE),
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF1E293B),
                                cursorColor = Color.White
                            ),
                            placeholder = { Text("yyyy-mm-dd", color = Color(0xFF94A3B8)) }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("\uD83D\uDCC5 After Date", color = Color.White)
                        OutlinedTextField(
                            value = afterDate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.clickable { openDatePicker { afterDate = it } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                unfocusedBorderColor = Color(0xFF22D3EE),
                                focusedBorderColor = Color(0xFF22D3EE),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedContainerColor = Color(0xFF1E293B),
                                cursorColor = Color.White
                            ),
                            placeholder = { Text("yyyy-mm-dd", color = Color(0xFF94A3B8)) }
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ElevatedButton(
                        onClick = {
                            val parts = location.split(",")
                            val lat = parts.getOrNull(0)?.toDoubleOrNull()
                            val lon = parts.getOrNull(1)?.toDoubleOrNull()
                            if (lat != null && lon != null && beforeDate.isNotBlank() && afterDate.isNotBlank()) {
                                viewModel.analyze(lat, lon, beforeDate, afterDate)
                            } else {
                                Toast.makeText(context, "Enter valid lat, lon and dates", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0CBBD9))
                    ) {
                        Text("Auto-Fetch & Analyze", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            location = ""
                            beforeDate = ""
                            afterDate = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Reset")
                    }
                }
            }

            item {
                viewModel.result?.let { res ->
                    Text("Area Type: ${res.area_type}", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = "http://192.168.31.201:5000${res.before_url}",
                        contentDescription = "Before Image",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = "http://192.168.31.201:5000${res.after_url}",
                        contentDescription = "After Image",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = "http://192.168.31.201:5000${res.change_map_url}",
                        contentDescription = "Change Map",
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                } ?: Text("\uD83D\uDE80 Analysis result will appear here", color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
fun Drainagenav(navhostController: NavController) {
    DrainageSystemsMonitoringScreen(onBackClick = {
        navhostController.navigate("home")
    })
}

@Preview(showBackground = true)
@Composable
fun DrainageSystemsMonitoringPreview() {
    DrainageSystemsMonitoringScreen()
}
