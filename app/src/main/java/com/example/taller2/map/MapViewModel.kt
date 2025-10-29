package com.example.taller2.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {
    private val uiVar = MutableStateFlow(MapUIState())
    val uI: StateFlow<MapUIState> = uiVar

    fun setPlace(value: String) = uiVar.update { it.copy(place = value) }
    fun addMarker(p: LatLng, title: String, snippet: String = "") =
        uiVar.update { it.copy(markers = it.markers + MyMarker(p, title, snippet)) }

    fun clearMarkers() = uiVar.update {
        it.copy(
            markers = emptyList(),
            showCurrentMarker = false,
            routePoints = emptyList()
        )
    }

    fun showCurrentMarker() = uiVar.update { it.copy(showCurrentMarker = true) }
    fun hideCurrentMarker() = uiVar.update { it.copy(showCurrentMarker = false) }
    fun loadUserPath(ctx: Context) =
        uiVar.update { it.copy(userPathPoints = MapUtils.readLocationsFromFile(ctx)) }

    fun setDarkMode(isDark: Boolean) = uiVar.update { it.copy(darkMode = isDark) }

    fun setRoute(points: List<LatLng>) = uiVar.update { it.copy(routePoints = points) }
    fun toggleUserPathColor() = uiVar.update { it.copy(useAltPathColor = !it.useAltPathColor) }
}