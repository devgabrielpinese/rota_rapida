package com.example.rota_rapida.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rota_rapida.data.input.ManualInputService
import com.example.rota_rapida.data.repository.RouteRepository
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RouteSharedViewModel
@Inject
constructor(
        private val repo: RouteRepository,
        private val manual: ManualInputService,
        private val excelService: com.example.rota_rapida.data.importe.ExcelImportService,
        @dagger.hilt.android.qualifiers.ApplicationContext
        private val context: android.content.Context
) : ViewModel() {

    private val TAG = "RotaRapida"

    // --- Estado Unificado ---
    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    // --- Eventos One-Shot ---
    private val eventQueue = EventQueue<RouteUiEvent>()
    val events = eventQueue.flow

    /** Cria uma nova rota e define como ativa. Substitui a lógica antiga do init. */
    fun iniciarNovaRota() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Agora retorna Rota? tipado corretamente
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

                        // Atualiza estado com a nova parada
                        _uiState.update { current ->
                            val novasParadas = current.paradas + parada
                            current.copy(isLoading = false, paradas = novasParadas, erro = null)
                        }

                        // Persiste a rota atualizada
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
            // Mantém rotaAtiva em sincronia com o que foi salvo
            _uiState.update { it.copy(rotaAtiva = rotaAtualizada) }
        }
    }

    // =========================================================================
    // NOVAS FUNCIONALIDADES DO MENU
    // =========================================================================

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
                // Tenta usar FileProvider se configurado, senão usa Uri.fromFile (pode falhar no
                // N+)
                // Como não podemos alterar o manifesto, assumimos que o FileProvider PODE não estar
                // lá.
                // Mas o requisito pede para gerar arquivo.
                // Vamos tentar pegar a URI de forma segura.
                val uri =
                        try {
                            androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "FileProvider não configurado ou erro: ${e.message}")
                            // Fallback arriscado, mas necessário se não puder mexer no manifesto
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
            // Carrega todas as rotas para exibir no diálogo
            // Como repo.observarRotas() retorna Flow, pegamos o primeiro valor
            val todasRotas =
                    try {
                        kotlinx.coroutines.flow.first(repo.observarRotas())
                    } catch (e: Exception) {
                        emptyList()
                    }
            // Ordena: rota atual primeiro (se existir), depois as outras por data (se tivesse data)
            // Como não temos data fácil, vamos apenas garantir que a atual (se houver) esteja no
            // topo ou filtrada
            val rotaAtualId = _uiState.value.rotaAtiva?.id
            val listaOrdenada = todasRotas.sortedByDescending { it.id == rotaAtualId }

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

            // Adiciona as paradas na rota destino
            // Como repo.addParada adiciona uma por uma, vamos fazer um loop
            // Idealmente o repo teria addParadas(List), mas vamos usar o que tem.
            runCatching {
                paradasAtuais.forEach { p ->
                    // Cria nova instância da parada para a nova rota (novo ID)
                    val novaParada =
                            p.copy(
                                    id = java.util.UUID.randomUUID().toString(),
                                    status = StatusParada.PENDENTE // Reseta status ao copiar
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

            // Otimização simples (Nearest Neighbor)
            // Assume que a primeira parada é o ponto de partida fixo
            val otimizadas = mutableListOf<Parada>()
            val pendentes = paradas.toMutableList()

            // Começa com a primeira da lista atual
            var atual = pendentes.removeAt(0)
            otimizadas.add(atual)

            while (pendentes.isNotEmpty()) {
                // Encontra a mais próxima da 'atual'
                val maisProxima =
                        pendentes.minByOrNull { p ->
                            val lat1 = atual.latitude ?: 0.0
                            val lng1 = atual.longitude ?: 0.0
                            val lat2 = p.latitude ?: 0.0
                            val lng2 = p.longitude ?: 0.0
                            // Distância Euclidiana simples para performance (funciona bem para
                            // pequenas distâncias)
                            // Para precisão geográfica real, usaríamos Haversine, mas aqui é
                            // MVP/rápido
                            (lat1 - lat2) * (lat1 - lat2) + (lng1 - lng2) * (lng1 - lng2)
                        }

                if (maisProxima != null) {
                    pendentes.remove(maisProxima)
                    otimizadas.add(maisProxima)
                    atual = maisProxima
                } else {
                    // Fallback (não deve acontecer se a lista não estiver vazia)
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

            // O requisito diz: "paradas DESORGANIZADAS, exatamente na ordem original de importação"
            // Como não podemos adicionar campo 'criadoEm', e a lista 'paradas' pode ter sido
            // reordenada,
            // NÃO TEMOS COMO saber a ordem original se ela já foi perdida.
            // O usuário disse: "A ordem original da planilha JÁ está preservada naturalmente no
            // fluxo atual."
            // Isso implica que se não reordenarmos, ela está lá. Mas se o usuário clicou em
            // "Reotimizar", perdeu.
            // Como o usuário proibiu alterar o model, vamos imprimir a lista ATUAL mesmo,
            // ou se tivermos sorte, a lista nunca foi reordenada permanentemente se não salvamos...
            // mas salvamos em 'salvarRotaAtual'.
            // Então, vamos imprimir a lista como está no uiState.

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
        viewModelScope.launch { eventQueue.send(RouteUiEvent.ShowRemoveDialog(paradas)) }
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
