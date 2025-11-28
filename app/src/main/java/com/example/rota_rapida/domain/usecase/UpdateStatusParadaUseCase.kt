package com.example.rota_rapida.domain.usecase

import com.example.rota_rapida.domain.model.StatusParada
// ✅ 1. CORREÇÃO: Importado do pacote 'data.repository'
import com.example.rota_rapida.data.repository.RouteRepository
import javax.inject.Inject

/**
 * Use case para atualizar status de paradas
 */
class UpdateStatusParadaUseCase @Inject constructor(
    private val repository: RouteRepository
) {

    /**
     * Atualiza o status de uma parada
     */
    // ✅ 2. CORREÇÃO: Adicionado o 'rotaId' que faltava
    suspend fun atualizarStatus(rotaId: String, paradaId: String, novoStatus: StatusParada): Boolean {
        return try {
            // ✅ 3. CORREÇÃO: Passando 'rotaId' para o repositório
            repository.updateParadaStatus(rotaId, paradaId, novoStatus)
            true
        } catch (e: Exception) {
            // O aviso 'Parameter 'e' is never used' é normal.
            // Se quiser removê-lo, troque 'e: Exception' por '_: Exception'
            false
        }
    }
}