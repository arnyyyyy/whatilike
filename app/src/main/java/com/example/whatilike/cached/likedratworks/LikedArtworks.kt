package com.example.whatilike.cached.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLikedArtworksIds(likedArtworksIds: List<Int>?): String? {
        return likedArtworksIds?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toLikedArtworksIds(likedArtworksIdsString: String?): List<Int>? {
        return likedArtworksIdsString?.let {
            val listType = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }
}

@Entity(tableName = "liked_artworks")
data class LikedArtworks(
    @PrimaryKey val uid: String,
    val likedArtworksIds : List<Int>
)
