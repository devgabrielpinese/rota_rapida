package com.example.rota_rapida.presentation.view.main

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tela inicial do aplicativo
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicial(
    onNavigateToCriarRota: () -> Unit,
    onNavigateToHistorico: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rota Rápida") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚚",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rota Rápida",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Otimize suas entregas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onNavigateToCriarRota,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Criar Nova Rota")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToHistorico,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Ver Histórico")
            }
        }
    }

}