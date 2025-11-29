package com.example.rota_rapida.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rota_rapida.data.input.ManualInputService
import com.example.rota_rapida.data.importe.ExcelImportService
import com.example.rota_rapida.data.repository.RouteRepository
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RouteSharedViewModel @Inject constructor(
    private val repo: RouteRepository,
    private val manual: ManualInputService,
    private val excelService: ExcelImportService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "RotaRapida"

    // --- Estado Unificado ---
    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    // --- Eventos One-Shot ---
    private val eventQueue = EventQueue<RouteUiEvent>()
    val events = eventQueue.flow

    // ========================================================================
    // FLUXO PRINCIPAL DE ROTA
    // ========================================================================

    /** Cria ou carrega a rota atual e define como ativa. */
    fun iniciarNovaRota() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val rotaExistente = repo.getRotaAtual()

            if (rotaExistente != null) {
                Log.d(TAG, "Rota existente carregada: ${rotaExistente.id}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rotaAtiva = rotaExistente,
                        paradas = rotaExistente.paradas,
                        isPrimeiraRota = false
                    )
                }
            } else {
                val novaRota =
                    Rota(
                        id = java.util.UUID.randomUUID().toString(),
                        nome = "Rota de hoje",
                        paradas = emptyList()
                    )

                repo.saveRota(novaRota)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rotaAtiva = novaRota,
                        paradas = emptyList(),
                        isPrimeiraRota = true
                    )
                }
                Log.d(TAG, "Nova rota iniciada: ${novaRota.id}")
            }
        }
    }

    fun adicionarParada(endereco: String) {
        if (endereco.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d(TAG, "Adicionando parada: $endereco")

            runCatching { manual.addParadaByText(endereco) }
                .onSuccess { res ->
                    val base = res.getOrNull()
                    val parada =
                        Parada(
                            id = java.util.UUID.randomUUID().toString(),
                            endereco = base?.endereco ?: endereco,
                            latitude = base?.latitude,
                            longitude = base?.longitude,
                            status = StatusParada.PENDENTE
                        )

                    _uiState.update { current ->
                        val novasParadas = current.paradas + parada
                        current.copy(isLoading = false, paradas = novasParadas, erro = null)
                    }

                    salvarRotaAtual()

                    Log.d(TAG, "Parada adicionada com sucesso: ${parada.endereco}")
                    eventQueue.send(RouteUiEvent.ShowMessage("Parada adicionada!"))
                }
                .onFailure { e ->
                    Log.e(TAG, "Erro ao adicionar parada", e)
                    _uiState.update { it.copy(isLoading = false, erro = e.message) }
                    eventQueue.send(
                        RouteUiEvent.ShowError("Erro ao adicionar parada: ${e.message}")
                    )
                }
        }
    }

    fun atualizarStatusParada(paradaId: String, novoStatus: StatusParada) {
        _uiState.update { current ->
            val novasParadas =
                current.paradas.map { p ->
                    if (p.id == paradaId) p.copy(status = novoStatus) else p
                }
            current.copy(paradas = novasParadas)
        }
        viewModelScope.launch { salvarRotaAtual() }
    }

    fun removerParadasConcluidas() {
        _uiState.update { current ->
            val novasParadas = current.paradas.filter { it.status == StatusParada.PENDENTE }
            current.copy(paradas = novasParadas)
        }
        viewModelScope.launch {
            salvarRotaAtual()
            eventQueue.send(RouteUiEvent.ShowMessage("Paradas concluídas removidas."))
        }
    }

    fun importarPlanilha(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val importadas = excelService.importarExcel(uri)
                if (importadas.isNotEmpty()) {
                    val novasParadas =
                        importadas.map { item ->
                            Parada(
                                id = java.util.UUID.randomUUID().toString(),
                                endereco = item.destinationAddress ?: "",
                                latitude = item.latitude,
                                longitude = item.longitude,
                                status = StatusParada.PENDENTE
                            )
                        }
                    _uiState.update { current ->
                        current.copy(paradas = current.paradas + novasParadas, isLoading = false)
                    }
                    salvarRotaAtual()
                    eventQueue.send(
                        RouteUiEvent.ShowMessage("${novasParadas.size} paradas importadas.")
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    eventQueue.send(
                        RouteUiEvent.ShowMessage("Nenhuma parada encontrada na planilha.")
                    )
                }
            }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    Log.e(TAG, "Erro ao importar planilha", e)
                    eventQueue.send(RouteUiEvent.ShowMessage("Erro ao importar: ${e.message}"))
                }
        }
    }

    private suspend fun salvarRotaAtual() {
        val state = _uiState.value
        state.rotaAtiva?.let { rota ->
            val rotaAtualizada = rota.copy(paradas = state.paradas)
            repo.saveRota(rotaAtualizada)
            _uiState.update { it.copy(rotaAtiva = rotaAtualizada) }
        }
    }

    // ========================================================================
    // FUNCIONALIDADES DO MENU (3 PONTOS)
    // ========================================================================

    fun shareRoute() {
        viewModelScope.launch {
            val paradas = _uiState.value.paradas
            if (paradas.isEmpty()) {
                eventQueue.send(RouteUiEvent.ShowMessage("Rota vazia, nada para compartilhar."))
                return@launch
            }

            val content = paradas.joinToString("\n") { it.endereco }
            val file = java.io.File(context.cacheDir, "rota_compartilhada.txt")

            runCatching {
                file.writeText(content)
                val uri =
                    try {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "FileProvider não configurado ou erro: ${e.message}")
                        android.net.Uri.fromFile(file)
                    }
                eventQueue.send(RouteUiEvent.ShareFile(uri))
            }
                .onFailure { e ->
                    Log.e(TAG, "Erro ao gerar arquivo de compartilhamento", e)
                    eventQueue.send(
                        RouteUiEvent.ShowError("Erro ao gerar arquivo: ${e.message}")
                    )
                }
        }
    }

    fun loadRoutesForCopy() {
        viewModelScope.launch {
            val todasRotas: List<Rota> =
                try {
                    repo.observarRotas().first()
                } catch (e: Exception) {
                    emptyList()
                }

            val rotaAtualId = _uiState.value.rotaAtiva?.id
            val listaOrdenada = todasRotas.sortedByDescending { rota ->
                rota.id == rotaAtualId
            }

            eventQueue.send(RouteUiEvent.ShowCopyDialog(listaOrdenada))
        }
    }

    fun copyStopsToRoute(targetRota: Rota) {
        viewModelScope.launch {
            val paradasAtuais = _uiState.value.paradas
            if (paradasAtuais.isEmpty()) {
                eventQueue.send(RouteUiEvent.ShowMessage("Rota atual não tem paradas para copiar."))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                paradasAtuais.forEach { p ->
                    val novaParada =
                        p.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            status = StatusParada.PENDENTE
                        )
                    repo.addParada(targetRota.id, novaParada)
                }
                eventQueue.send(
                    RouteUiEvent.ShowMessage("Paradas copiadas para ${targetRota.nome}")
                )
            }
                .onFailure { e ->
                    Log.e(TAG, "Erro ao copiar paradas", e)
                    eventQueue.send(RouteUiEvent.ShowError("Erro ao copiar: ${e.message}"))
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun optimizeRoute() {
        viewModelScope.launch {
            val paradas = _uiState.value.paradas
            if (paradas.size < 3) {
                eventQueue.send(RouteUiEvent.ShowMessage("Poucas paradas para otimizar."))
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val otimizadas = mutableListOf<Parada>()
            val pendentes = paradas.toMutableList()

            var atual = pendentes.removeAt(0)
            otimizadas.add(atual)

            while (pendentes.isNotEmpty()) {
                val maisProxima =
                    pendentes.minByOrNull { p ->
                        val lat1 = atual.latitude ?: 0.0
                        val lng1 = atual.longitude ?: 0.0
                        val lat2 = p.latitude ?: 0.0
                        val lng2 = p.longitude ?: 0.0
                        (lat1 - lat2) * (lat1 - lat2) + (lng1 - lng2) * (lng1 - lng2)
                    }

                if (maisProxima != null) {
                    pendentes.remove(maisProxima)
                    otimizadas.add(maisProxima)
                    atual = maisProxima
                } else {
                    break
                }
            }

            _uiState.update { it.copy(paradas = otimizadas, isLoading = false) }
            salvarRotaAtual()
            eventQueue.send(RouteUiEvent.ShowMessage("Rota reotimizada!"))
        }
    }

    fun printRoute() {
        viewModelScope.launch {
            val paradas = _uiState.value.paradas
            if (paradas.isEmpty()) {
                eventQueue.send(RouteUiEvent.ShowMessage("Nada para imprimir."))
                return@launch
            }

            val content = paradas.joinToString("\n") { it.endereco }
            val file = java.io.File(context.cacheDir, "rota_impressao.txt")

            runCatching {
                file.writeText(content)
                val uri =
                    try {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    } catch (e: Exception) {
                        android.net.Uri.fromFile(file)
                    }
                eventQueue.send(RouteUiEvent.PrintFile(uri))
            }
                .onFailure { e ->
                    eventQueue.send(
                        RouteUiEvent.ShowError("Erro ao gerar impressão: ${e.message}")
                    )
                }
        }
    }

    fun openRemoveDialog() {
        val paradas = _uiState.value.paradas
        if (paradas.isEmpty()) {
            viewModelScope.launch {
                eventQueue.send(RouteUiEvent.ShowMessage("Nenhuma parada para remover."))
            }
            return
        }
        viewModelScope.launch {
            eventQueue.send(RouteUiEvent.ShowRemoveDialog(paradas))
        }
    }

    fun removeStops(idsToRemove: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentParadas = _uiState.value.paradas
            val novasParadas = currentParadas.filterNot { it.id in idsToRemove }

            _uiState.update { it.copy(paradas = novasParadas, isLoading = false) }
            salvarRotaAtual()
            eventQueue.send(RouteUiEvent.ShowMessage("${idsToRemove.size} paradas removidas."))
        }
    }
}
