package com.example.whatilike.data

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatilike.repository.ArtRepository
import kotlinx.coroutines.launch

class ArtViewModel : ViewModel() {
    private val repository = ArtRepository()
    private val _artworks = mutableStateOf<List<ArtObject>>(emptyList())
    val artworks: State<List<ArtObject>> = _artworks
    private var isLoading = false

    fun loadRandomArtworks(count: Int) {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count)
                _artworks.value = _artworks.value + result
                Log.d("ArtViewModel", "Total artworks loaded: ${_artworks.value.size}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks", e)
            } finally {
                isLoading = false
            }
        }
    }
}