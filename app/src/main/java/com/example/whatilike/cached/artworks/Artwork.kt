package com.example.whatilike.cached.artworks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_artworks")
data class CachedArtwork(
    @PrimaryKey val objectID: Int,
    val title: String,
    val artistDisplayName: String,
    val primaryImage: String,
    val primaryImageSmall: String,
    val isCached: Boolean = false
)
