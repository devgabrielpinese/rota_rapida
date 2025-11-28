package com.example.rota_rapida.presentation.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rota_rapida.data.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de navegação.
 *
 * @SuppressLint("MissingPermission") é usado aqui porque a permissão
 * de localização é tratada (verificada e solicitada) pela MainActivity
 * antes que este ViewModel seja usado.
 */
@SuppressLint("MissingPermission") // ✅ CORREÇÃO: A anotação vai aqui
@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _distanciaProxParada = MutableStateFlow<Float?>(null)
    val distanciaProxParada: StateFlow<Float?> = _distanciaProxParada

    init {
        // Bloco 'init' não precisa mais da anotação
        viewModelScope.launch {
            // O Hilt garante que locationProvider não é nulo
            locationProvider.locationUpdates().collectLatest { location ->
                // TODO: Substituir este placeholder pelo cálculo real
                // usando o 'location' recebido e a próxima parada.
                _distanciaProxParada.value = 1234f
            }
        }
    }
}