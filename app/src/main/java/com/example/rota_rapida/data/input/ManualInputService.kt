// app/src/main/java/com/example/rota_rapida/data/input/ManualInputService.kt
package com.example.rota_rapida.data.input

import android.content.Context
import android.location.Geocoder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Serviço responsável por transformar texto digitado/planilha em dados de parada
 * (endereço normalizado + latitude/longitude quando possível).
 *
 * ATENÇÃO:
 * - Este serviço NÃO grava nada em banco — apenas resolve e devolve um resultado.
 * - O ViewModel decide como persistir.
 */
@Singleton
class ManualInputService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class ParsedParada(
        val endereco: String,
        val latitude: Double?,
        val longitude: Double?
    )

    /**
     * API principal usada pelo ViewModel.
     * Tenta geocodificar; se não achar, retorna apenas o endereço normalizado com lat/lng nulos.
     */
    suspend fun addParadaByText(texto: String): Result<ParsedParada> = runCatching {
        val enderecoBruto = texto.trim()
        val enderecoNormalizado = normalizarEndereco(enderecoBruto)

        // 1ª tentativa: endereço como veio
        geocode(enderecoNormalizado)?.let { (lat, lng) ->
            return@runCatching ParsedParada(
                endereco = enderecoNormalizado,
                latitude = lat,
                longitude = lng
            )
        }

        // 2ª tentativa: força contexto Brasil/SP se não houver cidade/UF
        val enriquecido = enriquecerEndereco(enderecoNormalizado)
        if (enriquecido != enderecoNormalizado) {
            geocode(enriquecido)?.let { (lat, lng) ->
                return@runCatching ParsedParada(
                    endereco = enderecoNormalizado, // guardo endereço “limpo” no app
                    latitude = lat,
                    longitude = lng
                )
            }
        }

        // 3ª tentativa: heurística simples (trocas comuns)
        val heuristico = heuristicaCorrecoes(enderecoNormalizado)
        if (heuristico != enderecoNormalizado) {
            geocode(heuristico)?.let { (lat, lng) ->
                return@runCatching ParsedParada(
                    endereco = enderecoNormalizado,
                    latitude = lat,
                    longitude = lng
                )
            }
        }

        // Falhou geocodificar — devolve sem coordenadas
        ParsedParada(
            endereco = enderecoNormalizado,
            latitude = null,
            longitude = null
        )
    }

    // -----------------------------------------------------------------------
    // Implementação de geocodificação
    // -----------------------------------------------------------------------

    private suspend fun geocode(endereco: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("pt", "BR"))
            // Em alguns devices, a implementação usa serviço remoto e pode demorar.
            val results = geocoder.getFromLocationName(endereco, 1)
            val first = results?.firstOrNull()
            val lat = first?.latitude
            val lng = first?.longitude
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                Pair(lat, lng)
            } else null
        } catch (e: Exception) {
            Log.e("RotaRapida", "Geocode falhou para: $endereco | ${e.message}", e)
            null
        }
    }

    // -----------------------------------------------------------------------
    // Saneamento e heurísticas leves
    // -----------------------------------------------------------------------

    private fun normalizarEndereco(e: String): String {
        // remove espaços duplicados, vírgulas sobrando, corrige acentos comuns (mantém pt-BR)
        return e
            .replace(Regex("\\s+"), " ")
            .replace(Regex(",\\s*,+"), ", ")
            .trim()
    }

    /** Caso o endereço não tenha cidade/UF/país, adiciona um contexto padrão. */
    private fun enriquecerEndereco(e: String): String {
        val low = e.lowercase(Locale.getDefault())
        val temCidade = listOf("são paulo", "sao paulo", "sp", "rio de janeiro", "rj", "belo horizonte", "bh")
            .any { low.contains(it) }
        val temPais = low.contains("brasil") || low.contains("brazil")

        return when {
            !temCidade && !temPais -> "$e, São Paulo, SP, Brasil"
            !temPais -> "$e, Brasil"
            else -> e
        }
    }

    /** Ajustes rápidos de abreviações/erros comuns. */
    private fun heuristicaCorrecoes(e: String): String {
        return e
            .replace("Av.", "Avenida", ignoreCase = true)
            .replace("R.", "Rua", ignoreCase = true)
            .replace("SP,", "SP, Brasil,", ignoreCase = true)
            .replace("São paulo", "São Paulo", ignoreCase = true)
    }
}
