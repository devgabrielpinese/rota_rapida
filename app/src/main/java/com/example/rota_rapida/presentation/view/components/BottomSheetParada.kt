package com.example.rota_rapida.presentation.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rota_rapida.domain.model.StatusParada

/**
 * Bottom sheet para alterar o status de uma Parada.
 *
 * @param statusAtual Status atual da parada (para destacar/usar lógica de UI)
 * @param onMudarStatus callback com o novo status escolhido
 */
@Composable
fun BottomSheetParada(
    statusAtual: StatusParada,
    onMudarStatus: (StatusParada) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Status da parada",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        // PENDENTE
        Button(
            onClick = { onMudarStatus(StatusParada.PENDENTE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.HourglassEmpty, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Marcar como Pendente")
        }

        Spacer(Modifier.height(8.dp))

        // ENTREGUE
        Button(
            onClick = { onMudarStatus(StatusParada.ENTREGUE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Marcar como Entregue")
        }

        Spacer(Modifier.height(8.dp))

        // NÃO ENTREGUE
        Button(
            onClick = { onMudarStatus(StatusParada.NAO_ENTREGUE) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Clear, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Marcar como Não Entregue")
        }
    }
}
