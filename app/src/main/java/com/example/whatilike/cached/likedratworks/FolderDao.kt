package com.example.whatilike.cached.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFolder(folder: Folder)

    @Query("SELECT * FROM folders  WHERE uid = :uid LIMIT 1")
    suspend fun getAllFolders(uid : String): List<Folder>

//    @Query("SELECT artworkIds FROM folders WHERE folderId = :folderId")
//    suspend fun getArtworkIdsInFolder(uid: String, folderId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFolders(folders: List<Folder>)

//    @Query("SELECT artworkIds FROM folders WHERE uid = :uid and folderId = :folderId")
//    suspend fun getArtworkIdsInFolder(uid : String, folderId: Int): List<Int>?

}