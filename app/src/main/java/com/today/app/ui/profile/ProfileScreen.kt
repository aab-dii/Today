package com.today.app.ui.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.TodayDivider
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodaySettingRow
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenStats: () -> Unit,
    onOpenMyOpinions: () -> Unit,
    onOpenFeed: () -> Unit = {},
    onOpenCircles: () -> Unit = {},
    onOpenRecap: () -> Unit = {},
    onOpenImportExport: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val msg = state.message ?: state.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Profil") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ProfileContent(
                state = state,
                onSave = viewModel::saveProfile,
                onSignOut = viewModel::signOut,
                onOpenStats = onOpenStats,
                onOpenRecap = onOpenRecap,
                onOpenImportExport = onOpenImportExport,
                onOpenMyOpinions = onOpenMyOpinions,
                onOpenFeed = onOpenFeed,
                onOpenCircles = onOpenCircles
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
private fun ProfileContent(
    state: ProfileUiState,
    onSave: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenRecap: () -> Unit,
    onOpenImportExport: () -> Unit,
    onOpenMyOpinions: () -> Unit,
    onOpenFeed: () -> Unit,
    onOpenCircles: () -> Unit
) {
    val profile = state.profile
    var editing by remember { mutableStateOf(false) }
    var name by remember(profile?.uid) { mutableStateOf(profile?.displayName.orEmpty()) }
    var bio by remember(profile?.uid) { mutableStateOf(profile?.bio.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(photoUrl = profile?.photoUrl, name = profile?.displayName)
            Spacer(modifier = Modifier.width(Spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile?.displayName ?: "Pengguna",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.session?.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.error != null) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        when {
            profile == null -> LoadingIndicator()

            editing -> {
                TodayTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama tampilan"
                )
                TodayTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    singleLine = false,
                    minLines = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    TodayPrimaryButton(
                        text = "Simpan",
                        onClick = {
                            onSave(name, bio)
                            editing = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TodayTonalButton(
                        text = "Batal",
                        onClick = {
                            name = profile.displayName
                            bio = profile.bio
                            editing = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            else -> {
                if (profile.bio.isNotBlank()) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TodayTonalButton(
                    text = "Ubah profil",
                    onClick = {
                        name = profile.displayName
                        bio = profile.bio
                        editing = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column {
                        ProfileSettingRow(
                            icon = Icons.Filled.DynamicFeed,
                            title = "Feed Komunitas",
                            subtitle = "Opini publik & akun yang kamu ikuti",
                            onClick = onOpenFeed
                        )
                        TodayDivider()
                        ProfileSettingRow(
                            icon = Icons.Filled.RateReview,
                            title = "Opini saya",
                            subtitle = "Opini yang kamu bagikan",
                            onClick = onOpenMyOpinions
                        )
                        TodayDivider()
                        ProfileSettingRow(
                            icon = Icons.Filled.Groups,
                            title = "Circle",
                            subtitle = "Klub nonton dengan teman via kode undangan",
                            onClick = onOpenCircles
                        )
                        TodayDivider()
                        ProfileSettingRow(
                            icon = Icons.Filled.BarChart,
                            title = "Statistik",
                            subtitle = "Ringkasan tontonanmu",
                            onClick = onOpenStats
                        )
                        TodayDivider()
                        ProfileSettingRow(
                            icon = Icons.Filled.AutoAwesome,
                            title = "Rekap Tahunan",
                            subtitle = "Ringkasan tontonan tahunanmu (wrapped)",
                            onClick = onOpenRecap
                        )
                        TodayDivider()
                        ProfileSettingRow(
                            icon = Icons.Filled.FileUpload,
                            title = "Impor / Ekspor",
                            subtitle = "Cadangkan atau pulihkan diary (CSV)",
                            onClick = onOpenImportExport
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column {
                        ProfileSettingRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Keluar",
                            subtitle = "Keluarkan akun dari perangkat ini",
                            onClick = onSignOut
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    TodaySettingRow(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
private fun Avatar(photoUrl: String?, name: String?) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name?.trim()?.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
