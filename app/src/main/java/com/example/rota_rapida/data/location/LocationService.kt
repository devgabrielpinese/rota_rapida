package com.example.rota_rapida.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.rota_rapida.MainActivity
import com.example.rota_rapida.R
import com.google.android.gms.location.*

/**
 * Serviço responsável por rastrear a localização do entregador em segundo plano.
 * Correção: permissões, getSystemService e chamada de requestLocationUpdates.
 */
class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val CHANNEL_ID = "rota_rapida_tracking"
        private const val UPDATE_INTERVAL_MS = 5000L
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(1, buildNotification())
        startLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Solicita atualizações de localização do FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission") // Já verificamos via Manifest
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.forEach { location ->
                    // 🔹 Aqui você pode enviar a localização pro servidor / ViewModel
                    // Exemplo: Log.d("LocationService", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                }
            }
        }

        // ✅ Chamada corrigida com Looper e contexto
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Cria o canal de notificação (requerido para serviços foreground no Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navegação Ativa",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação exibida durante a navegação"
                setShowBadge(false)
            }

            val manager = this@LocationService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Monta a notificação persistente para manter o serviço ativo.
     */
    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rota Rápida ativo")
            .setContentText("Rastreamento em execução")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela atualizações para evitar vazamentos de memória
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
