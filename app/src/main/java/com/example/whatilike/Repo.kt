package com.example.whatilike.repository

import android.util.Log
import com.example.whatilike.api.ApiClient
import com.example.whatilike.api.ArtObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ArtRepository {
    private val api = ApiClient.apiService

    suspend fun getRandomArtworks(count: Int = 5): List<ArtObject> = withContext(Dispatchers.IO) {
        val response = api.getAllObjectIDs()
        if (response.isSuccessful && response.body() != null) {
            val ids = response.body()?.objectIDs ?: emptyList()
            Log.d("ArtRepository", "Total Object IDs fetched: ${ids.size}")

            // Проверяем, что список не пустой
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
}
