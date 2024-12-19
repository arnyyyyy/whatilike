package com.example.whatilike.data


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


val harvardClient = OkHttpClient()

data class ExtractedRecord(
    val objectNumber: String?,
    val title: String?,
    val dated: String?,
    val description: String?,
    val imageUrl: String?,
    val url: String?
)

const val HARVARD_MUSEUM_SHIFT = 2000000

class HarvardMuseumApiService {
    suspend fun getObjectByID(objectId: Int): ArtObject? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "f9072a66-2e01-49ea-bdd8-c6c35f605790"


                val url = "https://api.harvardartmuseums.org/object?apikey=$apiKey&id=$objectId"

                val request = Request.Builder().url(url).build()

                var record = ExtractedRecord("", "", "", "", "", "")

                harvardClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    response.close()
                    println(responseData)
                    val jsonObject = JSONObject(responseData!!)
                    val records = jsonObject.getJSONArray("records")
                    if (records.length() == 0) {
                        return@withContext null
                    }
                    val firstRecord = records.getJSONObject(0)

                    val objectNumber = firstRecord.getString("objectnumber")
                    val title = firstRecord.optString("title")
                    val dated = firstRecord.optString("dated")
                    val description =
                        firstRecord.optString("description")
                    val imageUrl = firstRecord.optString("primaryimageurl", "")
                    val baseUrl = firstRecord.optString("url")
                    record = ExtractedRecord(
                        objectNumber = objectNumber,
                        title = title,
                        dated = dated,
                        description = description ?: "No description available",
                        imageUrl = imageUrl,
                        url = baseUrl
                    )

                }

                harvardClient.connectionPool.evictAll()

                if (record.imageUrl.isNullOrEmpty() || record.imageUrl == "null") {
                    return@withContext null
                }
                else {
                    Log.d("Harvard", record.imageUrl!!)
                }

                println("Title: ${record.title}")
                println("Description: ${record.description}")
                println("Object Number: ${record.objectNumber}")
                println("Date: ${record.dated}")
                println("Image URL: ${record.imageUrl}")
                println("Object URL: ${record.url}")

                return@withContext ArtObject(
                    objectID = objectId + HARVARD_MUSEUM_SHIFT,
                    primaryImage = record.imageUrl,
                    primaryImageSmall = record.imageUrl,
                    title = record.title!!,
                    period = record.description,
                    objectURL = record.url
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}