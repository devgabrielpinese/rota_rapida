package com.example.rota_rapida.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rota_rapida.presentation.view.main.PrimeiraRotaScreen
import com.example.rota_rapida.presentation.view.route.CriarRotaScreen
import com.example.rota_rapida.presentation.view.route.MapaParadasScreen
import com.example.rota_rapida.presentation.viewmodel.RouteSharedViewModel

@Composable
fun AppNavHost(
        navController: NavHostController,
        startDestination: String,
        onFirstRouteFinished: () -> Unit
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = startDestination) {

        // 🔵 Tela inicial (primeira vez)
        composable(NavigationRoutes.TELA_INICIAL) {
            PrimeiraRotaScreen(
                    onCriarPrimeiraRota = {
                        navController.navigate(NavigationRoutes.CRIAR_ROTA) {
                            popUpTo(NavigationRoutes.TELA_INICIAL) { inclusive = true }
                        }
                    }
            )
        }

        // 🟢 Criar rota
        composable(NavigationRoutes.CRIAR_ROTA) {
            val viewModel: RouteSharedViewModel = hiltViewModel()

            CriarRotaScreen(
                    viewModel = viewModel,
                    onRotaCriada = {
                        onFirstRouteFinished()
                        navController.navigate(NavigationRoutes.MAPA_PARADAS) {
                            popUpTo(NavigationRoutes.CRIAR_ROTA) { inclusive = true }
                        }
                    }
            )
        }

        // 🔴 Mapa de paradas
        composable(NavigationRoutes.MAPA_PARADAS) {
            val viewModel: RouteSharedViewModel = hiltViewModel()

            MapaParadasScreen(
                    viewModel = viewModel,
                    navController = navController // Passando navController para o Drawer funcionar
            )
        }

        // ⚙️ Configurações
        composable(NavigationRoutes.CONFIGURACOES) {
            com.example.rota_rapida.presentation.view.settings.ConfiguracoesScreen(
                    onNavigateToPreferences = {
                        navController.navigate(NavigationRoutes.PREFERENCIAS_ROTA)
                    },
                    onBack = { navController.popBackStack() }
            )
        }

        // 🛣️ Preferências de Rota
        composable(NavigationRoutes.PREFERENCIAS_ROTA) {
            com.example.rota_rapida.presentation.view.settings.PreferenciasRotaScreen(
                    onBack = { navController.popBackStack() }
            )
        }
    }
}
