@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.rota_rapida.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Menu "três pontinhos" reutilizável.
 * Controla o estado internamente e expõe callbacks por ação.
 */
@Composable
fun MoreOptionsMenu(
    modifier: Modifier = Modifier,
    onShareCopyRoute: () -> Unit = {},
    onCopyStops: () -> Unit = {},
    onReoptimizeRoute: () -> Unit = {},
    onImportSpreadsheet: () -> Unit = {},
    onPrintRoute: () -> Unit = {},
    onRemoveStops: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier.size(40.dp),
        onClick = { expanded = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Mais opções"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        fun select(action: () -> Unit) {
            expanded = false
            action()
        }

        DropdownMenuItem(
            text = { Text("Compartilhar cópia da rota") },
            onClick = { select(onShareCopyRoute) }
        )
        DropdownMenuItem(
            text = { Text("Copiar paradas…") },
            onClick = { select(onCopyStops) }
        )
        DropdownMenuItem(
            text = { Text("Reotimizar rota…") },
            onClick = { select(onReoptimizeRoute) }
        )
        DropdownMenuItem(
            text = { Text("Importar planilha") },
            onClick = { select(onImportSpreadsheet) }
        )
        DropdownMenuItem(
            text = { Text("Imprimir rota") },
            onClick = { select(onPrintRoute) }
        )
        DropdownMenuItem(
            text = { Text("Remover paradas…") },
            onClick = { select(onRemoveStops) }
        )
    }
}
