package com.example.whatilike.cached.user

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverters


@Database(entities = [LikedArtworks::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LikedArtworksDatabase : RoomDatabase() {
    abstract fun likedArtworks(): LikedArtworksDao

    companion object {
        @Volatile
        private var INSTANCE: LikedArtworksDatabase? = null

        fun getInstance(context: Context): LikedArtworksDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LikedArtworksDatabase::class.java,
                    "liked_artworks_database"
                )
//                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
