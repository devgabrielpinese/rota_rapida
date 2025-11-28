package com.example.rota_rapida.data.input

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import javax.inject.Inject

class GeocodingService @Inject constructor(
    private val context: Context
) {
    fun geocode(endereco: String): Pair<Double, Double>? = runCatching {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(endereco, 1)
        val first = results?.firstOrNull() ?: return null
        first.latitude to first.longitude
    }.getOrNull()

    fun reverseGeocode(lat: Double, lng: Double): String? = runCatching {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocation(lat, lng, 1)
        results?.firstOrNull()?.getAddressLine(0)
    }.getOrNull()
}
