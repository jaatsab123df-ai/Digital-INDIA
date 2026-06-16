package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BharamputraDao
import com.example.data.models.*

@Database(
    entities = [
        CreatorChannel::class,
        VideoItem::class,
        VideoComment::class,
        UserHistory::class,
        UserWatchLater::class,
        UserPlaylist::class,
        ReportedContent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BharamputraDatabase : RoomDatabase() {

    abstract fun bharamputraDao(): BharamputraDao

    companion object {
        @Volatile
        private var INSTANCE: BharamputraDatabase? = null

        fun getDatabase(context: Context): BharamputraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BharamputraDatabase::class.java,
                    "bharamputra_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
