package com.example.whatilike.cached.user


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class FolderViewModel(
    private val folderDao: FolderDao,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val context: Context
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadFolders() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val localFolders = withContext(Dispatchers.IO) {
                        folderDao.getAllFolders(firebaseUser.uid)
                    }

                    val folders = localFolders?: emptyList()

                    if (folders.isEmpty()) {
                        val remoteFolders = fetchFolders(firebaseUser.uid)
                        saveToLocalDatabase(remoteFolders)
                        _folders.value = remoteFolders
                    } else {
                        _folders.value = folders
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }


//    fun loadArtworksInFolder(folderId: Int) {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val artworksInFolder = folderDao.getArtworkIdsInFolder(folderId)
//                val likedIds = artworksInFolder?.likedArtworksIds ?: emptyList()
//                val artworks = fetchArtworksByIds(likedIds)
//                _likedArtworks.value = artworks
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }



    fun addFolder(folderName: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val folderDocument = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .collection("folders")
                        .document()

                    val newFolder = Folder(
                        folderId = folderDocument.id,
                        uid = firebaseUser.uid,
                        name = folderName,
                        artworkIds = emptyList()
                    )

                    folderDocument.set(mapOf(
                        "uid" to newFolder.uid,
                        "name" to newFolder.name,
                        "artworkIds" to newFolder.artworkIds
                    )).await()


                    folderDao.addFolder(newFolder)
                    loadFolders()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

//    fun deleteFolder(folderId: String) {
//        val user = FirebaseAuth.getInstance().currentUser
//        user?.let { firebaseUser ->
//            viewModelScope.launch {
//                try {
//                    firestore.collection("users")
//                        .document(firebaseUser.uid)
//                        .collection("folders")
//                        .document(folderId)
//                        .delete()
//                        .await()
//
////                    folderDao.deleteFolder(folderId)
//
//                    withContext(Dispatchers.IO) {
//                        val currentData = folderDao.getAllFolders(firebaseUser.uid)
//                        val updatedFolders: MutableList<Folder>  = currentData.toMutableList() ?: mutableListOf()
//
//                        // Удаляем идентификатор папки
//                        updatedFolders.remove(folderId)
//
//                        // Сохраняем обновленный список папок
//                        likedArtworksDao.saveFolders(
//                            Folders(
//                                uid = firebaseUser.uid,
//                                foldersIds = updatedFolders
//                            )
//                        )
//                    }
//
//                    // Обновляем интерфейс, если требуется
//                    loadLikedArtworks()
//
//                    loadFolders()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

//    fun deleteFolder(folderId: Int) {
//        val user = FirebaseAuth.getInstance().currentUser
//        user?.let { firebaseUser ->
//            viewModelScope.launch {
//                try {
//                    val querySnapshot = firestore.collection("users")
//                        .document(firebaseUser.uid)
//                        .collection("folders")
//                        .whereEqualTo("folderId", folderId)
//                        .get()
//                        .await()
//
//                    querySnapshot.documents.firstOrNull()?.reference?.delete()?.await()
//
//                    withContext(Dispatchers.IO) {
//                        val currentData = folderDao.getAllFolders(firebaseUser.uid)
//                        val updatedData =
//                            currentData?.toMutableList() ?: mutableListOf()
//                        updatedData.remove(folderId)
//                        folderDao.saveFolders(
//                            LikedArtworks(
//                                firebaseUser.uid,
//                                updatedIds
//                            )
//                        )
//                    }
//
//                    loadLikedArtworks()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

    private suspend fun saveToLocalDatabase(folders: List<Folder>) {
        withContext(Dispatchers.IO) {
            folderDao.saveFolders(folders)
        }
    }

    private suspend fun fetchFolders(userId: String): List<Folder> {
        val result = firestore.collection("users")
            .document(userId)
            .collection("folders")
            .get()
            .await()

        return result.documents.mapNotNull { document ->
            val folderId = document.id
            val uid = document.getString("uid")
            val name = document.getString("name")
            val artworkIds = document.get("artworkIds") as? List<Int>

            if (uid != null && name != null && artworkIds != null) {
                Folder(folderId, uid, name, artworkIds)
            } else {
                null
            }
        }
    }
}

class FolderViewModelFactory(
    private val folderDao: FolderDao,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FolderViewModel(folderDao, firestore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}