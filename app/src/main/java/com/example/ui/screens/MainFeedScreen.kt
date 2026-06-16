package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.CreatorChannel
import com.example.data.models.VideoItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.launch

@Composable
fun MainFeedScreen(viewModel: BharamputraViewModel) {
    var selectedTab by remember { mutableStateOf("home") }
    val user by viewModel.currentUser.collectAsState()
    val route by viewModel.currentRoute.collectAsState()

    var showProfileMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (selectedTab != "shorts") {
                TopAppBarContent(
                    viewModel = viewModel,
                    onProfileClicked = { showProfileMenu = true }
                )
            }
        },
        bottomBar = {
            BottomBarNavigation(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    if (it == "shorts") {
                        // select the first short as active or clear
                        viewModel.clearActiveVideo()
                    }
                }
            )
        },
        containerColor = RiverDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> HomeTabContent(viewModel = viewModel)
                "shorts" -> ShortsTabContent(viewModel = viewModel)
                "upload" -> UploadTabContent(viewModel = viewModel, onUploaded = { selectedTab = "home" })
                "subscriptions" -> SubscriptionsTabContent(viewModel = viewModel)
                "library" -> LibraryTabContent(viewModel = viewModel)
            }

            // Dropdown Profile Drawer/Menu Dialog
            if (showProfileMenu) {
                ProfileMenuDialog(
                    viewModel = viewModel,
                    onDismiss = { showProfileMenu = false }
                )
            }
        }
    }
}

@Composable
fun TopAppBarContent(
    viewModel: BharamputraViewModel,
    onProfileClicked: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    var isSearching by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RiverDarkSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isSearching) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BHARAMPUTRA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isSearching = true }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search videos", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RiverPrimary)
                        .clickable { onProfileClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.name?.take(1) ?: "U").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            // Expanded search layout
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search Bharamputra...", color = RiverMutedGrey, fontSize = 14.sp) },
                singleLine = true,
                leadingIcon = {
                    IconButton(onClick = {
                        isSearching = false
                        viewModel.updateSearchQuery("")
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { isSearching = false }),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = RiverDarkSurfaceVariant,
                    unfocusedContainerColor = RiverDarkSurfaceVariant,
                    focusedBorderColor = RiverSecondary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("global_search_input")
            )
        }
    }
}

@Composable
fun BottomBarNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = RiverDarkSurface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("home", "Home", Icons.Outlined.Home to Icons.Default.Home),
            Triple("shorts", "Shorts", Icons.Outlined.VideoLabel to Icons.Default.VideoLabel),
            Triple("upload", "Channel AI", Icons.Outlined.AddCircle to Icons.Default.AddCircle),
            Triple("subscriptions", "Subscribed", Icons.Outlined.Subscriptions to Icons.Default.Subscriptions),
            Triple("library", "Library", Icons.Outlined.VideoLibrary to Icons.Default.VideoLibrary)
        )

        items.forEach { (route, label, iconPair) ->
            val isSelected = selectedTab == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) iconPair.second else iconPair.first,
                        contentDescription = label,
                        tint = if (isSelected) RiverSecondary else RiverMutedGrey
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else RiverMutedGrey
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = RiverDarkSurfaceVariant
                )
            )
        }
    }
}

// --- TAB SUB-VIEWS ---

