package com.today.app.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.today.app.core.domain.model.Title
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.RatingDisplay
import com.today.app.ui.components.TodayDivider
import com.today.app.ui.components.formatDay
import com.today.app.ui.components.label
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing

@Composable
fun OpinionCard(
    opinion: Opinion,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    isFollowing: Boolean = false,
    onToggleFollow: (() -> Unit)? = null,
    onAuthorClick: (() -> Unit)? = null,
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
    onCommentClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var spoilerRevealed by remember(opinion.id) { mutableStateOf(false) }

    val cardModifier = if (onClick != null) {
        modifier.fillMaxWidth().clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Surface(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column {
            if (onToggleFollow != null || onAuthorClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm)
                ) {
                    val authorModifier = if (onAuthorClick != null) {
                        Modifier.weight(1f).clickable(onClick = onAuthorClick)
                    } else {
                        Modifier.weight(1f)
                    }
                    Text(
                        text = opinion.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (onAuthorClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = authorModifier
                    )
                    if (onToggleFollow != null) {
                        TextButton(onClick = onToggleFollow) {
                            Text(
                                text = if (isFollowing) "Berhenti ikuti" else "Ikuti",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                TodayDivider()
            }

            Row(modifier = Modifier.padding(Spacing.lg)) {
                AsyncImage(
                    model = posterUrl(opinion.posterPath),
                    contentDescription = opinion.titleName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 60.dp, height = 90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = opinion.titleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "${opinion.mediaType.label()} · ${formatDay(opinion.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (opinion.rating > 0.0) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        RatingDisplay(rating = opinion.rating)
                    }

                    opinion.mood?.let { mood ->
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "Mood: ${mood.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (opinion.spoiler && !spoilerRevealed) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { spoilerRevealed = true }
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
                                    text = "Opini mengandung spoiler. Ketuk untuk membuka.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        if (opinion.spoiler) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                TextButton(onClick = { spoilerRevealed = false }) {
                                    Text(text = "Sembunyikan", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (opinion.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = opinion.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus opini",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (onToggleLike != null || onCommentClick != null) {
                TodayDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.sm, vertical = 2.dp)
                ) {
                    if (onToggleLike != null) {
                        TextButton(
                            onClick = onToggleLike,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isLiked) "Batal suka" else "Suka",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (opinion.likeCount > 0) "${opinion.likeCount}" else "Suka",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    if (onCommentClick != null) {
                        TextButton(
                            onClick = onCommentClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = "Komentar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (opinion.commentCount > 0) "${opinion.commentCount}" else "Komentar",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Opinion.toTitle(): Title = Title(
    id = titleId,
    mediaType = mediaType,
    name = titleName,
    originalName = "",
    country = "",
    language = "",
    genre = "",
    releaseDate = "",
    rating = 0.0,
    popularity = 0.0,
    overview = "",
    posterPath = posterPath,
    backdropPath = null
)
