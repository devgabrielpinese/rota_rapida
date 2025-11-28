package com.example.rota_rapida.data.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {
    fun currentUserId(): String = "debug-user"
}
