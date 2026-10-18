package com.today.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.ui.auth.AuthScreen
import com.today.app.ui.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TodayApp(authViewModel: AuthViewModel = koinViewModel()) {
    val state by authViewModel.state.collectAsStateWithLifecycle()

    when {
        !state.ready -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.session == null -> AuthScreen(viewModel = authViewModel)

        else -> AppNavHost()
    }
}
