package com.example.rota_rapida.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// ✅ CORRIGIDO: Package correto do ImportacaoViewModel
import com.example.rota_rapida.presentation.viewmodel.ImportacaoViewModel

/**
 * Tela de importação de paradas
 * Suporta importação via:
 * - Entrada manual de texto
 * - Arquivo Excel (.xlsx, .xls)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportacaoScreen(
    viewModel: ImportacaoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var textoManual by remember { mutableStateOf("") }
    var showExcelPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Paradas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção de texto manual
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Entrada Manual",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Digite os endereços, um por linha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = textoManual,
                        onValueChange = { textoManual = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = {
                            Text("Rua Exemplo, 123\nAv. Principal, 456\n...")
                        },
                        maxLines = 10
                    )

                    Button(
                        onClick = {
                            // TODO: Processar texto manual
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Endereços")
                    }
                }
            }

            // Seção de importação de Excel
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Importar de Excel",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Selecione um arquivo .xlsx ou .xls com endereços",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { showExcelPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Selecionar Arquivo Excel")
                    }
                }
            }
        }
    }
}