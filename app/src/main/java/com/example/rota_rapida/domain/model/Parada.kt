package com.example.rota_rapida.domain.model

data class Parada(
    val id: String,
    val endereco: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val observacao: String? = null,
    val status: StatusParada = StatusParada.PENDENTE
)