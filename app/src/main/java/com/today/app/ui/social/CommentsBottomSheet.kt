package com.today.app.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.social.domain.model.Comment
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayChoiceChip
import com.today.app.ui.components.TodayDivider
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.formatDay
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    opinion: Opinion,
    onDismiss: () -> Unit,
    onAuthorClick: ((String, String) -> Unit)? = null,
    viewModel: CommentsViewModel = koinViewModel()
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var textInput by remember { mutableStateOf("") }
    var isSpoiler by remember { mutableStateOf(false) }

    LaunchedEffect(opinion.id) {
        viewModel.selectOpinion(opinion)
    }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.dismiss()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = posterUrl(opinion.posterPath),
                    contentDescription = opinion.titleName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 40.dp, height = 60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = opinion.titleName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Opini oleh ${opinion.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (opinion.rating > 0.0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        RatingDisplay(rating = opinion.rating)
                    }
                }
            }

            TodayDivider()

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada komentar.\nJadilah yang pertama berkomentar!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(Spacing.screen),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(items = comments, key = { it.id }) { comment ->
                            CommentItem(
                                comment = comment,
                                isRevealed = comment.id in state.revealedCommentIds,
                                isAuthor = comment.authorUid == state.session?.uid,
                                onAuthorClick = if (onAuthorClick != null) {
                                    { onAuthorClick(comment.authorUid, comment.authorName) }
                                } else null,
                                onToggleSpoiler = { viewModel.toggleSpoiler(comment.id) },
                                onDelete = { viewModel.deleteComment(comment.id) }
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            TodayDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                ) {
                    TodayChoiceChip(
                        text = "Spoiler",
                        selected = isSpoiler,
                        onClick = { isSpoiler = !isSpoiler }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TodayTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = "Tulis komentar...",
                        singleLine = false,
                        minLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    if (state.submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.addComment(textInput, isSpoiler)
                                textInput = ""
                                isSpoiler = false
                            },
                            enabled = textInput.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    isRevealed: Boolean,
    isAuthor: Boolean,
    onAuthorClick: (() -> Unit)? = null,
    onToggleSpoiler: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val authorModifier = if (onAuthorClick != null) {
                    Modifier.weight(1f).clickable(onClick = onAuthorClick)
                } else {
                    Modifier.weight(1f)
                }
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (onAuthorClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = authorModifier
                )
                Text(
                    text = formatDay(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isAuthor) {
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus komentar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            if (comment.spoiler && !isRevealed) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleSpoiler)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Komentar mengandung spoiler. Ketuk untuk membuka.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                if (comment.spoiler) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Spoiler",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onToggleSpoiler) {
                            Text(text = "Sembunyikan", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
