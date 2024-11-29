package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whatilike.cached.artworks.CachedArtworkDao
import kotlinx.coroutines.launch

class ArtViewModel(context: Context, dao: CachedArtworkDao) : ViewModel() {
    private val repository = ArtRepository(context, dao)
    private val _artworks = mutableStateOf<List<ArtObject>>(emptyList())
    val artworks: State<List<ArtObject>> = _artworks

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun removeArtworkFromCache(artwork: ArtObject) {
        viewModelScope.launch {
            repository.removeArtworkFromCache(artwork)
            _artworks.value = _artworks.value.filterNot { it.objectID == artwork.objectID }
        }
    }


    fun loadRandomArtworks(count: Int) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count)
                _artworks.value = (_artworks.value + result).distinctBy { it.objectID }
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
    private val dao: CachedArtworkDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtViewModel(context, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
