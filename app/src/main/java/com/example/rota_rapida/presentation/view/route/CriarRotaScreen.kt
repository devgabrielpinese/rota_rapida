package com.example.rota_rapida.presentation.view.route

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rota_rapida.presentation.viewmodel.RouteSharedViewModel
import com.example.rota_rapida.presentation.viewmodel.RouteUiEvent
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CriarRotaScreen(
    viewModel: RouteSharedViewModel = hiltViewModel(),
    // callback oficial (com dados)
    onConfirmar: (nomeRota: String, dataSelecionada: String) -> Unit = { _, _ -> },
    // compat: quem chama com 2 args
    onNavigateToMapaParadas: ((nomeRota: String, dataSelecionada: String) -> Unit)? = null,
    // compat: quem chama SEM args (ex.: { navController.navigate("MapaParadas") })
    onNavigateToMapaParadasNoArgs: (() -> Unit)? = null,
    // Callback para quando a rota for criada com sucesso (pode vir via evento também)
    onRotaCriada: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    // Consome eventos
    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is RouteUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                // Se houver evento de navegação específico, tratar aqui
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            CriarRotaContent(
                onConfirmar = { nome, data ->
                    // Chama a lógica do ViewModel
                    viewModel.iniciarNovaRota() // Exemplo: inicia nova rota. Poderia passar nome/data se o VM suportasse.
                    
                    // Mantém callbacks de navegação legados ou chama onRotaCriada
                    if (onNavigateToMapaParadas != null) {
                        onNavigateToMapaParadas(nome, data)
                    } else if (onNavigateToMapaParadasNoArgs != null) {
                        onNavigateToMapaParadasNoArgs()
                    } else {
                        onConfirmar(nome, data)
                        onRotaCriada()
                    }
                }
            )
        }
    }
}

@Composable
private fun CriarRotaContent(
    onConfirmar: (nomeRota: String, dataSelecionada: String) -> Unit
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()

    val df = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    val wf = SimpleDateFormat("EEEE", Locale("pt", "BR"))

    val dataHoje = df.format(cal.time)
    val diaSemanaHoje = wf.format(cal.time).replaceFirstChar { it.uppercase() }

    var nomeRota by remember { mutableStateOf(TextFieldValue("$diaSemanaHoje $dataHoje")) }
    var dataRotaHoje by remember { mutableStateOf(dataHoje) }
    var outraData by remember { mutableStateOf("") }

    val datePicker = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            outraData = String.format(Locale("pt", "BR"), "%02d/%02d/%04d", day, month + 1, year)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Criar Nova Rota",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nomeRota,
            onValueChange = { nomeRota = it },
            label = { Text("Nome da Rota (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dataRotaHoje,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            label = { Text("Data da Rota") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = if (outraData.isEmpty()) "Escolher outra data" else outraData,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            label = { Text("Escolher outra data") },
            trailingIcon = {
                TextButton(onClick = { datePicker.show() }) { Text("Selecionar") }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val dataFinal = if (outraData.isNotEmpty()) outraData else dataRotaHoje
                onConfirmar(nomeRota.text, dataFinal)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Confirmar") }
    }
}
