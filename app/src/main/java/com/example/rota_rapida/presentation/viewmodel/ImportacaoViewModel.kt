package com.example.rota_rapida.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.rota_rapida.domain.model.Parada // 👈 ADICIONE ESTA LINHA

/**
 * ViewModel para tela de importação de rotas
 */
class ImportacaoViewModel @Inject constructor() : ViewModel() {

    // Estado de carregamento
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensagem de erro
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Paradas importadas
    private val _paradasImportadas = MutableStateFlow<List<Parada>>(emptyList())
    val paradasImportadas: StateFlow<List<Parada>> = _paradasImportadas.asStateFlow()

    /**
     * Importa paradas de um arquivo
     */
    fun importarParadas(paradas: List<Parada>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Validação básica
                if (paradas.isEmpty()) {
                    _errorMessage.value = "Nenhuma parada encontrada no arquivo"
                    return@launch
                }

                _paradasImportadas.value = paradas

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao importar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpa as paradas importadas
     */
    fun limparImportacao() {
        _paradasImportadas.value = emptyList()
        _errorMessage.value = null
    }

    /**
     * Limpa mensagem de erro
     */
    fun limparErro() {
        _errorMessage.value = null
    }
}