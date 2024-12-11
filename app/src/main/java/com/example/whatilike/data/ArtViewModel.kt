package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtViewModel(context: Context) : ViewModel() {
    private val repository = ArtRepository(context)
    val currentApi = mutableStateOf(MuseumApi.MET)
    private val _artworks = MutableStateFlow<List<ArtObject>>(emptyList())
    val artworks: StateFlow<List<ArtObject>> = _artworks

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

//    fun removeArtworkFromCache(artwork: ArtObject) {
//        viewModelScope.launch {
////            repository.removeArtworkFromCache(artwork)
//            _artworks.value = _artworks.value.filterNot { it.objectID == artwork.objectID }
//        }
//    }

    fun setCurrentApi(currentApi : MuseumApi) {
        Log.d("View Model", "moved to ${currentApi.name}")
        _artworks.value = emptyList()
        repository.setCurrentApi(currentApi)
        loadRandomArtworks(20)
    }


    fun loadRandomArtworks(count: Int) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count * 5)
                _artworks.value = result
//                _artworks.value = (_artworks.value + result).distinctBy { it.objectID }
                Log.d("ArtViewModel", "Total artworks loaded: ${_artworks.value.size}")
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
