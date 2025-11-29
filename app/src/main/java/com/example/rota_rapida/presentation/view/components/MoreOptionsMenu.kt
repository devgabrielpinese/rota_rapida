package com.example.rota_rapida.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun MoreOptionsMenu(
    onCopyStops: () -> Unit,
    onReoptimizeRoute: () -> Unit,
    onImportSpreadsheet: () -> Unit,
    onRemoveStops: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Mais opções"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copiar paradas") },
                onClick = {
                    expanded = false
                    onCopyStops()
                }
            )
            DropdownMenuItem(
                text = { Text("Reotimizar rota") },
                onClick = {
                    expanded = false
                    onReoptimizeRoute()
                }
            )
            DropdownMenuItem(
                text = { Text("Importar planilha") },
                onClick = {
                    expanded = false
                    onImportSpreadsheet()
                }
            )
            DropdownMenuItem(
                text = { Text("Remover paradas") },
                onClick = {
                    expanded = false
                    onRemoveStops()
                }
            )
        }
    }
}
