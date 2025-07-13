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
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.himanshu.ecoscope.network.AnalyzeResponse
import com.himanshu.ecoscope.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlacialLakesMonitoringScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel()
    //val response by viewModel.result.collectAsState()
    var response by remember { mutableStateOf<AnalyzeResponse?>(null) }


    var location by remember { mutableStateOf("") }
    var beforeDate by remember { mutableStateOf("") }
    var afterDate by remember { mutableStateOf("") }

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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0F172A))) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Glacial Lakes Monitoring",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF22D3EE)
                        )
                        Text(
                            text = "Analyze changes in glacial lakes using satellite data",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            item {
                Column {
                    Text("📍 Target Location (lat,lon)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("28.7041,77.1025") },
                        trailingIcon = {
                            IconButton(onClick = {
                                Toast.makeText(context, "Map picker not implemented", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color(0xFF22D3EE),
                            focusedBorderColor = Color(0xFF22D3EE),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📅 Before Date", color = Color.White, fontSize = 14.sp)
                        OutlinedTextField(
                            value = beforeDate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    openDatePicker { beforeDate = it }
                                },
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
                        Text("📅 After Date", color = Color.White, fontSize = 14.sp)
                        OutlinedTextField(
                            value = afterDate,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    openDatePicker { afterDate = it }
                                },
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
                            if (parts.size == 2) {
                                val lat = parts[0].toDoubleOrNull()
                                val lon = parts[1].toDoubleOrNull()
                                if (lat != null && lon != null && beforeDate.isNotEmpty() && afterDate.isNotEmpty()) {
                                    viewModel.analyze(
                                        lat = lat,
                                        lon = lon,
                                        before = beforeDate,
                                        after = afterDate
                                    )
                                } else {
                                    Toast.makeText(context, "Invalid coordinates or dates", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Enter lat,lon correctly", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("Reset")
                    }
                }
            }

            item {
                if (response != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("📷 Before Image", color = Color.White, fontSize = 14.sp)
                        AsyncImage(model = response!!.before_url, contentDescription = "Before")

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("📷 After Image", color = Color.White, fontSize = 14.sp)
                        AsyncImage(model = response!!.after_url, contentDescription = "After")

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🌐 Change Map", color = Color.White, fontSize = 14.sp)
                        AsyncImage(model = response!!.change_map_url, contentDescription = "Change Map")

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🗺 Area Type: ${response!!.area_type}", color = Color(0xFF94A3B8))
                    }
                } else {
                    Text(
                        text = "🛰️ Analysis Result will appear here (before/after images & impact)",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
fun Glacialnav(navHostController: NavHostController){
    GlacialLakesMonitoringScreen(onBackClick = { navHostController.navigate("home") })

}