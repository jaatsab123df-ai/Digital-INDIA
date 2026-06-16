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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.data.models.VideoItem
import com.example.data.models.CreatorChannel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RiverDarkBackground
import com.example.ui.theme.RiverDarkSurface
import com.example.ui.theme.RiverDarkSurfaceVariant
import com.example.ui.theme.RiverPrimary
import com.example.ui.theme.RiverSecondary
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.random.Random

// --- Barak Premium Neon Palette ---
val BarakEmerald = Color(0xFF10B981)      // Neon Green Accent
val BarakDarkSlate = Color(0xFF0F172A)    // Dark Indigo Slate Card
val BarakDeepBlack = Color(0xFF020617)    // Luxury Midnight Base
val BarakPurpleSpark = Color(0xFF8B5CF6)  // Vibrant Secondary Purple Gradient
val BarakGoldMuted = Color(0xFFF59E0B)    // Moderation and Verification Warn

// --- Barak Independent Data Layer Models ---
data class BarakProfile(
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false
) : Serializable

data class BarakPost(
    val id: String,
    val authorUsername: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String,
    val imageUrl: String,
    val caption: String,
    val likesCount: Int,
    val commentsCount: Int,
    val relativeTime: String,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val authorIsVerified: Boolean = false
) : Serializable

data class BarakStory(
    val id: String,
    val username: String,
    val avatarUrl: String,
    val mediaUrl: String,
    val caption: String,
    val isWatched: Boolean = false
) : Serializable

data class BarakReel(
    val id: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatarUrl: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val comments: List<String>,
    val shares: Int,
    val likes: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val publishedToBharamputra: Boolean = false
) : Serializable

data class DirectMessage(
    val senderUsername: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val mediaUrl: String? = null
) : Serializable

data class DMThread(
    val threadId: String,
    val partnerUsername: String,
    val partnerDisplayName: String,
    val partnerAvatarUrl: String,
    val messages: List<DirectMessage>,
    val isGroupChat: Boolean = false,
    val isPendingRequest: Boolean = false
) : Serializable

data class BarakNotification(
    val id: String,
    val type: String, // LIKE, COMMENT, FOLLOW, MENTION, MESSAGE
    val triggeringUser: String,
    val triggeringAvatar: String,
    val description: String,
    val timestamp: Long
) : Serializable

class BarakActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access shared platform ViewModel to synchronize shared accounts and Optional Reels-to-Shorts Database integration
        val viewModel = ViewModelProvider(this).get(BharamputraViewModel::class.java)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BarakDeepBlack
                ) {
                    BarakApplicationRoot(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BarakApplicationRoot(viewModel: BharamputraViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- Barak Navigation State ---
    var barakScreen by remember { mutableStateOf("splash") }
    var activeTab by remember { mutableStateOf("home") } // home, explore, reels, dms, profile

    // --- Independent Social Data State ---
    val stories = remember {
        mutableStateListOf(
            BarakStory("str_1", "assam_vibes", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=720", "Golden morning river breeze"),
            BarakStory("str_2", "northeast_explorer", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=150", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=720", "Hidden waterfalls in Cherrapunji"),
            BarakStory("str_3", "vedic_coder", "https://images.unsplash.com/photo-1614149162883-504ce4d13909?q=80&w=150", "https://images.unsplash.com/photo-1542831371-29b0f74f9713?q=80&w=720", "Late night debugging loops"),
            BarakStory("str_4", "guwahati_tales", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=150", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=720", "Sunset on the hills")
        )
    }

    val posts = remember {
        mutableStateListOf(
            BarakPost("post_1", "assam_vibes", "Assam Tourism Alliance", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=720", "Chasing golden sunrises across the mighty Brahmaputra river valleys. Absolute luxury in natural design! #barak_valley #northeast #serenity", 1290, 84, "2 hours ago", authorIsVerified = true),
            BarakPost("post_2", "northeast_explorer", "NE Adventures", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=150", "https://images.unsplash.com/photo-1565120130276-dfbd9a7a3ad7?q=80&w=720", "Exploring the bamboo trails. The sound of local flutes echoing through the majestic forest is pure art. Mentioned our friend @assam_vibes which helped plan this itinerary!", 845, 41, "5 hours ago"),
            BarakPost("post_3", "vedic_coder", "Vedic Tech Hub", "https://images.unsplash.com/photo-1614149162883-504ce4d13909?q=80&w=150", "https://images.unsplash.com/photo-1555066931-4365d14bab8c?q=80&w=720", "Re-launching our main platform architecture. Optimized local caching with Jetpack Compose rendering widgets. Speed limit = Infinity! #android_kotlin #performance", 2045, 112, "1 day ago", authorIsVerified = true)
        )
    }

    val reels = remember {
        mutableStateListOf(
            BarakReel("reel_1", "music_beats", "NE Rhythm Beats", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", "Bihu Electronic Fusion Loop", "Vibe to the ancient traditional drums synthesized under heavy electronic sub-base. Let us know if you want the full audio conversion on Barak Music too!", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=720", listOf("This loop goes heavy!", "Bihu beats with subwoofers is magic!"), 1402, 9403, publishedToBharamputra = true),
            BarakReel("reel_2", "guwahati_tales", "Rohan Dasgupta", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=150", "Guwahati Evening Sky Walk", "Sunset walk across the mountain trail overlooks the riverbanks, absolutely mind-calming. #mountains #travel #calm", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=720", listOf("Northeast nature is undefeated.", "Need to travel here next summer!"), 940, 6802),
            BarakReel("reel_3", "vedic_coder", "Vedic Tech Hub", "https://images.unsplash.com/photo-1614149162883-504ce4d13909?q=80&w=150", "Cyber Matrix Neon Code", "Writing clean Kotlin widgets for Barak and synchronizing layouts across multi-screen targets. #barak #kotlin", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", "https://images.unsplash.com/photo-1614149162883-504ce4d13909?q=80&w=720", listOf("Super responsive UI blocks!", "Waiting for the deployment guide!"), 2310, 11409)
        )
    }

    val dmThreads = remember {
        mutableStateListOf(
            DMThread("th_1", "assam_vibes", "Assam Tourism Alliance", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", listOf(
                DirectMessage("assam_vibes", "Hello! Welcome to Barak social network platform.", System.currentTimeMillis() - 3600000 * 2, isRead = true),
                DirectMessage("me", "Thanks! Love the shared authentication experience across Bharamputra.", System.currentTimeMillis() - 3600000, isRead = true),
                DirectMessage("assam_vibes", "Are you planning to check the automatic Reels to Shorts publishing sync?", System.currentTimeMillis() - 60000, isRead = false)
            )),
            DMThread("th_2", "northeast_explorer", "NE Adventures", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=150", listOf(
                DirectMessage("northeast_explorer", "Hey buddy, sent a request to share the latest travel vlog draft.", System.currentTimeMillis() - 86400000 * 2, isRead = true),
                DirectMessage("me", "Sure, send me the link, I will verify it.", System.currentTimeMillis() - 86400000, isRead = true)
            )),
            DMThread("th_3", "stranger_wave", "Unknown Voyager", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=150", listOf(
                DirectMessage("stranger_wave", "Hello there, are we connected?", System.currentTimeMillis() - 86400000 * 3, isRead = false)
            ), isPendingRequest = true)
        )
    }

    val notificationAlerts = remember {
        mutableStateListOf(
            BarakNotification("notf_1", "COMMENT", "assam_vibes", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", "commented: 'Impressive code! Let us collaborate on more designs!'", System.currentTimeMillis() - 10 * 60000),
            BarakNotification("notf_2", "LIKE", "northeast_explorer", "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=150", "liked your profile introduction.", System.currentTimeMillis() - 45 * 60000),
            BarakNotification("notf_3", "FOLLOW", "guwahati_tales", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=150", "started following you.", System.currentTimeMillis() - 2 * 3600000),
            BarakNotification("notf_4", "MENTION", "assam_vibes", "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=150", "mentioned you in a post: '@explore_user checkout the design.'", System.currentTimeMillis() - 4 * 3600000)
        )
    }

    val blockedUsers = remember { mutableStateListOf<String>() }
    val safetyReports = remember { mutableStateListOf<String>() }

    // --- Independent Client-side User Barak Profile ---
    var barakMyProfile by remember {
        mutableStateOf(
            BarakProfile(
                username = "explorer_barak",
                displayName = currentUser?.name ?: "Bharamputra Explorer",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150",
                bio = "Connecting riverine cultures across India. Passionate vlogger, traveler, and designer sharing authentic moments.",
                followersCount = 4820,
                followingCount = 312,
                isVerified = true,
                isPrivate = false
            )
        )
    }

    // Sync profile display name when native shared signup registers
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val simpleUser = currentUser!!.name.lowercase().replace(" ", "_")
            barakMyProfile = barakMyProfile.copy(
                username = "${simpleUser}_barak",
                displayName = currentUser!!.name
            )
        }
    }

    // --- Active stories viewer state ---
    var activeStoryViewList by remember { mutableStateOf<List<BarakStory>?>(null) }
    var activeStoryIndex by remember { mutableStateOf(0) }

    // --- Active comments view sheet ---
    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }

    // Navigation transit block
    AnimatedContent(
        targetState = barakScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "barakRootTransition"
    ) { currentScreen ->
        when (currentScreen) {
            "splash" -> {
                BarakSpaciousSplash {
                    barakScreen = if (currentUser != null) "home_dashboard" else "login_gate"
                }
            }
            "login_gate" -> {
                BarakLoginGateScreen(
                    viewModel = viewModel,
                    onSuccess = {
                        barakScreen = "home_dashboard"
                    }
                )
            }
            "home_dashboard" -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.Black.copy(alpha = 0.95f),
                            tonalElevation = 10.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = activeTab == "home",
                                onClick = { activeTab = "home" },
                                icon = { Icon(imageVector = if (activeTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                                label = { Text("Feed") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = BarakEmerald,
                                    indicatorColor = BarakEmerald,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == "explore",
                                onClick = { activeTab = "explore" },
                                icon = { Icon(imageVector = if (activeTab == "explore") Icons.Filled.Explore else Icons.Outlined.Explore, contentDescription = "Explore") },
                                label = { Text("Explore") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = BarakEmerald,
                                    indicatorColor = BarakEmerald,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == "reels",
                                onClick = { activeTab = "reels" },
                                icon = { Icon(imageVector = if (activeTab == "reels") Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary, contentDescription = "Reels") },
                                label = { Text("Reels") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = BarakEmerald,
                                    indicatorColor = BarakEmerald,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == "dms",
                                onClick = { activeTab = "dms" },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            val unread = dmThreads.count { t -> t.messages.any { !it.isRead && it.senderUsername != "me" } }
                                            if (unread > 0) {
                                                Badge(containerColor = BarakEmerald) { Text(unread.toString(), color = Color.Black) }
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = if (activeTab == "dms") Icons.Filled.Mail else Icons.Outlined.Mail, contentDescription = "DMs")
                                    }
                                },
                                label = { Text("Chats") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = BarakEmerald,
                                    indicatorColor = BarakEmerald,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                selected = activeTab == "profile",
                                onClick = { activeTab = "profile" },
                                icon = { Icon(imageVector = if (activeTab == "profile") Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                                label = { Text("You") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = BarakEmerald,
                                    indicatorColor = BarakEmerald,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    },
                    containerColor = BarakDeepBlack
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabContentTransit"
                        ) { tab ->
                            when (tab) {
                                "home" -> {
                                    BarakHomeFeedTab(
                                        profile = barakMyProfile,
                                        stories = stories,
                                        posts = posts,
                                        blockedUsers = blockedUsers,
                                        onStoryClick = { story ->
                                            activeStoryViewList = stories
                                            activeStoryIndex = stories.indexOf(story).coerceAtLeast(0)
                                        },
                                        onToggleLike = { id ->
                                            val idx = posts.indexOfFirst { it.id == id }
                                            if (idx != -1) {
                                                val post = posts[idx]
                                                posts[idx] = post.copy(
                                                    isLiked = !post.isLiked,
                                                    likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
                                                )
                                            }
                                        },
                                        onToggleSave = { id ->
                                            val idx = posts.indexOfFirst { it.id == id }
                                            if (idx != -1) {
                                                val post = posts[idx]
                                                posts[idx] = post.copy(isSaved = !post.isSaved)
                                            }
                                        },
                                        onOpenComments = { postId ->
                                            activeCommentsPostId = postId
                                        },
                                        onReport = { user ->
                                            safetyReports.add(user)
                                            Toast.makeText(context, "Post flagged for professional moderation review.", Toast.LENGTH_SHORT).show()
                                        },
                                        onBlock = { user ->
                                            blockedUsers.add(user)
                                            Toast.makeText(context, "Blocked $user from interacting.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                "explore" -> {
                                    BarakExploreTab(
                                        posts = posts,
                                        reels = reels,
                                        onPlayReel = { reel ->
                                            activeTab = "reels"
                                        },
                                        onPostClick = { post ->
                                            activeTab = "home"
                                        }
                                    )
                                }
                                "reels" -> {
                                    BarakReelsTab(
                                        viewModel = viewModel,
                                        reels = reels,
                                        onCreateReel = { title, desc, url, publishShorts ->
                                            val id = "reel_${System.currentTimeMillis()}"
                                            val newReel = BarakReel(
                                                id = id,
                                                creatorUsername = barakMyProfile.username,
                                                creatorDisplayName = barakMyProfile.displayName,
                                                creatorAvatarUrl = barakMyProfile.avatarUrl,
                                                title = title,
                                                description = desc,
                                                videoUrl = url.ifEmpty { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" },
                                                thumbnailUrl = "https://images.unsplash.com/photo-1548504769-900b70ed122e?q=80&w=720",
                                                comments = emptyList(),
                                                shares = 0,
                                                likes = 1,
                                                isLiked = true,
                                                publishedToBharamputra = publishShorts
                                            )
                                            reels.add(0, newReel)

                                            // Push notifications alerting simulation
                                            val alert = BarakNotification(
                                                id = "notf_${System.currentTimeMillis()}",
                                                type = "MENTION",
                                                triggeringUser = "Barak Engine",
                                                triggeringAvatar = "cog",
                                                description = "successfully synchronized and published reel: '$title'",
                                                timestamp = System.currentTimeMillis()
                                            )
                                            notificationAlerts.add(0, alert)
                                        },
                                        onDeleteReel = { reelId ->
                                            reels.removeAll { it.id == reelId }
                                            Toast.makeText(context, "Reel deleted. Synchronized removal complete.", Toast.LENGTH_SHORT).show()
                                        },
                                        blockedUsers = blockedUsers
                                    )
                                }
                                "dms" -> {
                                    BarakDMsTab(
                                        threads = dmThreads,
                                        onSendMessage = { threadId, text ->
                                            val threadIdx = dmThreads.indexOfFirst { it.threadId == threadId }
                                            if (threadIdx != -1) {
                                                val thread = dmThreads[threadIdx]
                                                val newMsgs = thread.messages + DirectMessage("me", text, System.currentTimeMillis(), isRead = true)
                                                dmThreads[threadIdx] = thread.copy(messages = newMsgs)

                                                // Delay mock partner reply
                                                scope.launch {
                                                    delay(2500)
                                                    val replyMsg = DirectMessage(
                                                        senderUsername = thread.partnerUsername,
                                                        content = "Received! Barak secure gateway operates perfectly.",
                                                        timestamp = System.currentTimeMillis(),
                                                        isRead = false
                                                    )
                                                    val updatedMsgs = dmThreads[threadIdx].messages + replyMsg
                                                    dmThreads[threadIdx] = dmThreads[threadIdx].copy(messages = updatedMsgs)

                                                    // Trigger simulated alert
                                                    notificationAlerts.add(0, BarakNotification(
                                                        id = "notf_${System.currentTimeMillis()}",
                                                        type = "MESSAGE",
                                                        triggeringUser = thread.partnerUsername,
                                                        triggeringAvatar = thread.partnerAvatarUrl,
                                                        description = "sent you a direct message: 'Received!'",
                                                        timestamp = System.currentTimeMillis()
                                                    ))
                                                }
                                            }
                                        },
                                        onAcceptRequest = { threadId ->
                                            val tIdx = dmThreads.indexOfFirst { it.threadId == threadId }
                                            if (tIdx != -1) {
                                                dmThreads[tIdx] = dmThreads[tIdx].copy(isPendingRequest = false)
                                                Toast.makeText(context, "Accepted message request!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                                "profile" -> {
                                    BarakProfileTab(
                                        profile = barakMyProfile,
                                        personalPosts = posts.filter { it.authorUsername == barakMyProfile.username },
                                        personalReels = reels.filter { it.creatorUsername == barakMyProfile.username },
                                        onUpdateProfile = { updated ->
                                            barakMyProfile = updated
                                            Toast.makeText(context, "Barak Profile Updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        alerts = notificationAlerts,
                                        blockedList = blockedUsers,
                                        onUnblock = { user ->
                                            blockedUsers.remove(user)
                                            Toast.makeText(context, "Unblocked $user.", Toast.LENGTH_SHORT).show()
                                        },
                                        safetyReports = safetyReports,
                                        onClearReports = {
                                            safetyReports.clear()
                                        },
                                        onSignOut = {
                                            viewModel.logout()
                                            barakScreen = "login_gate"
                                        }
                                    )
                                }
                            }
                        }

                        // Passive Story Overlay Viewer
                        if (activeStoryViewList != null) {
                            BarakStoryViewer(
                                stories = activeStoryViewList!!,
                                startIndex = activeStoryIndex,
                                onDismiss = { activeStoryViewList = null }
                            )
                        }

                        // Passive Comment Bottom Sheet Drawer Simulation
                        if (activeCommentsPostId != null) {
                            val activePost = posts.find { it.id == activeCommentsPostId }
                            if (activePost != null) {
                                BarakCommentsDrawer(
                                    post = activePost,
                                    onDismiss = { activeCommentsPostId = null },
                                    onAddComment = { count ->
                                        val idx = posts.indexOfFirst { it.id == activePost.id }
                                        if (idx != -1) {
                                            posts[idx] = posts[idx].copy(commentsCount = count)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= SPLASH INSTANT FLUID INTRO =================
@Composable
fun BarakSpaciousSplash(onLoaded: () -> Unit) {
    var animateStart by remember { mutableStateOf(false) }

    val logoPulse by animateFloatAsState(
        targetValue = if (animateStart) 1.1f else 0.8f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )

    LaunchedEffect(Unit) {
        animateStart = true
        delay(2300)
        onLoaded()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1E36),
                        BarakDeepBlack
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoPulse)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(listOf(BarakEmerald, BarakPurpleSpark)))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(BarakDeepBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Barak Logo",
                        tint = BarakEmerald,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "BARAK",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 8.sp,
                modifier = Modifier.testTag("barak_splash_title")
            )

            Text(
                text = "independent luxury micro-social platform",
                color = BarakEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = BarakEmerald,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ================= SECURE SHARED AUTH GATE =================
@Composable
fun BarakLoginGateScreen(viewModel: BharamputraViewModel, onSuccess: () -> Unit) {
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Authentication Mode: "phone", "email" (Primary Phone authentication is focus)
    var authMode by remember { mutableStateOf("phone") }

    // --- Phone Flow State ---
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(0) }
    var isAutocompleting by remember { mutableStateOf(false) }
    var autoDetectActive by remember { mutableStateOf(false) }
    
    // --- SignUp State for New Phone ---
    var queryCheckedPhone by remember { mutableStateOf<String?>(null) }
    var isPhoneNewAccount by remember { mutableStateOf(false) }
    var newPhoneDisplayName by remember { mutableStateOf("") }
    var newPhoneEmail by remember { mutableStateOf("") }

    // --- Email Flow State ---
    var hasEmailAccount by remember { mutableStateOf(true) }
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // --- Pre-linked Mock Accounts Database mapping Phone -> Name, Email, Handle ---
    val linkedAccounts = remember {
        mapOf(
            "+919876543210" to Triple("Bharamputra Explorer", "explorer@bharamputra.com", "@explorer_barak"),
            "+911234567890" to Triple("Assam Tourism Alliance", "assam_vibes@bharamputra.com", "@assam_vibes"),
            "+919999999999" to Triple("Vedic Tech Hub", "vedic_coder@bharamputra.com", "@vedic_coder")
        )
    }

    // Countdown Timer Coroutine
    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        }
    }

    // Auth redirection
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onSuccess()
        }
    }

    // Automatic OTP detection simulation when Code is Sent
    LaunchedEffect(isCodeSent) {
        if (isCodeSent) {
            autoDetectActive = true
            isAutocompleting = false
            // Wait 3 seconds, then simulate the SMS retrieval activity
            delay(2500)
            if (autoDetectActive && verificationCode.isEmpty()) {
                isAutocompleting = true
                Toast.makeText(context, "SmsRetriever: Direct OTP SMS message detected!", Toast.LENGTH_SHORT).show()
                delay(1200)
                verificationCode = "420108"
                isAutocompleting = false
                Toast.makeText(context, "Code autocompleted successfully.", Toast.LENGTH_SHORT).show()
                
                // Automatically verify the code
                handlePhoneVerification(
                    phone = phoneNumber,
                    code = "420108",
                    linkedAccounts = linkedAccounts,
                    viewModel = viewModel,
                    context = context,
                    onNewAccountPrompt = {
                        isPhoneNewAccount = true
                        queryCheckedPhone = phoneNumber
                    },
                    onSuccess = onSuccess
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BarakDeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // High-end Brand Header
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(BarakEmerald, BarakPurpleSpark)))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(BarakDeepBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Barak Secure Auth Gateway",
                        tint = BarakEmerald,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BARAK SECURE AUTHSYNC",
                color = BarakEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )

            Text(
                text = "Unified Identity Ecosystem",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Secure phone number authentication integrated with Firebase services & the Bharamputra network.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth type Selectors: Phone (Primary) vs. Alternative (Email)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BarakDarkSlate)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { authMode = "phone" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (authMode == "phone") BarakEmerald else Color.Transparent,
                        contentColor = if (authMode == "phone") Color.Black else Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Phone OTP / Sign-In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { authMode = "email" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (authMode == "email") BarakEmerald else Color.Transparent,
                        contentColor = if (authMode == "email") Color.Black else Color.Gray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authError!!,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- NAVIGATION MAIN RENDER FROM AUTH MODE ---
            when (authMode) {
                "phone" -> {
                    if (isPhoneNewAccount) {
                        // Registration View for New Verified Phone Number
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BarakDarkSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = BarakEmerald, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Finish Account Sign-Up",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Phone number $queryCheckedPhone verified! Associate your new creator profile below.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = newPhoneDisplayName,
                                    onValueChange = { newPhoneDisplayName = it },
                                    label = { Text("Display Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BarakEmerald,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = BarakEmerald,
                                        unfocusedLabelColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = newPhoneEmail,
                                    onValueChange = { newPhoneEmail = it },
                                    label = { Text("Email Sync (Optional)") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BarakEmerald,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = BarakEmerald,
                                        unfocusedLabelColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        if (newPhoneDisplayName.isEmpty()) {
                                            Toast.makeText(context, "Please write display name.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val email = newPhoneEmail.ifEmpty { "${newPhoneDisplayName.lowercase().replace(" ", "")}@bharamputra.com" }
                                        viewModel.registerWithEmail(email, newPhoneDisplayName, queryCheckedPhone ?: "")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("ESTABLISH BARAK SESSIONS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(onClick = { isPhoneNewAccount = false; isCodeSent = false }) {
                                    Text("Back", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    } else if (!isCodeSent) {
                        // Phone Entering State
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BarakDarkSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "MOBILE PHONE ENTRY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BarakEmerald,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter your primary mobile number. If the number is linked with a Bharamputra account, we will log you in automatically.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Country code prefix block + mobile input
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BarakDeepBlack)
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
                                            focusedBorderColor = BarakEmerald,
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

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Demo numbers: +919876543210 (Explorer), +919999999999 (Vedic Coder)",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(22.dp))

                                Button(
                                    onClick = {
                                        val digitsOnly = phoneNumber.filter { it.isDigit() }
                                        if (digitsOnly.length < 10) {
                                            Toast.makeText(context, "Invalid phone number. Provide minimum 10 digits.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        
                                        // Send verification OTP code simulate
                                        scope.launch {
                                            isCodeSent = true
                                            countdownSeconds = 30
                                            Toast.makeText(context, "Firebase SMS Service: Transmitting verification code...", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("barak_send_otp_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Outbox, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SEND FIREBASE SMS OTP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Phone Verifying State (OTP sent)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BarakDarkSlate),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VERIFY MOBILE OTP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BarakEmerald,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "+91 $phoneNumber",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SMS transmitted successfully. Manual fallback simulation code is 420108.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
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
                                        focusedBorderColor = BarakEmerald,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = BarakEmerald,
                                        unfocusedLabelColor = Color.Gray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("barak_otp_code_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = Color.Gray) }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Automatic OTP detection panel
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BarakDeepBlack.copy(alpha = 0.6f)),
                                    border = BorderStroke(1.dp, if (autoDetectActive) BarakEmerald.copy(alpha = 0.5f) else Color.DarkGray),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Hearing,
                                                    contentDescription = null,
                                                    tint = if (autoDetectActive) BarakEmerald else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isAutocompleting) "Retrieving Secure SMS..." else "SmsRetriever Listener Active",
                                                    color = if (autoDetectActive) Color.White else Color.Gray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            if (autoDetectActive) {
                                                CircularProgressIndicator(
                                                    color = BarakEmerald,
                                                    strokeWidth = 1.5.dp,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        
                                        if (isAutocompleting) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                color = BarakEmerald,
                                                trackColor = Color.DarkGray,
                                                modifier = Modifier.fillMaxWidth().height(2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        autoDetectActive = false // dismiss listener
                                        handlePhoneVerification(
                                            phone = phoneNumber,
                                            code = verificationCode,
                                            linkedAccounts = linkedAccounts,
                                            viewModel = viewModel,
                                            context = context,
                                            onNewAccountPrompt = {
                                                isPhoneNewAccount = true
                                                queryCheckedPhone = phoneNumber
                                            },
                                            onSuccess = onSuccess
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("barak_phone_verify_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = verificationCode.length >= 4
                                ) {
                                    Text("CONFIRM VERIFICATION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Resend option with timer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            isCodeSent = false
                                            verificationCode = ""
                                            autoDetectActive = false
                                        }
                                    ) {
                                        Text("Change Phone", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    if (countdownSeconds > 0) {
                                        Text(
                                            text = "Resend OTP in ${countdownSeconds}s",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        TextButton(
                                            onClick = {
                                                countdownSeconds = 30
                                                verificationCode = ""
                                                Toast.makeText(context, "Resent verification code via Firebase SMS system.", Toast.LENGTH_SHORT).show()
                                                
                                                // Trigger auto detect again
                                                autoDetectActive = true
                                            }
                                        ) {
                                            Text("Resend OTP SMS", color = BarakEmerald, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "email" -> {
                    // Alternative Email Password Form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BarakDarkSlate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (hasEmailAccount) "LOG IN WITH BHARAMPUTRA ACCOUNT" else "Sync New Email Channel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BarakEmerald,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (!hasEmailAccount) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Display Name") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BarakEmerald,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = BarakEmerald,
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
                                    focusedBorderColor = BarakEmerald,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedLabelColor = BarakEmerald,
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
                                    focusedBorderColor = BarakEmerald,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedLabelColor = BarakEmerald,
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
                                        Toast.makeText(context, "Please write valid email.", Toast.LENGTH_SHORT).show()
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
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isAuthLoading
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (hasEmailAccount) "VERIFY & SIGN IN" else "CREATE AND SYNC",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            TextButton(
                                onClick = { hasEmailAccount = !hasEmailAccount },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = if (hasEmailAccount) "Need a new creator registration profile? Register" else "Sync existing stream profile? Sign In",
                                    color = BarakEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Google sign-in visual option (requirement spec Google Sign-In as optional)
            OutlinedButton(
                onClick = {
                    // Google flow simulate
                    viewModel.registerWithEmail("google_creator@gmail.com", "Google Streamer", "+919911991199")
                    Toast.makeText(context, "Google Play Identity Verified & Linked!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("gmail_quick_login"),
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BarakEmerald, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sync with Google Play ID Services", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

// Helper to handle OTP parsing and linked identity matching
private fun handlePhoneVerification(
    phone: String,
    code: String,
    linkedAccounts: Map<String, Triple<String, String, String>>,
    viewModel: BharamputraViewModel,
    context: Context,
    onNewAccountPrompt: () -> Unit,
    onSuccess: () -> Unit
) {
    if (code != "420108") {
        Toast.makeText(context, "Invalid Verification Code. Handshake failed.", Toast.LENGTH_SHORT).show()
        return
    }

    // Check pre-linked databases first
    val fullPhone = if (phone.startsWith("+91")) phone else "+91$phone"
    val match = linkedAccounts[fullPhone]
    
    if (match != null) {
        val (name, email, handle) = match
        Toast.makeText(context, "Found linked Bharamputra account! Automatically syncs in.", Toast.LENGTH_LONG).show()
        viewModel.registerWithEmail(email, name, fullPhone)
    } else {
        // Double check if currently logged-in details on Bharamputra can link
        val current = viewModel.currentUser.value
        if (current != null && current.phoneNumber == fullPhone) {
            Toast.makeText(context, "Linked with active session stream account in ecosystem.", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            // New user phone sign up needed
            onNewAccountPrompt()
        }
    }
}

// ================= TAB 1: HOME FEED TAB =================
@Composable
fun BarakHomeFeedTab(
    profile: BarakProfile,
    stories: List<BarakStory>,
    posts: List<BarakPost>,
    blockedUsers: List<String>,
    onStoryClick: (BarakStory) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onReport: (String) -> Unit,
    onBlock: (String) -> Unit
) {
    val context = LocalContext.current
    val nonBlockedPosts = remember(posts, blockedUsers) {
        posts.filter { !blockedUsers.contains(it.authorUsername) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top luxury header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "B A R A K",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )

                IconButton(
                    onClick = {
                        Toast.makeText(context, "Real-time feeds refreshed.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = BarakEmerald)
                }
            }
        }

        // Stories feed
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "STORY CHANNELS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(stories) { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onStoryClick(story) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(BarakEmerald, BarakPurpleSpark, BarakEmerald)
                                        )
                                    )
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = story.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.dp, Color.Black, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "@${story.username}",
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(76.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Post separators / divider
        item {
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
        }

        // Home Post objects
        if (nonBlockedPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Stream, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active social posts in feed. Explore other creators!", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(nonBlockedPosts) { post ->
                BarakPostCard(
                    post = post,
                    onToggleLike = { onToggleLike(post.id) },
                    onToggleSave = { onToggleSave(post.id) },
                    onOpenComments = { onOpenComments(post.id) },
                    onReport = { onReport(post.authorUsername) },
                    onBlock = { onBlock(post.authorUsername) }
                )
            }
        }
    }
}

@Composable
fun BarakPostCard(
    post: BarakPost,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenComments: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 12.dp)
    ) {
        // Author details row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorDisplayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (post.authorIsVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BarakEmerald, modifier = Modifier.size(15.dp))
                        }
                    }
                    Text(
                        text = "@${post.authorUsername} • ${post.relativeTime}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Post options", tint = Color.LightGray)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(BarakDarkSlate)
                ) {
                    DropdownMenuItem(
                        text = { Text("Mute / Report Content", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = BarakGoldMuted) },
                        onClick = {
                            showMenu = false
                            onReport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Block Creator Account", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onBlock()
                        }
                    )
                }
            }
        }

        // Post Media Image
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable { onToggleLike() },
            contentScale = ContentScale.Crop
        )

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleLike) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color.Red else Color.White
                    )
                }
                Text(
                    text = "${post.likesCount}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                IconButton(onClick = onOpenComments) {
                    Icon(
                        imageVector = Icons.Outlined.Comment,
                        contentDescription = "Comment",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${post.commentsCount}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                IconButton(onClick = {
                    // Simulated instant copy link share
                    onOpenComments()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }

            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSaved) BarakEmerald else Color.White
                )
            }
        }

        // Caption body and Hashtags
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = post.caption,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Secure Sandbox Sync: Guaranteed Encrypted Feed",
                color = BarakEmerald.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ================= TAB 2: EXPLORE DISCOVER TAB =================
@Composable
fun BarakExploreTab(
    posts: List<BarakPost>,
    reels: List<BarakReel>,
    onPlayReel: (BarakReel) -> Unit,
    onPostClick: (BarakPost) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val featuredTrends = listOf("#barak_culture", "#brahmaputra_river", "#bamboo_acoustic", "#northeast_rythyms", "#kotlin_compose")

    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isEmpty()) posts else {
            posts.filter {
                it.caption.contains(searchQuery, ignoreCase = true) ||
                it.authorDisplayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Discover artists, hashtags, and stories...", color = Color.Gray, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BarakEmerald,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = BarakDarkSlate,
                        unfocusedContainerColor = BarakDarkSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                )
            }
        },
        containerColor = BarakDeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hot hashtags horizontal Row
            Column {
                Text(
                    text = "HOT HASHTAG TRENDS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(featuredTrends) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(BarakDarkSlate)
                                .clickable { searchQuery = tag }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Tag, contentDescription = null, tint = BarakEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = tag.removePrefix("#"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Grid content
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "TRENDING REELS & POSTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 2 column pseudo grid
                val pairs = filteredPosts.chunked(2)
                pairs.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPostClick(item) }
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Overlay detailing views or play button
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                            )
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Text(
                                            text = "@${item.authorUsername}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "${item.likesCount}", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                        if (rowItems.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 3: REELS / SHORTS INTEGRATION TAB =================
@Composable
fun BarakReelsTab(
    viewModel: BharamputraViewModel,
    reels: List<BarakReel>,
    onCreateReel: (String, String, String, Boolean) -> Unit,
    onDeleteReel: (String) -> Unit,
    blockedUsers: List<String>
) {
    val context = LocalContext.current
    var activeReelIndex by remember { mutableStateOf(0) }
    var showCreateReelSheet by remember { mutableStateOf(false) }

    val activeReels = remember(reels, blockedUsers) {
        reels.filter { !blockedUsers.contains(it.creatorUsername) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (activeReels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active reels. Click + to compose your first reel!", color = Color.White)
            }
        } else {
            // Capture selected reel
            val currentReel = activeReels[activeReelIndex.coerceIn(0, activeReels.size - 1)]

            // Reel Swipe visual container
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive background image representing looping video
                AsyncImage(
                    model = currentReel.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High contrast dark shade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                // Simulated Play progress loop bar
                var progressSim by remember { mutableStateOf(0.4f) }
                LaunchedEffect(activeReelIndex) {
                    progressSim = 0.0f
                    while (true) {
                        delay(120)
                        progressSim += 0.01f
                        if (progressSim >= 1.0f) progressSim = 0.0f
                    }
                }

                LinearProgressIndicator(
                    progress = progressSim,
                    color = BarakEmerald,
                    trackColor = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(3.dp)
                )

                // Vertical scroll controls helper (up/down indicators for simulation overlay)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (activeReelIndex > 0) activeReelIndex--
                        },
                        enabled = activeReelIndex > 0,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Prev", tint = Color.White)
                    }

                    Text(
                        text = "${activeReelIndex + 1}/${activeReels.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            if (activeReelIndex < activeReels.size - 1) activeReelIndex++
                        },
                        enabled = activeReelIndex < activeReels.size - 1,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Next", tint = Color.White)
                    }
                }

                // Interactive sidebar controls (right edge)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Creator Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, BarakEmerald, CircleShape)
                    ) {
                        AsyncImage(
                            model = currentReel.creatorAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Like
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Reel Liked!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Like", tint = Color.Red, modifier = Modifier.size(32.dp))
                        }
                        Text(text = "${currentReel.likes}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Comment
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Comment feed overlay triggered.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Outlined.Comment, contentDescription = "Comments", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Text(text = "${currentReel.comments.size}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Copied secure reel share link!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Text(text = "${currentReel.shares}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete optionally (if my own reel)
                    if (currentReel.creatorUsername == "explorer_barak" || currentReel.creatorUsername.endsWith("_barak")) {
                        IconButton(
                            onClick = { onDeleteReel(currentReel.id) },
                            modifier = Modifier.background(Color.Red.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }

                // Overlay details (Bottom Left containing details & Optional sync notification)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 40.dp, end = 80.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentReel.creatorDisplayName,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@${currentReel.creatorUsername}",
                            color = BarakEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentReel.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = currentReel.description,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Sync Flag
                    if (currentReel.publishedToBharamputra) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(BarakEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = BarakEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Published to Bharamputra Shorts (Synced Metadata)", color = BarakEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // FLOATING ACTION COMPOSER FOR REELS (Optional sync checkout)
        FloatingActionButton(
            onClick = { showCreateReelSheet = true },
            containerColor = BarakEmerald,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .testTag("barak_add_reel")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Reel")
        }

        // Create Reel Slider Composers Sheet
        if (showCreateReelSheet) {
            BarakCreateReelDialog(
                onDismiss = { showCreateReelSheet = false },
                onSave = { title, desc, url, publishShorts ->
                    onCreateReel(title, desc, url, publishShorts)
                    showCreateReelSheet = false
                    Toast.makeText(context, "Reel posted! Sync check finished.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun BarakCreateReelDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var publishToBharamputra by remember { mutableStateOf(true) }

    // Draft / Trim Points / Filters State
    var trimStart by remember { mutableStateOf(0f) }
    var trimEnd by remember { mutableStateOf(15f) }
    var selectedFilter by remember { mutableStateOf("Emerald Glow") } // Mono, Cyber, Emerald Glow

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.VideoCall, contentDescription = null, tint = BarakEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compose Barak Reel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Caption Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Hashtags") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Custom Video/Image URL (Optional)") },
                    placeholder = { Text("https://...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // --- OPTIONAL SYNC CONTROLS INJECTED ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BarakEmerald.copy(alpha = 0.1f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Publish to Bharamputra Shorts", color = BarakEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Autolink media files through shared backend sync", color = Color.Gray, fontSize = 10.sp)
                    }
                    Switch(
                        checked = publishToBharamputra,
                        onCheckedChange = { publishToBharamputra = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BarakEmerald
                        )
                    )
                }

                // Custom basic editing trim sliders
                Text("BASIC REEL SUITE EDITING", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trim Start: ${trimStart.toInt()}s", color = Color.White, fontSize = 11.sp)
                    Text("Trim End: ${trimEnd.toInt()}s", color = Color.White, fontSize = 11.sp)
                }
                RangeSlider(
                    value = trimStart..trimEnd,
                    onValueChange = { range ->
                        trimStart = range.start
                        trimEnd = range.endInclusive
                    },
                    valueRange = 0f..30f,
                    colors = SliderDefaults.colors(thumbColor = BarakEmerald, activeTrackColor = BarakEmerald)
                )

                Text("Aesthetic Lens Filters", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mono", "Cyber", "Emerald Glow").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BarakEmerald,
                                selectedLabelColor = Color.Black,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, desc, mediaUrl, publishToBharamputra) },
                colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald)
            ) {
                Text("POST REEL", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = BarakDarkSlate
    )
}

// ================= TAB 4: DIRECT MESSAGE THREADS =================
@Composable
fun BarakDMsTab(
    threads: List<DMThread>,
    onSendMessage: (String, String) -> Unit,
    onAcceptRequest: (String) -> Unit
) {
    var activeThreadId by remember { mutableStateOf<String?>(null) }
    var chatInput by remember { mutableStateOf("") }
    var dmSearchQuery by remember { mutableStateOf("") }

    val filteredThreads = remember(threads, dmSearchQuery) {
        if (dmSearchQuery.isEmpty()) threads else {
            threads.filter { it.partnerDisplayName.contains(dmSearchQuery, ignoreCase = true) }
        }
    }

    if (activeThreadId != null) {
        val selectedThread = threads.find { it.threadId == activeThreadId }
        if (selectedThread != null) {
            // Live messaging frame
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BarakDarkSlate)
                            .padding(top = 44.dp, bottom = 12.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { activeThreadId = null }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(model = selectedThread.partnerAvatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = selectedThread.partnerDisplayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Secure Unified Tunnel", color = BarakEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                bottomBar = {
                    if (selectedThread.isPendingRequest) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onAcceptRequest(selectedThread.threadId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald)
                            ) {
                                Text("Accept Message Request", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Secure message...", color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.DarkGray
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotEmpty()) {
                                        onSendMessage(selectedThread.threadId, chatInput)
                                        chatInput = ""
                                    }
                                },
                                modifier = Modifier.background(BarakEmerald, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                            }
                        }
                    }
                },
                containerColor = BarakDeepBlack
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(selectedThread.messages) { msg ->
                        val isMe = msg.senderUsername == "me"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isMe) BarakEmerald else BarakDarkSlate)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.content,
                                        color = if (isMe) Color.Black else Color.White,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Verified Tunnel",
                                            color = (if (isMe) Color.Black else Color.Gray).copy(alpha = 0.5f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isMe) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(10.dp)
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
    } else {
        // Threads List feed
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // Header Search
            OutlinedTextField(
                value = dmSearchQuery,
                onValueChange = { dmSearchQuery = it },
                placeholder = { Text("Filter chats or search handles...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) }
            )

            Text(
                text = "SECURE CONVERSATIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredThreads) { thread ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeThreadId = thread.threadId }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(model = thread.partnerAvatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = thread.partnerDisplayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "@${thread.partnerUsername}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text(
                                text = thread.messages.lastOrNull()?.content ?: "Start chat",
                                color = if (thread.messages.lastOrNull()?.isRead == false) BarakEmerald else Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Pending Request Highlight
                        if (thread.isPendingRequest) {
                            Box(
                                modifier = Modifier
                                    .background(BarakEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("REQUEST", color = BarakEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= TAB 5: PROFILE MANAGEMENT & ANALYTICS =================
@Composable
fun BarakProfileTab(
    profile: BarakProfile,
    personalPosts: List<BarakPost>,
    personalReels: List<BarakReel>,
    onUpdateProfile: (BarakProfile) -> Unit,
    alerts: List<BarakNotification>,
    blockedList: List<String>,
    onUnblock: (String) -> Unit,
    safetyReports: List<String>,
    onClearReports: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf("Posts") } // Posts, Reels, Alerts, Safety

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Luxury Header Banner card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Brush.horizontalGradient(listOf(BarakEmerald, BarakPurpleSpark)))
        )

        // Details Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.Black, CircleShape)
                ) {
                    AsyncImage(model = profile.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Text("Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Text("Exit Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = profile.displayName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                if (profile.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified Profile", tint = BarakEmerald, modifier = Modifier.size(18.dp))
                }
            }
            Text(text = "@${profile.username}", color = BarakEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            // Bio
            Text(
                text = profile.bio,
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Stats counts Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(text = "${profile.followersCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "followers", color = Color.Gray, fontSize = 11.sp)
                }
                Column {
                    Text(text = "${profile.followingCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "following", color = Color.Gray, fontSize = 11.sp)
                }
                Column {
                    Text(text = "${personalPosts.size + personalReels.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "synced posts", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab sub controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(BarakDarkSlate, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Posts", "Reels", "Alerts", "Safety").forEach { t ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = t }
                        .background(if (activeSubTab == t) BarakEmerald else Color.Transparent, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = t, color = if (activeSubTab == t) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display contents
        when (activeSubTab) {
            "Posts" -> {
                if (personalPosts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No personal social feed posts created yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    personalPosts.forEach { post ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .background(BarakDarkSlate, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp))) {
                                AsyncImage(model = post.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = post.caption, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                                Text(text = "${post.likesCount} likes • ${post.commentsCount} comments", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            "Reels" -> {
                // Draft Analytics insights
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(BarakEmerald.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .border(1.dp, BarakEmerald.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("CREATOR INSIGHTS & INTEL", color = BarakEmerald, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Reel Plays", color = Color.White, fontSize = 12.sp)
                                Text("48,209", color = BarakEmerald, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Column {
                                Text("Shared Sync Success", color = Color.White, fontSize = 12.sp)
                                Text("99.8%", color = BarakEmerald, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (personalReels.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No reels recorded yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    personalReels.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .background(BarakDarkSlate, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp))) {
                                AsyncImage(model = r.thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = r.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                                Text(text = "${r.likes} likes • Synced: ${r.publishedToBharamputra}", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            "Alerts" -> {
                if (alerts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("All caught up. No new activity reports.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    alerts.forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .background(BarakDarkSlate, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape)) {
                                AsyncImage(model = alert.triggeringAvatar, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "@${alert.triggeringUser}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = alert.description, color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            "Safety" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("SECURE BLOCKLIST", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    if (blockedList.isEmpty()) {
                        Text("No creator profiles currently blocked.", color = Color.DarkGray, fontSize = 12.sp)
                    } else {
                        blockedList.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "@$user", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                TextButton(onClick = { onUnblock(user) }) {
                                    Text("UNBLOCK", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)

                    Text("REPORTED CONTENT INDEX", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    if (safetyReports.isEmpty()) {
                        Text("No reports. Platform operates secure and clean.", color = Color.DarkGray, fontSize = 12.sp)
                    } else {
                        safetyReports.forEach { rep ->
                            Text(text = "Report submitted -> Creator: @$rep", color = BarakGoldMuted, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onClearReports,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Clear local indexes", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // EDIT PROFILE BOTTOM DIALOG
        if (showEditDialog) {
            var inputName by remember { mutableStateOf(profile.displayName) }
            var inputBio by remember { mutableStateOf(profile.bio) }
            var isPrivateChecked by remember { mutableStateOf(profile.isPrivate) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Barak Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Display Name") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = inputBio,
                            onValueChange = { inputBio = it },
                            label = { Text("Profile Bio Description") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Private Profile Mode", color = Color.White, fontSize = 13.sp)
                                Text("Only approved following see content", color = Color.Gray, fontSize = 10.sp)
                            }
                            Switch(
                                checked = isPrivateChecked,
                                onCheckedChange = { isPrivateChecked = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = BarakEmerald)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateProfile(profile.copy(displayName = inputName, bio = inputBio, isPrivate = isPrivateChecked))
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald)
                    ) {
                        Text("Save Profile Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Close", color = Color.White)
                    }
                },
                containerColor = BarakDarkSlate
            )
        }
    }
}

// ================= COMPONENT STORY VIEWER =================
@Composable
fun BarakStoryViewer(
    stories: List<BarakStory>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    var activeIdx by remember { mutableStateOf(startIndex) }
    val currentStory = stories[activeIdx]

    // Automated linear progression progress line
    var timelineProgress by remember { mutableStateOf(0.0f) }
    LaunchedEffect(activeIdx) {
        timelineProgress = 0.0f
        while (timelineProgress < 1.0f) {
            delay(40)
            timelineProgress += 0.015f
        }
        // Auto skip to next
        if (activeIdx < stories.size - 1) {
            activeIdx++
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Broad background
        AsyncImage(
            model = currentStory.mediaUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Shade overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Progress lines row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            stories.forEachIndexed { idx, _ ->
                val progressVal = if (idx < activeIdx) 1.0f else if (idx == activeIdx) timelineProgress else 0.0f
                LinearProgressIndicator(
                    progress = progressVal,
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp),
                    color = BarakEmerald,
                    trackColor = Color.DarkGray
                )
            }
        }

        // Top Author details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(model = currentStory.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "@${currentStory.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Barak Stories Engine", color = BarakEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        // Visual Middle Actions (Click left to prev, click right to next)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (activeIdx > 0) activeIdx-- else onDismiss()
                    }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (activeIdx < stories.size - 1) activeIdx++ else onDismiss()
                    }
            )
        }

        // Bottom title caption
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = currentStory.caption,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ================= COMPONENT COMMENTS DRAWER =================
@Composable
fun BarakCommentsDrawer(
    post: BarakPost,
    onDismiss: () -> Unit,
    onAddComment: (Int) -> Unit
) {
    val tempComments = remember {
        mutableStateListOf(
            "This view is absolutely spectacular!",
            "I love Northeast cultures, Barak has the finest community.",
            "Unified authentication makes this app extremely convenient!"
        )
    }
    var inputStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Secure Comments Hub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tempComments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Anonymous Fan: ",
                                color = BarakEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = comment, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputStr,
                    onValueChange = { inputStr = it },
                    placeholder = { Text("Type comment...", color = Color.Gray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BarakEmerald, unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputStr.isNotEmpty()) {
                        tempComments.add(inputStr)
                        onAddComment(post.commentsCount + 1)
                        inputStr = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BarakEmerald)
            ) {
                Text("POST", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = BarakDarkSlate
    )
}
