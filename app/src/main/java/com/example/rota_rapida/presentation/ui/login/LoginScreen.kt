package com.example.rota_rapida.presentation.ui.login

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rota_rapida.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val clientId = context.getString(R.string.default_web_client_id)

    // Configura o Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientId) // para enviar idToken ao backend/Supabase
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    var loading by remember { mutableStateOf(false) }

    // Launcher para o fluxo de login
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        loading = false
        val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                // TODO: Envie idToken para seu backend/Supabase e crie sessão
                onSignedIn()
            } else {
                onError("Não foi possível obter o idToken do Google.")
            }
        } catch (e: ApiException) {
            onError("Falha no login: ${e.statusCode}")
        } catch (t: Throwable) {
            onError("Erro: ${t.message}")
        }
    }

    // UI
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone/ilustração simples (opcional)
            Icon(
                painter = painterResource(id = R.drawable.ic_cloud_lock), // coloque um vetor simples ou remova
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Por que preciso fazer login?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "O Rota Rápida sincroniza automaticamente suas rotas e andamento em todos os dispositivos em que você fez login. Assim, você sempre terá um backup para nunca perder uma rota.\n\nNão vendemos seus dados a terceiros.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            GoogleSignInButton(
                enabled = !loading,
                onClick = {
                    loading = true
                    val intent: Intent = googleClient.signInIntent
                    launcher.launch(intent)
                }
            )

            Spacer(Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Ao continuar, você concorda com a Política de privacidade e os Termos de uso.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Continuar com Google")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(onSignedIn = {}, onError = {})
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoginScreenDarkPreview() {
    LoginScreen(onSignedIn = {}, onError = {})
}
