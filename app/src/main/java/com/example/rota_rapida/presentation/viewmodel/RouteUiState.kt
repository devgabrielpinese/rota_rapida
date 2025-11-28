package com.example.rota_rapida.presentation.viewmodel

import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota

data class RouteUiState(
    val isLoading: Boolean = false,
    val rotaAtiva: Rota? = null,
    val paradas: List<Parada> = emptyList(),
    val erro: String? = null,
    val isPrimeiraRota: Boolean = false
)
