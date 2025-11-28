// app/src/main/java/com/example/rota_rapida/data/remote/RotaRapidaApi.kt
package com.example.rota_rapida.data.remote

import com.example.rota_rapida.data.dto.ParadaData
import com.example.rota_rapida.data.dto.RotaData
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface Retrofit para comunicação com o backend do Rota Rápida
 *
 * Base URL deve ser configurada no AppModule
 * Exemplo: https://api.rotarapida.com/v1/
 */
interface RotaRapidaApi {

    // ===== ENDPOINTS DE AUTENTICAÇÃO =====

    @POST("auth/login")
    suspend fun login(
        @Body credentials: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body userData: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/verify-payment")
    suspend fun verifyPayment(
        @Header("Authorization") token: String
    ): Response<PaymentStatusResponse>

    // ===== ENDPOINTS DE ROTAS =====

    @GET("routes")
    suspend fun getAllRotas(
        @Header("Authorization") token: String
    ): Response<List<RotaData>>

    @GET("routes/{id}")
    suspend fun getRotaById(
        @Header("Authorization") token: String,
        @Path("id") rotaId: String
    ): Response<RotaData>

    @POST("routes")
    suspend fun createRota(
        @Header("Authorization") token: String,
        @Body rota: CreateRotaRequest
    ): Response<RotaData>

    @PUT("routes/{id}")
    suspend fun updateRota(
        @Header("Authorization") token: String,
        @Path("id") rotaId: String,
        @Body rota: UpdateRotaRequest
    ): Response<RotaData>

    @DELETE("routes/{id}")
    suspend fun deleteRota(
        @Header("Authorization") token: String,
        @Path("id") rotaId: String
    ): Response<Unit>

    // ===== ENDPOINTS DE PARADAS =====

    @POST("routes/{rotaId}/stops")
    suspend fun addParada(
        @Header("Authorization") token: String,
        @Path("rotaId") rotaId: String,
        @Body parada: ParadaData
    ): Response<ParadaData>

    @PUT("stops/{id}/status")
    suspend fun updateParadaStatus(
        @Header("Authorization") token: String,
        @Path("id") paradaId: String,
        @Body status: UpdateStatusRequest
    ): Response<ParadaData>

    @DELETE("stops/{id}")
    suspend fun deleteParada(
        @Header("Authorization") token: String,
        @Path("id") paradaId: String
    ): Response<Unit>

    // ===== ENDPOINTS DE OTIMIZAÇÃO =====

    @POST("routes/{id}/optimize")
    suspend fun optimizeRoute(
        @Header("Authorization") token: String,
        @Path("id") rotaId: String,
        @Body params: OptimizeRouteRequest
    ): Response<OptimizeRouteResponse>
}

// ===== REQUEST/RESPONSE MODELS =====

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: String,
    val email: String,
    val ativo: Boolean,
    val dataVencimento: Long
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val nome: String,
    val telefone: String?
)

data class RegisterResponse(
    val userId: String,
    val message: String
)

data class PaymentStatusResponse(
    val ativo: Boolean,
    val dataVencimento: Long,
    val plano: String,
    val message: String?
)

data class CreateRotaRequest(
    val nome: String,
    val data: Long,
    val paradas: List<ParadaData>
)

data class UpdateRotaRequest(
    val nome: String?,
    val paradas: List<ParadaData>?
)

data class UpdateStatusRequest(
    val status: String, // "ENTREGUE", "NAO_ENTREGUE", "PENDENTE"
    val motivo: String?,
    val timestamp: Long
)

data class OptimizeRouteRequest(
    val startLat: Double,
    val startLng: Double,
    val algoritmo: String = "nearest_neighbor" // ou "genetic", "ant_colony", etc
)

data class OptimizeRouteResponse(
    val paradas: List<ParadaData>,
    val distanciaTotal: Double,
    val tempoEstimado: Long
)