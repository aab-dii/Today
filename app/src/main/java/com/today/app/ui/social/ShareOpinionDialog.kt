package com.today.app.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.today.app.core.domain.model.DiaryEntry
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing

@Composable
fun ShareOpinionDialog(
    entry: DiaryEntry,
    publishing: Boolean,
    onDismiss: () -> Unit,
    onShare: (note: String, spoiler: Boolean) -> Unit
) {
    var note by remember(entry.id) { mutableStateOf(entry.note) }
    var spoiler by remember(entry.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!publishing) onDismiss() },
        title = { Text(text = "Bagikan opini") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = posterUrl(entry.posterPath),
                        contentDescription = entry.titleName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 48.dp, height = 72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text = entry.titleName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.rating > 0.0) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            RatingDisplay(rating = entry.rating)
                        }
                        entry.mood?.let { mood ->
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Mood: ${mood.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Opini ini akan tampil publik dengan namamu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TodayTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Opini (opsional)",
                    singleLine = false,
                    minLines = 3,
                    enabled = !publishing
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !publishing) { spoiler = !spoiler }
                ) {
                    Checkbox(
                        checked = spoiler,
                        onCheckedChange = { spoiler = it },
                        enabled = !publishing
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Mengandung spoiler",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onShare(note, spoiler) },
                enabled = !publishing
            ) {
                if (publishing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(text = "Bagikan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !publishing) {
                Text(text = "Batal")
            }
        }
    )
}
