package com.example.rota_rapida.presentation.view.route

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rota_rapida.domain.model.StatusParada
import com.example.rota_rapida.presentation.ui.components.MoreOptionsMenu
import com.example.rota_rapida.presentation.utils.MapUtils
import com.example.rota_rapida.presentation.view.components.ListaParadas
import com.example.rota_rapida.presentation.view.scanner.QRScannerActivity
import com.example.rota_rapida.presentation.viewmodel.RouteSharedViewModel
import com.example.rota_rapida.presentation.viewmodel.RouteUiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaParadasScreen(viewModel: RouteSharedViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showCopyDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var routesForCopy by remember {
        mutableStateOf<List<com.example.rota_rapida.domain.model.Rota>>(emptyList())
    }
    var stopsForRemove by remember {
        mutableStateOf<List<com.example.rota_rapida.domain.model.Parada>>(emptyList())
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is RouteUiEvent.ShowMessage -> snackbar.showSnackbar(ev.message)
                is RouteUiEvent.ShowError -> snackbar.showSnackbar(ev.message)
                is RouteUiEvent.ShareFile -> {
                    val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, ev.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                    context.startActivity(Intent.createChooser(intent, "Compartilhar rota"))
                }
                is RouteUiEvent.PrintFile -> {
                    val intent =
                            Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(ev.uri, "text/plain")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                    context.startActivity(Intent.createChooser(intent, "Imprimir/Visualizar"))
                }
                is RouteUiEvent.ShowCopyDialog -> {
                    routesForCopy = ev.routes
                    showCopyDialog = true
                }
                is RouteUiEvent.ShowRemoveDialog -> {
                    stopsForRemove = ev.paradas
                    showRemoveDialog = true
                }
                else -> {}
            }
        }
    }

    if (showCopyDialog) {
        CopyStopsDialog(
                routes = routesForCopy,
                onDismiss = { showCopyDialog = false },
                onConfirm = { targetRota ->
                    viewModel.copyStopsToRoute(targetRota)
                    showCopyDialog = false
                }
        )
    }

    if (showRemoveDialog) {
        RemoveStopsDialog(
                paradas = stopsForRemove,
                onDismiss = { showRemoveDialog = false },
                onConfirm = { idsToRemove ->
                    viewModel.removeStops(idsToRemove)
                    showRemoveDialog = false
                }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = Color.Transparent) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(Modifier.fillMaxSize()) {

                // MAPA (50%)
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    MapaContent(
                            context = context,
                            uiState = uiState,
                            paradaSelecionada = paradaSelecionada,
                            onMapReady = { mapRef = it },
                            onMarkerClick = { id ->
                                paradaSelecionada = id
                                val idx = uiState.paradas.indexOfFirst { it.id == id }
                                if (idx != -1) scope.launch { listState.animateScrollToItem(idx) }
                            }
                    )

                    IconButton(
                            onClick = {
                                scope.launch {
                                    snackbar.showSnackbar("Menu lateral ainda não implementado")
                                }
                            },
                            modifier =
                                    Modifier.align(Alignment.TopStart)
                                            .padding(16.dp)
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.surface.copy(
                                                            alpha = 0.9f
                                                    )
                                            )
                    ) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                }

                // LISTA (50%)
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    ListaParadas(
                            paradas = uiState.paradas,
                            listState = listState,
                            onToggleStatus = { p ->
                                val novo =
                                        if (p.status == StatusParada.ENTREGUE) StatusParada.PENDENTE
                                        else StatusParada.ENTREGUE
                                viewModel.atualizarStatusParada(p.id, novo)
                            },
                            onSelectParada = { p ->
                                paradaSelecionada = p.id
                                if (p.latitude != null && p.longitude != null) {
                                    mapRef?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(p.latitude, p.longitude),
                                                    15.0
                                            )
                                    )
                                }
                            }
                    )
                }
            }

            // BARRA CIRCUIT
            CircuitActionBar(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                    viewModel = viewModel,
                    snackbarHostState = snackbar
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun CircuitActionBar(
        modifier: Modifier,
        viewModel: RouteSharedViewModel,
        snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current
    var endereco by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val scannerLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    res ->
                if (res.resultCode == android.app.Activity.RESULT_OK) {
                    res.data?.getStringExtra("qr_text")?.let { viewModel.adicionarParada(it) }
                }
            }

    val planilhaLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { viewModel.importarPlanilha(it) }
            }

    Surface(
            modifier = modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp
    ) {
        Row(
                Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    Modifier.weight(1f)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                BasicTextField(
                        value = endereco,
                        onValueChange = { endereco = it },
                        singleLine = true,
                        textStyle =
                                TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions =
                                KeyboardActions(
                                        onDone = {
                                            if (endereco.isNotBlank()) {
                                                viewModel.adicionarParada(endereco)
                                                endereco = ""
                                            }
                                        }
                                ),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )

                if (endereco.isBlank()) {
                    Text(
                            "Toque para adicionar",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                    onClick = {
                        val intent = Intent(context, QRScannerActivity::class.java)
                        scannerLauncher.launch(intent)
                    }
            ) { Icon(Icons.Default.QrCode, null) }

            IconButton(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Entrada por voz ainda não implementada")
                        }
                    }
            ) { Icon(Icons.Default.Mic, null) }

            MoreOptionsMenu(
                    onShareCopyRoute = { viewModel.shareRoute() },
                    onCopyStops = { viewModel.loadRoutesForCopy() },
                    onReoptimizeRoute = { viewModel.optimizeRoute() },
                    onImportSpreadsheet = {
                        planilhaLauncher.launch(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                    },
                    onPrintRoute = { viewModel.printRoute() },
                    onRemoveStops = { viewModel.openRemoveDialog() }
            )
        }
    }
}

