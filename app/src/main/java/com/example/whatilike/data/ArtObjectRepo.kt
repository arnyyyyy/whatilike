package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class MuseumApi {
    MET, HERMITAGE, HARVARD, MUSEUM
}

class ArtRepository(
    private val context: Context,
) {
    private val metMuseumApi: MetMuseumApiService = MetDatabase.apiService
    private val hermitageMuseumApi: HermitageMuseumApiService = HermitageMuseumApiService()
    private val currentApi = mutableStateOf(MuseumApi.MET)

    suspend fun getRandomArtworks(count: Int = 15): List<ArtObject> =
        if (currentApi.value == MuseumApi.MET) {
            getArtworksFromMetMuseum(count)
        } else if (currentApi.value == MuseumApi.HERMITAGE) {
            getArtworksFromHermitageMuseum(count)
        } else emptyList()


    private suspend fun getArtworksFromMetMuseum(count: Int = 15): List<ArtObject> =
        withContext(Dispatchers.IO) {
            val newArtworks =
                fetchArtworksFromMetMuseum(count)
            Log.d("ArtRepository", "Returning loaded artworks: ${newArtworks.size}")

            return@withContext newArtworks
        }

    private suspend fun getArtworksFromHermitageMuseum(count: Int): List<ArtObject> {
        return withContext(Dispatchers.IO) {
            val newArtworks =
                fetchHermitageArtworks(count)
            newArtworks
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
                    if (artResponse != null && !artResponse.primaryImage.isNullOrEmpty()) artResponse else null
                }
            }

            val artworks = deferredArtworks.awaitAll().filterNotNull()
            Log.d("ArtRepository", "Artworks retrieved from API: ${artworks.size}")
            return@withContext artworks
        }


    suspend fun getArtworksByIds(ids: List<Int>): List<ArtObject> = withContext(
        Dispatchers.IO
    ) {
        val (hermitageIds, metIds) = ids.partition { it > 1000000 }
        val hermitageArtworks = getArtworksByIdsHermitageMuseum(hermitageIds)
        val metArtworks = getArtworksByIdsMetMuseum(metIds)

        return@withContext hermitageArtworks + metArtworks

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
                hermitageMuseumApi.getObjectByID(id - MUSEUM_SHIFT)
            }
        }.awaitAll().filterNotNull()
    }
}

class ArtRepositoryFactory(
    private val context: Context
) {
    fun create(): ArtRepository {
        return ArtRepository(context)
    }
}