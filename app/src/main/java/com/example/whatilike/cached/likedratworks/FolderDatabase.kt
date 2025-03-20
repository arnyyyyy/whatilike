package com.example.whatilike.cached.user

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Folder::class], version = 1, exportSchema = false)
@TypeConverters(ConverterFolder::class)
abstract class ArtworkFoldersDatabase : RoomDatabase() {
    abstract fun folders(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: ArtworkFoldersDatabase? = null

        fun getInstance(context: Context): ArtworkFoldersDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArtworkFoldersDatabase::class.java,
                    "artworks_folders_database"
                )
//                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
