package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "creator_channels")
data class CreatorChannel(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val bio: String,
    val subscribers: Int,
    val isVerified: Boolean,
    val videoCount: Int
) : Serializable

@Entity(tableName = "videos")
data class VideoItem(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val views: Long,
    val likes: Long,
    val uploadTime: Long,
    val duration: String,
    val isShort: Boolean,
    val category: String, // Riverine, Tech, Music, Gaming, Sports, Shorts
    val tags: String, // comma-separated
    val privacy: String = "PUBLIC" // PUBLIC, PRIVATE, UNLISTED
) : Serializable

@Entity(tableName = "comments")
data class VideoComment(
    @PrimaryKey val id: String,
    val videoId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val timestamp: Long,
    val likes: Int,
    val isPinned: Boolean,
    val replyToId: String? = null // parent comment ID for nested replies
) : Serializable

@Entity(tableName = "watch_history")
data class UserHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_later")
data class UserWatchLater(
    @PrimaryKey val videoId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_playlists")
data class UserPlaylist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val videoIds: String // Comma-separated video IDs
)

@Entity(tableName = "reported_content")
data class ReportedContent(
    @PrimaryKey val id: String, // same as videoId or commentId
    val contentType: String, // VIDEO, COMMENT
    val titleOrContent: String, // display preview
    val reportReason: String,
    val reportedAt: Long = System.currentTimeMillis()
)
