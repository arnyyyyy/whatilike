package com.example.whatilike.cached.user

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import com.example.whatilike.data.ArtObject
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

//
//class LikedArtworksViewModel(
//    private val userLikedArtworks: LikedArtworksDao,
//    private val firestore: FirebaseFirestore = Firebase.firestore,
//    private val context: Context
//) : ViewModel() {
//
//    private val _currentLikedArtworks = MutableStateFlow<LikedArtworks?>(null)
//    val currentLikedArtworks: StateFlow<LikedArtworks?> = _currentLikedArtworks
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    fun initializeUserLikedArtworksFromFirebase() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val firebaseUser = FirebaseAuth.getInstance().currentUser
//                firebaseUser?.let { user ->
//                    val userId = user.uid
//                    val likedArtworksIds = emptyList<Int>()
//                    val document = firestore.collection("users").document(userId).collection("liked_artworks").get().await()
//                    if (!document.isEmpty) {
//                        val newProfile = LikedArtworks(
//                            uid = userId,
//                            likedArtworksIds = likedArtworksIds
//                        )
//                        firestore.collection("users").document(userId).set(newProfile).await()
//                    }
//                    loadLikedArtworks(userId)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//
//    fun loadUserProfile(userId: String) {
//        viewModelScope.launch {
//            _isLoading.value = true
//
//            try {
//                val localProfile = withContext(Dispatchers.IO) {
//                    userLikedArtworks.getUserProfile(userId)
//                }
//                localProfile?.let { _currentUserProfile.value = it }
//
//                val document = firestore.collection("users").document(userId).get().await()
//                val nickname = document.getString("nickname")
//                val photoUrl = document.getString("photoUrl")
//
//                val updatedProfile = UserProfile(
//                    uid = userId,
//                    nickname = nickname ?: localProfile?.nickname.orEmpty(),
//                    photoUrl = photoUrl ?: localProfile?.photoUrl.orEmpty()
//                )
//
//                withContext(Dispatchers.IO) {
//                    userLikedArtworks.saveUserProfile(updatedProfile)
//                }
//                _currentUserProfile.value = updatedProfile
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    suspend fun getLocalImage(userId: String): Bitmap? {
//        return withContext(Dispatchers.IO) {
//            val localProfile = userLikedArtworks.getUserProfile(userId)
//            localProfile?.photoUrl?.let {
//                val file = File(it)
//                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
//            }
//        }
//    }
//
//    fun updateNickname(userId: String, newNickname: String) {
//        viewModelScope.launch {
//            try {
//                firestore.collection("users").document(userId)
//                    .update("nickname", newNickname).await()
//
//                val currentProfile = withContext(Dispatchers.IO) {
//                    userLikedArtworks.getUserProfile(userId)
//                }
//
//                if (currentProfile != null) {
//                    val updatedProfile = currentProfile.copy(nickname = newNickname)
//                    withContext(Dispatchers.IO) {
//                        userLikedArtworks.saveUserProfile(updatedProfile)
//                    }
//                    _currentUserProfile.value = updatedProfile
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun uploadUserPhoto(userId: String, photoUri: Uri) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val storageRef = FirebaseStorage.getInstance().reference
//                val userPhotoRef =
//                    storageRef.child("user_photos/${userId}/${UUID.randomUUID()}.jpg")
//
//                userPhotoRef.putFile(photoUri).await()
//
//                val downloadUrl = userPhotoRef.downloadUrl.await()
//                firestore.collection("users").document(userId)
//                    .update("photoUrl", downloadUrl.toString()).await()
//
//                saveImageLocally(userId, photoUri)
//                updateProfile(userId, downloadUrl.toString())
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    private fun saveImageLocally(userId: String, photoUri: Uri) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val localProfile = userLikedArtworks.getUserProfile(userId)
//            val localPath = saveImageToFile(photoUri)
//
//            if (localProfile != null) {
//                userLikedArtworks.saveUserProfile(
//                    UserProfile(
//                        uid = userId,
//                        nickname = localProfile.nickname,
//                        photoUrl = localPath
//                    )
//                )
//            }
//        }
//    }
//
//
//    private fun addToLikedArtworks(userId: String, likedArtworkId: Int) {
//        _currentLikedArtworks.value?.let {
//            val updatedProfile = it.copy(photoUrl = photoUrl)
//            _currentLikedArtworks.value = updatedProfile
//            viewModelScope.launch(Dispatchers.IO) {
//                userLikedArtworks.saveUserProfile(updatedProfile)
//            }
//        }
//    }
//}
//
//class LikedArtworkViewModelFactory(
//    private val likedArtworkDao: LikedArtworksDao,
//    private val firestore: FirebaseFirestore,
//    private val context: Context
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(LikedArtworksViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return LikedArtworksViewModel(likedArtworkDao, firestore, context) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}

suspend fun fetchLikedArtworkIds(userId: String): List<Int> = withContext(Dispatchers.IO) {
    val result = FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("liked_artworks")
        .get()
        .await()
    return@withContext result.documents.mapNotNull { it.getLong("artworkId")?.toInt() }
}

fun deleteArtworkFromLiked(userId: String, artworkId: Int) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("liked_artworks")
        .whereEqualTo("artworkId", artworkId)
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents.firstOrNull()
            document?.reference?.delete()
                ?.addOnSuccessListener {
                    println("Artwork successfully deleted from liked list")
                }
                ?.addOnFailureListener { exception ->
                    println("Error deleting artwork: $exception")
                }
        }
        .addOnFailureListener { exception ->
            println("Error fetching liked artwork: $exception")
        }
}

//


fun addLikedArtworkToFirestore(userId: String, artwork: ArtObject) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .collection("liked_artworks")
        .document(artwork.objectID.toString())
        .set(mapOf("artworkId" to artwork.objectID))
        .addOnSuccessListener {
            println(artwork.objectID.toString())
            println("Artwork added to liked_artworks successfully")
        }
        .addOnFailureListener { exception ->
            println("Error adding artwork: $exception")
        }
}