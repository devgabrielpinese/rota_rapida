package com.example.rota_rapida.domain.model

data class Rota(
    val id: String,
    val nome: String,
    val paradas: List<Parada> = emptyList(),
    val criadaEmMillis: Long = System.currentTimeMillis()
)