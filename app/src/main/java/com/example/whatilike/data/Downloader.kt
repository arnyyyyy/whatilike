package com.example.whatilike.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.security.cert.X509Certificate

import javax.net.ssl.*


fun downloadArtwork(context: Context, url: String, artworkTitle: String) {
    setupTrustManager()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val connection = URL(url).openConnection() as HttpsURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("DownloadArtwork", "Failed to connect: ${connection.responseCode}")
                return@launch
            }

            saveImage(context, artworkTitle, connection.inputStream)
            Log.d("DownloadArtwork", "File saved successfully.")
        } catch (e: Exception) {
            Log.e("DownloadArtwork", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
}

private fun setupTrustManager() {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    try {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}




private fun saveImage(context: Context, fileName: String, inputStream: InputStream) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${fileName}_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/whatilike/")
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { imageUri ->
            context.contentResolver.openOutputStream(imageUri).use { outputStream ->
                inputStream.copyTo(outputStream!!)
                Log.d("Save Image", "Image successfully saved to: $imageUri")
            }
        } ?: Log.e("Save Image", "Failed to create media store entry")

    } else {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "${fileName}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
            Log.d("Save Image", "Image successfully saved to: $file")
        }
    }
}