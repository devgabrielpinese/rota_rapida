package com.example.rota_rapida.presentation.view.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
// ✅ IMPORTS CORRETOS PARA MAPLIBRE (não Mapbox!)
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Componente Compose que encapsula o MapLibre MapView
 *
 * @param modifier Modificador para customização do layout
 * @param onMapReady Callback chamado quando o mapa está pronto para uso
 */
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current

    // ✅ MapLibre não precisa de token (diferente do Mapbox)
    remember {
        MapLibre.getInstance(context)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(null)
                getMapAsync { maplibreMap ->
                    // ✅ Usar estilo padrão do MapLibre
                    maplibreMap.setStyle("https://demotiles.maplibre.org/style.json") {
                        onMapReady(this)
                    }
                }
            }
        },
        update = { mapView ->
            // Atualizações do mapa podem ser feitas aqui
        }
    )
}

/**
 * Versão do MapLibreView com estilo customizável
 */
@Composable
fun MapLibreViewWithStyle(
    modifier: Modifier = Modifier,
    styleUrl: String = "https://demotiles.maplibre.org/style.json",
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current

    remember {
        MapLibre.getInstance(context)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(null)
                getMapAsync { maplibreMap ->
                    maplibreMap.setStyle(styleUrl) {
                        onMapReady(this)
                    }
                }
            }
        }
    )
}