package com.example.whatilike.cached.user


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore

import com.example.whatilike.data.ArtObject
import com.example.whatilike.data.ArtRepositoryFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LikedArtworksViewModel(
    private val likedArtworksDao: LikedArtworksDao,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val context: Context
) : ViewModel() {

    private val _likedArtworks = MutableStateFlow<List<ArtObject>>(emptyList())
    val likedArtworks: StateFlow<List<ArtObject>> = _likedArtworks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    fun loadLikedArtworks() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val localLikedArtworks = withContext(Dispatchers.IO) {
                        likedArtworksDao.getUserLikedArtworks(firebaseUser.uid)
                    }

                    val likedIds = localLikedArtworks?.likedArtworksIds ?: emptyList()

                    if (likedIds.isEmpty()) {
                        val remoteIds = fetchLikedArtworkIds(firebaseUser.uid)
                        saveToLocalDatabase(firebaseUser.uid, remoteIds)
                        val artworks = fetchArtworksByIds(remoteIds)
                        _likedArtworks.value = artworks
                    } else {
                        val artworks = fetchArtworksByIds(likedIds)
                        _likedArtworks.value = artworks
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun addLikedArtwork(artworkId: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val document = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .collection("liked_artworks")
                        .document(artworkId.toString())

                    document.set(mapOf("artworkId" to artworkId)).await()

                    withContext(Dispatchers.IO) {
                        val currentData = likedArtworksDao.getUserLikedArtworks(firebaseUser.uid)
                        val updatedIds =
                            currentData?.likedArtworksIds?.toMutableList() ?: mutableListOf()
                        updatedIds.add(artworkId)
                        likedArtworksDao.saveLikedArtworks(
                            LikedArtworks(
                                firebaseUser.uid,
                                updatedIds
                            )
                        )
                    }

                    loadLikedArtworks()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteLikedArtwork(artworkId: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val querySnapshot = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .collection("liked_artworks")
                        .whereEqualTo("artworkId", artworkId)
                        .get()
                        .await()

                    querySnapshot.documents.firstOrNull()?.reference?.delete()?.await()

                    withContext(Dispatchers.IO) {
                        val currentData = likedArtworksDao.getUserLikedArtworks(firebaseUser.uid)
                        val updatedIds =
                            currentData?.likedArtworksIds?.toMutableList() ?: mutableListOf()
                        updatedIds.remove(artworkId)
                        likedArtworksDao.saveLikedArtworks(
                            LikedArtworks(
                                firebaseUser.uid,
                                updatedIds
                            )
                        )
                    }

                    loadLikedArtworks()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun saveToLocalDatabase(userId: String, likedIds: List<Int>) {
        withContext(Dispatchers.IO) {
            likedArtworksDao.saveLikedArtworks(LikedArtworks(userId, likedIds))
        }
    }

    private suspend fun fetchLikedArtworkIds(userId: String): List<Int> {
        val result = firestore.collection("users")
            .document(userId)
            .collection("liked_artworks")
            .get()
            .await()
        return result.documents.mapNotNull { it.getLong("artworkId")?.toInt() }
    }

    private suspend fun fetchArtworksByIds(ids: List<Int>): List<ArtObject> {
        val artRepository = ArtRepositoryFactory(context).create()
        return artRepository.getArtworksByIdsMetMuseum(ids)
    }
}

class LikedArtworksViewModelFactory(
    private val likedArtworksDao: LikedArtworksDao,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LikedArtworksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LikedArtworksViewModel(likedArtworksDao, firestore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}