package com.example.rota_rapida.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ============================
// PALETA PRINCIPAL – TEMA CLARO + AZUL
// ============================

// Azul primário (botões, destaques principais)
val BluePrimary = Color(0xFF1976D2)   // #1976D2

// Azul claro (variações, ícones secundários)
val BluePrimaryLight = Color(0xFF2196F3) // #2196F3

// Azul muito claro (fundos de elementos / containers)
val BlueUltraLight = Color(0xFFE3F2FD)   // #E3F2FD

// Fundos gerais claros
val BackgroundLight = Color(0xFFFAFAFA)  // #FAFAFA
val SurfaceLight = Color(0xFFFFFFFF)     // #FFFFFF
val SurfaceVariantLight = Color(0xFFF5F5F5) // #F5F5F5

// Texto com contraste suave (sem preto absoluto)
val TextPrimary = Color(0xFF333333)
val TextSecondary = Color(0xFF555555)
val TextDisabled = Color(0xFF9E9E9E)

// Borda suave para cards / inputs
val BorderLight = Color(0xFFE0E0E0)

// ============================
// CORES DE STATUS DAS PARADAS
// (mantidas, para não quebrar nada)
// ============================

val StatusPendente = Color(0xFFFFC107)    // Amarelo
val StatusEntregue = Color(0xFF4CAF50)    // Verde
val StatusNaoEntregue = Color(0xFFF44336) // Vermelho

// ============================
// CORES DA ROTA / MARCADORES
// ============================

// Linha da rota e marcador do usuário em azul consistente
val RotaLine = BluePrimary
val UserMarker = BluePrimary

// ============================
// ALIASES PARA COMPATIBILIDADE
// (se algum código antigo usar esses nomes)
// ============================

// Azul principal antigo reapontado para o novo primário
val PrimaryBlue = BluePrimary

// Se você tiver lógica usando essas cores em algum lugar, continuam válidas:
val SecondaryGreen = StatusEntregue
val TertiaryYellow = StatusPendente
