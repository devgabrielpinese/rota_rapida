package com.example.rota_rapida.data.dto

data class ParadaData(
    val id: String = "",
    val endereco: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val observacao: String? = null,
    val status: String = "PENDENTE" // manter string se já usado no storage
)
