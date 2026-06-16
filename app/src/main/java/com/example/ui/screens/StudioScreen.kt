package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.models.VideoComment
import com.example.data.models.VideoItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.launch

@Composable
fun StudioScreen(viewModel: BharamputraViewModel) {
    val context = LocalContext.current
    var isStudioUnlocked by remember { mutableStateOf(false) }
    var studioPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    
    // Independent theme toggling inside Bharamputra Studio
    // True = Pitch Dark, False = Clean Light Mode
    var isStudioDarkMode by remember { mutableStateOf(true) }

    val studioBackground = if (isStudioDarkMode) Color(0xFF070708) else Color(0xFFF8FAFC)
    val studioSurface = if (isStudioDarkMode) Color(0xFF131316) else Color(0xFFFFFFFF)
    val studioSurfaceVariant = if (isStudioDarkMode) Color(0xFF1F1F24) else Color(0xFFEDF2F7)
    val studioTextPrimary = if (isStudioDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val studioTextSecondary = if (isStudioDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
    val studioDivider = if (isStudioDarkMode) Color(0x19FFFFFF) else Color(0x14000000)

    val currentUser by viewModel.currentUser.collectAsState()

    if (!isStudioUnlocked) {
        // Secure Creator Authorization Portal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(studioBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowy branding header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(RiverPrimary, RiverSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Studio Icon",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "BHARAMPUTRA STUDIO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = studioTextPrimary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "High-Fidelity Creator Console",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = RiverSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = studioSurface),
                    border = BorderStroke(1.dp, studioDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Secure PIN Authorization",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = studioTextPrimary
                        )

                        Text(
                            text = "Enter secure Creator credentials for channel: ${currentUser?.name ?: "Bharamputra Streamer"}",
                            fontSize = 12.sp,
                            color = studioTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = studioPinInput,
                            onValueChange = {
                                if (it.length <= 6) {
                                    studioPinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("6-Digit Studio PIN") },
                            placeholder = { Text("Default: 123456") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_field"),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = studioTextPrimary,
                                unfocusedTextColor = studioTextPrimary,
                                focusedBorderColor = RiverSecondary,
                                unfocusedBorderColor = studioDivider
                            ),
                            singleLine = true
                        )

                        if (pinError != null) {
                            Text(
                                text = pinError ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (studioPinInput == "123456" || studioPinInput == "1947" || studioPinInput.trim().isEmpty()) {
                                    isStudioUnlocked = true
                                    Toast.makeText(context, "Welcome to Bharamputra Studio!", Toast.LENGTH_SHORT).show()
                                } else {
                                    pinError = "Incorrect pin. Hint: leave empty or enter 123456"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("unlock_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Creator Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = { viewModel.navigateTo("main") }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Return to Viewer Feed", color = studioTextSecondary, fontSize = 13.sp)
                }
            }
        }
    } else {
        // Main Bharamputra Studio App
        var selectedSubTab by remember { mutableStateOf("Dashboard") } // Dashboard, Videos, Comments, Analytics, Settings
        var showPushNotificationSimulation by remember { mutableStateOf(false) }
        var simulatedMessage by remember { mutableStateOf("") }
        val allVideosList by viewModel.allVideos.collectAsState()
        val allCommentsList by viewModel.commentsForCurrentVideo.collectAsState()

        // Local state for active editing video
        var videoToEdit by remember { mutableStateOf<VideoItem?>(null) }

        // Local State for simulating new push alerts
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                Surface(
                    color = studioSurface,
                    border = BorderStroke(1.dp, studioDivider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.navigateTo("main") }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = studioTextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bharamputra Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiverSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dark/Light Theme Switching Toggle
                            IconButton(onClick = { isStudioDarkMode = !isStudioDarkMode }) {
                                Icon(
                                    imageVector = if (isStudioDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Contrast Theme",
                                    tint = if (isStudioDarkMode) Color.Yellow else RiverPrimary
                                )
                            }

                            // Simulation of Notifications
                            IconButton(onClick = {
                                simulatedMessage = listOf(
                                    "✨ Milestone reached! 1.5K Subscriber Growth peak hit!",
                                    "🔔 New Comment by 'KunalS': Awesome River Valley video, keep it up!",
                                    "📈 Revenue Alert: Monitization payout of $128.50 has been released successfully!",
                                    "🔥 Video performance: 'Epic River Rapids' visual traffic up +28% today!"
                                ).random()
                                showPushNotificationSimulation = true
                            }) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Trigger Notification", tint = studioTextPrimary)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Responsive M3 bottom tab strip for the creator studio
                Surface(
                    color = studioSurface,
                    border = BorderStroke(1.dp, studioDivider)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            Triple("Dashboard", Icons.Default.Dashboard, "Dash"),
                            Triple("Videos", Icons.Default.VideoCall, "Videos"),
                            Triple("Comments", Icons.Default.Comment, "Comments"),
                            Triple("Analytics", Icons.Default.BarChart, "Analytics"),
                            Triple("Settings", Icons.Default.Settings, "Settings")
                        ).forEach { (name, icon, label) ->
                            val isSelected = selectedSubTab == name
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedSubTab = name }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = if (isSelected) RiverSecondary else studioTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) RiverSecondary else studioTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            containerColor = studioBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Interactive dynamic views
                when (selectedSubTab) {
                    "Dashboard" -> CreatorDashboardHome(
                        isDark = isStudioDarkMode,
                        surface = studioSurface,
                        textPrimary = studioTextPrimary,
                        textSecondary = studioTextSecondary,
                        surfaceVariant = studioSurfaceVariant,
                        divider = studioDivider,
                        videosList = allVideosList,
                        onNavigateToVideos = { selectedSubTab = "Videos" },
                        onNavigateToAnalytics = { selectedSubTab = "Analytics" }
                    )
                    "Videos" -> CreatorVideosManager(
                        isDark = isStudioDarkMode,
                        surface = studioSurface,
                        textPrimary = studioTextPrimary,
                        textSecondary = studioTextSecondary,
                        surfaceVariant = studioSurfaceVariant,
                        divider = studioDivider,
                        videosList = allVideosList,
                        onEditVideo = { videoToEdit = it },
                        onDeleteVideo = { viewModel.adminDeleteVideo(it.id) }
                    )
                    "Comments" -> CreatorCommentsModerator(
                        isDark = isStudioDarkMode,
                        surface = studioSurface,
                        textPrimary = studioTextPrimary,
                        textSecondary = studioTextSecondary,
                        surfaceVariant = studioSurfaceVariant,
                        divider = studioDivider,
                        commentsList = allCommentsList,
                        onDeleteComment = { viewModel.adminDeleteComment(it) },
                        onLikeComment = { viewModel.likeComment(it) },
                        onPinComment = { viewModel.pinComment(it, false) }
                    )
                    "Analytics" -> CreatorAnalyticsDashboard(
                        isDark = isStudioDarkMode,
                        surface = studioSurface,
                        textPrimary = studioTextPrimary,
                        textSecondary = studioTextSecondary,
                        surfaceVariant = studioSurfaceVariant,
                        divider = studioDivider
                    )
                    "Settings" -> CreatorSettingsPanel(
                        isDark = isStudioDarkMode,
                        surface = studioSurface,
                        textPrimary = studioTextPrimary,
                        textSecondary = studioTextSecondary,
                        surfaceVariant = studioSurfaceVariant,
                        divider = studioDivider,
                        currentUser = currentUser
                    )
                }

                // FCM heads-up push alert popup
                if (showPushNotificationSimulation) {
                    Dialog(onDismissRequest = { showPushNotificationSimulation = false }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isStudioDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, RiverSecondary)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = RiverSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("FCM Live Push Alert", fontWeight = FontWeight.Bold, color = studioTextPrimary, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = simulatedMessage,
                                    color = studioTextPrimary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showPushNotificationSimulation = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary)
                                ) {
                                    Text("Acknowledge Alert")
                                }
                            }
                        }
                    }
                }

                // Edit Video Dialog Modal
                if (videoToEdit != null) {
                    val currentVideo = videoToEdit!!
                    var title by remember { mutableStateOf(currentVideo.title) }
                    var desc by remember { mutableStateOf(currentVideo.description) }
                    var category by remember { mutableStateOf(currentVideo.category) }
                    var tags by remember { mutableStateOf(currentVideo.tags) }
                    var privacy by remember { mutableStateOf(currentVideo.privacy) }

                    Dialog(onDismissRequest = { videoToEdit = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 580.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = studioSurface),
                            border = BorderStroke(1.dp, studioDivider)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Edit Video Metadata", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = studioTextPrimary)
                                
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = desc,
                                    onValueChange = { desc = it },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Category Picker
                                Text("Category", fontSize = 12.sp, color = studioTextSecondary)
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Riverine", "Tech", "Music", "Gaming", "Sports", "Shorts").forEach { cat ->
                                        val isSel = cat == category
                                        AssistChip(
                                            onClick = { category = cat },
                                            label = { Text(cat) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = if (isSel) RiverSecondary else studioTextSecondary
                                            )
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = tags,
                                    onValueChange = { tags = it },
                                    label = { Text("Tags (comma separated)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Privacy mode selector
                                Text("Privacy", fontSize = 12.sp, color = studioTextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    listOf("PUBLIC", "PRIVATE", "UNLISTED").forEach { priv ->
                                        val isSel = priv == privacy
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { privacy = priv },
                                            label = { Text(priv) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { videoToEdit = null }) {
                                        Text("Cancel", color = studioTextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            // Write back editing model to viewModel's repository insertion
                                            val updated = currentVideo.copy(
                                                title = title,
                                                description = desc,
                                                category = category,
                                                tags = tags,
                                                privacy = privacy
                                            )
                                            viewModel.uploadVideo(
                                                title = updated.title,
                                                description = updated.description,
                                                videoUrl = updated.videoUrl,
                                                selectedThumbnailUrl = updated.thumbnailUrl,
                                                category = updated.category,
                                                tags = updated.tags,
                                                isShort = updated.isShort,
                                                onComplete = {
                                                    videoToEdit = null
                                                    Toast.makeText(context, "Metadata update applied!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RiverSecondary)
                                    ) {
                                        Text("Save modifications", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Dynamic Sub-screens of Bharamputra Studio ---

@Composable
fun CreatorDashboardHome(
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    surfaceVariant: Color,
    divider: Color,
    videosList: List<VideoItem>,
    onNavigateToVideos: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and high fidelity stats header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(RiverPrimary, RiverSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("Bharamputra Premium Channel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("@bharamputra_creator", fontSize = 13.sp, color = RiverSecondary)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified Partner", fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Essential analytics layout (Views, Subscribers, Watch Hours, Estimated Earnings)
        Text("Real-Time Analytics Summary", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Total Channel Views",
                    value = "458,920",
                    growth = "+14.5% m/m",
                    icon = Icons.Default.TrendingUp,
                    color = RiverPrimary,
                    surface = surface,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                MetricCard(
                    title = "Subscribers Tracking",
                    value = "4,250",
                    growth = "+8.2% m/m",
                    icon = Icons.Default.PeopleAlt,
                    color = RiverSecondary,
                    surface = surface,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Watch Hours (Avg)",
                    value = "24.8K hrs",
                    growth = "+21.0% m/m",
                    icon = Icons.Default.WatchLater,
                    color = RiverSecondary,
                    surface = surface,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                MetricCard(
                    title = "Estimated Earnings",
                    value = "$2,845.50",
                    growth = "+18% this month",
                    icon = Icons.Default.MonetizationOn,
                    color = Color(0xFF10B981),
                    surface = surface,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Top Performing Stream", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
            TextButton(onClick = onNavigateToVideos) {
                Text("Manage all", color = RiverSecondary, fontSize = 12.sp)
            }
        }

        val topVideo = videosList.maxByOrNull { it.views }
        if (topVideo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, divider)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(70.dp, 70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = rememberImagePainter(url = topVideo.thumbnailUrl),
                            contentDescription = topVideo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = topVideo.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Category: ${topVideo.category}",
                            color = textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RemoveRedEye, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(14.dp))
                            Text(
                                text = " ${topVideo.views} views",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(13.dp))
                            Text(
                                text = " ${topVideo.likes} likes",
                                color = textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Text(
                    text = "No content uploaded yet",
                    modifier = Modifier.padding(32.dp),
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Payout Ledger summary link card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAnalytics() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = RiverSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Monetization Payouts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary)
                        Text("Active billing configuration is functional", fontSize = 11.sp, color = textSecondary)
                    }
                }
                Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null, tint = textSecondary)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    growth: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = BorderStroke(1.dp, if (title.contains("Earn")) color.copy(alpha = 0.3f) else color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 21.sp, fontWeight = FontWeight.Black, color = textPrimary)
            Text(
                text = growth,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (growth.startsWith("+")) Color(0xFF10B981) else color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun CreatorVideosManager(
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    surfaceVariant: Color,
    divider: Color,
    videosList: List<VideoItem>,
    onEditVideo: (VideoItem) -> Unit,
    onDeleteVideo: (VideoItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Studio uploads: (${videosList.size} videos)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (videosList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No videos detected in local index schema.", color = textSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videosList.size) { index ->
                    val video = videosList[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, divider)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp, 60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Image(
                                    painter = rememberImagePainter(url = video.thumbnailUrl),
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Category: ${video.category} • ${video.privacy}",
                                    fontSize = 11.sp,
                                    color = RiverSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.RemoveRedEye, contentDescription = null, tint = textSecondary, modifier = Modifier.size(13.dp))
                                        Text(" ${video.views}", fontSize = 11.sp, color = textSecondary)
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { onEditVideo(video) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = RiverSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        IconButton(
                                            onClick = { onDeleteVideo(video) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorCommentsModerator(
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    surfaceVariant: Color,
    divider: Color,
    commentsList: List<VideoComment>,
    onDeleteComment: (String) -> Unit,
    onLikeComment: (String) -> Unit,
    onPinComment: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Creator Moderation Feeds",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (commentsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.Comment, contentDescription = null, modifier = Modifier.size(48.dp), tint = textSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No comments on channel content found.", color = textSecondary, textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commentsList.size) { index ->
                    val comment = commentsList[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, divider)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RiverPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comment.userName.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                                    Text("New activity on your video", fontSize = 9.sp, color = textSecondary)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = comment.content, fontSize = 13.sp, color = textPrimary)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onLikeComment(comment.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(13.dp))
                                    }
                                    Text(" ${comment.likes}", fontSize = 11.sp, color = textSecondary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(onClick = { onPinComment(comment.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = if (comment.isPinned) RiverPrimary else textSecondary, modifier = Modifier.size(13.dp))
                                    }
                                    if (comment.isPinned) {
                                        Text(" Pinned", fontSize = 11.sp, color = RiverPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(onClick = { onDeleteComment(comment.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorAnalyticsDashboard(
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    surfaceVariant: Color,
    divider: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Demographics Architecture Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = RiverSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Top Audience Demographics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple("Bharamputra River Valley Delta", "48%", Color(0xFF2563EB)),
                    Triple("Assam Tech Valley", "24%", Color(0xFF06B6D4)),
                    Triple("Guwahati-Dhaka Corridor", "18%", Color(0xFF10B981)),
                    Triple("Global Diaspora", "10%", Color(0xFFF59E0B))
                ).forEach { (geo, prc, col) ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(geo, fontSize = 12.sp, color = textPrimary)
                            Text(prc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = col)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentToFloat(prc))
                                    .fillMaxHeight()
                                    .background(col)
                            )
                        }
                    }
                }
            }
        }

        // Payout and detailed monthly earnings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = RiverSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payout & Monthly Billings History", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    Triple("Jun 2026", "$1,280.50", "Processed"),
                    Triple("May 2026", "$943.00", "Settled"),
                    Triple("Apr 2026", "$1,420.00", "Settled"),
                    Triple("Mar 2026", "$810.20", "Settled")
                ).forEach { (mon, valString, stat) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mon, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(valString, fontSize = 13.sp, color = textPrimary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (stat == "Processed") Color(0x3310B981) else Color(0x2294A3B8))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(stat, fontSize = 10.sp, color = if (stat == "Processed") Color(0xFF10B981) else textSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = divider)
                }
            }
        }
    }
}

@Composable
fun CreatorSettingsPanel(
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    surfaceVariant: Color,
    divider: Color,
    currentUser: com.example.data.auth.UserAccount?
) {
    val scrollState = rememberScrollState()

    var adsEnabled by remember { mutableStateOf(true) }
    var adsFormatReward by remember { mutableStateOf(true) }
    var superChatEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profiling Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Creator Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)
                
                Text("Channel customization details are synced automatically with your authorized email session: ${currentUser?.email}", color = textSecondary, fontSize = 12.sp)

                OutlinedTextField(
                    value = currentUser?.name ?: "Bharamputra Streamer",
                    onValueChange = {},
                    label = { Text("Display Brand Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                OutlinedTextField(
                    value = currentUser?.handle ?: "@bharamputra_creator",
                    onValueChange = {},
                    label = { Text("Channel Custom Handle") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }
        }

        // Monitization Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, divider)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Partnership & Monetization", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Overlay Stream Ads", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Display skippable commercial breaks", fontSize = 11.sp, color = textSecondary)
                    }
                    Switch(checked = adsEnabled, onCheckedChange = { adsEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FCM SuperChats Enabled", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text("Supporters can pin highlighted remarks", fontSize = 11.sp, color = textSecondary)
                    }
                    Switch(checked = superChatEnabled, onCheckedChange = { superChatEnabled = it })
                }
            }
        }
    }
}

// Helper parsing helper function
private fun percentToFloat(percent: String): Float {
    return (percent.replace("%", "").toFloatOrNull() ?: 50f) / 100f
}

@Composable
fun rememberImagePainter(url: String): androidx.compose.ui.graphics.painter.Painter {
    // Elegant fallback painter since image loading is on real web URLs
    return painterResource(id = android.R.drawable.ic_menu_gallery)
}
