package com.example.rota_rapida.data.mapper

import com.example.rota_rapida.data.dto.ParadaData
import com.example.rota_rapida.data.dto.RotaData
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada

// String -> enum seguro
private fun String.toStatus(): StatusParada = when (uppercase()) {
    "ENTREGUE" -> StatusParada.ENTREGUE
    "NAO_ENTREGUE", "NÃO_ENTREGUE" -> StatusParada.NAO_ENTREGUE
    else -> StatusParada.PENDENTE
}

private fun StatusParada.toStringValue(): String = when (this) {
    StatusParada.ENTREGUE -> "ENTREGUE"
    StatusParada.NAO_ENTREGUE -> "NAO_ENTREGUE"
    StatusParada.PENDENTE -> "PENDENTE"
}

fun ParadaData.toParada(): Parada = Parada(
    id = id,
    endereco = endereco,
    latitude = latitude,
    longitude = longitude,
    observacao = observacao,
    status = status.toStatus()
)

fun Parada.toParadaData(): ParadaData = ParadaData(
    id = id,
    endereco = endereco,
    latitude = latitude,
    longitude = longitude,
    observacao = observacao,
    status = status.toStringValue()
)

fun RotaData.toRota(): Rota = Rota(
    id = id,
    nome = nome,
    paradas = paradas.map { it.toParada() },
    criadaEmMillis = criadaEmMillis
)

fun Rota.toRotaData(): RotaData = RotaData(
    id = id,
    nome = nome,
    paradas = paradas.map { it.toParadaData() },
    criadaEmMillis = criadaEmMillis
)
