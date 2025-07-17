package com.example.izmirimkartprojesi.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.izmirimkartprojesi.model.DuraklarModel
import com.example.izmirimkartprojesi.services.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BusStopViewModel : ViewModel() {

    private val _busStops = MutableStateFlow<List<DuraklarModel>>(emptyList())
    val busStops: StateFlow<List<DuraklarModel>> = _busStops

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Resource ID must be texted here.
    val RESOURCE_ID = ""

    fun fetchBusStops() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    RetrofitClient.durakApi.getDuraklar(RESOURCE_ID)
                if (response.isSuccessful) {
                    val records = response.body()?.result?.records
                    _busStops.value = records ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Veri çekilemedi: ${e.message}"
            }
        }
    }
}