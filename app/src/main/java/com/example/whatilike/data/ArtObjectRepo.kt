package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.whatilike.cached.artworks.ArtDatabase
import com.example.whatilike.cached.artworks.CachedArtwork
import com.example.whatilike.cached.artworks.CachedArtworkDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.invoke
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class MuseumApi {
    MET, HERMITAGE
}

class ArtRepository(
    private val context: Context,
    private val cachedArtworkDao: CachedArtworkDao,
) {
    private val metMuseumApi: MetMuseumApiService = MetDatabase.apiService
    private val hermitageMuseumApi: HermitageMuseumApiService = HermitageMuseumApiService()
    private val currentApi = mutableStateOf(MuseumApi.MET)
//    private var currentApi: MuseumApi = MuseumApi.MET

//

//    fun switchMuseumApi(toHermitage: Boolean) {
//        currentApi = if (toHermitage) MuseumApi.HERMITAGE else MuseumApi.MET
//    }

    private val imageLoader = ImageLoader(context)


    suspend fun removeArtworkFromCache(artwork: ArtObject) {
        withContext(Dispatchers.IO) {
            Log.d(
                "ArtRepository",
                "Art repository cached size: ${cachedArtworkDao.getArtworkCount()}"
            )

            val cachedArtwork = CachedArtwork(
                objectID = artwork.objectID,
                title = artwork.title,
                artistDisplayName = artwork.artistDisplayName,
                primaryImage = artwork.primaryImage ?: "",
                primaryImageSmall = artwork.primaryImageSmall ?: ""
            )
            cachedArtworkDao.deleteArtwork(cachedArtwork)
            Log.d("ArtRepository", "Artwork removed from cache: ${artwork.objectID}")
            Log.d(
                "ArtRepository",
                "Art repository cached size: ${cachedArtworkDao.getArtworkCount()}"
            )

        }
    }

    suspend fun getRandomArtworks(count: Int = 15): List<ArtObject> =
        if (currentApi.value == MuseumApi.MET) {
            getArtworksFromMetMuseum(count)
        } else {
            getArtworksFromHermitageMuseum(count)
        }


    suspend fun getArtworksFromMetMuseum(count: Int = 15): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val cachedArtworks = cachedArtworkDao.getCachedArtworks(count)
            val alreadyLoadedIDs = cachedArtworks.map { it.objectID }.toSet()

            val newArtworks =
                fetchArtworksFromMetMuseum(count).filterNot { alreadyLoadedIDs.contains(it.objectID) }

            if (newArtworks.isNotEmpty()) {
                cacheAndPreloadImages(newArtworks)
            }

            Log.d("ArtRepository", "Returning loaded artworks: ${newArtworks.size}")

            return@withContext newArtworks
        }

    private suspend fun getArtworksFromHermitageMuseum(count: Int): List<ArtObject> {
//        val cachedArtworks = cachedArtworkDao.getCachedArtworks(count)
//        val alreadyLoadedIDs = cachedArtworks.map { it.objectID }.toSet()
        return withContext(Dispatchers.IO) {
            val newArtworks =
                fetchHermitageArtworks(count * 10)
//                .filterNot { alreadyLoadedIDs.contains(it.objectID) }

            if (newArtworks.isNotEmpty()) {
                cacheAndPreloadImages(newArtworks)
            }

            newArtworks
        }
    }

    private suspend fun cacheAndPreloadImages(newArtworks: List<ArtObject>) {
        withContext(Dispatchers.IO) {
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
                        .listener(
                            onError = { _, throwable -> Log.e("ImageLoader", "Error loading image ${throwable.throwable}") },
                            onSuccess = { _, _ -> Log.d("ImageLoader", "Image loaded successfully") }
                        )
                        .build()
                )
            }

            Log.d("ArtRepository", "New artworks cached and images preloaded.")
        }
    }

    fun setCurrentApi(currentApi_: MuseumApi) {
        currentApi.value = currentApi_
        Log.d("Repo", "moved to ${currentApi.value.name}")

    }

    private suspend fun fetchArtworksFromMetMuseum(count: Int): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val response = metMuseumApi.getObjectByID(1)
            if (response.isSuccessful && response.body() != null) {
                val ids = List(count) { Random.nextInt(0, 400000 + 1) }
                Log.d("ArtRepository", "Total Object IDs fetched: ${ids.size}")

                val randomIds = ids.shuffled().take(count)

                val deferredArtworks = randomIds.map { id ->
                    async {
                        val artResponse = metMuseumApi.getObjectByID(id)
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

    private suspend fun fetchHermitageArtworks(count: Int): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val ids = List(count) { Random.nextInt(0, 900000 + 1) }
            Log.d("ArtRepository", "Total Object IDs fetched: ${ids.size}")

            val randomIds = ids.shuffled().take(count)

            val deferredArtworks = randomIds.map { id ->
                async {
                    val artResponse = hermitageMuseumApi.getObjectByID(id)
                    val artObject = artResponse
                    if (artObject != null && !artObject.primaryImage.isNullOrEmpty()) artObject else null
                }
            }

            val artworks = deferredArtworks.awaitAll().filterNotNull()
            Log.d("ArtRepository", "Artworks retrieved from API: ${artworks.size}")
            return@withContext artworks
        }


    suspend fun getArtworksByIdsMetMuseum(ids: List<Int>): List<ArtObject> = withContext(
        Dispatchers.IO
    ) {
        ids.map { id ->
            async {
                metMuseumApi.getObjectByID(id).body()
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun getArtworksByIdsHermitageMuseum(ids: List<Int>): List<ArtObject> = withContext(
        Dispatchers.IO
    ) {
        ids.map { id ->
            async {
                hermitageMuseumApi.getObjectByID(id)
            }
        }.awaitAll().filterNotNull()
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