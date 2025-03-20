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
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UserProfileViewModel(
    private val userProfileDao: UserProfileDao,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val context: Context) : ViewModel() {

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile
    val user = FirebaseAuth.getInstance().currentUser
    val uid = mutableStateOf("")

    var bitmap = mutableStateOf<Bitmap?>(null)

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                uid.value = user!!.uid
                loadUserProfile(user.uid)
                bitmap.value = getLocalImage(uid.value)

                Log.d("User Profile", "User authorized successfully ")
            } catch (e: Exception) {
                Log.e("User Profile", "Failed to authorize", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initializeUserProfileFromFirebase() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                firebaseUser?.let { user ->
                    val userId = user.uid
                    val nickname = user.displayName ?: "User"
                    val photoUrl = user.photoUrl?.toString() ?: ""
                    val document = firestore.collection("users").document(userId).get().await()
                    if (!document.exists()) {
                        val newProfile = UserProfile(
                            uid = userId,
                            nickname = nickname,
                            photoUrl = photoUrl
                        )
                        firestore.collection("users").document(userId).set(newProfile).await()
                    }
                    loadUserProfile(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }


    suspend fun loadUserProfile(userId: String) {
        try {
                val localProfile = withContext(Dispatchers.IO) {
                    userProfileDao.getUserProfile(userId)
                }
                localProfile?.let { _currentUserProfile.value = it }

                val document = firestore.collection("users").document(userId).get().await()
                val nickname = document.getString("nickname")
                val photoUrl = document.getString("photoUrl")

                val updatedProfile = UserProfile(
                    uid = userId,
                    nickname = nickname ?: localProfile?.nickname.orEmpty(),
                    photoUrl = photoUrl ?: localProfile?.photoUrl.orEmpty()
                )

                withContext(Dispatchers.IO) {
                    userProfileDao.saveUserProfile(updatedProfile)
                }
                _currentUserProfile.value = updatedProfile

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }

    suspend fun getLocalImage(userId: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val localProfile = userProfileDao.getUserProfile(userId)
            localProfile?.photoUrl?.let {
                val file = File(it)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            }
        }
    }

    fun updateNickname(userId: String, newNickname: String) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .update("nickname", newNickname).await()

                val currentProfile = withContext(Dispatchers.IO) {
                    userProfileDao.getUserProfile(userId)
                }

                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(nickname = newNickname)
                    withContext(Dispatchers.IO) {
                        userProfileDao.saveUserProfile(updatedProfile)
                    }
                    _currentUserProfile.value = updatedProfile
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uploadUserPhoto(userId: String, photoUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                val userPhotoRef =
                    storageRef.child("user_photos/${userId}/${UUID.randomUUID()}.jpg")

                userPhotoRef.putFile(photoUri).await()

                val downloadUrl = userPhotoRef.downloadUrl.await()
                firestore.collection("users").document(userId)
                    .update("photoUrl", downloadUrl.toString()).await()

                saveImageLocally(userId, photoUri)
                updateProfile(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveImageLocally(userId: String, photoUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val localProfile = userProfileDao.getUserProfile(userId)
            val localPath = saveImageToFile(photoUri)

            if (localProfile != null) {
                userProfileDao.saveUserProfile(
                    UserProfile(
                        uid = userId,
                        nickname = localProfile.nickname,
                        photoUrl = localPath
                    )
                )
            }
        }
    }

    private suspend fun saveImageToFile(photoUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "user_photo_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        }
    }

    private fun updateProfile(photoUrl: String) {
        _currentUserProfile.value?.let {
            val updatedProfile = it.copy(photoUrl = photoUrl)
            _currentUserProfile.value = updatedProfile
            viewModelScope.launch(Dispatchers.IO) {
                userProfileDao.saveUserProfile(updatedProfile)
            }
        }
    }
}

class UserProfileViewModelFactory(
    private val userProfileDao: UserProfileDao,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(userProfileDao, firestore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
