package com.today.app.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.CustomListItem
import com.today.app.core.domain.model.WatchlistItem
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.posterUrl
import com.today.app.ui.lists.AddToListDialog
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onTitleClick: (WatchlistItem) -> Unit,
    viewModel: WatchlistViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var listTarget by remember { mutableStateOf<CustomListItem?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Watchlist") }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TodayEmptyState(
                    title = "Watchlist masih kosong",
                    subtitle = "Tandai judul dengan ikon bookmark di halaman detail untuk menyimpannya di sini.",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Spacing.screen),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                items(items, key = { "${it.mediaType.value}-${it.titleId}" }) { item ->
                    WatchlistTile(
                        item = item,
                        onClick = { onTitleClick(item) },
                        onRemove = { viewModel.remove(item) },
                        onAddToList = {
                            listTarget = CustomListItem(
                                titleId = item.titleId,
                                mediaType = item.mediaType,
                                titleName = item.titleName,
                                posterPath = item.posterPath
                            )
                        }
                    )
                }
            }
        }
    }

    listTarget?.let { target ->
        AddToListDialog(
            item = target,
            onDismiss = { listTarget = null },
            onSaved = { listTarget = null }
        )
    }
}

@Composable
private fun WatchlistTile(
    item: WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAddToList: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = posterUrl(item.posterPath),
                contentDescription = item.titleName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opsi",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(text = "Tambah ke daftar") },
                        onClick = {
                            menuOpen = false
                            onAddToList()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Hapus dari watchlist") },
                        onClick = {
                            menuOpen = false
                            onRemove()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = item.titleName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
