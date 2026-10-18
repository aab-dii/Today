package com.today.app.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.today.app.core.domain.model.Title
import com.today.app.ui.components.ErrorBanner
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.TitleCard
import com.today.app.ui.components.TodayPosterBox
import com.today.app.ui.components.TodaySectionHeader
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun CatalogScreen(
    onTitleClick: (Title) -> Unit,
    onSearchClick: () -> Unit,
    onQuickLog: (Title) -> Unit,
    viewModel: CatalogViewModel = koinViewModel()
) {
    val titles = viewModel.titles.collectAsLazyPagingItems()
    val rowsState by viewModel.rowsState.collectAsStateWithLifecycle()
    val refreshState = titles.loadState.refresh

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Jelajah") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = Spacing.xl)
        ) {
            item {
                SearchBarButton(
                    onClick = onSearchClick,
                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.sm)
                )
            }

            when (val state = rowsState) {
                is CatalogRowsState.Loading -> item { LoadingIndicator() }

                is CatalogRowsState.Failed -> item {
                    ErrorBanner(
                        message = state.message,
                        onRetry = { viewModel.loadRows() }
                    )
                }

                is CatalogRowsState.Ready -> {
                    state.sections.forEach { section ->
                        item(key = "header-${section.row.name}") { Spacer(Modifier.height(Spacing.lg)) }
                        item(key = "title-${section.row.name}") {
                            TodaySectionHeader(title = section.row.title)
                        }
                        item(key = "row-${section.row.name}") {
                            Spacer(Modifier.height(Spacing.md))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Spacing.screen),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                items(
                                    items = section.titles,
                                    key = { "${it.mediaType.value}-${it.id}" }
                                ) { title ->
                                    TodayPosterBox(
                                        titleName = title.name,
                                        posterPath = title.posterPath,
                                        onClick = { onTitleClick(title) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "gap-semua") { Spacer(Modifier.height(Spacing.sectionGap)) }
            item(key = "header-semua") { TodaySectionHeader(title = "Semua") }
            item(key = "gap-semua-2") { Spacer(Modifier.height(Spacing.md)) }

            if (refreshState is LoadState.Loading && titles.itemCount == 0) {
                item { LoadingIndicator() }
            } else if (refreshState is LoadState.Error && titles.itemCount == 0) {
                item {
                    ErrorBanner(
                        message = refreshState.error.localizedMessage ?: "Gagal memuat katalog",
                        onRetry = { titles.retry() }
                    )
                }
            } else {
                items(
                    count = titles.itemCount,
                    key = titles.itemKey { "${it.mediaType.value}-${it.id}" }
                ) { index ->
                    val title = titles[index]
                    if (title != null) {
                        TitleCard(
                            title = title,
                            onClick = { onTitleClick(title) },
                            onQuickLog = { onQuickLog(title) },
                            modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs)
                        )
                    }
                }

                when (val append = titles.loadState.append) {
                    is LoadState.Loading -> item { LoadingIndicator() }
                    is LoadState.Error -> item {
                        ErrorBanner(
                            message = append.error.localizedMessage ?: "Gagal memuat",
                            onRetry = { titles.retry() }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun SearchBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                text = "Cari film, serial, anime…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
