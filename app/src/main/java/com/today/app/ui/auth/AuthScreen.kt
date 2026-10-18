@file:Suppress("DEPRECATION")

package com.today.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.today.app.ui.components.TodayChoiceChip
import com.today.app.ui.components.TodayDivider
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AuthForm(
                error = state.error,
                onSignIn = viewModel::signInWithEmail,
                onSignUp = viewModel::signUpWithEmail,
                onGoogle = viewModel::signInWithGoogle,
                onReset = viewModel::sendPasswordReset,
                onGoogleError = viewModel::reportError
            )

            if (state.busy) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AuthForm(
    error: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onGoogle: (String) -> Unit,
    onReset: (String) -> Unit,
    onGoogleError: (String) -> Unit
) {
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showReset by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val webClientId = remember(context) {
        val resId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        if (resId != 0) context.getString(resId) else null
    }
    val googleClient = remember(context, webClientId) {
        webClientId?.let { clientId ->
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
            )
        }
    }
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val token = account.idToken
            if (token != null) {
                onGoogle(token)
            } else {
                onGoogleError("Google tidak mengembalikan token.")
            }
        } catch (e: ApiException) {
            onGoogleError("Google Sign-In gagal (kode ${e.statusCode}).")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Spacer(modifier = Modifier.height(Spacing.xxl))

        Text(
            text = "Today",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Catat apa yang kamu tonton hari ini. Film, serial, dan anime — dengan mood dan ceritamu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TodayChoiceChip(
                text = "Masuk",
                selected = !register,
                onClick = { register = false }
            )
            TodayChoiceChip(
                text = "Daftar",
                selected = register,
                onClick = { register = true }
            )
        }

        if (register) {
            TodayTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Nama tampilan"
            )
        }
        TodayTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null
                )
            }
        )
        TodayTextField(
            value = password,
            onValueChange = { password = it },
            label = "Kata sandi",
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        TodayPrimaryButton(
            text = if (register) "Daftar" else "Masuk",
            onClick = {
                if (register) onSignUp(email, password, displayName) else onSignIn(email, password)
            },
            enabled = email.isNotBlank() && password.length >= 6,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!register) {
            TextButton(
                onClick = { showReset = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Lupa kata sandi?", style = MaterialTheme.typography.labelLarge)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TodayDivider(modifier = Modifier.weight(1f))
            Text(
                text = "atau",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md)
            )
            TodayDivider(modifier = Modifier.weight(1f))
        }

        TodayTonalButton(
            text = "Masuk dengan Google",
            onClick = { googleClient?.let { googleLauncher.launch(it.signInIntent) } },
            enabled = googleClient != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (googleClient == null) {
            Text(
                text = "Google Sign-In belum aktif. Lengkapi SHA-1 di Firebase, aktifkan provider Google, " +
                    "lalu perbarui google-services.json.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showReset) {
        var resetEmail by remember { mutableStateOf(email) }
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset kata sandi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Kami akan mengirim tautan reset ke emailmu.")
                    TodayTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = "Email"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset(resetEmail)
                        showReset = false
                    }
                ) { Text("Kirim") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Batal") }
            }
        )
    }
}
