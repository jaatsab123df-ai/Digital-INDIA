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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.data.auth.BharamputraAuth
import com.example.data.auth.UserAccount
import com.example.data.models.VideoItem
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Premium Minimal Studio Theme Palette (YouTube Studio Dark style) ---
val StudioMidnight = Color(0xFF0B0F12)       // Rich Indigo Midnight Slate
val StudioCardBg = Color(0xFF161E24)         // Content Surface Card
val StudioVideoRed = Color(0xFFFC3C31)       // Premium Video/Impressions Crimson
val StudioMusicGold = Color(0xFFFDBF2D)      // Glowing Gold Streaming Accent
val StudioGrowthEmerald = Color(0xFF10B981)  // Success Green Trend Accent
val StudioIndicatorBlue = Color(0xFF3B82F6)  // Clean Blue Link Info

val StudioLightBg = Color(0xFFF4F6F8)        // Light Clean Grey
val StudioCardLight = Color(0xFFFFFFFF)      // Full White Card
val StudioLightTextPrimary = Color(0xFF1F2937)// Deep Charcoal

class AnalyticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this).get(BharamputraViewModel::class.java)

        setContent {
            var isDarkMode by remember { mutableStateOf(true) }

            MaterialTheme(
                colorScheme = if (isDarkMode) {
                    darkColorScheme(
                        primary = StudioVideoRed,
                        secondary = StudioMusicGold,
                        background = StudioMidnight,
                        surface = StudioCardBg
                    )
                } else {
                    lightColorScheme(
                        primary = StudioVideoRed,
                        secondary = StudioMusicGold,
                        background = StudioLightBg,
                        surface = StudioCardLight
                    )
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkMode) StudioMidnight else StudioLightBg
                ) {
                    BharamputraAnalyticsNavigation(
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
fun BharamputraAnalyticsNavigation(
    viewModel: BharamputraViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var hasVerifiedByPhone by remember { mutableStateOf(false) }

    // Synchronize workspace credentials inside the Bharamputra ecosystem
    LaunchedEffect(currentUser) {
        if (currentUser != null && !hasVerifiedByPhone) {
            hasVerifiedByPhone = true
            Toast.makeText(context, "Welcome, ${currentUser?.name}! Aligning multi-app studio statistics.", Toast.LENGTH_SHORT).show()
        }
    }

    if (!hasVerifiedByPhone && currentUser == null) {
        // Shared Phone OTP Authentication gateway specifically designed for creator sync
        AnalyticsCreatorAuthGateway(
            viewModel = viewModel,
            onVerified = { phone, account ->
                hasVerifiedByPhone = true
                Toast.makeText(context, "Analytics successfully connected with +91 $phone", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        // Main Professional Analytics Interface (YouTube Studio Style + Merged Ecosystem metrics)
        BharamputraStudioDashboard(
            viewModel = viewModel,
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onLogout = {
                hasVerifiedByPhone = false
                viewModel.logout()
            }
        )
    }
}

// --- SECURE PHONE MULTI-APP AUTH ---
@Composable
fun AnalyticsCreatorAuthGateway(
    viewModel: BharamputraViewModel,
    onVerified: (String, UserAccount?) -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(0) }
    var isSmsListening by remember { mutableStateOf(false) }

    // Dummy creators associated with the ecosystem
    val prelinkedDatabase = remember {
        mapOf(
            "+919876543210" to Triple("Bharamputra Explorer", "explorer@bharamputra.com", "@explorer_barak"),
            "+911111111111" to Triple("Vedic Beats", "beats_vedic@bharamputra.com", "@vedic_beats"),
            "+919999999999" to Triple("Vedic Tech Hub", "vedic_coder@bharamputra.com", "@vedic_coder")
        )
    }

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        }
    }

    LaunchedEffect(isCodeSent) {
        if (isCodeSent) {
            isSmsListening = true
            delay(2000)
            if (isSmsListening) {
                verificationCode = "888123"
                Toast.makeText(context, "SmsRetriever API: Secure OTP intercepted!", Toast.LENGTH_SHORT).show()
                isSmsListening = false
                delay(800)
                // Auto verify
                val match = prelinkedDatabase[if (phoneNumber.startsWith("+91")) phoneNumber else "+91$phoneNumber"]
                if (match != null) {
                    val (name, email, handle) = match
                    viewModel.registerWithEmail(email, name, phoneNumber)
                    onVerified(phoneNumber, viewModel.currentUser.value)
                } else {
                    viewModel.registerWithEmail("${phoneNumber}@bharamputra.com", "Studio Creator", phoneNumber)
                    onVerified(phoneNumber, viewModel.currentUser.value)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioMidnight)
    ) {
        // Radial Background Ambient Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = StudioVideoRed.copy(alpha = 0.08f),
                radius = size.width,
                center = Offset(0f, 0f)
            )
            drawCircle(
                color = StudioMusicGold.copy(alpha = 0.06f),
                radius = size.width * 0.8f,
                center = Offset(size.width, size.height)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Visual Icon representing YouTube Studio + Merged analytics
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(StudioVideoRed, StudioMusicGold)))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(StudioMidnight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Bharamputra Analytics Hub",
                        tint = StudioVideoRed,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BHARAMPUTRA ANALYTICS HUB",
                color = StudioVideoRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 5.sp
            )

            Text(
                text = "Creator Studio App",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Synchronized Video & Music Insights",
                fontSize = 13.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isCodeSent) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = StudioVideoRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SECURE PHONE RETRIEVAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Provide your registered mobile number below to load merged subscriber stats, video views, and streaming logs.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StudioMidnight)
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
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
                                    focusedBorderColor = StudioVideoRed,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedPlaceholderColor = Color.DarkGray,
                                    unfocusedPlaceholderColor = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("barak_phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sandbox test numbers: +919876543210, +919999999999",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (phoneNumber.filter { it.isDigit() }.length < 10) {
                                    Toast.makeText(context, "Please enter a valid 10-digit number.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isCodeSent = true
                                countdownSeconds = 30
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioVideoRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("barak_send_otp_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("REQUEST CREATOR OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "OTP SENT TO +91 $phoneNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioVideoRed
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto verification active. Standard simulated SMS code is 888123.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = { verificationCode = it },
                            label = { Text("6-Digit Verification Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = StudioVideoRed,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = StudioVideoRed,
                                unfocusedLabelColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("barak_otp_code_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom SMS listener progress area
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudioMidnight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = StudioVideoRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Waiting for SMS retrieve...", color = Color.Gray, fontSize = 11.sp)
                                }
                                Box(modifier = Modifier.size(14.dp)) {
                                    CircularProgressIndicator(strokeWidth = 2.dp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (verificationCode == "888123") {
                                    val match = prelinkedDatabase[if (phoneNumber.startsWith("+91")) phoneNumber else "+91$phoneNumber"]
                                    if (match != null) {
                                        val (name, email, handle) = match
                                        viewModel.registerWithEmail(email, name, phoneNumber)
                                        onVerified(phoneNumber, viewModel.currentUser.value)
                                    } else {
                                        viewModel.registerWithEmail("${phoneNumber}@bharamputra.com", "Studio Creator", phoneNumber)
                                        onVerified(phoneNumber, viewModel.currentUser.value)
                                    }
                                } else {
                                    Toast.makeText(context, "Invalid OTP code. Try again.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioVideoRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("barak_phone_verify_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CONFIRM ALIGNMENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { isCodeSent = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Change Number", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick bypass bypass button for demonstration
            OutlinedButton(
                onClick = {
                    viewModel.registerWithEmail("bharamputra_studio@gmail.com", "Bharamputra Explorer", "+919876543210")
                    onVerified("9876543210", viewModel.currentUser.value)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gmail_quick_login"),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = StudioMusicGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUICK DEMO ALIGNMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// --- MAIN STUDIO DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BharamputraStudioDashboard(
    viewModel: BharamputraViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    var activeTab by remember { mutableStateOf("overview") } // overview, revenue, content, audience, growth
    
    // Live Synchronisation stats
    var isLiveSyncing by remember { mutableStateOf(false) }
    var syncAnimationRotation by remember { mutableStateOf(0f) }
    
    // Growth factors custom values
    var videoViewsScale by remember { mutableStateOf(1.0f) }
    var musicStreamsScale by remember { mutableStateOf(1.0f) }

    // Aggregate values
    val baseVideoViews = remember(allVideos) { allVideos.sumOf { it.views }.coerceAtLeast(148500) }
    val baseMusicStreams = 89430L // Merged from Audio system database
    
    val videoViews = (baseVideoViews * videoViewsScale).toLong()
    val musicStreams = (baseMusicStreams * musicStreamsScale).toLong()
    val totalSubs = 12850
    val totalVideoLikes = allVideos.sumOf { it.likes }.coerceAtLeast(2450)

    val videoRevenue = videoViews * 0.05 // $0.05 per 100 views
    val musicRevenue = musicStreams * 0.12 // $0.12 per stream
    val totalCombinedRevenue = videoRevenue + musicRevenue

    // Syncing state trigger
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }

    val handleSyncClick = {
        scope.launch {
            isLiveSyncing = true
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + 360f,
                animationSpec = tween(1200, easing = LinearEasing)
            )
            // Simulated calculation updates from firebase
            delay(200)
            videoViewsScale += Random.nextFloat() * 0.05f + 0.02f
            musicStreamsScale += Random.nextFloat() * 0.07f + 0.03f
            isLiveSyncing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(StudioVideoRed, StudioMusicGold)))
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(StudioMidnight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = StudioVideoRed, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Bharamputra Analytics Hub",
                                color = if (isDarkMode) Color.White else StudioLightTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Video & Music Studio Engine",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Sync Button with animated rotation
                    IconButton(
                        onClick = { handleSyncClick() },
                        modifier = Modifier.rotate(rotationAnim.value)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Firebase Metrics",
                            tint = if (isLiveSyncing) StudioVideoRed else Color.LightGray
                        )
                    }

                    // Simple Light / Dark mode toggle
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Aesthetic Theme",
                            tint = Color.LightGray
                        )
                    }

                    // Logout / Flush Credentials
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Disconnect Ecosystem",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) StudioMidnight else StudioLightBg
                )
            )
        },
        bottomBar = {
            // Elegant canonical navigation bar
            NavigationBar(
                containerColor = if (isDarkMode) StudioCardBg else StudioCardLight,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val navItems = listOf(
                    Triple("overview", "Overview", Icons.Default.Dashboard),
                    Triple("revenue", "Revenue", Icons.Default.AttachMoney),
                    Triple("content", "Content", Icons.Default.VideoLibrary),
                    Triple("audience", "Audience", Icons.Default.People),
                    Triple("growth", "Growth", Icons.Default.ShowChart)
                )

                navItems.forEach { (route, label, icon) ->
                    val isSelected = activeTab == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { activeTab = route },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioMidnight,
                            selectedTextColor = StudioVideoRed,
                            indicatorColor = StudioVideoRed,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = if (isDarkMode) StudioMidnight else StudioLightBg,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen router
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "Studio Tab Animation"
            ) { tab ->
                when (tab) {
                    "overview" -> OverviewTab(
                        isDarkMode = isDarkMode,
                        currentUser = currentUser,
                        videoViews = videoViews,
                        videoLikes = totalVideoLikes,
                        musicStreams = musicStreams,
                        totalSubs = totalSubs,
                        totalRevenue = totalCombinedRevenue,
                        allVideos = allVideos,
                        isLiveSyncing = isLiveSyncing
                    )
                    "revenue" -> RevenueTab(
                        isDarkMode = isDarkMode,
                        videoRevenue = videoRevenue,
                        musicRevenue = musicRevenue,
                        totalRevenue = totalCombinedRevenue,
                        videoViews = videoViews,
                        musicStreams = musicStreams
                    )
                    "content" -> ContentTab(
                        isDarkMode = isDarkMode,
                        allVideos = allVideos,
                        musicStreams = musicStreams,
                        videoViews = videoViews
                    )
                    "audience" -> AudienceTab(
                        isDarkMode = isDarkMode
                    )
                    "growth" -> GrowthTab(
                        isDarkMode = isDarkMode,
                        totalSubs = totalSubs,
                        videoViews = videoViews,
                        musicStreams = musicStreams
                    )
                }
            }
        }
    }
}

// ---------------- OVERVIEW TAB ----------------
@Composable
fun OverviewTab(
    isDarkMode: Boolean,
    currentUser: UserAccount?,
    videoViews: Long,
    videoLikes: Long,
    musicStreams: Long,
    totalSubs: Int,
    totalRevenue: Double,
    allVideos: List<VideoItem>,
    isLiveSyncing: Boolean
) {
    var lineChartSelectedIndex by remember { mutableStateOf(-1) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Creator Channel Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(StudioVideoRed, StudioMusicGold)))
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=240",
                            contentDescription = "Creator Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Bharamputra Explorer",
                            color = if (isDarkMode) Color.White else StudioLightTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.handle ?: "@explorer_barak",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StudioVideoRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL PARTNER",
                                    color = StudioVideoRed,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Simple Live sync pulse indicator
                    if (isLiveSyncing) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StudioGrowthEmerald)
                        )
                    }
                }
            }
        }

        // Consolidated Stats Overview Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "CONSOLIDATED PERFORMANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    "LAST 28 DAYS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioVideoRed
                )
            }
        }

        // Core Aggregate Metrics Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Video Views Card
                    StudioMetricItem(
                        title = "Video Views",
                        value = formatNumber(videoViews),
                        trend = "+12.4%",
                        accentColor = StudioVideoRed,
                        icon = Icons.Default.VideoFile,
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )

                    // Music Streams Card
                    StudioMetricItem(
                        title = "Music Streams",
                        value = formatNumber(musicStreams),
                        trend = "+18.9%",
                        accentColor = StudioMusicGold,
                        icon = Icons.Default.MusicNote,
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Combined Revenue Card
                    StudioMetricItem(
                        title = "Est. Earnings",
                        value = String.format("$%.2f", totalRevenue),
                        trend = "+22.1%",
                        accentColor = StudioGrowthEmerald,
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )

                    // Combined Subscribers count
                    StudioMetricItem(
                        title = "Channel Fans",
                        value = formatNumber(totalSubs.toLong()),
                        trend = "+840 this mo",
                        accentColor = StudioIndicatorBlue,
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }
            }
        }

        // Custom High-Fidelity Canvas Line Chart
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "COMBINED AUDIENCE IMPRESSIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Live Synchronized Trends",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDarkMode) Color.White else StudioLightTextPrimary
                            )
                        }

                        // Mini visualizer legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StudioVideoRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Video", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StudioMusicGold))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audio", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Line Chart primitive
                    val chartDataPoints = listOf(28f, 42f, 35f, 56f, 49f, 68f, 75f)
                    val chartInteractiveValue = listOf("1.2K", "1.6K", "1.4K", "2.1K", "1.9K", "2.5K", "2.9K")
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw grid lines
                            val parts = 4
                            val stepY = size.height / parts
                            for (i in 0..parts) {
                                drawLine(
                                    color = Color.DarkGray.copy(alpha = 0.2f),
                                    start = Offset(0f, i * stepY),
                                    end = Offset(size.width, i * stepY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw Line path using Bezier calculations
                            val stepX = size.width / (chartDataPoints.size - 1)
                            val linePath = Path()
                            val fillPath = Path()

                            chartDataPoints.forEachIndexed { index, value ->
                                val normalizedValue = (100f - value) / 100f // inverted for canvas coordinate
                                val x = index * stepX
                                val y = normalizedValue * size.height

                                if (index == 0) {
                                    linePath.moveTo(x, y)
                                    fillPath.moveTo(x, size.height)
                                    fillPath.lineTo(x, y)
                                } else {
                                    val prevX = (index - 1) * stepX
                                    val prevY = ((100f - chartDataPoints[index - 1]) / 100f) * size.height
                                    val controlX1 = prevX + (stepX / 2)
                                    val controlY1 = prevY
                                    val controlX2 = prevX + (stepX / 2)
                                    val controlY2 = y

                                    linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                }

                                if (index == chartDataPoints.size - 1) {
                                    fillPath.lineTo(x, size.height)
                                    fillPath.close()
                                }
                            }

                            // Draw gradient area under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(StudioVideoRed.copy(alpha = 0.18f), Color.Transparent)
                                )
                            )

                            // Main stroked path
                            drawPath(
                                path = linePath,
                                color = StudioVideoRed,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw cursor tracking dot if interacted
                            val lastIndex = if (lineChartSelectedIndex == -1) chartDataPoints.size - 1 else lineChartSelectedIndex
                            val lastX = lastIndex * stepX
                            val lastY = ((100f - chartDataPoints[lastIndex]) / 100f) * size.height

                            drawCircle(
                                color = StudioMidnight,
                                radius = 7.dp.toPx(),
                                center = Offset(lastX, lastY)
                            )
                            drawCircle(
                                color = StudioVideoRed,
                                radius = 4.dp.toPx(),
                                center = Offset(lastX, lastY)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sparkline labels (Mon to Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        days.forEachIndexed { index, name ->
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                color = if (lineChartSelectedIndex == index) StudioVideoRed else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { lineChartSelectedIndex = index }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }
                    }

                    if (lineChartSelectedIndex != -1) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudioMidnight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Selected Day Impressions:",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    chartInteractiveValue[lineChartSelectedIndex],
                                    fontSize = 12.sp,
                                    color = StudioVideoRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Performing Videos / Songs Header
        item {
            Text(
                "TOP PERFORMING CONSOLIDATED MEDIA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
        }

        // Top Media list
        items(allVideos.take(3)) { video ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    // Video Thumbnail
                    Box(
                        modifier = Modifier
                            .size(72.dp, 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = "Media Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                video.duration,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = if (isDarkMode) Color.White else StudioLightTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${video.category} • Synced from Video app",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(formatNumber(video.views), fontSize = 10.sp, color = Color.LightGray)

                            Spacer(modifier = Modifier.width(10.dp))

                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(formatNumber(video.likes), fontSize = 10.sp, color = Color.LightGray)
                        }
                    }

                    // Score Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioGrowthEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "#1",
                            color = StudioGrowthEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ---------------- REVENUE TAB ----------------
@Composable
fun RevenueTab(
    isDarkMode: Boolean,
    videoRevenue: Double,
    musicRevenue: Double,
    totalRevenue: Double,
    videoViews: Long,
    musicStreams: Long
) {
    var revenueCategoryFilter by remember { mutableStateOf("all") } // all, video, music
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Income Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "COMBINED REVENUE ECOSYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format("$%,.2f", totalRevenue),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = StudioGrowthEmerald
                    )

                    Text(
                        text = "Total estimated creator earnings merged securely",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tab Selector Inside Revenue Screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) StudioMidnight else StudioLightBg)
                            .padding(3.dp)
                    ) {
                        listOf("all" to "Combined", "video" to "Video Share", "music" to "Music Share").forEach { (term, label) ->
                            val isSelected = revenueCategoryFilter == term
                            Button(
                                onClick = { revenueCategoryFilter = term },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) StudioGrowthEmerald else Color.Transparent,
                                    contentColor = if (isSelected) StudioMidnight else Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Custom Ring/Pie Chart showing Platform Earnings Distribution
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "REVENUE DISTRIBUTION ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawing Circle Chart
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val total = videoRevenue + musicRevenue
                                val videoAngle = (videoRevenue / total * 360f).toFloat()
                                val musicAngle = (musicRevenue / total * 360f).toFloat()

                                drawArc(
                                    color = StudioVideoRed,
                                    startAngle = -90f,
                                    sweepAngle = videoAngle,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )

                                drawArc(
                                    color = StudioMusicGold,
                                    startAngle = -90f + videoAngle,
                                    sweepAngle = musicAngle,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ROI Splits", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("M3 Engine", fontSize = 9.sp, color = StudioGrowthEmerald, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Platform Earnings side breakdown
                        Column(
                            modifier = Modifier.weight(1.2f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StudioVideoRed))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bharamputra Video", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = String.format("$%,.2f (%.1f%%)", videoRevenue, (videoRevenue / totalRevenue) * 100f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color.White else StudioLightTextPrimary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StudioMusicGold))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bharamputra Music", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = String.format("$%,.2f (%.1f%%)", musicRevenue, (musicRevenue / totalRevenue) * 100f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color.White else StudioLightTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Growth revenue prediction graph / card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StudioGrowthEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "GROWTH ESTIMATION & METRIC AUTO-PREDICTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Based on current subscriber multiplication and digital stream acceleration rules, your channel is on track to earn:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next milestone aggregates
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) StudioMidnight else StudioLightBg),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Next 90 Days Dynamic", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = String.format("$%,.2f", totalRevenue * 3.42),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioVideoRed
                                )
                                Text("Confidence level: 94%", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isDarkMode) StudioMidnight else StudioLightBg),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Projected 1-Yr Net", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = String.format("$%,.2f", totalRevenue * 15.6),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioMusicGold
                                )
                                Text("Confidence level: 86%", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CONTENT TAB ----------------
@Composable
fun ContentTab(
    isDarkMode: Boolean,
    allVideos: List<VideoItem>,
    musicStreams: Long,
    videoViews: Long
) {
    var contentSubTab by remember { mutableStateOf("videos") } // videos vs audio
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Platform Hub Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val states = listOf("videos" to "Videos Hub", "audio" to "Music Tracks")
                    states.forEach { (route, text) ->
                        val isSelected = contentSubTab == route
                        Button(
                            onClick = { contentSubTab = route },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) StudioVideoRed else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.Gray
                            )
                        ) {
                            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active listing
        if (contentSubTab == "videos") {
            item {
                Text(
                    "INDIVIDUAL VIDEO METRICS INVENTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }

            if (allVideos.isEmpty()) {
                item {
                    EmptyStatePlaceholder(text = "No Creator Videos Synced from Platform Room DB.")
                }
            } else {
                items(allVideos) { video ->
                    VideoMetricsRow(video = video, isDarkMode = isDarkMode)
                }
            }
        } else {
            item {
                Text(
                    "AUDIO TRACKS & RADIO CHANNELS STREAM ANALYTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
            }

            // High custom mock music tracks matching the stream ecosystem
            val mockAudioTracks = listOf(
                Triple("Golden Tezpur Waves", "Assam Beats", 42300L),
                Triple("Hajar Jibon folk remix", "Lachit Digital Orchestra", 28400L),
                Triple("Late-Night Coder Lofi", "NE Beats Synthesizer", 11230L),
                Triple("Bharamputra Sunset Strings", "Northeastern Beats", 7500L)
            )

            items(mockAudioTracks) { (title, artist, streams) ->
                MusicMetricsRow(title = title, artist = artist, streams = streams, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun VideoMetricsRow(video: VideoItem, isDarkMode: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .size(80.dp, 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        video.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color.White else StudioLightTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Category: ${video.category} • Uploaded Successfully",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini analytical indicators in horizontal row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("VIEWS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(formatNumber(video.views), fontSize = 12.sp, color = StudioVideoRed, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("LIKES", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(formatNumber(video.likes), fontSize = 12.sp, color = StudioMusicGold, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("WATCH TIME", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${(video.views * 1.4).toInt()}h", fontSize = 12.sp, color = StudioGrowthEmerald, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("CTR", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${6.4 + (video.likes % 4)}%", fontSize = 12.sp, color = StudioIndicatorBlue, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun MusicMetricsRow(title: String, artist: String, streams: Long, isDarkMode: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioMidnight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = StudioMusicGold, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color.White else StudioLightTextPrimary
                    )
                    Text(artist, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }

                // Small live streaming icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioMusicGold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("AAC 256K", color = StudioMusicGold, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Music indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL STREAMS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(formatNumber(streams), fontSize = 12.sp, color = StudioMusicGold, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("REPLAY GAIN", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("+82.1%", fontSize = 12.sp, color = StudioGrowthEmerald, fontWeight = FontWeight.Black)
                }

                Column {
                    Text("RETENTION", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${58 + (streams % 10)}%", fontSize = 12.sp, color = StudioIndicatorBlue, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ---------------- AUDIENCE TAB ----------------
@Composable
fun AudienceTab(isDarkMode: Boolean) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "REGIONAL & CRITICAL AUDIENCE DEMOGRAPHICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // Region Hotspots Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = StudioVideoRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GEOGRAPHIC TOP STREAMING REGIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val regions = listOf(
                        "Guwahati Metro" to 54,
                        "Tezpur & Jorhat Districts" to 22,
                        "Dibrugarh Audio Circle" to 14,
                        "Shillong & Northeast" to 6,
                        "Delhi/Noida Synced" to 4
                    )

                    regions.forEach { (name, percent) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color.White else StudioLightTextPrimary
                                )
                                Text("$percent%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = StudioVideoRed)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Custom progress meter
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percent / 100f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Brush.linearGradient(listOf(StudioVideoRed, StudioMusicGold)))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Engagement Heatmap / Clock Sync representation
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = StudioMusicGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "WHEN YOUR FANS ARE ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Calculated across merged Firebase activity trackers representing active play behaviors in Barak Social & Video apps.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Heatmap Grid Matrix
                    val periods = listOf("6 AM", "12 PM", "6 PM", "12 AM")
                    val gridMap = listOf(
                        listOf(0.2f, 0.4f, 0.7f, 0.3f), // Mon
                        listOf(0.1f, 0.5f, 0.8f, 0.2f), // Wed
                        listOf(0.3f, 0.6f, 0.9f, 0.4f), // Fri
                        listOf(0.4f, 0.8f, 1.0f, 0.6f), // Sun
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("M", "W", "F", "S").forEach { day ->
                                Text(
                                    day,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Gray,
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            gridMap.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.forEach { intensity ->
                                        Box(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StudioVideoRed.copy(alpha = intensity))
                                        )
                                    }
                                }
                            }

                            // Horizontal labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                periods.forEach { time ->
                                    Text(time, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- GROWTH TAB (MONETISATION & MILESTONES) ----------------
@Composable
fun GrowthTab(
    isDarkMode: Boolean,
    totalSubs: Int,
    videoViews: Long,
    musicStreams: Long
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "MONETISATION & CREATOR GOAL MILESTONES",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // Program Progress Meter
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StudioMusicGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "BHARAMPUTRA REVENUE ACCESS METERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress 1: Subscribers
                    val subProgress = (totalSubs.toFloat() / 100000f).coerceAtMost(1.0f)
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Channel Fans Reach (Goal: 100K Silver Plaque)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${totalSubs}/100,000",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else StudioLightTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { subProgress },
                            color = StudioGrowthEmerald,
                            trackColor = Color.DarkGray.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    // Progress 2: Watch/Stream items combined
                    val videoStreamSum = (videoViews * 1.5).toLong()
                    val requiredStreamHours = 4000L
                    val currentStreamHours = (videoStreamSum / 100L).coerceAtMost(requiredStreamHours)
                    val streamRatio = currentStreamHours.toFloat() / requiredStreamHours.toFloat()

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stream Watch Hours (Goal: 4,000 hrs)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(
                                "$currentStreamHours/4,000 hrs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else StudioLightTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { streamRatio },
                            color = StudioVideoRed,
                            trackColor = Color.DarkGray.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // Creator Badge Room
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "UNLOCKABLE CREATOR BADGES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val badges = listOf(
                            Triple("Golden River", "1M+ combined play cycles", Icons.Default.WaterDrop),
                            Triple("Vedic Artisan", "Synced tech codes upload", Icons.Default.Code),
                            Triple("High Earner", "Verified $2k milestone", Icons.Default.TrendingUp)
                        )

                        badges.forEach { (name, desc, icon) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(StudioMidnight)
                                        .border(2.dp, StudioMusicGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = StudioMusicGold, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else StudioLightTextPrimary, textAlign = TextAlign.Center)
                                Text(desc, fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- UTILS & EMPTY STATE ----------------
@Composable
fun StudioMetricItem(
    title: String,
    value: String,
    trend: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) StudioCardBg else StudioCardLight
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isDarkMode) Color.White else StudioLightTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = StudioGrowthEmerald,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = trend,
                    fontSize = 10.sp,
                    color = StudioGrowthEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}
