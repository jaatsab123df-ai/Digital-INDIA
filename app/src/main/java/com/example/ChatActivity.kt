package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.data.auth.BharamputraAuth
import com.example.data.auth.UserAccount
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.random.Random

// --- Theme Colors for Barak Chat (Teal / Forest Palette) ---
val ChatDarkBg = Color(0xFF0B141A)       // WhatsApp Forest Midnight
val ChatCardDarkBg = Color(0xFF111B21)   // Dark Card Background
val ChatTealAccent = Color(0xFF00A884)   // WhatsApp Emerald Teal
val ChatTealLight = Color(0xFF005C4B)    // Dark Send Bubble
val ChatIncomingDark = Color(0xFF202C33) // Dark Receive Bubble

val ChatLightBg = Color(0xFFF0F2F5)      // Classic Light Grey Cream
val ChatCardLightBg = Color(0xFFFFFFFF)  // White Card
val ChatTealPrimary = Color(0xFF008069)  // Rich Accent Teal Light
val ChatSendBubbleLight = Color(0xFFE1F3D4) // Light Mode Send Bubble (Green Tint)
val ChatReceiveBubbleLight = Color(0xFFFFFFFF) // Light Mode Receive Bubble

// --- Chat Models ---
data class ChatContact(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val isSystemUser: Boolean = false,
    val isBlocked: Boolean = false
) : Serializable

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val status: String, // SENT (single grey tick), DELIVERED (double grey tick), READ (double blue tick)
    val attachmentType: String? = null, // "IMAGE", "VIDEO", "AUDIO", "DOCUMENT"
    val attachmentName: String? = null,
    val attachmentUrl: String? = null
) : Serializable

