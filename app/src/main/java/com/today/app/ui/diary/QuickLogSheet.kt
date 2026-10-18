package com.today.app.ui.diary

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.today.app.ui.components.RatingBar
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.posterUrl
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogSheet(
    title: Title,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onFullForm: () -> Unit,
    viewModel: QuickLogViewModel = koinViewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    var rating by remember(title.id, title.mediaType) { mutableDoubleStateOf(0.0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxl)
                .padding(bottom = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = posterUrl(title.posterPath),
                        contentDescription = title.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Text(
                text = "Kasih nilai berapa?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            RatingBar(rating = rating, onRatingChange = { rating = it })

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodayTonalButton(
                    text = "Isi lengkap",
                    onClick = onFullForm,
                    modifier = Modifier.weight(1f)
                )
                TodayPrimaryButton(
                    text = "Simpan",
                    onClick = { viewModel.save(title, rating, onSaved) },
                    enabled = rating > 0.0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
