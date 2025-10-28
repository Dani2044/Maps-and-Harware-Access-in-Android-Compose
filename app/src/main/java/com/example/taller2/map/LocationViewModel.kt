package com.example.taller2.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LocationViewModel : ViewModel() {
    val uiStateVar = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = uiStateVar

    fun update(lat: Double, lon: Double) {
        uiStateVar.update { it.copy(lat, lon) }
    }
}