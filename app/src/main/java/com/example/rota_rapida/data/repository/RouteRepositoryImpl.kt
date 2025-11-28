package com.example.rota_rapida.data.repository

import com.example.rota_rapida.data.source.LocalDataSource
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : RouteRepository {

    override fun observarRotas(): Flow<List<Rota>> = localDataSource.getRotasFlow()

    override suspend fun getRotaById(rotaId: String): Rota? =
        localDataSource.getRotasFlow().first().find { it.id == rotaId }

    override suspend fun getRotaAtual(): Rota? =
        localDataSource.getRotasFlow().first().firstOrNull()

    override suspend fun saveRota(rota: Rota) = localDataSource.saveRota(rota)

    override suspend fun deleteRota(rotaId: String) = localDataSource.deleteRota(rotaId)

    override suspend fun addParada(rotaId: String, parada: Parada) {
        val rota = getRotaById(rotaId) ?: return
        saveRota(rota.copy(paradas = rota.paradas + parada))
    }

    override suspend fun updateParada(rotaId: String, parada: Parada) {
        val rota = getRotaById(rotaId) ?: return
        saveRota(rota.copy(paradas = rota.paradas.map { if (it.id == parada.id) parada else it }))
    }

    override suspend fun removeParada(rotaId: String, paradaId: String) {
        val rota = getRotaById(rotaId) ?: return
        saveRota(rota.copy(paradas = rota.paradas.filterNot { it.id == paradaId }))
    }

    override suspend fun updateParadaStatus(rotaId: String, paradaId: String, novoStatus: StatusParada) {
        val rota = getRotaById(rotaId) ?: return
        val paradasAtualizadas = rota.paradas.map { p ->
            if (p.id == paradaId) p.copy(status = novoStatus) else p
        }
        saveRota(rota.copy(paradas = paradasAtualizadas))
    }
}
