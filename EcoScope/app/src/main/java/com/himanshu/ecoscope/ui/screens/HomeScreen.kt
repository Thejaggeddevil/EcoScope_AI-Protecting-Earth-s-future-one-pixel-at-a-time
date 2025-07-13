package com.himanshu.ecoscope.ui.screens

import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.himanshu.ecoscope.R
import com.himanshu.ecoscope.ui.components.ContactPicker
import com.himanshu.ecoscope.ui.components.ContactManager
import com.himanshu.ecoscope.utils.PrefsManager
import com.himanshu.ecoscope.ui.viewmodel.AppViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun ClickableWarning(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.sos), // make sure sos.png is in res/drawable
        contentDescription = "Warning Icon",
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoEyeDashboardScreen(
    navController: NavController? = null,
    onLogout: () -> Unit = {},
    onGlacialLakesClick: () -> Unit = {},
    onRoadNetworksClick: () -> Unit = {},
    onDrainageSystemsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel()
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        // TODO: Implement contact parsing logic
    }

    var userFirstName by remember { mutableStateOf("User") }
    val savedContacts = remember { mutableStateListOf<ContactPicker.ContactData>() }
    var customName by remember { mutableStateOf("") }
    var customNumber by remember { mutableStateOf("") }
    var useSavedContact by remember { mutableStateOf(true) }
    val checkedLocationOnce = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .get()
                .await()
            userFirstName = doc.getString("firstName") ?: "User"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("EcoScope AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigate("settings") }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFF0F172A),
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Welcome, $userFirstName 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF22D3EE)
                    )
                }

                item { LocationServicesCard() }

                item { WeatherMonitoringSection() }

                item {
                    MonitoringModulesSection(
                        onGlacialLakesClick = onGlacialLakesClick,
                        onRoadNetworksClick = onRoadNetworksClick,
                        onDrainageSystemsClick = onDrainageSystemsClick
                    )
                }
item {
    if (useSavedContact) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            savedContacts.forEach { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp)
                        .border(1.dp, Color(0xFF1A1B41), RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📱 ${contact.name}", color = Color(0xFF1A1B41))
                    IconButton(onClick = {
                        savedContacts.remove(contact)
                        val updatedList = savedContacts.map { "${it.name}::${it.number}" }
                        PrefsManager.saveContacts(context, updatedList)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.dlt_icon), // put dlt_icon.png in res/drawable
                            contentDescription = "Delete",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            if (savedContacts.size < 1) {
                ElevatedButton(
                    onClick = { contactPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth(0.5f),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color(0xFFEF5350),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Contact")
                }
            }
        }
    }
}

                item {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ClickableWarning {
                            val selected = if (useSavedContact && savedContacts.isNotEmpty()) {
                                savedContacts.first()
                            } else if (!useSavedContact && customNumber.isNotBlank()) {
                                ContactPicker.ContactData(
                                    customName.ifBlank { "Unknown" },
                                    customNumber
                                )
                            } else null

                            if (selected == null) {
                                Toast.makeText(
                                    context,
                                    "⚠️ No valid contact selected",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "🚨 SOS Triggered", Toast.LENGTH_SHORT).show()
                                viewModel.sendSOS(
                                    context = context,
                                    contacts = listOf(selected.number)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(9.dp))
                    Text("Send SOS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1B41))
                    Text("Press button for help", color = Color(0xFF1A1B41), fontSize = 14.sp)
                }
            }
        }
    )
}

@Composable
fun LocationServicesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFF22D3EE),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Location Services", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text("Enable for precise satellite positioning", fontSize = 14.sp, color = Color(0xFF94A3B8))
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WeatherMonitoringSection() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Cloud, contentDescription = "Weather", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Weather Monitoring", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherCard(Icons.Default.Thermostat, "Temperature", "15°C", Modifier.weight(1f))
            WeatherCard(Icons.Default.WbSunny, "Condition", "Clear Sky", Modifier.weight(1f))
            WeatherCard(Icons.Default.Water, "Humidity", "45%", Modifier.weight(1f))
            WeatherCard(Icons.Default.Air, "Wind Speed", "12 km/h", Modifier.weight(1f))
        }
    }
}

@Composable
fun WeatherCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF22D3EE), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun MonitoringModulesSection(
    onGlacialLakesClick: () -> Unit,
    onRoadNetworksClick: () -> Unit,
    onDrainageSystemsClick: () -> Unit
) {
    Column {
        Text("Monitoring Modules", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MonitoringModuleCard("Glacial Lakes", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFF0891B2), Color(0xFF06B6D4))), Icons.Default.Landscape, onGlacialLakesClick)
            MonitoringModuleCard("Road Networks", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))), Icons.Default.Route, onRoadNetworksClick)
            MonitoringModuleCard("Drainage Systems", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))), Icons.Default.Water, onDrainageSystemsClick)
        }
    }
}

@Composable
fun MonitoringModuleCard(
    title: String,
    subtitle: String,
    backgroundColor: Brush,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Arrow", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun homenav(navhostController: NavController) {
    GeoEyeDashboardScreen(
        navController = navhostController,
        onLogout = { navhostController.navigate("login") },
        onGlacialLakesClick = { navhostController.navigate("glacial") },
        onRoadNetworksClick = { navhostController.navigate("road") },
        onDrainageSystemsClick = { navhostController.navigate("drainage") }
    )
}

@Preview(showBackground = true)
@Composable
fun GeoEyeDashboardScreenPreview() {
    GeoEyeDashboardScreen()
}
