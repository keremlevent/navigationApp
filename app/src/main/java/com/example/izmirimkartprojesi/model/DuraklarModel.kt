package com.example.izmirimkartprojesi.model

import com.google.gson.annotations.SerializedName

data class DuraklarModel(
    @SerializedName("DURAK_ID")
    val id: Int,
    @SerializedName("DURAK_ADI")
    val name: String,
    @SerializedName("ENLEM")
    val latitude: Double,
    @SerializedName("BOYLAM")
    val longitude: Double,
    @SerializedName("DURAKTAN_GECEN_HATLAR")
    val routes: String?
)

data class ResponseModel(
    val result: ResultModel
)

data class ResultModel(
    val records: List<DuraklarModel>
)

