package com.example.whatilike.repository

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.whatilike.cached.artworks.ArtDatabase
import com.example.whatilike.cached.artworks.CachedArtwork
import com.example.whatilike.cached.artworks.CachedArtworkDao
import com.example.whatilike.data.ApiDatabase
import com.example.whatilike.data.ArtObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ArtRepository(private val context: Context, private val cachedArtworkDao: CachedArtworkDao) {
    private val api = ApiDatabase.apiService
    private val imageLoader = ImageLoader(context)

    suspend fun removeArtworkFromCache(artwork: ArtObject) {
        withContext(Dispatchers.IO) {
            val cachedArtwork = CachedArtwork(
                objectID = artwork.objectID,
                title = artwork.title,
                artistDisplayName = artwork.artistDisplayName,
                primaryImage = artwork.primaryImage ?: "",
                primaryImageSmall = artwork.primaryImageSmall ?: ""
            )
            cachedArtworkDao.deleteArtwork(cachedArtwork)
        }
    }

    suspend fun getRandomArtworks(count: Int = 15): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val cachedArtworks =
                cachedArtworkDao.getCachedArtworks(count / 2 + 1).first()
            val alreadyLoadedIDs = cachedArtworks.map { it.objectID }.toSet()

            val newArtworks =
                fetchArtworksFromApi(count).filterNot { alreadyLoadedIDs.contains(it.objectID) }

            if (newArtworks.isNotEmpty()) {
                cacheAndPreloadImages(newArtworks)
            }

            val combinedArtworks = cachedArtworks.map { cached ->
                ArtObject(
                    objectID = cached.objectID,
                    title = cached.title,
                    artistDisplayName = cached.artistDisplayName,
                    primaryImage = cached.primaryImage,
                    primaryImageSmall = cached.primaryImageSmall
                )
            } + newArtworks

            Log.d("ArtRepository", "Returning combined artworks: ${combinedArtworks.size}")
            return@withContext combinedArtworks
        }


    fun cacheAndPreloadImages(newArtworks: List<ArtObject>) {
        val cachedArtworks = newArtworks.map { artwork ->
            CachedArtwork(
                objectID = artwork.objectID,
                title = artwork.title,
                artistDisplayName = artwork.artistDisplayName,
                primaryImage = artwork.primaryImage ?: "",
                primaryImageSmall = artwork.primaryImageSmall ?: ""
            )
        }
        cachedArtworkDao.insertArtworks(cachedArtworks)

        newArtworks.forEach { artwork ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(artwork.primaryImage)
                    .build()
            )
        }

        Log.d("ArtRepository", "New artworks cached and images preloaded.")
    }

    private suspend fun fetchArtworksFromApi(count: Int): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val response = api.getAllObjectIDs()
            if (response.isSuccessful && response.body() != null) {
                val ids = response.body()?.objectIDs ?: emptyList()
                Log.d("ArtRepository", "Total Object IDs fetched: ${ids.size}")

                val randomIds = ids.shuffled().take(count)

                val deferredArtworks = randomIds.map { id ->
                    async {
                        val artResponse = api.getObjectByID(id)
                        val artObject = artResponse.body()
                        if (artObject != null && !artObject.primaryImage.isNullOrEmpty()) artObject else null
                    }
                }

                val artworks = deferredArtworks.awaitAll().filterNotNull()
                Log.d("ArtRepository", "Artworks retrieved from API: ${artworks.size}")
                return@withContext artworks
            } else {
                Log.e("ArtRepository", "Failed to fetch object IDs: ${response.code()}")
                return@withContext emptyList<ArtObject>()
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

class ArtRepositoryFactory(
    private val context: Context
) {
    fun create(): ArtRepository {
        val cachedArtworkDao = ArtDatabase.getInstance(context).cachedArtworkDao()
        return ArtRepository(context, cachedArtworkDao)
    }
}
