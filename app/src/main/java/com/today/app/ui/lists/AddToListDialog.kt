package com.today.app.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import com.today.app.core.domain.model.CustomListItem
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddToListDialog(
    item: CustomListItem,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddToListViewModel = koinViewModel()
) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(item.titleId, item.mediaType) {
        viewModel.load(item.titleId, item.mediaType)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Tambah ke daftar") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (lists.isEmpty()) {
                    Text(
                        text = "Belum ada daftar. Buat daftar baru di bawah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    lists.forEach { list ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggle(list.id) }
                                .padding(vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected.contains(list.id),
                                onCheckedChange = { viewModel.toggle(list.id) }
                            )
                            Text(
                                text = list.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                TodayTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = "Daftar baru"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.confirm(item, newName, onSaved) }) {
                Text(text = "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Batal") }
        }
    )
}
