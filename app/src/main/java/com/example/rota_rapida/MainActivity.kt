package com.example.rota_rapida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.rota_rapida.presentation.ui.navigation.AppNavHost
import com.example.rota_rapida.presentation.ui.navigation.NavigationRoutes
import com.example.rota_rapida.presentation.ui.theme.RotaRapidaTheme
import com.example.rota_rapida.presentation.utils.UserPrefs
import com.example.rota_rapida.presentation.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 👇 Agora TODO o app usa o tema claro + azul
            RotaRapidaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isFirstRun by UserPrefs.isFirstRunFlow(this).collectAsState(initial = null)

                    when (isFirstRun) {
                        null -> {
                            LoadingScreen()
                        }
                        else -> {
                            val navController = rememberNavController()

                            val startDest = if (isFirstRun == true) {
                                NavigationRoutes.TELA_INICIAL
                            } else {
                                NavigationRoutes.MAPA_PARADAS
                            }

                            AppNavHost(
                                navController = navController,
                                startDestination = startDest,
                                onFirstRouteFinished = {
                                    homeViewModel.onFirstRouteFinished()

                                    lifecycleScope.launch {
                                        UserPrefs.markFirstRouteCreated(this@MainActivity)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
