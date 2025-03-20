package com.example.whatilike.cached.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LikedArtworksDao {
    @Query("SELECT * FROM liked_artworks WHERE uid = :uid LIMIT 1")
    suspend fun getUserLikedArtworks(uid: String): LikedArtworks?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLikedArtworks(likedArtworks: LikedArtworks)
}

