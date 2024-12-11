package com.example.whatilike.cached.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConverterFolder {
    private val gson = Gson()

    @TypeConverter
    fun fromFolderArtworkIds(artworkIds: List<Int>?): String? {
        return artworkIds?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFolderArtworkIds(artworkIdsString: String?): List<Int>? {
        return artworkIdsString?.let {
            val listType = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }
}

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val folderId: String,
    val uid: String,
    val name: String,
    val artworkIds : List<Int>
)