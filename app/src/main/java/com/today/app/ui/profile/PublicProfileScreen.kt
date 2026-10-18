package com.today.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.today.app.core.domain.model.Title
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.TodayEmptyState
import com.today.app.ui.components.TodayPrimaryButton
import com.today.app.ui.components.TodaySectionHeader
import com.today.app.ui.components.TodayStatPill
import com.today.app.ui.components.TodayTonalButton
import com.today.app.ui.components.TodayTopBar
import com.today.app.ui.social.CommentsBottomSheet
import com.today.app.ui.social.OpinionCard
import com.today.app.ui.social.toTitle
import com.today.app.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    targetUid: String,
    fallbackName: String,
    onBack: () -> Unit,
    onTitleClick: (Title) -> Unit,
    viewModel: PublicProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val opinions by viewModel.opinions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedOpinionForComments by remember { mutableStateOf<Opinion?>(null) }

    LaunchedEffect(targetUid, fallbackName) {
        viewModel.initProfile(targetUid, fallbackName)
    }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TodayTopBar(title = state.displayName, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = Spacing.xxl)
        ) {
            item {
                ProfileHeader(
                    state = state,
                    opinionCount = opinions.size,
                    onToggleFollow = { viewModel.toggleFollow() }
                )
            }

            item {
                Spacer(Modifier.height(Spacing.md))
                TodaySectionHeader(title = "Opini Publik (${opinions.size})")
                Spacer(Modifier.height(Spacing.sm))
            }

            if (opinions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        TodayEmptyState(
                            title = "Belum ada opini",
                            subtitle = "Pengguna ini belum membagikan opini publik.",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.RateReview,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            } else {
                items(items = opinions, key = { it.id }) { opinion ->
                    Box(modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs)) {
                        OpinionCard(
                            opinion = opinion,
                            onClick = { onTitleClick(opinion.toTitle()) },
                            isLiked = state.isLiked(opinion.id),
                            onToggleLike = { viewModel.toggleLike(opinion) },
                            onCommentClick = { selectedOpinionForComments = opinion }
                        )
                    }
                }
            }
        }
    }

    selectedOpinionForComments?.let { opinion ->
        CommentsBottomSheet(
            opinion = opinion,
            onDismiss = { selectedOpinionForComments = null }
        )
    }
}

@Composable
private fun ProfileHeader(
    state: PublicProfileUiState,
    opinionCount: Int,
    onToggleFollow: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.screen),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val photoUrl = state.profile?.photoUrl
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = state.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = state.displayName.firstOrNull()?.uppercaseChar()
                    if (initial != null) {
                        Text(
                            text = initial.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = state.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            val bio = state.profile?.bio
            if (!bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            TodayStatPill(
                text = "$opinionCount opini",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            if (state.isSelf) {
                TodayStatPill(text = "Ini profil Anda")
            } else {
                when {
                    state.busyFollow -> CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    state.isFollowing -> TodayTonalButton(
                        text = "Mengikuti",
                        onClick = onToggleFollow,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    else -> TodayPrimaryButton(
                        text = "Ikuti",
                        onClick = onToggleFollow,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
