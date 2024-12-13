package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ArtViewModel(context: Context) : ViewModel() {
    private val repository = ArtRepository(context)

    private val _metArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    private val _hermitageArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    var artworks: MutableStateFlow<List<ArtObject>> = _metArtworks

    var currentMetIndex = mutableIntStateOf(0)
    var currentHermitageIndex = mutableIntStateOf(0)
    var currentIndex = mutableIntStateOf(0)

    var currentApi_ = MuseumApi.MET

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun setCurrentApi(currentApi: MuseumApi) {
        repository.setCurrentApi(currentApi)
        Log.d("View Model", "moved to ${currentApi.name}")

        when (currentApi_) {
            MuseumApi.MET -> {
                currentMetIndex.value = currentIndex.value
            }
            else -> {
                currentHermitageIndex.value = currentIndex.value
            }
        }

        artworks = when (currentApi) {
            MuseumApi.MET -> {
                _metArtworks
            }

            else -> {
                _hermitageArtworks
            }
        }
    }


    fun loadRandomArtworks(count: Int, isMainApi: Boolean) {
        viewModelScope.launch {
            try {
                val currentApi = if (isMainApi) MuseumApi.MET else MuseumApi.HERMITAGE
                val result = repository.getRandomArtworks(count * 4, currentApi)

                if (isMainApi) {
                    _metArtworks.value = result
                } else {
                    _hermitageArtworks.value = result
                }
                Log.d("ArtViewModel", "Total artworks loaded: ${artworks.value.size}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks", e)
            } finally {
                _isLoading.value = false
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
