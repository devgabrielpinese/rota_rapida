// app/src/main/java/com/example/rota_rapida/ui/navigation/NavigationRoutes.kt
package com.example.rota_rapida.presentation.ui.navigation

/**
 * Objeto que contém todas as rotas de navegação do app. Centraliza as strings de rota para evitar
 * erros de digitação.
 */
object NavigationRoutes {
    const val TELA_INICIAL = "tela_inicial"
    const val CRIAR_ROTA = "criar_rota"
    const val MAPA_PARADAS = "mapa_paradas"
    const val MAPA_NAVEGACAO = "mapa_navegacao"
    const val DETALHES_PARADA = "detalhes_parada"
    const val CONFIGURACOES = "configuracoes"
    const val PERFIL = "perfil"
    const val HISTORICO = "historico"
    const val PREFERENCIAS_ROTA = "configuracoes/preferencias_rota"

    // Rotas com argumentos
    fun mapaParadasComRota(rotaId: String) = "$MAPA_PARADAS/$rotaId"
    fun mapaNavegacaoComRota(rotaId: String) = "$MAPA_NAVEGACAO/$rotaId"
    fun detalhesParadaComId(paradaId: String) = "$DETALHES_PARADA/$paradaId"
}
