package com.example.data.dao

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BharamputraDao {

    // --- Channel Operations ---
    @Query("SELECT * FROM creator_channels")
    fun getAllChannels(): Flow<List<CreatorChannel>>

    @Query("SELECT * FROM creator_channels WHERE id = :id")
    fun getChannelById(id: String): Flow<CreatorChannel?>

    @Query("SELECT * FROM creator_channels WHERE id = :id")
    suspend fun getChannelByIdOneShot(id: String): CreatorChannel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: CreatorChannel)

    @Query("UPDATE creator_channels SET subscribers = :subs WHERE id = :id")
    suspend fun updateSubscribersCount(id: String, subs: Int)

    @Query("UPDATE creator_channels SET videoCount = videoCount + 1 WHERE id = :id")
    suspend fun incrementVideoCount(id: String)


    // --- Video Operations ---
    @Query("SELECT * FROM videos ORDER BY uploadTime DESC")
    fun getAllVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY uploadTime DESC")
    fun getLongVideos(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY uploadTime DESC")
    fun getShorts(): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE creatorId = :channelId ORDER BY uploadTime DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoItem>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: String): Flow<VideoItem?>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoByIdOneShot(id: String): VideoItem?

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoItem)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)

    @Query("UPDATE videos SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("UPDATE videos SET likes = likes + 1 WHERE id = :id")
    suspend fun incrementLikes(id: String)


    // --- Comments Operations ---
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY isPinned DESC, timestamp DESC")
    fun getCommentsForVideo(videoId: String): Flow<List<VideoComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: VideoComment)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)

    @Query("UPDATE comments SET isPinned = :pinned WHERE id = :commentId")
    suspend fun updateCommentPinStatus(commentId: String, pinned: Boolean)

    @Query("UPDATE comments SET likes = likes + 1 WHERE id = :commentId")
    suspend fun incrementCommentLikes(commentId: String)


    // --- Watch History ---
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getWatchHistoryRaw(): Flow<List<UserHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: UserHistory)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()


    // --- Watch Later ---
    @Query("SELECT * FROM watch_later ORDER BY addedAt DESC")
    fun getWatchLaterRaw(): Flow<List<UserWatchLater>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchLater(watchLater: UserWatchLater)

    @Query("DELETE FROM watch_later WHERE videoId = :videoId")
    suspend fun deleteWatchLater(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE videoId = :videoId)")
    fun isWatchLater(videoId: String): Flow<Boolean>


    // --- Playlists ---
    @Query("SELECT * FROM user_playlists")
    fun getAllPlaylists(): Flow<List<UserPlaylist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: UserPlaylist)

    @Query("DELETE FROM user_playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)


    // --- Flagged Moderation ---
    @Query("SELECT * FROM reported_content ORDER BY reportedAt DESC")
    fun getReportedContent(): Flow<List<ReportedContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun reportContent(reported: ReportedContent)

    @Query("DELETE FROM reported_content WHERE id = :id")
    suspend fun dismissReport(id: String)
}
