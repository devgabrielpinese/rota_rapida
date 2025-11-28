package com.example.rota_rapida.domain.model

/**
 * Status da parada no domínio da aplicação.
 * - PENDENTE: estado inicial
 * - ENTREGUE: concluída com sucesso
 * - NAO_ENTREGUE: tentativa frustrada / devolução / falha
 */
enum class StatusParada {
    PENDENTE,
    ENTREGUE,
    NAO_ENTREGUE
}

/** Utilitário opcional para lógicas de UI/Regras. */
fun StatusParada.isFinalizada(): Boolean =
    this == StatusParada.ENTREGUE || this == StatusParada.NAO_ENTREGUE
