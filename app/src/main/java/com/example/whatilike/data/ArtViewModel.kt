package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whatilike.cached.artworks.CachedArtworkDao
import com.example.whatilike.repository.ArtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//class ArtViewModel(context: Context, dao: CachedArtworkDao) : ViewModel() {
//    private val repository = ArtRepository(context, dao)
//    private val _artworks = mutableStateOf<List<ArtObject>>(emptyList())
//    val artworks: State<List<ArtObject>> = _artworks
//
//    private val _isLoading = mutableStateOf(false)
//    val isLoading: State<Boolean> = _isLoading
//
//    suspend fun removeArtworkFromCache(artwork: ArtObject) {
//        viewModelScope.launch {
//            repository.removeArtworkFromCache(artwork)
//        }
//    }
//
//    fun loadRandomArtworks(count: Int, idx : Int) {
//        _isLoading.value = true
//
//        viewModelScope.launch {
//            try {
//                val result = repository.getRandomArtworks(count, idx)
//                _artworks.value = _artworks.value + result
//                Log.d("ArtViewModel", "Total artworks loaded: ${_artworks.value.size}")
//            } catch (e: Exception) {
//                Log.e("ArtViewModel", "Failed to load artworks", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//}


class ArtViewModel(context: Context, dao: CachedArtworkDao) : ViewModel() {
    private val repository = ArtRepository(context, dao)

    val artworks: StateFlow<List<ArtObject>> = repository
        .getRandomArtworks(15)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun removeArtworkFromCache(artwork: ArtObject) {
        viewModelScope.launch {
            try {
                repository.removeArtworkFromCache(artwork)
                Log.d("ArtViewModel", "Artwork removed from cache: ${artwork.objectID}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to remove artwork", e)
            }
        }
    }

    fun loadRandomArtworks(count: Int) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count).first()
                _isLoading.value = false
                Log.d("ArtViewModel", "Total artworks loaded: ${result.size}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks", e)
                _isLoading.value = false
            }
        }
    }
}


//class ArtViewModel(context: Context, dao: CachedArtworkDao) : ViewModel() {
//    private val repository = ArtRepository(context, dao)
//    private val _artworks = MutableStateFlow<List<ArtObject>>(emptyList())
//    val artworks: StateFlow<List<ArtObject>> = _artworks
//
//    val artworks: StateFlow<List<ArtObject>> = repository
//        .getRandomArtworks(15)
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
//
//    private val _isLoading = mutableStateOf(false)
//    val isLoading: State<Boolean> = _isLoading
//
//    fun removeArtworkFromCache(artwork: ArtObject) {
//        viewModelScope.launch {
//            try {
//                repository.removeArtworkFromCache(artwork)
//                Log.d("ArtViewModel", "Artwork removed from cache: ${artwork.objectID}")
//            } catch (e: Exception) {
//                Log.e("ArtViewModel", "Failed to remove artwork", e)
//            }
//        }
//    }
//
//
//
//}


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
