package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ArtViewModel(context: Context) : ViewModel() {
    private val repository = ArtRepository(context)

    private val _context = context

    private val _metArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    private val _hermitageArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    var artworks: MutableStateFlow<List<ArtObject>> = _metArtworks

    private var currentMetIndex = mutableStateOf(0)
    private var currentHermitageIndex = mutableStateOf(0)
    var currentIndex = mutableStateOf(0)

    private var currentApi = mutableStateOf(MuseumApi.MET)

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    val imageLoader = mutableStateOf(ImageLoader(context))

    init {
        CoroutineScope(coroutineContext).launch {
            val load = async { loadRandomArtworks(20, true)}
            val load1 = async { loadRandomArtworks(20, false) }

            awaitAll(load1, load)
        }
    }

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


    fun setCurrentApi(newApi: MuseumApi) {
        repository.setCurrentApi(newApi)
        Log.d("View Model", "moved to ${newApi.name}")

        when (currentApi.value) {
            MuseumApi.MET -> {
                currentMetIndex.value = currentIndex.value
                imageLoader.value = Coil.imageLoader(_context)
            }

            else -> {
                currentHermitageIndex.value = currentIndex.value
                imageLoader.value = ImageLoader.Builder(_context)
                    .okHttpClient(client)
                    .build()
            }
        }

        when (newApi) {
            MuseumApi.MET -> {
                artworks = _metArtworks
                currentIndex.value = currentMetIndex.value
            }
            else -> {
                artworks = _hermitageArtworks
                currentIndex.value = currentHermitageIndex.value
            }
        }
        currentApi.value = newApi
    }

    fun loadArtworks(isMainApi: Boolean) {
        viewModelScope.launch {
            loadRandomArtworks(count = 100, isMainApi = isMainApi)
        }
    }


    suspend fun loadRandomArtworks(count: Int, isMainApi: Boolean) {
            try {
                val currentApi = if (isMainApi) MuseumApi.MET else MuseumApi.HERMITAGE
                val result = repository.getRandomArtworks(count * 4, currentApi)

                if (isMainApi) {
                    _metArtworks.value += result
                } else {
                    _hermitageArtworks.value += result
                }
                Log.d("ArtViewModel", "Total artworks loaded: ${artworks.value.size}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks", e)
            } finally {
                _isLoading.value = false
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
