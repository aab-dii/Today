package com.today.app.ui.circle

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.today.app.core.domain.model.Title
import com.today.app.social.domain.model.Opinion
import com.today.app.ui.components.LoadingIndicator
import com.today.app.ui.components.TodayPrimaryButton
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
fun CircleDetailScreen(
    circleId: String,
    onBack: () -> Unit,
    onTitleClick: (Title) -> Unit,
    onAuthorClick: (String, String) -> Unit,
    viewModel: CircleDetailViewModel = koinViewModel()
) {
    LaunchedEffect(circleId) { viewModel.load(circleId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingLeave by remember { mutableStateOf(false) }
    var selectedOpinion by remember { mutableStateOf<Opinion?>(null) }

    LaunchedEffect(state.message, state.error) {
        (state.message ?: state.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.left) {
        if (state.left) onBack()
    }

    val circle = state.circle

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TodayTopBar(
                title = circle?.name ?: "Circle",
                onBack = onBack,
                actions = {
                    if (circle != null) {
                        IconButton(onClick = { context.shareInvite(circle.name, circle.inviteCode) }) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = "Bagikan kode")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            !state.loaded -> LoadingIndicator(
                modifier = Modifier.fillMaxSize().padding(padding)
            )

            circle == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Circle tidak ditemukan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(Spacing.screen),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item(key = "invite") {
                    InviteCard(
                        inviteCode = circle.inviteCode,
                        memberCount = state.members.size,
                        onShare = { context.shareInvite(circle.name, circle.inviteCode) }
                    )
                }

                item(key = "members") {
                    Column {
                        SectionHeader(text = "Anggota (${state.members.size})")
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column(modifier = Modifier.padding(Spacing.sm)) {
                                if (state.members.isEmpty()) {
                                    Text(
                                        text = "Belum ada anggota lain.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(Spacing.sm)
                                    )
                                } else {
                                    state.members.forEach { member ->
                                        MemberRow(
                                            name = member.name,
                                            isOwner = member.uid == circle.ownerUid
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "feed-header") {
                    SectionHeader(text = "Opini anggota")
                }

                if (state.opinions.isEmpty()) {
                    item(key = "feed-empty") {
                        Text(
                            text = "Belum ada opini dari anggota circle ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(items = state.opinions, key = { it.id }) { opinion ->
                        OpinionCard(
                            opinion = opinion,
                            onClick = { onTitleClick(opinion.toTitle()) },
                            onAuthorClick = { onAuthorClick(opinion.authorUid, opinion.authorName) },
                            isLiked = state.isLiked(opinion.id),
                            onToggleLike = { viewModel.toggleLike(opinion) },
                            onCommentClick = { selectedOpinion = opinion }
                        )
                    }
                }

                item(key = "leave") {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    TodayTonalButton(
                        text = "Keluar dari circle",
                        onClick = { pendingLeave = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    selectedOpinion?.let { opinion ->
        CommentsBottomSheet(
            opinion = opinion,
            onDismiss = { selectedOpinion = null }
        )
    }

    if (pendingLeave) {
        AlertDialog(
            onDismissRequest = { pendingLeave = false },
            title = { Text(text = "Keluar dari circle?") },
            text = { Text(text = "Kamu akan berhenti melihat opini anggota circle ini.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.leave()
                    pendingLeave = false
                }) { Text(text = "Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingLeave = false }) { Text(text = "Batal") }
            }
        )
    }
}

@Composable
private fun MemberRow(name: String, isOwner: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isOwner) {
            TodayStatPill(text = "Pemilik")
        }
    }
}

@Composable
private fun InviteCard(
    inviteCode: String,
    memberCount: Int,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Kode undangan",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = inviteCode,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$memberCount anggota",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            TodayPrimaryButton(
                text = "Bagikan kode",
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private fun android.content.Context.shareInvite(name: String, code: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Gabung circle \"$name\" di Today dengan kode undangan: $code"
        )
    }
    startActivity(Intent.createChooser(intent, "Bagikan kode undangan"))
}
