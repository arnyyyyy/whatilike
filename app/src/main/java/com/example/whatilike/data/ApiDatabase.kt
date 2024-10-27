package com.example.whatilike.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiDatabase {
    private const val BASE_URL = "https://collectionapi.metmuseum.org/"

    val apiService: MetMuseumApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MetMuseumApiService::class.java)
    }
}
