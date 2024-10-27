package com.example.whatilike.api

import retrofit2.Response
import retrofit2.http.GET

data class ArtObject(
    val objectID: Int,
    val primaryImage: String?,
    val title: String,
    val artistDisplayName: String
)

data class ArtObjectResponse(val objectIDs: List<Int>)

interface MetMuseumApiService {
    @GET("public/collection/v1/objects")
    suspend fun getAllObjectIDs(): Response<ArtObjectResponse>

    @GET("public/collection/v1/objects/{objectID}")
    suspend fun getObjectByID(@retrofit2.http.Path("objectID") id: Int): Response<ArtObject>
}
