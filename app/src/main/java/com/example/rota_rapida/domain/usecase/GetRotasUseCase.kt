package com.example.rota_rapida.domain.usecase

import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada
import com.example.rota_rapida.data.repository.RouteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case para obter rotas e paradas (baseado no contrato atual do RouteRepository).
 *
 * Observação:
 * O repositório não expõe "getAllParadas" global.
 * Em vez disso, buscamos por rota (rotaId) e filtramos as paradas localmente.
 */
class GetRotasUseCase @Inject constructor(
    private val repository: RouteRepository
) {
    /** Fluxo com todas as rotas (domínio) */
    fun observarRotas(): Flow<List<Rota>> = repository.observarRotas()

    /** Obtém uma rota específica */
    suspend fun obterRota(rotaId: String): Rota? = repository.getRotaById(rotaId)

    /** Obtém as paradas de uma rota */
    suspend fun obterParadasDaRota(rotaId: String): List<Parada> {
        return repository.getRotaById(rotaId)?.paradas ?: emptyList()
    }

    /** Obtém uma parada específica por ID dentro de uma rota */
    suspend fun obterParadaPorId(rotaId: String, paradaId: String): Parada? {
        return repository.getRotaById(rotaId)?.paradas?.find { it.id == paradaId }
    }

    /** Obtém paradas por status dentro de uma rota */
    suspend fun obterParadasPorStatus(rotaId: String, status: StatusParada): List<Parada> {
        val rota = repository.getRotaById(rotaId) ?: return emptyList()
        return rota.paradas.filter { it.status == status }
    }

    /** (Opcional) Snapshot imediato de todas as rotas */
    suspend fun listarRotasSnapshot(): List<Rota> = repository.observarRotas().first()
}
