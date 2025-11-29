package com.example.rota_rapida.presentation.view.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.Rota
import com.example.rota_rapida.domain.model.StatusParada

@Composable
fun CopyStopsDialog(
    routes: List<Rota>,
    onDismiss: () -> Unit,
    onConfirm: (Rota) -> Unit
) {
    var selectedRouteId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copiar paradas para rota") },
        text = {
            if (routes.isEmpty()) {
                Text("Nenhuma rota disponível.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    itemsIndexed(routes) { index, rota ->
                        val label = rota.nome.ifBlank { "Rota ${index + 1}" }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .toggleable(
                                    value = rota.id == selectedRouteId,
                                    onValueChange = { selectedRouteId = rota.id }
                                )
                        ) {
                            RadioButton(
                                selected = rota.id == selectedRouteId,
                                onClick = { selectedRouteId = rota.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rota = routes.firstOrNull { it.id == selectedRouteId }
                    if (rota != null) {
                        onConfirm(rota)
                    }
                },
                enabled = routes.isNotEmpty() && selectedRouteId != null
            ) {
                Text("Copiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RemoveStopsDialog(
    paradas: List<Parada>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var removeCompleted by remember { mutableStateOf(true) }
    val selectedIds = remember { mutableStateMapOf<String, Boolean>() }

    // garante que o mapa acompanha a lista de paradas
    LaunchedEffect(paradas) {
        selectedIds.clear()
        paradas.forEach { p ->
            selectedIds[p.id] = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remover paradas") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // checkbox: remover todas concluídas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .toggleable(
                            value = removeCompleted,
                            onValueChange = { removeCompleted = it }
                        )
                ) {
                    Checkbox(
                        checked = removeCompleted,
                        onCheckedChange = { removeCompleted = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remover todas as paradas concluídas")
                }

                // lista para seleção manual (se NÃO estiver removendo todas concluídas)
                if (!removeCompleted) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(paradas) { parada ->
                            val checked = selectedIds[parada.id] ?: false

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .toggleable(
                                        value = checked,
                                        onValueChange = {
                                            selectedIds[parada.id] = it
                                        }
                                    )
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        selectedIds[parada.id] = it
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(parada.endereco)
                                    Text(
                                        text = "Status: ${
                                            if (parada.status == StatusParada.ENTREGUE)
                                                "Entregue"
                                            else
                                                "Pendente"
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val idsToRemove: List<String> =
                        if (removeCompleted) {
                            paradas
                                .filter { it.status == StatusParada.ENTREGUE }
                                .map { it.id }
                        } else {
                            selectedIds.filter { it.value }.keys.toList()
                        }

                    onConfirm(idsToRemove)
                }
            ) {
                Text("Remover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
