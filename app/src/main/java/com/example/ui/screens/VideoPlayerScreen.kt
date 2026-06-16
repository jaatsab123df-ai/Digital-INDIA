package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.models.VideoComment
import com.example.data.models.VideoItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.BharamputraViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(viewModel: BharamputraViewModel) {
    val activeVideo by viewModel.activePlayingVideo.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val comments by viewModel.commentsForCurrentVideo.collectAsState()
    val subIds by viewModel.subscribedChannelIds.collectAsState()
    val likedIds by viewModel.likedVideoIds.collectAsState()

    val context = LocalContext.current

    if (activeVideo == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RiverDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No Active Video Selected", color = Color.White)
        }
        return
    }

    val video = activeVideo!!
    val isLiked = likedIds.contains(video.id)
    val isSubscribed = subIds.contains(video.creatorId)

    // Player state attributes
    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1.0f) }
    var currentPlaybackPosition by remember { mutableStateOf(0) }
    var videoDurationSeconds by remember { mutableStateOf(300) } // Default mock dur

    // Comment controller
    var commentText by remember { mutableStateOf("") }
    var isCommentsExpanded by remember { mutableStateOf(false) }

    // Simulating Picture-In-Picture mode dialog
    var isPipSimulated by remember { mutableStateOf(false) }

    val relatedVideos = remember(allVideos, video) {
        allVideos.filter { it.id != video.id && !it.isShort }
    }

    // Keep position ticking for simulation seekbar
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentPlaybackPosition < videoDurationSeconds) {
                currentPlaybackPosition++
            } else {
                currentPlaybackPosition = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RiverDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. High Performance Video Player Layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Color.Black)
        ) {
            if (!isPipSimulated) {
                // Wrapper to render active playable mp4 direct stream links
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(video.videoUrl))
                            setOnPreparedListener { media ->
                                media.isLooping = true
                                videoDurationSeconds = (media.duration / 1000).coerceAtLeast(10)
                                media.playbackParams = media.playbackParams.setSpeed(speed)
                                start()
                            }
                        }
                    },
                    update = { view ->
                        // Dynamically adjust parameters like play/pause and play speeds!
                        if (isPlaying) {
                            view.start()
                        } else {
                            view.pause()
                        }
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                view.post {
                                    val mp = view.javaClass.getDeclaredField("mMediaPlayer").apply {
                                        isAccessible = true
                                    }.get(view) as? android.media.MediaPlayer
                                    mp?.playbackParams = mp?.playbackParams?.setSpeed(speed) ?: mp?.playbackParams!!
                                }
                            }
                        } catch (e: Exception) {
                            // Suppress speed modifications on unsupported OS levels
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Dimmed PIP floating card representation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RiverDarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Picture-in-Picture Active Mode", color = RiverSecondary, fontWeight = FontWeight.Bold)
                }
            }

            // High-fidelity standard controls overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Control items
                IconButton(
                    onClick = {
                        viewModel.clearActiveVideo()
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Close stream", tint = Color.White)
                }

                // Play / Pause core toggle
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentPlaybackPosition = (currentPlaybackPosition - 10).coerceAtLeast(0) }) {
                        Icon(imageVector = Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Trigger play state",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(onClick = { currentPlaybackPosition = (currentPlaybackPosition + 10).coerceAtMost(videoDurationSeconds) }) {
                        Icon(imageVector = Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                // Down-bar holding Seekbar and Speed selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDuration(currentPlaybackPosition) + " / " + formatDuration(videoDurationSeconds),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Play speed selector toggle
                            Text(
                                text = "${speed}x",
                                color = RiverSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        speed = when (speed) {
                                            1.0f -> 1.5f
                                            1.5f -> 2.0f
                                            2.0f -> 0.5f
                                            else -> 1.0f
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            )

                            // PiP trigger
                            IconButton(onClick = { isPipSimulated = !isPipSimulated }) {
                                Icon(
                                    imageVector = if (isPipSimulated) Icons.Default.PictureInPictureAlt else Icons.Default.PictureInPicture,
                                    contentDescription = "PiP",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { currentPlaybackPosition.toFloat() / videoDurationSeconds.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = RiverSecondary,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }

        // 2. Video Metadata, Comments, and Related lists
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Video Titles
                Text(
                    text = video.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${formatViews(video.views)} views • ${formatTimeAgo(video.uploadTime)}",
                        color = RiverMutedGrey,
                        fontSize = 12.sp
                    )
                }
            }

            // Interactive Actions Bar (Like, Library, Share, Flag)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button link
                    IconButtonWithText(
                        icon = Icons.Default.ThumbUp,
                        label = formatViews(video.likes + if (isLiked) 1 else 0),
                        tint = if (isLiked) RiverSecondary else Color.White,
                        onClick = { viewModel.toggleLikeVideo(video.id) }
                    )

                    // Watch later control
                    IconButtonWithText(
                        icon = Icons.Default.PlaylistAdd,
                        label = "Add",
                        tint = Color.White,
                        onClick = {
                            viewModel.toggleWatchLater(video.id)
                        }
                    )

                    // Share option
                    IconButtonWithText(
                        icon = Icons.Default.Share,
                        label = "Share",
                        tint = Color.White,
                        onClick = {
                            // Copy popup
                        }
                    )

                    // Report Flag
                    IconButtonWithText(
                        icon = Icons.Default.Flag,
                        label = "Report",
                        tint = Color.Red.copy(alpha = 0.8f),
                        onClick = {
                            viewModel.flagContent(video.id, "VIDEO", video.title, "Inappropriate metadata or copyright infringement claim.")
                            // Toast
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = RiverDarkSurfaceVariant)
            }

            // Publisher/Creator stats panel
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(RiverPrimary, RiverSecondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = video.creatorName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = video.creatorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(14.dp))
                        }
                        Text(text = "Verified Channel", color = RiverMutedGrey, fontSize = 11.sp)
                    }

                    // Subscribe action button
                    Button(
                        onClick = { viewModel.toggleSubscription(video.creatorId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubscribed) RiverDarkSurfaceVariant else RiverPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("subscribe_button_detail")
                    ) {
                        Text(
                            text = if (isSubscribed) "Unsubscribe" else "Subscribe",
                            color = if (isSubscribed) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Long description dropdown card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RiverDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Stream Specification Description", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = video.description,
                            color = OnBackgroundDark.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Interactive Comments collapsible widget
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RiverDarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCommentsExpanded = !isCommentsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thread Node Comments  (${comments.size})",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = if (isCommentsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        if (!isCommentsExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Preview the first pinned / premium comment
                            val topComment = comments.firstOrNull()
                            if (topComment != null) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(RiverSecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = topComment.userName.take(1).uppercase(), fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = topComment.content,
                                        fontSize = 12.sp,
                                        color = OnBackgroundDark.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text("No streams comments mapped. Initiate comment node!", fontSize = 11.sp, color = RiverMutedGrey)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            // Comments writing field input
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("Broadcasting point...", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("comment_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RiverSecondary,
                                        unfocusedBorderColor = RiverDarkSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotEmpty()) {
                                            viewModel.addComment(commentText)
                                            commentText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(RiverPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive comments listing
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                comments.forEach { comment ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(RiverPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = comment.userName.take(1).uppercase(), color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = comment.userName, color = RiverSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                if (comment.isPinned) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = RiverTertiary, modifier = Modifier.size(10.dp))
                                                    Text("Pinned", color = RiverTertiary, fontSize = 9.sp)
                                                }
                                            }
                                            Text(text = comment.content, fontSize = 12.sp, color = Color.White)

                                            // upvote / upvote replies
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { viewModel.likeComment(comment.id) }) {
                                                    Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(14.dp))
                                                }
                                                Text(text = "${comment.likes}", fontSize = 10.sp, color = RiverMutedGrey)

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Text(
                                                    text = "Flag",
                                                    fontSize = 11.sp,
                                                    color = Color.Red.copy(alpha = 0.6f),
                                                    modifier = Modifier.clickable {
                                                        viewModel.flagContent(comment.id, "COMMENT", comment.content, "Spam or negative stream interaction feedback.")
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
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = RiverDarkSurfaceVariant)
            }

            // Related/Recommended videos column on base
            item {
                Text(
                    text = "Flowing Related Streams",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(relatedVideos) { item ->
                RelatedVideoListItem(video = item, onClick = { viewModel.selectVideo(item) })
            }
        }
    }
}

@Composable
fun IconButtonWithText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = RiverMutedGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RelatedVideoListItem(
    video: VideoItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(110.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = video.creatorName, color = RiverSecondary, fontSize = 11.sp, maxLines = 1)
            Text(text = "${formatViews(video.views)} views", color = RiverMutedGrey, fontSize = 10.sp)
        }
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
