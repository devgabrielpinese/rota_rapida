package com.example.rota_rapida.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "rota_rapida_prefs"
        private const val KEY_FIRST_ROUTE_DONE = "first_route_done"
    }

    enum class StartDestination {
        LOADING,
        PRIMEIRA_ROTA,
        MAPA_PARADAS
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _startDestination = MutableStateFlow(StartDestination.LOADING)
    val startDestination: StateFlow<StartDestination> = _startDestination

    init {
        decidirTelaInicial()
    }

    private fun decidirTelaInicial() {
        // SharedPreferences é síncrono, mas jogamos dentro de uma coroutine
        // pra manter o padrão de chamada assíncrona no ViewModel.
        viewModelScope.launch {
            val firstRouteDone = prefs.getBoolean(KEY_FIRST_ROUTE_DONE, false)
            _startDestination.value = if (firstRouteDone) {
                StartDestination.MAPA_PARADAS
            } else {
                StartDestination.PRIMEIRA_ROTA
            }
        }
    }

    fun onFirstRouteFinished() {
        viewModelScope.launch {
            prefs.edit()
                .putBoolean(KEY_FIRST_ROUTE_DONE, true)
                .apply()

            _startDestination.value = StartDestination.MAPA_PARADAS
        }
    }
}
