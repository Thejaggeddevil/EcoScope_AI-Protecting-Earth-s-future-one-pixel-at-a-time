package com.himanshu.ecoscope.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.himanshu.ecoscope.viewmodel.AuthViewModel
import com.himanshu.ecoscope.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoEyeSignupWithFirebaseScreen(
    onSignUpSuccess: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    // State values
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var preferredLanguage by remember { mutableStateOf("English") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val languages = listOf("English", "Hindi", "Spanish", "French", "German", "Chinese", "Japanese")
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Handle authentication result
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthResult.Success -> {
                errorMessage = ""
                onSignUpSuccess()
            }
            is AuthResult.Error -> {
                if (state.message != "User not signed in" && state.message != "User signed out") {
                    errorMessage = state.message
                }
            }
            AuthResult.Loading -> {
                errorMessage = ""
            }
        }
    }

    // ✅ Your full UI remains the same from here onward
    // just paste the remaining layout logic exactly as you had it
    // from Box(...) to GeoEyeSignupWithFirebaseScreenPreview()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A)
                    ),
                    center = androidx.compose.ui.geometry.Offset(0.5f, 0.3f),
                    radius = 1000f
                )
            )
    ) {
        // Space background effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x4D1E3A8A),
                            Color.Transparent,
                            Color(0x4D0F172A)
                        )
                    )
                )
        )

        // Stars effect overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A78C8FF),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(0.2f, 0.8f),
                        radius = 800f
                    )
                )
        )

        // Signup Card Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x66334155)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF10B981),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "GeoEye Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Join GeoEye Sentinel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text = "Create your account for satellite monitoring",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name Fields Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // First Name
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "First Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = firstName,
                                onValueChange = {
                                    firstName = it
                                    errorMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "John",
                                        color = Color(0xFF94A3B8)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF10B981),
                                    focusedContainerColor = Color(0x80475569),
                                    unfocusedContainerColor = Color(0x80475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !isLoading
                            )
                        }

                        // Last Name
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = lastName,
                                onValueChange = {
                                    lastName = it
                                    errorMessage = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "Doe",
                                        color = Color(0xFF94A3B8)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF10B981),
                                    focusedContainerColor = Color(0x80475569),
                                    unfocusedContainerColor = Color(0x80475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                enabled = !isLoading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Column {
                        Text(
                            text = "Email Address",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "john.doe@example.com",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF10B981),
                                focusedContainerColor = Color(0x80475569),
                                unfocusedContainerColor = Color(0x80475569)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Field
                    Column {
                        Text(
                            text = "Phone Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "+1 (555) 123-4567",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF10B981),
                                focusedContainerColor = Color(0x80475569),
                                unfocusedContainerColor = Color(0x80475569)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferred Language Dropdown
                    Column {
                        Text(
                            text = "Preferred Language",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = isLanguageDropdownExpanded,
                            onExpandedChange = {
                                if (!isLoading) {
                                    isLanguageDropdownExpanded = !isLanguageDropdownExpanded
                                }
                            }
                        ) {
                            OutlinedTextField(
                                value = preferredLanguage,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (isLanguageDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF94A3B8)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Language",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0x80475569),
                                    unfocusedContainerColor = Color(0x80475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isLoading
                            )

                            ExposedDropdownMenu(
                                expanded = isLanguageDropdownExpanded,
                                onDismissRequest = { isLanguageDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                languages.forEach { language ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = language,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            preferredLanguage = language
                                            isLanguageDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Column {
                        Text(
                            text = "Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "••••••••",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF10B981),
                                focusedContainerColor = Color(0x80475569),
                                unfocusedContainerColor = Color(0x80475569)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    Column {
                        Text(
                            text = "Confirm Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "••••••••",
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (password.isNotEmpty() && confirmPassword.isNotEmpty() && authViewModel.passwordsMatch(password, confirmPassword)) Color(0xFF10B981) else if (confirmPassword.isNotEmpty() && !authViewModel.passwordsMatch(password, confirmPassword)) Color(0xFFEF4444) else Color(0xFF475569),
                                unfocusedBorderColor = if (confirmPassword.isNotEmpty() && !authViewModel.passwordsMatch(password, confirmPassword)) Color(0xFFEF4444) else Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF10B981),
                                focusedContainerColor = Color(0x80475569),
                                unfocusedContainerColor = Color(0x80475569)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            enabled = !isLoading
                        )
                    }

                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign Up Button
                    val isFormValid = firstName.isNotEmpty() && lastName.isNotEmpty() &&
                            email.isNotEmpty() && phoneNumber.isNotEmpty() &&
                            password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                            authViewModel.passwordsMatch(password, confirmPassword)

                    Button(
                        onClick = {
                            when {
                                !authViewModel.isValidEmail(email) -> {
                                    errorMessage = "Please enter a valid email address"
                                }
                                !authViewModel.isValidPassword(password) -> {
                                    errorMessage = "Password must be at least 6 characters long"
                                }
                                !authViewModel.passwordsMatch(password, confirmPassword) -> {
                                    errorMessage = "Passwords do not match"
                                }
                                !authViewModel.isValidPhoneNumber(phoneNumber) -> {
                                    errorMessage = "Please enter a valid phone number"
                                }
                                else -> {
                                    authViewModel.signUp(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        phoneNumber = phoneNumber,
                                        preferredLanguage = preferredLanguage,
                                        password = password
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading && isFormValid
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Create Account",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Create Account",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Link
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        TextButton(
                            onClick = onSignInClick,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 14.sp,
                                color = Color(0xFF22D3EE),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer Text
                    Text(
                        text = "By creating an account, you agree to our Terms of Service and Privacy Policy",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
@Composable
fun Signnav(navHostController: NavHostController) {
    GeoEyeSignupWithFirebaseScreen(onSignUpSuccess = {
        navHostController.navigate("home")
    }, onSignInClick = {
        navHostController.navigate("login")
    })

}

@Preview(showBackground = true)
@Composable
fun GeoEyeSignupWithFirebaseScreenPreview() {
    GeoEyeSignupWithFirebaseScreen()
}
