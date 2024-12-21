package com.example.whatilike.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

val harvardClient = OkHttpClient()

data class HarvardArtObject(
    val objectID: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val artistDisplayName: String = "",
    val objectDate: String = "",
    val description: String = "",
    val culture: String = "",
    val objectURL: String = ""
)

const val HARVARD_MUSEUM_SHIFT = 2000000

class HarvardMuseumApiService {
    suspend fun getObjectByID(objectId: Int): ArtObject? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "f9072a66-2e01-49ea-bdd8-c6c35f605790"

                val url = "https://api.harvardartmuseums.org/object?apikey=$apiKey&id=$objectId"

                val request = Request.Builder().url(url).build()

                var record = HarvardArtObject()

                harvardClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    response.close()

                    val jsonObject = JSONObject(responseData!!)
                    val records = jsonObject.getJSONArray("records")
                    if (records.length() == 0) {
                        return@withContext null
                    }
                    val firstRecord = records.getJSONObject(0)

                    val objectID = firstRecord.getString("objectnumber")
                    val imageUrl = firstRecord.optString("primaryimageurl", "")
                    val title = firstRecord.optString("title", "")
                    val objectDate = firstRecord.optString("dated", "")
                    val description = firstRecord.optString("description", "")
                    val culture = firstRecord.optString("culture", "")

                    val objectURL = firstRecord.optString("url", "")

                    val people = firstRecord.getJSONArray("people")
                    var artistDisplayName = ""
                    if (people.length() > 0) {
                        val firstAuthor = people.getJSONObject(0)
                        artistDisplayName = firstAuthor.optString("displayname", "")
                    }

                    record = HarvardArtObject(
                        objectID = objectID,
                        imageUrl = imageUrl,
                        title = title,
                        artistDisplayName = artistDisplayName,
                        objectDate = objectDate,
                        description = description ?: "No description available",
                        culture = culture,
                        objectURL = objectURL
                    )

                }

                harvardClient.connectionPool.evictAll()

                if (record.imageUrl.isBlank()|| record.imageUrl == "null") {
                    return@withContext null
                }
                else {
                    Log.d("Harvard", record.imageUrl)
                }


                return@withContext ArtObject(
                    objectID = objectId + HARVARD_MUSEUM_SHIFT,
                    primaryImage = record.imageUrl,
                    primaryImageSmall = record.imageUrl,
                    title = record.title,
                    artistDisplayName = record.artistDisplayName,
                    objectDate = record.objectDate,
                    period = record.description,
                    culture = record.culture,
                    objectURL = record.objectURL
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}