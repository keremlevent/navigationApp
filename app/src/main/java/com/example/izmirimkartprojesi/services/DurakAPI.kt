package com.example.izmirimkartprojesi.services

import com.example.izmirimkartprojesi.model.ResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DurakAPI {
    @GET("api/3/action/datastore_search")
    suspend fun getDuraklar(
        @Query("resource_id") resourceId: String,
        @Query("limit") limit: Int = 100
    ): Response<ResponseModel>
}
