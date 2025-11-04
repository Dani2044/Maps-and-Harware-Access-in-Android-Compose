package com.example.taller2.map

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

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
    fun fetchRoute(origin: LatLng, destination: LatLng): List<LatLng> {
        val client = OkHttpClient() //https://square.github.io/okhttp/
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origin.longitude},${origin.latitude};" +
                "${destination.longitude},${destination.latitude}" +
                "?overview=full&geometries=polyline" //https://project-osrm.org/docs/v5.7.0/api/#requests

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()

        val json = JSONObject(body)
        val routes = json.optJSONArray("routes") ?: return emptyList()
        if (routes.length() == 0) return emptyList()

        val geometry = routes.getJSONObject(0).optString("geometry")
        return if (geometry.isNullOrBlank()) emptyList() else PolyUtil.decode(geometry) //https://developers.google.com/maps/documentation/routes/polylinedecoder
    }
}