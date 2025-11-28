package com.example.rota_rapida.presentation.viewmodel

/**
 * Eventos de UI usados pelo RouteSharedViewModel.
 * Pode ser expandido depois, mas por enquanto vamos
 * focar em mensagens e navegação básica.
 */
sealed class RouteUiEvent {

    // Mensagens de feedback (snackbar)
    data class ShowMessage(val message: String) : RouteUiEvent()
    data class ShowError(val message: String) : RouteUiEvent()

    // Eventos de navegação / ações (mantidos para compatibilidade)
    object NavigateToMapaParadas : RouteUiEvent()
    object NavigateToPrimeiraRota : RouteUiEvent()
    object OpenFilePicker : RouteUiEvent()

    // Novos eventos para o menu de opções
    data class ShareFile(val uri: android.net.Uri) : RouteUiEvent()
    data class PrintFile(val uri: android.net.Uri) : RouteUiEvent()
    data class ShowCopyDialog(val routes: List<com.example.rota_rapida.domain.model.Rota>) : RouteUiEvent()
    data class ShowRemoveDialog(val paradas: List<com.example.rota_rapida.domain.model.Parada>) : RouteUiEvent()
}