@Composable
fun HomeTabContent(viewModel: BharamputraViewModel) {
    val videos by viewModel.allVideos.collectAsState()
    val searchVal by viewModel.searchQuery.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()

    val categories = listOf("All", "Cinema", "Sci-Fi", "Riverine", "Tech", "Music", "Gaming", "Sports")

    // Filter videos combined by search value and category selection
    val filteredVideos = remember(videos, searchVal, category) {
        videos.filter { video ->
            val matchesQuery = searchVal.isEmpty() ||
                video.title.contains(searchVal, ignoreCase = true) ||
                video.description.contains(searchVal, ignoreCase = true) ||
                video.category.contains(searchVal, ignoreCase = true) ||
                video.tags.contains(searchVal, ignoreCase = true)

            val matchesCategory = category == "All" || video.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory && !video.isShort
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal category filters scroll list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(RiverDarkBackground)
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.White else Color(0x19FFFFFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(if (isSelected) Color.White else RiverDarkSurfaceVariant)
                        .clickable { viewModel.setCategoryFilter(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (filteredVideos.isEmpty()) {
            EmptyPlaceholderBox(
                title = "No Matches Plotted",
                desc = "We couldn't locate any streams fitting those parameters. Initiate another query or optimize filters!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredVideos) { video ->
                    VideoListItemCard(video = video, onClick = { viewModel.selectVideo(video) })
                }
            }
        }
    }
}

@Composable
fun VideoListItemCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}")
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RiverDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = video.duration, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Category badge top-start
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(RiverPrimary.copy(alpha = 0.9f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = video.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Channel Avatar mockup
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(RiverPrimary, RiverSecondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = video.creatorName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = video.creatorName,
                            color = RiverSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Channel",
                            tint = RiverSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatViews(video.views)} views • ${formatTimeAgo(video.uploadTime)}",
                        color = RiverMutedGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortsTabContent(viewModel: BharamputraViewModel) {
    val shortsList by viewModel.shorts.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var activeIndex by remember { mutableStateOf(0) }
    var likesCountOffset by remember { mutableStateOf(0) }

    if (shortsList.isEmpty()) {
        EmptyPlaceholderBox(
            title = "No Shorts Uploaded",
            desc = "Upload brief under 15-second streams to initiate the vertical speed river!"
        )
    } else {
        val currentShort = remember(shortsList, activeIndex) {
            shortsList.getOrNull(activeIndex % shortsList.size)
        }

        if (currentShort != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Vertical drag transition gesture zone
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {},
                                onDragCancel = {},
                                onVerticalDrag = { change, dragAmount ->
                                    if (dragAmount < -50) {
                                        // Swipe Up -> next short
                                        if (activeIndex < shortsList.size - 1) {
                                            activeIndex++
                                            likesCountOffset = 0
                                        }
                                        change.consume()
                                    } else if (dragAmount > 50) {
                                        // Swipe Down -> previous short
                                        if (activeIndex > 0) {
                                            activeIndex--
                                            likesCountOffset = 0
                                        }
                                        change.consume()
                                    }
                                }
                            )
                        }
                ) {
                    // Simulating high-fidelity player background using stylized artwork / AsyncImage matching stream
                    AsyncImage(
                        model = currentShort.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )

                    // Layer to darken for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Black.copy(alpha = 0.9f)
                                    ),
                                    startY = 200f
                                )
                            )
                    )

                    // Stream info and publisher overlay at base
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RiverPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentShort.creatorName.take(1).uppercase(), color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentShort.creatorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUBSCRIBE",
                                color = RiverSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.toggleSubscription(currentShort.creatorId)
                                        Toast.makeText(context, "Subscribed to ${currentShort.creatorName}", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentShort.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "#shorts #bharamputra #viral",
                            color = RiverSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Floating action menu on RHS
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 40.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Like control
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    likesCountOffset = if (likesCountOffset == 0) 1 else 0
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Like",
                                    tint = if (likesCountOffset > 0) Color.Red else Color.White
                                )
                            }
                            Text(
                                text = formatViews(currentShort.likes + likesCountOffset),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Comment control
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var showShortComments by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showShortComments = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = "Comments",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "124",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (showShortComments) {
                                AlertDialog(
                                    onDismissRequest = { showShortComments = false },
                                    containerColor = RiverDarkSurface,
                                    confirmButton = {
                                        TextButton(onClick = { showShortComments = false }) {
                                            Text("Dismiss", color = RiverSecondary)
                                        }
                                    },
                                    title = { Text("Bharamputra Short Feed Panel", color = Color.White, fontSize = 16.sp) },
                                    text = {
                                        Column {
                                            Text("Comments are simulated for this vertical speed stream.", color = RiverMutedGrey, fontSize = 13.sp)
                                        }
                                    }
                                )
                            }
                        }

                        // Share control
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Transmitting high-speed Bharamputra Stream Link!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Share",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.selectVideo(currentShort)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(RiverSecondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play High Quality",
                                tint = Color.Black
                            )
                        }
                    }

                    // Swipe tip top right banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Drag Vertically to Swipe ↕️", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UploadTabContent(
    viewModel: BharamputraViewModel,
    onUploaded: () -> Unit
) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tech") }
    var tags by remember { mutableStateOf("") }
    var isShort by remember { mutableStateOf(false) }
    var mockupUrl by remember { mutableStateOf("") }

    val categories = listOf("Cinema", "Sci-Fi", "Riverine", "Tech", "Music", "Gaming", "Sports")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bharamputra Broadcast Studio",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Broadcast streams instantly. Leverage native Gemini AI to expand headings and design SEO descriptions.",
                fontSize = 12.sp,
                color = RiverMutedGrey
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Draft Stream Key/Title") },
                    placeholder = { Text("e.g. My boat trip down the river") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RiverSecondary,
                        unfocusedBorderColor = RiverDarkSurfaceVariant
                    )
                )

                // Draft Desc
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Content Draft Description") },
                    placeholder = { Text("Provide details or keywords for AI optimization...") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("upload_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RiverSecondary,
                        unfocusedBorderColor = RiverDarkSurfaceVariant
                    )
                )

                // AI Optimize Button
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.enhanceWithGemini(title, desc, category) { enhancedTitle, enhancedDesc, suggestedTags ->
                                title = enhancedTitle
                                desc = enhancedDesc
                                tags = suggestedTags
                                Toast.makeText(context, "AI Optimizer completed successfully!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Input at least a draft title to invoke AI", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RiverSecondary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isAiLoading
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Bharamputra AI Optimizing...", color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enhance Metadata with Google Gemini AI", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = RiverDarkSurfaceVariant)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category selection
                Text("Select Primary Node Category", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) RiverPrimary else RiverDarkSurfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Short vs Long
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Publish as Short Speed-Stream", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Scales output into vertical scroll sections.", fontSize = 11.sp, color = RiverMutedGrey)
                    }
                    Switch(
                        checked = isShort,
                        onCheckedChange = { isShort = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RiverSecondary)
                    )
                }

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Stream Search Tags (comma separated)") },
                    placeholder = { Text("e.g. river, adventure, boat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RiverSecondary,
                        unfocusedBorderColor = RiverDarkSurfaceVariant
                    )
                )

                // Optional Direct Stream Url
                OutlinedTextField(
                    value = mockupUrl,
                    onValueChange = { mockupUrl = it },
                    label = { Text("Direct MP4 URL (Optional override)") },
                    placeholder = { Text("Leave blank to default to high speed stream") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RiverSecondary,
                        unfocusedBorderColor = RiverDarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Submit broadcast
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.uploadVideo(
                                title = title,
                                description = desc,
                                videoUrl = mockupUrl,
                                selectedThumbnailUrl = null, // auto fallback
                                category = category,
                                tags = tags,
                                isShort = isShort,
                                onComplete = {
                                    Toast.makeText(context, "$title successfully uploaded to global node!", Toast.LENGTH_LONG).show()
                                    onUploaded()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Broadcasting requires a valid Title!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("upload_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Broadcast Stream Live", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SubscriptionsTabContent(viewModel: BharamputraViewModel) {
    val channels by viewModel.allChannels.collectAsState()
    val subIds by viewModel.subscribedChannelIds.collectAsState()
    val videos by viewModel.allVideos.collectAsState()

    val subscribedChannels = remember(channels, subIds) {
        channels.filter { subIds.contains(it.id) }
    }

    val subscriptionVideos = remember(videos, subIds) {
        videos.filter { subIds.contains(it.creatorId) && !it.isShort }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel circles row
        Text(
            text = "Your Subscribed Channels",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (subscribedChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(RiverDarkSurface, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tap Subscribe on creators to organize your stream dashboard!", color = RiverMutedGrey, fontSize = 12.sp)
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subscribedChannels) { channel ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.navigateTo("channel/${channel.id}")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(RiverPrimary, RiverSecondary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = channel.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = channel.name, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = RiverDarkSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Latest Uploads From Subscriptions",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )

        if (subscriptionVideos.isEmpty()) {
            EmptyPlaceholderBox(
                title = "Channel Streams Silent",
                desc = "No recent long-format streams found. Explore home to find active Broadcasters!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subscriptionVideos) { video ->
                    VideoListItemCard(video = video, onClick = { viewModel.selectVideo(video) })
                }
            }
        }
    }
}

@Composable
fun LibraryTabContent(viewModel: BharamputraViewModel) {
    val history by viewModel.watchHistory.collectAsState()
    val watchLaterList by viewModel.watchLater.collectAsState()

    var showAnalyticsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // High fidelity Analytics button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RiverDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, RiverSecondary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAnalyticsDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bharamputra Stream Analytics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text("Review real-time subscriber scaling and channel traffic graphs.", fontSize = 12.sp, color = RiverMutedGrey)
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                }
            }
        }

        // Watch history segment
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Watch History", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                if (history.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearWatchHistory() }) {
                        Text("Clear All", color = RiverSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RiverDarkSurface, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No watch histories recorded.", color = RiverMutedGrey, fontSize = 12.sp)
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(history) { pair ->
                        val video = pair.second
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { viewModel.selectVideo(video) }
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Watch later segment
        item {
            Text("Watch Later Queue", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (watchLaterList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RiverDarkSurface, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Empty queue. Tap '+' on players to bookmark.", color = RiverMutedGrey, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    watchLaterList.forEach { video ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RiverDarkSurface, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectVideo(video) }
                                .padding(10.dp)
                        ) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp, 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(video.creatorName, color = RiverSecondary, fontSize = 11.sp)
                            }
                            IconButton(onClick = { viewModel.toggleWatchLater(video.id) }) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAnalyticsDialog) {
        AnalyticsDisplayDialog(onDismiss = { showAnalyticsDialog = false })
    }
}

// --- SUB LEVEL HELPER COMPOSABLES ---

@Composable
fun EmptyPlaceholderBox(title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = RiverSecondary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = RiverMutedGrey,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ProfileMenuDialog(
    viewModel: BharamputraViewModel,
    onDismiss: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = RiverSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Account Console", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Active Stream Identity: ${user?.name}", color = Color.White, fontSize = 14.sp)
                Text(text = "Handle: ${user?.handle}", color = RiverSecondary, fontSize = 13.sp)
                Text(text = "Security Role: ${user?.role}", color = RiverSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = RiverDarkSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                // Route to Admin Dash if ADMIN
                if (user?.role == "ADMIN") {
                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.navigateTo("admin")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RiverPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Moderation Console")
                    }
                }

                // Route to Bharamputra Studio creator application
                Button(
                    onClick = {
                        onDismiss()
                        viewModel.navigateTo("studio")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RiverSecondary)
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Bharamputra Studio (Creator Model)", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onDismiss()
                        viewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("De-authorize Session")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RiverSecondary)
            }
        },
        containerColor = RiverDarkSurface
    )
}

@Composable
fun AnalyticsDisplayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Regional Node Analytics", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("43.9K", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RiverSecondary)
                        Text("Active Subscribers", fontSize = 11.sp, color = RiverMutedGrey)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1.25M", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RiverPrimary)
                        Text("Views Traffic", fontSize = 11.sp, color = RiverMutedGrey)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Traffic Scalability Graph", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                // High-fidelity dynamic Canvas diagram drawing traffic lines
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(RiverDarkBackground, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // grid baselines
                    drawRect(color = RiverDarkSurfaceVariant)

                    // Draw line paths
                    val points = listOf(
                        height * 0.9f,
                        height * 0.75f,
                        height * 0.62f,
                        height * 0.33f,
                        height * 0.15f
                    )

                    val segmentWidth = width / (points.size - 1)
                    val path = androidx.compose.ui.graphics.Path()

                    points.forEachIndexed { idx, yVal ->
                        val xVal = idx * segmentWidth
                        if (idx == 0) {
                            path.moveTo(xVal, yVal)
                        } else {
                            path.lineTo(xVal, yVal)
                        }
                        drawCircle(color = RiverSecondary, radius = 5f, center = androidx.compose.ui.geometry.Offset(xVal, yVal))
                    }

                    drawPath(
                        path = path,
                        color = RiverSecondary,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                }
                Text("Traffic scaling registers exponential acceleration over the last 5 tracking cycles.", fontSize = 11.sp, color = RiverMutedGrey, lineHeight = 14.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = RiverSecondary)
            }
        },
        containerColor = RiverDarkSurface
    )
}

// --- UTILITY FORMATS ---
fun formatViews(views: Long): String {
    return when {
        views >= 1000000 -> String.format("%.1fM", views / 1000000.0)
        views >= 1000 -> String.format("%.1fK", views / 1000.0)
        else -> views.toString()
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val hrs = diff / (1000 * 60 * 60)
    val days = hrs / 24
    return when {
        days > 0 -> "$days days ago"
        hrs > 0 -> "$hrs hours ago"
        else -> "Just now"
    }
}
