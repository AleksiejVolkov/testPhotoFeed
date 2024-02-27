package com.offmind.photofeed.network

import com.offmind.photofeed.model.PhotoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApiService {
    @GET("v1/curated")
    suspend fun getCuratedPhotos(
        @Header("Authorization") apiKey: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): PhotoResponse
}