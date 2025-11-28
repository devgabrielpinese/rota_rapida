package com.example.rota_rapida.data.repository

import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun observarRotas(): Flow<List<Rota>>
    suspend fun getRotaById(rotaId: String): Rota?
    suspend fun saveRota(rota: Rota)
    suspend fun deleteRota(rotaId: String)
    suspend fun addParada(rotaId: String, parada: Parada)
    suspend fun updateParada(rotaId: String, parada: Parada)
    suspend fun removeParada(rotaId: String, paradaId: String)

    // ✅ novo método
    suspend fun updateParadaStatus(rotaId: String, paradaId: String, novoStatus: StatusParada)

    suspend fun getRotaAtual(): Rota?

}
