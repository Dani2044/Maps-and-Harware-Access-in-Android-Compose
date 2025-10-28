package com.example.taller2.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {
    private val _ui = MutableStateFlow(MapUIState())
    val uI: StateFlow<MapUIState> = _ui

    fun setPlace(value: String) = _ui.update { it.copy(place = value) }
    fun addMarker(p: LatLng, title: String, snippet: String = "") = _ui.update { it.copy(markers = it.markers + MyMarker(p, title, snippet)) }
    fun clearMarkers() = _ui.update { it.copy(markers = emptyList(), showCurrentMarker = false) }
    fun showCurrentMarker() = _ui.update { it.copy(showCurrentMarker = true) }
    fun hideCurrentMarker() = _ui.update { it.copy(showCurrentMarker = false) }
    fun loadUserPath(ctx: Context) = _ui.update { it.copy(userPathPoints = MapUtils.readLocationsFromFile(ctx)) }
    fun setDarkMode(isDark: Boolean) = _ui.update { it.copy(darkMode = isDark) }
}