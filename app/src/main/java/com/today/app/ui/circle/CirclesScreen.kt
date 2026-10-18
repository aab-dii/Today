package com.today.app.ui.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.social.domain.model.Circle
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CirclesScreen(
    onBack: () -> Unit,
    onOpenCircle: (String) -> Unit,
    viewModel: CirclesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.openCircleId) {
        state.openCircleId?.let { id ->
            onOpenCircle(id)
            viewModel.consumeOpenCircle()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TodayTopBar(
                title = "Circle",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showJoin = true }) {
                        Icon(
                            imageVector = Icons.Filled.GroupAdd,
                            contentDescription = "Gabung circle"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "Buat circle") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))

                state.circles.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TodayEmptyState(
                        title = "Belum ikut circle",
                        subtitle = state.error
                            ?: "Buat circle baru atau gabung dengan kode undangan temanmu.",
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(items = state.circles, key = { it.id }) { circle ->
                        CircleCard(circle = circle, onClick = { onOpenCircle(circle.id) })
                    }
                }
            }
        }
    }

    if (showCreate) {
        CircleNameDialog(
            title = "Buat circle",
            label = "Nama circle",
            confirmText = "Buat",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                viewModel.createCircle(name)
                showCreate = false
            }
        )
    }

    if (showJoin) {
        CircleNameDialog(
            title = "Gabung circle",
            label = "Kode undangan",
            confirmText = "Gabung",
            onDismiss = { showJoin = false },
            onConfirm = { code ->
                viewModel.joinByCode(code)
                showJoin = false
            }
        )
    }
}

@Composable
private fun CircleCard(circle: Circle, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = circle.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Oleh ${circle.ownerName} · ${circle.memberCount} anggota",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircleNameDialog(
    title: String,
    label: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            TodayTextField(
                value = value,
                onValueChange = { value = it },
                label = label
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) { Text(text = confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Batal") }
        }
    )
}
