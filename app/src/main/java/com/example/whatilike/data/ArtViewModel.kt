package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.util.DebugLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ArtViewModel(context: Context) : ViewModel() {
    private val repository = ArtRepository(context)

    private val _context = context

    private val _metArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    private val _hermitageArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    private val _harvardArtworks = MutableStateFlow<List<ArtObject>>(emptyList())

    var artworks: MutableStateFlow<List<ArtObject>> = _hermitageArtworks

    private var currentMetIndex = mutableStateOf(0)
    private var currentHermitageIndex = mutableStateOf(0)
    private var currentHarvardIndex = mutableStateOf(0)

    var currentIndex = mutableStateOf(0)

    private var currentApi = mutableStateOf(MuseumApi.HERMITAGE)
    var isLoading = mutableStateOf(true)

    private val unsafeTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        override fun checkServerTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, arrayOf<TrustManager>(unsafeTrustManager), java.security.SecureRandom())
    }

    private val client = OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
        .build()

    val imageLoader = mutableStateOf(
        ImageLoader.Builder(_context)
            .okHttpClient(client)
            .logger(DebugLogger())
            .build()
    )
//        mutableStateOf(Coil.imageLoader(context).newBuilder().logger(DebugLogger()).build())

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val initLoading = async { loadRandomArtworks(80, MuseumApi.HERMITAGE) }
                initLoading.await()

                if(artworks.value.isNotEmpty()) {
                    val artworksToPreload = listOf(
                        artworks.value.get(0),
                        artworks.value.get(1),
                        artworks.value.get(2),
                        artworks.value.get(3),
                        artworks.value.get(4),
                        artworks.value.get(5),
                        artworks.value.get(6),
                        artworks.value.get(7),
                        artworks.value.get(8),
                        artworks.value.get(9),
                        artworks.value.get(10),
                        artworks.value.get(11),
                        artworks.value.get(12),
                        artworks.value.get(13),
                    )

                    val loadImages = artworksToPreload.map { artwork ->
                        async { preloadImage(artwork.primaryImageSmall, context) }
                    }
                    loadImages.awaitAll()
                }

                Log.d("ArtViewModel", "Artworks loaded successfully.")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks during init", e)
            } finally {
                isLoading.value = false

                launch { loadRandomArtworks(80, MuseumApi.MET) }
                launch { loadRandomArtworks(80, MuseumApi.HARVARD) }
                launch { loadRandomArtworks(400, MuseumApi.HERMITAGE) }
                launch { loadRandomArtworks(400, MuseumApi.MET) }
                launch { loadRandomArtworks(400, MuseumApi.HARVARD) }
            }
        }
    }


    fun setCurrentApi(newApi: MuseumApi) {
        repository.setCurrentApi(newApi)
        Log.d("View Model", "moved to ${newApi.name}")

        when (currentApi.value) {
            MuseumApi.MET -> {
                currentMetIndex.value = currentIndex.value
            }

            MuseumApi.HERMITAGE -> {
                currentHermitageIndex.value = currentIndex.value
            }

            MuseumApi.HARVARD -> {
                currentHarvardIndex.value = currentIndex.value
            }

            else -> {}
        }

        when (newApi) {
            MuseumApi.MET -> {
                artworks = _metArtworks
                currentIndex.value = currentMetIndex.value
            }

            MuseumApi.HERMITAGE -> {
                artworks = _hermitageArtworks
                currentIndex.value = currentHermitageIndex.value
            }

            MuseumApi.HARVARD -> {
                artworks = _harvardArtworks
                currentIndex.value = currentHarvardIndex.value
            }

            else -> {}
        }
        currentApi.value = newApi
    }

    suspend fun preloadImage(imageUrl: String?, context: Context) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        try {
            val result = imageLoader.value.execute(request)

            if (result is ErrorResult) {
                Log.e("ImageLoadError", "${result.throwable.message}")

            }
            if (result is SuccessResult) {
                val drawable = result.drawable
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadRandomArtworks(count: Int, museumApi: MuseumApi) {
        val result = MutableStateFlow<List<ArtObject>>(emptyList())
        try {


            result.value = repository.getRandomArtworks(count, museumApi)

            when (museumApi) {
                MuseumApi.MET -> {
                    _metArtworks.value += result.value
                }
                MuseumApi.HERMITAGE -> {
                    _hermitageArtworks.value += result.value
                }
                MuseumApi.HARVARD -> {
                    _harvardArtworks.value += result.value
                }

                else -> {}
            }

            Log.d("ArtViewModel", "Total artworks loaded: ${artworks.value.size}")
        } catch (e: Exception) {
            Log.e("ArtViewModel", "Failed to load artworks", e)
        } finally {
            viewModelScope.launch {
                if (result.value.isNotEmpty()) {
                    val artworksToPreload = listOf(
                        result.value.get(0),
                        result.value.get(1),
                        result.value.get(2),
                        result.value.get(3),
                        result.value.get(4),
                        result.value.get(5),
                        result.value.get(6),
                    )

                    artworksToPreload.map { artwork ->
                        launch { preloadImage(artwork.primaryImageSmall, _context) }
                    }
                }
            }
        }
    }
}

class ArtViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
