package com.example.rota_rapida.presentation.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(onNavigateToPreferences: () -> Unit, onBack: () -> Unit) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Configurações") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                            }
                        }
                )
            }
    ) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
        ) {
            // Seção: Preferências de rota (Destaque)
            SettingsItem(
                    title = "Preferências de rota",
                    subtitle = "App de navegação, lado da parada, etc.",
                    onClick = onNavigateToPreferences
            )

            Divider()

            // Seção: Preferências gerais
            SettingsSectionTitle("Preferências gerais")
            SettingsItem(
                    title = "Unidade de distância",
                    value = "Quilômetros",
                    onClick = { /* TODO */}
            )
            SettingsItem(title = "Tema", value = "Claro", onClick = { /* TODO */})

            Divider()

            // Seção: Assinatura
            SettingsSectionTitle("Assinatura")
            SettingsItem(title = "Cancelar assinatura", onClick = { /* TODO */})
            SettingsItem(title = "Comparar planos", onClick = { /* TODO */})

            Divider()

            // Seção: Informações legais
            SettingsSectionTitle("Informações legais")
            SettingsItem(title = "Licenças", onClick = { /* TODO */})
            SettingsItem(title = "Termos de uso", onClick = { /* TODO */})
            SettingsItem(title = "Política de privacidade", onClick = { /* TODO */})

            Divider()

            // Seção: Versão
            Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "Versão: RotaRapida-v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botão Sair
            TextButton(
                    onClick = { /* TODO: Logout */},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors =
                            ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                            )
            ) { Text("Sair") }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
        title: String,
        subtitle: String? = null,
        value: String? = null,
        onClick: () -> Unit
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable(onClick = onClick)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (value != null) {
            Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
            )
        }

        Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
