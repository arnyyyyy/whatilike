package com.example.whatilike.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtViewModel(context: Context) : ViewModel() {
    private val repository = ArtRepository(context)
    private val _artworks = MutableStateFlow<List<ArtObject>>(emptyList())
    val artworks: StateFlow<List<ArtObject>> = _artworks
    var currentIndex = mutableIntStateOf(0)

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun setCurrentApi(currentApi: MuseumApi) {
        _artworks.value = emptyList()
        _isLoading.value = true
        repository.setCurrentApi(currentApi)
        Log.d("View Model", "moved to ${currentApi.name}")
        currentIndex.value = 0
        loadRandomArtworks(20)
    }

    fun loadRandomArtworks(count: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count * 4)
                _artworks.value = result
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
