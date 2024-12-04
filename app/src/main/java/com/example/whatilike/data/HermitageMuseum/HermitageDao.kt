package com.example.whatilike.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Path
import org.jsoup.Jsoup
import retrofit2.Response


//fun HermitageArtObjectResponse.toArtObject(): ArtObject {
//    return ArtObject(
//        objectID = this.id,
//        title = this.title,
//        artistDisplayName = this.artist ?: "Unknown",
//        primaryImage = this.imageUrl ?: "",
//        primaryImageSmall = this.thumbnailUrl ?: ""
//    )
//}


class HermitageMuseumApiService {
    suspend fun getObjectByID(objectId: Int): ArtObject? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://collections.hermitage.ru/entity/OBJECT/$objectId"
                val document = Jsoup.connect(url).get()

//                val imageUrl =
//                    document.select("meta[property=og:image]").attr("content").substringBefore("?") + "?w=500&h=500"
                val imageUrl = document.select("meta[property=og:image]").attr("content")
                    .substringBefore("?")
                    .let {
                        if (it.startsWith("http://")) {
                            it.replace("http://", "https://")
                        } else {
                            it
                        }
                    } + "?w=500&h=500"

                val title = document.select("meta[property=og:title]").attr("content")
                val description = document.select("meta[property=og:description]").attr("content")
                val objectURL = document.select("meta[property=og:url]").attr("content")

                if (title.contains("Государственный Эрмитаж", ignoreCase = true) && description.contains("Смотрите музейные коллекции онлайн", ignoreCase = true)) {
                    println("Объект $objectId пропущен: данные отсутствуют")
                    return@withContext null
                }
                else {
                    println("Объект $objectId с кайфом принят")

                }

                ArtObject(
                    objectID = objectId,
                    primaryImage = imageUrl,
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