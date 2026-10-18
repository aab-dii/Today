package com.today.app.ui.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.core.domain.model.Title
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOpinionsScreen(
    onBack: () -> Unit,
    onTitleClick: (Title) -> Unit,
    viewModel: OpinionViewModel = koinViewModel()
) {
    val opinions by viewModel.myOpinions.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Opinion?>(null) }
    var selectedOpinionForComments by remember { mutableStateOf<Opinion?>(null) }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Opini saya", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (opinions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TodayEmptyState(
                    title = "Belum ada opini",
                    subtitle = "Buka catatan tontonanmu, lalu pilih \"Bagikan\" untuk menerbitkan opini.",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(items = opinions, key = { it.id }) { opinion ->
                    OpinionCard(
                        opinion = opinion,
                        onClick = { onTitleClick(opinion.toTitle()) },
                        onDelete = { pendingDelete = opinion },
                        onCommentClick = { selectedOpinionForComments = opinion }
                    )
                }
            }
        }
    }

    selectedOpinionForComments?.let { opinion ->
        CommentsBottomSheet(
            opinion = opinion,
            onDismiss = { selectedOpinionForComments = null }
        )
    }

    pendingDelete?.let { opinion ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = "Hapus opini?") },
            text = { Text(text = "\"${opinion.titleName}\" akan dihapus dari opini publik.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOpinion(opinion.id)
                    pendingDelete = null
                }) { Text(text = "Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(text = "Batal") }
            }
        )
    }
}