@Composable
fun MapaContent(
        context: Context,
        uiState: com.example.rota_rapida.presentation.viewmodel.RouteUiState,
        paradaSelecionada: String?,
        onMapReady: (MapLibreMap) -> Unit,
        onMarkerClick: (String) -> Unit
) {

    MapLibre.getInstance(context)

    val mapView = remember {
        MapView(context).apply {
            layoutParams =
                    ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
        }
    }

    val scope = rememberCoroutineScope()
    val symbolIds = remember { mutableMapOf<Long, String>() }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }

    AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { mapLibre ->
                    onMapReady(mapLibre)

                    if (symbolManager != null) {
                        scope.launch {
                            renderMarkersAndCamera(
                                    mapLibre,
                                    symbolManager!!,
                                    uiState,
                                    paradaSelecionada,
                                    symbolIds
                            )
                        }
                    } else {
                        mapLibre.setStyle(Style.Builder().fromUri(MapUtils.STADIA_STYLE_URL)) {
                                style ->
                            style.addImage("marker-default", makeMarker(context))

                            val sm =
                                    SymbolManager(view, mapLibre, style).apply {
                                        iconAllowOverlap = true
                                        iconIgnorePlacement = true
                                        addClickListener { symbol ->
                                            symbolIds[symbol.id]?.let(onMarkerClick)
                                            true
                                        }
                                    }

                            symbolManager = sm

                            scope.launch {
                                renderMarkersAndCamera(
                                        mapLibre,
                                        sm,
                                        uiState,
                                        paradaSelecionada,
                                        symbolIds
                                )
                            }
                        }
                    }
                }
            }
    )
}

private fun renderMarkersAndCamera(
        map: MapLibreMap,
        sm: SymbolManager,
        uiState: com.example.rota_rapida.presentation.viewmodel.RouteUiState,
        paradaSelecionada: String?,
        ids: MutableMap<Long, String>
) {
    sm.deleteAll()
    ids.clear()

    val bounds = LatLngBounds.Builder()
    val markers = mutableListOf<Symbol>()

    uiState.paradas.forEach { p ->
        val latLng =
                LatLng(
                        p.latitude ?: MapUtils.SAO_PAULO_CENTER.latitude,
                        p.longitude ?: MapUtils.SAO_PAULO_CENTER.longitude
                )

        val marker =
                sm.create(
                        SymbolOptions()
                                .withLatLng(latLng)
                                .withIconImage("marker-default")
                                .withIconSize(if (p.id == paradaSelecionada) 1.3f else 1f)
                )

        ids[marker.id] = p.id
        markers.add(marker)
        bounds.include(latLng)
    }

    if (markers.isNotEmpty() && paradaSelecionada == null) {
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
    } else if (markers.isEmpty()) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapUtils.SAO_PAULO_CENTER, 12.0))
    }
}

private fun makeMarker(context: Context, dpSize: Float = 30f): Bitmap {
    val px = (dpSize * context.resources.displayMetrics.density).toInt()
    val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor("#1976D2") }

    val stroke =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }

    val radius = px / 2f
    c.drawCircle(radius, radius, radius - 4f, fill)
    c.drawCircle(radius, radius, radius - 4f, stroke)

    return bmp
}

@Composable
fun CopyStopsDialog(
        routes: List<com.example.rota_rapida.domain.model.Rota>,
        onDismiss: () -> Unit,
        onConfirm: (com.example.rota_rapida.domain.model.Rota) -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Copiar paradas para...") },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(routes.size) { i ->
                        val rota = routes[i]
                        TextButton(
                                onClick = { onConfirm(rota) },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(rota.nome, modifier = Modifier.weight(1f))
                            Text("${rota.paradas.size} paradas", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun RemoveStopsDialog(
        paradas: List<com.example.rota_rapida.domain.model.Parada>,
        onDismiss: () -> Unit,
        onConfirm: (List<String>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<String>() }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Remover paradas") },
            text = {
                Column {
                    Button(
                            onClick = {
                                val concluidas =
                                        paradas.filter { it.status == StatusParada.ENTREGUE }.map {
                                            it.id
                                        }
                                selectedIds.clear()
                                selectedIds.addAll(concluidas)
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) { Text("Selecionar concluídas") }

                    androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 300.dp)) {
                        items(paradas.size) { i ->
                            val p = paradas[i]
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .clickable {
                                                        if (selectedIds.contains(p.id))
                                                                selectedIds.remove(p.id)
                                                        else selectedIds.add(p.id)
                                                    }
                                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                        checked = selectedIds.contains(p.id),
                                        onCheckedChange = { chk ->
                                            if (chk) selectedIds.add(p.id)
                                            else selectedIds.remove(p.id)
                                        }
                                )
                                Text(
                                        p.endereco,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(selectedIds.toList()) }) { Text("Remover") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
