package com.example.taller2.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {
    val uIVar = MutableStateFlow(MapUIState())
    val uI: StateFlow<MapUIState> = uIVar

    fun setPlace(value: String) = uIVar.update {
        it.copy(place = value)
    }
    fun setSearchMarker(p: LatLng?) = uIVar.update {
        it.copy(searchMarker = p)
    }
    fun setLongClickMarker(p: LatLng?) = uIVar.update {
        it.copy(longClickMarker = p)
    }
    fun setRoute(points: List<LatLng>) = uIVar.update {
        it.copy(routePoints = points)
    }
    fun clearOverlays() = uIVar.update {
        it.copy(
            place = "",
            searchMarker = null,
            longClickMarker = null,
            routePoints = emptyList(),
            userPathPoints = emptyList()
        )
    }
    fun loadUserPath(ctx: Context) = uIVar.update {
        it.copy(userPathPoints = MapUtils.readLocationsFromFile(ctx))
    }
    fun setDarkMode(isDark: Boolean) = uIVar.update {
        it.copy(darkMode = isDark)
    }
}