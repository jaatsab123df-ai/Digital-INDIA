package com.example

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import coil.compose.AsyncImage
import com.example.data.models.CreatorChannel
import com.example.data.models.VideoItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RiverDarkBackground
import com.example.ui.theme.RiverDarkSurface
import com.example.ui.theme.RiverDarkSurfaceVariant
import com.example.ui.theme.RiverPrimary
import com.example.ui.theme.RiverSecondary
import com.example.ui.theme.RiverTertiary
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Gold Luxury Accents for Bharamputra Music ---
val MusicGold = Color(0xFFFBBF24)      // Premium Gold-400
val MusicDarkBronze = Color(0xFF78350F)  // Luxury Bronze
val MusicGoldMuted = Color(0xFFD97706)   // Warm Bronze-Gold
val MusicSecondaryAccent = Color(0xFFA855F7) // Royal Purple for premium contrast

class MusicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access our shared platform ViewModel
        val viewModel = ViewModelProvider(this).get(BharamputraViewModel::class.java)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RiverDarkBackground
                ) {
                    BharamputraMusicNavigationContainer(viewModel)
                }
            }
        }
    }
}

@Composable
fun BharamputraMusicNavigationContainer(viewModel: BharamputraViewModel) {
    var currentMusicScreen by remember { mutableStateOf("music_splash") }
    val currentUser by viewModel.currentUser.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    
    // Auto-syncing tracks filter (videos categorized as Music)
    val songTracks = remember(allVideos) {
        allVideos.filter { it.category.equals("Music", ignoreCase = true) }
    }

    // Shared Playback Engine State
    var activeSong by remember { mutableStateOf<VideoItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableStateOf(0f) } // 0.0 to 1.0f
    val recentlyPlayed = remember { mutableStateListOf<VideoItem>() }
    val likedSongIds = remember { mutableStateListOf<String>() }
    val localPlaylists = remember { mutableStateListOf<Pair<String, List<VideoItem>>>() }
    
    var showFullPlayer by remember { mutableStateOf(false) }
    var audioQuality by remember { mutableStateOf("High-Fidelity (320kbps)") }
    var isShuffleMode by remember { mutableStateOf(false) }
    var isRepeatMode by remember { mutableStateOf(false) }
    var activePlaylistFilter by remember { mutableStateOf("All Songs") }
    var selectedArtistChannel by remember { mutableStateOf<CreatorChannel?>(null) }

    // Seed local playlists and likes on first run
    LaunchedEffect(songTracks) {
        if (songTracks.isNotEmpty() && recentlyPlayed.isEmpty()) {
            recentlyPlayed.addAll(songTracks.take(2))
            likedSongIds.add(songTracks.first().id)
            localPlaylists.add(Pair("Chill River Mornings", songTracks.take(2)))
            localPlaylists.add(Pair("NE Late-Night Hacking", listOf(songTracks.last())))
        }
    }

    // Playback Progress simulation coroutine loop
    LaunchedEffect(isPlaying, activeSong) {
        if (isPlaying && activeSong != null) {
            while (true) {
                delay(1000)
                playbackProgress += 0.02f
                if (playbackProgress >= 1.0f) {
                    // Loop or skip
                    if (isRepeatMode) {
                        playbackProgress = 0f
                    } else {
                        // Skip next
                        val currIndex = songTracks.indexOf(activeSong)
                        if (currIndex != -1 && currIndex < songTracks.size - 1) {
                            activeSong = songTracks[currIndex + 1]
                            playbackProgress = 0f
                        } else {
                            isPlaying = false
                            playbackProgress = 0f
                        }
                    }
                }
            }
        }
    }

    val playSong = { song: VideoItem ->
        activeSong = song
        isPlaying = true
        playbackProgress = 0f
        if (!recentlyPlayed.any { it.id == song.id }) {
            recentlyPlayed.add(0, song)
            if (recentlyPlayed.size > 8) recentlyPlayed.removeLast()
        }
    }

    // Animate between screens
    AnimatedContent(
        targetState = currentMusicScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "musicNavigationTransit"
    ) { screen ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                "music_splash" -> {
                    MusicSplashScreen {
                        currentMusicScreen = if (currentUser != null) "music_home" else "music_auth"
                    }
                }
                "music_auth" -> {
                    MusicAuthScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            currentMusicScreen = "music_home"
                        }
                    )
                }
                "music_home" -> {
                    MusicHomeScreen(
                        viewModel = viewModel,
                        songTracks = songTracks,
                        recentSongs = recentlyPlayed,
                        likedSongs = likedSongIds,
                        playlists = localPlaylists,
                        activeSong = activeSong,
                        isPlaying = isPlaying,
                        onPlaySong = playSong,
                        onToggleLike = { id ->
                            if (likedSongIds.contains(id)) likedSongIds.remove(id) else likedSongIds.add(id)
                        },
                        onCreatePlaylist = { name ->
                            localPlaylists.add(Pair(name, emptyList()))
                        },
                        onPlayPlaylist = { pl ->
                            if (pl.second.isNotEmpty()) {
                                playSong(pl.second.first())
                            }
                        },
                        onOpenSettings = {
                            currentMusicScreen = "music_settings"
                        },
                        onOpenArtist = { artistName ->
                            val channel = viewModel.allChannels.value.find { it.name == artistName }
                            if (channel != null) {
                                selectedArtistChannel = channel
                                currentMusicScreen = "artist_profile"
                            } else {
                                Toast.makeText(viewModel.getApplication(), "Artist details fetching...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onMaximizePlayer = {
                            showFullPlayer = true
                        },
                        onTogglePlayState = { isPlaying = !isPlaying }
                    )
                }
                "artist_profile" -> {
                    ArtistProfileScreen(
                        channel = selectedArtistChannel,
                        songTracks = songTracks.filter { it.creatorName == selectedArtistChannel?.name },
                        onBack = { currentMusicScreen = "music_home" },
                        onPlaySong = playSong,
                        likedSongs = likedSongIds,
                        onToggleLike = { id ->
                            if (likedSongIds.contains(id)) likedSongIds.remove(id) else likedSongIds.add(id)
                        },
                        activeSong = activeSong,
                        isPlaying = isPlaying,
                        onTogglePlayState = { isPlaying = !isPlaying },
                        onMaximizePlayer = { showFullPlayer = true }
                    )
                }
                "music_settings" -> {
                    MusicSettingsScreen(
                        quality = audioQuality,
                        onQualityChange = { audioQuality = it },
                        onBack = { currentMusicScreen = "music_home" },
                        currentUser = currentUser,
                        onSignOut = {
                            viewModel.logout()
                            currentMusicScreen = "music_auth"
                        }
                    )
                }
            }

            // Persistently hovering mini music player
            if (activeSong != null && !showFullPlayer && currentMusicScreen != "music_splash" && currentMusicScreen != "music_auth") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 98.dp) // elevate above navigation bottom spacing
                ) {
                    MiniMusicPlayer(
                        song = activeSong!!,
                        isPlaying = isPlaying,
                        progress = playbackProgress,
                        onTogglePlay = { isPlaying = !isPlaying },
                        onNext = {
                            val currIdx = songTracks.indexOf(activeSong)
                            if (currIdx != -1 && currIdx < songTracks.size - 1) {
                                playSong(songTracks[currIdx + 1])
                            }
                        },
                        onMaximize = { showFullPlayer = true }
                    )
                }
            }

            // Sliding Full screen Player Sheet
            if (showFullPlayer && activeSong != null) {
                FullScreenMusicPlayer(
                    song = activeSong!!,
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    isShuffle = isShuffleMode,
                    isRepeat = isRepeatMode,
                    onTogglePlay = { isPlaying = !isPlaying },
                    onSeekBarChange = { playbackProgress = it },
                    onPrev = {
                        val currIdx = songTracks.indexOf(activeSong)
                        if (currIdx > 0) {
                            playSong(songTracks[currIdx - 1])
                        }
                    },
                    onNext = {
                        val currIdx = songTracks.indexOf(activeSong)
                        if (currIdx != -1 && currIdx < songTracks.size - 1) {
                            playSong(songTracks[currIdx + 1])
                        }
                    },
                    onToggleShuffle = { isShuffleMode = !isShuffleMode },
                    onToggleRepeat = { isRepeatMode = !isRepeatMode },
                    onDismiss = { showFullPlayer = false },
                    isLiked = likedSongIds.contains(activeSong!!.id),
                    onToggleLike = {
                        if (likedSongIds.contains(activeSong!!.id)) {
                            likedSongIds.remove(activeSong!!.id)
                        } else {
                            likedSongIds.add(activeSong!!.id)
                        }
                    }
                )
            }
        }
    }
}

