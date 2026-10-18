package com.today.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.core.domain.model.Title
import com.today.app.ui.components.ErrorBanner
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayTextField
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.components.TitleCard
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    onTitleClick: (Title) -> Unit,
    onQuickLog: (Title) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Cari judul", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TodayTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "Cari film, serial, anime…",
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.sm)
            )

            when (val state = uiState) {
                is SearchUiState.Idle -> TodayEmptyState(
                    title = "Cari judul",
                    subtitle = "Ketik minimal ${SearchViewModel.MIN_QUERY_LENGTH} karakter untuk mulai mencari."
                )

                is SearchUiState.Error -> ErrorBanner(
                    message = state.message,
                    onRetry = { viewModel.onQueryChange(query) }
                )

                is SearchUiState.Success -> {
                    if (state.titles.isEmpty()) {
                        TodayEmptyState(
                            title = "Tidak ada hasil",
                            subtitle = "Tidak ada hasil untuk \"$query\"."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = Spacing.screen,
                                vertical = Spacing.sm
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(
                                items = state.titles,
                                key = { "${it.mediaType.value}-${it.id}" }
                            ) { title ->
                                TitleCard(
                                    title = title,
                                    onClick = { onTitleClick(title) },
                                    onQuickLog = { onQuickLog(title) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
