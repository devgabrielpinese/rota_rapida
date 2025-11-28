package com.example.rota_rapida.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rota_rapida.domain.model.Parada
import com.example.rota_rapida.domain.model.StatusParada

// Por enquanto: NÃO esconder nada, mostrar exatamente o que vier do Excel.
// Depois, quando tivermos endereço humano separado do código, voltamos a filtrar.
fun String.toDisplayAddress(): String = this

@Composable
fun ListaParadas(
    paradas: List<Parada>,
    onToggleStatus: (Parada) -> Unit,
    onSelectParada: (Parada) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 80.dp,
            start = 16.dp,
            end = 16.dp,
            top = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(paradas) { parada ->
            ParadaItem(
                parada = parada,
                onToggleStatus = onToggleStatus,
                onSelectParada = onSelectParada
            )
        }
    }
}

@Composable
fun ParadaItem(
    parada: Parada,
    onToggleStatus: (Parada) -> Unit,
    onSelectParada: (Parada) -> Unit
) {
    // Detalhe lateral com cores vivas (nada de cinza apático)
    val statusColor = when (parada.status) {
        StatusParada.PENDENTE -> MaterialTheme.colorScheme.primary           // azul Circuit
        StatusParada.ENTREGUE -> MaterialTheme.colorScheme.secondary.copy(
            // verde suave para entregue
            red = 0.30f,
            green = 0.70f,
            blue = 0.45f
        )
        StatusParada.NAO_ENTREGUE -> MaterialTheme.colorScheme.error          // vermelho
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable { onSelectParada(parada) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra lateral de status (detalhe forte em azul / verde / vermelho)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(statusColor)
                    .clickable { onToggleStatus(parada) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Endereço (texto principal)
            Text(
                text = parada.endereco.toDisplayAddress(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}