// ================= SPLASH SCREEN =================
@Composable
fun MusicSplashScreen(onSplashDone: () -> Unit) {
    var startAnimate by remember { mutableStateOf(false) }

    val recordRotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "recordRotation"
    )

    val scaleUp by animateFloatAsState(
        targetValue = if (startAnimate) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleUp"
    )

    LaunchedEffect(Unit) {
        startAnimate = true
        delay(2600)
        onSplashDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0E17), 
                        Color(0xFF050508)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Revolving Luxury Vinyl Record Icon
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scaleUp)
                    .graphicsLayer(rotationZ = recordRotation)
                    .clip(CircleShape)
                    .border(3.dp, Brush.sweepGradient(listOf(MusicGold, MusicSecondaryAccent, MusicGold)), CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl grooves
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                )
                // Center disk label
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MusicGold, MusicDarkBronze))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Bharamputra Music",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MusicGold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "THE SOUND OF ORIGIN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MusicGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ================= AUDIO QUALITY & ACCIDENTS SETTINGS =================
@Composable
fun MusicSettingsScreen(
    quality: String,
    onQualityChange: (String) -> Unit,
    onBack: () -> Unit,
    currentUser: com.example.data.auth.UserAccount?,
    onSignOut: () -> Unit
) {
    val qualities = listOf(
        "High-Fidelity (320kbps Lossless AAC)",
        "Balanced (192kbps Quality Stream)",
        "Data Saver (96kbps Low Buffer)"
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(top = 44.dp, bottom = 12.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Audio Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = RiverDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "STREAMING QUALITY",
                color = MusicGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            qualities.forEach { q ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (quality == q) RiverDarkSurfaceVariant else Color.Transparent)
                        .clickable { onQualityChange(q) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (quality == q) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (quality == q) MusicGold else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = q, color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SHARED ACCOUNT PROFILE",
                color = MusicGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RiverDarkSurface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MusicGold, MusicSecondaryAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.take(1)?.uppercase() ?: "E",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = currentUser?.name ?: "Bharamputra Fan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = currentUser?.handle ?: "@explorer", color = Color.Gray, fontSize = 13.sp)
                    Text(text = "Status: Shared Sync Enabled", color = Color.Green.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Sign Out from Shared System", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= ARTIST PROFILE VIEW =================
@Composable
fun ArtistProfileScreen(
    channel: CreatorChannel?,
    songTracks: List<VideoItem>,
    onBack: () -> Unit,
    onPlaySong: (VideoItem) -> Unit,
    likedSongs: List<String>,
    onToggleLike: (String) -> Unit,
    activeSong: VideoItem?,
    isPlaying: Boolean,
    onTogglePlayState: () -> Unit,
    onMaximizePlayer: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (channel == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Artist profile loading...", color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RiverDarkBackground)
    ) {
        // Hero Background Header
        AsyncImage(
            model = channel.bannerUrl, // can also use matching Unsplash placeholder dynamically
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .alpha(0.4f),
            contentScale = ContentScale.Crop,
            error = painterResource(id = android.R.drawable.presence_online)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Action overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(110.dp))

            // Artist details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    if (channel.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Artist",
                            tint = MusicGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${channel.subscribers / 1000}K Monthly Listeners • ${songTracks.size} Tracks Synced",
                    color = MusicGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = channel.bio,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Play / Follow Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (songTracks.isNotEmpty()) {
                                onPlaySong(songTracks.first())
                            } else {
                                Toast.makeText(context, "No tracks available.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MusicGold),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shuffle Play", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Following Artist!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Follow Artist", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Popular tracks container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "POPULAR SONGS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MusicGold,
                    letterSpacing = 2.sp
                )

                if (songTracks.isEmpty()) {
                    Text(
                        text = "This creator hasn't published any videos under 'Music' category yet on Bharamputra.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    songTracks.forEachIndexed { idx, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (activeSong?.id == track.id) RiverDarkSurface else Color.Transparent)
                                .clickable { onPlaySong(track) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}",
                                color = if (activeSong?.id == track.id) MusicGold else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp),
                                fontSize = 14.sp
                            )

                            AsyncImage(
                                model = track.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (activeSong?.id == track.id) MusicGold else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.views / 1000}K plays",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(onClick = { onToggleLike(track.id) }) {
                                Icon(
                                    imageVector = if (likedSongs.contains(track.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (likedSongs.contains(track.id)) MusicGold else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= AUTH/SYNC SCREEN =================
@Composable
fun MusicAuthScreen(
    viewModel: BharamputraViewModel,
    onLoginSuccess: () -> Unit
) {
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var selectedMethod by remember { mutableStateOf("otp") } // otp, password
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isNewUserMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C0714), 
                        Color(0xFF030304)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Brand
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(MusicGold, MusicSecondaryAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isNewUserMode) "Create Shared Identity" else "Connect Shared Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Use the same credentials as Bharamputra Stream",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error notice
            if (authError != null) {
                Text(
                    text = authError!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Tabs Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f)),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = "otp" }
                        .background(if (selectedMethod == "otp") MusicGold else Color.Transparent)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Secure OTP", 
                        fontWeight = FontWeight.Bold, 
                        color = if (selectedMethod == "otp") Color.Black else Color.White,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = "password" }
                        .background(if (selectedMethod == "password") MusicGold else Color.Transparent)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Email & Password", 
                        fontWeight = FontWeight.Bold, 
                        color = if (selectedMethod == "password") Color.Black else Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            if (selectedMethod == "otp") {
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number (+91...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MusicGold,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { otpInput = it },
                    label = { Text("6-Digit Login Verification Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MusicGold,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }
                )
            } else {
                if (isNewUserMode) {
                    OutlinedTextField(
                        value = displayNameInput,
                        onValueChange = { displayNameInput = it },
                        label = { Text("Full Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MusicGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = MusicGold,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Shared Account Email Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MusicGold,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password credentials") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MusicGold,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Gray) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedMethod == "otp") {
                        if (phoneInput.length >= 10 && otpInput.length == 6) {
                            viewModel.registerWithEmail("shared_${phoneInput}@bharamputra.com", "Shared Fan", phoneInput)
                        } else {
                            Toast.makeText(context, "Please enter a valid phone and 6-digit OTP", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (isNewUserMode) {
                            if (emailInput.isNotEmpty() && displayNameInput.isNotEmpty()) {
                                viewModel.registerWithEmail(emailInput, displayNameInput)
                            } else {
                                Toast.makeText(context, "Fill in all credentials.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (emailInput.isNotEmpty()) {
                                viewModel.loginWithEmail(emailInput)
                            } else {
                                Toast.makeText(context, "Please provide your email.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MusicGold),
                shape = RoundedCornerShape(12.dp),
                enabled = !isAuthLoading
            ) {
                if (isAuthLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isNewUserMode) "Register Shared Profile" else "Access Audio Library",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedMethod == "password") {
                Text(
                    text = if (isNewUserMode) "Already have an account? Sign In" else "Create a shared profile",
                    color = MusicGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { isNewUserMode = !isNewUserMode }
                        .padding(8.dp)
                )
            }
        }
    }
}

// ================= HOME DASHBOARD VIEW =================
@Composable
fun MusicHomeScreen(
    viewModel: BharamputraViewModel,
    songTracks: List<VideoItem>,
    recentSongs: List<VideoItem>,
    likedSongs: List<String>,
    playlists: List<Pair<String, List<VideoItem>>>,
    activeSong: VideoItem?,
    isPlaying: Boolean,
    onPlaySong: (VideoItem) -> Unit,
    onToggleLike: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onPlayPlaylist: (Pair<String, List<VideoItem>>) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenArtist: (String) -> Unit,
    onMaximizePlayer: () -> Unit,
    onTogglePlayState: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("Home") } // Home, Library, Search
    var searchQuery by remember { mutableStateOf("") }
    var showAddPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(top = 44.dp, bottom = 12.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "BHARAMPUTRA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MusicGold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Music Experience",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Row {
                        IconButton(onClick = onOpenSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "Home",
                    onClick = { activeTab = "Home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Explore") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MusicGold,
                        indicatorColor = MusicGold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Search",
                    onClick = { activeTab = "Search" },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MusicGold,
                        indicatorColor = MusicGold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = activeTab == "Library",
                    onClick = { activeTab = "Library" },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                    label = { Text("My Beats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MusicGold,
                        indicatorColor = MusicGold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = RiverDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Home" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 12.dp)
                    ) {
                        // Banner/Carousel promo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MusicDarkBronze,
                                            MusicSecondaryAccent.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MusicGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ORIGIN PREVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Bamboo Flutes & Celtic Bass", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                                Text("Processed dynamically into 320kbps Atmos soundscapes", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        // Recommendation Section
                        SectionHeader(title = "SUGGESTED FOR YOU")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(songTracks) { song ->
                                RecommendTrackCard(
                                    song = song,
                                    onClick = { onPlaySong(song) },
                                    onArtistClick = { onOpenArtist(song.creatorName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Trending Row List
                        SectionHeader(title = "TRENDING SOUND FREQUENCIES")
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            songTracks.take(4).forEach { song ->
                                TrendingRowItem(
                                    song = song,
                                    onClick = { onPlaySong(song) },
                                    isLiked = likedSongs.contains(song.id),
                                    onToggleLike = { onToggleLike(song.id) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recently Played
                        if (recentSongs.isNotEmpty()) {
                            SectionHeader(title = "RECENTLY CAPTURED FREQUENCIES")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recentSongs) { song ->
                                    RecommendTrackCard(
                                        song = song,
                                        onClick = { onPlaySong(song) },
                                        onArtistClick = { onOpenArtist(song.creatorName) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(110.dp)) // padding below floating mini player
                    }
                }
                "Search" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search songs, artists, playlists...", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MusicGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MusicGold) }
                        )

                        val filteredSongs = remember(searchQuery, songTracks) {
                            if (searchQuery.isEmpty()) songTracks
                            else songTracks.filter { 
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                it.creatorName.contains(searchQuery, ignoreCase = true) ||
                                it.tags.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            if (filteredSongs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("No matches. Try searching other categories.", color = Color.Gray)
                                        }
                                    }
                                }
                            } else {
                                items(filteredSongs) { song ->
                                    TrendingRowItem(
                                        song = song,
                                        onClick = { onPlaySong(song) },
                                        isLiked = likedSongs.contains(song.id),
                                        onToggleLike = { onToggleLike(song.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                "Library" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "CUSTOM PLAYLISTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicGold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Playlists Grid View
                        playlists.forEach { pl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RiverDarkSurface)
                                    .clickable { onPlayPlaylist(pl) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MusicDarkBronze),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.QueueMusic, contentDescription = null, tint = MusicGold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = pl.first, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "${pl.second.size} tracks synced", color = Color.Gray, fontSize = 12.sp)
                                }
                                IconButton(onClick = { onPlayPlaylist(pl) }) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = MusicGold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showAddPlaylistDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MusicDarkBronze),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MusicGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Playlist Collection", color = MusicGold, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "LIKED SONGS COLLECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicGold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val likedTrackItems = songTracks.filter { likedSongs.contains(it.id) }
                        if (likedTrackItems.isEmpty()) {
                            Text(text = "No liked songs yet. Tap favorite on any song to compile your beats.", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            likedTrackItems.forEach { song ->
                                TrendingRowItem(
                                    song = song,
                                    onClick = { onPlaySong(song) },
                                    isLiked = true,
                                    onToggleLike = { onToggleLike(song.id) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }
        }
    }

    if (showAddPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlaylistDialog = false },
            title = { Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name i.e. Late night coding") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotEmpty()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showAddPlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicGold)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlaylistDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = RiverDarkSurface
        )
    }
}

// Helper design modifier for sized icon
fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.then(Modifier.width(size).height(size))

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MusicGold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
    )
}

@Composable
fun RecommendTrackCard(
    song: VideoItem,
    onClick: () -> Unit,
    onArtistClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = song.creatorName,
            color = MusicGold.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(onClick = onArtistClick)
        )
    }
}

@Composable
fun TrendingRowItem(
    song: VideoItem,
    onClick: () -> Unit,
    isLiked: Boolean,
    onToggleLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RiverDarkSurface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.creatorName,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onToggleLike) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) MusicGold else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ================= MINI MUSIC PLAYER =================
@Composable
fun MiniMusicPlayer(
    song: VideoItem,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onMaximize: () -> Unit
) {
    // Beautiful wave indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    val waveHeight1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )
    val waveHeight2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )
    val waveHeight3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 50),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave3"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MusicGold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(onClick = onMaximize)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.creatorName,
                    color = MusicGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Wave equalizer
            if (isPlaying) {
                Row(
                    modifier = Modifier.width(28.dp).padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(3.dp).height(waveHeight1.dp).background(MusicGold))
                    Box(modifier = Modifier.width(3.dp).height(waveHeight2.dp).background(MusicGold))
                    Box(modifier = Modifier.width(3.dp).height(waveHeight3.dp).background(MusicGold))
                }
            }

            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // Tiny Progress slider indicator line
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MusicGold,
            trackColor = Color.DarkGray
        )
    }
}

// ================= FULL-SCREEN PLAYER SHEET =================
@Composable
fun FullScreenMusicPlayer(
    song: VideoItem,
    isPlaying: Boolean,
    progress: Float,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onTogglePlay: () -> Unit,
    onSeekBarChange: (Float) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onDismiss: () -> Unit,
    isLiked: Boolean,
    onToggleLike: () -> Unit
) {
    val recordRotation by rememberInfiniteTransition(label = "vinyl").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Lyrics system synced with current duration %
    val lyricsLines = listOf(
        Pair(0.0f, "🌊 Let the grand river flows..."),
        Pair(0.12f, "🌾 Heavy bamboo beats in your heart"),
        Pair(0.24f, "🎸 Hear the golden guitar loops"),
        Pair(0.36f, "⚡ Hacking late into the Kotlin night"),
        Pair(0.48f, "🌌 Finding order within cosmic space"),
        Pair(0.60f, "🌾 Carrying folklore along the stream"),
        Pair(0.72f, "🌅 Savoring the misty morning sunrise"),
        Pair(0.84f, "🚣 Floating down standard riverways..."),
        Pair(0.95f, "✨ Pure ambient silence echoes")
    )

    val activeLyric = remember(progress) {
        lyricsLines.filter { progress >= it.first }.lastOrNull()?.second ?: "🎵 Instrumental intro..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) {} // block clickthrough
    ) {
        // Blurred backdrop of album artwork
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
                .blur(32.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dismiss arrow and quality
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PLAYING FROM SYNC", fontSize = 10.sp, color = MusicGold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Atmospheric Stream", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }

                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Premium Circular Vinyl Artwork
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer(rotationZ = if (isPlaying) recordRotation else 0f)
                    .clip(CircleShape)
                    .border(4.dp, Brush.linearGradient(listOf(MusicGold, MusicSecondaryAccent)), CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Outer record rings
                Box(modifier = Modifier.size(220.dp).border(1.dp, Color.DarkGray.copy(alpha = 0.4f), CircleShape))
                Box(modifier = Modifier.size(180.dp).border(1.dp, Color.DarkGray.copy(alpha = 0.4f), CircleShape))

                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Vinyl label hole center
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.9f))
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Title, Artist, and Like Column
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(0.85f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.creatorName,
                        color = MusicGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onToggleLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MusicGold else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-time scrolling Synced Lyrics Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Comment, contentDescription = null, tint = MusicGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activeLyric,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Progress Bar Slider Seeker
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = onSeekBarChange,
                    colors = SliderDefaults.colors(
                        thumbColor = MusicGold,
                        activeTrackColor = MusicGold,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val minutes = (progress * 4).toInt() // assume a 4 minute song mock
                    val seconds = ((progress * 4 % 1) * 60).toInt()
                    Text(
                        text = String.format("%d:%02d", minutes, seconds),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "4:12",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Control Buttons Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) MusicGold else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onPrev) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Giant polished play glass orb circle
                FloatingActionButton(
                    onClick = onTogglePlay,
                    containerColor = MusicGold,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeat) MusicGold else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}
