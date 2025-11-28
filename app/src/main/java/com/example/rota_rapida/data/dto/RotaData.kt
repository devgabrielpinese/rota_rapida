package com.example.rota_rapida.data.dto

data class RotaData(
    val id: String = "",
    val nome: String = "",
    val paradas: List<ParadaData> = emptyList(),
    val criadaEmMillis: Long = System.currentTimeMillis()
)
