package com.mansi.ecoscope_frontend.ui.screens



import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.mansi.ecoscope_frontend.FirebaseHelper
import com.mansi.ecoscope_frontend.R
import com.mansi.ecoscope_frontend.ui.components.ContactPicker
import com.mansi.ecoscope_frontend.ui.components.ContactManager
import com.mansi.ecoscope_frontend.utils.PrefsManager
import com.mansi.ecoscope_frontend.ui.viewmodel.AppViewModel
import kotlinx.coroutines.tasks.await
import kotlin.compareTo
import android.app.DatePickerDialog
import com.google.android.gms.maps.model.LatLng
import com.mansi.ecoscope_frontend.ui.components.MapPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.mansi.ecoscope_frontend.BuildConfig


@Composable
fun SOSEmergencyCard() {
    val context = LocalContext.current
    var showSOSDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        val contact = ContactPicker.extractContact(context, uri)
        contact?.let {
            Toast.makeText(context, "Picked: ${it.name} (${it.number})", Toast.LENGTH_SHORT).show()
            // You can add logic here to save or use the contact
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "SOS",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🚨 SOS Emergency",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap for emergency assistance",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fix: Use Row with equal weights and proper spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showSOSDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SEND SOS", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showContactDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CONTACTS", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { contactPickerLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22D3EE),
                            contentColor = Color.White
                        )
                    ) {
                        Text("PICK CONTACT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showSOSDialog) {
        SOSDialog(
            onDismiss = { showSOSDialog = false },
            onSendSOS = { message, alertType ->
                Toast.makeText(context, "SOS Alert Sent!", Toast.LENGTH_LONG).show()
                showSOSDialog = false
            }
        )
    }

    if (showContactDialog) {
        EmergencyContactsDialog(
            onDismiss = { showContactDialog = false }
        )
    }
}


