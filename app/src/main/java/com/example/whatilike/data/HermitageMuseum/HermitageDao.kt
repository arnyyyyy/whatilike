package com.example.whatilike.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.Connection
import org.jsoup.nodes.Document
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object SSLHelper {
    private fun socketFactory(): SSLSocketFactory {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()

            override fun checkClientTrusted(certs: Array<java.security.cert.X509Certificate>, authType: String) {}

            override fun checkServerTrusted(certs: Array<java.security.cert.X509Certificate>, authType: String) {}
        })

        return SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }.socketFactory
    }

    fun getConnection(url: String): Connection {
        return Jsoup.connect(url).sslSocketFactory(socketFactory())
    }
}

const val MUSEUM_SHIFT = 1000000

class HermitageMuseumApiService {
    suspend fun getObjectByID(objectId: Int): ArtObject? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://collections.hermitage.ru/entity/OBJECT/$objectId"
                val document: Document = SSLHelper.getConnection(url).get()

                val imageUrl = document.select("meta[property=og:image]").attr("content")
                    .substringBefore("?")
                    .let {
                        if (it.startsWith("http://")) {
                            it.replace("http://", "https://")
                        } else {
                            it
                        }
                    } + "?w=1000&h=1000"


                val title = document.select("meta[property=og:title]").attr("content")
                val description = document.select("meta[property=og:description]").attr("content")
                val objectURL = document.select("meta[property=og:url]").attr("content")

                if (title.contains("Государственный Эрмитаж", ignoreCase = true) && description.contains("Смотрите музейные коллекции онлайн", ignoreCase = true)) {
                    return@withContext null
                }

                ArtObject(
                    objectID = objectId + MUSEUM_SHIFT,
                    primaryImage = imageUrl,
                    primaryImageSmall = imageUrl,
                    title = title,
                    period = description,
                    objectURL = objectURL
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}