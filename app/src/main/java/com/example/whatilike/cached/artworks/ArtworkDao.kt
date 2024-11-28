package com.example.whatilike.cached.artworks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CachedArtworkDao {
    @Query("SELECT * FROM cached_artworks LIMIT :count")
    fun getCachedArtworks(count: Int): List<CachedArtwork>

    @Query("SELECT COUNT(*) FROM cached_artworks")
    suspend fun getArtworkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtworks(artworks: List<CachedArtwork>)

    @Update
    fun updateArtwork(artwork: CachedArtwork)

    @Delete
    suspend fun deleteArtwork(artwork: CachedArtwork)
}
