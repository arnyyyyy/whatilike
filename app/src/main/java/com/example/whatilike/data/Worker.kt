package com.example.whatilike.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val fileName = "downloaded_artwork.jpg"

        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("DownloadWorker", "Failed to connect: ${connection.responseCode}")
                return Result.failure()
            }

            saveToGallery(applicationContext, fileName, connection.inputStream)
            Log.d("DownloadWorker", "File saved successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error: ${e.message}")
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun saveToGallery(context: Context, fileName: String, inputStream: InputStream) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create new MediaStore record.")

        resolver.openOutputStream(uri).use { outputStream ->
            inputStream.copyTo(outputStream!!)
        }
    }
}


fun downloadArtwork(context: Context, url: String) {
    val workManager = WorkManager.getInstance(context)

    val data = workDataOf("url" to url)

    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setInputData(data)
        .build()

    workManager.enqueue(downloadWorkRequest)
}
