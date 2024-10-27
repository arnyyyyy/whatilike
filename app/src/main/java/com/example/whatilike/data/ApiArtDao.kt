package com.example.whatilike.data

import retrofit2.Response
import retrofit2.http.GET

data class ArtObjectResponse(val objectIDs: List<Int>)

interface MetMuseumApiService {
    @GET("public/collection/v1/objects")
    suspend fun getAllObjectIDs(): Response<ArtObjectResponse>

    @GET("public/collection/v1/objects/{objectID}")
    suspend fun getObjectByID(@retrofit2.http.Path("objectID") id: Int): Response<ArtObject>
}
