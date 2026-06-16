package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BharamputraDatabase
import com.example.data.auth.BharamputraAuth
import com.example.data.auth.UserAccount
import com.example.data.gemini.BharamputraGeminiClient
import com.example.data.models.*
import com.example.data.repository.BharamputraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BharamputraViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BharamputraDatabase.getDatabase(application)
    private val repository = BharamputraRepository(database.bharamputraDao())
    private val authManager = BharamputraAuth(application)
    private val geminiClient = BharamputraGeminiClient()

    // --- Authentication States ---
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // --- Media & Library States ---
    val allVideos: StateFlow<List<VideoItem>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val longVideos: StateFlow<List<VideoItem>> = repository.longVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shorts: StateFlow<List<VideoItem>> = repository.shorts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChannels: StateFlow<List<CreatorChannel>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<UserPlaylist>> = repository.playLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchLater: StateFlow<List<VideoItem>> = repository.watchLater
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<Pair<UserHistory, VideoItem>>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedContent: StateFlow<List<ReportedContent>> = repository.reportedContent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI/Navigation Control ---
    private val _currentRoute = MutableStateFlow("splash")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activePlayingVideo = MutableStateFlow<VideoItem?>(null)
    val activePlayingVideo: StateFlow<VideoItem?> = _activePlayingVideo.asStateFlow()

    val commentsForCurrentVideo: StateFlow<List<VideoComment>> = _activePlayingVideo
        .flatMapLatest { video ->
            if (video != null) repository.getCommentsForVideo(video.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Metadata Generation ---
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Subscriptions Tracking ---
    private val _subscribedChannelIds = MutableStateFlow<Set<String>>(emptySet())
    val subscribedChannelIds: StateFlow<Set<String>> = _subscribedChannelIds.asStateFlow()

    // --- Liked Video Tracking ---
    private val _likedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    val likedVideoIds: StateFlow<Set<String>> = _likedVideoIds.asStateFlow()

    init {
        // Hydrate User Session
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            val savedUser = authManager.getPersistedUser()
            if (savedUser != null) {
                _currentUser.value = savedUser
                // Let's seed initial subscriptions for demo
                _subscribedChannelIds.value = setOf("bharamputra_official", "vedic_coding")
            }
        }
    }

    // --- Navigation ---
    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    fun selectVideo(video: VideoItem) {
        _activePlayingVideo.value = video
        viewModelScope.launch {
            repository.incrementViews(video.id)
            repository.insertHistory(video.id)
        }
        navigateTo("player")
    }

    fun clearActiveVideo() {
        _activePlayingVideo.value = null
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Authentication ---
    fun registerWithEmail(email: String, name: String, phone: String = "") {
        if (!authManager.validateEmail(email)) {
            _authError.value = "Invalid email format"
            return
        }
        _isAuthLoading.value = true
        viewModelScope.launch {
            val shortUser = name.lowercase().replace(" ", "_")
            val dummyId = "usr_${System.currentTimeMillis()}"
            val newAcct = UserAccount(
                id = dummyId,
                email = email,
                name = name,
                handle = "@$shortUser",
                role = "ADMIN", // Elevate session to ADMIN for platform flexibility
                avatarUrl = "river_shield",
                phoneNumber = phone
            )
            authManager.persistUser(newAcct)
            _currentUser.value = newAcct
            _authError.value = null
            _isAuthLoading.value = false
            navigateTo("main")
        }
    }

    fun loginWithEmail(email: String) {
        if (!authManager.validateEmail(email)) {
            _authError.value = "Invalid email format"
            return
        }
        _isAuthLoading.value = true
        viewModelScope.launch {
            val dummyId = "usr_${System.currentTimeMillis()}"
            val shortEmail = email.substringBefore("@")
            val acct = UserAccount(
                id = dummyId,
                email = email,
                name = shortEmail.replaceFirstChar { it.uppercase() },
                handle = "@$shortEmail",
                role = "ADMIN", // Elevate to ADMIN so user can Moderation and Sandbox
                avatarUrl = "river_shield"
            )
            authManager.persistUser(acct)
            _currentUser.value = acct
            _authError.value = null
            _isAuthLoading.value = false
            navigateTo("main")
        }
    }

    fun logout() {
        authManager.clearSession()
        _currentUser.value = null
        _subscribedChannelIds.value = emptySet()
        _likedVideoIds.value = emptySet()
        navigateTo("auth")
    }

    // --- Subscriptions ---
    fun toggleSubscription(channelId: String) {
        viewModelScope.launch {
            val currentList = _subscribedChannelIds.value.toMutableSet()
            val channels = repository.allChannels.first()
            val channel = channels.find { it.id == channelId } ?: return@launch

            if (currentList.contains(channelId)) {
                currentList.remove(channelId)
                repository.updateSubscribersCount(channelId, (channel.subscribers - 1).coerceAtLeast(0))
            } else {
                currentList.add(channelId)
                repository.updateSubscribersCount(channelId, channel.subscribers + 1)
            }
            _subscribedChannelIds.value = currentList
        }
    }

    // --- Likes/Dislikes ---
    fun toggleLikeVideo(videoId: String) {
        viewModelScope.launch {
            val current = _likedVideoIds.value.toMutableSet()
            if (current.contains(videoId)) {
                current.remove(videoId)
            } else {
                current.add(videoId)
                repository.incrementLikes(videoId)
            }
            _likedVideoIds.value = current
        }
    }

    // --- Library Controls ---
    fun toggleWatchLater(videoId: String) {
        viewModelScope.launch {
            val list = repository.watchLater.first()
            val alreadyAdded = list.any { it.id == videoId }
            repository.toggleWatchLater(videoId, alreadyAdded)
        }
    }

    fun createPlaylist(name: String, description: String, initialVideoId: String? = null) {
        viewModelScope.launch {
            repository.createPlaylist(name, description, initialVideoId)
        }
    }

    fun addVideoToPlaylist(playlistId: String, videoId: String) {
        viewModelScope.launch {
            repository.addVideoToPlaylist(playlistId, videoId)
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Channel Customizer & Video Uploads ---
    fun createChannel(name: String, bio: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val channelId = "chan_${user.id}"
            val newChannel = CreatorChannel(
                id = channelId,
                name = name,
                handle = user.handle,
                avatarUrl = user.avatarUrl,
                bannerUrl = "blue_gradient",
                bio = bio,
                subscribers = 0,
                isVerified = false,
                videoCount = 0
            )
            repository.insertChannel(newChannel)
            // Update local user if needed, navigate to main
            navigateTo("main")
        }
    }

    fun uploadVideo(
        title: String,
        description: String,
        videoUrl: String,
        selectedThumbnailUrl: String?,
        category: String,
        tags: String,
        isShort: Boolean,
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val thumbnail = selectedThumbnailUrl ?: "https://images.unsplash.com/photo-1516280440614-37939bbacd6a?auto=format&fit=crop&q=80&w=720"
            val videoId = "vid_${System.currentTimeMillis()}"
            val finalVideoUrl = videoUrl.ifEmpty { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" }
            val videoItem = VideoItem(
                id = videoId,
                title = title,
                description = description,
                videoUrl = finalVideoUrl,
                thumbnailUrl = thumbnail,
                creatorId = "bharamputra_official", // default link inside seed creators for visibility
                creatorName = user.name,
                creatorAvatar = user.avatarUrl,
                views = 0,
                likes = 0,
                uploadTime = System.currentTimeMillis(),
                duration = if (isShort) "0:15" else "9:56",
                isShort = isShort,
                category = category,
                tags = tags
            )
            repository.insertVideo(videoItem)
            repository.incrementVideoCount("bharamputra_official")
            onComplete()
        }
    }

    // Gemini Metadata Enhancement
    fun enhanceWithGemini(
        draftTitle: String,
        draftDescription: String,
        category: String,
        onResult: (enhancedTitle: String, enhancedDesc: String, tags: String) -> Unit
    ) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val result = geminiClient.enhanceVideoMetadata(draftTitle, draftDescription, category)
            _isAiLoading.value = false
            onResult(result.first, result.second, result.third)
        }
    }

    // --- Comments ---
    fun addComment(content: String) {
        val video = _activePlayingVideo.value ?: return
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val newComment = VideoComment(
                id = "cmt_${System.currentTimeMillis()}",
                videoId = video.id,
                userName = user.name,
                userAvatar = user.avatarUrl,
                content = content,
                timestamp = System.currentTimeMillis(),
                likes = 0,
                isPinned = false
            )
            repository.insertComment(newComment)
        }
    }

    fun pinComment(commentId: String, currentPin: Boolean) {
        viewModelScope.launch {
            repository.updateCommentPinStatus(commentId, !currentPin)
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch {
            repository.incrementCommentLikes(commentId)
        }
    }

    fun flagContent(id: String, contentType: String, content: String, reason: String) {
        viewModelScope.launch {
            if (contentType == "VIDEO") {
                repository.reportVideo(id, content, reason)
            } else {
                repository.reportComment(id, content, reason)
            }
        }
    }

    // --- Admin Commands ---
    fun adminDeleteVideo(videoId: String) {
        viewModelScope.launch {
            repository.deleteVideo(videoId)
            repository.dismissReport(videoId)
        }
    }

    fun adminDeleteComment(commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(commentId)
            repository.dismissReport(commentId)
        }
    }

    fun adminDismissReport(id: String) {
        viewModelScope.launch {
            repository.dismissReport(id)
        }
    }
}
