
package com.example.rota_rapida.presentation.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenciasRotaScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferências de rota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // App de navegação
            PreferenceItem(
                title = "App de navegação",
                value = "Waze",
                onClick = { /* TODO: Dialog de escolha */ }
            )

            // Lado da parada
            PreferenceItem(
                title = "Lado da parada",
                value = "Qualquer lado do veículo",
                onClick = { /* TODO: Dialog de escolha */ }
            )

            // Tempo médio na parada
            PreferenceItem(
                title = "Tempo médio na parada",
                value = "1 min",
                onClick = { /* TODO: Dialog de escolha */ }
            )

            // Tipo de veículo
            PreferenceItem(
                title = "Tipo de veículo",
                value = "Moto",
                onClick = { /* TODO: Dialog de escolha */ }
            )

            Divider()

            // Evitar pedágios
            SwitchPreferenceItem(
                title = "Evitar pedágios",
                checked = false,
                onCheckedChange = { /* TODO */ }
            )

            // Balão do modo de navegação
            SwitchPreferenceItem(
                title = "Balão do modo de navegação",
                subtitle = "Veja informações de entrega enquanto navega",
                checked = true,
                onCheckedChange = { /* TODO */ }
            )
        }
    }
}

@Composable
fun PreferenceItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SwitchPreferenceItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
