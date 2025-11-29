package com.example.rota_rapida.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RotaRapidaDrawer(
        drawerState: DrawerState,
        onNavigateToSettings: () -> Unit,
        onNavigateToCreateRoute: () -> Unit,
        content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.width(300.dp) // Largura fixa ou percentual
                ) {
                    Column(Modifier.fillMaxSize()) {
                        // 1. Cabeçalho
                        DrawerHeader(onSettingsClick = onNavigateToSettings)

                        HorizontalDivider()

                        // 2. Lista de Rotas (Histórico)
                        Box(Modifier.weight(1f)) { DrawerRouteList() }

                        HorizontalDivider()

                        // 3. Rodapé
                        DrawerFooter(onCreateRouteClick = onNavigateToCreateRoute)
                    }
                }
            },
            content = content
    )
}

@Composable
fun DrawerHeader(onSettingsClick: () -> Unit) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
                modifier =
                        Modifier.size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = "U", // Placeholder para inicial do usuário
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            // Se tiver imagem real:
            // Image(painter = ..., contentDescription = null, modifier = Modifier.fillMaxSize(),
            // contentScale = ContentScale.Crop)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info Usuário
        Column(Modifier.weight(1f)) {
            Text(
                    text = "usuario@email.com",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
            )
            Text(
                    text = "Plano Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Configurações
        IconButton(onClick = onSettingsClick) {
            Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DrawerRouteList() {
    // Mock Data
    val history =
            listOf(
                    "Início desta semana" to
                            listOf("27-11-2025 – Rota de Gabriel", "26-11-2025 – Entrega Centro"),
                    "Início deste mês" to
                            listOf("15-11-2025 – Rota Zona Sul", "10-11-2025 – Teste Importação")
            )

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        history.forEach { (sectionTitle, routes) ->
            item {
                Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(routes) { routeName -> DrawerRouteItem(routeName) }
        }
    }
}

@Composable
fun DrawerRouteItem(name: String) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable { /* TODO: Carregar rota */}
                            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            // Extraindo data fictícia do nome para exibir formatado
            val parts = name.split(" – ")
            val date = parts.getOrNull(0) ?: ""
            val title = parts.getOrNull(1) ?: name

            Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
            )
        }

        IconButton(onClick = { /* TODO: Menu da rota */}) {
            Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opções",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DrawerFooter(onCreateRouteClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Button(
                onClick = onCreateRouteClick,
                modifier = Modifier.fillMaxWidth(),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2) // Azul Circuit-like
                        ),
                shape = RoundedCornerShape(8.dp)
        ) { Text("Criar rota", color = Color.White) }
    }
}
