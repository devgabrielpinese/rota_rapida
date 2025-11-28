package com.example.rota_rapida

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Classe principal da aplicação.
 * É aqui que o Hilt inicializa o container de dependências global.
 * Referenciada no AndroidManifest.xml via android:name=".RotaRapidaApplication".
 */
@HiltAndroidApp
class RotaRapidaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("RotaRápida", "✅ RotaRapidaApplication inicializada com sucesso (Hilt ativo).")
    }
}
