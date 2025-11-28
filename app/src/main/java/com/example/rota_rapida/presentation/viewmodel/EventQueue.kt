package com.example.rota_rapida.presentation.viewmodel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class EventQueue<T> {
    private val channel = Channel<T>(Channel.BUFFERED)
    val flow: Flow<T> = channel.receiveAsFlow()

    fun send(event: T) {
        channel.trySend(event)
    }
}
