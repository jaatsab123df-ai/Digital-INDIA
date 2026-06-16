package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(viewModel: BharamputraViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var usePhoneFlow by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RiverDarkBackground)
    ) {
        // Decorative background water gradient waves
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RiverDarkSurfaceVariant,
                            RiverDarkBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Grand header logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RiverPrimary,
                                RiverSecondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bharamputra",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Stream. Share. Discover.",
                fontSize = 12.sp,
                color = RiverMutedGrey,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = RiverDarkSurface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main layout options (Email/Pass vs Phone OTP)
                    TabRow(
                        selectedTabIndex = if (usePhoneFlow) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = RiverPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = !usePhoneFlow,
                            onClick = { usePhoneFlow = false },
                            text = { Text("Email Sync", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = usePhoneFlow,
                            onClick = { usePhoneFlow = true },
                            text = { Text("Phone OTP", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!usePhoneFlow) {
                        EmailPasswordForm(
                            isSignUp = isSignUp,
                            isLoading = isAuthLoading,
                            errorMsg = authError,
                            onToggleSignUp = { isSignUp = !isSignUp },
                            onSubmit = { email, name, phone ->
                                if (isSignUp) {
                                    viewModel.registerWithEmail(email, name, phone)
                                } else {
                                    viewModel.loginWithEmail(email)
                                }
                            }
                        )
                    } else {
                        PhoneOtpForm(
                            isLoading = isAuthLoading,
                            onVerified = { phone ->
                                viewModel.registerWithEmail("$phone@bharamputra.com", "Phone Creator", phone)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmailPasswordForm(
    isSignUp: Boolean,
    isLoading: Boolean,
    errorMsg: String?,
    onToggleSignUp: () -> Unit,
    onSubmit: (email: String, name: String, phone: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Channel/Creator Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("creator_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RiverPrimary,
                    unfocusedBorderColor = RiverDarkSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RiverPrimary,
                unfocusedBorderColor = RiverDarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RiverPrimary,
                unfocusedBorderColor = RiverDarkSurfaceVariant
            )
        )

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSubmit(email, name.ifEmpty { "Guest Explorer" }, phone) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isSignUp) "Register System Account" else "Explore Bharamputra Stream",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Simulation
        OutlinedButton(
            onClick = { onSubmit("google_creator@gmail.com", "Google Streamer", "") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("google_signin_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(RiverPrimary, RiverSecondary))
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = RiverSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google Play ID", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onToggleSignUp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (isSignUp) "Already have a stream account? Sign In" else "Create a Bharamputra Creator Channel",
                color = RiverSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PhoneOtpForm(
    isLoading: Boolean,
    onVerified: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!codeSent) {
            Text(
                text = "Secure Mobile Entry",
                fontSize = 14.sp,
                color = RiverSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter phone number. We will route a secure 6-digit mock OTP simulation instantly.",
                fontSize = 12.sp,
                color = RiverMutedGrey
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Mobile Number (e.g. +91 9876543210)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RiverPrimary,
                    unfocusedBorderColor = RiverDarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (phoneNumber.length >= 10) {
                        scope.launch {
                            message = "Sending secure handshake request..."
                            delay(1000)
                            codeSent = true
                            message = "OTP Sent! Mock code is [420108]"
                        }
                    } else {
                        message = "Invalid phone format"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Transmit SMS OTP", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "Verify Handshake",
                fontSize = 14.sp,
                color = RiverSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SMS transmitted successfully to $phoneNumber. Use simulated verification code: 420108",
                fontSize = 12.sp,
                color = RiverMutedGrey
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Enter 6-Digit OTP") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("otp_code_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RiverPrimary,
                    unfocusedBorderColor = RiverDarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (otpCode == "420108") {
                        onVerified(phoneNumber)
                    } else {
                        message = "Invalid verification code"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RiverSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm & Establish Session", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { codeSent = false; otpCode = "" },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Change Mobile Number", color = RiverPrimary, fontSize = 13.sp)
            }
        }

        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = RiverDarkSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = RiverSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
