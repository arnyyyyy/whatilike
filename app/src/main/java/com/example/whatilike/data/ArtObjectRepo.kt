package com.example.whatilike.repository

import android.util.Log
import com.example.whatilike.data.ApiDatabase
import com.example.whatilike.data.ArtObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArtRepository {
    private val api = ApiDatabase.apiService

    suspend fun getRandomArtworks(count: Int = 15): List<ArtObject> = withContext(Dispatchers.IO) {
        val response = api.getAllObjectIDs()
        if (response.isSuccessful && response.body() != null) {
            val ids = response.body()?.objectIDs ?: emptyList()
            Log.d("ArtRepository", "Total Object IDs fetched: ${ids.size}")

            if (ids.isNotEmpty()) {
                val randomIds = ids.shuffled().take(count)

                val artworks = randomIds.mapNotNull { id ->
                    val artResponse = api.getObjectByID(id)
                    val artObject = artResponse.body()
                    if (artObject != null && !artObject.primaryImage.isNullOrEmpty()) {
                        artObject
                    } else null
                }

                Log.d("ArtRepository", "Artworks retrieved: ${artworks.size}")
                artworks
            } else {
                Log.e("ArtRepository", "No object IDs found")
                emptyList()
            }
        } else {
            Log.e("ArtRepository", "Failed to fetch object IDs: ${response.code()}")
            emptyList()
        }
    }

    suspend fun getArtworksByIds(ids: List<Int>): List<ArtObject> = withContext(Dispatchers.IO) {
        ids.mapNotNull { id ->
            val response = api.getObjectByID(id)
            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                Log.e("ArtRepository", "Failed to fetch artwork with ID $id: ${response.code()}")
                null
            }
        }
    }
}
