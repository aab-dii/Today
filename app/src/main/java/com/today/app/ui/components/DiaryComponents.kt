package com.today.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun RatingDisplay(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val full = rating >= i - 0.25
            val half = !full && rating >= i - 0.75
            Icon(
                imageVector = when {
                    full -> Icons.Filled.Star
                    half -> Icons.AutoMirrored.Filled.StarHalf
                    else -> Icons.Filled.StarBorder
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp
) {
    Row(modifier) {
        for (i in 0 until 5) {
            RatingStar(
                fill = (rating - i).coerceIn(0.0, 1.0),
                starSize = starSize,
                onSet = { fraction -> onRatingChange(i + fraction) }
            )
        }
    }
}

@Composable
private fun RatingStar(
    fill: Double,
    starSize: Dp,
    onSet: (Double) -> Unit
) {
    val icon = when {
        fill >= 0.75 -> Icons.Filled.Star
        fill >= 0.25 -> Icons.AutoMirrored.Filled.StarHalf
        else -> Icons.Filled.StarBorder
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(starSize.coerceAtLeast(MIN_TOUCH_TARGET))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val half = size.width / 2f
                    onSet(if (offset.x < half) 0.5 else 1.0)
                }
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(starSize)
        )
    }
}

private val MIN_TOUCH_TARGET = 48.dp

@Composable
fun PosterTile(
    titleName: String,
    posterPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    badge: String? = null
) {
    Column(
        modifier = modifier
            .width(width)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(width * 1.5f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = posterUrl(posterPath),
                contentDescription = titleName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
            if (badge != null) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = titleName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
