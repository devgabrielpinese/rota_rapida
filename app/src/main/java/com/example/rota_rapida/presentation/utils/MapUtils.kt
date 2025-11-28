package com.example.rota_rapida.presentation.utils

import com.example.rota_rapida.domain.model.Parada
import kotlin.math.abs
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

/**
 * Utilitários de mapa compatíveis com MapLibre + Stadia Maps. Arquivo limpo, sem duplicações e sem
 * caracteres invisíveis.
 */
object MapUtils {

    /**
     * URL do estilo Stadia Maps (LIGHT) com API KEY já incluída. Se mudar a key, troque aqui.
     *
     * Opções de estilos Stadia comuns:
     * - alidade_smooth.json → mapa claro padrão
     * - alidade_smooth_dark.json → mapa escuro
     */
    const val STADIA_STYLE_URL =
        "https://tiles.stadiamaps.com/styles/alidade_smooth.json?api_key=dba67619-5278-4fe0-98a3-931b9c5e89ac"



    /** Centro de São Paulo (fallback quando não há coordenadas) */
    val SAO_PAULO_CENTER = LatLng(-23.55052, -46.63331)

    /** Bounds aproximados de SP (SW → NE), usado para fitbounds */
    val SAO_PAULO_BOUNDS: LatLngBounds =
            LatLngBounds.Builder()
                    .include(LatLng(-23.8650, -46.9990))
                    .include(LatLng(-23.3500, -46.3600))
                    .build()

    /** Converte Parada → LatLng; retorna null se latitude/longitude forem inválidas */
    fun paradaToLatLng(parada: Parada): LatLng? {
        val lat = parada.latitude
        val lon = parada.longitude
        if (lat == null || lon == null) return null
        if (!lat.isFinite() || !lon.isFinite()) return null
        if (abs(lat) > 90.0 || abs(lon) > 180.0) return null
        return LatLng(lat, lon)
    }

    /** Lista de LatLng válidos */
    fun paradasToLatLngList(paradas: List<Parada>): List<LatLng> =
            paradas.mapNotNull { paradaToLatLng(it) }

    /** Calcula bounds a partir de pontos; retorna null se vazio */
    fun computeBounds(points: List<LatLng>): LatLngBounds? {
        if (points.isEmpty()) return null
        val b = LatLngBounds.Builder()
        points.forEach { b.include(it) }
        return b.build()
    }

    /** Retorna bounds calculados OU fallback de São Paulo */
    fun boundsOrSaoPaulo(paradas: List<Parada>): LatLngBounds {
        val pts = paradasToLatLngList(paradas)
        return computeBounds(pts) ?: SAO_PAULO_BOUNDS
    }
}
