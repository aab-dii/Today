package com.today.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onBack: () -> Unit,
    viewModel: ImportExportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val csv = viewModel.exportCsv()
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    ?: error("Tidak bisa menulis berkas")
            }.onSuccess {
                viewModel.onExported(state.entryCount)
            }.onFailure {
                viewModel.onError(it.localizedMessage ?: "Gagal mengekspor")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                    ?: error("Tidak bisa membaca berkas")
            }.onSuccess {
                viewModel.importCsv(it)
            }.onFailure {
                viewModel.onError(it.localizedMessage ?: "Gagal membaca berkas")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Impor / Ekspor", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(
                text = "Cadangkan diary-mu sebagai berkas CSV, atau pulihkan dari cadangan / impor dari Letterboxd.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
                    Text(
                        text = "Diary saat ini",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "${state.entryCount} catatan",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            TodayPrimaryButton(
                text = "Ekspor ke CSV",
                onClick = { exportLauncher.launch("today-diary-${dateStamp()}.csv") },
                enabled = !state.busy && state.entryCount > 0,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            TodayTonalButton(
                text = "Impor dari CSV",
                onClick = {
                    importLauncher.launch(
                        arrayOf(
                            "text/csv",
                            "text/comma-separated-values",
                            "text/plain",
                            "application/vnd.ms-excel"
                        )
                    )
                },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Text(
                text = "Format yang didukung: CSV hasil ekspor Today (kolom titleId, mediaType, titleName, " +
                    "watchedDate, rating, mood, ...) dan CSV diary Letterboxd (Date, Name, Year, Rating, " +
                    "Watched Date). Catatan dari Letterboxd tidak terhubung ke katalog TMDB.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun dateStamp(): String =
    SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
