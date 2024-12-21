package com.example.whatilike.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun downloadArtwork(context: Context, url: String) {
    CoroutineScope(Dispatchers.IO).launch {
        val fileName = "downloaded_artwork.jpg"

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("DownloadArtwork", "Failed to connect: ${connection.responseCode}")
                return@launch
            }

            saveToGallery(context, fileName, connection.inputStream)
            Log.d("DownloadArtwork", "File saved successfully.")
        } catch (e: Exception) {
            Log.e("DownloadArtwork", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
}

private fun saveToGallery(context: Context, fileName: String, inputStream: InputStream) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IOException("Failed to create new MediaStore record.")

    resolver.openOutputStream(uri).use { outputStream ->
        inputStream.copyTo(outputStream!!)
    }
}