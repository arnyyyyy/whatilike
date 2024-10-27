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

    fun loadRandomArtworks(count: Int) {
        viewModelScope.launch {
            try {
                val result = repository.getRandomArtworks(count)
                _artworks.value = result
                Log.d("ArtViewModel", "Number of artworks loaded: ${result.size}")
            } catch (e: Exception) {
                Log.e("ArtViewModel", "Failed to load artworks", e)
            }
        }
    }
}