data class ChatGroup(
    val id: String,
    val name: String,
    val description: String,
    val avatarUrl: String,
    val members: List<String>, // User IDs
    val adminId: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class ChatStatus(
    val id: String,
    val userId: String,
    val userName: String,
    val avatarUrl: String,
    val contentText: String,
    val imageUrl: String? = null,
    val timestamp: Long,
    val sharedToShorts: Boolean = false,
    val sharedToStories: Boolean = false
) : Serializable

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this).get(BharamputraViewModel::class.java)

        setContent {
            var isDarkMode by remember { mutableStateOf(true) }

            MaterialTheme(
                colorScheme = if (isDarkMode) {
                    darkColorScheme(
                        primary = ChatTealAccent,
                        background = ChatDarkBg,
                        surface = ChatCardDarkBg
                    )
                } else {
                    lightColorScheme(
                        primary = ChatTealPrimary,
                        background = ChatLightBg,
                        surface = ChatCardLightBg
                    )
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkMode) ChatDarkBg else ChatLightBg
                ) {
                    BarakChatNavigationContainer(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}

@Composable
fun BarakChatNavigationContainer(
    viewModel: BharamputraViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var hasVerifiedByPhone by remember { mutableStateOf(false) }

    // If an active session is already present in Bharamputra ecosystem, auto-link it!
    LaunchedEffect(currentUser) {
        if (currentUser != null && !hasVerifiedByPhone) {
            hasVerifiedByPhone = true
            Toast.makeText(context, "Welcome back, ${currentUser?.name}! Workspace profile synchronized.", Toast.LENGTH_SHORT).show()
        }
    }

    if (!hasVerifiedByPhone && currentUser == null) {
        BarakChatAuthGateway(
            viewModel = viewModel,
            onVerified = { phoneNum, existingUser ->
                hasVerifiedByPhone = true
                if (existingUser != null) {
                    // Logged in automatically since match found
                    Toast.makeText(context, "SmsRetriever: Authenticated as ${existingUser.name}!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    } else {
        // Authenticated Session View
        BarakChatDashboard(
            viewModel = viewModel,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onLogout = {
                hasVerifiedByPhone = false
                viewModel.navigateTo("splash") // reset shared state if applicable
            }
        )
    }
}

// --- PHONE OTP / MAIN AUTH GATEWAY ---
@Composable
fun BarakChatAuthGateway(
    viewModel: BharamputraViewModel,
    onVerified: (String, UserAccount?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()

    var loginMode by remember { mutableStateOf("phone") } // "phone", "email"

    // --- Phone Form Elements ---
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(0) }
    var smsListening by remember { mutableStateOf(false) }
    var smsAutocompleting by remember { mutableStateOf(false) }

    // --- Registration form for new device/number ---
    var isNewProfileNeeded by remember { mutableStateOf(false) }
    var signupPhone by remember { mutableStateOf("") }
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }

    // --- Email Flow State ---
    var hasEmailAccount by remember { mutableStateOf(true) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // Demo Pre-linked Accounts Database (Bharamputra Ecosystem Sync)
    val prelinkedDatabase = remember {
        mapOf(
            "+919876543210" to Triple("Bharamputra Explorer", "explorer@bharamputra.com", "@explorer_barak"),
            "+911234567890" to Triple("Assam Media Hub", "assam_media@bharamputra.com", "@assam_media"),
            "+919999999999" to Triple("Vedic Tech Hub", "vedic_coder@bharamputra.com", "@vedic_coder")
        )
    }

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        }
    }

    // SmsRetriever listening behavior
    LaunchedEffect(isCodeSent) {
        if (isCodeSent) {
            smsListening = true
            smsAutocompleting = false
            delay(2500)
            if (smsListening && verificationCode.isEmpty()) {
                smsAutocompleting = true
                Toast.makeText(context, "SmsRetriever: OTP Message intercepted!", Toast.LENGTH_SHORT).show()
                delay(1000)
                verificationCode = "420108"
                smsAutocompleting = false
                Toast.makeText(context, "OTP autocompleted successfully.", Toast.LENGTH_SHORT).show()
                
                // Triggers validation
                validateVerificationCode(
                    phone = phoneNumber,
                    code = "420108",
                    prelinkedDatabase = prelinkedDatabase,
                    viewModel = viewModel,
                    context = context,
                    onNewUser = {
                        signupPhone = phoneNumber
                        isNewProfileNeeded = true
                    },
                    onSuccess = { uAcct ->
                        onVerified(phoneNumber, uAcct)
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatDarkBg)
    ) {
        // Aesthetic Top Wave Background
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val brush = Brush.verticalGradient(
                colors = listOf(ChatTealAccent.copy(alpha = 0.3f), Color.Transparent)
            )
            drawRect(brush = brush)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Brand Logo Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(ChatTealAccent, Color(0xFF10B981))))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(ChatDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Barak Chat Secure Gateway",
                        tint = ChatTealAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BARAK CHAT",
                color = ChatTealAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Text(
                text = "Secure Communication",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Bharamputra Synchronized Messaging Ecosystem",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tab switch between Phone and Email Method
            if (!isNewProfileNeeded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChatCardDarkBg)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { loginMode = "phone" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (loginMode == "phone") ChatTealAccent else Color.Transparent,
                            contentColor = if (loginMode == "phone") Color.Black else Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Phone OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { loginMode = "email" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (loginMode == "email") ChatTealAccent else Color.Transparent,
                            contentColor = if (loginMode == "email") Color.Black else Color.Gray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Body Flow Components
            if (isNewProfileNeeded) {
                // New Account Creation
                Card(
                    colors = CardDefaults.cardColors(containerColor = ChatCardDarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ChatTealAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Barak Profile", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your phone $signupPhone is verified, but has no pre-associated creator channel. Establish a new profile below:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = signupName,
                            onValueChange = { signupName = it },
                            label = { Text("Display Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ChatTealAccent,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = ChatTealAccent,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = { signupEmail = it },
                            label = { Text("Email Address (Optional)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ChatTealAccent,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = ChatTealAccent,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (signupName.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter your Display Name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val finalEmail = signupEmail.ifEmpty { "${signupName.lowercase().replace(" ", "")}@bharamputra.com" }
                                viewModel.registerWithEmail(finalEmail, signupName, signupPhone)
                                onVerified(signupPhone, viewModel.currentUser.value)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CREATE PLATFORM CHANNEL", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                isNewProfileNeeded = false
                                isCodeSent = false
                                verificationCode = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
            } else if (loginMode == "phone") {
                // Phone Flow Layouts
                if (!isCodeSent) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ChatCardDarkBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "ENTER PHONE NUMBER",
                                fontSize = 11.sp,
                                color = ChatTealAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Provide your mobile number. Automatic Firebase alignment will look up your matching profile across Bharamputra.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ChatDarkBg)
                                        .border(1.dp, Color.DarkGray, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 15.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🇮🇳", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+91", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    placeholder = { Text("98765 43210") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ChatTealAccent,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedPlaceholderColor = Color.DarkGray,
                                        unfocusedPlaceholderColor = Color.DarkGray
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("barak_phone_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sync numbers: +919876543210 (Explorer), +919999999999 (Vedic Coder)",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val cleanNum = phoneNumber.filter { it.isDigit() }
                                    if (cleanNum.length < 10) {
                                        Toast.makeText(context, "Invalid Phone. Provide at least 10 digits.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isCodeSent = true
                                    countdownSeconds = 30
                                    Toast.makeText(context, "Firebase OTP dispatching to +91 $cleanNum...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("barak_send_otp_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("REQUEST VERIFICATION SMS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // Sent code states
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ChatCardDarkBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VERIFY SMS CODE",
                                    fontSize = 11.sp,
                                    color = ChatTealAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+91 $phoneNumber",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Please enter the 6-digit firebase code. Safe fallback demo code is 420108.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                label = { Text("Firebase SMS Code") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ChatTealAccent,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedLabelColor = ChatTealAccent,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("barak_otp_code_field"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color.Gray) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom SMS Retriever active banner
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ChatDarkBg),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (smsListening) ChatTealAccent.copy(alpha = 0.4f) else Color.DarkGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Hearing,
                                                contentDescription = null,
                                                tint = if (smsListening) ChatTealAccent else Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (smsAutocompleting) "Retrieving Secure SMS..." else "SmsRetriever API Listening...",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (smsListening) {
                                            CircularProgressIndicator(
                                                color = ChatTealAccent,
                                                strokeWidth = 1.5.dp,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    if (smsAutocompleting) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            color = ChatTealAccent,
                                            trackColor = Color.DarkGray,
                                            modifier = Modifier.fillMaxWidth().height(2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    smsListening = false // stop retriever
                                    validateVerificationCode(
                                        phone = phoneNumber,
                                        code = verificationCode,
                                        prelinkedDatabase = prelinkedDatabase,
                                        viewModel = viewModel,
                                        context = context,
                                        onNewUser = {
                                            signupPhone = phoneNumber
                                            isNewProfileNeeded = true
                                        },
                                        onSuccess = { uAcct ->
                                            onVerified(phoneNumber, uAcct)
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("barak_phone_verify_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent),
                                shape = RoundedCornerShape(10.dp),
                                enabled = verificationCode.length >= 4
                            ) {
                                Text("CONFIRM OTP HANDSHAKE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        isCodeSent = false
                                        verificationCode = ""
                                        smsListening = false
                                    }
                                ) {
                                    Text("Change Number", color = Color.Gray, fontSize = 11.sp)
                                }

                                if (countdownSeconds > 0) {
                                    Text("Resend in ${countdownSeconds}s", color = Color.Gray, fontSize = 11.sp)
                                } else {
                                    TextButton(
                                        onClick = {
                                            countdownSeconds = 30
                                            verificationCode = ""
                                            smsListening = true
                                            Toast.makeText(context, "Resending OTP verification via Firebase SMS...", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Resend OTP", color = ChatTealAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Alternative Email Flow
                Card(
                    colors = CardDefaults.cardColors(containerColor = ChatCardDarkBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (hasEmailAccount) "EMAIL SIGN IN" else "SYNC NEW ACCOUNT",
                            fontSize = 11.sp,
                            color = ChatTealAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!hasEmailAccount) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Display Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ChatTealAccent,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedLabelColor = ChatTealAccent,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ChatTealAccent,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = ChatTealAccent,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ChatTealAccent,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedLabelColor = ChatTealAccent,
                                    unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (emailInput.isEmpty()) {
                                    Toast.makeText(context, "Please enter email key.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (hasEmailAccount) {
                                    viewModel.loginWithEmail(emailInput)
                                } else {
                                    if (nameInput.isEmpty()) {
                                        Toast.makeText(context, "Please write display name.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.registerWithEmail(emailInput, nameInput)
                                }
                                onVerified("Email Flow", viewModel.currentUser.value)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent)
                        ) {
                            Text(
                                text = if (hasEmailAccount) "VERIFY & LOG IN" else "CREATE EMAIL CHANNEL",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { hasEmailAccount = !hasEmailAccount },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (hasEmailAccount) "Setup alternative stream email? Register" else "Sign with existing channel",
                                color = ChatTealAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google sign-in secondary optional indicator
            OutlinedButton(
                onClick = {
                    viewModel.registerWithEmail("google_chat_sync@gmail.com", "Google Chat-Link", "+919922992299")
                    Toast.makeText(context, "Google Play Identity Connected & Synced!", Toast.LENGTH_SHORT).show()
                    onVerified("+919922992299", viewModel.currentUser.value)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("gmail_quick_login"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ChatTealAccent)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Align credentials via Google Play ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun validateVerificationCode(
    phone: String,
    code: String,
    prelinkedDatabase: Map<String, Triple<String, String, String>>,
    viewModel: BharamputraViewModel,
    context: Context,
    onNewUser: () -> Unit,
    onSuccess: (UserAccount?) -> Unit
) {
    if (code != "420108") {
        Toast.makeText(context, "Invalid Verification Code. Authentication failed.", Toast.LENGTH_SHORT).show()
        return
    }

    val finalPhone = if (phone.startsWith("+91")) phone else "+91$phone"
    val match = prelinkedDatabase[finalPhone]

    if (match != null) {
        val (name, email, handle) = match
        Toast.makeText(context, "Existing Bharamputra profile found! Setting up Barak Session...", Toast.LENGTH_LONG).show()
        viewModel.registerWithEmail(email, name, finalPhone)
        onSuccess(viewModel.currentUser.value)
    } else {
        // Verification was successful, but this phone has no active model account. Redirection for input!
        onNewUser()
    }
}

// --- SECURE CHAT CLIENT DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarakChatDashboard(
    viewModel: BharamputraViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf("chats") } // "chats", "social", "status", "groups"

    // Mock Database States (Initialized inside composed state to allow additions/modifications)
    val contacts = remember {
        mutableStateListOf(
            ChatContact("user_1", "Bharamputra Explorer", "@explorer_barak", "river_shield", "Exploring the Brahmaputra network.", true, "Online"),
            ChatContact("user_2", "Vedic Tech Hub", "@vedic_coder", "river_shield", "Writing secure kotlin components.", true, "Online"),
            ChatContact("user_3", "Assam Tourism Alliance", "@assam_tourism", "river_shield", "Official state marketing. Follow!", false, "Last seen 20 min ago"),
            ChatContact("user_4", "Guwahati Dev Circle", "@guwahati_devs", "river_shield", "Indie devs uniting.", false, "Last seen Yesterday"),
            ChatContact("user_5", "Northeastern Beats", "@ne_beats", "river_shield", "Local music and sounds.", true, "Online")
        )
    }

    val groups = remember {
        mutableStateListOf(
            ChatGroup("grp_1", "Assam Music Producers", "Sharing beats and visual shorts from our local streams.", "river_shield", listOf("user_1", "user_2", "user_5"), "user_2"),
            ChatGroup("grp_2", "Tech Innovation India", "Discussing database schemas and cloud synchronizations.", "river_shield", listOf("user_1", "user_2", "user_3", "user_4"), "user_1")
        )
    }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("m1", "user_2", "Vedic Tech Hub", "Hello explorer! Welcome to the premium Barak Chat client.", System.currentTimeMillis() - 3600000, "READ"),
            ChatMessage("m2", "user_2", "Vedic Tech Hub", "Our phone encryption is aligned with Bharamputra databases.", System.currentTimeMillis() - 1200000, "READ"),
            ChatMessage("m3", "system", "You", "Thanks! Glad to have single sync capability.", System.currentTimeMillis() - 600000, "READ"),
            ChatMessage("m4", "user_2", "Vedic Tech Hub", "Are you interested in collaborating on Vedic Code shorts?", System.currentTimeMillis() - 30000, "READ")
        )
    }

    val groupMessages = remember {
        mutableStateMapOf<String, MutableList<ChatMessage>>().apply {
            put("grp_1", mutableStateListOf(
                ChatMessage("gm1", "user_5", "Northeastern Beats", "Sent a new draft beat. Please review!", System.currentTimeMillis() - 800000, "DELIVERED"),
                ChatMessage("gm2", "user_2", "Vedic Tech Hub", "Sounds pristine. Let's make an visual animation for it.", System.currentTimeMillis() - 400000, "DELIVERED")
            ))
            put("grp_2", mutableStateListOf(
                ChatMessage("gm3", "user_1", "Bharamputra Explorer", "We need a secure Room database and clean migration path.", System.currentTimeMillis() - 900000, "DELIVERED"),
                ChatMessage("gm4", "user_4", "Guwahati Dev Circle", "Agreed, Repository Pattern is mandatory.", System.currentTimeMillis() - 100000, "DELIVERED")
            ))
        }
    }

    val statuses = remember {
        mutableStateListOf(
            ChatStatus("st_1", "user_2", "Vedic Tech Hub", "river_shield", "Hacking late-night Kotlin Compose widgets!", null, System.currentTimeMillis() - 18000000, false, false),
            ChatStatus("st_2", "user_5", "Northeastern Beats", "river_shield", "Check out my new folk audio status!", null, System.currentTimeMillis() - 32000000, false, false),
            ChatStatus("st_3", "user_3", "Assam Tourism Alliance", "river_shield", "Golden autumn river views.", "river_shield", System.currentTimeMillis() - 50000000, false, false)
        )
    }

    // Interactive Client Settings / Toggles
    var isOfflineMode by remember { mutableStateOf(false) }
    var offlineQueue = remember { mutableStateListOf<ChatMessage>() }
    var showSecurityOptions by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }

    // Navigation Screens
    var activeChatRoomContact by remember { mutableStateOf<ChatContact?>(null) }
    var activeChatRoomGroup by remember { mutableStateOf<ChatGroup?>(null) }

    // Media compression states
    var imageCompressing by remember { mutableStateOf(false) }

    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Active screen switcher (Inside chat or general dashboard)
    if (activeChatRoomContact != null) {
        // Individual private chat conversation
        val blockState = remember(activeChatRoomContact) { contacts.firstOrNull { it.id == activeChatRoomContact?.id }?.isBlocked ?: false }
        
        ChatRoomScreen(
            contact = activeChatRoomContact!!,
            currentUser = currentUser,
            messagesList = messages.filter { (it.senderId == activeChatRoomContact?.id && it.senderName != "You") || (it.senderId == "system" && it.senderName == "You") },
            isBlocked = blockState,
            isOffline = isOfflineMode,
            onSendMessage = { text, attachmentType, attName ->
                val newMsg = ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    senderId = "system",
                    senderName = "You",
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    status = if (isOfflineMode) "SENT" else "DELIVERED",
                    attachmentType = attachmentType,
                    attachmentName = attName
                )
                if (isOfflineMode) {
                    offlineQueue.add(newMsg)
                    Toast.makeText(context, "Offline: Message stored in secure local queue.", Toast.LENGTH_SHORT).show()
                } else {
                    messages.add(newMsg)
                }
                
                // Simulate real response with typing indicators
                if (!isOfflineMode && !blockState) {
                    scope.launch {
                        delay(1200)
                        // Trigger typing behavior inside message stream if possible
                        delay(1000)
                        val reply = ChatMessage(
                            id = "reply_${System.currentTimeMillis()}",
                            senderId = activeChatRoomContact!!.id,
                            senderName = activeChatRoomContact!!.name,
                            text = "ACK: Received successfully via Barak secure network.",
                            timestamp = System.currentTimeMillis(),
                            status = "SENT"
                        )
                        messages.add(reply)
                        
                        // Set previous to read
                        val updated = messages.map {
                            if (it.senderId == "system") it.copy(status = "READ") else it
                        }
                        messages.clear()
                        messages.addAll(updated)
                    }
                }
            },
            onBack = { activeChatRoomContact = null },
            onBlockToggle = {
                val index = contacts.indexOfFirst { it.id == activeChatRoomContact?.id }
                if (index != -1) {
                    val updatedContact = contacts[index].copy(isBlocked = !contacts[index].isBlocked)
                    contacts[index] = updatedContact
                    Toast.makeText(context, if (updatedContact.isBlocked) "User blocked successfully." else "User unblocked.", Toast.LENGTH_SHORT).show()
                }
            },
            onReportSpam = { reason ->
                Toast.makeText(context, "Spam registered! Security team notified. Content ID: ${activeChatRoomContact?.username}", Toast.LENGTH_LONG).show()
            },
            onAttachmentCompression = { active ->
                imageCompressing = active
            },
            imageCompressing = imageCompressing,
            isDarkMode = isDarkMode
        )
    } else if (activeChatRoomGroup != null) {
        // Group Conversation Room
        val currentGroup = activeChatRoomGroup!!
        val grpMessages = groupMessages[currentGroup.id] ?: remember { mutableStateListOf() }

        GroupRoomScreen(
            group = currentGroup,
            currentUser = currentUser,
            messagesList = grpMessages,
            contactsList = contacts,
            isOffline = isOfflineMode,
            onSendMessage = { text, attType, attName ->
                val newMsg = ChatMessage(
                    id = "gmsg_${System.currentTimeMillis()}",
                    senderId = currentUser?.id ?: "you",
                    senderName = currentUser?.name ?: "You",
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    status = "DELIVERED",
                    attachmentType = attType,
                    attachmentName = attName
                )
                if (isOfflineMode) {
                    offlineQueue.add(newMsg)
                    Toast.makeText(context, "Offline queue active. Message cached.", Toast.LENGTH_SHORT).show()
                } else {
                    grpMessages.add(newMsg)
                    groupMessages[currentGroup.id] = grpMessages
                }

                // Simulate reply
                if (!isOfflineMode) {
                    scope.launch {
                        delay(1500)
                        val randomReplier = contacts.filter { currentGroup.members.contains(it.id) }.randomOrNull() ?: contacts.first()
                        val replies = listOf(
                            "Agreed! I will review the documents shortly.",
                            "Splendid! This update looks incredibly polished.",
                            "Let me sync with the core team.",
                            "Can you upload the assets to Bharamputra storage?"
                        )
                        val gReply = ChatMessage(
                            id = "greply_${System.currentTimeMillis()}",
                            senderId = randomReplier.id,
                            senderName = randomReplier.name,
                            text = replies.random(),
                            timestamp = System.currentTimeMillis(),
                            status = "DELIVERED"
                        )
                        grpMessages.add(gReply)
                        groupMessages[currentGroup.id] = grpMessages
                    }
                }
            },
            onUpdateGroup = { name, desc, memberIds ->
                val idx = groups.indexOfFirst { it.id == currentGroup.id }
                if (idx != -1) {
                    val updatedGroup = groups[idx].copy(name = name, description = desc, members = memberIds)
                    groups[idx] = updatedGroup
                    activeChatRoomGroup = updatedGroup
                    Toast.makeText(context, "Group profile configuration updated.", Toast.LENGTH_SHORT).show()
                }
            },
            onBack = { activeChatRoomGroup = null },
            isDarkMode = isDarkMode
        )
    } else {
        // Core Tabbed View
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Barak Chat",
                                fontWeight = FontWeight.Black,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Text(
                                text = "Secure Real-time Client",
                                fontSize = 11.sp,
                                color = ChatTealAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    actions = {
                        // Offline Toggle
                        IconButton(onClick = {
                            isOfflineMode = !isOfflineMode
                            if (!isOfflineMode) {
                                // Dispatch queued
                                if (offlineQueue.isNotEmpty()) {
                                    messages.addAll(offlineQueue.filter { it.senderId == "system" })
                                    offlineQueue.clear()
                                    Toast.makeText(context, "Network online! Dispatched local message queue.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Simulating offline queue. Ticks locked to single gray.", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                contentDescription = "Simulated network mode",
                                tint = if (isOfflineMode) Color.Red else ChatTealAccent
                            )
                        }

                        // Theme switch
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = "Switch Theme"
                            )
                        }

                        // Profile action
                        IconButton(onClick = { showProfileModal = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                tint = ChatTealAccent,
                                contentDescription = "Profile Panel"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = if (isDarkMode) ChatDarkBg else ChatLightBg
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDarkMode) ChatCardDarkBg else ChatCardLightBg,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == "chats",
                        onClick = { activeTab = "chats" },
                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chats") },
                        label = { Text("Chats", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = ChatTealAccent,
                            indicatorColor = ChatTealAccent
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == "social",
                        onClick = { activeTab = "social" },
                        icon = { Icon(Icons.Default.PeopleOutline, contentDescription = "Social") },
                        label = { Text("Social", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = ChatTealAccent,
                            indicatorColor = ChatTealAccent
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == "groups",
                        onClick = { activeTab = "groups" },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Groups") },
                        label = { Text("Groups", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = ChatTealAccent,
                            indicatorColor = ChatTealAccent
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == "status",
                        onClick = { activeTab = "status" },
                        icon = { Icon(Icons.Default.Cameraswitch, contentDescription = "Status") },
                        label = { Text("Status", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = ChatTealAccent,
                            indicatorColor = ChatTealAccent
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(if (isDarkMode) ChatDarkBg else ChatLightBg)
            ) {
                // Main switcher for content tabs
                when (activeTab) {
                    "chats" -> {
                        ChatsTab(
                            contacts = contacts,
                            lastMessages = messages,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onContactSelected = { activeChatRoomContact = it },
                            isDarkMode = isDarkMode
                        )
                    }

                    "social" -> {
                        SocialTab(
                            contacts = contacts,
                            currentUser = currentUser,
                            onAddContact = { name, handle, bio ->
                                val newC = ChatContact(
                                    id = "usr_${System.currentTimeMillis()}",
                                    name = name,
                                    username = handle,
                                    avatarUrl = "river_shield",
                                    bio = bio,
                                    isOnline = false,
                                    lastSeen = "Last seen Just now"
                                )
                                contacts.add(newC)
                                Toast.makeText(context, "$name connected via directory directory lookup!", Toast.LENGTH_SHORT).show()
                            },
                            onChatSelected = { activeChatRoomContact = it },
                            isDarkMode = isDarkMode
                        )
                    }

                    "groups" -> {
                        GroupsTab(
                            groupsList = groups,
                            groupMessages = groupMessages,
                            contacts = contacts,
                            onGroupSelected = { activeChatRoomGroup = it },
                            onCreateGroup = { name, desc, memberIds ->
                                val newG = ChatGroup(
                                    id = "grp_${System.currentTimeMillis()}",
                                    name = name,
                                    description = desc,
                                    avatarUrl = "river_shield",
                                    members = memberIds,
                                    adminId = currentUser?.id ?: "you"
                                )
                                groups.add(newG)
                                groupMessages[newG.id] = mutableStateListOf()
                                Toast.makeText(context, "Group $name deployed successfully!", Toast.LENGTH_SHORT).show()
                            },
                            isDarkMode = isDarkMode
                        )
                    }

                    "status" -> {
                        StatusTab(
                            statuses = statuses,
                            currentUser = currentUser,
                            onPostStatus = { text, img ->
                                val newSt = ChatStatus(
                                    id = "st_${System.currentTimeMillis()}",
                                    userId = currentUser?.id ?: "you",
                                    userName = currentUser?.name ?: "Barak Streamer",
                                    avatarUrl = "river_shield",
                                    contentText = text,
                                    imageUrl = img,
                                    timestamp = System.currentTimeMillis()
                                )
                                statuses.add(0, newSt)
                                Toast.makeText(context, "Story status published successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onShareToShorts = { status ->
                                val updated = statuses.map {
                                    if (it.id == status.id) it.copy(sharedToShorts = true) else it
                                }
                                statuses.clear()
                                statuses.addAll(updated)
                                Toast.makeText(context, "Shared successfully to Bharamputra Shorts ecosystem!", Toast.LENGTH_SHORT).show()
                            },
                            onShareToStories = { status ->
                                val updated = statuses.map {
                                    if (it.id == status.id) it.copy(sharedToStories = true) else it
                                }
                                statuses.clear()
                                statuses.addAll(updated)
                                Toast.makeText(context, "Shared to platform wide creator stories.", Toast.LENGTH_SHORT).show()
                            },
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }

    // Interactive Profile configuration modal dialog
    if (showProfileModal) {
        AlertDialog(
            onDismissRequest = { showProfileModal = false },
            title = { Text("Synchronized Profile") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(ChatTealAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.take(2)?.uppercase() ?: "US",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = currentUser?.name ?: "Barak Explorer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = currentUser?.handle ?: "@explorer", color = ChatTealAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = currentUser?.email ?: "anonymous@bharamputra.com", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Phone: ${currentUser?.phoneNumber?.ifEmpty { "Not linked" } ?: "Not linked"}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) ChatDarkBg else ChatLightBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Authenticated with high-safety Firebase parameters. Unified tokens allow direct access to Bharamputra Music and Video apps.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileModal = false }) {
                    Text("OK", color = ChatTealAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileModal = false
                    onLogout()
                }) {
                    Text("LOG OUT SESSION", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ======================== TABS IMPLEMENTATION ========================

@Composable
fun ChatsTab(
    contacts: List<ChatContact>,
    lastMessages: List<ChatMessage>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onContactSelected: (ChatContact) -> Unit,
    isDarkMode: Boolean
) {
    val filteredContacts = contacts.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search private conversations...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChatTealAccent,
                unfocusedBorderColor = Color.DarkGray
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredContacts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active chats found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredContacts) { contact ->
                    // Grab last message for snippet
                    val userMsg = lastMessages.filter {
                        (it.senderId == contact.id && it.senderName != "You") || (it.senderId == "system" && it.senderName == "You")
                    }
                    val snippet = userMsg.lastOrNull()?.text ?: "Click to open encryption canal."
                    val timeString = userMsg.lastOrNull()?.let {
                        val min = (System.currentTimeMillis() - it.timestamp) / 60000
                        if (min < 1) "Just now" else "${min}m ago"
                    } ?: "Open"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactSelected(contact) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar URL simulation badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ChatTealAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = contact.name.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            // Online Indicator
                            if (contact.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Green)
                                        .border(2.dp, if (isDarkMode) ChatDarkBg else ChatLightBg, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = contact.name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else Color.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = timeString,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = snippet,
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Divider(color = if (isDarkMode) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.4f), thickness = 0.5.dp, modifier = Modifier.padding(start = 82.dp))
                }
            }
        }
    }
}

@Composable
fun SocialTab(
    contacts: List<ChatContact>,
    currentUser: UserAccount?,
    onAddContact: (String, String, String) -> Unit,
    onChatSelected: (ChatContact) -> Unit,
    isDarkMode: Boolean
) {
    var searchDirectoryQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newSocialName by remember { mutableStateOf("") }
    var newSocialHandle by remember { mutableStateOf("") }
    var newSocialBio by remember { mutableStateOf("") }

    val filtered = contacts.filter {
        it.username.contains(searchDirectoryQuery, ignoreCase = true) || it.name.contains(searchDirectoryQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchDirectoryQuery,
                onValueChange = { searchDirectoryQuery = it },
                placeholder = { Text("Search users in Bharamputra Directory...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatTealAccent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ChatTealAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact", tint = Color.Black)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Bharamputra Directory Matches",
                    fontWeight = FontWeight.ExtraBold,
                    color = ChatTealAccent,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (filtered.isEmpty()) {
                item {
                    Text("No social directories found for query.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(filtered) { contact ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) ChatCardDarkBg else ChatCardLightBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(ChatTealAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.name.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color.Black, fontSize = 14.sp)
                                    Text(contact.username, color = ChatTealAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                TextButton(
                                    onClick = { onChatSelected(contact) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ChatTealAccent)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = contact.bio, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Connect with User") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSocialName,
                        onValueChange = { newSocialName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSocialHandle,
                        onValueChange = { newSocialHandle = it },
                        label = { Text("Username Handle (e.g. @assam_vibes)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSocialBio,
                        onValueChange = { newSocialBio = it },
                        label = { Text("Profile Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSocialName.isNotEmpty() && newSocialHandle.isNotEmpty()) {
                        onAddContact(newSocialName, newSocialHandle, newSocialBio)
                        showAddDialog = false
                        newSocialName = ""
                        newSocialHandle = ""
                        newSocialBio = ""
                    }
                }) {
                    Text("FOLLOW & SYNC", color = ChatTealAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Dismiss", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun GroupsTab(
    groupsList: List<ChatGroup>,
    groupMessages: Map<String, List<ChatMessage>>,
    contacts: List<ChatContact>,
    onGroupSelected: (ChatGroup) -> Unit,
    onCreateGroup: (String, String, List<String>) -> Unit,
    isDarkMode: Boolean
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNICATION CHANNELS",
                color = ChatTealAccent,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("NEW GROUP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (groupsList.isEmpty()) {
                item {
                    Text("No collaborative groups constructed.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(groupsList) { grp ->
                    val lastM = groupMessages[grp.id]?.lastOrNull()
                    val msgPreview = lastM?.let { "${it.senderName}: ${it.text}" } ?: "Open channel conversation."
                    val activeSnippetTime = lastM?.let {
                        val min = (System.currentTimeMillis() - it.timestamp) / 60000
                        if (min < 1) "Just now" else "${min}m ago"
                    } ?: ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupSelected(grp) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ChatTealAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(grp.name.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = grp.name,
                                    color = if (isDarkMode) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )

                                Text(activeSnippetTime, color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msgPreview,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Divider(color = if (isDarkMode) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.4f), thickness = 0.5.dp, modifier = Modifier.padding(start = 78.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Assemble Chat Group") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Text("Select Group Members:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ChatTealAccent)
                    Spacer(modifier = Modifier.height(6.dp))

                    contacts.forEach { c ->
                        val checked = selectedMembers.contains(c.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (checked) selectedMembers.remove(c.id) else selectedMembers.add(c.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (it == true) selectedMembers.add(c.id) else selectedMembers.remove(c.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ChatTealAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(c.name, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (groupName.isNotEmpty()) {
                        onCreateGroup(groupName, groupDesc, selectedMembers.toList())
                        showCreateDialog = false
                        groupName = ""
                        groupDesc = ""
                        selectedMembers.clear()
                    }
                }) {
                    Text("DEPLOY GROUP", color = ChatTealAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Dismiss", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun StatusTab(
    statuses: List<ChatStatus>,
    currentUser: UserAccount?,
    onPostStatus: (String, String?) -> Unit,
    onShareToShorts: (ChatStatus) -> Unit,
    onShareToStories: (ChatStatus) -> Unit,
    isDarkMode: Boolean
) {
    var showPostStatusDialog by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var mockImageChecked by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STATUS STORIES",
                    color = ChatTealAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Statuses auto expire after 24 hrs.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = { showPostStatusDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ChatTealAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("POST STATUS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(statuses) { status ->
                // Render status card
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDarkMode) ChatCardDarkBg else ChatCardLightBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ChatTealAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(status.userName.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(status.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                // Simulated 24H timer
                                val hoursLeft = 24 - ((System.currentTimeMillis() - status.timestamp) / 3600000 % 24)
                                Text("${hoursLeft}h remaining until auto-expiration", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = status.contentText, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color.Black)

                        if (status.imageUrl != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simulate status image using standard placeholder icon
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) ChatDarkBg else ChatLightBg)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = ChatTealAccent)
                                }
                            }
                        }

                        // Options footer
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shorts integration
                            Button(
                                onClick = { onShareToShorts(status) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status.sharedToShorts) Color.DarkGray else ChatTealAccent.copy(alpha = 0.15f),
                                    contentColor = if (status.sharedToShorts) Color.Gray else ChatTealAccent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                enabled = !status.sharedToShorts
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (status.sharedToShorts) "Shared to Shorts" else "Share to Shorts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Stories integration
                            Button(
                                onClick = { onShareToStories(status) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status.sharedToStories) Color.DarkGray else ChatTealAccent.copy(alpha = 0.15f),
                                    contentColor = if (status.sharedToStories) Color.Gray else ChatTealAccent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                enabled = !status.sharedToStories
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (status.sharedToStories) "Stories Sync" else "Share Story", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPostStatusDialog) {
        AlertDialog(
            onDismissRequest = { showPostStatusDialog = false },
            title = { Text("What is your current Status?") },
            text = {
                Column {
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        placeholder = { Text("Type what's on your mind...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = mockImageChecked,
                            onCheckedChange = { mockImageChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = ChatTealAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attach simulated scenic camera image", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (statusText.isNotEmpty()) {
                        onPostStatus(statusText, if (mockImageChecked) "river_shield" else null)
                        showPostStatusDialog = false
                        statusText = ""
                        mockImageChecked = false
                    }
                }) {
                    Text("PUBLISH STATUS", color = ChatTealAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostStatusDialog = false }) {
                    Text("Dismiss", color = Color.Gray)
                }
            }
        )
    }
}

// ======================== ROOM CONVERSATIONS ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    contact: ChatContact,
    currentUser: UserAccount?,
    messagesList: List<ChatMessage>,
    isBlocked: Boolean,
    isOffline: Boolean,
    onSendMessage: (String, String?, String?) -> Unit,
    onBack: () -> Unit,
    onBlockToggle: () -> Unit,
    onReportSpam: (String) -> Unit,
    onAttachmentCompression: (Boolean) -> Unit,
    imageCompressing: Boolean,
    isDarkMode: Boolean
) {
    var textMessageInput by remember { mutableStateOf("") }
    var expandedMenu by remember { mutableStateOf(false) }
    var showAbuseReport by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ChatTealAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(contact.name.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isBlocked) "Blocked" else contact.lastSeen,
                                fontSize = 11.sp,
                                color = if (contact.isOnline && !isBlocked) Color.Green else Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expandedMenu = !expandedMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBlocked) "Unblock Creator" else "Block Creator") },
                            onClick = {
                                onBlockToggle()
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )

                        DropdownMenuItem(
                            text = { Text("Report Spam/Abuse") },
                            onClick = {
                                showAbuseReport = true
                                expandedMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) ChatCardDarkBg else ChatCardLightBg
                )
            )
        },
        bottomBar = {
            if (isBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.15f))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Conversation is locked. Unblock user to activate secure messenger channel.", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = if (isDarkMode) ChatDarkBg else ChatLightBg,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column {
                        // Compression Indicator Spinner
                        if (imageCompressing) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ChatTealAccent.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = ChatTealAccent, strokeWidth = 1.5.dp, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Encrypting & Compressing visual payload (60% ratio optimization rules)...", fontSize = 11.sp, color = ChatTealAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rich attachment menu
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    onAttachmentCompression(true)
                                    delay(1500)
                                    onAttachmentCompression(false)
                                    onSendMessage("Dispatched secure image attachment blueprint", "IMAGE", "scenic_photo.png")
                                }
                            }) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Send Image", tint = ChatTealAccent)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            OutlinedTextField(
                                value = textMessageInput,
                                onValueChange = { textMessageInput = it },
                                placeholder = { Text("Write encrypted sentence...", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                maxLines = 4,
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChatTealAccent,
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = {
                                    if (textMessageInput.trim().isNotEmpty()) {
                                        onSendMessage(textMessageInput, null, null)
                                        textMessageInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ChatTealAccent)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDarkMode) ChatDarkBg else ChatLightBg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Top,
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Messages displayed reversed for standard chat flow
                val sorted = messagesList.sortedByDescending { it.timestamp }
                items(sorted) { msg ->
                    val isYou = msg.senderId == "system" || msg.senderName == "You"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isYou) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isYou) {
                                    if (isDarkMode) ChatTealLight else ChatSendBubbleLight
                                } else {
                                    if (isDarkMode) ChatIncomingDark else ChatReceiveBubbleLight
                                }
                            ),
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isYou) 14.dp else 0.dp,
                                bottomEnd = if (isYou) 0.dp else 14.dp
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (msg.attachmentType != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.15f))
                                            .padding(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (msg.attachmentType) {
                                                "IMAGE" -> Icons.Default.InsertPhoto
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = ChatTealAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = msg.attachmentName ?: "Payload", fontSize = 11.sp, maxLines = 1)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val timeLabel = remember(msg.timestamp) { "Now" }
                                    Text(text = timeLabel, color = Color.Gray, fontSize = 9.sp)
                                    
                                    if (isYou) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = when (msg.status) {
                                                "READ" -> Icons.Default.DoneAll
                                                "DELIVERED" -> Icons.Default.DoneAll
                                                else -> Icons.Default.Done
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp),
                                            tint = if (msg.status == "READ") ChatTealAccent else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAbuseReport) {
        AlertDialog(
            onDismissRequest = { showAbuseReport = false },
            title = { Text("Report Abuse/Spam") },
            text = {
                Column {
                    Text("Identify reason of report for user ${contact.username}:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Spamming promotion, cyber-harrasment, etc.") },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (reportReason.isNotEmpty()) {
                        onReportSpam(reportReason)
                        showAbuseReport = false
                        reportReason = ""
                    }
                }) {
                    Text("SUBMIT REPORT", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbuseReport = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRoomScreen(
    group: ChatGroup,
    currentUser: UserAccount?,
    messagesList: List<ChatMessage>,
    contactsList: List<ChatContact>,
    isOffline: Boolean,
    onSendMessage: (String, String?, String?) -> Unit,
    onUpdateGroup: (String, String, List<String>) -> Unit,
    onBack: () -> Unit,
    isDarkMode: Boolean
) {
    var textMessageInput by remember { mutableStateOf("") }
    var showGroupConfig by remember { mutableStateOf(false) }

    var configName by remember { mutableStateOf(group.name) }
    var configDesc by remember { mutableStateOf(group.description) }
    val configMembers = remember { mutableStateListOf<String>().apply { addAll(group.members) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { showGroupConfig = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChatTealAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(group.name.take(1).uppercase(), color = ChatTealAccent, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(group.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("${group.members.size} secure communication participants", fontSize = 11.sp, color = ChatTealAccent)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showGroupConfig = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Group")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) ChatCardDarkBg else ChatCardLightBg
                )
            )
        },
        bottomBar = {
            Surface(
                color = if (isDarkMode) ChatDarkBg else ChatLightBg,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onSendMessage("Shared collaborative diagram attachment", "IMAGE", "chart_diagram.jpg")
                    }) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Shared Docs", tint = ChatTealAccent)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedTextField(
                        value = textMessageInput,
                        onValueChange = { textMessageInput = it },
                        placeholder = { Text("Write group channel statement...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChatTealAccent,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (textMessageInput.trim().isNotEmpty()) {
                                onSendMessage(textMessageInput, null, null)
                                textMessageInput = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ChatTealAccent)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDarkMode) ChatDarkBg else ChatLightBg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                val sorted = messagesList.sortedByDescending { it.timestamp }
                items(sorted) { msg ->
                    val isYou = msg.senderId == currentUser?.id || msg.senderName == "You"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isYou) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isYou) {
                                    if (isDarkMode) ChatTealLight else ChatSendBubbleLight
                                } else {
                                    if (isDarkMode) ChatIncomingDark else ChatReceiveBubbleLight
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!isYou) {
                                    Text(msg.senderName, fontSize = 11.sp, color = ChatTealAccent, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                if (msg.attachmentType != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.15f))
                                            .padding(6.dp)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = ChatTealAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = msg.attachmentName ?: "Shared Sync", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Text(text = msg.text, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupConfig) {
        val isAdmin = group.adminId == currentUser?.id || group.adminId == "you"
        AlertDialog(
            onDismissRequest = { showGroupConfig = false },
            title = { Text("Group Management Controls") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Admin: ${if (isAdmin) "You (Full control)" else "Collaboration channel"}", color = ChatTealAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = configName,
                        onValueChange = { if (isAdmin) configName = it },
                        label = { Text("Name") },
                        enabled = isAdmin,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = configDesc,
                        onValueChange = { if (isAdmin) configDesc = it },
                        label = { Text("Description") },
                        enabled = isAdmin,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                    )

                    Text("Active Group Members:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ChatTealAccent)
                    Spacer(modifier = Modifier.height(6.dp))

                    contactsList.forEach { c ->
                        val inGrp = configMembers.contains(c.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isAdmin) {
                                    if (inGrp) configMembers.remove(c.id) else configMembers.add(c.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = inGrp,
                                onCheckedChange = {
                                    if (isAdmin) {
                                        if (it == true) configMembers.add(c.id) else configMembers.remove(c.id)
                                    }
                                },
                                enabled = isAdmin,
                                colors = CheckboxDefaults.colors(checkedColor = ChatTealAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(c.name, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (isAdmin) {
                    TextButton(onClick = {
                        onUpdateGroup(configName, configDesc, configMembers.toList())
                        showGroupConfig = false
                    }) {
                        Text("SAVE CONFIGURATION", color = ChatTealAccent, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { showGroupConfig = false }) {
                        Text("Dismiss", color = Color.Gray)
                    }
                }
            },
            dismissButton = {
                if (isAdmin) {
                    TextButton(onClick = { showGroupConfig = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        )
    }
}
