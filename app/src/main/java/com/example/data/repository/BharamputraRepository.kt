package com.example.data.repository

import com.example.data.dao.BharamputraDao
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class BharamputraRepository(private val dao: BharamputraDao) {

    val allChannels: Flow<List<CreatorChannel>> = dao.getAllChannels()
    val allVideos: Flow<List<VideoItem>> = dao.getAllVideos()
    val longVideos: Flow<List<VideoItem>> = dao.getLongVideos()
    val shorts: Flow<List<VideoItem>> = dao.getShorts()
    val playLists: Flow<List<UserPlaylist>> = dao.getAllPlaylists()
    val reportedContent: Flow<List<ReportedContent>> = dao.getReportedContent()

    fun getChannelById(id: String): Flow<CreatorChannel?> = dao.getChannelById(id)
    fun getVideoById(id: String): Flow<VideoItem?> = dao.getVideoById(id)
    fun getVideosByChannel(channelId: String): Flow<List<VideoItem>> = dao.getVideosByChannel(channelId)
    fun searchVideos(query: String): Flow<List<VideoItem>> = dao.searchVideos(query)
    fun getCommentsForVideo(videoId: String): Flow<List<VideoComment>> = dao.getCommentsForVideo(videoId)
    fun isWatchLater(videoId: String): Flow<Boolean> = dao.isWatchLater(videoId)

    // --- Complex Joint Flow for Watch History ---
    val watchHistory: Flow<List<Pair<UserHistory, VideoItem>>> = dao.getWatchHistoryRaw()
        .combine(dao.getAllVideos()) { rawHistory, videos ->
            val videoMap = videos.associateBy { it.id }
            rawHistory.mapNotNull { history ->
                val video = videoMap[history.videoId]
                if (video != null) Pair(history, video) else null
            }
        }

    // --- Complex Joint Flow for Watch Later ---
    val watchLater: Flow<List<VideoItem>> = dao.getWatchLaterRaw()
        .combine(dao.getAllVideos()) { rawLater, videos ->
            val videoMap = videos.associateBy { it.id }
            rawLater.mapNotNull { later -> videoMap[later.videoId] }
        }

    // --- Suspended Single Operations ---
    suspend fun insertChannel(channel: CreatorChannel) = dao.insertChannel(channel)
    suspend fun insertVideo(video: VideoItem) = dao.insertVideo(video)
    suspend fun incrementViews(videoId: String) = dao.incrementViews(videoId)
    suspend fun incrementLikes(videoId: String) = dao.incrementLikes(videoId)
    suspend fun updateSubscribersCount(channelId: String, count: Int) = dao.updateSubscribersCount(channelId, count)
    suspend fun incrementVideoCount(channelId: String) = dao.incrementVideoCount(channelId)

    suspend fun insertComment(comment: VideoComment) = dao.insertComment(comment)
    suspend fun deleteComment(commentId: String) = dao.deleteComment(commentId)
    suspend fun updateCommentPinStatus(commentId: String, pinned: Boolean) = dao.updateCommentPinStatus(commentId, pinned)
    suspend fun incrementCommentLikes(commentId: String) = dao.incrementCommentLikes(commentId)

    suspend fun insertHistory(videoId: String) = dao.insertHistory(UserHistory(videoId = videoId))
    suspend fun clearHistory() = dao.clearHistory()

    suspend fun toggleWatchLater(videoId: String, currentlyExists: Boolean) {
        if (currentlyExists) {
            dao.deleteWatchLater(videoId)
        } else {
            dao.insertWatchLater(UserWatchLater(videoId = videoId))
        }
    }

    suspend fun deleteVideo(videoId: String) = dao.deleteVideo(videoId)

    // --- Playlist Operations ---
    suspend fun createPlaylist(name: String, description: String, initialVideoId: String? = null) {
        val id = "playlist_${System.currentTimeMillis()}"
        val videoIds = initialVideoId ?: ""
        dao.insertPlaylist(UserPlaylist(id, name, description, videoIds))
    }

    suspend fun addVideoToPlaylist(playlistId: String, videoId: String) {
        val playlists = dao.getAllPlaylists().first()
        val playlist = playlists.find { it.id == playlistId } ?: return
        val currentIds = playlist.videoIds.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!currentIds.contains(videoId)) {
            currentIds.add(videoId)
            dao.insertPlaylist(playlist.copy(videoIds = currentIds.joinToString(",")))
        }
    }

    suspend fun deletePlaylist(id: String) = dao.deletePlaylist(id)

    // --- Flagging/Moderation ---
    suspend fun reportVideo(videoId: String, title: String, reason: String) {
        dao.reportContent(ReportedContent(id = videoId, contentType = "VIDEO", titleOrContent = title, reportReason = reason))
    }

    suspend fun reportComment(commentId: String, content: String, reason: String) {
        dao.reportContent(ReportedContent(id = commentId, contentType = "COMMENT", titleOrContent = content, reportReason = reason))
    }

    suspend fun dismissReport(id: String) = dao.dismissReport(id)

    // --- Seed Data Setup ---
    suspend fun seedDatabaseIfEmpty() {
        // Only seed if empty
        val existingVideos = dao.getAllVideos().first()
        if (existingVideos.isNotEmpty()) return

        // 1. Seed standard creators
        val channels = listOf(
            CreatorChannel(
                id = "bharamputra_official",
                name = "Bharamputra Media",
                handle = "@bharamputra",
                avatarUrl = "river_shield", // Placeholders mapped in app Icons
                bannerUrl = "river_wide",
                bio = "Welcome to the grand river of premium original content. Flowing with tech, cinema, music, and local high-resolution creative narratives.",
                subscribers = 1250000,
                isVerified = true,
                videoCount = 4
            ),
            CreatorChannel(
                id = "assam_explorer",
                name = "Assam Explorations",
                handle = "@explorer_ne",
                avatarUrl = "face_explorer",
                bannerUrl = "majuli_green",
                bio = "Documenting the untamed landscapes, wild sanctuaries, local folklore, and ancient river heritage of Northeast India.",
                subscribers = 43000,
                isVerified = false,
                videoCount = 2
            ),
            CreatorChannel(
                id = "vedic_coding",
                name = "Vedic Tech Labs",
                handle = "@vedic_tech",
                avatarUrl = "face_tech",
                bannerUrl = "neon_matrix",
                bio = "Jetpack Compose, Kotlin, serverless systems, and future-forward cross-platform development simplified.",
                subscribers = 98000,
                isVerified = true,
                videoCount = 2
            ),
            CreatorChannel(
                id = "northeast_rythms",
                name = "NE Music Rythms",
                handle = "@ne_beats",
                avatarUrl = "headphones_gold",
                bannerUrl = "stage_lights",
                bio = "Bringing you heavy bamboo bass, ethnic instrumental fuses, and modern electronic river frequencies.",
                subscribers = 512000,
                isVerified = true,
                videoCount = 2
            )
        )

        channels.forEach { dao.insertChannel(it) }

        // 2. Seed High-quality playable videos
        val videos = listOf(
            // --- Long Videos ---
            VideoItem(
                id = "bbb_stream",
                title = "Big Buck Bunny - 4K Ultra Cinematic Stream",
                description = "Experience the classic open-source animated tale of a large, gentle rabbit who takes comedic revenge on three cheeky forest rodents. Masterfully encoded in multiple bitrate structures for adaptive performance.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&q=80&w=720", // Stunning high-quality placeholder URL from unsplash
                creatorId = "bharamputra_official",
                creatorName = "Bharamputra Media",
                creatorAvatar = "river_shield",
                views = 428930,
                likes = 12903,
                uploadTime = System.currentTimeMillis() - 86400000 * 3, // 3 days ago
                duration = "9:56",
                isShort = false,
                category = "Cinema",
                tags = "animation,4k,bunny,shorts,bharamputra"
            ),
            VideoItem(
                id = "sintel_saga",
                title = "Sintel - Cinematic Story of a Warrior & Dragon",
                description = "A powerful, beautifully touching fantasy action story of a young lone girl searching for her baby dragon comrade. Excellent high-bitrate adaptive streaming illustrating deep cinematic colors on Bharamputra playback engines.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&q=80&w=720",
                creatorId = "bharamputra_official",
                creatorName = "Bharamputra Media",
                creatorAvatar = "river_shield",
                views = 981102,
                likes = 84092,
                uploadTime = System.currentTimeMillis() - 86400000 * 7, // 7 days ago
                duration = "14:48",
                isShort = false,
                category = "Cinema",
                tags = "sintel,fantasy,dragon,adventure,cgi"
            ),
            VideoItem(
                id = "tears_of_steel",
                title = "Tears of Steel - Sci-Fi VFX Showcase",
                description = "Exploring the cosmic cybernetic ruins of Amsterdam where a group of elite scientists seek to rescue the universe using giant robotic machinery and particle beams. A classic high-performance adaptive streaming demonstration.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=720",
                creatorId = "bharamputra_official",
                creatorName = "Bharamputra Media",
                creatorAvatar = "river_shield",
                views = 239010,
                likes = 18933,
                uploadTime = System.currentTimeMillis() - 86400000 * 1, // 1 day ago
                duration = "12:14",
                isShort = false,
                category = "Sci-Fi",
                tags = "scifi,vfx,robots,amsterdam,cinematic"
            ),
            VideoItem(
                id = "nest_explorer",
                title = "Unexplored Wilds: Journey into Deep Majuli",
                description = "Take an exclusive cinematic journey into Majuli, the world's largest river island located on the Brahmaputra River. Meet local craftspeople of riverboats, explore lush forests, and capture the sunrise over local waterways.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=720",
                creatorId = "assam_explorer",
                creatorName = "Assam Explorations",
                creatorAvatar = "face_explorer",
                views = 42000,
                likes = 3108,
                uploadTime = System.currentTimeMillis() - 86400000 * 5,
                duration = "10:53",
                isShort = false,
                category = "Riverine",
                tags = "majuli,assam,river,brahmaputra,unesco,travel"
            ),
            VideoItem(
                id = "compose_m3_masterclass",
                title = "Jetpack Compose M3 Layouts & Adaptive Rails",
                description = "In this complete technical walk-through, we break down exactly how to construct fluid Material 3 layouts, enable edge-to-edge screens with window insets, build side navigation rails, and deliver gorgeous dark layouts in Android.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&q=80&w=720",
                creatorId = "vedic_coding",
                creatorName = "Vedic Tech Labs",
                creatorAvatar = "face_tech",
                views = 129402,
                likes = 10408,
                uploadTime = System.currentTimeMillis() - 86400000 * 2,
                duration = "9:56",
                isShort = false,
                category = "Tech",
                tags = "compose,android,kotlin,m3,development"
            ),
            VideoItem(
                id = "bihu_rhythm_beats",
                title = "Bihu Rhythm Beats - Folk Fusion Stream",
                description = "Authentic Bihu folk instruments blended with modern ambient electronic basslines. Perfect for a premium audio visualization of Northeast India's soul.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1548504769-900b70ed122e?auto=format&fit=crop&q=80&w=720",
                creatorId = "northeast_rythms",
                creatorName = "NE Music Rythms",
                creatorAvatar = "headphones_gold",
                views = 153090,
                likes = 12903,
                uploadTime = System.currentTimeMillis() - 86400000 * 4,
                duration = "4:32",
                isShort = false,
                category = "Music",
                tags = "music,bihu,folk,electronic,rythms"
            ),
            VideoItem(
                id = "river_ambient_lofi",
                title = "Raindrops over Brahmaputra Basin - Ambient Lo-Fi",
                description = "Relax and unwind with soft, earthy rain sounds overlaid with beautiful bamboo flute melodies. Processed dynamically into premium stream-ready audio formats.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?auto=format&fit=crop&q=80&w=720",
                creatorId = "northeast_rythms",
                creatorName = "NE Music Rythms",
                creatorAvatar = "headphones_gold",
                views = 289412,
                likes = 34098,
                uploadTime = System.currentTimeMillis() - 86400000 * 8,
                duration = "5:12",
                isShort = false,
                category = "Music",
                tags = "music,lofi,relax,rain,instrumental"
            ),
            VideoItem(
                id = "vedic_cyber_techno",
                title = "Vedic Tech Anthem - Cyber Matrix Echoes",
                description = "Dynamic synthwave chords, digital soundscapes, and driving cyber tempos. Dedicated soundtrack for late-night Kotlin hacking sessions.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1614149162883-504ce4d13909?auto=format&fit=crop&q=80&w=720",
                creatorId = "vedic_coding",
                creatorName = "Vedic Tech Labs",
                creatorAvatar = "face_tech",
                views = 89040,
                likes = 7801,
                uploadTime = System.currentTimeMillis() - 86400000 * 1,
                duration = "3:45",
                isShort = false,
                category = "Music",
                tags = "music,synthwave,cyber,techno,coding"
            ),

            // --- Shorts (Using 3-4 minute clip loops but rendering vertically) ---
            VideoItem(
                id = "short_blazes",
                title = "Crazy Bamboo Fire Festival 🎋🔥 #shorts",
                description = "Running through the bamboo fires of tribal dance under full moon night in rural Assam! Intensely epic adrenaline loop.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?auto=format&fit=crop&q=80&w=360",
                creatorId = "assam_explorer",
                creatorName = "Assam Explorations",
                creatorAvatar = "face_explorer",
                views = 892100,
                likes = 78200,
                uploadTime = System.currentTimeMillis() - 3600000 * 12, // 12 hours ago
                duration = "0:15",
                isShort = true,
                category = "Shorts",
                tags = "shorts,assam,fire,festival,adrenaline"
            ),
            VideoItem(
                id = "short_escapes",
                title = "Speed Boat River Race Challenge! 🛥️💦",
                description = "Absolute high speed maneuvers during river flooding championships on the Brahmaputra basin. Insane splash waves!",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&q=80&w=360",
                creatorId = "bharamputra_official",
                creatorName = "Bharamputra Media",
                creatorAvatar = "river_shield",
                views = 1204000,
                likes = 143800,
                uploadTime = System.currentTimeMillis() - 3600000 * 4,
                duration = "0:14",
                isShort = true,
                category = "Shorts",
                tags = "shorts,boat,race,river,speed"
            ),
            VideoItem(
                id = "short_fun",
                title = "Electric Guitar Bamboo Fusion Solo 🎸🎋",
                description = "Slapping the heavy strings on the bamboo-crafted bass hybrid! Flowing rhythms direct from the stage of NE Music Festival.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&q=80&w=360",
                creatorId = "northeast_rythms",
                creatorName = "NE Music Rythms",
                creatorAvatar = "headphones_gold",
                views = 439000,
                likes = 31908,
                uploadTime = System.currentTimeMillis() - 3600000 * 22,
                duration = "0:15",
                isShort = true,
                category = "Shorts",
                tags = "shorts,music,guitar,instrument,ethnic"
            ),
            VideoItem(
                id = "short_joyrides",
                title = "Fastest Serverless Coding Record 🚀💻",
                description = "Watch me deploy a fully scalable custom video analytics webhook to Firebase Cloud Functions in 14 seconds flat. Native Kotlin syntax compiler speed test!",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&q=80&w=360",
                creatorId = "vedic_coding",
                creatorName = "Vedic Tech Labs",
                creatorAvatar = "face_tech",
                views = 980000,
                likes = 94800,
                uploadTime = System.currentTimeMillis() - 3600000 * 2,
                duration = "0:15",
                isShort = true,
                category = "Shorts",
                tags = "shorts,tech,coding,fast,firebase"
            )
        )

        videos.forEach { dao.insertVideo(it) }

        // 3. Seed some initial comments for Big Buck Bunny
        val initialComments = listOf(
            VideoComment(
                id = "seed_comment_1",
                videoId = "bbb_stream",
                userName = "Sneha Bora",
                userAvatar = "face_sneha",
                content = "Bharamputra's adaptive playback is incredibly smooth! Barely any buffer compared to standard platforms in rural areas. Kudos to the engineering team. 🌊🙌",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                likes = 342,
                isPinned = true
            ),
            VideoComment(
                id = "seed_comment_2",
                videoId = "bbb_stream",
                userName = "Pritam Das",
                userAvatar = "face_pritam",
                content = "Subscribed instantly! Can we please get a detailed series about Majuli boating and regional vessel craftsmanship?",
                timestamp = System.currentTimeMillis() - 3600000 * 20,
                likes = 89,
                isPinned = false
            ),
            VideoComment(
                id = "seed_reply_1",
                videoId = "bbb_stream",
                userName = "Assam Explorations",
                userAvatar = "face_explorer",
                content = "Absolutely Pritam! We have started filming our upcoming 4-part series detailing exact construction masterclasses. Staying tuned!",
                timestamp = System.currentTimeMillis() - 3600000 * 18,
                likes = 42,
                isPinned = false,
                replyToId = "seed_comment_2"
            )
        )

        initialComments.forEach { dao.insertComment(it) }
    }
}
