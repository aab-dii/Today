package com.today.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.core.domain.model.Title
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.TodayChoiceChip
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.social.CommentsBottomSheet
import com.today.app.ui.social.OpinionCard
import com.today.app.ui.social.toTitle
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onBack: () -> Unit,
    onTitleClick: (Title) -> Unit,
    onAuthorClick: ((String, String) -> Unit)? = null,
    viewModel: FeedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val forYou by viewModel.forYou.collectAsStateWithLifecycle()
    val followingFeed by viewModel.followingFeed.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedOpinionForComments by remember { mutableStateOf<Opinion?>(null) }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = "Feed Komunitas", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TodayChoiceChip(
                    text = "Untuk Anda",
                    selected = state.mode == FeedMode.FOR_YOU,
                    onClick = { viewModel.setMode(FeedMode.FOR_YOU) }
                )
                TodayChoiceChip(
                    text = "Mengikuti",
                    selected = state.mode == FeedMode.FOLLOWING,
                    onClick = { viewModel.setMode(FeedMode.FOLLOWING) }
                )
            }

            if (state.busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when (state.mode) {
                FeedMode.FOR_YOU -> {
                    if (forYou.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            TodayEmptyState(
                                title = "Belum ada opini publik",
                                subtitle = "Jadilah yang pertama membagikan opini dari catatan tontonanmu!",
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screen),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(items = forYou, key = { it.id.ifEmpty { "${it.authorUid}-${it.createdAt}" } }) { opinion ->
                                val isSelf = opinion.authorUid == state.session?.uid
                                OpinionCard(
                                    opinion = opinion,
                                    onClick = { onTitleClick(opinion.toTitle()) },
                                    isFollowing = state.isFollowing(opinion.authorUid),
                                    onToggleFollow = if (!isSelf) {
                                        { viewModel.toggleFollow(opinion.authorUid) }
                                    } else null,
                                    onAuthorClick = if (onAuthorClick != null) {
                                        { onAuthorClick(opinion.authorUid, opinion.authorName) }
                                    } else null,
                                    isLiked = state.isLiked(opinion.id),
                                    onToggleLike = { viewModel.toggleLike(opinion) },
                                    onCommentClick = { selectedOpinionForComments = opinion }
                                )
                            }
                        }
                    }
                }

                FeedMode.FOLLOWING -> {
                    val empty = when {
                        state.following.isEmpty() ->
                            "Kamu belum mengikuti siapa pun. Temukan opini menarik di tab \"Untuk Anda\" dan ikuti akun lain!" to "Belum mengikuti siapa pun"
                        followingFeed.isEmpty() ->
                            "Belum ada ulasan baru dari akun yang kamu ikuti." to "Belum ada ulasan baru"
                        else -> null
                    }
                    if (empty != null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            TodayEmptyState(
                                title = empty.second,
                                subtitle = empty.first,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.screen),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(items = followingFeed, key = { it.id.ifEmpty { "${it.authorUid}-${it.createdAt}" } }) { opinion ->
                                val isSelf = opinion.authorUid == state.session?.uid
                                OpinionCard(
                                    opinion = opinion,
                                    onClick = { onTitleClick(opinion.toTitle()) },
                                    isFollowing = state.isFollowing(opinion.authorUid),
                                    onToggleFollow = if (!isSelf) {
                                        { viewModel.toggleFollow(opinion.authorUid) }
                                    } else null,
                                    onAuthorClick = if (onAuthorClick != null) {
                                        { onAuthorClick(opinion.authorUid, opinion.authorName) }
                                    } else null,
                                    isLiked = state.isLiked(opinion.id),
                                    onToggleLike = { viewModel.toggleLike(opinion) },
                                    onCommentClick = { selectedOpinionForComments = opinion }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedOpinionForComments?.let { opinion ->
        CommentsBottomSheet(
            opinion = opinion,
            onDismiss = { selectedOpinionForComments = null },
            onAuthorClick = onAuthorClick
        )
    }
}