@Composable
fun SOSDialog(
    onDismiss: () -> Unit,
    onSendSOS: (String, String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf("general") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🚨 Send SOS Alert", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Emergency Message (Optional):")
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Describe your emergency...") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Alert Type:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("general", "medical", "fire", "crime").forEach { type ->
                        FilterChip(
                            selected = alertType == type,
                            onClick = { alertType = type },
                            label = { Text(type.capitalize()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendSOS(message, alertType) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626)
                )
            ) {
                Text("SEND SOS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmergencyContactsDialog(onDismiss: () -> Unit) {
    var showAddContact by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("📞 Emergency Contacts", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                if (showAddContact) {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("No emergency contacts added yet.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddContact = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Emergency Contact")
                    }
                }
            }
        },
        confirmButton = {
            if (showAddContact) {
                Button(
                    onClick = {
                        // Add contact logic
                        showAddContact = false
                        newContactName = ""
                        newContactPhone = ""
                    }
                ) {
                    Text("Add")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (showAddContact) {
                TextButton(
                    onClick = {
                        showAddContact = false
                        newContactName = ""
                        newContactPhone = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoEyeDashboardScreen(
    navController: NavController? = null,

    onGlacialLakesClick: () -> Unit = {},
    onRoadNetworksClick: () -> Unit = {},
    onDrainageSystemsClick: () -> Unit = {},
    onAIAnalysisClick: () -> Unit = {},
    onSatelliteAnalysisClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel()


    var userFirstName by remember { mutableStateOf("User") }
    val savedContacts = remember { mutableStateListOf<ContactPicker.ContactData>() }
    var customName by remember { mutableStateOf("") }
    var customNumber by remember { mutableStateOf("") }
    var useSavedContact by remember { mutableStateOf(true) }
    val checkedLocationOnce = rememberSaveable { mutableStateOf(false) }
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        val contact = ContactPicker.extractContact(context, uri)
        contact?.let {
            val exists = savedContacts.any { c -> c.number == it.number }
            if (!exists && savedContacts.size < 1) {
                savedContacts.add(it)
                val formattedList = savedContacts.map { c -> "${c.name}::${c.number}" }
                PrefsManager.saveContacts(context, formattedList)
                FirebaseHelper.saveContact(it.number, it.name)
            } else {
                Toast.makeText(context, "Contact already added or max limit reached", Toast.LENGTH_SHORT).show()
            }
        }
    }
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

                item { WeatherMonitoringSection(viewModel) }

                item {
                    MonitoringModulesSection(
                        onGlacialLakesClick = onGlacialLakesClick,
                        onRoadNetworksClick = onRoadNetworksClick,
                        onDrainageSystemsClick = onDrainageSystemsClick,
                        onAIAnalysisClick = onAIAnalysisClick,
                        onSatelliteAnalysisClick = onSatelliteAnalysisClick
                    )
                }

                item {
                    SOSEmergencyCard()
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
                    MapAndDatePickerSection()
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
fun WeatherMonitoringSection(viewModel: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val weather = viewModel.weather
    val loading = viewModel.weatherLoading
    val error = viewModel.weatherError

    // TODO: Replace with user's real location if available
    val defaultLat = 28.7041
    val defaultLon = 77.1025
    val apiKey = BuildConfig.OPENWEATHERMAP_API_KEY

    LaunchedEffect(Unit) {
        if (weather == null && !loading) {
            viewModel.fetchWeather(defaultLat, defaultLon, apiKey)
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Cloud, contentDescription = "Weather", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Weather Monitoring", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        if (loading) {
            CircularProgressIndicator(color = Color(0xFF22D3EE), modifier = Modifier.size(32.dp))
        } else if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else if (weather != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherCard(Icons.Default.Thermostat, "Temperature", "${weather.main.temp}°C", Modifier.weight(1f))
                WeatherCard(Icons.Default.WbSunny, "Condition", weather.weather.firstOrNull()?.main ?: "-", Modifier.weight(1f))
                WeatherCard(Icons.Default.Water, "Humidity", "${weather.main.humidity}%", Modifier.weight(1f))
                WeatherCard(Icons.Default.Air, "Wind Speed", "${weather.wind.speed} km/h", Modifier.weight(1f))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherCard(Icons.Default.Thermostat, "Temperature", "-", Modifier.weight(1f))
                WeatherCard(Icons.Default.WbSunny, "Condition", "-", Modifier.weight(1f))
                WeatherCard(Icons.Default.Water, "Humidity", "-", Modifier.weight(1f))
                WeatherCard(Icons.Default.Air, "Wind Speed", "-", Modifier.weight(1f))
            }
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
    onDrainageSystemsClick: () -> Unit,
    onAIAnalysisClick: () -> Unit,
    onSatelliteAnalysisClick: () -> Unit
) {
    Column {
        Text("Monitoring Modules", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MonitoringModuleCard("Glacial Lakes", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFF0891B2), Color(0xFF06B6D4))), Icons.Default.Landscape, onGlacialLakesClick)
            MonitoringModuleCard("Road Networks", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B))), Icons.Default.Route, onRoadNetworksClick)
            MonitoringModuleCard("Drainage Systems", "AI-powered monitoring", Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))), Icons.Default.Water, onDrainageSystemsClick)
            MonitoringModuleCard("AI Analysis", "Environmental impact analysis", Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))), Icons.Default.Psychology, onAIAnalysisClick)
            MonitoringModuleCard("Satellite Analysis", "Historical comparison & predictions", Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444))), Icons.Default.Satellite, onSatelliteAnalysisClick)
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
fun MapAndDatePickerSection() {
    val context = LocalContext.current
    var lat by remember { mutableStateOf(28.7041) }
    var lon by remember { mutableStateOf(77.1025) }
    var showMapPicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Map & Date Picker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = lat.toString(),
                    onValueChange = { lat = it.toDoubleOrNull() ?: lat },
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
                    value = lon.toString(),
                    onValueChange = { lon = it.toDoubleOrNull() ?: lon },
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
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    modifier = Modifier.weight(1f).clickable { openDatePicker { startDate = it } },
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
                    value = endDate,
                    onValueChange = {},
                    label = { Text("End Date") },
                    readOnly = true,
                    modifier = Modifier.weight(1f).clickable { openDatePicker { endDate = it } },
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
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    // Example backend call (satellite/compare)
                    val client = OkHttpClient()
                    val formBody = FormBody.Builder()
                        .add("latitude", lat.toString())
                        .add("longitude", lon.toString())
                        .add("start_date", startDate)
                        .add("end_date", endDate)
                        .build()
                    val request = Request.Builder()
                        .url("http://10.0.2.2:8000/satellite/compare") // Change to your backend URL
                        .post(formBody)
                        .build()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = client.newCall(request).execute()
                            val body = response.body?.string() ?: "No response"
                            resultText = body
                        } catch (e: IOException) {
                            resultText = "Error: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
            ) {
                Text("Send to Backend for Analysis", color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (resultText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(resultText, color = Color.White, fontSize = 12.sp)
            }
        }
    }
    if (showMapPicker) {
        AlertDialog(
            onDismissRequest = { showMapPicker = false },
            confirmButton = {},
            dismissButton = {},
            text = {
                MapPicker(
                    initialPosition = LatLng(lat, lon),
                    onLocationPicked = {
                        lat = it.latitude
                        lon = it.longitude
                        showMapPicker = false
                    },
                    onCancel = { showMapPicker = false }
                )
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GeoEyeDashboardScreenPreview() {
    GeoEyeDashboardScreen()
}
@Preview(showBackground = true)
@Composable
fun SOSDialogPreview() {
    // This state is just for the preview to make the dialog visible
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        SOSDialog(
            onDismiss = { showDialog = false },
            onSendSOS = { message, alertType ->
                println("Preview SOS Sent: $message, Type: $alertType")
                showDialog = false
            }
        )
    }
}

@Preview(showBackground = true, name = "SOS Emergency Card Preview")
@Composable
fun SOSEmergencyCardPreview() {
    // You can wrap with a Theme if you have one, for consistent styling
    // YourAppTheme {
    Box(modifier = Modifier.padding(16.dp)) { // Add some padding around the card for better visualization
        SOSEmergencyCard()
    }
    // }
}

// Preview to specifically show the SOS Dialog when a button in SOSEmergencyCard is clicked
@Preview(showBackground = true, name = "SOS Card with Dialog Visible")
@Composable
fun SOSEmergencyCardWithDialogPreview() {
    var showSOSDialogFromCard by remember { mutableStateOf(false) }

    // This is a simplified version of SOSEmergencyCard for this specific preview
    // to control the dialog visibility directly.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showSOSDialogFromCard = true}) {
            Text("Show SOS Dialog (from preview)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        SOSEmergencyCard() // The actual card
    }


    if (showSOSDialogFromCard) {
        SOSDialog( // Using the dummy/actual SOSDialog
            onDismiss = { showSOSDialogFromCard = false },
            onSendSOS = { _, _ -> showSOSDialogFromCard = false }
        )
    }
}